//
//  NotificationsViewController.swift
//  Conversify
//
//  Created by Apple on 12/11/18.
//

import UIKit
import ESPullToRefresh
import EZSwiftExtensions

class NotificationsViewController: BaseRxViewController {
    
    //MARK: - Outlets
    @IBOutlet weak var lblNoDataFound: UILabel!
    @IBOutlet weak var btnClear: UIButton!
    @IBOutlet weak var constraintHeightNavigationHeader: NSLayoutConstraint!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    let viewModel = NotificationsViewModel()
    var isFromNotification = false
    
    //MARK: - View Hierarchy
    override func viewDidLoad() {
        super.viewDidLoad()
        setupView()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        refreshRetrieval()
    }
    
    //MARK: - Setup UI
    func setupView() {
        
        retrieveNotifications()
        addPaging()
        tableView?.tableFooterView = UIView()
        tableView?.estimatedRowHeight = 100
        tableView?.rowHeight = UITableView.automaticDimension
    }
    
    override func bindings() {
        
        viewModel.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        viewModel.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    DispatchQueue.main.async {
                        self?.lblNoDataFound?.isHidden = !(self?.viewModel.items.value.count == 0)
                        self?.refreshControl.endRefreshing()
                        self?.tableView?.layoutIfNeeded()
                    }
                    
                }
            })<bag
        
        viewModel.items.asObservable().subscribe({ [weak self] (items) in
            self?.btnClear.isEnabled = /self?.viewModel.items.value.count != 0
            self?.btnClear.alpha = /self?.viewModel.items.value.count == 0 ? 0.4 : 1
        })<bag
        
        
        
        viewModel.items
            .asObservable()
            .bind(to: tableView.rx.items) { (tableView, row, element) in
                let indexPath = IndexPath(row: row, section: 0)
                guard let cell = tableView.dequeueReusableCell(withIdentifier: (element.type == .groupInvite || element.type == .venueInvite || element.type == .groupRequest || element.type == .venueRequest ||  element.type == .requestFollow   ) ? "InviteNotificationCell" : "PostNotificationCell", for: indexPath) as? NotificationTableViewCell else { return UITableViewCell() }
                cell.item = element
                cell.acceptRejectBlock = { [weak self] accepted in
                    self?.viewModel.acceptRequest(notification: element, accept: accepted, { (completed) in
                        if completed {
                            self?.viewModel.items.value.remove(at: indexPath.row)
                            self?.lblNoDataFound?.isHidden = !(self?.viewModel.items.value.count == 0)
                        }
                    })
                }
                return cell
            }<bag
        
        tableView.rx.setDelegate(self)<bag
        
        btnClear.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.viewModel.clearNotification()
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let `self` = self else { return }
            self.view.endEditing(true)
            self.popVC()
        })<bag
    }
    
    func retrieveNotifications() {
        tableView?.es.resetNoMoreData()
        self.viewModel.getNotifications { [weak self] (success) in
            ez.runThisInMainThread {
                
                self?.tableView?.es.stopPullToRefresh()
                self?.tableView?.es.stopLoadingMore()
                self?.refreshControl.endRefreshing()
                if !(/self?.viewModel.loadMore){
                    self?.tableView?.es.noticeNoMoreData()
                }
                self?.tableView?.es.stopPullToRefresh()
                self?.tableView?.es.stopLoadingMore()
            }
        }
        
    }
    
    //MARK::- REFRESH
    
    func refreshRetrieval(){
        viewModel.page =  1
        retrieveNotifications()
        view.endEditing(true)
    }
    
}

extension NotificationsViewController {
    
    func addPaging(){
        tableView.es.addInfiniteScrolling { [weak self] in
            guard let `self` = self else { return }
            if self.viewModel.loadMore {
                self.viewModel.page = self.viewModel.page + 1
                self.retrieveNotifications()
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
        
        refreshCalled = { [weak self] in
            self?.viewModel.page =  1
            self?.retrieveNotifications()
            self?.view.endEditing(true)
            self?.refreshControl.endRefreshing()
        }
        
    }
}

extension NotificationsViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let object = viewModel.items.value[indexPath.row]
        if let type = object.type {
            switch type {
                
            
            case  .converseNearBy , .lookNearBy:
                guard let vc = R.storyboard.people.foundMatchViewController() else { return }
                vc.isPost = (type == .converseNearBy || type ==  .lookNearBy)
                vc.postId = /object.post?.id
                vc.message = type == .converseNearBy ? /object.user?.userName + " wants to converse with someone nearby. Tap here to check the details " : /object.user?.userName + " has crossed your path at " + /object.user?.userName + " " + /object.locAddress
                vc.userId = /object.user?.id
                vc.userName = /object.user?.userName
                vc.image = /object.user?.imageUrl?.original
                vc.lat = (object.latLong?.last as? Double ?? 0)
                vc.lng = (object.latLong?.first as? Double ?? 0)
               
                vc.unDimPost = {
                    ez.runThisAfterDelay(seconds: 0.1, after: {
                        (UIApplication.topViewController())?.view.undim()
                        guard let vc = R.storyboard.post.postDetailViewController() else { return }
                        vc.postDetailViewModal.postId.value = /object.post?.id
                        UIApplication.topViewController()?.pushVC(vc)
                    })
                    
                }
                vc.unDimDismiss = {
                    ez.runThisAfterDelay(seconds: 0.1, after: {
                        (UIApplication.topViewController())?.view.undim()
                    })
                }
                vc.unDimProfile = {
                    ez.runThisAfterDelay(seconds: 0.1, after: {
                        (UIApplication.topViewController())?.view.undim()
                        guard let vc = R.storyboard.home.profileViewController() else { return }
                        vc.profileVM.userType = .otherUser
                        vc.profileVM.userId = /object.user?.id
                        (UIApplication.topViewController())?.pushVC(vc)
                    })
                }
                (UIApplication.topViewController())?.view.dim()
                (UIApplication.topViewController())?.presentVC(vc)
                
                
            case .chat , .groupChat , .venueChat , .callDisconnect:
                break
                
            case .follow , .acceptRequestFollow , .requestFollow:
                
//                if Singleton.sharedInstance.loggedInUser?.id == /object.toId{
//                    return
//                }
                guard let vc = R.storyboard.home.profileViewController() else { return }
                vc.profileVM.userType = .otherUser
                vc.profileVM.userId = object.pushBy?.id
                self.pushVC(vc)
                
            case .acceptInviteGroup , .crossedPath:
                break
                
            case .acceptInviteVenue:
                break
                
            case .acceptRequestGroup , .joinedGroup:
                let conversationId = object.group?.conversationId
                let userName =  object.group?.title
                let groupId = object.group?.id
                
                guard let vc = R.storyboard.chats.chatViewController() else { return }
                vc.chatModal = ChatViewModal(conversationId: /conversationId , chatId: "" , groupId: groupId)
                Singleton.sharedInstance.conversationId = /conversationId
                vc.isFromChat = true
                vc.chatingType = .oneToMany
                let user = User()
                user.id = groupId
                user.userName = userName
                user.img = object.group?.image
                vc.receiverData = user
                UIApplication.topViewController()?.pushVC(vc)
                
            case .acceptRequestVenue , .joinedVenue:
                
                let venueId = object.venue?.id
                let venueTitle = object.venue?.title
                let convId = object.venue?.conversationId
                
                let venue = Venues()
                venue.groupId = venueId
                venue.venueTitle = venueTitle
                venue.isMine = true
                
                guard let vc = R.storyboard.chats.chatViewController() else { return }
                Singleton.sharedInstance.conversationId = /convId
                vc.isFromChat = true
                vc.chatingType = .venue
                vc.chatModal = ChatViewModal(membersData: nil, venueD: venue)
                vc.chatModal.chatId.value = ""
                UIApplication.topViewController()?.pushVC(vc)
                
            case .group:
                let conversationId = object.group?.conversationId
                let userName =  object.group?.title
                let groupId = object.group?.id
                
                guard let vc = R.storyboard.chats.chatViewController() else { return }
                vc.isFromChat = true
                vc.chatModal = ChatViewModal(conversationId: /conversationId , chatId: "" , groupId: groupId)
                Singleton.sharedInstance.conversationId = /conversationId
                vc.chatingType = .oneToMany
                let user = User()
                user.id = groupId
                user.userName = userName
                user.img = object.group?.image
                vc.receiverData = user
                UIApplication.topViewController()?.pushVC(vc)
                
            case .venue:
                let venueId = object.venue?.id
                let venueTitle = object.venue?.title
                let convId = object.venue?.conversationId
                let venue = Venues()
                venue.groupId = venueId
                venue.venueTitle = venueTitle
                venue.isMine = true
                Singleton.sharedInstance.conversationId = /convId
                guard let vc = R.storyboard.chats.chatViewController() else { return }
                vc.isFromChat = true
                vc.chatingType = .venue
                vc.chatModal.chatId.value = ""
                vc.chatModal = ChatViewModal(membersData: nil, venueD: venue)
                vc.chatModal.groupIdToJoin.value = venueId
                UIApplication.topViewController()?.pushVC(vc)
                
            case .likePost , .likeReply, .likeComment , .comment , .reply , .post , .tagComment , .tagReply :
                
                guard let vc = R.storyboard.post.postDetailViewController() else { return }
                vc.postDetailViewModal.postId.value = /object.post?.id
                UIApplication.topViewController()?.pushVC(vc)
                
            case .groupRequest:
                break
                
            case .venueRequest:
                break
                
            case .groupInvite:
                break
                
            case .venueInvite :
                break
            case .receivedreddempoint:
                break
            case .spendEarnPoint:
                break
            }
        }
        
    }
}
