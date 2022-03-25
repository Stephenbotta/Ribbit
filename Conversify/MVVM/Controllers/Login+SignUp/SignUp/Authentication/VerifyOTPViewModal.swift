//
//  VerifyOTPViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 11/10/18.
//

import UIKit
import RxSwift
import RxCocoa
import Foundation


class VerifyOTPViewModal: BaseRxViewModel {
    
    var otpValue = Variable<String?>(nil)
    var userInfo = Variable<User?>(nil)
    var otpVerified = PublishSubject<Bool>()
    var otpNotVerified = PublishSubject<Bool>()
    var isVerificationProcedure = Variable<Bool?>(nil)
    var verified: (() -> ())?
   var helpBool = Variable<Bool?>(true)
   var isValid: Observable<Bool> {
       return Observable.combineLatest(otpValue.asObservable(), helpBool.asObservable()) { (otp,phone) in
           (self.isValidOTP(/otp)) && /self.helpBool.value
       }
   }
    override init() {
        super.init()
    }
    
    init(user: User? , isVerificationProcedure: Bool? = false) {
        super.init()
        self.userInfo.value = user
        self.isVerificationProcedure.value = isVerificationProcedure
    }
    
    //MARK: - Verify OTP
    /** Submit Otp **/
    func submitOtp(otp: String) {
        if !isValidOTP(otp){
            UtilityFunctions.makeToast(text: "Please enter valid otp", type: .error)
            return
        }
        otpValue.value = otp
        guard let safeValue = self.userInfo.value else { return }
        
        LoginTarget.verifyOTP(phoneNumber: safeValue.phoneNumber, otpCode: otpValue.value, email: safeValue.email, countryCode: safeValue.countryCode )
            .request(apiBarrier: true)
            .asObservable()
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else { return }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                self.userInfo.value = Singleton.sharedInstance.loggedInUser
                self.otpVerified.onNext(true)
            }, onError: { [weak self] (error) in
                if let err = error as? ResponseStatus {
                    self?.otpNotVerified.onNext(true)
                    self?.handleError(error: err)
                }
            })<bag
    }
    
    func verifyPhone(otp: String){
        if !isValidOTP(otp){
            UtilityFunctions.makeToast(text: "Please enter valid otp", type: .error)
            return
        }
        otpValue.value = otp
        guard let safeValue = self.userInfo.value else { return }
        
        LoginTarget.phoneVerification(otpCode: otpValue.value)
            .request(apiBarrier: true)
            .asObservable()
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else { return }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                self.userInfo.value = Singleton.sharedInstance.loggedInUser
                self.otpVerified.onNext(true)
            }, onError: { [weak self] (error) in
                if let err = error as? ResponseStatus {
                    self?.otpNotVerified.onNext(true)
                    self?.handleError(error: err)
                }
            })<bag
    }
    
    
    //MARK: - Resend OTP
    /** Resend Otp **/
    func resendOtp() {
        
        
        guard let safeValue = self.userInfo.value else { return }
        LoginTarget.resendOTP(phoneNumber: safeValue.phoneNumber, email: safeValue.email, countryCode: safeValue.countryCode)
            .request(apiBarrier: true)
            .asObservable()
            .subscribe(onNext: { (response) in
                UtilityFunctions.makeToast(text: "OTP has been resent successfully.", type: .success)
            }, onError: { [weak self] (error) in
                if let err = error as? ResponseStatus {
                    self?.handleError(error: err)
                }
            })<bag
    }
    
     func isValidOTP(_ testStr:String) -> Bool {
        if testStr.isBlank { return false }
        if testStr.length != 4 { return false }
        return true
    }
    
}
