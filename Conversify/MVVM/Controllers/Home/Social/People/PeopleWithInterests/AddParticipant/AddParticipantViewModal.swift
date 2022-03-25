//
//  AddParticipantViewModal.swift
//  Conversify
//
//  Created by Harminder on 05/12/18.
//


import UIKit
import RxCocoa
import RxSwift
import Foundation

class AddParticipantViewModal: BaseRxViewModel {
    
    //MARK::- VARIABLES
    var participantsList = Variable<[User?]>([])
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var particpantsIds : (([User])->())?
    var groupId = Variable<String?>(nil)
    var venueId = Variable<String?>(nil)
    
    override init() {
        super.init()
    }
    
    init(group: String? , venue: String?){
        self.groupId.value = group
        self.venueId.value = venue
    }
    
    
    //array of users already selected
    var selectedUsers = Variable<[User?]>([])
    
    //MARK::- FUNCTIONS
    func retrieveFollowers(){
        beginCommunication.onNext(true)
        PeopleTarget.getFollowersListing(groupId: self.groupId.value , venueId: self.venueId.value).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                self?.participantsList.value = safeResponse.array ?? []
                if self?.selectedUsers.value.count != 0{
                    let selectedIds = self?.selectedUsers.value.map { (user) -> String in
                        return /user?.id
                    }
                    self?.participantsList.value.forEachEnumerated({ [ weak self ] (index, user) in
                        if /selectedIds?.contains(/user?.id){
                            self?.participantsList.value[index]?.selectedForGroup = true
                        }
                    })
                }
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                     self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    func retrieveAllFollowers(){
        beginCommunication.onNext(true)
        PeopleTarget.followedPeopleListing(flag: "1").request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                self?.participantsList.value = safeResponse.array ?? []
                if self?.selectedUsers.value.count != 0{
                    let selectedIds = self?.selectedUsers.value.map { (user) -> String in
                        return /user?.id
                    }
                    self?.participantsList.value.forEachEnumerated({ [ weak self ] (index, user) in
                        if /selectedIds?.contains(/user?.id){
                            self?.participantsList.value[index]?.selectedForGroup = true
                        }
                    })
                }
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
}
