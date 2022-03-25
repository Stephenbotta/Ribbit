//
//  SelectInterestCollectionViewCell.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 13/10/18.
//

import UIKit
import SDWebImage

class SelectInterestCollectionViewCell: UICollectionViewCell {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var imageInterest: UIImageView!
    @IBOutlet weak var imageIsSelected: UIImageView!
    @IBOutlet weak var labelName: UILabel!
    
    
    //MARK::- PROPERTIES
    
    var interest: Interests? {
        didSet{
            labelName?.text = /interest?.category?.uppercaseFirst
            //imageInterest?.kf.setImage(with: URL(string: /interest?.imageStr))
            //imageInterest?.kf.setImage(with: URL(string: /interest?.imageStr), placeholder: #imageLiteral(resourceName: "ic_placeholder"))
            imageInterest.image(url:  /interest?.imageStr, placeholder: #imageLiteral(resourceName: "ic_placeholder"))
            imageIsSelected.image = /interest?.isSelected ? R.image.ic_tick() : R.image.ic_add()
        }
    }
    
    var selectedIndex : Bool?{
        didSet{
            imageIsSelected.image = /selectedIndex ? R.image.ic_tick() : R.image.ic_add()
        }
    }
    
    
    
}
