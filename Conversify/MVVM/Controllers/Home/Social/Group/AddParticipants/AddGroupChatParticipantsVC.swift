//
//  AddGroupChatParticipantsVC.swift
//  Conversify
//
//  Created by Apple on 10/12/18.
//

import UIKit

class AddGroupChatParticipantsVC: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var constraintHeightAddParticipant: NSLayoutConstraint!
    @IBOutlet weak var btnMore: UIButton!
    @IBOutlet weak var viewTableHeader: UIView!
    @IBOutlet weak var labelGroupDesc: UILabel!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var btnAddParticipants: UIButton!
    @IBOutlet weak var labelNoOfMembers: UILabel!
    @IBOutlet weak var btnEdit: UIButton!
    @IBOutlet weak var labelGroupName: UILabel!
    @IBOutlet weak var txtfGroupName: UITextField!
    @IBOutlet weak var groupProfilePic: UIImageView!
    @IBOutlet weak var btnNotification: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnExitGroup: UIButton!
    @IBOutlet weak var btnShare: UIButton!
    @IBOutlet weak var btnArchiveGroup: UIButton!
    
    //MARK::- PROPERTIES
    var chatVM = ChatViewModal()
    var addGroupVM = AddGroupViewModal()
    var isFromDetail = false
    var newGroupName = ""
    var backPressed : (() -> ())?
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        isFromDetail ? grabDetails() : setupView()
        refreshCalled = { [weak self] in
            self?.refreshControl.endRefreshing()
        }
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        (txtfGroupName.rx.text <-> chatVM.groupName)<bag
        
        chatVM.particpantAdded.filter { (_) -> Bool in
            self.tableView.reloadData()
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.tableView.reloadData()
                }
            })<bag
        
        addGroupVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        addGroupVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.labelNoOfMembers.text = "Members · " + /self?.addGroupVM.groupMembers.value.count.toString
                    self?.setUpGroupDetails()
                    self?.refreshControl.endRefreshing()
                }
            })<bag
        chatVM.updateNameSuccessfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.labelGroupName?.text = /self?.newGroupName
                }
            })<bag
        
        chatVM.exitSuccesfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.navigationController?.viewControllers.forEach({ (controller) in
                        if controller is OnboardTabViewController{
                            self?.chatVM.exit?()
                            (controller as? OnboardTabViewController)?.updateGroupChat()
                            (controller as? OnboardTabViewController)?.updateGroup()
                            self?.navigationController?.popToViewController(controller, animated: true)
                            return
                        }
                    })
                }
            })<bag
        
        chatVM.notificationOn.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if !bool {
                    self?.btnNotification.isSelected = /self?.chatVM.groupChatData.value?.notification
                }else{
                    self?.chatVM.groupChatData.value?.notification = /self?.btnNotification.isSelected
                }
            })<bag
        
        btnNotification.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.btnNotification.isSelected = /self?.btnNotification.isSelected.toggle()
            self?.chatVM.notificationGroupChat(action: /self?.btnNotification.isSelected ? "true" :"false")
        })<bag
        
        btnExitGroup.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.exitGroup(isExit: !(/self?.addGroupVM.group.value?.adminId == /Singleton.sharedInstance.loggedInUser?.id) )
        })<bag
        
        btnArchiveGroup.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.archiveGroup()
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.backPressed?()
            self?.chatVM.updateGroupDetails?(self?.chatVM.groupChatData.value)
            self?.popVC()
        })<bag
        
        btnEdit.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.btnEdit.isSelected = /self?.btnEdit.isSelected.toggle()
            self?.labelGroupName?.isHidden = /self?.labelGroupName?.isHidden.toggle()
            self?.txtfGroupName?.isHidden = /self?.txtfGroupName?.isHidden.toggle()
            if /self?.txtfGroupName.text != ""{
                self?.chatVM.addEditPostGroupName(groupID: /self?.isFromDetail ? /self?.addGroupVM.group.value?.id :  /self?.chatVM.groupChatData.value?.groupId)
                self?.newGroupName = /self?.txtfGroupName.text
                let group = self?.addGroupVM.group.value
                group?.groupName = /self?.txtfGroupName.text
                self?.addGroupVM.updateYourGroupDetails?(group)
                self?.txtfGroupName.text = ""
            }
        })<bag
        
        btnAddParticipants.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            guard let vc = R.storyboard.people.addParticipantViewController() else { return }
            vc.isNew = false
            vc.participantVM = AddParticipantViewModal(group: /self?.isFromDetail ? /self?.addGroupVM.group.value?.id :  /self?.chatVM.groupChatData.value?.groupId , venue: nil)
            vc.participantVM.particpantsIds = { [weak self] users in
                let userIds = users.map({ (user) -> String in
                    return /user.id
                })
                if users.count != 0 && users != nil{
                    self?.chatVM.addMoreParticipants(particpants: userIds.toJson(), groupId: /self?.isFromDetail ? /self?.addGroupVM.group.value?.id :  /self?.chatVM.groupChatData.value?.groupId , venueId: "")
                }
                self?.labelNoOfMembers.text = "Members · " + /self?.chatVM.groupMembers.value.count.toString
            }
            self?.presentVC(vc)
        })<bag
        tableView.estimatedRowHeight = 60.0
        if isFromDetail{
            addGroupVM.groupMembers
                .asObservable()
                .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupParticipantTableViewCell.identifier, cellType: GroupParticipantTableViewCell.self)) { (row,element,cell) in
                    cell.labelIsAdmin?.isHidden = !(/element?.isAdmin)
                    cell.members = element
                }<bag
        }else{
            chatVM.groupMembers
                .asObservable()
                .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupParticipantTableViewCell.identifier, cellType: GroupParticipantTableViewCell.self)) { [weak self](row,element,cell) in
                    cell.labelIsAdmin?.isHidden = !(self?.chatVM.groupChatData.value?.adminId == element?.id)
                    cell.user = element
                }<bag
            tableView.rowHeight = 48
        }
        
        btnShare.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            UtilityFunctions.share(mssage: APIConstants.shareTextGroup, url: nil, image: nil)
        })<bag
        
        btnMore.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            UtilityFunctions.show(nativeActionSheet: "Select option", subTitle: "", vc: self, senders: ["Invite to network"], success: { (value, index) in
                switch index{
                case 0://invite
                    let contactPickerScene = EPContactsPicker(delegate: self, multiSelection:true, subtitleCellType: SubtitleCellValue.email)
                    contactPickerScene.multiSelectEnabled = true
                    let navigationController = UINavigationController(rootViewController: contactPickerScene)
                    self?.present(navigationController, animated: true, completion: nil)
                default:
                    break
                }
            })
        })<bag
        
    }
    
}

//MARK::- CUSTOM METHODS
extension AddGroupChatParticipantsVC {
    
    func setupView() {
        chatVM.groupMembers.value = chatVM.groupChatData.value?.groupMembers?.map({$0.user}) ?? []
        tableView.reloadData()
        btnNotification.isSelected = /chatVM.groupChatData.value?.notification
        groupProfilePic?.image(url:  /chatVM.groupChatData.value?.grpImg?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /chatVM.groupChatData.value?.grpImg?.original))
        labelGroupName?.text = /chatVM.groupChatData.value?.groupName?.uppercaseFirst
        btnEdit?.isHidden = !(chatVM.groupChatData.value?.adminId == Singleton.sharedInstance.loggedInUser?.id)
        labelGroupDesc.text = /chatVM.groupChatData.value?.desc
        labelNoOfMembers.text = "Members · " + /chatVM.groupMembers.value.count.toString
        //        constraintHeightAddParticipant.constant = /chatVM.groupChatData.value?.adminId == /Singleton.sharedInstance.loggedInUser?.id ? 48 : 0
        btnExitGroup.setTitle((chatVM.groupChatData.value?.adminId == Singleton.sharedInstance.loggedInUser?.id) ? "Delete Network" : "Exit Network", for: .normal)
        updateHeight()
    }
    
    func setUpGroupDetails(){
        let groupConvoList = GroupConvoList()
        groupConvoList.groupId = isFromDetail ? /addGroupVM.group.value?.id : /chatVM.venue.value?.groupId
        chatVM.groupChatData.value = groupConvoList
        groupProfilePic?.image(url:  /addGroupVM.group.value?.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /addGroupVM.group.value?.imageUrl?.original))
        labelGroupName?.text = /addGroupVM.group.value?.groupName?.uppercaseFirst
        btnEdit?.isHidden = !(addGroupVM.group.value?.adminId == Singleton.sharedInstance.loggedInUser?.id)
        btnExitGroup.setTitle((addGroupVM.group.value?.adminId == Singleton.sharedInstance.loggedInUser?.id) ? "Delete Network" : "Exit Network", for: .normal)
        
        labelNoOfMembers.text = "Members · " + /addGroupVM.groupMembers.value.count.toString
        btnNotification.isSelected = /addGroupVM.group.value?.notification
        labelGroupDesc.text = /addGroupVM.group.value?.desc
        
        updateHeight()
    }
    
    func updateHeight(){
        let height1 = labelGroupDesc.getEstimatedHeight() + 140 + UIScreen.main.bounds.width
                 viewTableHeader.frame = CGRect(x: 0 , y: 0, w: UIScreen.main.bounds.width, h: height1)
        tableView.tableHeaderView = viewTableHeader
        tableView.layoutIfNeeded()
        tableView.reloadData()
    }
    
    func grabDetails(){
        addGroupVM.retrieveDetail()
        setUpGroupDetails()
        updateHeight()
    }
    
    func exitGroup(isExit: Bool){
        UtilityFunctions.show(alert: "Are you sure you want to " + (/isExit ? "exit" : "delete") + " this network?", message: "", buttonOk: { [weak self] in
            self?.chatVM.exitPostGroup()
            }, viewController: self, buttonText: "Yes")
    }
    
    func archiveGroup(){
        UtilityFunctions.show(alert: "Are you sure you want to archive this network?", message: "", buttonOk: { [weak self] in
            self?.chatVM.archivePostGroup()
            }, viewController: self, buttonText: "Yes")
    }
    
}

extension AddGroupChatParticipantsVC : EPPickerDelegate{
    
    func epContactPicker(_: EPContactsPicker, didContactFetchFailed error : NSError){
        print("Failed with error \(error.description)")
    }
    
    func epContactPicker(_: EPContactsPicker, didSelectContact contact : EPContact){
        print("Contact \(contact.displayName()) has been selected")
    }
    
    func epContactPicker(_: EPContactsPicker, didCancel error : NSError){
        print("User canceled the selection");
    }
    
    func epContactPicker(_ picker: EPContactsPicker, didSelectMultipleContacts contacts: [EPContact]) {
        print("The following contacts are selected")
        for contact in contacts {
            print("\(contact.displayName())")
        }
        
        var contacNum = [String]()
        var emails = [String]()
        if contacts.count != 0{
            contacts.forEach { (contac) in
                contac.emails.forEach({ (mailID,lbl) in
                    emails.append(/mailID)
                })
                contac.phoneNumbers.forEach({ (phn,lbl) in
                    contacNum.append(/phn)
                })
            }
        }
        
        if emails.count > 0 || contacNum.count > 0{
            chatVM.inviteContacts(phone:  contacNum.toJson(), groupId: isFromDetail ? /addGroupVM.group.value?.id : /chatVM.groupChatData.value?.groupId , email: emails.toJson(), isGroup: true)
        }
        
        picker.dismissVC(completion: nil)
        
    }
}
