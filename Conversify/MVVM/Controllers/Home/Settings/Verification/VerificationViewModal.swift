//
//  VerificationViewmodal.swift
//  Conversify
//
//  Created by Harminder on 08/01/19.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class VerificationViewModal: BaseRxViewModel {
    
    var verificationInfo = Variable<User?>(nil)
    var update = PublishSubject<Bool>()
    var email = Variable<String?>(nil)
    var uploadedDocUrl = Variable<String?>(nil)
    var documentUrl = Variable<URL?>(nil)
    var docImage = Variable<UIImage?>(nil)
    
    func getDetails(){
        SettingsTarget.verification(email: /email.value , phone: "", docUrl: /uploadedDocUrl.value)
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                
                if /self?.email.value != ""{
                    UtilityFunctions.makeToast(text: "We have sent verification email to your registered email id. Please verify your email.", type: .success)
                }
                //                Singleton.sharedInstance.loggedInUser = safeResponse.data
                self?.email.value = ""
                self?.uploadedDocUrl.value = ""
                self?.update.onNext(true)
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func getUserProfileData(userId : String? , _ completion:@escaping (Bool)->()){
        //        beginCommunication.onNext(true)
        ProfileTarget.getUserProfileData(userId: userId).request(apiBarrier: false).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<UserList> else{
                    return
                }
                if /userId == Singleton.sharedInstance.loggedInUser?.id{
                    let user = Singleton.sharedInstance.loggedInUser
                    user?.followerCount = safeResponse.data?.followerCount
                    user?.followingCount = safeResponse.data?.followingCount
                    user?.isEmailVerified = safeResponse.data?.isEmailVerified
                    user?.isPhoneNumberVerified = safeResponse.data?.isPhoneNumberVerified
                    user?.isUploaded = safeResponse.data?.isUploaded
                    Singleton.sharedInstance.loggedInUser = user
                }
                
                completion(true)
                }, onError: { [weak self] (error) in
                    //                    self?.endCommunication.onNext(true)
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    
    func uploadImage(){
        Loader.shared.start()
        S3.upload(image: docImage.value , success: { (imageName) in
            print(imageName)
            self.uploadedDocUrl.value = imageName
            self.getDetails()
        }) { (error) in
            print(error)
        }
    }
    
    func uploadDocuments(){
        if self.documentUrl.value != nil{
            Loader.shared.start()
            S3.upload(document: documentUrl.value, uploadProgress: { (val, _) in
                print(val)
            }, success: { (str, _, _) in
                print(str)
                self.uploadedDocUrl.value = str
                self.getDetails()
            }) { (str) in
                print("=== Error ==== " , str)
            }
        }
        
        
    }
    
}
