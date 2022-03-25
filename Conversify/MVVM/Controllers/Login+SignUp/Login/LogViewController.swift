//
//  LogViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 10/10/18.
//

import UIKit
import IBAnimatable
import GoogleSignIn
import FlagPhoneNumber

class LogViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var constraintWidthButtonCC: NSLayoutConstraint!
    @IBOutlet weak var constraintHeightButtonCC: NSLayoutConstraint!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var labelPhone: UILabel!
    @IBOutlet weak var btnFaceBook: AnimatableButton!
    @IBOutlet weak var textFieldPhoneNum: UITextField!
    @IBOutlet weak var btnSelectCountryCode: UIButton!
    @IBOutlet weak var labelEmailID: UILabel!
    @IBOutlet weak var btnGoogle: AnimatableButton!
    @IBOutlet weak var imageFlag: UIImageView!
    @IBOutlet weak var btnNext: AnimatableButton!
    @IBOutlet weak var viewBorder: AnimatableView!
    @IBOutlet weak var labelEmailPhoneText: AnimatableLabel!
    @IBOutlet weak var phoneNumberTextField: FPNTextField!{
        didSet{
            phoneNumberTextField.textColor = UIColor.white
        }
    }
    
    //MARK::- PROPERTIES
    
    var registerVM = LogViewModal()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    //MARK::- BINDINGS
    
    override func bindings() {
        textFieldPhoneNum.isHidden = true
        textFieldPhoneNum.delegate = self
        phoneNumberTextField.delegate = self
        
        (textFieldPhoneNum.rx.text <-> registerVM.email)<bag
        (phoneNumberTextField.rx.text <-> registerVM.phoneNumber)<bag
        
        registerVM.isValid.subscribe { [unowned self] (valid) in
            if /self.registerVM.isEmail{
                self.btnNext.isEnabled = /valid.element
                self.btnNext.alpha = /valid.element ? 1.0 : 0.4
            }
            }<bag
        
        
        registerVM.countryDet.asObservable().subscribe(onNext: { [weak self] (countryDetails) in
            guard let safeValue = countryDetails else { return }
            self?.btnSelectCountryCode.setTitle(safeValue.dialCode, for: .normal)
            self?.imageFlag.image =  safeValue.countryImage
        })<bag
        
        btnSelectCountryCode.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.registerVM.getCountryCode()
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popVC()
            //            self?.dismissVC(completion: nil)
        })<bag
        
        btnGoogle.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.textFieldPhoneNum.text = ""
            self?.view.isUserInteractionEnabled = false
            self?.indicatorG?.startAnimating()
            GIDSignIn.sharedInstance().signIn()
        })<bag
        
        btnFaceBook.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.textFieldPhoneNum.text = ""
            self?.view.isUserInteractionEnabled = false
            self?.FBLogin()
        })<bag
        
        let nextBtnTapSignal = btnNext.rx.tap
        nextBtnTapSignal.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)
            self?.getUserDetails()
        })<bag
        
//        registerVM.isValid.subscribe { (valid) in
//            self.btnNext.isEnabled = /valid.element
//            self.btnNext.alpha = /valid.element ? 1.0 : 0.4
//            }<bag
        
        registerVM.successfullyRegistered.filter { (_) -> Bool in
            return true 
        }.subscribe(onNext: { [weak self] (bool) in
            if bool {
                guard let user = self?.registerVM.userInfo.value else { return }
                
                
                if user.facebookId != "" || user.googleId != ""{
                    if /user.isVerified{
                        self?.moveToHome()
                    }
                }else{
                if /user.isVerified{
                    if /user.isPasswordExist{
                        if /user.isProfileComplete{
                            self?.moveToHome()
                        }else{
                            self?.moveToCreateProfile()
                        }
                    }else{
                        self?.moveToCreatePassword()
                    }
                }else{
                    self?.moveToOptVerification()
                }}
            }
        })<bag
    }
    
    
}

//MARK::- FUNCTIONS
extension LogViewController {
    
    //DECIDE SCREEN TO PROCEED
    func getUserDetails(){
        registerVM.getDetails { [weak self] (status) in
            if status{
                guard let user = self?.registerVM.userInfo.value else { return }
                if /user.isVerified{
                    if /user.isPasswordExist{
                        if /user.isProfileComplete{
                            guard let vc = R.storyboard.main.logInViewController() else { return }
                            if /self?.textFieldPhoneNum.text?.isEmpty {
                                vc.logInVM = LogInViewModal(user: self?.registerVM.userInfo.value , selectedVal: /self?.registerVM.userInfo.value?.phoneNumber)
                            }else {
                            vc.logInVM = LogInViewModal(user: self?.registerVM.userInfo.value , selectedVal: /self?.textFieldPhoneNum.text?.replacingOccurrences(of: " ", with: ""))
                            }
                            self?.pushVC(vc)
                            //                            self?.present(vc, animated: true, completion: nil)
                        }else{
                            self?.moveToCreateProfile()
                        }
                    }else{
                        self?.moveToCreatePassword()
                    }
                }else{
                    self?.moveToOptVerification()
                }
            }
        }
    }
    
    func onLoad(){
        
        labelEmailPhoneText.attributedText = NSMutableAttributedString().specifyAttributes("Your registered " , font: UIFont.systemFont(ofSize: 14, weight: .medium)).specifyAttributes("Phone number ", font:  UIFont.systemFont(ofSize: 14, weight: .bold)).specifyAttributes(" / Email ID ", font:  UIFont.systemFont(ofSize: 14, weight: .medium))
        labelEmailPhoneText.isUserInteractionEnabled = true
        labelEmailPhoneText.addGestureRecognizer(UITapGestureRecognizer.init(target: self, action: #selector(termsTapped(_:))))
        
        //        self.textFieldPhoneNum.maxLength = 15
        setGoogleSignInDelegates()
//        registerVM.configureCountryPicker()
        setUpPhoneNumberField()
    }
    
    @objc func termsTapped(_ gesture: UITapGestureRecognizer) {
        
        let text = /labelEmailPhoneText.text
        let termsRange = (text as NSString).range(of: "Your registered Phone")
        if gesture.didTapAttributedTextInLabel(label: labelEmailPhoneText, inRange: termsRange) {
            print("Phone number")
            self.textFieldPhoneNum.isHidden = true
            self.phoneNumberTextField.isHidden = false
            if self.textFieldPhoneNum.tag == 1{
                self.textFieldPhoneNum.maxLength = 15
                self.imageFlag.isHidden = false
                self.registerVM.isEmail = false
                self.textFieldPhoneNum.text = ""
                self.textFieldPhoneNum.tag = 0
                self.constraintHeightButtonCC.constant = 48
                self.constraintWidthButtonCC.constant = 80
                self.labelPhone?.text = "Phone no:"
                self.textFieldPhoneNum.keyboardType = .numberPad
                self.btnNext.isEnabled = false
                self.btnNext.alpha =  0.4
                self.view.layoutIfNeeded()
            }
            labelEmailPhoneText.attributedText = NSMutableAttributedString().specifyAttributes("Your registered " , font: UIFont.systemFont(ofSize: 14, weight: .medium)).specifyAttributes("Phone number ", font:  UIFont.systemFont(ofSize: 14, weight: .bold)).specifyAttributes(" / Email ID ", font:  UIFont.systemFont(ofSize: 14, weight: .medium))
        }
        else{
            if self.textFieldPhoneNum.tag == 0{
                self.textFieldPhoneNum.isHidden = false
                self.phoneNumberTextField.isHidden = true
                self.textFieldPhoneNum.maxLength = 30
                self.registerVM.isEmail = true
                self.imageFlag.isHidden = true
                 self.labelPhone?.text = "Email ID:"
                self.textFieldPhoneNum.text = ""
                self.textFieldPhoneNum.tag = 1
                self.constraintHeightButtonCC.constant = 0
                self.textFieldPhoneNum.placeholder = " Enter Email ID"
                self.textFieldPhoneNum.keyboardType = .emailAddress
                self.constraintWidthButtonCC.constant = 0
                self.btnNext.isEnabled = false
                self.btnNext.alpha =  0.4
                self.view.layoutIfNeeded()
            }
            labelEmailPhoneText.attributedText = NSMutableAttributedString().specifyAttributes("Your registered " , font: UIFont.systemFont(ofSize: 14, weight: .medium)).specifyAttributes("Phone number ", font:  UIFont.systemFont(ofSize: 14, weight: .medium)).specifyAttributes(" / Email ID ", font:  UIFont.systemFont(ofSize: 14, weight: .bold))
            self.btnNext.isEnabled = /self.registerVM.isValidEmail(/self.registerVM.email.value)
            self.btnNext.alpha = /self.registerVM.isValidEmail(/self.registerVM.email.value) ? 1.0 : 0.4
        }
    }
    
    func FBLogin(){
        facebookLogin(isLogin: false, completion: { [weak self] (user) in
            self?.registerVM.login(FbId: /user?.facebookId, GId: "")
        })
    }
    
}

extension LogViewController : UIApplicationDelegate, GIDSignInDelegate{
    
    public func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error!){
        
        self.view.isUserInteractionEnabled = true
        if (error == nil) {
            self.user.email = user.profile.email
            let fullName = /user.profile.name
            let components = fullName.components(separatedBy: " ")
            if(components.count > 0){
                self.indicatorG?.stopAnimating()
                self.view.isUserInteractionEnabled =  true
                self.registerVM.login(FbId: "", GId: /user.userID)
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
    
    
}

//MARK::- VIEW CONTROLLER INSTANTIATE
extension LogViewController {
    
    func moveToCreateProfile(){
        guard let vc = R.storyboard.main.createProfileViewController() else { return }
        vc.createProVM = CreateProfileViewModal(user: user , countryDet: nil)
        self.pushVC(vc)
        //        self.present(vc, animated: true, completion: nil)
    }
    
    func moveToCreatePassword(){
        guard let createPasswordVC = R.storyboard.main.createPasswordViewController() else { return }
        createPasswordVC.createPassVM = CreatePasswordViewModal(countryCode: /self.registerVM.userInfo.value?.countryCode  , phone: /self.registerVM.userInfo.value?.phoneNumber , user: self.registerVM.userInfo.value)
        self.pushVC(createPasswordVC)
        //        self.present(createPasswordVC, animated: true, completion: nil)
    }
    
    func moveToOptVerification(){
        guard let verifyOtp = R.storyboard.main.verifyOTPViewController() else { return }
        verifyOtp.otpVM = VerifyOTPViewModal(user: self.registerVM.userInfo.value)
        self.pushVC(verifyOtp)
        //        self.present(verifyOtp, animated: true, completion: nil)
    }
    
}

extension LogViewController: UITextFieldDelegate , FPNTextFieldDelegate {
    func fpnDisplayCountryList() {
        
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
    
    func setUpPhoneNumberField(){
        phoneNumberTextField.borderStyle = .none
//        phoneNumberTextField.parentViewController = self
        phoneNumberTextField.delegate = self
        phoneNumberTextField.backgroundColor = UIColor.clear
        // Custom the size/edgeInsets of the flag button
//        phoneNumberTextField.flagSize = CGSize(width: 35, height: 35)
//        phoneNumberTextField.flagButtonEdgeInsets = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
        phoneNumberTextField.hasPhoneNumberExample = true
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
        self.btnNext.isEnabled = isValid
        self.btnNext.alpha = isValid ? 1.0 : 0.4
        
    }
    
    func fpnDidSelectCountry(name: String, dialCode: String, code: String) {
        print(name, dialCode, code)
        let countryD = CountryParam()
        countryD.countryCode = code
        countryD.countryName = name
        countryD.dialCode = "        " + dialCode
        self.registerVM.countryDet.value = countryD
    }
}

