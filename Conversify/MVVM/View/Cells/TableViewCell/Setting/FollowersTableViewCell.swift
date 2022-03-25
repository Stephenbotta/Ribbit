//
//  FollowersTableViewCell.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 11/01/19.
//

import UIKit

class FollowersTableViewCell: UITableViewCell {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var labelName: UILabel!
    @IBOutlet weak var imageUser: UIImageView!
    
    //MARK::- PROPERTIES
    

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
