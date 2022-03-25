//
//  VerifyOTPViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 08/10/18.
//

import UIKit
import SVPinView
import IBAnimatable

class VerifyOTPViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var labelVerificationCode: UITextField!{
        didSet{
            labelVerificationCode.delegate = self
        }
    }
    @IBOutlet weak var labelHeader: UILabel!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var labelResendCode: UILabel!
    @IBOutlet weak var labelCodeSendTo: UILabel!
    //    @IBOutlet weak var otpView: SVPinView!{
    //        didSet {
    //            otpView.pinLength = 4
    //            otpView.interSpace = 16
    //            otpView.textColor = UIColor.white
    //            otpView.borderLineColor = UIColor.white.withAlphaComponent(0.4)
    //            otpView.borderLineThickness = 1
    //            otpView.shouldSecureText = false
    //            otpView.style = .underline
    //            otpView.font = UIFont.systemFont(ofSize: 24)
    //            otpView.keyboardType = .phonePad
    //            otpView.pinIinputAccessoryView = UIView()
    //
    //        }
    //    }
    @IBOutlet weak var btnNext: AnimatableButton!
    
    //MARK::- PROPERTIES
    
    var otpVM = VerifyOTPViewModal()
    var isSignUpPhon : Bool?
    
    //MARK::- VIEW CYCLE
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
        // Do any additional setup after loading the view.
    }
    
    
    //MARK::- BINDINGS
    
    override func bindings() {
        (labelVerificationCode.rx.text <-> otpVM.otpValue)<bag
        labelVerificationCode.text = ""
        otpVM.otpNotVerified.filter { (bool) -> Bool in
            return true
        }.subscribe(onNext: { [weak self] (bool) in
            self?.btnNext.isEnabled = false
            self?.btnNext.alpha = 0
            self?.labelVerificationCode.text = ""
        })<bag
        
        otpVM.otpVerified.filter { (bool) -> Bool in
            return true
        }.subscribe(onNext: { [weak self] (bool) in
            if bool {
                if /self?.otpVM.isVerificationProcedure.value{
                    self?.otpVM.verified?()
                    self?.popVC()
                    //                        self?.dismissVC(completion: nil)
                }else{
                    if(self?.isSignUpPhon == true){
                        guard let createPasswordVC = R.storyboard.main.createPasswordViewController() else { return }
                        createPasswordVC.createPassVM = CreatePasswordViewModal(countryCode: /self?.otpVM.userInfo.value?.countryCode  , phone: /self?.otpVM.userInfo.value?.phoneNumber , user: self?.otpVM.userInfo.value)
                        self?.pushVC(createPasswordVC)
                    }
                  else if(Singleton.sharedInstance.loggedInUser?.googleId != "" || Singleton.sharedInstance.loggedInUser?.facebookId != ""){
                                        guard let interestVc = R.storyboard.main.selectInterestsViewController() else { return }
                                        self?.pushVC(interestVc)

                    }
                }
                
            }
        })<bag
        
        //Submit OTP
        let submitTapSignal = btnNext.rx.tap
        submitTapSignal.asDriver().drive(onNext: { [weak self] () in
            if /self?.otpVM.isVerificationProcedure.value {
                self?.otpVM.verifyPhone (otp: /self?.labelVerificationCode.text)
            }else{
                self?.otpVM.submitOtp(otp: /self?.labelVerificationCode.text)
            }
            
        })<bag
        
        //Resent OTP
        
        labelResendCode.onTap { [weak self] (gesture) in
            self?.otpVM.resendOtp()
        }
        
        
        otpVM.userInfo.asObservable().subscribe(onNext: { [weak self] (user) in
            guard let safeValue = user else { return }
            //            self?.txtOtp.text = /safeValue.otp
            self?.otpVM.otpValue.value = /safeValue.otp
            
        })<bag
        
        //        btnNext.rx.tap.asDriver().drive(onNext: {  [weak self] () in
        //
        //        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            
            self?.popVC()
            //            self?.dismissVC(completion: nil)
        })<bag
        
        
        otpVM.isValid.asObservable().subscribe { [weak self] (valid) in
            self?.btnNext.isEnabled = /valid.element
            self?.btnNext.alpha = /valid.element ? 1.0 : 0.4
            }<bag
    }
    
}

//MARK::- FUNCTIONS
extension VerifyOTPViewController : UITextFieldDelegate {
    
    func onLoad(){
        if /self.otpVM.isVerificationProcedure.value {
            self.otpVM.userInfo.value = Singleton.sharedInstance.loggedInUser
            self.otpVM.resendOtp()
        }
        self.btnNext.isEnabled = false
        self.btnNext.alpha = 0.4
        labelHeader.text = "We sent you a code to verify your " + (/otpVM.userInfo.value?.phoneNumber == "" ? "email ": "number")
        labelCodeSendTo.text = "Code sent to " + (/otpVM.userInfo.value?.phoneNumber == "" ? /otpVM.userInfo.value?.email : (/otpVM.userInfo.value?.countryCode + " " + /otpVM.userInfo.value?.phoneNumber) )
        
        //        otpView.delegate = self
        //        otpView.becomeFirstResponderAtIndex = 0
        //        otpView.didFinishCallback = { [ weak self]  pin in
        //            self?.btnNext.isEnabled = true
        //            self?.btnNext.alpha = 1
        //        }
        
    }
    
}

//extension VerifyOTPViewController : DelegateSVPinElementEntered{
//
//    func delegateSVPinElementEntered(){
//        let pinVal = self.otpView.getPin()
//        print(pinVal)
//        btnNext.isEnabled = pinVal.count == 4
//        btnNext.alpha = pinVal.count == 4 ? 1.0 : 0.4
//    }
//}

