//
//  FollowersViewModal.swift
//  Conversify
//
//  Created by Harminder on 09/01/19.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

enum PrivacySelectedWhoCanSeeFlags : String{
    case profilePic = "3"
    case privateInfo = "7"
    case userName = "4"
    case messageTag = "6"
    case posting = "100"
    case likes = "101"
}

class FollowersViewModal: BaseRxViewModel {
    
    var groupMembers = Variable<[User?]>([])
    var allMembers = Variable<[User?]>([])
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var group = Variable<YourGroup?>(nil)
    var selectedPrivacy : PrivacySelectedWhoCanSeeFlags?
    var selectedUsers: ((_ users: [User])->())?
    var onlyFollowers: (()->())?
    var everyOne: (()->())?
    var isProfile = false
    
    var type = 0
    var queryOfUser : String = ""
    //FOR POST LIKERS
    var postId = ""
    
    var usersSelectedForPostModules = Variable<[User?]>([])
    
    init(selectedPrivacy: PrivacySelectedWhoCanSeeFlags?){
        self.selectedPrivacy = selectedPrivacy
    }
    
    override init() {
        super.init()
    }
    
    func submitQuery(){
        beginCommunication.onNext(true)
        SettingsTarget.query(message: /queryOfUser).request(apiBarrier: false).asObservable().subscribeOn(MainScheduler.instance).subscribe(onNext: { [weak self] (response) in
            self?.endCommunication.onNext(true)
            }, onError: { [weak self] (error) in
                print("Query Error \(error)")
                self?.endCommunication.onNext(true)
                if let err = error as? ResponseStatus {
                    self?.handleError(error: err)
                }
        })<bag
    }
    
    func retrieveFollowing(){
        beginCommunication.onNext(true)
        SettingsTarget.listFollowing().request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                self?.groupMembers.value = safeResponse.array ?? []
                self?.allMembers.value = safeResponse.array ?? []
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    func retrieveFollowers(){
        beginCommunication.onNext(true)
        SettingsTarget.listFollowers().request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                self?.groupMembers.value = safeResponse.array ?? []
                self?.allMembers.value = safeResponse.array ?? []
                if /self?.isProfile{
                    
                }else{
                    
                    var usersSelected = [User]()
                    var ids = [String]()
                    switch self?.selectedPrivacy ?? .profilePic {
                    case .profilePic:
                        usersSelected = Singleton.sharedInstance.loggedInUser?.imageVisibility ?? []
                    case .privateInfo :
                        usersSelected = Singleton.sharedInstance.loggedInUser?.personalInfoVisibility ?? []
                    case .userName :
                        usersSelected = Singleton.sharedInstance.loggedInUser?.nameVisibility ?? []
                    case .messageTag:
                        usersSelected = Singleton.sharedInstance.loggedInUser?.tagPermission ?? []
                    case .posting:
                        usersSelected = (self?.usersSelectedForPostModules.value as? [User]) ?? []
                    case .likes:
                        usersSelected = (self?.usersSelectedForPostModules.value as? [User]) ?? []
                    }
                    usersSelected.forEach({ (user) in
                        ids.append(/user.id)
                    })
                    self?.groupMembers.value.forEach({ (user) in
                        if ids.contains(/user?.id){
                            user?.isSelected = true
                        }
                    })
                }
                
                
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    
    func retrieveBlockedUsers(){
        beginCommunication.onNext(true)
        SettingsTarget.getBlockedUsers().request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                self?.groupMembers.value = safeResponse.array ?? []
                self?.allMembers.value = safeResponse.array ?? []
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    func retrieveLikes(){
        beginCommunication.onNext(true)
        SettingsTarget.getListLikers(postId: /postId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                self?.groupMembers.value = safeResponse.array ?? []
                self?.allMembers.value = safeResponse.array ?? []
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    
}
