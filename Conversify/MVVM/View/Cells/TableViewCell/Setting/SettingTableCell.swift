//
//  SettingTableCell.swift
//  Conversify
//
//  Created by Apple on 04/12/18.
//

import UIKit

class SettingTableCell: UITableViewCell {

    //MARK::- OUTLETS
    @IBOutlet weak var labelTitle: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
