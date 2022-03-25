//
//  PostDetailTableCell.swift
//  Conversify
//
//  Created by Apple on 19/11/18.
//

import UIKit
import IBAnimatable

protocol DelegateRepliesCell : class {
    
    func likeReply(indx : IndexPath?)
    func sendReply(indx : IndexPath?)
    func deleteReply(indx : IndexPath?)
}

class PostDetailTableCell: BaseTableViewCell {
    
    //MARK::- OUTLETS
    @IBOutlet weak var imgUserPic: AnimatableImageView!
    @IBOutlet weak var btnLike: SparkButton!
    @IBOutlet weak var btnReply: UIButton!
    @IBOutlet weak var btnTime: UIButton!
    @IBOutlet weak var btnLikesCount: UIButton!
    @IBOutlet weak var txtMessage: NonEditableTextView!
    
    @IBOutlet weak var vwReply: UIView!
    
    //MARK::- PROPERTIES
    let label = ActiveLabel()
    var row : IndexPath?
    var delegate : DelegateRepliesCell?
    var item : ReplyList?{
        didSet{
            let likeTxt = (item?.likeCount == 1) ? (/item?.likeCount?.toString + " Like") : (/item?.likeCount?.toString + " Likes")
            btnLikesCount.setTitle(likeTxt, for: .normal)
            let postdate = Date(milliseconds: /item?.createdOn)
            btnTime.setTitle(postdate.timePassed(), for: .normal)
            imgUserPic.image(url:  /item?.replyBy?.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /item?.replyBy?.imageUrl?.thumbnail))
            btnLike?.isSelected = /item?.liked
            txtMessage?.tintColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 1)
            txtMessage?.linkTextAttributes = [:]
            txtMessage.text =  /item?.reply
            txtMessage.resolvePostTags(userName: /item?.replyBy?.userName)
            self.contentView.backgroundColor = UIColor.clear
            vwReply.layer.cornerRadius = 8
            vwReply.clipsToBounds = true
        }
    }
    
    
    //MARK: - View Hierarchy
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        txtMessage?.textContainerInset = UIEdgeInsets.zero
        txtMessage?.textContainer.lineFragmentPadding = 0
        txtMessage?.delegate = self
    }
    
    
    override func bindings(){
        
        self.addLongPressGesture { [weak self] (gesture) in
            if /self?.item?.replyBy?.id != /Singleton.sharedInstance.loggedInUser?.id {
                return
            }
            UIApplication.topViewController()?.view.endEditing(true)
            UtilityFunctions.show(nativeActionSheet: "Do you want to delete this message", subTitle: "", vc: UIApplication.topViewController() ?? UIViewController(), senders: ["Delete comment"], success: { [weak self] (value, index) in
                switch index {
                default:
                    self?.delegate?.deleteReply(indx: /self?.row)
                }
            })
            
        }
        
        imgUserPic.addTapGesture { [weak self] (gesture) in
            print("pic")
            if /Singleton.sharedInstance.loggedInUser?.id == /self?.item?.replyBy?.id{
                return
            }
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.profileVM.userId = /self?.item?.replyBy?.id
            UIApplication.topViewController()?.pushVC(vc)
        }
        
        btnReply.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.delegate?.sendReply(indx: /self?.row)
        })<bag
        
        btnLike.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.btnLike.isSelected = /self?.btnLike.isSelected.toggle()
            if /self?.btnLike.isSelected{
                self?.btnLike.likeBounce(0.6)
                self?.btnLike.animate()
            }else{
                self?.btnLike.unLikeBounce(0.4)
            }
            var likeCount = /self?.item?.likeCount
            likeCount = /self?.item?.liked ? ( likeCount - 1)  : ( likeCount + 1)
            let likeTxt = (likeCount == 1) ? (/likeCount.toString + " Like") : (/likeCount.toString + " Likes")
            
            self?.btnLikesCount.setTitle(likeTxt, for: .normal)
            self?.delegate?.likeReply(indx: self?.row)
            
        })<bag
    }
   
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}

extension PostDetailTableCell :  UITextViewDelegate {
    
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange, interaction: UITextItemInteraction) -> Bool {
        switch URL.scheme {
        case "user":
            print("user tapped \(URL.host)")
            guard let vc = R.storyboard.home.profileViewController() else { return false }
            vc.profileVM.userType = .otherUser
            vc.userName = "\(/URL.host)"
            vc.isMentioning = true
            UIApplication.topViewController()?.pushVC(vc)
        case "hash":
            print("hash tapped \(URL.host)")
        case "mentioning":
            guard let vc = R.storyboard.home.profileViewController() else { return false }
            vc.profileVM.userType = .otherUser
            vc.userName = "\(/URL.host)"
            vc.isMentioning = true
            UIApplication.topViewController()?.pushVC(vc)
            print("mentioning tapped \(URL.host)")
        default:
            print("")
        }
        return false
    }
}

