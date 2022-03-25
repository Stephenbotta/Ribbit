//
//  EditProfileCell.swift
//  Conversify
//
//  Created by Apple on 07/12/18.
//

import UIKit
import EZSwiftExtensions
import IBAnimatable

enum genderType : String {
    case Male = "1"
    case Female = "2"
    case Other = "3"
}

protocol DelegateUpdateUser: class {
    func updateUser(val: String? , row: Int , sec: Int)
    func validateUserName(userName :  String?)
}

class EditProfileCell: BaseTableViewCell {
    
    //MARK::- OUTLETS
    @IBOutlet weak var viewBottomtextF: UIView!
    @IBOutlet weak var txtfEntryField: SectionalTextField!
    @IBOutlet weak var textViewBio: UITextView!
    @IBOutlet weak var labelTitle: UILabel!
    @IBOutlet weak var btnOpenDetail: UIButton!
    @IBOutlet weak var imgVerify: UIImageView!
    @IBOutlet weak var txtfTrailingConstraint: NSLayoutConstraint!
    @IBOutlet weak var textFieldBgView: UIView!
    @IBOutlet weak var textViewBgView: UIView!
    
    //MARK::- PROPERTIES
    weak var delegate: DelegateUpdateUser?
    var indexPath : IndexPath?{
        didSet{
            txtfEntryField?.row = indexPath?.row
            txtfEntryField?.section = indexPath?.section
        }
    }
    
    var item : ItemDetail? {
        didSet {
            textViewBio.delegate = self
            txtfEntryField.delegate = self
            txtfEntryField.keyboardType = item?.keyBoardType ?? .default
            labelTitle.text = item?.title
            txtfEntryField.placeholder = item?.placeHolder
            txtfEntryField.isUserInteractionEnabled = (item?.keyBoardType != UIKeyboardType.phonePad)
            if item?.text != nil || !(/item?.text?.isEmpty) {
                txtfEntryField.text = item?.text
                textViewBio.text = /item?.text
            }
            btnOpenDetail.isHidden = (item?.title != "Gender" )
            if indexPath?.section == 0 && indexPath?.row == 1{
                imgVerify.isHidden = false
                txtfTrailingConstraint.constant = 32 + 16
            }else {
                imgVerify.isHidden = true
                txtfTrailingConstraint.constant =  16
            }
        }
    }
    
    override func bindings() {
        btnOpenDetail.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            UtilityFunctions.show(nativeActionSheet: "Select Gender", subTitle: "", vc: ez.topMostVC , senders: [ "Male" , "Female" , "Others" ], success: {[weak self] (str, indx) in
                self?.txtfEntryField.text = str as? String
                self?.delegate?.updateUser(val: str as? String , row: /self?.indexPath?.row , sec: /self?.indexPath?.section)
            })
        })<bag
        txtfEntryField.addTarget(self, action: #selector(self.textFieldDidChange(textField:)), for: .editingChanged)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}

//MARK::- TEXT FIELD DELEGATE
extension EditProfileCell : UITextFieldDelegate , UITextViewDelegate{
    
    func textViewDidEndEditing(_ textView: UITextView){
         delegate?.updateUser(val: textView.text , row: /indexPath?.row , sec: /indexPath?.section)
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        delegate?.updateUser(val: textField.text , row: /indexPath?.row , sec: /indexPath?.section)
    }
    
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        let newText = (textView.text as NSString).replacingCharacters(in: range, with: text)
        let numberOfChars = newText.count
        return numberOfChars < 150    // 10 Limit Value
    }
        
    @objc func textFieldDidChange(textField: UITextField){
        if indexPath?.section == 0 && indexPath?.row == 1{
            delegate?.validateUserName(userName: /textField.text)
        }
        
    }
}

class SectionalTextField : AnimatableTextField {
    var row: Int?
    var section: Int?
}
