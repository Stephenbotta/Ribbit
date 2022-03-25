//
//  LogInViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 10/10/18.
//

import UIKit
import RxCocoa
import RxSwift
import IBAnimatable

class LogInViewController: BaseRxViewController {
    
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var textFieldPassword: UITextField!
    @IBOutlet weak var labelName: UILabel!
    @IBOutlet weak var labelPhone: UILabel!
    @IBOutlet weak var imageProfile: UIImageView!
    @IBOutlet weak var btnNext: UIButton!
    @IBOutlet weak var btnResetPassword: UIButton!
    @IBOutlet weak var viewBorder: AnimatableView!
    
    //MARK::- PROPERTIES
    var logInVM = LogInViewModal()
    
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        LocationManager.sharedInstance.startTrackingUser()
        onLoad()
        // Do any additional setup after loading the view.
    }
    
    
    //MARK::- BINDINGS
    
    override func bindings() {
        textFieldPassword.delegate = self
        (textFieldPassword.rx.text <-> logInVM.password)<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popVC()
//            self?.dismissVC(completion: nil)
        })<bag
        
        labelPhone.text = /logInVM.userInfo.value?.fullphoneNumber
        
        let nextBtnTapSignal = btnNext.rx.tap
        nextBtnTapSignal.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)
            self?.logInVM.login()
        })<bag
        
        logInVM.successfullyRegistered.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.moveToHm()
                }
            })<bag
       
        btnResetPassword.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)
            self?.logInVM.forgotPassword({ (status) in
                
            })
        })<bag
        
    }

    
    func moveToHm(){
        if /logInVM.userInfo.value?.isInterestSelected{
            self.moveToHome()
        }else{
            guard let interestVc = R.storyboard.main.selectInterestsViewController() else { return }
            self.pushVC(interestVc)
//           self.present(interestVc, animated: true, completion: nil)
        }
       
    }
    
}

extension LogInViewController {
    
    func onLoad(){
        labelName.text = /logInVM.userInfo.value?.firstName?.uppercaseFirst
        imageProfile.image(url: /logInVM.userInfo.value?.img?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /logInVM.userInfo.value?.img?.original))
        
//        var emailStarreVal = /logInVM.userInfo.value?.email
//        var newVal = ""
//        for (index,chr) in emailStarreVal.characters.enumerated() {
//            if index == 0 || index == 1{
//                newVal.append(chr)
//            }else{
//                newVal.append("*")
//            }
//        }
//        labelPhone.text = newVal
        (textFieldPassword.rx.text <-> logInVM.password)<bag
        logInVM.isValid.subscribe { (valid) in
            self.btnNext.isEnabled = /valid.element
            self.btnNext.alpha = /valid.element ? 1.0 : 0.4
            }<bag
    }
}

extension LogInViewController: UITextFieldDelegate {
    
    func textFieldDidBeginEditing(_ textField: UITextField){
        if textField == textFieldPassword{
            viewBorder.layer.borderColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
        }else{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textFieldDidEndEditing(_ textField: UITextField){
        if textField == textFieldPassword{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
}
