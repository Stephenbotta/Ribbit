//
//  ProfileTableViewCell.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 08/10/18.
//

import UIKit
import IBAnimatable

class ProfileTableViewCell: BaseTableViewCell {

    //MARK::- OUTLETS
    
    @IBOutlet weak var labelHint: UILabel!
    @IBOutlet weak var labelHeader: UILabel!
    @IBOutlet weak var labelValue: AnimatableTextField!
    @IBOutlet weak var imgVerify: UIImageView!
    @IBOutlet weak var viewBorder: AnimatableView!
    
    //MARK::- PROPERTIES
    var header: ProfileElements?{
        didSet{
            labelHeader?.text = header?.header
            labelValue?.text = (/header?.value?.replacingOccurrences(of: " ", with: "")) == "" ? "" : header?.value
            labelValue?.toolbarPlaceholder = header?.placHolder
            labelValue?.placeholder = header?.placHolder
            labelValue?.keyboardType = header?.keyBoardType ?? .default
            labelValue?.placeholderColor = #colorLiteral(red: 0.7921568627, green: 0.7921568627, blue: 0.7921568627, alpha: 0.62)
        }
    }  
    
    //MARK::- VIEW CYCLE
    
    override func awakeFromNib() {
        super.awakeFromNib()
        labelValue.delegate = self
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}

extension ProfileTableViewCell : UITextFieldDelegate {
    
    func textFieldDidBeginEditing(_ textField: UITextField){
        if textField == labelValue{
            viewBorder.layer.borderColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
        }else{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textFieldDidEndEditing(_ textField: UITextField){
        if textField == labelValue{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
}
