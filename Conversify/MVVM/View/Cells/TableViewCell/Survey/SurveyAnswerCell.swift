//
//  SurveyAnswerCell.swift
//  Conversify
//
//  Created by Apple on 11/12/19.
//

import UIKit

class SurveyAnswerCell: UITableViewCell {

    @IBOutlet weak var labelAnswer: UILabel!
    @IBOutlet weak var btnCheckbox: UIButton!{
        didSet{
            btnCheckbox.isUserInteractionEnabled = false
        }
    }
    
    var item: SurveyAnswer?{
        didSet{
            labelAnswer.text = /item?.name
           // btnCheckbox.isSelected = /item?.isSelected
        
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
