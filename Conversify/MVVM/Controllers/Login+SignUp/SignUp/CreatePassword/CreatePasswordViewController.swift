//
//  CreatePasswordViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 08/10/18.
//

import UIKit
import IBAnimatable

class CreatePasswordViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var textFieldPassWord: UITextField!
    @IBOutlet weak var btnShow: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnNext: UIButton!
    @IBOutlet weak var viewBorder: AnimatableView!
    
    //MARK::- PROPERTIES
    var createPassVM = CreatePasswordViewModal()
    
    //MARK::- BINDINGS
    
    override func bindings() {
        textFieldPassWord.delegate = self
        (textFieldPassWord.rx.text <-> createPassVM.password)<bag
        
        createPassVM.isValid.subscribe { (valid) in
            self.btnNext.isEnabled = /valid.element
            self.btnNext.alpha = /valid.element ? 1.0 : 0.4
            }<bag
        
        btnNext.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.proceed()
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
//            self?.dismissVC(completion: nil)
        })<bag
        
        btnShow.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.btnShow.isSelected = /self?.btnShow.isSelected.toggle()
            self?.textFieldPassWord.isSecureTextEntry = /self?.textFieldPassWord.isSecureTextEntry.toggle()
        })<bag
    }
    
    func proceed(){
        createPassVM.proceed { [weak self] (status) in
            if status{
                guard let createProfilVC = R.storyboard.main.createProfileViewController() else { return }
                let user1 = createPassVM.userInfo.value
                user1?.password = createPassVM.password.value
                
                let ccDet = CountryParam()
                ccDet.dialCode = /createPassVM.countryCode.value
                
                createProfilVC.createProVM = CreateProfileViewModal(user: user1 , countryDet: ccDet)
                self?.pushVC(createProfilVC)
//                self?.present(createProfilVC, animated: true, completion: nil)
            }
        }
    }
    
}


extension CreatePasswordViewController : UITextFieldDelegate {
    
    func textFieldDidBeginEditing(_ textField: UITextField){
        if textField == textFieldPassWord{
            viewBorder.layer.borderColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
        }else{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textFieldDidEndEditing(_ textField: UITextField){
        if textField == textFieldPassWord{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
}
