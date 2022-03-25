//
//  CreateProfileViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 10/10/18.
//

import UIKit
import ADCountryPicker
import RxSwift
import Foundation
import RxCocoa

class CreateProfileViewModal: BaseRxViewModel {
    
    var arrayProfileDetails = Variable<[ProfileElements]>([])
    var userInfo = Variable<User?>(nil)
    let picker = ADCountryPicker()
    var countryDet = Variable<CountryParam?>(nil)
    var phoneNumber = Variable<String?>(nil)
    var referralCode = Variable<String?>(nil)
    var fullName = Variable<String?>(nil)
    var email = Variable<String?>(nil)
    var userName = Variable<String?>(nil)
    var fbID = Variable<String?>(nil)
    var googleID = Variable<String?>(nil)
    var successfullyCreated = PublishSubject<Bool>()
    var flag = Variable<String?>(nil)
    var isEmail = false
    var isValid: Observable<Bool> {
        return Observable.combineLatest(fullName.asObservable(), userName.asObservable(), email.asObservable(),phoneNumber.asObservable()) { (name,userNm,email,phone) in
            self.isValidName(name: /name) && self.isValidEmail(/email) && self.isValidPhone(/phone?.replacingOccurrences(of: " ", with: "")) && self.isValidUserName(name: /userNm)
        }
    }
    var userType = "STUDENT"
    
    override init() {
        super.init()
    }
    
    init(user: User? , countryDet: CountryParam?) {
        super.init()
        self.userInfo.value = user
        self.phoneNumber.value = /user?.phoneNumber
        self.email.value = /user?.email
        self.fbID.value = /user?.facebookId
        self.googleID.value = /user?.googleId
        
        if countryDet != nil{
            self.countryDet.value = countryDet
        }
        
    }
    
    
    func getProfileArray(){
        
        arrayProfileDetails.value = [ ProfileElements(head: "Your full name", val: /userInfo.value?.firstName?.uppercaseFirst + " " + /userInfo.value?.lastName, holder: "Enter Full name", keyType: UIKeyboardType.namePhonePad )   , ProfileElements(head: "Username", val: "", holder: "Enter Username", keyType: UIKeyboardType.default)  ]
        if /userInfo.value?.email == "" {
            arrayProfileDetails.value.append(ProfileElements(head: "And your email ID?", val: "", holder: "Enter Email ID", keyType: UIKeyboardType.emailAddress))
            flag.value = "3"
        }else{
            flag.value = "4"
        }
        
        if /self.fbID.value != ""{
            flag.value = "1"
        }
        
        if /self.googleID.value != ""{
            flag.value = "2"
        }
        
    }
    
    
    func isValidValues() -> Bool{
        return self.isValidName(name: /fullName.value) && self.isValidUserName(name: /userName.value)
    }
    
    
}

//MARK::- API HANDLER
extension CreateProfileViewModal {
    
    fileprivate func isValidPhone(_ testStr:String) -> Bool {
        var newName = testStr.replacingOccurrences(of: "-", with: "")
        var newName1 = newName.replacingOccurrences(of: "(", with: "")
        var newName2 = newName1.replacingOccurrences(of: ")", with: "")
        if newName2.length < 0 { return false }
        if newName2.isBlank { return false }
        if newName2.isEveryCharcterZero { return false }
        if newName2.hasSpecialCharcters { return false }
        return true
    }
    
    fileprivate func isValidEmail(_ testStr:String) -> Bool {
        let emailTest = NSPredicate(format:"SELF MATCHES %@", RegexExpresssions.EmailRegex)
        return emailTest.evaluate(with: testStr)
    }
    
    func isValidName(name: String) -> Bool {
        if name.length < 0 { return false }
        if name.isBlank { return false }
        var newName = name.replacingOccurrences(of: "-", with: "")
        newName = newName.replacingOccurrences(of: ".", with: "")
        newName = newName.replacingOccurrences(of: "'", with: "")
        newName = newName.replacingOccurrences(of: " ", with: "")
        if newName.hasSpecialCharcters { return false }
        return true
    }
    
    func isValidUserName(name: String) -> Bool {
        let emailTest = NSPredicate(format:"SELF MATCHES %@", RegexExpresssions.UserName)
        return emailTest.evaluate(with: name)
        
    }
    
    func checkUsername(text : String?) -> Bool{
        let characterSet:  NSMutableCharacterSet = NSMutableCharacterSet.alphanumeric()
        characterSet.addCharacters(in: "_-.")
        let characterSetInverted:  NSMutableCharacterSet = characterSet.inverted as! NSMutableCharacterSet
        if text?.rangeOfCharacter(from: characterSetInverted as CharacterSet) != nil {
            return false
        }else {
            return true
        }
    }
    
    func validateProfile() -> Bool{
        let phnNumber = /phoneNumber.value == "" ? /self.userInfo.value?.phoneNumber : /phoneNumber.value
        let newNumber = phnNumber.replacingOccurrences(of: "-", with: "")
        let status = Validation.shared.isValidProfile(name: /fullName.value, userName: /userName.value, email: /email.value, phoneNum: newNumber)
        
        switch status {
        case .success:
            return true
        case .failure:
            return false
        }
        
    }
    
    
   
    func createProfile(){
        
        if !validateProfile(){
            return
        }
        let ph = /phoneNumber.value == "" ? /self.userInfo.value?.phoneNumber : /phoneNumber.value
        let referral = referralCode.value
        LoginTarget.signUp(userType:/userType , fullName: /fullName.value, email:  /email.value , phoneNumber: /phoneNumber.value == "" ? /self.userInfo.value?.phoneNumber : /phoneNumber.value , password: /self.userInfo.value?.password , countryCode: /countryDet.value?.dialCode == "" ? (ph == "" ? "" : "+1") : /countryDet.value?.dialCode, userName: /userName.value, facebookId: /fbID.value , googleId: /googleID.value , flag: /flag.value, platform: "ios", referralCode: referral ).request(apiBarrier: true)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else { return }
                self.userInfo.value = safeResponse.data
                print(response)
                Singleton.sharedInstance.loggedInUser = self.userInfo.value
                self.successfullyCreated.onNext(true)
                
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
            })<bag
        
    }
    
    func checkIsUserNameAvailable(name : String? , _ completion:@escaping (Bool , _  isNameAvailable : Bool)->()) {
        LoginTarget.userNameCheck(userName: /name).request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<CheckUserName> else { return }
                completion(true , /safeResponse.data?.isNameAvailable)
                
            }, onError: { (error) in
                completion(false , false)
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
            })<bag
    }
    
    
    
}



class ProfileElements{
    
    var header: String?
    var value: String?
    var placHolder: String?
    var keyBoardType: UIKeyboardType?
    
    init(head : String?,val: String? , holder: String? , keyType: UIKeyboardType?) {
        header = head
        value = val
        placHolder = holder
        keyBoardType = keyType
    }
    
}
