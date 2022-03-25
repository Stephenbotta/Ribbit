//
//  RegisterViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 08/10/18.
//

import UIKit
import ADCountryPicker
import RxSwift
import Foundation
import RxCocoa

class RegisterViewModal: BaseRxViewModel {
    
    let picker = ADCountryPicker()
    var countryDet = Variable<CountryParam?>(nil)
    var phoneNumber = Variable<String?>(nil)
    var userInfo = Variable<User?>(nil)
    var email = Variable<String?>(nil)
    var successfullyRegistered = PublishSubject<Bool>()
    var successfullyFound = PublishSubject<Bool>()
    var isEmail = false
    var testBool = Variable<Bool?>(false)
    var helpBool = Variable<Bool?>(true)
    var isValid: Observable<Bool> {
        return Observable.combineLatest(email.asObservable(), helpBool.asObservable()) { (email,phone) in
            (self.isValidEmail(/email)) && /self.helpBool.value
        }
    }
    
    
    func getCountryCode(){
        let pickerNavigationController = UINavigationController(rootViewController: picker)
        UIApplication.topViewController()?.present(pickerNavigationController, animated: true, completion: nil)
    }
    
    
    func isValidEmail(_ testStr:String) -> Bool {
        let emailTest = NSPredicate(format:"SELF MATCHES %@", RegexExpresssions.EmailRegex)
        return emailTest.evaluate(with: testStr)
    }
    
    
    func register() {
        LoginTarget.registerEmailNPhone(phoneNumber: isEmail ? "" : /phoneNumber.value , email: isEmail ? /email.value : "", countryCode: /countryDet.value?.dialCode == "" ? "+1" : countryDet.value?.dialCode)
            .request(apiBarrier: true)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else { return }
                self.userInfo.value = safeResponse.data
                self.successfullyRegistered.onNext(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
            })<bag
        
    }
    
    func socialCheck(){
        let user = userInfo.value
        LoginTarget.signUp(userType: "", fullName: (/userInfo.value?.firstName + " " + /userInfo.value?.lastName), email:  /user?.email , phoneNumber: /user?.phoneNumber , password: /user?.password , countryCode: /user?.countryCode , userName: /user?.userName, facebookId: /user?.facebookId , googleId: /user?.googleId , flag: /user?.facebookId == "" ? "2" : "1", platform: "ios", referralCode: "" ).request(apiBarrier: true)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else { return }
                self?.userInfo.value = safeResponse.data
                print(response)
                self?.successfullyFound.onNext(true)
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
//extension RegisterViewModal : ADCountryPickerDelegate {
//    
//    func countryPicker(_ picker: ADCountryPicker, didSelectCountryWithName name: String, code: String, dialCode: String, image: UIImage?) {
//        let countryD = CountryParam()
//        countryD.countryCode = code
//        countryD.countryName = name
//        countryD.dialCode = "        " + dialCode
//        countryD.countryImage = image
//        countryDet.value = countryD
//        picker.dismiss(animated: true)
//    }
//    
//    
//    func configureCountryPicker(){
//        picker.delegate = self
//        picker.showCallingCodes = true
//        picker.showFlags = true
//        picker.pickerTitle = "Select country"
//        picker.defaultCountryCode = "US"
//        picker.alphabetScrollBarTintColor = UIColor.black
//        picker.alphabetScrollBarBackgroundColor = UIColor.clear
//        picker.closeButtonTintColor = UIColor.black
//        picker.font = UIFont.systemFont(ofSize: 12)
//        picker.flagHeight = 40
//        picker.hidesNavigationBarWhenPresentingSearch = true
//        picker.searchBarBackgroundColor = UIColor.lightGray
//    }
//    
//    
//    
//}


class CountryParam : NSObject {
    
    var countryCode = "US"
    var dialCode = "  +1   "
    var countryName = "US"
    var countryImage : UIImage? = R.image.ic_flag()
    
}
