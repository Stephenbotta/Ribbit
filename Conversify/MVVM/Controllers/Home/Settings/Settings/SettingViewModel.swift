//
//  SettingViewModel.swift
//  Conversify
//
//  Created by Apple on 04/12/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa
import RxDataSources

class SettingViewModel: BaseRxViewModel {
    
    var arraySettings = Variable<[SettingElements]>([])
    var updated = PublishSubject<Bool>()
    var contactDetails = ""
    var selectedEmails = [String]()
    var alertUpdated = PublishSubject<Bool>()
    
    func getSettingArray(){
        arraySettings.value = [ SettingElements(head: "Profile", items: ["Verification","Invite People", "Share your Contact Details"]) , SettingElements(head: "Privacy", items: ["Hide Personal Info. from other users" , "Blocked Users","Access Location"]) ,SettingElements(head: "About", items: ["Contact Us" ,
                                                                                                                                                                                                                                                                                                        "Terms and conditions","Alert/Report" ,"Logout"])]
    }
    
    
    func logout(_ completion:@escaping (Bool)->()){
        LoginTarget.logout()
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
            })<bag
    }
    
    func updateProfilePicSettings(everyOne: Bool , isMyFollowers: Bool , followers: [String]){
        SettingsTarget.whoCanSeeMyProfilePic(everyOne: everyOne , imageVisibilityForFollowers: isMyFollowers, userIds: followers.toJson() ).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                self?.updated.onNext(true)
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func updateUserNameSettings(everyOne: Bool , nameVisibility: Bool , followers: [String]){
        SettingsTarget.whoCanSeeMyUserName(everyOne: everyOne, nameVisibility: nameVisibility, userIds: followers.toJson()).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                self?.updated.onNext(true)
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func updateTagSettings(everyOne: Bool , tagging: Bool , followers: [String]){
        SettingsTarget.whoCanMessageMe(everyOne: everyOne, tagPermission: tagging, userIds: followers.toJson()).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                self?.updated.onNext(true)
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func updatePrivacy(permission: Bool){
        SettingsTarget.updateAccountType(action: "\(/permission)").request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                self?.updated.onNext(true)
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func updatePrivateInfoSettings(everyOne: Bool , permission: Bool , followers: [String]){
        SettingsTarget.whoCanSeePrivateInfo(everyOne: everyOne, permission: permission, userIds: followers.toJson() ).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                self?.updated.onNext(true)
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func invitePpl(emails:[String]?, phonNum: [String]?){
        SettingsTarget.invitePpl(email: /emails?.toJson(), phoneNum: /phonNum?.toJson()).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                print("invited")
             //   UtilityFunctions.makeToast(text: "Invitations sent succesfully", type: .success)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
            })<bag
    }
    
    
    func handleAlert(action: Bool){
        SettingsTarget.alertSettings(action: "\(action)").request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                print("invited")
                
                let user = Singleton.sharedInstance.loggedInUser
                user?.isAlert = action
                Singleton.sharedInstance.loggedInUser = user
                self?.alertUpdated.onNext(true)
                }, onError: { [weak self] (error) in
                    self?.alertUpdated.onNext(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func gatherContactDetails(){
        let name = "Name : " + /Singleton.sharedInstance.loggedInUser?.firstName
        let userName = "Username : " + /Singleton.sharedInstance.loggedInUser?.userName
        let website = "Website : " + /Singleton.sharedInstance.loggedInUser?.website
        let bio = "Bio : " + /Singleton.sharedInstance.loggedInUser?.bio
        let desig = "Designation : " + /Singleton.sharedInstance.loggedInUser?.designation
        let company = "Company/Workplace : " + /Singleton.sharedInstance.loggedInUser?.company
        let email = "Email : " + /Singleton.sharedInstance.loggedInUser?.email
        let phn = "Phone : " + /Singleton.sharedInstance.loggedInUser?.countryCode + " " + /Singleton.sharedInstance.loggedInUser?.phoneNumber
        let gender = "Gender : " + /Singleton.sharedInstance.loggedInUser?.gender
        
        let prof = (/Singleton.sharedInstance.loggedInUser?.designation == "" && /Singleton.sharedInstance.loggedInUser?.company == "") ? "" : ("Professional Information" + "\n")
        
        contactDetails = name + "\n" + userName +  "\n" + (/Singleton.sharedInstance.loggedInUser?.website == "" ? "" : website  + "\n") + (/Singleton.sharedInstance.loggedInUser?.bio == "" ? "" : bio  + "\n") +  prof + (/Singleton.sharedInstance.loggedInUser?.designation == "" ? "" : desig  + "\n")  + (/Singleton.sharedInstance.loggedInUser?.company == "" ? "" : company  + "\n") + "Private Information" + "\n" + email + "\n" + phn + "\n" + (/Singleton.sharedInstance.loggedInUser?.gender == "" ? "" : gender )
        
    }
    
}


struct SettingElements {
    var head: String
    var items: [Item]
}

extension SettingElements : SectionModelType {
    
    typealias Item = Any
    
    init(original: SettingElements, items: [Item]) {
        self = original
        self.items = items
    }
}

