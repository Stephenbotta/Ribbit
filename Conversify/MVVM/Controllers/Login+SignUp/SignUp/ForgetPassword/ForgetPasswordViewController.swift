//
//  ForgetPasswordViewController.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 14/10/18.
//

import UIKit
import IBAnimatable

class ForgetPasswordViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var constraintWidthButtonCC: NSLayoutConstraint!
    @IBOutlet weak var constraintHeightButtonCC: NSLayoutConstraint!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var labelPhone: UILabel!
    @IBOutlet weak var textFieldPhoneNum: UITextField!
    @IBOutlet weak var btnSelectCountryCode: UIButton!
    @IBOutlet weak var labelEmailID: UILabel!
    @IBOutlet weak var btnNext: AnimatableButton!
    @IBOutlet weak var imageFlag: UIImageView!
    
    //MARK::- PROPERTIES
    var registerVM = RegisterViewModal()
    
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
        // Do any additional setup after loading the view.
    }
    
    
    override func bindings() {
        (textFieldPhoneNum.rx.text <-> registerVM.email)<bag
        (textFieldPhoneNum.rx.text <-> registerVM.phoneNumber)<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

//            self?.dismissVC(completion: nil)
            self?.popVC()
        })<bag
        
        registerVM.countryDet.asObservable().subscribe(onNext: { [weak self] (countryDetails) in
            guard let safeValue = countryDetails else { return }
            self?.btnSelectCountryCode.setTitle(safeValue.dialCode, for: .normal)
            self?.imageFlag.image =  safeValue.countryImage
        })<bag
        
        btnSelectCountryCode.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.registerVM.getCountryCode()
        })<bag
        
        
        let nextBtnTapSignal = btnNext.rx.tap
        nextBtnTapSignal.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)
            self?.registerVM.register()
        })<bag
        
        registerVM.successfullyRegistered.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    UtilityFunctions.makeToast(text: "Reset password link sent succesfully", type: .success)
//                    self?.dismissVC(completion: nil)
                    self?.popVC()
                }
            })<bag
    }
    
}

//MARK::- FUNCTIONS
extension ForgetPasswordViewController {
    
    func onLoad(){
        
        labelPhone.onTap { [weak self] (gesture) in
            if self?.textFieldPhoneNum.tag == 1{
                self?.imageFlag.isHidden = false
                self?.textFieldPhoneNum.text = ""
                self?.labelPhone.textColor = #colorLiteral(red: 0.9999960065, green: 1, blue: 1, alpha: 1)
                self?.labelEmailID.textColor = #colorLiteral(red: 0.2901960784, green: 0.337254902, blue: 0.4235294118, alpha: 1)
                self?.textFieldPhoneNum.tag = 0
                self?.constraintHeightButtonCC.constant = 48
                self?.constraintWidthButtonCC.constant = 80
                self?.textFieldPhoneNum.placeholder = "    Enter phone number"
                self?.view.layoutIfNeeded()
            }
            
        }
        
        labelEmailID.onTap { [weak self] (gesture) in
            if self?.textFieldPhoneNum.tag == 0{
                self?.imageFlag.isHidden = true
                self?.textFieldPhoneNum.text = ""
                self?.labelEmailID.textColor = #colorLiteral(red: 0.9999960065, green: 1, blue: 1, alpha: 1)
                self?.labelPhone.textColor = #colorLiteral(red: 0.2901960784, green: 0.337254902, blue: 0.4235294118, alpha: 1)
                self?.textFieldPhoneNum.tag = 1
                self?.constraintHeightButtonCC.constant = 0
                self?.textFieldPhoneNum.placeholder = "    Enter Email ID"
                self?.constraintWidthButtonCC.constant = 0
                self?.view.layoutIfNeeded()
            }
            
        }
    }
    
    func moveToProfile(user: User?){
        guard let vc = R.storyboard.main.createProfileViewController() else { return }
        vc.createProVM = CreateProfileViewModal(user: user , countryDet: nil)
        self.pushVC(vc)
//        self.present(vc, animated: true, completion: nil)
    }
    
}
