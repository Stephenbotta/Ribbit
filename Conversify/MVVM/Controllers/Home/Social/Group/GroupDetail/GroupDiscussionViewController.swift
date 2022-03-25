//
//  GroupDiscussionViewController.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import UIKit
import GrowingTextView
import IBAnimatable
import Sheeeeeeeeet

class GroupDiscussionViewController: BaseRxViewController {
    
    //MARK:- OUTLETS
    @IBOutlet weak var labelGroupName: UILabel!
    @IBOutlet weak var btnFavrt: UIButton!
    @IBOutlet weak var btnMore: UIButton!
    @IBOutlet weak var btnAddPost: UIButton!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var labelNoData: UILabel!
    
    //MARK::- PROPERTIES
    weak var replyCell: GroupDiscussionTableViewCell?
    var groupDiscussVM = GroupDiscussionViewModal()
    
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        groupDiscussVM.getPosts(isRefresh: false)
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        
        
        
        groupDiscussVM.exitSuccesfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                     self?.groupDiscussVM.backRefreshLeft?()
                    self?.groupDiscussVM.backRefresh?()
                    self?.popVC()
                }
            })<bag
        
        groupDiscussVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        groupDiscussVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    DispatchQueue.main.async {
                        self?.labelNoData.isHidden = /self?.groupDiscussVM.posts.value.count != 0
                        self?.labelGroupName.text = /self?.groupDiscussVM.group.value?.groupName?.uppercaseFirst
                        self?.btnFavrt.isSelected = /self?.groupDiscussVM.group.value?.isMember
                        self?.tableView.es.stopPullToRefresh()
                        self?.tableView.es.stopLoadingMore()
                        self?.refreshControl.endRefreshing()
                        self?.tableView.layoutIfNeeded()
                        self?.tableView.reloadData()
                    }
                    
                }
            })<bag
        
        tableView.estimatedRowHeight = 60.0
        tableView.rowHeight = UITableView.automaticDimension
        
        groupDiscussVM.posts
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupDiscussionTableViewCell.identifier, cellType: GroupDiscussionTableViewCell.self)) { [ unowned self ](row,element,cell) in
                cell.post = element
                cell.row = row
                cell.comment = { [weak self] (row , comment)  in
                    self?.groupDiscussVM.addComment(row: row, comment: comment)
                }
                cell.likeTapped = { [weak self] (row , state)  in
                    self?.groupDiscussVM.fvrtPost( row: row, state: state)
                }
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            let index_path = /indexPath.element
            guard let vc = R.storyboard.post.postDetailViewController() else { return }
            vc.postDetailViewModal.postId.value = /self?.groupDiscussVM.posts.value[index_path.row].id
//            vc.postDetailViewModal.r
            UIApplication.topViewController()?.pushVC(vc)
            }<bag
        
        tableView.rx.setDelegate(self)<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.groupDiscussVM.backRefresh?()
           self?.popVC()
        })<bag
        
        btnMore.rx.tap.asDriver().drive(onNext: {  [unowned self] () in
            let actionSheet = self.showStandardActionSheet()
            actionSheet.present(in: self, from: self.btnMore)
        })<bag
        
    }
    
    //MARK::- FUNCTIONS
    
    func onLoad(){
        addPaging()
        tableView.sectionHeaderHeight = 0
        groupDiscussVM.getPosts()
        refreshCalled = { [weak self] in
            self?.groupDiscussVM.page = 1
            self?.groupDiscussVM.getPosts()
            self?.refreshControl.endRefreshing()
        }
    }
    
    func addPaging(){
        tableView.es.addInfiniteScrolling {
            if self.groupDiscussVM.loadMore {
                self.groupDiscussVM.page = self.groupDiscussVM.page + 1
                self.groupDiscussVM.getPosts()
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
    }
    
    
    func showStandardActionSheet() -> ActionSheet {
        
        let item1 = MenuItem(title: /groupDiscussVM.groupDetail.value?.adminId == /Singleton.sharedInstance.loggedInUser?.id ? "Delete" : "Exit", value: 1 , image: R.image.ic_exit())
        let item2 = MenuItem(title: "Share", value: 2 , image: R.image.ic_share())
        let item3 = MenuItem(title: "Network Chat", value: 3 , image: R.image.ic_chat_picker())
        let item4 = MenuItem(title: "Create a new post", value: 4 , image: R.image.ic_plus_picker())
         let item5 = MenuItem(title: "Network Detail", value: 5 , image: R.image.ic_information())
        
        return ActionSheet(menu: Menu.init(title: "", items: [item1 ,item2 ,item3 , item4 , item5])) { [weak self] sheet, item in
            self?.view.endEditing(true)
            if let value = item.value as? Int {
                switch /value{
                case 1:
                    self?.exitGroup()
                    
                case 2:
                    self?.shareGroup()
                    
                case 3:
                    
                    let conversationId = self?.groupDiscussVM.groupDetail.value?.conversationId
                    let userName =  self?.groupDiscussVM.groupDetail.value?.groupName
                    let groupId = /self?.groupDiscussVM.groupDetail.value?.id
                    Singleton.sharedInstance.conversationId = /self?.groupDiscussVM.groupDetail.value?.conversationId
                    guard let vc = R.storyboard.chats.chatViewController() else { return }
                     vc.isFromChat = false
                    vc.chatModal = ChatViewModal(conversationId: /conversationId , chatId: "" , groupId: groupId)
                    vc.chatingType = .oneToMany
                    let user = User()
                    user.id = groupId
                    user.userName = userName
                    user.img = /self?.groupDiscussVM.groupDetail.value?.imageUrl
                    vc.receiverData = user
                    UIApplication.topViewController()?.pushVC(vc)
                    
                case 4:
                    
                    guard let vc = R.storyboard.post.createPostViewController() else { return }
                    vc.isPostingInGroup = true
                    let groupDet = GroupList(idV: /self?.groupDiscussVM.groupDetail.value?.id , image: /self?.groupDiscussVM.groupDetail.value?.imageUrl , name: /self?.groupDiscussVM.groupDetail.value?.groupName)
                    vc.createPostModal.isInsideGroup = true
                    vc.selectedGroup = groupDet
                    self?.pushVC(vc)
                    
                case 5:
                    guard let vc = R.storyboard.groups.addGroupChatParticipantsVC() else { return }
                    vc.isFromDetail = true
                    vc.addGroupVM = AddGroupViewModal(categor: self?.groupDiscussVM.groupDetail.value)
                    vc.addGroupVM.updateYourGroupDetails = { group in
                        self?.groupDiscussVM.groupDetail.value = group
                    }
                    self?.pushVC(vc)
                    
                    
                default:
                    break
                }
            }        }
        
        
    }
    
    func reportGroup(){
        
    }
    
    
    func exitGroup(){
        UtilityFunctions.show(alert: "Are you sure you want to exit this network?", message: "", buttonOk: { [weak self] in
            self?.groupDiscussVM.exitGroup()
            }, viewController: self, buttonText: "Yes")
    }
    
    
    func shareGroup(){
        UtilityFunctions.share(mssage: APIConstants.shareTextGroup, url: nil, image: nil)
    }
    
}



extension GroupDiscussionViewController: UITextViewDelegate {
    
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        return true
    }
}

