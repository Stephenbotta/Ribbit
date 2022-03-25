//
//  GroupListingTableViewCell.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 13/11/18.
//

import UIKit
import IBAnimatable

class GroupListingTableViewCell: UITableViewCell {
    
    //MARK: - Outlets
    @IBOutlet weak var imgGroup: AnimatableImageView!
    @IBOutlet weak var lblGroupName: UILabel!
    @IBOutlet weak var lblCount: AnimatableLabel!
    @IBOutlet weak var imageJoined: AnimatableImageView!
    @IBOutlet weak var labelMemberCount: UILabel!
    @IBOutlet weak var imageIsPrivate: UIImageView!
    @IBOutlet weak var labelRequestStatus: UILabel!
    
    //MARK::- PROPERTIES
    var groups: Any?{
        didSet{
            guard let yourGroup = groups as? YourGroup else { return }
            lblGroupName?.text = yourGroup.groupName?.uppercaseFirst
            lblCount?.text = /yourGroup.unReadCounts?.toString
            lblCount.isHidden = /yourGroup.unReadCounts == 0
            imgGroup?.image(url:  /yourGroup.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /yourGroup.imageUrl?.original))
            labelMemberCount.text = /yourGroup.memberCounts?.toString +  (/yourGroup.memberCounts == 1 ? " member" : " members" )
            if /yourGroup.createdBy == Singleton.sharedInstance.loggedInUser?.id {
                imageJoined?.image(url:  /Singleton.sharedInstance.loggedInUser?.img?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /Singleton.sharedInstance.loggedInUser?.img?.thumbnail))
            }else{
                imageJoined.image = R.image.ic_joined()
            }
            imageIsPrivate.isHidden = !(/yourGroup.isPrivate)
            labelRequestStatus?.text = ""
        }
        
    }
    
    var group: SuggestedGroup?{
        didSet{
            lblGroupName?.text = group?.groupName?.uppercaseFirst
            lblCount.isHidden = true
            imgGroup?.image(url:  /group?.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /group?.imageUrl?.original))
            labelMemberCount.isHidden = true
            imageIsPrivate?.isHidden = !(/group?.isPrivate)
            switch RequestStatus(rawValue: /group?.requestStatus) ?? .none{
            case .pending:
                labelRequestStatus.text = "PENDING"
            case .rejected , .none:
                labelRequestStatus.text = ""
            }
            imageJoined.isHidden = !(/group?.isMember)
            if /group?.createdBy == Singleton.sharedInstance.loggedInUser?.id {
                imageJoined?.image(url:  /Singleton.sharedInstance.loggedInUser?.img?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /Singleton.sharedInstance.loggedInUser?.img?.thumbnail))
            }else{
                imageJoined.image = R.image.ic_joined()
            }
            
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
    
}
