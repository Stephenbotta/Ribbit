//
//  SuggestedGroupCollectionViewCell.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 13/11/18.
//

import UIKit


class SuggestedGroupCollectionViewCell: BaseCollectionViewCell {
    
    @IBOutlet weak var imageIsPrivate: UIImageView!
    @IBOutlet weak var btnCross: UIButton!
    @IBOutlet weak var lblGroupName: UILabel!
    @IBOutlet weak var lblGroupMembers: UILabel!
    @IBOutlet weak var btnFavourite: UIButton!
    @IBOutlet weak var imgGroup: UIImageView!
    @IBOutlet weak var labelRequestStatus: UILabel!
    
    //MARK::- SUGGESTED GROUPS
    var likeTapped: LikeTapped?
    var row: Int?{
        didSet{
            btnFavourite.tag = /row
        }
    }
  
    var suggested: Any?{
        didSet{
            guard let suggestedItem = suggested as? SuggestedGroup else { return }
            lblGroupName?.text = suggestedItem.groupName?.uppercased()
            lblGroupMembers?.text = /suggestedItem.memberCounts?.toString + (/suggestedItem.memberCounts == 1 ? " member" : " members" )
            imgGroup?.image(url:  /suggestedItem.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /suggestedItem.imageUrl?.original))
            imageIsPrivate?.isHidden = !(/suggestedItem.isPrivate)
            btnFavourite?.isSelected = /suggestedItem.isMember
            switch RequestStatus(rawValue: /suggestedItem.requestStatus) ?? .none{
            case .pending:
                labelRequestStatus.text = "PENDING"
            case .rejected , .none:
                labelRequestStatus.text = ""
            }
        }
    }
    
    override func bindings() {
        
    }
    
}


