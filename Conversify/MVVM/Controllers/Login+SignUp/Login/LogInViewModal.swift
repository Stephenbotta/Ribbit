//
//  LogInViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 14/10/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class LogInViewModal: BaseRxViewModel {
    
    
    var userInfo = Variable<User?>(nil)
    var password = Variable<String?>(nil)
    var successfullyRegistered = PublishSubject<Bool>()
    var helpBool =  Variable<Bool?>(true)
    var isValid: Observable<Bool> {
        return Observable.combineLatest(password.asObservable(),helpBool.asObservable()) { (password,helpBool) in
            self.isValidPassword(/password) && /helpBool
        }
    }
    var loginValue = Variable<String?>(nil)
    
    override init() {
        super.init()
    }
    
    init(user: User? , selectedVal: String?) {
        super.init()
        self.userInfo.value = user
        self.loginValue.value = selectedVal
    }
    
    func login() {
        LoginTarget.login(userCredentials: /self.loginValue.value?.trimmed() , phoneNumber: /userInfo.value?.phoneNumber?.contains("@") ? "" :  userInfo.value?.phoneNumber, countryCode: /userInfo.value?.countryCode == "" ? "+1" : /userInfo.value?.countryCode , email: /userInfo.value?.phoneNumber?.contains("@") ? userInfo.value?.phoneNumber : "", password: /password.value , facebookId: "", googleId: "", platform: "ios")
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
    
   
    
    func forgotPassword(_ completion:@escaping (Bool)->()){
        LoginTarget.forgotPassword(phoneNumber: /userInfo.value?.phoneNumber?.contains("@") ? "" :  userInfo.value?.phoneNumber , email: /userInfo.value?.email, countryCode: /userInfo.value?.countryCode == "" ? "+1" : /userInfo.value?.countryCode)
            .request(apiBarrier: true)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
               UtilityFunctions.makeToast(text: "Reset password link sent succesfully", type: .success)
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
    
    fileprivate func isValidPassword(_ testStr:String) -> Bool {
        if testStr.length <= 0 { return false }
        if testStr.isBlank  { return false  }
        
        //let isPasswordFormat = checkPassword(text: self)
        //if !isPasswordFormat { return .passwordFormat }
        if testStr.characters.count >= 6 && testStr.characters.count <= 20 { return true } else{
            return false
        }
        
    }
    
}
