//
//  UsersTagCell.swift
//  Conversify
//
//  Created by Apple on 28/11/18.
//

import UIKit
import IBAnimatable

class UsersTagCell: UITableViewCell {

    //MARK::- OUTLETS
    @IBOutlet weak var imgUserPic: AnimatableImageView!
    @IBOutlet weak var lblFullName: UILabel!
    @IBOutlet weak var labelUserName: UILabel!
    
    var item : UserList? {
        didSet {
            labelUserName.text = item?.userName
            lblFullName.text = item?.fullName
            imgUserPic.image(url:  /item?.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /item?.imageUrl?.thumbnail))
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
