//
//  ReceiverTxtCell.swift
//  NequoreUser
//
//  Created by MAC_MINI_6 on 04/08/18.
//

import UIKit

class ReceiverTxtCell: BaseChatCell {
    
    //MARK::- OUTLET
    @IBOutlet weak var imgLeadingContsraint: NSLayoutConstraint!
    @IBOutlet weak var contsraintWidthImg: NSLayoutConstraint!
    @IBOutlet weak var constraintHeightImg: NSLayoutConstraint!
    @IBOutlet weak var constraintHeightName: NSLayoutConstraint!
    @IBOutlet weak var labelName: UILabel!
    @IBOutlet weak var lblMessage: ActiveLabel!
    @IBOutlet weak var imgView: UIImageView!
    
    override func setUpData() {
        super.setUpData()
        lblMessage.highlightFontSize = 16
        lblMessage.mentionColor = lblMessage.textColor
        lblMessage.enabledTypes = [.mention]
        lblMessage.handleMentionTap { (mention) in
//            if Singleton.sharedInstance.loggedInUser?.userName == /mention{
//                return
//            }
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.userName = /mention
            vc.isMentioning = true
            UIApplication.topViewController()?.pushVC(vc)
        }
        labelName.text = /item?.senderId?.firstName?.uppercaseFirst
        lblMessage.text = /item?.mesgDetail?.message
        imgView.image(url:  /item?.senderId?.img?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /item?.senderId?.img?.thumbnail))
        
    }
    
}
