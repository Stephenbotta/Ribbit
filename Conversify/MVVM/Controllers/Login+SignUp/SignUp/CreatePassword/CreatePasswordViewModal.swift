//
//  CreatePasswordViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 13/10/18.
//

import UIKit
import ADCountryPicker
import RxSwift
import Foundation
import RxCocoa

class CreatePasswordViewModal: BaseRxViewModel {
    
    var password = Variable<String?>(nil)
    var phoneNumber = Variable<String?>(nil)
    var countryCode = Variable<String?>(nil)
    var userInfo = Variable<User?>(nil)
    var helpBool =  Variable<Bool?>(true)
    var isValid: Observable<Bool> {
        return Observable.combineLatest(password.asObservable(),helpBool.asObservable()) { (password,helpBool) in
            self.isValidPassword(/password) && /helpBool
        }
    }
    
    
    override init() {
        super.init()
    }
    
    
    init(countryCode: String? , phone: String , user: User?) {
        super.init()
        self.countryCode.value = countryCode
        self.phoneNumber.value = phone
        self.userInfo.value = user
    }
    
    func proceed(_ completion:(Bool)->()){
        let status = Validation.shared.isValidForgetPassword(phone: password.value)
        switch status {
        case .success:
            completion(true)
        case .failure:
            completion(false)
        }
        
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
