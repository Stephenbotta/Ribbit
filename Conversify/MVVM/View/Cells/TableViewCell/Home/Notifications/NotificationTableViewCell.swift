//
//  NotificationCell.swift
//  Conversify
//
//  Created by Apple on 12/11/18.
//

import UIKit
import IBAnimatable

class NotificationTableViewCell: BaseTableViewCell {
    
    //MARK: - Outlets
    @IBOutlet weak var btnAccept: AnimatableButton?
    @IBOutlet weak var btnReject: AnimatableButton?
    @IBOutlet weak var imgProfilePic: AnimatableImageView!
    @IBOutlet weak var txtMessage: UITextView!
    @IBOutlet weak var lblTime: UILabel!
    @IBOutlet weak var imgPost: AnimatableImageView?
    @IBOutlet weak var contraintHeightImagePost: NSLayoutConstraint!
    @IBOutlet weak var btnProfile: UIButton!
    
    
    //MARK::- PROPERTIES
    var acceptRejectBlock: ((Bool) -> ())?
    
    //MARK: - View Hierarchy
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        txtMessage?.textContainerInset = UIEdgeInsets.zero
        txtMessage?.textContainer.lineFragmentPadding = 0
        txtMessage?.delegate = self
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    var item: AppNotification? {
        didSet {
            imgProfilePic?.image(url:  /item?.user?.imageUrl?.thumbnail, placeholder: R.image.ic_account() ?? #imageLiteral(resourceName: "ic_account")) //.kf.setImage(with: URL(string: /item?.user?.imageUrl?.thumbnail), placeholder: R.image.ic_account())
            
            imgPost?.image(url:  /item?.post?.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /item?.post?.imageUrl?.thumbnail))
            
            lblTime?.text = item?.createdOn?.toString(format: "hh:mm a")
            let username = /item?.user?.userName
            if item?.actionPerformed == true {
                btnAccept?.isEnabled = false
                btnReject?.isEnabled = false
            }
            
            let textColour = UIColor.white
            var group = ""
            var message = ""
            var objectId = ""
            var linkColour = UIColor(red: 0.0/255.0, green: 105.0/255.0, blue: 225.0/255.0, alpha: 1)
            contraintHeightImagePost?.constant = 40
            
            if let type = item?.type {
                switch type {
                    
                case .requestFollow:
                    message = " request to follow "
                    group = /item?.group?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    
                case .acceptRequestFollow:
                    
                    message = " has accepted your follow request "
                    group = ""
                    objectId = ""
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                    
                case .joinedGroup:
                    message = " you are now member of network "
                    group = /item?.group?.title?.uppercaseFirst
                    objectId = ""
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                case .joinedVenue:
                    message = " you are now member of venue "
                    group = /item?.venue?.title?.uppercaseFirst
                    objectId = ""
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                case .tagComment:
                    message = " mentioned you in comment "
                    group = ""
                    objectId = ""
                    linkColour = textColour
                    contraintHeightImagePost?.constant = /item?.post?.imageUrl?.thumbnail == "" ? 0 : 40
                    
                case .tagReply:
                    message = " mentioned you in reply "
                    group = ""
                    objectId = ""
                    linkColour = textColour
                    contraintHeightImagePost?.constant = /item?.post?.imageUrl?.thumbnail == "" ? 0 : 40
                    
                case .chat , .groupChat , .venueChat:
                    break
                    
                case .follow:
                    message = " has started following you "
                    group = ""
                    objectId = ""
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                case .post:
                    
                    message = " has posted on your group "
                    group = /item?.group?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                case .acceptInviteGroup:
                    
                    message = " has accepted your invite for group "
                    group = /item?.group?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                    
                case .acceptInviteVenue:
                    message = " has accepted your invite for venue "
                    group = /item?.venue?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                    
                case .acceptRequestGroup:
                    message = " has accepted your request for network. Now you are the member of group "
                    group = /item?.group?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                case .acceptRequestVenue:
                    message = " has accepted your request for venue. Now you are the member of venue "
                    group = /item?.venue?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                case .group:
                    message = " has joined your group "
                    group = /item?.group?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                case .venue:
                    message = " has joined your venue "
                    group = /item?.venue?.title?.uppercaseFirst
                    objectId = "venue://" + /item?.venue?.id
                    linkColour = textColour
                    contraintHeightImagePost?.constant = 0
                    
                case .likePost:
                    contraintHeightImagePost?.constant = /item?.post?.imageUrl?.thumbnail == "" ? 0 : 40
                    message = " liked your post "
                    group = /item?.post?.postCategory?.categoryName?.uppercaseFirst
                    objectId = "post://" + /item?.post?.postCategory?.id
                    
                case .likeReply:
                    contraintHeightImagePost?.constant = /item?.post?.imageUrl?.thumbnail == "" ? 0 : 40
                    message = " liked your reply " + /item?.reply?.reply
                    group = ""
                    objectId = "post://" + /item?.post?.postCategory?.id
                    
                case .likeComment:
                    contraintHeightImagePost?.constant = /item?.post?.imageUrl?.thumbnail == "" ? 0 : 40
                    message = " liked your comment: " + /item?.comment?.comment
                    group = ""
                    objectId = "post://" + /item?.post?.postCategory?.id
                    
                case .comment:
                    contraintHeightImagePost?.constant = /item?.post?.imageUrl?.thumbnail == "" ? 0 : 40
                    message = " commented: " + /item?.comment?.comment
                    group = ""
                    objectId = "post://" + /item?.post?.postCategory?.id
                    
                case .groupRequest:
                    message = " sent request to join "
                    group = /item?.group?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    
                case .venueRequest:
                    message = " sent request to join "
                    group = /item?.venue?.title?.uppercaseFirst
                    objectId = "venue://" + /item?.venue?.id
                    linkColour = textColour
                    
                case .reply:
                    message = " replied on your comment "
                    group = /item?.post?.postCategory?.categoryName?.uppercaseFirst
                    objectId = "post://" + /item?.post?.postCategory?.id
                    
                case .groupInvite:
                    message = " invited you to join "
                    group = /item?.group?.title?.uppercaseFirst
                    objectId = "group://" + /item?.group?.id
                    linkColour = textColour
                    
                case .venueInvite:
                    message = " invited you to join "
                    group = /item?.venue?.title?.uppercaseFirst
                    objectId = "venue://" + /item?.venue?.id
                    linkColour = textColour
                    
                case .lookNearBy:
                    message = " has crossed your path " + (/item?.locName == "" ? "" : (" at " + /item?.locName + " " + /item?.locAddress))
                    group = ""
                    objectId = ""
                    linkColour = textColour
                    
                    
                case .converseNearBy:
                    message = " wants to converse with someone nearby. Tap here to check the details "
                    group = ""
                    objectId = ""
                    linkColour = textColour
                case .receivedreddempoint:
                    message = item?.text ?? ""
                    group = ""
                    objectId = ""
                    linkColour = textColour
                case .spendEarnPoint:
                    message = item?.text ?? ""
                    group = ""
                    objectId = ""
                    linkColour = textColour

                default:
                    break
//                case .crossedPath:
                    //message = item?.text ?? ""
//                    group = ""
//                    objectId = ""
//                    linkColour = textColour
                    
                }
            }
            
            contraintHeightImagePost?.constant = /item?.post?.imageUrl?.thumbnail == "" ? 0 : 40
            
            //The problem with UITextView linkTextAttributes is that it applies to all automatically detected links.
            txtMessage?.tintColor = textColour
            //trick: configure the links as part of the text view's attributed text, and set the linkTextAttributes to an empty dictionary.
            txtMessage?.linkTextAttributes = [:]
            txtMessage?.attributedText = NSMutableAttributedString().setAttributes(username, attributes: [.font: UIFont.boldSystemFont(ofSize: 14) , .foregroundColor: textColour, .link: "user://" + /item?.user?.id]).setAttributes(message, attributes: [.font: UIFont.systemFont(ofSize: 14), .foregroundColor: textColour]).setAttributes(group, attributes: [.font: UIFont.boldSystemFont(ofSize: 14), .foregroundColor: linkColour, .link: objectId])
            self.contentView.backgroundColor = UIColor.clear
            
        }
        
    }
    
    
    override func bindings() {
        guard let btn = btnProfile else { return }
        btn.rx.tap.asDriver().drive(onNext: {  [weak self] () in
//            if Singleton.sharedInstance.loggedInUser?.id == /self?.item?.pushBy?.id{
//                return
//            }
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.profileVM.userId = self?.item?.pushBy?.id
            UIApplication.topViewController()?.pushVC(vc)
        })<bag
        
    }
    
    //MARK: - Button Actions
    @IBAction func accept(_ sender: Any) {
        acceptRejectBlock?(true)
    }
    
    @IBAction func reject(_ sender: Any) {
        acceptRejectBlock?(false)
    }
}

extension NotificationTableViewCell: UITextViewDelegate {
    
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange, interaction: UITextItemInteraction) -> Bool {
        switch URL.scheme {
        case "user":
            print("user tapped \(URL.host)")
            guard let vc = R.storyboard.home.profileViewController() else { return false}
            vc.profileVM.userType = .otherUser
            vc.profileVM.userId = self.item?.user?.id
            UIApplication.topViewController()?.pushVC(vc)
        case "post":
            print("post tapped \(URL.host)")
        case "group":
            print("group tapped \(URL.host)")
        case "venue":
            print("venue tapped \(URL.host)")
        default:
            print("")
        }
        return false
    }
}
