//
//  NotificationsViewModel.swift
//  Conversify
//
//  Created by Apple on 29/11/18.
//

import UIKit
import RxSwift

class NotificationsViewModel: BaseRxViewModel {
    
    var items = Variable<[AppNotification]>([])
    var page = 1
    var loadMore = false
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var backRefresh : (()-> ())?
    
    
    func getNotifications(_ completion:@escaping (Bool)->()){
        beginCommunication.onNext(true)
        HomeSearchTarget.getNotifications(pageNo: "\(page)").request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                print(response)
                print(self!.page)
                completion(true)
                guard let safeResponse = response as? DictionaryResponse<AppNotification> else{
                    return
                }
                if self?.page == 1{
                    self?.items.value.removeAll()
                }
                for not in safeResponse.array ?? []{
                    self?.items.value.append(not)
                }
                self?.loadMore = (safeResponse.array?.count == 10)
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
                    completion(false)
            })<bag
    }
    
    func acceptRequest(notification: AppNotification, accept: Bool,  _ completion:@escaping (Bool)->()) {
        var acceptType = ""
        var groupType = ""
        switch notification.type! {
        case .groupInvite:
            acceptType = "INVITE"
            groupType = "GROUP"
        case .venueInvite:
            acceptType = "INVITE"
            groupType = "VENUE"
        case .groupRequest:
            acceptType = "REQUEST"
            groupType = "GROUP"
        case .venueRequest:
            acceptType = "REQUEST"
            groupType = "VENUE"
            
        case .requestFollow:
            acceptType = "FOLLOW"
            groupType = ""
            
        default:
            break
        }
        HomeSearchTarget.acceptRejectRequest(userId: /notification.user?.id, groupId: (notification.type == .groupRequest || notification.type == .groupInvite ) ? /notification.group?.id : /notification.venue?.id , groupType: groupType, acceptType: acceptType, accept: accept).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
//                if acceptType == "REQUEST"{
//                    UtilityFunctions.makeToast(text: "You ar", type: .success)
//                }
                completion(true)
                }, onError: { (error) in
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
                    
                    print(error)
                    completion(false)
            })<bag
    }
    
    func readNotification(notification: AppNotification) {
        HomeSearchTarget.readNotifications(notificationId: /notification.id).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                }, onError: { (error) in
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    func clearNotification(){
        PeopleTarget.clearNotification().request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<AppNotification> else{
                    return
                }
                self?.page = 1
                if self?.page == 1{
                    self?.items.value.removeAll()
                }
                for not in safeResponse.array ?? []{
                    self?.items.value.append(not)
                }
                self?.loadMore = (safeResponse.array?.count == 10)
                self?.endCommunication.onNext(true)
                }, onError: { (error) in
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
}
