
//
//  HomePostViewModal.swift
//  Conversify
//
//  Created by Apple on 15/11/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class HomePostViewModal: BaseRxViewModel {
    
    var items = Variable<[PostList]>([])
    var itemsToShow = Variable<[PostList]>([])
    var page = 1
    var loadMore = false
    var userTags = Variable<[UserList]>([])
    var allMembers = Variable<[User?]>([])
    var whileData = WheelDetail()
    
    func searchUsers(serachText : String? , _ completion:@escaping ([UserList]?)->()){
        PostTarget.searchUser(search: serachText).request(apiBarrier: false).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<UserList> else{
                    return
                }
                self?.userTags.value.removeAll()
                self?.userTags.value = safeResponse.array ?? []
                completion(safeResponse.array)
                }, onError: { (error) in
                    
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    
    func getPostListing(_ completion:@escaping (Bool)->()){
        PostTarget.getPostListing(currentLat: LocationManager.sharedInstance.currentLocation?.currentLat, currentLong: LocationManager.sharedInstance.currentLocation?.currentLng, flag: /page.toString).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                guard let safeResponse = response as? DictionaryResponse<PostList> else{
                    return
                }
                if self?.page == 1{
                    self?.items.value.removeAll()
                }
               
                for not in safeResponse.array ?? []{
                    
                    self?.items.value.append(not)
                }
                self?.itemsToShow.value = self?.items.value ?? []
                let totalAds = Int((self?.items.value.count ?? 0) / 5) ?? 0
                if(totalAds > 0){
                        for i in 1..<totalAds {
                            print("adds")
                            self?.itemsToShow.value.insert(PostList(), at: ((i * 5) + i))
                        }}
                
                print(safeResponse.array?.count)
                self?.loadMore = (safeResponse.array?.count == 10)
                
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    func getFollowingListing(_ completion:@escaping (Bool)->()){
        SettingsTarget.listFollowing().request(apiBarrier: false)
        .asObservable()
        .subscribeOn(MainScheduler.instance)
        .subscribe(onNext: { [weak self] (response) in
            guard let safeResponse = response as? DictionaryResponse<User> else{
                return
            }
            completion(true)
           
            self?.allMembers.value = safeResponse.array ?? []
            
            }, onError: { [weak self] (error) in
                print(error)
             completion(false)
                if let err = error as? ResponseStatus {
                    self?.handleError(error: err)
                }
        })<bag
    }
    func commentOnPost(mediaId:String? , postId : String? , commentId : String? , userHashTag : String? , comment : String? ,postBy : String? , attachmentUrl:NSDictionary, _ completion:@escaping (Bool)->()){
        
        PostTarget.addEditComment(mediaId: /mediaId , postId: postId, commentId: commentId, userIdTag: userHashTag, comment: comment, postBy: postBy, attachmentUrl: attachmentUrl).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    
    func likePost(mediaId: String? , postId : String? , action : String? , postBy : String? ,  _ completion:@escaping (Bool)->()){
        PostTarget.likeOrUnlikePost(mediaId: /mediaId, postId: postId, action: action, postBy: postBy).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    
    func getOtherPostListing(userId:String?,_ completion:@escaping (Bool)->()){
        PostTarget.otherUserPost(pageNo: /page.toString, id: userId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                guard let safeResponse = response as? DictionaryResponse<PostList> else{
                    return
                }
                if self?.page == 1{
                    self?.items.value.removeAll()
                }
                for not in safeResponse.array ?? []{
                    self?.items.value.append(not)
                }
                print(safeResponse.array?.count)
                self?.loadMore = (safeResponse.array?.count == 10)
                
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    
    func showSpinWheel(_ completion:@escaping (Bool)->()){
        PostTarget.showSpinWheel.request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let resp = response as? WheelDetail else{return}
                self?.whileData = resp
                print(response)
                completion(true)
                }, onError: { (error) in
                  //  completion(false)
                    guard let err = error as? ResponseStatus else { return }
//                    switch err {
//                   
//                    case .clientError(let message):
//                        print("abc")
//                      //  UtilityFunctions.makeToast(text: message, type: .error)
//                    default : break
//                    }
            })<bag
    }
    
}
