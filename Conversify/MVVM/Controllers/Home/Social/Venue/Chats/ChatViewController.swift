//
//  ChatViewController.swift
//  Conversify
//
//  Created by Apple on 02/11/18.
//

import UIKit
import RxDataSources
import IQKeyboardManagerSwift
import EZSwiftExtensions
import Lightbox
import AVFoundation
import AVKit
import ESPullToRefresh
import GrowingTextView
import GiphyUISDK
//import GiphyCoreSDK
import OpenTok
import iRecordView

enum chatType {
    case oneToOne
    case venue
    case oneToMany
    case none
}

class ChatViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableViewUsers: UITableView!
    @IBOutlet weak var btnDetailGroup: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var labelReceiverName: UILabel!
    @IBOutlet weak var labelNoData: UILabel!
    @IBOutlet weak var tableView: ChatTable!{
        didSet {
            tableView.registerXIB(MessageType.Text.cellID(isOwn: true))
            tableView.registerXIB(MessageType.Text.cellID(isOwn: false))
            tableView.registerXIB(MessageType.Image.cellID(isOwn: false))
            tableView.registerXIB(MessageType.Image.cellID(isOwn: true))
            tableView.registerXIB(MessageType.Audio.cellID(isOwn: true))
            tableView.registerXIB(MessageType.Audio.cellID(isOwn: false))
            tableView.registerXIBForHeaderFooter("DateHeaderView")
        }
    }
    @IBOutlet weak var imgUser: UIImageView!
    
    @IBOutlet weak var vwChatAssessery: ChatAccessory!
    
    @IBOutlet weak var bottomContstraint: NSLayoutConstraint!
    
    
    //MARK::- PROPERTIES
    var chatArray : [String]? = []
    var chatModal = ChatViewModal()
    var searchUsers: [Members] = []
    var dataSource: RxTableViewSectionedReloadDataSource<ChatDataSec>?
    var chatingType : chatType?
    var receiverData : Any?
    var isPush = false
    var isFromChat = false
    
    //MARK::- VC LIFE CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Giphy.configure(apiKey: Keys.giphyKey)
        
        isLoginSignp = false
        addPaging(isPush: false)
        onLoad(isPush: false)
        
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector:  #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        self.tableView.keyboardDismissMode = .interactive
        vwChatAssessery.createRecorder()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        Singleton.sharedInstance.isTopIsChatController = true
        IQKeyboardManager.shared.enable = false
        IQKeyboardManager.shared.enableAutoToolbar = false
        //        if chatModal.arrayOfChat.value.count != 0{
        tableView.becomeFirstResponder()
        //        }
        
    }
    
    
    override func viewDidDisappear(_ animated: Bool) {
        Singleton.sharedInstance.isTopIsChatController = false
        IQKeyboardManager.shared.enableAutoToolbar = true
        IQKeyboardManager.shared.enable = true
    }
    
    deinit {
        tableView?.removeObserver(self, forKeyPath: "contentInset")
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "contentInset" {
            if let insets = change?[NSKeyValueChangeKey.newKey] as? UIEdgeInsets {
                tableViewUsers.contentInset = insets
            }
        }
    }
    
    override func viewDidLayoutSubviews() {
        updateTextVw()
    }
    
    @IBAction func actionCall(_ sender: Any) {
       
        var receiverId = String()
        var receiverName = String()
        
        if let receiverData = receiverData as? UserList {
            receiverId = /receiverData.id
            receiverName = /receiverData.fullName
        } else if let receiverData  = receiverData as? User {
            receiverId = /receiverData.id
            receiverName = "\(/receiverData.firstName) \(/receiverData.lastName)"
        }
        guard let vc = R.storyboard.main.voipViewController() else { return }
        vc.caller = Caller(name: receiverName, userId: receiverId , userImg : imgUser.image)
        vc.modalPresentationStyle = .overFullScreen
        self.present(vc, animated: true, completion: nil)
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        
        btnDetailGroup.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            if !(/self?.isFromChat) { return }
            switch self?.chatingType ?? .none{
            case .oneToMany :
                guard let vc = R.storyboard.groups.addGroupChatParticipantsVC() else { return }
                vc.backPressed = { [weak self] in
                    SocketIOManager.shared.addHandlers()
                    SocketIOManager.shared.checkSocketConnection()
                    self?.tableView.becomeFirstResponder()
                }
                vc.chatVM = ChatViewModal(membersData: self?.chatModal.groupChatData.value , venueD: self?.chatModal.venue.value )
                vc.chatVM.updateGroupDetails = { [weak self] groupDetail in
                    self?.labelReceiverName.text = groupDetail?.groupName
                }
                vc.chatVM.exit = { [weak self] in
                    self?.chatModal.exit?()
                }
                self?.pushVC(vc)
            case .venue :
                guard let vc = R.storyboard.venue.venueGroupChatDetailViewController() else { return }
                vc.chatVM = ChatViewModal(membersData: self?.chatModal.groupChatData.value , venueD: self?.chatModal.venue.value )
                vc.chatVM.updateVenueDetails = { [weak self] venue in
                    self?.chatModal.groupChatData.value?.venueTitle = /venue?.venueTitle?.uppercaseFirst
                    self?.chatModal.venue.value = venue
                    self?.labelReceiverName.text = /venue?.venueTitle?.uppercaseFirst
                }
                vc.chatVM.exit = { [weak self] in
                    self?.chatModal.exit?()
                }
                self?.pushVC(vc)
            default :
                guard let vc = R.storyboard.home.profileViewController() else { return }
                vc.profileVM.userType = .otherUser
                if let receiverData = self?.receiverData as? UserList {
                    vc.profileVM.userId = receiverData.id
                }else if let receiverData  = self?.receiverData as? User{
                    vc.profileVM.userId = receiverData.id
                }
                //                if Singleton.sharedInstance.loggedInUser?.id == vc.profileVM.userId{
                //                    return
                //                }
                UIApplication.topViewController()?.pushVC(vc)
            }
            
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            SocketIOManager.shared.disConnectMsgEvent()
            self?.chatModal.backRefresh?()
            AppDelegate.shared?.senderPlayer?.pause()
            AppDelegate.shared?.receiverPlayer?.pause()
            self?.popVC()
        })<bag
        
        dataSource = RxTableViewSectionedReloadDataSource<ChatDataSec>(configureCell: { (_, tableView, indexPath, element) -> UITableViewCell in
            let chatInfo = element as? ChatData
            var cell = UITableViewCell()
            switch chatInfo?.mesgDetail?.type {
            case MsgType.txt.rawValue :
                cell = tableView.dequeueReusableCell(withIdentifier: /chatInfo?.isOwnMessage ?  "SenderTxtCell" : "ReceiverTxtCell", for: indexPath)
            case MsgType.audio.rawValue :
                cell = tableView.dequeueReusableCell(withIdentifier: /chatInfo?.isOwnMessage ?  "SenderAudioCell" : "ReceiverAudioCell", for: indexPath)
                
            default :
                cell = tableView.dequeueReusableCell(withIdentifier: /chatInfo?.isOwnMessage ?  "SenderImgCell" : "ReceiverImgCell", for: indexPath)
            }

            let item = chatInfo
            (cell as? SenderImgCell)?.indexPath = indexPath
            (cell as? SenderImgCell)?.row = indexPath.row
            (cell as? SenderImgCell)?.sec = indexPath.section
            (cell as? SenderImgCell)?.delegate = self
            (cell as? SenderImgCell)?.delegatePlay = self
            (cell as? ReceiverImgCell)?.delegatePlay = self
            (cell as? ReceiverImgCell)?.row = indexPath.row
            (cell as? ReceiverImgCell)?.sec = indexPath.section
            
            switch /item?.mesgDetail?.type {
            case "AUDIO" :
                if /item?.isOwnMessage {
                    (cell as? SenderAudioCell)?.item = item
                    (cell as? SenderAudioCell)?.viewBase.addLongPressGesture { [weak self] (gesture) in
                        print(indexPath.section)
                        print(indexPath.row)
                        if /item?.id == ""{
                            return
                        }
                        self?.deleteMessageAtBothEnd(item:item, indexPath:indexPath)
                        print("message pressed")
                        
                       
                    }
                }else{
                        (cell as? ReceiverAudioCell)?.item = item
                    
                }
            case "TEXT":
                if /item?.isOwnMessage {
                    (cell as? SenderTxtCell)?.item = item
                    (cell as? SenderTxtCell)?.viewBase.addLongPressGesture { [weak self] (gesture) in
                        print(indexPath.section)
                        print(indexPath.row)
                        if /item?.id == ""{
                            return
                        }
                        self?.deleteMessageAtBothEnd(item:item, indexPath:indexPath)
                        print("message pressed")
                    }
                } else {
                    (cell as? ReceiverTxtCell)?.item = item
                }
            default:
                if /item?.isOwnMessage {
                    (cell as? SenderImgCell)?.item = item
                    (cell as? SenderImgCell)?.imgView.addLongPressGesture { [weak self] (gesture) in
                        if /item?.id == ""{
                            return
                        }
                        self?.deleteMessageAtBothEnd(item:item, indexPath:indexPath)
                        print("message pressed")
                    }
                    (cell as? SenderImgCell)?.viewVideoDim.addLongPressGesture { [weak self] (gesture) in
                        if /item?.id == ""{
                            return
                        }
                        self?.deleteMessageAtBothEnd(item:item, indexPath:indexPath)
                        print("message pressed")
                    }
                } else {
                    (cell as? ReceiverImgCell)?.item = item
                }
            }
            if indexPath.row != 0{
                let lastChatObj = self.chatModal.arrayOfChat.value[indexPath.section].items[indexPath.row - 1] as? ChatData
                let currentObj = self.chatModal.arrayOfChat.value[indexPath.section].items[indexPath.row] as? ChatData
                (cell as? ReceiverTxtCell)?.imgView.isHidden = /lastChatObj?.senderId?.id == /currentObj?.senderId?.id
                (cell as? ReceiverTxtCell)?.constraintHeightName.constant = /lastChatObj?.senderId?.id == /currentObj?.senderId?.id ? 0 : 32
                (cell as? ReceiverImgCell)?.imageUser.isHidden = /lastChatObj?.senderId?.id == /currentObj?.senderId?.id
                
            }else{
                (cell as? ReceiverTxtCell)?.constraintHeightName.constant = 32
                (cell as? ReceiverTxtCell)?.imgView.isHidden = false
                (cell as? ReceiverImgCell)?.imageUser.isHidden = false
            }
            
            if self.chatingType == .oneToOne {
                (cell as? ReceiverTxtCell)?.constraintHeightName.constant = 0
                (cell as? ReceiverTxtCell)?.constraintHeightImg.constant = 0
                (cell as? ReceiverTxtCell)?.imgLeadingContsraint.constant = 0
                (cell as? ReceiverTxtCell)?.contsraintWidthImg.constant = 0
                
                (cell as? ReceiverImgCell)?.constraintWidthImg.constant = 0
                (cell as? ReceiverImgCell)?.leadingConstraintImg.constant = 0
                (cell as? ReceiverImgCell)?.constraintHeightImg.constant = 0
                
                (cell as? ReceiverTxtCell)?.imgView.isHidden = true
                (cell as? ReceiverImgCell)?.imageUser.isHidden = true
            }
            
            return cell
        })
        
        guard let safeDatasource = dataSource else{return}
        chatModal.arrayOfChat.asObservable().bind(to: tableView.rx.items(dataSource: safeDatasource))<bag
        tableView.rx.setDelegate(self)<bag
    }
    
    func updateTextVw(){
        //        vwChatAssessery.layoutIfNeeded()
        //        (vwChatAssessery.textView).forceLayoutSubviews()
    }
    
}

//MARK::- CUSTOM METHODS
extension ChatViewController {
    
    func addPaging(isPush: Bool){
        
        tableView.es.addPullToRefresh {
            if self.chatModal.loadMore {
                self.chatModal.chatId.value = (self.chatModal.arrayOfChat.value.first?.items.first as? ChatData)?.id
                switch self.chatingType ?? .none{
                case .oneToOne , .oneToMany :
                    self.getIndividualChatHistory(isPull: true, isPush: false)
                case .venue :
                    self.getGroupChatconversation(isPull: true,isPush: isPush)
                default : break
                }
            }else {
                self.tableView.es.stopPullToRefresh()
            }
        }
    }
    
    func onLoad(isPush: Bool){
        
        Singleton.sharedInstance.isTopIsChatController = true
        vwChatAssessery.typeOfChat = chatingType
        tableViewUsers.tableFooterView = UIView.init(frame: CGRect.zero)
        tableView.addObserver(self, forKeyPath: "contentInset", options: .new, context: nil)
        switch chatingType ?? .none {
        case .venue :
            getGroupChatconversation(isPush: isPush)
        case .oneToOne:
            getOneToOneChatConversation(isPush: isPush)
        case .oneToMany :
            getOneToOneChatConversation(isPush: isPush)
        default:
            break
        }
        SocketIOManager.shared.addHandlers()
        SocketIOManager.shared.checkSocketConnection()
        setUpSocketListner()
        vwChatAssessery.groupId = chatModal.groupIdToJoin.value
        
        if chatingType == .oneToOne{
            vwChatAssessery.gotConvoId = { [weak self] chatData in
                self?.chatModal.conversationId.value = /chatData.conversationId
                self?.chatModal.assignConvoId?(/chatData.conversationId)
            }
        }
    }
    
    func socketSetUp(){
        
    }
    
}


//MARK::- API CALLS
extension ChatViewController {
    
    func getIndividualChatHistory(isPull: Bool = false , isPush: Bool){
        self.view.endEditing(true)
        chatModal.getChatConversation(isPush: isPush) { [unowned self] (completed) in
            self.tableView.es.stopPullToRefresh()
            if completed {
                self.tableView.becomeFirstResponder()
                self.vwChatAssessery.delegate = self
                self.labelNoData.isHidden = self.chatModal.arrayOfChat.value.count != 0
                if self.chatModal.arrayOfChat.value.count != 0 && !isPull{
                    ez.runThisInMainThread({
                        let indx =  IndexPath(row: /self.chatModal.arrayOfChat.value.last?.items.count - 1, section: self.chatModal.arrayOfChat.value.count - 1)
                        self.tableView.scrollToRow(at: indx, at: .bottom, animated: true)
                    })
                }
            }
        }
    }
    
    func getOneToOneChatConversation(isPush: Bool){
        
        if let receiverData = receiverData as? UserList {
            
            labelReceiverName.text = receiverData.userName
            imgUser.image(url:  /receiverData.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /receiverData.imageUrl?.thumbnail))
            vwChatAssessery.receiverData = receiverData
            
        }else if let receiverData  = receiverData as? User{
            
            labelReceiverName.text = /receiverData.groupName == "" ? receiverData.userName : receiverData.groupName
            imgUser.image(url:  /receiverData.img?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /receiverData.img?.thumbnail))
            vwChatAssessery.receiverData = receiverData
        }
        getIndividualChatHistory(isPush: isPush)
    }
    
    func updateDetails(name: String , image: String){
        labelReceiverName.text = name
        imgUser.image(url:  /image, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /image))
    }
    
    func getGroupChatconversation(isPull: Bool = false , isPush: Bool){
        self.view.endEditing(true)
        chatModal.getVenueConversation(isPush: isPush) { [unowned self] (completed) in
            self.tableView.es.stopPullToRefresh()
            if completed {
                self.tableView.becomeFirstResponder()
                self.vwChatAssessery.delegate = self
                self.labelNoData.isHidden = self.chatModal.arrayOfChat.value.count != 0
                if self.chatModal.arrayOfChat.value.count != 0 && !isPull{
                    ez.runThisInMainThread({
                        let indx =  IndexPath(row: /self.chatModal.arrayOfChat.value.last?.items.count - 1, section: self.chatModal.arrayOfChat.value.count - 1)
                        self.tableView.scrollToRow(at: indx, at: .bottom, animated: true)
                    })
                }
                self.labelReceiverName.text = /self.chatModal.groupChatData.value?.venueTitle?.uppercaseFirst
                self.imgUser.image(url:  /self.chatModal.groupChatData.value?.grpImg?.thumbnail,placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /self.chatModal.groupChatData.value?.grpImg?.thumbnail))
                
                self.searchUsers = /self.chatModal.groupChatData.value?.groupMembers
                self.tableViewUsers.reloadData()
            }
        }
    }
}

//MARK::- TABLEVIEW DATASOURCE
extension ChatViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return searchUsers.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "SimpleUserCell") as! SimpleUserCell
        cell.user = searchUsers[indexPath.row].user
        return cell
    }
}

//MARK::- TABLEVIEW DELEGATES
extension ChatViewController : UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if tableView == tableViewUsers {
            let user = searchUsers[indexPath.row]
            self.vwChatAssessery.setTag(member: user)
            hideUsersTable()
            return
        }
        
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if tableView == tableViewUsers {
            return 50
        }
        return /(chatModal.arrayOfChat.value[indexPath.section].items[indexPath.row] as? ChatData)?.mesgDetail?.typeOfMsg?.height
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if tableView == tableViewUsers {
            return 0
        }
        return 50.0
    }
    
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if tableView == tableViewUsers {
            return nil
        }
        if let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: "DateHeaderView"){
            
            let sectionDate = chatModal.arrayOfChat.value[section].header
            (header as? DateHeaderView)?.labelDate.text =  Date(fromString: sectionDate, format: "EEEE . MMM d yyyy")?.toString(.custom("EEEE . MMM d"))
            return header
        }else {
            return nil
        }
    }
    
}


//MARK::- CHATACCESSORY DELEGATE
extension ChatViewController : SendMessagedDelegate {
    
    func showUsersTable() {
        if self.vwChatAssessery.searchUserText().isEmpty {
            self.searchUsers = []
            self.chatModal.groupChatData.value?.groupMembers?.forEach({ (member) in
                if member.user?.id != Singleton.sharedInstance.loggedInUser?.id{
                    self.searchUsers.append(member)
                }
            })
        }
        else {
            let users = self.chatModal.groupChatData.value?.groupMembers?.filter({ (member) -> Bool in
                return member.user?.userName?.contains(self.vwChatAssessery.searchUserText(), compareOption: .caseInsensitive) ?? false
            }) ?? []
            self.searchUsers = []
            users.forEach { (member) in
                if member.user?.id != Singleton.sharedInstance.loggedInUser?.id{
                    self.searchUsers.append(member)
                }
            }
        }
        tableViewUsers.reloadData()
        tableViewUsers.isHidden = false
    }
    
    func hideUsersTable() {
        tableViewUsers.isHidden = true
    }
    
    func updateMessage(with message: ChatData){
        for (index,messag) in (self.chatModal.arrayOfChat.value.last?.items.enumerated())!{
            if (messag as? ChatData)?.mesgDetail?.messageID == message.mesgDetail?.messageID{
                (self.chatModal.arrayOfChat.value.last?.items[index] as? ChatData)?.id = message.id
                break
            }
        }
        DispatchQueue.main.async { [ weak self] in
            self?.tableView.reloadData()
        }
    }
    
    func updateUploadingFail(with message: ChatData){
        
        DispatchQueue.global(qos: .userInitiated).async { [ weak self] in
            
            for (index,messag) in (self?.chatModal.arrayOfChat.value.last?.items.enumerated())!{
                if (messag as? ChatData)?.mesgDetail?.messageID == message.mesgDetail?.messageID{
                    (self?.chatModal.arrayOfChat.value.last?.items[index] as? ChatData)?.mesgDetail?.isUploaded = false
                    (self?.chatModal.arrayOfChat.value.last?.items[index] as? ChatData)?.mesgDetail?.isFail = true
                    break
                }
            }
            DispatchQueue.main.async {
                self?.tableView.reloadData()
                let indx =  IndexPath(row: /self?.chatModal.arrayOfChat.value.last?.items.count - 1, section: /self?.chatModal.arrayOfChat.value.count - 1)
                self?.tableView.scrollToRow(at: indx, at: .bottom, animated: false)
            }
        }
        
    }
    
    
    
    func updateUploadingCompleted(with message: ChatData) {
        
        DispatchQueue.global(qos: .userInitiated).async { [ weak self] in
            
            for (index,messag) in (self?.chatModal.arrayOfChat.value.last?.items.enumerated())!{
                if (messag as? ChatData)?.mesgDetail?.messageID == message.mesgDetail?.messageID{
                    (self?.chatModal.arrayOfChat.value.last?.items[index] as? ChatData)?.mesgDetail?.isUploaded = true
                    (self?.chatModal.arrayOfChat.value.last?.items[index] as? ChatData)?.mesgDetail?.isFail = false
                    break
                }
            }
            DispatchQueue.main.async {
                self?.tableView.reloadData()
                let indx =  IndexPath(row: /self?.chatModal.arrayOfChat.value.last?.items.count - 1, section: /self?.chatModal.arrayOfChat.value.count - 1)
                self?.tableView.scrollToRow(at: indx, at: .bottom, animated: false)
            }
        }
        
    }
    
    
    func appendNewMsg(msgObj : ChatData?) {
        self.labelNoData.isHidden = true
        makeSectionalData(msgObj: msgObj)
    }
    
    func makeSectionalData(msgObj : ChatData?){
        if msgObj?.date == chatModal.arrayOfChat.value.last?.header{
            DispatchQueue.main.async {
                let lastSec = self.chatModal.arrayOfChat.value.count ?? 1
                self.chatModal.arrayOfChat.value[lastSec - 1].items.append(msgObj)
                self.tableView.reloadData()
                if /self.chatModal.arrayOfChat.value.count > 0 {
                    self.tableView.scrollToRow(at: IndexPath(row: /self.chatModal.arrayOfChat.value.last?.items.count - 1, section: /self.chatModal.arrayOfChat.value.count - 1), at: .bottom, animated: false)
                }
            }
        }else{
            DispatchQueue.global(qos: .userInitiated).async { [ weak self] in
                self?.insertChat(msgObj: msgObj)
                DispatchQueue.main.async {
                    self?.reloadRow(msgObj: msgObj)
                }
            }
        }
    }
    
    func insertChat(msgObj : ChatData?){
        DispatchQueue.main.async { [weak self] in
            self?.labelNoData.isHidden = true
        }
        let chatArr = ChatDataSec(header: /msgObj?.date, items: [msgObj] )
        chatModal.arrayOfChat.value.append(chatArr)
    }
    
    func reloadRow(msgObj : ChatData?){
        self.tableView.reloadRows(at: [IndexPath.init(row: /chatModal.arrayOfChat.value.last?.items.count - 1, section: chatModal.arrayOfChat.value.count - 1)], with: .none)
        if chatModal.arrayOfChat.value.count > 0 {
            let row = (chatModal.arrayOfChat.value.last?.items.count == 0) ? 0 : (/chatModal.arrayOfChat.value.last?.items.count - 1)
            let indx =  IndexPath(row: row, section: chatModal.arrayOfChat.value.count - 1)
            self.tableView.scrollToRow(at: indx, at: .bottom, animated: false)
        }
    }
    
}

//MARK::- SOCKETS METHODS

extension ChatViewController {
    
    //MARK::- LISTENERS , LISTEN MESSAGE FROM SERVER
    func setUpSocketListner(){
        
        SocketIOManager.shared.messageFromServer { [weak self]  (msg) in
            if /self?.chatModal.conversationId.value == ""{
                //venue chat
                if /self?.chatModal.groupIdToJoin.value != /msg?.groupId{
                    return
                }
            }else{
                //1-1 or many - 1
                if /self?.chatModal.conversationId.value != /msg?.conversationId{
                    return
                }
            }
            self?.makeSectionalData(msgObj: msg)
        }
        
        SocketIOManager.shared.messageFromServerDeleteChat { [weak self] (deleteChat) in
            
            switch chatGroupType(rawValue: /deleteChat?.type) ?? .individual{
            case .group:
                if self?.chatModal.groupIdToJoin.value == /deleteChat?.groupId{
                    deleteMessage(messageId: /deleteChat?.messageId)
                }
            case .venue:
                if self?.chatModal.groupIdToJoin.value == /deleteChat?.groupId{
                    deleteMessage(messageId: /deleteChat?.messageId)
                }
            case .individual:
                var receiverId = ""
                if let receiverData = self?.receiverData as? UserList {
                    receiverId = /receiverData.id
                }else if let receiverData  = self?.receiverData as? User{
                    receiverId = /receiverData.id
                }
                if /receiverId == /deleteChat?.senderId{
                    deleteMessage(messageId: /deleteChat?.messageId)
                }
            }
            self?.updateTextVw()
        }
        
        func deleteMessage(messageId: String){
            var selectedSectionIndex = -1
            var selectedRowIndex = -1
            chatModal.arrayOfChat.value.forEachEnumerated({ (index, chat) in
                (chat.items as? [ChatData])?.forEachEnumerated({ (ind, chatData) in
                    if chatData.id ==  /messageId {
                        selectedRowIndex = ind
                        selectedSectionIndex = index
                    }
                })
                if selectedSectionIndex != -1{
                    self.chatModal.arrayOfChat.value[selectedSectionIndex].items.remove(at: selectedRowIndex)
                    self.tableView.reloadData()
                }
            })
            self.updateTextVw()
        }
        
    }
}


//MARK::- SHOW IMAGE, VIDEO, DOCS
extension ChatViewController  {
    
    func deleteMessageAtBothEnd(item: ChatData? , indexPath: IndexPath){
        vwChatAssessery.textView.resignFirstResponder()
        
        UtilityFunctions.showWithCancel(nativeActionSheet: "Select action", subTitle: "", vc: UIApplication.shared.topMostViewController(), senders: ["Delete message"], success: { (value, index) in
            switch index{
            case 0:
                var msgToSocket = [String:String]()
                msgToSocket["messageId"] = /item?.id
                switch self.chatingType ?? .none {
                case .venue :
                    msgToSocket["groupId"] = /self.chatModal.groupIdToJoin.value
                    msgToSocket["type"] = "VENUE"
                case .oneToOne:
                    if let receiverData = self.receiverData as? UserList {
                        msgToSocket["receiverId"] = receiverData.id
                    }else if let receiverData  = self.receiverData as? User{
                        msgToSocket["receiverId"] = receiverData.id
                    }
                    msgToSocket["senderId"] = /Singleton.sharedInstance.loggedInUser?.id
                    msgToSocket["type"] = "INDIVIDUAL"
                case .oneToMany :
                    msgToSocket["groupId"] = /self.chatModal.groupIdToJoin.value
                    msgToSocket["type"] = "GROUP"
                default:
                    break
                }
                self.tableView.becomeFirstResponder()
                SocketIOManager.shared.deleteMessage(indexPath : indexPath , data: msgToSocket ) { [weak self] (status , convoId) in
                    print("deleting item at")
                    print(indexPath.row)
                    
                    self?.chatModal.arrayOfChat.value[indexPath.section].items.remove(at: (indexPath.row))
                    self?.tableView.reloadData()
                    self?.vwChatAssessery.layoutIfNeeded()
                    self?.vwChatAssessery.textView.invalidateIntrinsicContentSize()
                }
            default:
                break
            }
        }, cancel: { [ weak self] in
            self?.tableView.becomeFirstResponder()
        })
        
        
    }
    
    func playLocalVideo(_ urlString: String) {
        let videoURL = URL.init(string: urlString)!
        let player = AVPlayer(url: videoURL)
        let playerViewController = AVPlayerViewController()
        playerViewController.player = player
        self.present(playerViewController, animated: true) {
            playerViewController.player!.play()
        }
    }
    
    func showLightbox(video: MessageVideo? , image: MessageImage? , isVideo: Bool) {
        var data = [LightboxImage]()
        if isVideo{
            if let _ : UIImage = video?.thumbnail as? UIImage {
                self.playLocalVideo(/video?.url)
                return
            } else {
                guard let imgUrl = URL.init(string: /(image?.thumbnail as? String)) , let videoUrl = URL.init(string: /(video?.thumbnail as? String)) else { return }
                
                data = [ LightboxImage(
                    imageURL: imgUrl ,
                    text: "",
                    videoURL: videoUrl)
                ]
            }
        }else{
            if let image: UIImage = image?.thumbnail as? UIImage {
                data = [ LightboxImage(
                    image: image,
                    text: ""
                    )]
            } else {
                guard let imgUrl = URL.init(string: /(image?.thumbnail as? String)) else { return }
                data = [LightboxImage(imageURL: imgUrl)]
            }
        }
        
        let controller = LightboxController(images: data )
        controller.dynamicBackground = true
        controller.modalPresentationStyle = .fullScreen
        self.presentVC(controller)
        //        UIApplication.topViewController()?.presentVC(controller)
        //        present(controller, animated: true, completion: nil)
    }
    
    
    func showGif(video: MessageVideo? , image: MessageImage? ) {
        
        guard let imgUrl = URL.init(string: /(image?.thumbnail as? String)) else { return }
        guard let vc = R.storyboard.chats.showGifViewController() else {return}
        vc.imgUrl = imgUrl
        vc.modalPresentationStyle = .fullScreen
        
        present(vc , animated: true, completion: nil)
    }
}


//MARK::- CELL ACTIONS
extension ChatViewController : DelegateUploadItem , DelegateImagePlay  {
    
    override func canPerformAction(_ action: Selector, withSender sender: Any?) -> Bool {
        return super.canPerformAction(action, withSender: sender)
        
    }
    
    override var canBecomeFirstResponder: Bool {
        return true
    }
    
    @objc func saveTapped() {
        print("save tapped")
        
    }
    
    @objc func deleteTapped() {
        print("delete tapped")
    }
    
    func playVideo(sec: Int , row: Int){
        self.view.endEditing(true)
        let item = (chatModal.arrayOfChat.value[sec].items[row] as? ChatData)?.mesgDetail
        switch item?.typeOfMsg ?? .txt {
        case .txt:
            break
        case .img:
            
            //            let selectedIndexPath = IndexPath(row: row, section: sec)
            //            let cell = tableView.cellForRow(at: selectedIndexPath)
            //
            //            let saveMenuItem = UIMenuItem(title: "Save", action: #selector(saveTapped))
            //            let deleteMenuItem = UIMenuItem(title: "Delete", action: #selector(deleteTapped))
            //            UIMenuController.shared.menuItems = [saveMenuItem, deleteMenuItem]
            //
            //            // Tell the menu controller the first responder's frame and its super view
            //            if let vw = cell as? SenderImgCell {
            //                UIMenuController.shared.setTargetRect(CGRect(x: 0, y: 0, width: 150, height: 35), in: vw.imgPlay!)
            //            }
            //
            //            // Animate the menu onto view
            //            UIMenuController.shared.setMenuVisible(true, animated: true)
            
            //            UtilityFunctions.showWithCancel(nativeActionSheet: "Select action", subTitle: "", vc: self, senders: ["Show image","Delete image"], success: { [weak self] (item, index) in
            //                switch index{
            //                case 0:
            self.showLightbox(video: (self.chatModal.arrayOfChat.value[sec].items[row] as? ChatData)?.mesgDetail?.video , image: (self.chatModal.arrayOfChat.value[sec].items[row] as? ChatData)?.mesgDetail?.imageM , isVideo: false)
            
            //                default:
            //                    self?.deleteMessageAtBothEnd(item: (self?.chatModal.arrayOfChat.value[sec].items[row] as? ChatData) , indexPath: IndexPath(row: row, section: sec))
            //                }
            //            }) { [weak self] in
            //                self?.tableView.becomeFirstResponder()
            //            }
            
        case .video:
            
            //            UtilityFunctions.showWithCancel(nativeActionSheet: "Select action", subTitle: "", vc: self, senders: ["Show video","Delete video"], success: {[weak self] (item, index) in
            //                switch index{
            //                case 0:
            self.showLightbox(video: (self.chatModal.arrayOfChat.value[sec].items[row] as? ChatData)?.mesgDetail?.video , image: (self.chatModal.arrayOfChat.value[sec].items[row] as? ChatData)?.mesgDetail?.imageM , isVideo: true)
            
            //                default:
            //                    self?.deleteMessageAtBothEnd(item: (self?.chatModal.arrayOfChat.value[sec].items[row] as? ChatData) , indexPath: IndexPath(row: row, section: sec))
            //                }
            //                }, cancel: {[weak self] in
            //                    self?.tableView.becomeFirstResponder()
            //            })
            
        case .gif:
            //            UtilityFunctions.showWithCancel(nativeActionSheet: "Select action", subTitle: "", vc: self, senders: ["Show gif","Delete gif"], success: {[weak self] (item, index) in
            //                switch index{
            //                case 0:
            self.showGif(video: (self.chatModal.arrayOfChat.value[sec].items[row] as? ChatData)?.mesgDetail?.video, image: (self.chatModal.arrayOfChat.value[sec].items[row] as? ChatData)?.mesgDetail?.imageM)
            
            //                default:
            //                    self?.deleteMessageAtBothEnd(item: (self?.chatModal.arrayOfChat.value[sec].items[row] as? ChatData) , indexPath: IndexPath(row: row, section: sec))
            //                }
            //                }, cancel: {[weak self] in
            //                    self?.tableView.becomeFirstResponder()
            //            })
            
        default: break
        }
    }
    
    func uploadItem(index: Int, sec: Int) {
        
        guard let message = (chatModal.arrayOfChat.value[sec].items[index] as? ChatData)?.mesgDetail else { return }
        switch message.typeOfMsg ?? .txt {
        case .txt:
            break
        case .video:
            self.vwChatAssessery.uploadImage(image: [message.video?.thumbnail as? UIImage ?? UIImage()], message: message)
        case .img:
            self.vwChatAssessery.uploadImage(image: [message.imageM?.thumbnail as? UIImage ?? UIImage()], message: message)
        default: break
        }
    }
    
}
extension ChatViewController {
    @objc func keyboardWillShow(_ notification: Notification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            let keyboardHeight = keyboardRectangle.height
            
            bottomContstraint.constant = -(keyboardHeight - 35)
            
            if keyboardHeight > 100 {
                let numberOfSection = self.tableView.numberOfSections
                if numberOfSection > 0 {
                    let numberOfRows = self.tableView.numberOfRows(inSection: self.tableView.numberOfSections - 1)
                    self.tableView.scrollToRow(at: IndexPath.init(row: numberOfRows - 1, section: numberOfSection - 1), at: .bottom, animated: true)
                }
                //                scrollToBottom()
            }
        }
    }
    
    @objc func keyboardWillHide(_ notification: NSNotification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            let keyboardHeight = keyboardRectangle.height
            bottomContstraint.constant = keyboardHeight
            self.vwChatAssessery.textView.invalidateIntrinsicContentSize()
        }
    }
}
