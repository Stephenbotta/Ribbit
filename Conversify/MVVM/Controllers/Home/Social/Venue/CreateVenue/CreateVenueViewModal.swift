//
//  CreateVenueViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa


class CreateVenueViewModal: BaseRxViewModel {
    
    var items = Variable<[Interests]>([])
    var isVenue = true
    var loadMore = false
    var page = 1
    var backWithInterest:((Interests?)->())?
    var back:(()->())?
    
    init(isVenueNav: Bool?) {
        super.init()
        self.isVenue = /isVenueNav
        if Singleton.sharedInstance.interests?.count != 0 && Singleton.sharedInstance.interests != nil{
            self.items.value = Singleton.sharedInstance.interests ?? []
        }
    }
    
    override init() {
        super.init()
    }
    
    func retrieveCategories(_ completion:@escaping (Bool)->()){
        if Singleton.sharedInstance.interests?.count != 0 && Singleton.sharedInstance.interests != nil{
            self.items.value = Singleton.sharedInstance.interests ?? []
            completion(true)
            return
        }
        LoginTarget.getInterests()
            .request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<Interests> else{
                    return
                }
                Singleton.sharedInstance.interests = safeResponse.array ?? []
                self?.items.value = safeResponse.array ?? []
                }, onError: { (error) in
                    completion(false)
            })<bag
    }
    
    
}
