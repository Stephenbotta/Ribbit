//
//  DiscoverPeopleCell.swift
//  Conversify
//
//  Created by Apple on 13/11/18.
//

import UIKit
import IBAnimatable
import EZSwiftExtensions

class DiscoverPeopleTableViewCell: UITableViewCell {
    
    //MARK: - Outlets
    @IBOutlet weak var imgProfilePic: AnimatableImageView!
    @IBOutlet weak var lblUserName: UILabel!
    @IBOutlet weak var lblDesignation: UILabel!
    @IBOutlet weak var lblTime: UILabel!
    @IBOutlet weak var btnMsg: UIButton!
    
    //MARK::- PEOPLE
    
    var ppl: UserCrossed?{
        didSet{
            imgProfilePic?.image(url:  /ppl?.crossedUserId?.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /ppl?.crossedUserId?.imageUrl?.original))
            lblUserName?.text = ppl?.crossedUserId?.fullName?.uppercaseFirst
            lblDesignation?.text = ""
            lblTime?.text = /Date(milliseconds: Double(/ppl?.time)).timePassed()
        }
    }
    
    //MARK: - View Hierarchy
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    //MARK: - Button Actions
    @IBAction func startChat(_ sender: Any) {
        
        guard let vc = R.storyboard.chats.chatViewController() else { return }
         vc.isFromChat = false
        vc.receiverData = ppl?.crossedUserId
        vc.chatModal.assignConvoId = { [weak self] convoId in
            
        }
        vc.chatModal.conversationId.value = ppl?.conversationId
        Singleton.sharedInstance.conversationId =  /ppl?.conversationId
        vc.chatingType = .oneToOne
        ez.topMostVC?.pushVC(vc)
        
    }
    
}
