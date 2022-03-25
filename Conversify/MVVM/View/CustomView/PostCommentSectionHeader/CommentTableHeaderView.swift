//
//  CommentTableHeaderView.swift
//  Conversify
//
//  Created by Apple on 17/11/18.
//

import UIKit
import IBAnimatable

protocol DelegateCommentHeaderView : class {
    func deleteComment(indx : Int?)
    func replyOnComment(indx : Int?)
    func likeOnComment(indx : Int?)
    func loadMoreReplies(indx : Int? , _ completion:@escaping (Int)->())
    
}

class CommentTableHeaderView: UITableViewHeaderFooterView {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnTime: UIButton!
    @IBOutlet weak var indicator: UIActivityIndicatorView!
    @IBOutlet weak var btnLike: SparkButton!
    @IBOutlet weak var btnLoadMore: UIButton!
    @IBOutlet weak var btnLikesCount: UIButton!
    @IBOutlet weak var btnReply: UIButton!
    @IBOutlet weak var imgUserPic: AnimatableImageView!
    @IBOutlet weak var heightLoadMoreBtn: NSLayoutConstraint!
    @IBOutlet weak var txtMessage: NonEditableTextView!
    @IBOutlet weak var constHeightPostImgVw: NSLayoutConstraint!
    @IBOutlet weak var imgVwPost: UIImageView!
    //MARK::- PROPERTIES
    var row : Int?
    var delegate : DelegateCommentHeaderView?
    var item : CommentList? {
        didSet{
            if /item?.isIndicator{
                indicator.startAnimating()
                btnLoadMore.isEnabled = false
            }else{
                indicator.stopAnimating()
                btnLoadMore.isEnabled = true
            }
            
            let likeTxt = (item?.likeCount == 1) ? (/item?.likeCount?.toString + " Like") : (/item?.likeCount?.toString + " Likes")
            btnLikesCount.setTitle(likeTxt, for: .normal)
            let postdate = Date(milliseconds: /item?.createdOn)
            btnTime.setTitle(postdate.timePassed(), for: .normal)
            imgUserPic.image(url:  /item?.commentBy?.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder")) //.kf.setImage(with: URL(string: /item?.commentBy?.imageUrl?.thumbnail))
            
            txtMessage?.tintColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
            txtMessage?.linkTextAttributes = [:]
            txtMessage.text =  /item?.comment
            txtMessage.resolveTags(userName: /item?.commentBy?.userName)
            self.contentView.backgroundColor = UIColor.clear
            
            btnLike.isSelected = /item?.liked
            btnLoadMore.isHidden = (/item?.replyCount == 0)
            heightLoadMoreBtn.constant = (/item?.replyCount == 0) ? 0 : 32
            btnLoadMore.setTitle("Load replies (\(/item?.replyCount?.toString))", for: .normal)
            if item?.loadedElementCount == item?.replyCount {
                btnLoadMore.setTitle("Hide all replies (\(/item?.replyCount?.toString))", for: .normal)
            }else if item?.loadedElementCount != 0 {
                btnLoadMore.setTitle("Load replies (\(/item?.loadedElementCount?.toString))", for: .normal)
            }
            
           
            if ((item?.attachment?.thumbnail) != "") {
                constHeightPostImgVw.constant = 128
                imgVwPost.image(url:  /item?.attachment?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /item?.attachment?.thumbnail))
            } else {
                constHeightPostImgVw.constant = 0
            }
            
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        txtMessage?.textContainerInset = UIEdgeInsets.zero
        txtMessage?.textContainer.lineFragmentPadding = 0
        txtMessage?.delegate = self
        
        self.addLongPressGesture { [weak self] (gesture) in
            if /self?.item?.commentBy?.id != /Singleton.sharedInstance.loggedInUser?.id {
                return
            }
            UIApplication.topViewController()?.view.endEditing(true)
            UtilityFunctions.show(nativeActionSheet: "Do you want to delete this message", subTitle: "", vc: UIApplication.topViewController() ?? UIViewController(), senders: ["Delete comment"], success: { [weak self] (value, index) in
                switch index {
                default:
                     self?.delegate?.deleteComment(indx: /self?.row)
                }
            })
            
        }
        
        imgUserPic.addTapGesture { [weak self] (gesture) in
//            if Singleton.sharedInstance.loggedInUser?.id == /self?.item?.commentBy?.id{
//                return
//            }
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.profileVM.userId = /self?.item?.commentBy?.id
            UIApplication.topViewController()?.pushVC(vc)
        }
    }
    
    //MARK::- BUTTON ACTIONS
    
    @IBAction func btnActionLikeReply(_ sender: UIButton) {
        btnLike.isSelected = /btnLike.isSelected.toggle()
        if /btnLike.isSelected{
            btnLike.likeBounce(0.6)
            btnLike.animate()
        }else{
            btnLike.unLikeBounce(0.4)
        }
        var likeCount = /self.item?.likeCount
        likeCount = /self.item?.liked ? ( likeCount - 1)  : ( likeCount + 1)
        let likeTxt = (likeCount == 1) ? (/likeCount.toString + " Like") : (/likeCount.toString + " Likes")
        btnLikesCount.setTitle(likeTxt, for: .normal)
        delegate?.likeOnComment(indx: /row)
    }
    
    @IBAction func btnActionReplyOnComment(_ sender: UIButton) {
        delegate?.replyOnComment(indx: /row)
    }
    
    @IBAction func btnActionLoadMoreReplies(_ sender: UIButton) {
        indicator.startAnimating()
        delegate?.loadMoreReplies(indx: /row, { (a) in
            let count = /self.item?.replyCount - a
            self.item?.loadedElementCount = a
            if self.item?.replyCount == a{
                self.btnLoadMore.setTitle("Hide all replies (\(/self.item?.replyCount?.toString))", for: .normal)
            }else {
                self.btnLoadMore.setTitle("Load replies (\(/count.toString))", for: .normal)
            }
        })
    }
}

extension CommentTableHeaderView :  UITextViewDelegate {
    
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
            print("mentioning tapped \(URL.host)")
            guard let vc = R.storyboard.home.profileViewController() else { return false }
            vc.profileVM.userType = .otherUser
            vc.userName = "\(/URL.host)"
            vc.isMentioning = true
            UIApplication.topViewController()?.pushVC(vc)
        default:
            print("")
        }
        return false
    }
}
