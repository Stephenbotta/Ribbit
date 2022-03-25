//
//  OtherUserPostListVC.swift
//  Conversify
//
//  Created by admin on 08/08/20.
//

import UIKit
import ESPullToRefresh
import GrowingTextView
import EZSwiftExtensions
import IQKeyboardManagerSwift
import JJFloatingActionButton

class OtherUserPostListVC: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var labelNoPostFound: UILabel!
    
    @IBOutlet weak var btnBack: UIButton!
    //MARK::- PROPERTIES
    var postListModal = HomePostViewModal()
    var searchTagUserView : UserTagView?
    var keyboardHeight : CGFloat?
    let actionButton = JJFloatingActionButton()
    var userId: String?
    //MARK::- VC LIFE CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        addPaging()
        setUpTagView()
        onLoad()
        
    }
    
    
    
    override func bindings() {
        tableView.estimatedRowHeight = 84.0
        tableView.rowHeight = UITableView.automaticDimension
        postListModal.items
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.homePostTableCell.identifier, cellType: HomePostTableCell.self)) { (row, element, cell) in
                cell.row = row
                cell.btnComment.tag = row
                cell.item = element
                cell.delegate = self
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.showPostDetail(indx: indexPath.element?.row)
            }<bag
        
        tableView.rx.didScroll.subscribe { [weak self] (indexPath) in
            
            }<bag
        self.btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.popVC()
        })<bag
    }
}

//MARK::- CUTSOM METHODS
extension OtherUserPostListVC {
    
    func addFloating(){
        actionButton.buttonImage = R.image.plus()
        actionButton.buttonColor = #colorLiteral(red: 1, green: 0.8666666667, blue: 0.08235294118, alpha: 1)
        
        actionButton.addItem(title: "Create new post", image: UIImage(named: "ic_add_noBg")?.withRenderingMode(.alwaysTemplate)) { [weak self] item in
            if /Singleton.sharedInstance.loggedInUser?.groupCount != 0{
                guard let vc = R.storyboard.post.newPostViewController() else { return }
                vc.groupListModal.getGroupListing({ [weak self](isSuccess) in
                })
                self?.pushVC(vc)
            }else{
                guard let vc = R.storyboard.post.createPostViewController() else { return }
                vc.isPostingInGroup = false
                self?.pushVC(vc)
            }
        }
        
        actionButton.addItem(title: "Find someone", image:R.image.ic_binoculars()?.tint(with:#colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1) )) { [weak self] item in
            guard let vc = R.storyboard.home.converseOptionsViewController() else { return }
            self?.pushVC(vc)
        }
        
        actionButton.display(inViewController: self)
        actionButton.translatesAutoresizingMaskIntoConstraints = false
        
    }
    
    func showPostDetail(indx : Int?){
        
        if self.postListModal.items.value.count == 0 {
            return
        }
        guard let vc = R.storyboard.post.postDetailViewController() else { return }
        vc.postDetailViewModal.postId.value = postListModal.items.value[/indx].id
        vc.selectedInterest = { [weak self](detail) in
            self?.postListModal.items.value[/indx].liked = detail.liked
            self?.postListModal.items.value[/indx].likesCount = detail.likesCount
            self?.postListModal.items.value[/indx].commentCount = detail.commentCount
            let indxPath = IndexPath.init(item: /indx, section: 0)
            guard let cell = self?.tableView.cellForRow(at: indxPath) as? HomePostTableCell else { return }
            cell.btnLike.isSelected = /detail.liked
            let likeTxt = (detail.likesCount == 1) ? (/detail.likesCount?.toString + " Like") : (/detail.likesCount?.toString + " Likes")
            let cmtTxt = (detail.commentCount == 1) ? (/detail.commentCount?.toString + " Reply · ") : (/detail.commentCount?.toString + " Replies · ")
            cell.btnLikes.setTitle(likeTxt, for: .normal)
            cell.btnReplies.setTitle(cmtTxt, for: .normal)
        }
        vc.refresh = { [ weak self] in
            self?.onLoad()
        }
        pushVC(vc)
    }
    
    func onLoad(){
        
        postListModal.page = 1
        postListModal.getOtherPostListing(userId:self.userId, { [weak self](_) in
            ez.runThisInMainThread {
                self?.labelNoPostFound.isHidden = (self?.postListModal.items.value.count != 0)
                self?.tableView.es.stopPullToRefresh()
                self?.tableView.es.stopLoadingMore()
                //                self?.tableView.es.noticeNoMoreData()
            }
        })
    }
    
    
    func setUpTagView(){
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        let newframe = CGRect(x: 0, y: 0, width: tableView.frame.width, height: (keyboardHeight ?? 200.0) - 88.0)
        searchTagUserView = UserTagView.init(frame: newframe)
        searchTagUserView?.delegate = self
    }
    
    func setUserTags(user : UserList? , indx : Int?){
        let indxPath = IndexPath(row: /indx, section: 0)
        guard let cell = tableView.cellForRow(at: indxPath) as? HomePostTableCell else { return }
        cell.setTag(member: user ?? UserList())
    }
    
    @objc func keyboardWillShow(_ notification: Notification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            keyboardHeight = keyboardRectangle.height
        }
    }
}

//MARK::- TABLEVIEW REFRESH CONTROLS
extension OtherUserPostListVC {
    
    func addPaging(){
        
        tableView.es.addInfiniteScrolling {
            if self.postListModal.loadMore {
                self.postListModal.page = self.postListModal.page + 1
                
                self.postListModal.getOtherPostListing(userId:self.userId, { [weak self](_) in
                    ez.runThisInMainThread {
                        self?.labelNoPostFound.isHidden = (self?.postListModal.items.value.count != 0)
                        self?.tableView.es.stopPullToRefresh()
                        self?.tableView.es.stopLoadingMore()
                    }
                })
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
        
        tableView.es.addPullToRefresh {
            self.postListModal.page =  1
            self.tableView.es.resetNoMoreData()
            self.postListModal.getOtherPostListing(userId:self.userId, { [weak self](_) in
                ez.runThisInMainThread {
                    self?.labelNoPostFound.isHidden = (self?.postListModal.items.value.count != 0)
                    self?.tableView.es.stopPullToRefresh()
                    //                    self?.tableView.es.noticeNoMoreData()
                    self?.tableView.es.stopLoadingMore()
                }
            })
        }
    }
}


//MARK::- DELEGATE FROM HOMEPOSTLIST TABLE VIEW CELL
extension OtherUserPostListVC : PostCellDelegates {
    
    func likePost(indx: Int?) {
        
        let action = /postListModal.items.value[/indx].liked ? "2" : "1"
        let item = postListModal.items.value[/indx]
        let inc = /postListModal.items.value[/indx].liked ? (-1) : (1)
        item.liked = item.liked?.toggle()
        item.likesCount = /item.likesCount + inc
        
        postListModal.likePost(mediaId: "", postId: postListModal.items.value[/indx].id, action: action, postBy: postListModal.items.value[/indx].postBy?.id , { [weak self](status) in
            if status {
            }else {
                self?.tableView.reloadRows(at: [IndexPath.init(row: /indx, section: 0)], with: .none)
            }
        })
    }
    
    func commentOnPost(indx: Int?, cmmnt: String?) {
        
        let indxPath = IndexPath(row: /indx, section: 0)
        
        guard let cell = tableView.cellForRow(at: indxPath) as? HomePostTableCell else { return }
        let hastags = cmmnt?.usertags()
        let item = postListModal.items.value[/indx]
        item.commentCount = /item.commentCount + 1
        self.view.layoutIfNeeded()
        let cmtTxt = (item.commentCount == 1) ? (/item.commentCount?.toString + " Reply · ") : (/item.commentCount?.toString + " Replies · ")
        cell.btnReplies.setTitle(cmtTxt, for: .normal)
        
        postListModal.commentOnPost(mediaId: "", postId: postListModal.items.value[/indx].id, commentId: nil, userHashTag: (hastags?.count == 0) ? nil : hastags?.toJson(), comment: cmmnt, postBy: postListModal.items.value[/indx].postBy?.id, attachmentUrl: NSDictionary()) { [weak self](status) in
            if status { }else {
                self?.tableView.reloadRows(at: [IndexPath.init(row: /indx, section: 0)], with: .none)
            }
        }
    }
    
    func showUserTags(searchText: String? , indx : Int?) {
        
        postListModal.searchUsers(serachText: /searchText) { [weak self](_) in
            if !UIApplication.shared.isKeyboardPresented {
                self?.searchTagUserView?.removeFromSuperview()
                return
            }
            let myViews = self?.view?.subviews.filter{$0 is UserTagView}
            if self?.postListModal.userTags.value.count == 0 {
                if /myViews?.count > 0 {
                    self?.searchTagUserView?.removeFromSuperview()
                }
                return
            }
            let fheight = UIScreen.main.bounds.height - ((self?.keyboardHeight ?? 200) + 64.0)
            let newframe = CGRect(x: 0, y: 20, width: self?.tableView.frame.width ?? 0.0, height: fheight)
            self?.searchTagUserView?.frame = newframe
            self?.searchTagUserView?.setUpUI(superView: self?.view, dataList: self?.postListModal.userTags.value, indx: /indx)
        }
    }
    
    func removeSearchView() {
        searchTagUserView?.removeFromSuperview()
    }
    
    func showPost(indx: Int?) {
        showPostDetail(indx: /indx)
    }
}


//MARK::- TAG VIEW DELEGATE
extension OtherUserPostListVC : DelegateUserSerachTag {
    func selectedData(detail: UserList?, indx: Int?) {
        searchTagUserView?.removeFromSuperview()
        setUserTags(user: detail, indx: indx)
    }
}






