//
//  HomePostListViewController.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import UIKit
import ESPullToRefresh
import GrowingTextView
import EZSwiftExtensions
import IQKeyboardManagerSwift
import JJFloatingActionButton
import GoogleMobileAds

//ca-app-pub-5182485480585869~7168371323
class HomePostListViewController: BaseRxViewController, GADBannerViewDelegate, GADNativeAdDelegate, GADVideoControllerDelegate {
    
    //MARK::- OUTLETS
   // @IBOutlet weak var viewadd: UIView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var labelNotificationCount: UILabel!
    @IBOutlet weak var btnSearch: UIButton!
    @IBOutlet weak var labelNoPostFound: UILabel!
    @IBOutlet weak var btnActionAdd: UIButton!
    @IBOutlet weak var btnNotification: UIButton!
    @IBOutlet weak var btnAddStory: UIButton!
    @IBOutlet weak var cVwFollowers: UICollectionView!
 //   @IBOutlet weak var btnrefresh: UIButton!
    
    //MARK::- PROPERTIES
    var bannerView: GADBannerView!
    var postListModal = HomePostViewModal()
    var searchTagUserView : UserTagView?
    var keyboardHeight : CGFloat?
    let actionButton = JJFloatingActionButton()
    var followerVM = FollowersViewModal()
    var storiesData = StoriesDetail()
    var whileData = WheelDetail()
    var isSeenStory = false

    var isSeenArray = [Int]()
    //MARK::- VC LIFE CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.register(UINib(nibName: R.reuseIdentifier.googleAdCell.identifier, bundle: nil), forCellReuseIdentifier: R.reuseIdentifier.googleAdCell.identifier)
        addPaging()
        setUpTagView()
        onLoad()
        getFollowing()
        Spinweel()

       // loadAdds()
    }
    
    override func viewWillAppear(_ animated: Bool) {
       
        super.viewWillAppear(animated)
        getStroies()
    }
    
    
    func Spinweel(){
        postListModal.showSpinWheel { SUCCESS in
           
            guard let vc = R.storyboard.survey.spinWheelVC() else{
                return
            }
            vc.items = self.postListModal.whileData.data!
            self.present(vc, animated: true, completion: nil)
        }
    }
    override func bindings() {
        tableView.estimatedRowHeight = 84.0
        
        tableView.rowHeight = UITableView.automaticDimension
        
//        postListModal.items
//            .asObservable()
//            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.homePostTableCell.identifier, cellType: HomePostTableCell.self)) { (row, element, cell) in
//                cell.row = row
//                cell.btnComment.tag = row
//                cell.item = element
//                cell.delegate = self
//
//            }<bag

        postListModal
            .itemsToShow
            .asObservable()
            .bind(to: tableView.rx.items){
                (tableView, row, element) in
              
                let indexPath = IndexPath(row: row, section: 0)
                if /indexPath.row > 0 && /indexPath.row % 5 == 0 {
                    guard  let cell = tableView.dequeueReusableCell(withIdentifier:R.reuseIdentifier.googleAdCell.identifier, for: indexPath) as? GoogleAdCell else { return UITableViewCell() }
                    cell.showNativeAd(rootVC: self)
                    return cell
                } else {
                    guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.homePostTableCell.identifier, for: indexPath) as? HomePostTableCell else { return UITableViewCell() }
                    cell.row = row
                    cell.btnComment.tag = row
                    cell.item = element
                    cell.delegate = self
                    return cell
                }
            }<bag
      
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.showPostDetail(indx: indexPath.element?.row)
            }<bag
        
//        tableView.rx.didScroll.subscribe { [weak self] (indexPath) in
//
//            }<bag
        
        self.btnSearch.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.search.homeSearchViewController() else { return }
            self?.pushVC(vc)
        })<bag
        
        btnActionAdd.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.post.converseNearByViewController() else { return }
            vc.converseVM = ConverseNearByViewModal(type: 2)
            self?.pushVC(vc)
        })<bag
        
        btnNotification.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.home.notificationsViewController() else { return }
            self?.pushVC(vc)
        })<bag
        btnAddStory.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.home.addStoryViewController()else{return}
            self?.pushVC(vc)
        })<bag
        
    }
}

//MARK::- CUTSOM METHODS
extension HomePostListViewController {
    
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
        if( (/indx > 0) &&  (/indx % 5) == 0){
            print("hello")
        }else{
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
       
    }
    
    func onLoad(){
        getStroies()
        postListModal.page = 1
        postListModal.getPostListing { [weak self](_) in
            ez.runThisInMainThread {
                self?.labelNoPostFound.isHidden = (self?.postListModal.items.value.count != 0)
                self?.tableView.es.stopPullToRefresh()
                self?.tableView.es.stopLoadingMore()
            }
        }
    }
    
    func getFollowing(){
       
        postListModal.getFollowingListing { [weak self](_) in
            self?.labelNoPostFound.isHidden = (self?.postListModal.items.value.count != 0)
            self?.cVwFollowers.reloadData()
        }
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
extension HomePostListViewController {
    
    func addPaging(){
        
        tableView.es.addInfiniteScrolling {
            if self.postListModal.loadMore {
                self.postListModal.page = self.postListModal.page + 1
                self.postListModal.getPostListing({ [weak self](_) in
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
            self.postListModal.getPostListing({ [weak self](_) in
                ez.runThisInMainThread {
                    self?.labelNoPostFound.isHidden = (self?.postListModal.items.value.count != 0)
                    self?.tableView.es.stopPullToRefresh()
                  
                    self?.tableView.es.stopLoadingMore()
                }
            })
            self.getFollowing()
            self.getStroies()
        }
    }
}


//MARK::- DELEGATE FROM HOMEPOSTLIST TABLE VIEW CELL
extension HomePostListViewController : PostCellDelegates {
    
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
extension HomePostListViewController : DelegateUserSerachTag {
    func selectedData(detail: UserList?, indx: Int?) {
        searchTagUserView?.removeFromSuperview()
        setUserTags(user: detail, indx: indx)
    }
}


//MARK :- Storyes CollectionView

extension HomePostListViewController : UICollectionViewDelegate , UICollectionViewDataSource {
    func numberOfSections(in collectionView: UICollectionView) -> Int{
        return 1
    }
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.storiesData.data?.count ?? 0
    }
    
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "FollowersListCVC" ,
                                                      for: indexPath) as! FollowersListCVC
        let url = URL(string: storiesData.data?[indexPath.row].imageUrl?.original ?? "")
        cell.imgVwUser.sd_setImage(with: url, placeholderImage: #imageLiteral(resourceName: "ic_placeholder"))
        print(isSeenArray)
        if isSeenArray[indexPath.row] == 0 {
            cell.imgVwUser.borderColor = UIColor.init(named: "YellowTheme")
           
        }else{
            cell.imgVwUser.borderColor = .clear
        }
        cell.lblName.text = storiesData.data?[indexPath.row].firstName
        return cell
    }
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
//        var socketAndUserId = [String: Any]()
//        socketAndUserId = ["id" :
//                            /storiesData.data?[indexPath.row].stories?[0].id! ]
//        SocketIOManager.shared.viewStory( data:socketAndUserId ) { [weak self] (status)  in
//            if status{
//                print("hello socket Hit")
//               
//            }
//        }

        
        guard let vc = R.storyboard.home.viewStoryViewController() else { return }
        vc.storyData = self.storiesData.data?[indexPath.row]
        self.pushVC(vc)
    }
}


extension HomePostListViewController : UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize {
        
        
        return  CGSize(width: cVwFollowers.frame.size.width/4, height: cVwFollowers.frame.size.height)
        
    }
}


//MARK:- API
extension HomePostListViewController {
    func getStroies(){
        ProfileTarget
            .getStories
            .request()
            .asObservable()
            .subscribe(onNext: { [weak self] (response) in
                
                
                guard let resp = response as? StoriesDetail  else{return}
                self?.storiesData = resp

                self?.isSeenArray.removeAll()
                
                if (self?.storiesData.data != nil){
                for i in 0..<(self?.storiesData.data!.count)! {
                    var isSeenstory = false
                    for j in 0 ..< (self?.storiesData.data?[i].stories!.count)!{
                        
                        
                        if(self?.storiesData.data?[i].stories?[j].isSeen == 0){
                            print("false")
                            isSeenstory = false
                            continue
                        }else{
                            print("true")
                            isSeenstory = true
                           
                        }
                    }
                    if isSeenstory == true{
                        self?.isSeenArray.append(1)
                    }else{
                        self?.isSeenArray.append(0)
                    }
                }
                    
                }
                DispatchQueue.main.async {
                    self?.cVwFollowers.reloadData()
                }
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
        })
       
    }
}


