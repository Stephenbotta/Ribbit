//
//  VenueDetailViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 19/12/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class VenueDetailViewModal: BaseRxViewModel {
    
    var venue = Variable<Venues?>(nil)
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var groupMembers = Variable<[Members?]>([])
    var groupJoinesSuccessfully = PublishSubject<Bool>()
    var privateJoinedSuccessfully = PublishSubject<Bool>()
    var refresh : (()->())?
     var refreshJoined : (()->())?
    
    override init() {
        super.init()
    }
    
    init(venu: Venues?){
        super.init()
        self.venue.value = venu
    }
    
    
    func retrieveVenues(){
        beginCommunication.onNext(true)
        VenueTarget.getVenueDetail(venueId: venue.value?.groupId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<Venues> else{
                    return
                }
                self?.venue.value = safeResponse.data
                self?.groupMembers.value = self?.venue.value?.membersList ?? []
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.retrieveVenues()
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    
    func joinVenue(groupId: String , isPrivate: String , adminId: String?){
        VenueTarget.joinVenue(groupId: groupId , userId: "", isPrivate: /isPrivate, adminId: /adminId).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                if /isPrivate.toBool(){
                    self?.privateJoinedSuccessfully.onNext(true)
                    UtilityFunctions.makeToast(text: "Notification sent to admin. Please wait for approval", type: .success)
                }else{
                    self?.groupJoinesSuccessfully.onNext(true)
                }
                
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
}
