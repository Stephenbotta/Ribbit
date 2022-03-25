//
//  SelectCategoryTableViewCell.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit

class SelectCategoryTableViewCell: UITableViewCell {

    //MARK::- OUTLETS
    @IBOutlet weak var labelVenueName: UILabel!
    @IBOutlet weak var imageVenue: UIImageView!
    
    //MARK::- PROPERTIES
    var interest: Interests? {
        didSet{
            labelVenueName?.text = /interest?.category?.uppercaseFirst
            imageVenue?.image(url:  /interest?.img?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /interest?.img?.original))
        }
    }
    
    
    //MARK::- VIEW CYCLE
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
