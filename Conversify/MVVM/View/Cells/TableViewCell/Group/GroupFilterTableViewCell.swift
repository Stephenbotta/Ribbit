//
//  GroupFilterCell.swift
//  Conversify
//
//  Created by Apple on 12/11/18.
//

import UIKit

class GroupFilterTableViewCell: UITableViewCell {

    //MARK: - Outlets
    @IBOutlet weak var imgFilter: UIImageView!
    @IBOutlet weak var lblTitle: UILabel!
    
    
    var interest: Interests? {
        didSet{
            lblTitle?.text = /interest?.category?.uppercaseFirst
            imgFilter?.image(url: /interest?.img?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /interest?.img?.original))
            
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
