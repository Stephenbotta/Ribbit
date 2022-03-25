//
//  SenderTxtCell.swift
//  NequoreUser
//
//  Created by MAC_MINI_6 on 04/08/18.
//

import UIKit
import IBAnimatable

class SenderTxtCell: BaseChatCell {
    
    @IBOutlet weak var lblMessage: ActiveLabel!
    
    @IBOutlet weak var viewBase: AnimatableView!
    
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
        lblMessage.text = item?.mesgDetail?.message
        self.updateConstraints()
        self.layoutIfNeeded()
       
        
        //    self.reloadInputViews()
    }
    
}
