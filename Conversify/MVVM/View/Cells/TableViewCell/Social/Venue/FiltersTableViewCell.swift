//
//  FiltersTableViewCell.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/11/18.
//

import UIKit

class FiltersTableViewCell: UITableViewCell {

    
    //MARK::- OUTLETS
    
    @IBOutlet weak var labelTitle: UILabel!
    
    
    //MARK::- PROPERTIES
    
    var title: String?{
        didSet{
            labelTitle?.text = /title?.uppercaseFirst
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
