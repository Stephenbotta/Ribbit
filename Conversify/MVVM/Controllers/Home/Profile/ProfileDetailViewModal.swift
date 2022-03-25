//
//  ProfileDetailViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 14/10/18.
//
import UIKit
import RxSwift
import Foundation
import RxCocoa

class ProfileDetailViewModal: BaseRxViewModel {
    
    //MARK::- PRPOERTY
    var userInfo = Variable<UserList?>(nil)
    var userType : ProfileScreenType = .loggedInUser
    var userId : String?
    var userData : UserList?
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var userBlocked = PublishSubject<Bool>()
    var stats = Variable<UserStats?>(nil)
    var daylyChalengeData = Variable<StoriesDetail?>(nil)
    
    //MARK::- FUNCTION
    func getUserProfileData(userId : String? , _ completion:@escaping (Bool)->()){
        beginCommunication.onNext(true)
        ProfileTarget.getUserProfileData(userId: userId).request(apiBarrier: false).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                
                guard let safeResponse = response as? DictionaryResponse<UserList> else{
                    return
                }
                self?.userInfo.value = safeResponse.data
                self?.userId = /safeResponse.data?.id
                self?.endCommunication.onNext(true)
                completion(true)
                }, onError: { [weak self] (error) in
                    self?.endCommunication.onNext(true)
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func getUserNameProfileData(userName : String? , _ completion:@escaping (Bool)->()){
        beginCommunication.onNext(true)
        ProfileTarget.getUserNameProfileData(userName: userName).request(apiBarrier: false).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<UserList> else{
                    return
                }
                self?.userId = /safeResponse.data?.id
                self?.userInfo.value = safeResponse.data
                self?.endCommunication.onNext(true)
                completion(true)
                }, onError: { [weak self] (error) in
                    self?.endCommunication.onNext(true)
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    
    
    func followUser(userId : String? , follow: Bool){
        
        PeopleTarget.followPeople(follow: follow ? "1" : "2", userId: userId ).request(apiBarrier: false).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
            })<bag
        
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
    
    
    func blockUser(userId: String? , isBlock: Bool){
        ProfileTarget.blockUser(userId: /userId, isBlock: isBlock)
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.userBlocked.onNext(true)
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    func getUserStats(_ completion:@escaping (Bool)->()){
        ProfileTarget.getStats
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let resp = (  response as? DictionaryResponse<UserStats>) else { return }
                self?.stats.value = resp.data
                
                completion(true)
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    func dailyChalenge (_ completion:@escaping (Bool)->()){
        ProfileTarget.getDailyChalenge
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let resp =  response as? StoriesDetail else { return }
                self?.daylyChalengeData.value = resp
                self?.daylyChalengeData.value?.daiLyChailenge?[0].title
                completion(true)
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default: break
                    }
            })<bag
    }
    
    
    func followUserByQR(userId : String? , follow: Bool, _ completion:@escaping (Bool)->()){
        
        PeopleTarget.followPeople(follow: follow ? "1" : "2", userId: userId ).request(apiBarrier: false).asObservable()
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
    
}

