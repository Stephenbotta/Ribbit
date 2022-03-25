//
//  ChatViewController.swift
//  Conversify
//
//  Created by Apple on 23/11/18.
//

import UIKit

class ChatListingViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var searchTextBar: UISearchBar!
    @IBOutlet weak var btnGroupChat: UIButton!
    @IBOutlet weak var btnIndividualChat: UIButton!
    @IBOutlet weak var scrollingView: UIView!
   
    
    //MARK::- PROPERTIES
    var chatVM = ChatListViewModal()
    var isFirstTime : Bool = true
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
        btnIndividualChat.isSelected = true
        searchTextBar.delegate = self
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        getMessagesList()
    }
    
    override func bindings() {
        
        tableView.estimatedRowHeight = 84.0
        tableView.rowHeight = UITableView.automaticDimension
        
        chatVM.items.asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: "ChatListingTblCell", cellType: ChatListingTblCell.self)) { (row,element,cell) in
                cell.item = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            guard let vc = R.storyboard.chats.chatViewController() else { return }
            vc.isFromChat = true
            vc.receiverData = self?.chatVM.items.value[/indexPath.element?.row].senderId
            Singleton.sharedInstance.conversationId = /self?.chatVM.items.value[/indexPath.element?.row].conversationId
            vc.chatModal.conversationId.value = /self?.chatVM.items.value[/indexPath.element?.row].conversationId
            if self?.chatVM.flag == 1 {
                vc.chatingType = .oneToOne
                self?.pushVC(vc)
            }else {
                vc.chatModal.groupIdToJoin.value = self?.chatVM.items.value[/indexPath.element?.row].senderId?.id
                vc.chatingType = .oneToMany
                self?.pushVC(vc)
            }
            vc.chatModal.backRefresh = { [weak self] in
                self?.getMessagesList()
            }
            }<bag
        
        btnIndividualChat.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.chatVM.flag = 1
            self?.chatVM.items.value = self?.chatVM.individualChat.value ?? []
            self?.selectDeselectButtons((self?.btnIndividualChat)!)
            UtilityFunctions.checkNilData(arr: self?.chatVM.items.value, tableView: self?.tableView, contentMode: .center, msg: "No Chat Found")
            self?.chatVM.page = 1
            self?.getMessagesList()
            self?.tableView.reloadData()
        })<bag
        
        btnGroupChat.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.chatVM.flag = 2
//            if /self?.isFirstTime {
//                self?.isFirstTime = false
                self?.chatVM.page = 1
                self?.getMessagesList()
//            }else{
//                self?.chatVM.items.value = self?.chatVM.groupChat.value ?? []
//            }
            UtilityFunctions.checkNilData(arr: self?.chatVM.items.value, tableView: self?.tableView, contentMode: .center, msg: "No Chat Found")
            self?.selectDeselectButtons((self?.btnGroupChat)!)
            
            self?.tableView.reloadData()
        })<bag
        
    }
    
    func getMessagesList(){
        refreshControl.beginRefreshing()
        chatVM.getChatList() { [weak self](_) in
            self?.refreshControl.endRefreshing()
            UtilityFunctions.checkNilData(arr: self?.chatVM.items.value, tableView: self?.tableView, contentMode: .center, msg: "No Chat Found")
        }
    }
    
    func selectDeselectButtons(_ sender: UIButton) {
        UIView.animate(withDuration: 0.35) {
            if let frame = self.scrollingView?.frame {
                var point = self.scrollingView.center
                point.x = (sender.center.x)
                self.scrollingView.center = point
                self.scrollingView.layoutIfNeeded()
            }
        }
        if let superView = sender.superview {
            for subView in superView.subviews {
                if let btn = subView as? UIButton {
                    btn.isSelected = (btn == sender)
                }
            }
        }
    }
    
    func onLoad(){
        refreshCalled = { [weak self] in
             self?.view.endEditing(true)
            self?.getMessagesList()
            self?.refreshControl.endRefreshing()
        }
    }
    
}


//MARK::- SEARCH BAR DELEGATE
extension ChatListingViewController : UISearchBarDelegate {
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        
        let txt = searchTextBar.text?.lowercased().trimmingCharacters(in: .whitespaces)
        if /txt?.isEmpty {
            chatVM.items.value = (chatVM.flag == 2) ? chatVM.groupChat.value : chatVM.individualChat.value
        }else {
            if chatVM.flag == 1{
                let searchIndividualChat = chatVM.individualChat.value.filter { /($0.senderId?.userName?.contains(/txt)) }
                chatVM.items.value = searchIndividualChat
            }else {
                let searchGroupChat = chatVM.groupChat.value.filter { /($0.senderId?.groupName?.contains(/txt)) }
                chatVM.items.value = searchGroupChat
            }
        }
        UtilityFunctions.checkNilData(arr: chatVM.items.value, tableView: tableView, contentMode: .center, msg: "No Chat Found")
        tableView.reloadData()
    }
    
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        
    }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        chatVM.items.value = (chatVM.flag == 2) ? chatVM.groupChat.value : chatVM.individualChat.value
        UtilityFunctions.checkNilData(arr: chatVM.items.value, tableView: tableView, contentMode: .center, msg: "No Chat Found")
        tableView.reloadData()
        self.view.endEditing(true)
    }
    
}
