//
//  RegisterViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 08/10/18.
//

import UIKit
import IBAnimatable
import GoogleSignIn
import FlagPhoneNumber

class RegisterViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var constraintWidthButtonCC: NSLayoutConstraint!
    @IBOutlet weak var constraintHeightButtonCC: NSLayoutConstraint!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var labelPhone: UILabel?
    @IBOutlet weak var btnFaceBook: AnimatableButton!
    @IBOutlet weak var textFieldPhoneNum: UITextField!
    @IBOutlet weak var btnSelectCountryCode: UIButton!
    @IBOutlet weak var labelEmailID: UILabel?
    @IBOutlet weak var btnGoogle: AnimatableButton!
    @IBOutlet weak var btnNext: AnimatableButton!
    @IBOutlet weak var phoneNumberTextField: FPNTextField!{
        didSet{
            phoneNumberTextField.textColor = UIColor.white
        }
    }
    @IBOutlet weak var viewBorder: AnimatableView!
    @IBOutlet weak var labelEmailPhoneText: AnimatableLabel!
    
    //MARK::- PROPERTIES
    var registerVM = RegisterViewModal()
    
    //MARK::- VIEW CYCLE
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        
        textFieldPhoneNum.delegate = self
        phoneNumberTextField.delegate = self
        
        (textFieldPhoneNum.rx.text <-> registerVM.email)<bag
        (phoneNumberTextField.rx.text <-> registerVM.phoneNumber)<bag
        
        registerVM.isValid.subscribe { [unowned self] (valid) in
            if /self.registerVM.isEmail{
                self.btnNext.isEnabled = /valid.element
                self.btnNext.alpha = /valid.element ? 1.0 : 0.4
            }}<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            
            self?.popVC()
            //            self?.dismissVC(completion: nil)
        })<bag
        
        
        registerVM.countryDet.asObservable().subscribe(onNext: { [weak self] (countryDetails) in
            guard let safeValue = countryDetails else { return }
            self?.btnSelectCountryCode.setTitle(safeValue.dialCode, for: .normal)
        })<bag
        
        btnSelectCountryCode.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.registerVM.getCountryCode()
        })<bag
        
        btnGoogle.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            //            self?.textFieldPhoneNum.text = ""
            
            self?.view.isUserInteractionEnabled = false
            self?.indicatorG?.startAnimating()
            GIDSignIn.sharedInstance().signIn()
        })<bag
        
        btnFaceBook.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            //            self?.textFieldPhoneNum.text = ""
            self?.view.isUserInteractionEnabled = false
            
            self?.FBLogin()
        })<bag
        
        //Next button Action
        let nextBtnTapSignal = btnNext.rx.tap
        nextBtnTapSignal.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)
            self?.registerVM.register()
        })<bag
        
        registerVM.successfullyRegistered.filter { (_) -> Bool in
            return true
        }.subscribe(onNext: { [weak self] (bool) in
            if bool {
                guard let verifyOtp = R.storyboard.main.verifyOTPViewController() else { return }
                verifyOtp.otpVM = VerifyOTPViewModal(user: self?.registerVM.userInfo.value)
                verifyOtp.isSignUpPhon = true
                self?.pushVC(verifyOtp)
                //                    self?.present(verifyOtp, animated: true, completion: nil)
            }
        })<bag
        
        
        
        
        registerVM.successfullyFound.filter { (_) -> Bool in
            return true
        }.subscribe(onNext: { [weak self] (bool) in
            if bool {
                
                guard let user = self?.registerVM.userInfo.value else { return }
                
                if user.facebookId != "" || user.googleId != ""{
                    if /user.isVerified{
                        self?.moveToHm()
                    }
                }else{
                    if /user.isVerified{
                        if /user.isPasswordExist{
                            if /user.isProfileComplete{
                                self?.moveToHm()
                            }else{
                                self?.moveToProfile(user: user)
                            }
                        }else{
                            self?.moveToCreatePassword()
                        }
                    }else{
                        self?.moveToOptVerification()
                    }
                }
            }
        })<bag
        
    }
    
}

//MARK::- FUNCTIONS
extension RegisterViewController {
    
    func moveToHm(){
        if /registerVM.userInfo.value?.isInterestSelected{
            Singleton.sharedInstance.loggedInUser = registerVM.userInfo.value
            self.moveToHome()
        }else{
            guard let interestVc = R.storyboard.main.selectInterestsViewController() else { return }
            self.pushVC(interestVc)
            //            self.present(interestVc, animated: true, completion: nil)
        }
        
    }
    
    func moveToCreatePassword(){
        guard let createPasswordVC = R.storyboard.main.createPasswordViewController() else { return }
        createPasswordVC.createPassVM = CreatePasswordViewModal(countryCode: /self.registerVM.userInfo.value?.countryCode  , phone: /self.registerVM.userInfo.value?.phoneNumber , user: self.registerVM.userInfo.value)
        self.pushVC(createPasswordVC)
        //        self.present(createPasswordVC, animated: true, completion: nil)
    }
    
    func moveToOptVerification(){
        guard let verifyOtp = R.storyboard.main.verifyOTPViewController() else { return }
        verifyOtp.isSignUpPhon = true
        verifyOtp.otpVM = VerifyOTPViewModal(user: self.registerVM.userInfo.value)
        self.pushVC(verifyOtp)
        //        self.present(verifyOtp, animated: true, completion: nil)
    }
    
    func onLoad(){
        
        labelEmailPhoneText.attributedText = NSMutableAttributedString().specifyAttributes("We need your " , font: UIFont.systemFont(ofSize: 14, weight: .medium)).specifyAttributes("Phone number ", font:  UIFont.systemFont(ofSize: 14, weight: .bold)).specifyAttributes("/ Email ID \n to identify you", font:  UIFont.systemFont(ofSize: 14, weight: .medium))
        labelEmailPhoneText.isUserInteractionEnabled = true
        labelEmailPhoneText.addGestureRecognizer(UITapGestureRecognizer.init(target: self, action: #selector(termsTapped(_:))))
        
        setGoogleSignInDelegates()
        setUpPhoneNumberField()
        
    }
    
    @objc func termsTapped(_ gesture: UITapGestureRecognizer) {
        
        let text = /labelEmailPhoneText.text
        let termsRange = (text as NSString).range(of: "We need your Phone")
        if gesture.didTapAttributedTextInLabel(label: labelEmailPhoneText, inRange: termsRange) {
            print("Phone number")
            self.textFieldPhoneNum.isHidden = true
            self.phoneNumberTextField.isHidden = false
            self.labelEmailPhoneText.attributedText = NSMutableAttributedString().specifyAttributes("We need your " , font: UIFont.systemFont(ofSize: 14, weight: .medium)).specifyAttributes("Phone number ", font:  UIFont.systemFont(ofSize: 14, weight: .bold)).specifyAttributes("/ Email ID \n to identify you", font:  UIFont.systemFont(ofSize: 14, weight: .medium))
            if self.textFieldPhoneNum.tag == 1{
                self.textFieldPhoneNum.maxLength = 15
                self.registerVM.isEmail = false
                self.labelPhone?.text = "Phone no:"
                self.textFieldPhoneNum.tag = 0
                self.constraintHeightButtonCC.constant = 48
                self.constraintWidthButtonCC.constant = 80
                self.textFieldPhoneNum.placeholder = "Enter phone number"
                self.textFieldPhoneNum.keyboardType = .numberPad
                
                self.view.layoutIfNeeded()
            }
            self.btnNext.isEnabled = /self.registerVM.testBool.value
            self.btnNext.alpha = /self.registerVM.testBool.value ? 1.0 : 0.4
            
        } else {
            if self.textFieldPhoneNum.tag == 0{
                self.textFieldPhoneNum.isHidden = false
                self.phoneNumberTextField.isHidden = true
                self.textFieldPhoneNum.maxLength = 30
                self.registerVM.isEmail = true
                self.labelPhone?.text = "Email ID:"
                self.textFieldPhoneNum.tag = 1
                self.constraintHeightButtonCC.constant = 0
                self.textFieldPhoneNum.placeholder = "Enter Email ID"
                self.textFieldPhoneNum.keyboardType = .emailAddress
                self.constraintWidthButtonCC.constant = 0
                self.view.layoutIfNeeded()
            }
            self.labelEmailPhoneText.attributedText = NSMutableAttributedString().specifyAttributes("We need your Phone number " , font: UIFont.systemFont(ofSize: 14, weight: .medium)).specifyAttributes("/ Email ID ", font:  UIFont.systemFont(ofSize: 14, weight: .bold)).specifyAttributes(" \n to identify you", font:  UIFont.systemFont(ofSize: 14, weight: .medium))
            self.btnNext.isEnabled = /self.registerVM.isValidEmail(/self.registerVM.email.value)
            self.btnNext.alpha = /self.registerVM.isValidEmail(/self.registerVM.email.value) ? 1.0 : 0.4
            print("Email")
        }
    }
    
    func FBLogin(){
        facebookLogin(isLogin: false, completion: { [weak self] (user) in
            self?.validateSocial(isFb: true , user: user)
        })
    }
    
    func validateSocial(isFb: Bool , user: User?){
        registerVM.userInfo.value = user
        registerVM.socialCheck()
     //   self.moveToProfile(user: user)
    }
    
    func moveToProfile(user: User?){
        guard let vc = R.storyboard.main.createProfileViewController() else { return }
        vc.createProVM = CreateProfileViewModal(user: user , countryDet: nil)
        self.pushVC(vc)
        //        self.present(vc, animated: true, completion: nil)
    }
    
}


extension RegisterViewController : UIApplicationDelegate, GIDSignInDelegate{
    
    public func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error!){
        
        self.view.isUserInteractionEnabled = true
        if (error == nil) {
            self.user.email = user.profile.email
            let fullName = /user.profile.name
            var components = fullName.components(separatedBy: " ")
            if(components.count > 0){
                let firstName = components.removeFirst()
                let lastName = components.joined(separator: " ")
                self.user.firstName = /firstName
                self.user.lastName = /lastName
                self.user.googleId = /user.userID
                self.user.facebookId = ""
                self.indicatorG?.stopAnimating()
                self.view.isUserInteractionEnabled =  true
                validateSocial(isFb: false , user: self.user)
            }
        } else {
            self.indicatorG?.stopAnimating()
            print("\(error.localizedDescription)")
        }
    }
    
    func setGoogleSignInDelegates(){
        GIDSignIn.sharedInstance()?.presentingViewController = self
        GIDSignIn.sharedInstance().delegate = self
    }
    
    
    private func getCustomTextFieldInputAccessoryView(with items: [UIBarButtonItem]) -> UIToolbar {
        let toolbar: UIToolbar = UIToolbar()
        toolbar.barStyle = UIBarStyle.default
        toolbar.items = items
        toolbar.sizeToFit()
        
        return toolbar
    }
}

extension RegisterViewController : FPNTextFieldDelegate , UITextFieldDelegate {
    func fpnDisplayCountryList() {
        
    }
    
    
    func setUpPhoneNumberField(){
        phoneNumberTextField.borderStyle = .none
//        phoneNumberTextField.parentViewController = self
        phoneNumberTextField.delegate = self
        phoneNumberTextField.backgroundColor = UIColor.clear
        // Custom the size/edgeInsets of the flag button
//        phoneNumberTextField.flagSize = CGSize(width: 35, height: 35)
//        phoneNumberTextField.flagButtonEdgeInsets = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
        phoneNumberTextField.hasPhoneNumberExample = true
        phoneNumberTextField.tintColor = .white
        let attributes: [NSAttributedString.Key: UIColor] = [.foregroundColor: #colorLiteral(red: 0.7921568627, green: 0.7921568627, blue: 0.7921568627, alpha: 0.62)]
        phoneNumberTextField.attributedPlaceholder = NSAttributedString(string: /phoneNumberTextField.attributedPlaceholder?.string, attributes: attributes)
        let defaultCountryCode = getCurrentCountryCode()
        let countryD = CountryParam()
        countryD.countryCode = /defaultCountryCode["code"]
        countryD.countryName = /defaultCountryCode["name"]
        countryD.dialCode = /defaultCountryCode["dial_code"]
        registerVM.countryDet.value = countryD
        
    }
    
    func fpnDidValidatePhoneNumber(textField: FPNTextField, isValid: Bool) {
        if textField.text == ""{
            return
        }
        textField.rightViewMode = .always
        textField.rightView = UIImageView(image: isValid ? R.image.ic_verify() : R.image.ic_unverify())
        registerVM.testBool.value = isValid
        self.btnNext.isEnabled = isValid
        self.btnNext.alpha = isValid ? 1.0 : 0.4
        
    }
    
    func fpnDidSelectCountry(name: String, dialCode: String, code: String) {
        print(name, dialCode, code)
        let attributes: [NSAttributedString.Key: UIColor] = [.foregroundColor: #colorLiteral(red: 0.7921568627, green: 0.7921568627, blue: 0.7921568627, alpha: 0.62)]
        phoneNumberTextField.attributedPlaceholder = NSAttributedString(string: /phoneNumberTextField.attributedPlaceholder?.string, attributes: attributes)
        let countryD = CountryParam()
        countryD.countryCode = code
        countryD.countryName = name
        countryD.dialCode = "        " + dialCode
        self.registerVM.countryDet.value = countryD
    }
    
    func textFieldDidBeginEditing(_ textField: UITextField){
        if textField == textFieldPhoneNum{
            viewBorder.layer.borderColor = #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1)
        }else if  textField == phoneNumberTextField{
            viewBorder.layer.borderColor = #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1)
        }else{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textFieldDidEndEditing(_ textField: UITextField){
        if textField == textFieldPhoneNum{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else if  textField == phoneNumberTextField{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            viewBorder.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
}

