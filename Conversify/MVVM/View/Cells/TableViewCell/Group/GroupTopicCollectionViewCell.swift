//
//  GroupTopicCollectionViewCell.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 14/11/18.
//

import UIKit

class GroupTopicCollectionViewCell: UICollectionViewCell {
    
    //MARK: - Outlets
    @IBOutlet weak var btnCross: UIButton!
    @IBOutlet weak var lblGroupName: UILabel!
    @IBOutlet weak var lblGroupMembers: UILabel!
    @IBOutlet weak var btnFavourite: UIButton!
    @IBOutlet weak var imgGroup: UIImageView!
    
    //MARK::- PROPERTIES
    var group: Any?{
        didSet{
            guard let suggestedItem = group as? SuggestedGroup else { return }
            lblGroupName?.text = suggestedItem.groupName?.uppercased()
            lblGroupMembers?.text = /suggestedItem.memberCounts?.toString + (/suggestedItem.memberCounts == 1 ? " member" : " members" )
            imgGroup?.image(url:  /suggestedItem.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /suggestedItem.imageUrl?.original))
            btnCross?.isHidden = !(/suggestedItem.isPrivate)
            btnFavourite?.isSelected = /suggestedItem.isMember
        }
    }
    
    
    //MARK: - Button Actions
    @IBAction func addToFavourite(_ sender: Any) {
        
    }
    
    @IBAction func cancel(_ sender: Any) {
        
    }
}
