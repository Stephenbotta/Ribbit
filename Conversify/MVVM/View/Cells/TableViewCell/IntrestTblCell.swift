//
//  IntrestTblCell.swift
//  Conversify
//
//  Created by Apple on 09/01/20.
//

import UIKit

class IntrestTblCell: UITableViewCell {

    @IBOutlet weak var lblTitle: UILabel!
    @IBOutlet weak var imgView: UIImageView!
    
    var interest:Interests?{
        
        didSet{
            
            self.lblTitle.text = /interest?.category?.uppercaseFirst
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
