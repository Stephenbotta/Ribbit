//
//  PostMediaDetailViewController.swift
//  Conversify
//
//  Created by Harminder on 11/09/19.
//

import UIKit
import IBAnimatable
import RxDataSources
import IQKeyboardManagerSwift
import EZSwiftExtensions
import Lightbox
import GiphyUISDK

class PostMediaDetailViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var viewTableHeader: UIView!
    @IBOutlet weak var imgPost: UIImageView!
    @IBOutlet weak var labelGroupName: UILabel!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnInfo: UIButton!
    @IBOutlet weak var btnUpload: UIButton!
    @IBOutlet weak var tableView: CommentTable!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
            tableView.registerXIBForHeaderFooter("CommentTableHeaderView")
        }
    }
    @IBOutlet weak var imgUserPic: AnimatableImageView!
    @IBOutlet weak var labelPostDate: UILabel!
    @IBOutlet weak var labelName: ActiveLabel!
    @IBOutlet weak var labelPostText: ActiveLabel!
    @IBOutlet weak var btnLikesCount: UIButton!
    @IBOutlet weak var btnCommentsCount: UIButton!
    @IBOutlet weak var collectionView: UICollectionView!{
        didSet{
            collectionView.delegate = self
            collectionView.dataSource = self
        }
    }
    @IBOutlet weak var pageControl: UIPageControl!
    
    //MARK::- PROPERTIES
    var postListModal = HomePostViewModal()
    var postDetailViewModal = PostDetailViewModal()
    var dataSource : RxTableViewSectionedReloadDataSource<PostDetailDataSec>?
    var selectedComment : CommentList?
    var selectedReply : ReplyList?
    var commentOn : SelectionType = .comment
    var selectedIndx : Int?
    var selectedInterest: SelectPostDetail?
    var searchTagUserView : UserTagView?
    var keyboardHeight : CGFloat?
    var refresh : (() -> ())?
    var lastMentioningText = ""
    var postImage : UIImage?
    
    var selectedCollectionIndex: Int?

    //MARK::- VIEW CYCLE
    
    override func viewDidLoad() {
        super.viewDidLoad()
        Giphy.configure(apiKey: Keys.giphyKey)
        onLoad()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        IQKeyboardManager.shared.enable = false
        self.tableView.becomeFirstResponder()
    }
    
    
    override func viewWillDisappear(_ animated: Bool){
        self.view.endEditing(true)
        IQKeyboardManager.shared.enable = true
    }
    
    //MARK::- BINDINGS
    
    override func bindings() {
        self.btnInfo.isEnabled = false
        tableView.estimatedRowHeight = 350
        tableView.rowHeight = UITableView.automaticDimension
        
        btnInfo.rx.tap.asDriver().drive(onNext: { [unowned self] () in
            UtilityFunctions.share(mssage: /self.labelPostText?.text, url: "app link", image: self.imgPost.image)
        })<bag
        
        
        dataSource = RxTableViewSectionedReloadDataSource<PostDetailDataSec>(configureCell: { (_, tableView, indexPath, element) -> UITableViewCell in
            let replyData = element
            guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.postDetailTableCell.identifier, for: indexPath) as? PostDetailTableCell else { return UITableViewCell()}
            cell.row = indexPath
            cell.delegate = self
            cell.item = replyData
            return cell
        })
        
        guard let safeDatasource = dataSource else{return}
        postDetailViewModal.arrayOfComments.asObservable().bind(to: tableView.rx.items(dataSource: safeDatasource))<bag
        tableView.rx.setDelegate(self)<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            
            self?.popVC()
            if self?.postDetailViewModal.postDetail.value?.id != nil {
                self?.selectedInterest?(self?.postDetailViewModal.postDetail.value ?? PostList())
            }
        })<bag
    }
    
}


//MARK::- CUSTOM METHODS
extension PostMediaDetailViewController {
    
    func onLoad(){
        isLoginSignp = false
        pageControl.numberOfPages = /postDetailViewModal.postDetail.value?.media?.count
        tableView.inputAccessory.delegate = self
        refreshControl.beginRefreshing()
        setUpTagView()
        callToPostDetailApi()
        refreshCalled = { [weak self] in
            self?.callToPostDetailApi()
        }
        imgUserPic.addTapGesture { [weak self] (gesture) in
            
            //            if Singleton.sharedInstance.loggedInUser?.id == /self?.postDetailViewModal.postDetail.value?.postBy?.id{
            //                return
            //            }
            guard let vc = R.storyboard.home.profileViewController() else { return }
            self?.btnInfo.isHidden = /self?.postDetailViewModal.postDetail.value?.postBy?.id != Singleton.sharedInstance.loggedInUser?.id
            vc.profileVM.userType = .otherUser
            vc.profileVM.userId = self?.postDetailViewModal.postDetail.value?.postBy?.id
            UIApplication.topViewController()?.pushVC(vc)
        }
        //        ez.runThisAfterDelay(seconds: .0) { [weak self] in
        
        //        }
    }
    
    
    func callToPostDetailApi(){
        postDetailViewModal.getPostDetail { [weak self](status) in
            self?.refreshControl.endRefreshing()
            if status {
               // self?.pageControl.isHidden = (/self?.postDetailViewModal.postDetail.value?.media?.count == 1)
                self?.pageControl.numberOfPages = /self?.postDetailViewModal.postDetail.value?.media?.count
                //                self?.tableView.inputAccessory.txtfComments.becomeFirstResponder()
                self?.imgPost.image(url:  /self?.postDetailViewModal.postDetail.value?.media?.first?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /self?.postDetailViewModal.postDetail.value?.media?.first?.original))
                self?.btnInfo.isEnabled = true
                self?.tableView.inputAccessory.btnLike.isSelected = /self?.postDetailViewModal.postDetail.value?.liked
                self?.setPostDetailData(postDetail: self?.postDetailViewModal.postDetail.value)
                self?.collectionView.reloadData()
            }
        }
    }
    
    
    
    func setPostDetailData(postDetail : PostList?){
        
        //Set Label Name in Category
        
        labelGroupName.text = ((/postDetail?.groupDetail?.groupName?.uppercaseFirst) != "") ? (/postDetail?.groupDetail?.groupName?.uppercaseFirst + " [" + /postDetail?.postCategory?.categoryName?.uppercaseFirst + "]") : ""
        let customType3 = ActiveType.custom(pattern: /postDetail?.postBy?.userName)//Regex that looks for "with"
        labelName.enabledTypes = [customType3]
        labelName.customColor[customType3] =  #colorLiteral(red: 1, green: 0.8, blue: 0, alpha: 1)
        labelName.handleCustomTap(for: customType3) { [weak self] element in
            if Singleton.sharedInstance.loggedInUser?.id == /postDetail?.postBy?.id{
                return
            }
            guard let vc = R.storyboard.home.profileViewController() else { return }
            
            vc.profileVM.userType = .otherUser
            vc.profileVM.userId = /postDetail?.postBy?.id
            UIApplication.topViewController()?.pushVC(vc)
        }
        
        if postDetail?.postCategory != nil{
            labelName.text = /postDetail?.postBy?.userName
        }else {
            labelName.text = postDetail?.postBy?.userName
        }
        
        let postdate = Date(milliseconds: /postDetail?.createdOn)
        labelPostDate.text = postdate.timePassed()
        let likeTxt = (postDetail?.likesCount == 1) ? (/postDetail?.likesCount?.toString + " Like") : (/postDetail?.likesCount?.toString + " Likes")
        let cmtTxt = (postDetail?.commentCount == 1) ? (/postDetail?.commentCount?.toString + " Reply ·") : (/postDetail?.commentCount?.toString + " Replies ·")
        btnLikesCount.setTitle(likeTxt, for: .normal)
        btnCommentsCount.setTitle(cmtTxt, for: .normal)
        imgUserPic.image(url: /postDetail?.postBy?.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /postDetail?.postBy?.imageUrl?.thumbnail))
        
        labelPostText.highlightFontSize = 16
        labelPostText.handleMentionTap { (mention) in
            
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.userName = /mention
            vc.isMentioning = true
            UIApplication.topViewController()?.pushVC(vc)
        }
        labelPostText.enabledTypes = [.mention, .hashtag, .url]
        labelPostText.text = postDetail?.postText?.capitalizedFirst()
        
        tableView.inputAccessory.btnLike.isSelected = /postDetail?.liked
        //        tableView.sizeHeaderToFit()
        calcHeight()
    }
    func setUpTagView(){
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        let newframe = CGRect(x: 0, y: 20, width: tableView.frame.width, height: (keyboardHeight ?? 200.0) )
        searchTagUserView = UserTagView.init(frame: newframe)
        searchTagUserView?.delegate = self
    }
    
    @objc func keyboardWillShow(_ notification: Notification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            keyboardHeight = keyboardRectangle.height
        }
    }
    
    func calcHeight(){
        let height1 = 120 + labelPostText.getEstimatedHeight() + UIScreen.main.bounds.width
        viewTableHeader.frame = CGRect(x: 0 , y: 0, w: UIScreen.main.bounds.width, h: height1)
        tableView.tableHeaderView = viewTableHeader
        tableView.layoutIfNeeded()
        tableView.reloadData()
    }
    
    
}

//MARK::- TABLEVIEW DATASOURCE AND DELEGATES
extension PostMediaDetailViewController : UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        //cmnt label top + bottom+height
        let cmntHeight = /postDetailViewModal.arrayOfComments.value[section].header?.comment?.height(withConstrainedWidth: UIScreen.main.bounds.width - 136, font: UIFont.systemFont(ofSize: 14)) + 16 + 4
        
        //like comment height
        let likeComntHeight : CGFloat = 24
        
        //if replies then 32 else 0
        let reply = postDetailViewModal.arrayOfComments.value[section].header?.replyCount
        let ht = cmntHeight +  (/reply == 0 ? 0 : 32) + likeComntHeight + CGFloat(17)
        if postDetailViewModal.arrayOfComments.value[section].header?.attachment?.thumbnail != "" {
            return ht + 150
        } else {
             return ht + 25
        }
        
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if /self.postDetailViewModal.arrayOfComments.value[/indexPath.section].header?.isHide {
            self.postDetailViewModal.arrayOfComments.value[/indexPath.section].header?.loadedElementCount = 0
            return  0
        }else {
            
            tableView.estimatedRowHeight = 80
            return UITableView.automaticDimension
        }
    }
    
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: "CommentTableHeaderView"){
            (header as? CommentTableHeaderView)?.row = section
            (header as? CommentTableHeaderView)?.item = postDetailViewModal.arrayOfComments.value[section].header
            (header as? CommentTableHeaderView)?.delegate = self
            return header
        }else {
            return nil
        }
    }
}

//MARK::- DelegateCommentHeaderView
extension PostMediaDetailViewController : DelegateCommentHeaderView {
    
    func deleteComment(indx : Int?){
        
        let repliesCount = /postDetailViewModal.arrayOfComments.value[/indx].header?.replyCount
        decreaseCommentCount(count: repliesCount + 1)
        postDetailViewModal.deleteCommentReply(mediaId: /postDetailViewModal.mediaId.value  ,commentId: /postDetailViewModal.arrayOfComments.value[/indx].header?.id, replyId: "")
        
        ez.runThisInMainThread { [ weak self] in
            self?.postDetailViewModal.arrayOfComments.value.remove(at: /indx)
            self?.tableView.reloadData()
        }
        
        
    }
    
    func likeOnComment(indx: Int?) {
        
        let action = /postDetailViewModal.arrayOfComments.value[/indx].header?.liked ? "2" : "1"
        
        let item = postDetailViewModal.arrayOfComments.value[/indx]
        let inc = /postDetailViewModal.arrayOfComments.value[/indx].header?.liked ? (-1) : (1)
        item.header?.liked = item.header?.liked?.toggle()
        item.header?.likeCount = /item.header?.likeCount + inc
       //postDetailViewModal.arrayOfComments.value[/indx] = item
        
        postDetailViewModal.likeUnlikeComment(mediaId: /postDetailViewModal.mediaId.value  , commentId: /postDetailViewModal.arrayOfComments.value[/indx].header?.id, action: action, commentBy: /postDetailViewModal.arrayOfComments.value[/indx].header?.commentBy?.id) { (status) in
            
        }
    }
    
    func replyOnComment(indx: Int?) {
        typeC = "Reply"
        commentOn = .reply
        selectedIndx = /indx
        selectedComment = postDetailViewModal.arrayOfComments.value[/indx].header
        tableView.inputAccessory.txtfComments.text = "@" + /postDetailViewModal.arrayOfComments.value[/indx].header?.commentBy?.userName + " "
        tableView.inputAccessory.constraintHeightViewReply.constant = 24
        tableView.inputAccessory.btnReplyTo.text = "Replying to @" + /postDetailViewModal.arrayOfComments.value[/indx].header?.commentBy?.userName
        tableView.inputAccessory.txtfComments.becomeFirstResponder()
        tableView.inputAccessory.btnCamera.isHidden = true
        
        //get index
        let index = IndexPath(row: NSNotFound, section: /indx )
        
        //        //get height
        //        let section = /indx
        //        let cmntHeight = /postDetailViewModal.arrayOfComments.value[section].header?.comment?.height(withConstrainedWidth: UIScreen.main.bounds.width - 136, font: UIFont.systemFont(ofSize: 14)) + 16 + 4
        //
        //        //like comment height
        //        let likeComntHeight : CGFloat = 24
        //
        //        //if replies then 32 else 0
        //        let reply = postDetailViewModal.arrayOfComments.value[section].header?.replyCount
        //        let ht = cmntHeight +  (/reply == 0 ? 0 : 32) + likeComntHeight + CGFloat(17)
        //        //update keyboard and scroll row near to keyboard
        tableView.updateContentView(indexPath: index, height: UIScreen.main.bounds.height/15)
        
    }
    
    func loadMoreReplies(indx: Int? , _ completion:@escaping (Int)->()) {
        self.postDetailViewModal.arrayOfComments.value[/indx].header?.isIndicator = true
        if let header = self.tableView.headerView(forSection: /indx ) as? CommentTableHeaderView {
            header.indicator.startAnimating()
            header.btnLoadMore.isEnabled = false
        }
        let currentReplies = self.postDetailViewModal.arrayOfComments.value[/indx].items.count
        let totalReplies = self.postDetailViewModal.arrayOfComments.value[/indx].header?.replyCount
        let currntIndxCount = self.postDetailViewModal.arrayOfComments.value[/indx].header?.loadedElementCount
        if (currentReplies == totalReplies) && (currentReplies == currntIndxCount ) {
            let item = self.postDetailViewModal.arrayOfComments.value[/indx]
            item.header?.isHide = true
            item.header?.isIndicator = false
            self.postDetailViewModal.arrayOfComments.value[/indx] = item
            if let header = self.tableView.headerView(forSection: /indx ) as? CommentTableHeaderView {
                header.indicator.stopAnimating()
                header.btnLoadMore.isEnabled = true
            }
            self.tableView.reloadRows(at: [IndexPath.init(row: 0, section: /indx)], with: .none)
            return
        }
        var replyId = (postDetailViewModal.arrayOfComments.value[/indx].items == nil) ? nil : postDetailViewModal.arrayOfComments.value[/indx].items.first?.id
        
        replyId = (postDetailViewModal.arrayOfComments.value[/indx].header?.loadedElementCount == 0) ? nil : replyId
        postDetailViewModal.getCommentReplies(mediaId: /postDetailViewModal.mediaId.value  ,indx: indx, commentId: postDetailViewModal.arrayOfComments.value[/indx].header?.id, replyId: replyId, totalReply: postDetailViewModal.arrayOfComments.value[/indx].header?.replyCount?.toString) { [weak self](isSuccess) in
            if isSuccess {
                guard let item = self?.postDetailViewModal.arrayOfComments.value[/indx] else { return }
                item.header?.isHide = false
                item.header?.isIndicator = false
                self?.postDetailViewModal.arrayOfComments.value[/indx] = item
                completion(/self?.postDetailViewModal.arrayOfComments.value[/indx].items.count)
                self?.tableView.reloadData()
                //                self?.tableView.reloadRows(at: [IndexPath.init(row: 0, section: /indx)], with: .none)
            }
        }
    }
}

//MARK::- DELEGATE FROM TABLE ACCESSORY VIEW
extension PostMediaDetailViewController : DelegateCommentAccessory {
    func showUserTags(searchText: String?) {
        
        lastMentioningText = /searchText
        if searchText == ""{
            self.searchTagUserView?.removeFromSuperview()
            return
        }
        
        postListModal.searchUsers(serachText: /searchText) { [weak self](_) in
            
            if /self?.lastMentioningText == "" {
                return
            }
            
            
            let myViews = self?.view?.subviews.filter{$0 is UserTagView}
            if self?.postListModal.userTags.value.count == 0 {
                if /myViews?.count > 0 {
                    self?.searchTagUserView?.removeFromSuperview()
                }
                return
            }
            let fheight = UIScreen.main.bounds.height - ((self?.keyboardHeight ?? 200))
            let newframe = CGRect(x: 0, y: 20, width: self?.tableView.frame.width ?? 0.0, height: fheight )
            self?.searchTagUserView?.frame = newframe
            if /self?.lastMentioningText == "" {
                self?.searchTagUserView?.removeFromSuperview()
                return
            }
            self?.searchTagUserView?.setUpUI(superView: self?.view, dataList: self?.postListModal.userTags.value, indx: 0)
            
        }
    }
    
    func removeSearchView() {
        searchTagUserView?.removeFromSuperview()
    }
    
    func removeSelectedTag() {
        selectedComment = nil
        searchTagUserView?.removeFromSuperview()
    }
    
    
    
    func sendComment(cmnt: String?, attachmentUrl: NSDictionary) {
        
        let userTags = cmnt?.usertags()
        // && (userTags?.first == selectedComment?.commentBy?.userName)
        if selectedComment != nil && !(/cmnt?.isEmpty)  {
            
            let reply = ReplyList()
            reply.reply = cmnt
            reply.createdOn = /Date().millisecondsSince1970.toDouble()
            let postBy = PostBy(id:"")
            reply.replyBy = postBy
            guard var item = self.postDetailViewModal.arrayOfComments.value[/selectedIndx].items as? [ReplyList]  else { return }
            item.append(reply)
            if self.postDetailViewModal.arrayOfComments.value[/selectedIndx].header?.replyCount == self.postDetailViewModal.arrayOfComments.value[/selectedIndx].header?.loadedElementCount {
                self.postDetailViewModal.arrayOfComments.value[/selectedIndx].header?.loadedElementCount = /self.postDetailViewModal.arrayOfComments.value[/selectedIndx].header?.loadedElementCount + 1
            }
            self.postDetailViewModal.arrayOfComments.value[/selectedIndx].header?.replyCount = /self.postDetailViewModal.arrayOfComments.value[/selectedIndx].header?.replyCount + 1
            
            
            self.postDetailViewModal.arrayOfComments.value[/selectedIndx].items = item
            
            self.tableView.reloadData()
            self.tableView.inputAccessory.constraintHeightViewReply.constant = 0
            tableView.inputAccessory.btnCamera.isHidden = false
            let copiedSelectedIndx = self.selectedIndx
            let copiedSelectedComment = self.selectedComment
            self.selectedIndx = nil
            self.selectedComment = nil
            
            //increase comment count
            self.increaseCommentCount()
            
            postDetailViewModal.replyOnComment(mediaId: /postDetailViewModal.mediaId.value  ,sec : /copiedSelectedIndx ,commentBy: copiedSelectedComment?.commentBy?.id, commentId: /copiedSelectedComment?.id, userIdTag: (userTags?.count == 0) ? nil : userTags?.toJson(), replyId: nil, reply: cmnt) { (_) in
            }
            self.tableView.inputAccessory.constraintHeightViewReply.constant = 0
            tableView.inputAccessory.btnCamera.isHidden = false
        }else {
            print("api hit comment")
            postDetailViewModal.commentOnPost(mediaId: /postDetailViewModal.mediaId.value  , postId: postDetailViewModal.postDetail.value?.id, commentId: nil, userHashTag: (userTags?.count == 0) ? nil : userTags?.toJson(), comment: cmnt, postBy:  postDetailViewModal.postDetail.value?.postBy?.id, attachmentUrl: attachmentUrl as? NSDictionary ?? NSDictionary()) { [weak self](_) in
                
                
                self?.increaseCommentCount()
                
                
                let curentComments = self?.postDetailViewModal.arrayOfComments.value.count
                
                self?.tableView.reloadData()
                
                //                var isRun = true
                //
                //                for post in self?.postDetailViewModal.arrayOfComments.value ?? []{
                //                    if !(/post.header?.isHide){
                //                        isRun = false
                //                        break
                //                    }
                //                }
                
                //                if isRun{
                if /curentComments > 0{
                    let index = IndexPath(row: NSNotFound, section: (/curentComments - 1))
                    self?.tableView.scrollToRow(at: index, at: .bottom , animated: true)
                }
                
                //                }
                
            }
        }
    }
    
    func increaseCommentCount(){
        self.postDetailViewModal.postDetail.value?.commentCount = /self.postDetailViewModal.postDetail.value?.commentCount + 1
        let curentComments = self.postDetailViewModal.postDetail.value?.commentCount
        let cmtTxt = (curentComments == 1) ? (/curentComments?.toString + " Reply ·") : (/curentComments?.toString + " Replies ·")
        self.btnCommentsCount.setTitle(cmtTxt, for: .normal)
    }
    
    func decreaseCommentCount(count: Int){
        if /self.postDetailViewModal.postDetail.value?.commentCount == 0 && /self.postDetailViewModal.postDetail.value?.commentCount > count{
            return
        }
        self.postDetailViewModal.postDetail.value?.commentCount = /self.postDetailViewModal.postDetail.value?.commentCount - count
        let curentComments = self.postDetailViewModal.postDetail.value?.commentCount
        let cmtTxt = (curentComments == 1) ? (/curentComments?.toString + " Reply ·") : (/curentComments?.toString + " Replies ·")
        self.btnCommentsCount.setTitle(cmtTxt, for: .normal)
        self.tableView.becomeFirstResponder()
    }
    
    
    func likePost() {
        
        let action = /postDetailViewModal.postDetail.value?.liked ? "2" : "1"
        let inc = /postDetailViewModal.postDetail.value?.liked ? (-1) : (1)
        let initialLikeCount =  /postDetailViewModal.postDetail.value?.likesCount
        let likeCount = /initialLikeCount + inc
        postDetailViewModal.postDetail.value?.liked = postDetailViewModal.postDetail.value?.liked?.toggle()
        let likeTxt = (likeCount == 1) ? (/likeCount.toString + " Like") : (/likeCount.toString + " Likes")
        btnLikesCount.setTitle(likeTxt, for: .normal)
        postDetailViewModal.postDetail.value?.likesCount = likeCount
        
        postListModal.likePost(mediaId: /postDetailViewModal.mediaId.value, postId: /postDetailViewModal.postId.value, action: action, postBy: postDetailViewModal.postDetail.value?.postBy?.id , { [weak self](status) in
            if status {
                //self?.postDetailViewModal.postDetail.value?.likesCount = likeCount
            }else {
                self?.postDetailViewModal.postDetail.value?.likesCount = initialLikeCount
                let likeCount = /self?.postDetailViewModal.postDetail.value?.likesCount
                let likeTxt = (likeCount == 1) ? (/likeCount.toString + " Like") : (/likeCount.toString + " Likes")
                self?.btnLikesCount.setTitle(likeTxt, for: .normal)
            }
        })
    }
}

//MARK::- DELEGATE FROM REPLY CELL
extension PostMediaDetailViewController : DelegateRepliesCell {
    
    func deleteReply(indx : IndexPath?){
        decreaseCommentCount(count: 1)
        if self.postDetailViewModal.arrayOfComments.value[/selectedIndx].header?.replyCount == self.postDetailViewModal.arrayOfComments.value[/selectedIndx].header?.loadedElementCount {
            self.postDetailViewModal.arrayOfComments.value[/indx?.section].header?.replyCount = /self.postDetailViewModal.arrayOfComments.value[/indx?.section].header?.replyCount - 1
            self.postDetailViewModal.arrayOfComments.value[/indx?.section].header?.loadedElementCount = /self.postDetailViewModal.arrayOfComments.value[/indx?.section].header?.loadedElementCount - 1
        }
        
        postDetailViewModal.deleteCommentReply(mediaId: /postDetailViewModal.mediaId.value  ,commentId: "", replyId: /postDetailViewModal.arrayOfComments.value[/indx?.section].items[/indx?.row].id)
        ez.runThisInMainThread { [weak self] in
            self?.postDetailViewModal.arrayOfComments.value[/indx?.section].items.remove(at: /indx?.row)
            self?.tableView.reloadData()
        }
        
        
    }
    
    func likeReply(indx: IndexPath?) {
        let action = /postDetailViewModal.arrayOfComments.value[/indx?.section].items[/indx?.row].liked ? "2" : "1"
        let item = postDetailViewModal.arrayOfComments.value[/indx?.section]
        let inc = /postDetailViewModal.arrayOfComments.value[/indx?.section].items[/indx?.row].liked ? (-1) : (1)
        item.items[/indx?.row].liked = item.items[/indx?.row].liked?.toggle()
        item.items[/indx?.row].likeCount = /item.items[/indx?.row].likeCount + inc
        //postDetailViewModal.arrayOfComments.value[/indx?.section] = item
        
        postDetailViewModal.likeUnlikeReply(mediaId: /postDetailViewModal.mediaId.value  , replyId: item.items[/indx?.row].id, action: action, replyBy: item.items[/indx?.row].replyBy?.id) { (_) in
        }
    }
    
    func sendReply(indx: IndexPath?) {
        commentOn = .reply
        selectedIndx = indx?.section
        tableView.inputAccessory.txtfComments.becomeFirstResponder()
        selectedComment = postDetailViewModal.arrayOfComments.value[/indx?.section].header
        tableView.inputAccessory.constraintHeightViewReply.constant = 24
        tableView.inputAccessory.btnCamera.isHidden = true
        tableView.inputAccessory.btnReplyTo.text = "Replying to @" + /postDetailViewModal.arrayOfComments.value[/indx?.section].items[/indx?.row].replyBy?.userName
        tableView.inputAccessory.txtfComments.text = "@" + /postDetailViewModal.arrayOfComments.value[/indx?.section].items[/indx?.row].replyBy?.userName + " "
        tableView.inputAccessory.layoutIfNeeded()
        guard let indexExist = indx else { return }
        tableView.updateContentView(indexPath: indexExist , height: UIScreen.main.bounds.height/15)
        
    }
}


//MARK::- TAG VIEW DELEGATE
extension PostMediaDetailViewController : DelegateUserSerachTag {
    
    func selectedData(detail: UserList?, indx: Int?) {
        searchTagUserView?.removeFromSuperview()
        tableView.inputAccessory.setTag(member: detail ?? UserList())
    }
}

//MARK::- COLLECTIONVIEW DELEGATES & DATASOURCE
extension PostMediaDetailViewController: UICollectionViewDataSource  , UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
        
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return /postDetailViewModal.postDetail.value?.media?.count
    }
    
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell{
        //        pager.currentPage = /indexPath.row
        let identifier = R.reuseIdentifier.selectedImageCollectionViewCell.identifier
        guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: identifier ,
                                                            for: indexPath) as? SelectedImageCollectionViewCell else { return UICollectionViewCell() }
        cell.media = postDetailViewModal.postDetail.value?.media?[indexPath.row]
        
        cell.collectionIndex = indexPath.row
        cell.selectedIndex = selectedCollectionIndex ?? -1
        cell.btnPlayVideo?.tag = indexPath.row

        cell.btnPlayVideo?.addTarget(self, action: #selector(actionPlayVide(_:)), for: .touchUpInside)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        //        guard let media = item?.media?[indexPath.row] as? Media , let vc = R.storyboard.post.postMediaDetailViewController() else { return }
        //        vc.postDetailViewModal.postId.value = /item?.id
        //        vc.postDetailViewModal.mediaId.value = /media.id
        //        UIApplication.topViewController()?.pushVC(vc)
        self.view.endEditing(true)
        guard let media = postDetailViewModal.postDetail.value?.media else { return }
        showLightbox(media: media, isVideo: true)

    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        //return CGSize(width: UIScreen.main.bounds.width  , height: UIScreen.main.bounds.width)
        switch /postDetailViewModal.postDetail.value?.media?.count{
               case 1:
                   return CGSize(width: UIScreen.main.bounds.width  , height: UIScreen.main.bounds.width)
               case 2:
                   return CGSize(width: UIScreen.main.bounds.width/2  , height: UIScreen.main.bounds.width)
               case 3:
                   switch indexPath.row {
                   case 0:
                       return
                           CGSize(width: UIScreen.main.bounds.width/2  , height:  UIScreen.main.bounds.width)
                   case 1:
                        return  CGSize(width: UIScreen.main.bounds.width/2  , height: UIScreen.main.bounds.width/2)
                   default:
                       return CGSize(width: UIScreen.main.bounds.width/2  , height:  UIScreen.main.bounds.width/2)
                   }
               case 4:
                   return CGSize(width: UIScreen.main.bounds.width/2  , height: UIScreen.main.bounds.width/2)
               default:
                   return CGSize(width: collectionView.frame.size.width  , height: collectionView.frame.size.width)
               }
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0.0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 0.0
    }
    
    
    @objc func actionPlayVide(_ sender: UIButton)  {
        selectedCollectionIndex = sender.tag
        collectionView.reloadData()
    }
       
    
    func scrollViewDidEndDragging(_ scrollView: UIScrollView, willDecelerate decelerate: Bool) {
        //        if !decelerate {
        
        //        }
    }
    
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let pageIndex = round(scrollView.contentOffset.x/UIScreen.main.bounds.width)
        pageControl.currentPage = Int(pageIndex)
//        if pageControl.currentPage <= /postDetailViewModal.postDetail.value?.media?.count - 1 {
//        let nextPage = CGFloat(pageControl.currentPage + 1)
//        let contentOffSet = CGPoint(x: (scrollView.bounds.size.width * nextPage), y: 0)
//        scrollView.setContentOffset(contentOffSet, animated: true)
//            pageControl.currentPage = nextPage
//        }
    }
    
    //PREVIEW
    func showLightbox(media: [Media] , isVideo:Bool) {
        
       
        
        var data = [LightboxImage]()
            // if isVideo{
        for i in 0..<media.count {
             guard let media = postDetailViewModal.postDetail.value?.media?[i] else { return }
            switch /media.mediaType{
                   case "VIDEO":
                    guard let imgUrl =  URL.init(string: /media.thumbnail) , let videoUrl = URL.init(string: /media.videoUrl) else { return }
                    data.append( LightboxImage(
                        imageURL: imgUrl ,
                        text: "",
                        videoURL: videoUrl))
                    
                       
                   case "IMAGE", "GIF":
                       guard let imgUrl = URL.init(string: /media.thumbnail) else { return }
                       data.append(LightboxImage(imageURL: imgUrl))
//                   case "GIF":
//                       guard let gifUrl = URL(string: /media.original), let vc = R.storyboard.home.expandedGifViewController() else { return }
//                       vc.imageUrl = gifUrl
//                       UIApplication.topViewController()?.presentVC(vc)
                   default:
                       break
                   }
            
            
        }
            
      
        
        let controller = LightboxController(images: data )
        controller.dynamicBackground = true
        controller.modalPresentationStyle = .fullScreen
        UIApplication.topViewController()?.presentVC(controller)
        
    }
    
}
