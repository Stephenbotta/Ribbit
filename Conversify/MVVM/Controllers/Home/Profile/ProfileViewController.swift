//
//  ProfileViewController.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 14/10/18.
//

import UIKit
import Tags

enum ProfileScreenType {
    case otherUser
    case loggedInUser
    
}
class ProfileViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var tableHeaderView: UIView!
    @IBOutlet weak var userDetailView: UIView!
    @IBOutlet weak var labelProfile: UILabel!
    @IBOutlet weak var followUnfollowView: UIView!
    @IBOutlet weak var qrView: UIView!
    @IBOutlet weak var btnDisplayQr: UIButton!
    @IBOutlet weak var viewNoUser: UIView!
    @IBOutlet weak var btnFollowers: UIButton!
    @IBOutlet weak var imageIsPrivate: UIImageView!
    @IBOutlet weak var btnFollowing: UIButton!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var bgViewEditBtn: UIView!
    @IBOutlet weak var labelUserType: UIImageView!
    @IBOutlet weak var apiIndicator: UIActivityIndicatorView!
    @IBOutlet weak var imgUserPic: UIImageView!
    @IBOutlet weak var labelDesignation: UILabel!
    @IBOutlet weak var labelUserName: UILabel!
    @IBOutlet weak var labelFollowingCount: UILabel!
    @IBOutlet weak var labelFollowerCount: UILabel!
    @IBOutlet weak var labelBio: UILabel!
    @IBOutlet weak var labelBioHeader: UILabel!
    @IBOutlet weak var tagView: TagsView!
    @IBOutlet weak var navigationHeaderView: UIView!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnMore: UIButton!
    @IBOutlet weak var btnEditOrMsg: UIButton!
    @IBOutlet weak var btnFollow: UIButton!
    @IBOutlet weak var otherUsersTagView: TagsView!
    @IBOutlet weak var constraintTopInterestHeader: NSLayoutConstraint!
    @IBOutlet weak var labelGender: UILabel!
    @IBOutlet weak var labelEmail: UILabel!
    @IBOutlet weak var labelPhoneNo: UILabel!
    @IBOutlet weak var labelWorkplace: UILabel!
    @IBOutlet weak var labelWebsite: UILabel!
    @IBOutlet weak var viewGender: UIView!
    @IBOutlet weak var viewEmail: UIView!
    @IBOutlet weak var viewPhoneNumber: UIView!
    @IBOutlet weak var viewWorkplace: UIView!
    @IBOutlet weak var viewWebsite: UIView!
    @IBOutlet weak var labelDesig: UILabel?
    @IBOutlet weak var viewDesignation: UIView!
    @IBOutlet weak var btnScanQR: UIButton!
    //MARK::- PROPERTIES
    var profileVM = ProfileDetailViewModal()
    var isMentioning = false
    var userName = ""
    
    //MARK::- VIEW CYCLE
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        onLoad()
    }
 
    //MARK::- BINDINGS
    override func bindings() {
        
        profileVM.userBlocked.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.profileVM.userInfo.value?.isBlocked = !(/self?.profileVM.userInfo.value?.isBlocked)
                }
            })<bag
        
        profileVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    DispatchQueue.main.async {
                        self?.refreshControl.beginRefreshing()
                    }
                }
            })<bag
        
        profileVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    DispatchQueue.main.async {
                        if (self?.profileVM.userType != .loggedInUser){
                            self?.viewNoUser.isHidden = !(/self?.profileVM.userInfo.value?.id == "")
                        }
                        self?.apiIndicator.stopAnimating()
                        self?.refreshControl.endRefreshing()
                        self?.tableView.layoutIfNeeded()
                    }
                    
                }
            })<bag
        
        btnFollowers.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.settings.followerViewController() else { return }
            vc.isFollower = true
            vc.followerVM.isProfile = true
            self?.pushVC(vc)
        })<bag
        
        btnFollowing.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.settings.followerViewController() else { return }
            vc.isFollower = false
            vc.followerVM.isProfile = true
            self?.pushVC(vc)
        })<bag
        
        btnFollow.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.btnFollow.isSelected = /self?.btnFollow.isSelected.toggle()
            self?.profileVM.followUser(userId: (self?.profileVM.userType == .loggedInUser) ? nil : /self?.profileVM.userInfo.value?.id , follow: /self?.btnFollow.isSelected)
        })<bag
        
        btnMore.setImage((profileVM.userType == .loggedInUser) ? R.image.ic_settings() : R.image.ic_more() , for: .normal)
        
        btnMore.rx.tap.asDriver().drive(onNext: { [weak self] in
            self?.view.endEditing(true)
            if (self?.profileVM.userType == .loggedInUser) {
                guard let vc = R.storyboard.settings.settingViewController() else { return }
                self?.pushVC(vc)
            }else{
                if /self?.profileVM.userId == ""{
                    return
                }
                UtilityFunctions.show(nativeActionSheet: "Select option", subTitle: "", vc: self, senders: [ /self?.profileVM.userInfo.value?.isBlocked ? "Unblock" : "Block"], success: { [weak self] (value, index) in
                    switch index{
                    case 0://invite
                        self?.profileVM.blockUser(userId: self?.profileVM.userId, isBlock: !(/self?.profileVM.userInfo.value?.isBlocked) )
                    default:
                        break
                    }
                })
            }
            
        })<bag
        
        btnDisplayQr.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.home.displayQRVC() else { return }
            self?.pushVC(vc)
        })<bag
        btnScanQR.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.home.qrScannerController() else { return }
            self?.pushVC(vc)
        })<bag
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popVC()
        })<bag
        
        btnEditOrMsg.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            switch self?.profileVM.userType ?? .loggedInUser{
            case .loggedInUser :
                guard let vc = R.storyboard.home.editProfileViewController() else { return }
                vc.editVM.backRefresh = { [weak self] in
                    self?.setUpView(userData: Singleton.sharedInstance.loggedInUser)
                }
                self?.pushVC(vc)
                
            case .otherUser :
                guard let vc = R.storyboard.chats.chatViewController() else { return }
                vc.chatModal = ChatViewModal(conversationId: /self?.profileVM.userInfo.value?.conversationId , chatId: nil, groupId: nil)
                Singleton.sharedInstance.conversationId = /self?.profileVM.userInfo.value?.conversationId
                vc.isFromChat = false
                vc.chatingType = .oneToOne
                vc.receiverData = self?.profileVM.userInfo.value
                self?.pushVC(vc)
            }
        })<bag
    }
}

//MARK::- CUSTOM METHODS
extension ProfileViewController {
    
    
    
    func onLoad() {
        
        if (self.profileVM.userType != .loggedInUser) {
            btnEditOrMsg.isEnabled = false
            
        }
        refreshCalled = { [weak self] in
            self?.refreshInfo()
        }
        
        tagView.delegate = self
        otherUsersTagView.tagFont = UIFont.systemFont(ofSize: 14)
        tagView.tagFont = UIFont.systemFont(ofSize: 14)
        tagView.isHidden = (profileVM.userType == .otherUser)
        otherUsersTagView.isHidden = (profileVM.userType == .loggedInUser)
       // imgUserPic.kf.indicatorType = .activity
        if (profileVM.userType == .loggedInUser) {
            userDetailView.isHidden = false
            btnDisplayQr.isHidden = false
            //qrView.isHidden = false
            btnMore.isHidden = false
            btnEditOrMsg.isHidden = false
            let user = Singleton.sharedInstance.loggedInUser
            btnEditOrMsg.setImage(R.image.ic_edit_profile(), for: .normal)
            imgUserPic.image(url:  /user?.img?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder")) //.kf.setImage(with: URL(string: /user?.img?.original))
            
            
            refreshInfo()
            self.setUpView(userData: user)
        }else {
            userDetailView.isHidden = true
            btnDisplayQr.isHidden = true
            //qrView.isHidden = true
            apiIndicator.startAnimating()
            imgUserPic.image(url:  /profileVM.userData?.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder")) //kf.setImage(with: URL(string: /profileVM.userData?.imageUrl?.original))
            labelUserName.text = profileVM.userData?.userName
            btnEditOrMsg.setImage(R.image.ic_message(), for: .normal)
            if isMentioning{
                profileVM.getUserNameProfileData(userName: userName) { [weak self](status) in
                    if status {
                        self?.btnEditOrMsg.isEnabled = true
                        let data = self?.profileVM.userInfo.value
                        self?.setUpView(userData: data)
                    }
                }
            }else{
                profileVM.getUserProfileData(userId: (profileVM.userType == .loggedInUser) ? nil : profileVM.userId) { [weak self](status) in
                    if status {
                        self?.btnEditOrMsg.isEnabled = true
                        let data = self?.profileVM.userInfo.value
                        self?.setUpView(userData: data)
                    }
                }
            }
        }
        
        
        
    }
    
    func refreshInfo(){
        self.profileVM.getUserProfileData(userId: (self.profileVM.userType == .loggedInUser) ? /Singleton.sharedInstance.loggedInUser?.id  : self.profileVM.userId) { [weak self](status) in
            if status {
                if (self?.profileVM.userType == .loggedInUser) {
                    let user = Singleton.sharedInstance.loggedInUser
                    user?.followerCount = self?.profileVM.userInfo.value?.followerCount
                    user?.followingCount = self?.profileVM.userInfo.value?.followingCount
                    user?.referralCode = self?.profileVM.userInfo.value?.referralCode
                    Singleton.sharedInstance.loggedInUser = user
                    self?.setUpView(userData: Singleton.sharedInstance.loggedInUser)
                }else{
                    
                    if self?.userName == Singleton.sharedInstance.loggedInUser?.userName || self?.profileVM.userId == /Singleton.sharedInstance.loggedInUser?.id{
                        self?.btnEditOrMsg.isHidden = true
                        self?.btnMore.isHidden = true
                        self?.bgViewEditBtn.isHidden = true
                    }
                    self?.btnEditOrMsg.isEnabled = true
                    let data = self?.profileVM.userInfo.value
                    self?.setUpView(userData: data)
                }
            }
        }
    }
    
    func setUpView(userData : Any?){
        
        
        
        btnBack.isHidden = (profileVM.userType == .loggedInUser)
        labelProfile.isHidden = (profileVM.userType != .loggedInUser)
        if let userDetail = userData as? User {
//            labelUserType?.image = R.i
            labelUserType?.image = /userDetail.userType == "STUDENT" ? R.image.ic_stu() : R.image.ic_tea()
            btnFollowers?.isEnabled = true
            btnFollowing?.isEnabled = true
            imageIsPrivate?.isHidden = !(/userDetail.isAccountPrivate)
            btnFollow.isHidden = true
            followUnfollowView.isHidden = true
            labelUserName.text = userDetail.firstName
            labelDesignation.text = userDetail.designation
            labelBioHeader.text =  /userDetail.bio == "" ? "" : "Bio"
            labelBio.text = userDetail.bio
            labelFollowerCount.text = userDetail.followerCount?.toString
            labelFollowingCount.text = userDetail.followingCount?.toString
            imgUserPic.image(url:  /userDetail.img?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder")) //.kf.setImage(with: URL(string: /userDetail.img?.original))
            var interstTags : [String] = []
            Singleton.sharedInstance.selectedInterests?.forEach({ (tags) in
                interstTags.append(/tags.category)
            })
            tagView.removeAll()
            tagView.append(contentsOf: interstTags)
            if (self.profileVM.userType == .loggedInUser) {
               let userData = Singleton.sharedInstance.loggedInUser
                labelUserType?.image = /userData?.userType == "STUDENT" ? R.image.ic_stu() : R.image.ic_tea()
                labelEmail.text = /userData?.email
                labelGender.text = /userData?.gender
                labelDesig?.text = /userData?.designation
                labelWorkplace.text = /userData?.company
                labelWebsite.text = /userData?.website
                viewWebsite.isHidden = (/userData?.website == "")
                viewWorkplace.isHidden = (/userData?.company == "")
                viewEmail.isHidden = (/userData?.email == "")
                viewGender.isHidden = (/userData?.gender == "")
                viewDesignation.isHidden = (/userData?.designation == "")
                
                let phnNumber = /userData?.countryCode + /userData?.phoneNumber
                labelPhoneNo.text = /phnNumber
                viewPhoneNumber.isHidden = (/phnNumber == "")
//                labelEmail: UILabel!
//                labelPhoneNo: UILabel!
//                labelWorkplace: UILabel!
//                labelWebsite:
                //                let user = Singleton.sharedInstance.loggedInUser
                //                user?.followerCount = self.profileVM.userInfo.value?.followerCount
                //                user?.followingCount = self.profileVM.userInfo.value?.followingCount
                //                Singleton.sharedInstance.loggedInUser = user
            }else{
                
                if self.userName == Singleton.sharedInstance.loggedInUser?.userName || self.profileVM.userId == /Singleton.sharedInstance.loggedInUser?.id{
                    labelUserType?.image = /Singleton.sharedInstance.loggedInUser?.userType == "STUDENT" ? R.image.ic_stu() : R.image.ic_tea()
                    self.btnEditOrMsg.isHidden = true
                    self.btnMore.isHidden = true
                    self.bgViewEditBtn.isHidden = true
                }else{
                    self.btnEditOrMsg.isHidden = false
                    self.btnEditOrMsg.isEnabled = true
                    self.bgViewEditBtn.isHidden = false
                    self.btnMore.isHidden = false
                }
                
            }
        }else if let  userDetail = userData as? UserList {
            
            btnFollowers.isEnabled = false
            btnFollowing.isEnabled = false
            
            if (self.profileVM.userType == .loggedInUser) {
                let user = Singleton.sharedInstance.loggedInUser
                labelUserType?.image = /Singleton.sharedInstance.loggedInUser?.userType == "STUDENT" ? R.image.ic_stu() : R.image.ic_tea()
                user?.followerCount = self.profileVM.userInfo.value?.followerCount
                user?.followingCount = self.profileVM.userInfo.value?.followingCount
                Singleton.sharedInstance.loggedInUser = user
            }else{
                if self.userName == Singleton.sharedInstance.loggedInUser?.userName || self.profileVM.userId == /Singleton.sharedInstance.loggedInUser?.id{
                    labelUserType?.image = /Singleton.sharedInstance.loggedInUser?.userType == "STUDENT" ? R.image.ic_stu() : R.image.ic_tea()
                    self.btnEditOrMsg.isHidden = true
                    self.bgViewEditBtn.isHidden = true
                    self.btnMore.isHidden = true
                    imgUserPic.image(url:  /Singleton.sharedInstance.loggedInUser?.img?.original,placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /Singleton.sharedInstance.loggedInUser?.img?.original))
                }else{
                    labelUserType?.image = /userDetail.userType == "STUDENT" ? R.image.ic_stu() : R.image.ic_tea()
                    self.btnEditOrMsg.isHidden = false
                    self.btnEditOrMsg.isEnabled = true
                    self.btnMore.isHidden = false
                    self.bgViewEditBtn.isHidden = false
                    btnEditOrMsg?.isHidden = /userDetail.isAccountPrivate && !(/userDetail.isFollowing)
                    self.bgViewEditBtn.isHidden = /userDetail.isAccountPrivate && !(/userDetail.isFollowing)
                    imgUserPic.image(url:  /userDetail.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /userDetail.imageUrl?.original))
                }
            }
            imageIsPrivate?.isHidden = !(/userDetail.isAccountPrivate)
            
            btnFollow?.isHidden = (/userDetail.id == /Singleton.sharedInstance.loggedInUser?.id)
            followUnfollowView.isHidden = (/userDetail.id == /Singleton.sharedInstance.loggedInUser?.id)
            btnFollow?.isSelected = /userDetail.isFollowing
            if /userDetail.askForFollowBack {
                btnFollow?.setTitle("Follow Back", for: .normal)
            }
            labelUserName?.text = userDetail.userName
            labelDesignation?.text = userDetail.designation
            labelBio?.text = userDetail.bio
            labelBioHeader?.text =  /userDetail.bio == "" ? "" : "Bio"
            labelFollowerCount?.text = userDetail.followerCount?.toString
            labelFollowingCount?.text = userDetail.followingCount?.toString
            
            var interstTags : [String] = []
            userDetail.interestTags?.forEach({ (tags) in
                interstTags.append(/tags.category)
            })
            otherUsersTagView.removeAll()
            otherUsersTagView.append(contentsOf: interstTags)
        }
        constraintTopInterestHeader.constant = /labelBioHeader.text == "" ? -20 : 28
        tableView.sizeFooterToFit()
    }
    
    func selfUser(){
        
    }
    
}


//MARK::- TAG VIEW DELEGATE
extension ProfileViewController : TagsDelegate {
    
    func tagsLastTagAction(_ tagsView: TagsView, tagButton: TagButton) {
        guard let interestVc = R.storyboard.main.selectInterestsViewController() else { return }
        interestVc.interestsVM.isFromEditProfile = true
        interestVc.interestsVM.selectedInterests = Singleton.sharedInstance.selectedInterests ?? []
        interestVc.interestsVM.selectedFilterInterests = { [weak self] (interests) in
            self?.tagView.removeAll()
            self?.profileVM.userInfo.value?.interestTags = interests
            var interstTags : [String] = []
            interests?.forEach({ (tags) in
                interstTags.append(/tags.category)
            })
            Singleton.sharedInstance.selectedInterests = interests
            self?.tagView.append(contentsOf: interstTags)
        }
//        self.pushVC(interestVc)
        self.present(interestVc, animated: true, completion: nil)
    }
    
}
