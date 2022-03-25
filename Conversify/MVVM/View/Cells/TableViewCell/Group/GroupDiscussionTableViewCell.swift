//
//  GroupDiscussionTableViewCell.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import UIKit
import IBAnimatable

typealias EmptyBlock = () -> ()
typealias LikeTapped = (Int , String) -> ()
typealias Comment = (Int , String) -> ()

class GroupDiscussionTableViewCell: BaseTableViewCell {
    
    //MARK: - Outlets
    @IBOutlet weak var viewShowMedia: UIView!
    @IBOutlet weak var labelRemainingMediaCount: UILabel!
    @IBOutlet weak var viewRemainingMediaCount: AnimatableView!
    @IBOutlet weak var imgProfilePic: AnimatableImageView!
    @IBOutlet weak var lblUserName: UILabel!
    @IBOutlet weak var lblTime: UILabel!
    @IBOutlet weak var lblMessage: UILabel!
    @IBOutlet weak var lblLikes: UILabel!
    @IBOutlet weak var imgPost: UIImageView!
    @IBOutlet weak var tfReply: AnimatableTextField!
    @IBOutlet weak var btnFavourite: SparkButton!
    @IBOutlet weak var btnSend: UIButton!
    @IBOutlet weak var labelLoc: UILabel!
    
    //MARK::- PROPERTIES
    
    //    var replyTapped: EmptyBlock?
    var userName : String?
    var likeTapped: LikeTapped?
    var comment: Comment?
    var row: Int?{
        didSet{
            btnFavourite?.tag = /row
            btnSend?.tag = /row
        }
    }
    var post: ConversData?{
        didSet{
            let time = Date(milliseconds: Double(/post?.createdOn))
            lblTime?.text = time.timePassed()
            lblLikes?.text = " " + (/post?.commentCount?.toString) + " " + ( /post?.commentCount == 0 || /post?.commentCount == 1 ? "Reply" : "Replies") + " · \(/post?.likeCount?.toString) " + ( /post?.likeCount == 0 || /post?.likeCount == 1 ? "Like" : "Likes")
            userName = /post?.postBy?.id
            lblUserName?.text = /post?.postBy?.userName
            lblMessage?.text = /post?.postText
            imgPost?.image(url:  /post?.media?.first?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /post?.media?.first?.original))
            imgProfilePic?.image(url:  /post?.postBy?.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /post?.postBy?.imageUrl?.original))
            btnFavourite.isSelected = /post?.liked
            labelRemainingMediaCount?.text = "+" +  /(/post?.media?.count - 1).toString
            labelRemainingMediaCount?.isHidden = post?.media?.count == 0 || post?.media?.count == 1
            viewRemainingMediaCount?.isHidden = post?.media?.count == 0 || post?.media?.count == 1
        }
    }
    
    //MARK: - View Hierarchy
    override func awakeFromNib() {
        super.awakeFromNib()
        NotificationCenter.default.addObserver(self, selector:  #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        // Initialization code
        tfReply.delegate = self
    }
    
    override func bindings() {
        
        viewShowMedia?.onTap({ [weak self] (gesture) in
            guard let vc = R.storyboard.home.previewMediaViewController() else { return }
            vc.previewVM.media.value = self?.post?.media ?? []
            UIApplication.topViewController()?.presentVC(vc)
        })
        
        imgProfilePic.addTapGesture { [weak self] (gesture) in
            self?.leadToUserDetail()
        }
        
        lblUserName.addTapGesture { [weak self] (gesture) in
            self?.leadToUserDetail()
            
        }
        
        btnFavourite.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.btnFavourite.isSelected = /self?.btnFavourite.isSelected.toggle()
            if /self?.btnFavourite.isSelected{
                self?.btnFavourite.likeBounce(0.6)
                self?.btnFavourite.animate()
            }else{
                self?.btnFavourite.unLikeBounce(0.4)
            }
            self?.likeTapped?( /self?.btnFavourite.tag , /self?.btnFavourite.isSelected ? "1" : "2")
            
        })<bag
        btnSend.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.comment?(/self?.btnSend.tag , /self?.tfReply.text)
            self?.tfReply.text = ""
            self?.btnFavourite.isHidden = false
            self?.btnSend.isHidden = true
        })<bag
        
        
        
        
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    @objc func keyboardWillHide(_ notification: NSNotification) {
        
        if /self.tfReply.text != ""{
            self.comment?(btnSend.tag , /tfReply.text)
        }
        
        self.tfReply.text = ""
        self.btnFavourite.isHidden = false
        self.btnSend.isHidden = true
    }
    
    func leadToUserDetail(){
        self.endEditing(true)
        guard let vc = R.storyboard.home.profileViewController() else { return }
        vc.profileVM.userType = .otherUser
        vc.profileVM.userId = /userName
        UIApplication.topViewController()?.pushVC(vc)
    }
    
    
}


extension GroupDiscussionTableViewCell: UITextFieldDelegate {
    
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        btnFavourite.isHidden = true
        btnSend.isHidden = false
        return true
    }
    
    
}
