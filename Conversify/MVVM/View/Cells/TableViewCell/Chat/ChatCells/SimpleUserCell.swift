//
//  SimpleUserCell.swift
//  Conversify
//
//  Created by Apple on 26/11/18.
//

import UIKit
import IBAnimatable

class SimpleUserCell: UITableViewCell {

    @IBOutlet weak var imgProfile: AnimatableImageView!
    @IBOutlet weak var lblName: UILabel!
    
    var user: User? {
        didSet {
            lblName.text = /user?.userName
            imgProfile.setImage(image: user?.img?.thumbnail, placeholder: R.image.ic_account())
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}

