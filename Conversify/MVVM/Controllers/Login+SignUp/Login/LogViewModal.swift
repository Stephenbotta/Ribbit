//
//  LogViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 14/10/18.
//

import UIKit
import ADCountryPicker
import RxSwift
import Foundation
import RxCocoa

class LogViewModal: BaseRxViewModel {

    let picker = ADCountryPicker()
    var countryDet = Variable<CountryParam?>(nil)
    var phoneNumber = Variable<String?>(nil)
    var userInfo = Variable<User?>(nil)
    var email = Variable<String?>(nil)
    var successfullyRegistered = PublishSubject<Bool>()
    var isEmail = false
    var isValid: Observable<Bool> {
        return Observable.combineLatest(email.asObservable(),phoneNumber.asObservable()) { (email,phone) in
            (self.isValidEmail(/email))
        }
    }
    
    func getCountryCode(){
        let pickerNavigationController = UINavigationController(rootViewController: picker)
        UIApplication.topViewController()?.present(pickerNavigationController, animated: true, completion: nil)
    }
    
    fileprivate func isValidPhone(_ testStr:String) -> Bool {
        if testStr.length < 0 { return false }
        if testStr.isBlank { return false }
        if testStr.isEveryCharcterZero { return false }
        if testStr.hasSpecialCharcters { return false }
        if testStr.characters.count >= 5 && testStr.characters.count <= 20 { return true
        }else{
            return false
        }
    }
    
     func isValidEmail(_ testStr:String) -> Bool {
        let emailTest = NSPredicate(format:"SELF MATCHES %@", RegexExpresssions.EmailRegex)
        return emailTest.evaluate(with: testStr)
    }
    
    
    func login(FbId: String , GId: String) {
        LoginTarget.login(userCredentials: "", phoneNumber: /phoneNumber.value?.contains("@") ? "" :  phoneNumber.value, countryCode: /countryDet.value?.dialCode == "" ? "+1" : countryDet.value?.dialCode, email: /phoneNumber.value?.contains("@") ? phoneNumber.value : "", password: "", facebookId: FbId, googleId: GId, platform: "ios")
            .request(apiBarrier: true)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else { return }
                self.userInfo.value = safeResponse.data
                Singleton.sharedInstance.loggedInUser = self.userInfo.value
                self.successfullyRegistered.onNext(true)
                
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
            })<bag
        
    }
    
    
}

//MARK::- COUNTRY PICKER
extension LogViewModal : ADCountryPickerDelegate {
    func countryPicker(_ picker: ADCountryPicker, didSelectCountryWithName name: String, code: String, dialCode: String) {
        
//    }
    
//    func countryPicker(_ picker: ADCountryPicker, didSelectCountryWithName name: String, code: String, dialCode: String, image: UIImage?) {
        let countryD = CountryParam()
        countryD.countryCode = code
        countryD.countryName = name
        countryD.dialCode = "        " + dialCode
        countryD.countryImage = UIImage()//image
        countryDet.value = countryD
        picker.dismiss(animated: true)
    }
    
    
    func configureCountryPicker(){
        picker.delegate = self
        picker.showCallingCodes = true
        picker.showFlags = true
        picker.pickerTitle = "Select country"
        picker.defaultCountryCode = "US"
        picker.alphabetScrollBarTintColor = UIColor.white
        picker.alphabetScrollBarBackgroundColor = UIColor.clear
        picker.closeButtonTintColor = UIColor.black
        picker.font = UIFont.systemFont(ofSize: 18, weight: .medium)
        picker.flagHeight = 40
        picker.hidesNavigationBarWhenPresentingSearch = true
        picker.searchBarBackgroundColor = UIColor.lightGray
    }
    
    func getDetails(_ completion:@escaping (Bool)->()){
        let numb = phoneNumber.value?.replacingOccurrences(of: " ", with: "")
        let phoneNo = (/countryDet.value?.dialCode == "" ? "+1" : /countryDet.value?.dialCode) + /numb
        LoginTarget.getUserDetails(userCredentials : isEmail ? /email.value : phoneNo.trimmed()).request(apiBarrier: true)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else { return }
                self.userInfo.value = safeResponse.data
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    
}

