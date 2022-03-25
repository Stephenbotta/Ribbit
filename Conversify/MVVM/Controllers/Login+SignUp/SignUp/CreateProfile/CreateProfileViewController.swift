
//
//  CreateProfileViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 08/10/18.
//

import UIKit
import RxCocoa
import RxSwift
import FlagPhoneNumber
import IBAnimatable

class CreateProfileViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var constraintHeightViewPhoneNum: NSLayoutConstraint!
    @IBOutlet weak var btnBack: AnimatableButton!
    @IBOutlet weak var labelStudent: UILabel!
    @IBOutlet weak var labelMentor: UILabel!
    @IBOutlet weak var btnMentor: UIButton!
    @IBOutlet weak var btnStudent: UIButton!
    @IBOutlet weak var btnNext: UIButton!
    @IBOutlet weak var textReferralcode: UITextField!
    @IBOutlet weak var textFieldPhoneNum: FPNTextField!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var viewFooter: UIView!
    @IBOutlet weak var labelAddYourPhone: UILabel!
    
    //MARK::- PROPERTIES
    var createProVM = CreateProfileViewModal()
    var validUserNameBackEnd = false
    
    //MARK::- VIEW CYCLE
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    //MARK::- BINDINGS
    
    override func bindings() {
        super.bindings()
        textFieldPhoneNum.delegate = self
        (textFieldPhoneNum.rx.text <-> createProVM.phoneNumber)<bag
        (textReferralcode.rx.text <-> createProVM.referralCode)<bag
        tableView.estimatedRowHeight = 84.0
        tableView.rowHeight = UITableView.automaticDimension
        
        createProVM.arrayProfileDetails
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.profileTableViewCell.identifier, cellType: ProfileTableViewCell.self)) { (row,element,cell) in
                cell.labelValue.tag = row
                cell.labelValue.addTarget(self, action: #selector(self.textFieldDidChange(textField:)), for: .editingChanged)
                cell.imgVerify.isHidden = !(row == 1)
                cell.header = element
                cell.labelHint.text = row == 1 ? "*Username must contains at least 6 characters containing only alphanumeric, period, hyphen and underscore." : ""
            }<bag
        tableView.reloadData()
        
        labelAddYourPhone.isHidden = createProVM.userInfo.value?.phoneNumber != nil
        viewFooter.isHidden = createProVM.userInfo.value?.phoneNumber != nil
        constraintHeightViewPhoneNum.constant =  createProVM.userInfo.value?.phoneNumber != nil ? 0 : 48
        textFieldPhoneNum.isEnabled = !(viewFooter.isHidden)
        
        btnNext.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.getValues()
            
        })<bag
        
        createProVM.successfullyCreated.filter { (_) -> Bool in
            return true
        }.subscribe(onNext: { [weak self] (bool) in
            if bool {
//                guard let interestVc = R.storyboard.main.selectInterestsViewController() else { return }
//                self?.pushVC(interestVc)
                guard let verifyOtp = R.storyboard.main.verifyOTPViewController() else { return }
                verifyOtp.otpVM = VerifyOTPViewModal(user: self?.createProVM.userInfo.value)
                self?.pushVC(verifyOtp)
                // self?.present(interestVc, animated: true, completion: nil)
                print("created")
            }
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            
            self?.popVC()
            //            self?.dismissVC(completion: nil)
        })<bag
        
        btnStudent.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.btnStudent.isSelected = /self?.btnStudent.isSelected.toggle()
            self?.btnMentor.isSelected = /self?.btnMentor.isSelected.toggle()
            self?.labelStudent.font = UIFont.systemFont(ofSize: 16, weight: .bold)
            self?.labelMentor.font = UIFont.systemFont(ofSize: 16, weight: .regular)
            self?.createProVM.userType = "STUDENT"
        })<bag
        
        btnMentor.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.createProVM.userType = "MENTOR"
            self?.btnStudent.isSelected = /self?.btnStudent.isSelected.toggle()
            self?.btnMentor.isSelected = /self?.btnMentor.isSelected.toggle()
            self?.labelMentor.font = UIFont.systemFont(ofSize: 16, weight: .bold)
            self?.labelStudent.font = UIFont.systemFont(ofSize: 16, weight: .regular)
        })<bag
    }
    
}

//MARK::- FUNCTIONS

extension CreateProfileViewController {
    
    //MARK::- TEXTFIELD DELEGATE
    @objc func textFieldDidChange(textField: UITextField){
        
        let searchTxt = /textField.text
        if textField.tag == 1 {
            if searchTxt.count >= 6 {
                checkUserName(str: searchTxt)
            }else{
                validUserNameBackEnd = false
                btnNext.isEnabled = false
                btnNext.alpha = 0.4
                guard let cell = self.tableView.cellForRow(at: IndexPath(row: 1, section: 0)) as? ProfileTableViewCell else { return }
                cell.imgVerify.image =  R.image.ic_unverify()
                
            }
        }
    }
    
    //MARK::- USERNAME VALIDATION
    func checkUserName(str : String?){
        if !(createProVM.isValidUserName(name: /str)){
            return
        }
        createProVM.checkIsUserNameAvailable(name: /str) { [weak self] (status, isNameAvailable) in
            if status {
                self?.validUserNameBackEnd = isNameAvailable
                guard let cell = self?.tableView.cellForRow(at: IndexPath(row: 1, section: 0)) as? ProfileTableViewCell else { return }
                let txt = /cell.labelValue.text
                cell.imgVerify.image = (isNameAvailable && /self?.createProVM.isValidUserName(name: /txt) && txt.count >= 6) ? R.image.ic_verify() : R.image.ic_unverify()
                
                if (!(/self?.createProVM.phoneNumber.value?.isEmpty) || !(/self?.createProVM.email.value?.isEmpty) ) && isNameAvailable && /self?.createProVM.isValidValues(){
                    self?.btnNext.isEnabled = isNameAvailable
                    self?.btnNext.alpha = isNameAvailable ? 1.0 : 0.4
                }
            }
        }
    }
    
    func getValues(){
        tableView.scrollToBottom()
        guard let nameTextField = (self.tableView.cellForRow(at: IndexPath(row: 0, section: 0)) as? ProfileTableViewCell)?.labelValue else { return }
        createProVM.fullName.value = nameTextField.text
        
        guard let userNameTextField = (self.tableView.cellForRow(at: IndexPath(row: 1, section: 0)) as? ProfileTableViewCell)?.labelValue else { return }
        createProVM.userName.value = userNameTextField.text
        
        if createProVM.arrayProfileDetails.value.count != 2{
            guard let emailTextField = (self.tableView.cellForRow(at: IndexPath(row: 2, section: 0)) as? ProfileTableViewCell)?.labelValue else { return }
            createProVM.email.value = emailTextField.text
            
        }
        self.createProVM.createProfile()
    }
    
    func handleUI(){
        viewFooter.isHidden = !(/createProVM.phoneNumber.value?.isEmpty)
        labelAddYourPhone.isHidden = !(/createProVM.phoneNumber.value?.isEmpty)
        
    }
    
    func onLoad(){
        handleUI()
        setUpPhoneNumberField()
        createProVM.getProfileArray()
        
        guard let nameTextField = (self.tableView.cellForRow(at: IndexPath(row: 0, section: 0)) as? ProfileTableViewCell)?.labelValue else { return }
        (nameTextField.rx.text <-> createProVM.fullName)<bag
        
        guard let userNameTextField = (self.tableView.cellForRow(at: IndexPath(row: 1, section: 0)) as? ProfileTableViewCell)?.labelValue else { return }
        (userNameTextField.rx.text <-> createProVM.userName)<bag
        
        if createProVM.arrayProfileDetails.value.count != 2{
            guard let emailTextField = (self.tableView.cellForRow(at: IndexPath(row: 2, section: 0)) as? ProfileTableViewCell)?.labelValue else { return }
            (emailTextField.rx.text <-> createProVM.email)<bag
        }
        
        createProVM.isValid.subscribe { [weak self] (valid) in
            if /self?.validUserNameBackEnd{
                self?.btnNext.isEnabled = /valid.element
                self?.btnNext.alpha = /valid.element ? 1.0 : 0.4
            }else{
                self?.btnNext.isEnabled = false
                self?.btnNext.alpha = 0.4
            }
            }<bag
    }
    
}


//MARK::- COUNTRY PICKER
extension CreateProfileViewController : FPNTextFieldDelegate {
    func fpnDisplayCountryList() {
        
    }
    
    
    func fpnDidValidatePhoneNumber(textField: FPNTextField, isValid: Bool) {
        if textField.text == ""{
            return
        }
        textField.rightViewMode = .always
        textField.rightView = UIImageView(image: isValid ? R.image.ic_verify() : R.image.ic_unverify())
        btnNext.isEnabled = isValid && /self.createProVM.isValidValues()
        btnNext.alpha = (isValid && /self.createProVM.isValidValues()) ? 1.0 : 0.4
    }
    
    func fpnDidSelectCountry(name: String, dialCode: String, code: String) {
        print(name, dialCode, code)
        let countryD = CountryParam()
        countryD.countryCode = code
        countryD.countryName = name
        countryD.dialCode = dialCode
        self.createProVM.countryDet.value = countryD
    }
    
    func setUpPhoneNumberField(){
        
        textFieldPhoneNum.borderStyle = .none
//        textFieldPhoneNum.parentViewController = self
        textFieldPhoneNum.delegate = self
        textFieldPhoneNum.backgroundColor = UIColor.clear
        // Custom the size/edgeInsets of the flag button
//        textFieldPhoneNum.flagSize = CGSize(width: 35, height: 35)
//        textFieldPhoneNum.flagButtonEdgeInsets = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
        textFieldPhoneNum.hasPhoneNumberExample = true
        let attributes: [NSAttributedString.Key: UIColor] = [.foregroundColor: #colorLiteral(red: 0.7921568627, green: 0.7921568627, blue: 0.7921568627, alpha: 0.62)]
        textFieldPhoneNum.attributedPlaceholder = NSAttributedString(string: /textFieldPhoneNum.attributedPlaceholder?.string, attributes: attributes)
        let defaultCountryCode = getCurrentCountryCode()
        let countryD = CountryParam()
        countryD.countryCode = /defaultCountryCode["code"]
        countryD.countryName = /defaultCountryCode["name"]
        countryD.dialCode = /defaultCountryCode["dial_code"]
        createProVM.countryDet.value = countryD
    }
    
}

extension CreateProfileViewController : UITextFieldDelegate {
    
    func textFieldDidBeginEditing(_ textField: UITextField){
        if textField == textFieldPhoneNum{
            viewFooter.layer.borderColor = #colorLiteral(red: 0.9999960065, green: 1, blue: 1, alpha: 1)
        }else{
            viewFooter.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textFieldDidEndEditing(_ textField: UITextField){
        if textField == textFieldPhoneNum{
            viewFooter.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            viewFooter.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
}
