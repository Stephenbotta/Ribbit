//
//  VenueBaseViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 31/10/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class VenueBaseViewModal: BaseRxViewModel {
    
    var interests = Variable<[Venues]?>([])
    var date = Variable<String?>(nil)
    var categoryId = Variable<String?>(nil)
    var access = Variable<String?>(nil)
    var lat = Variable<String?>(nil)
    var long = Variable<String?>(nil)
    var selectedFilter = Variable<String?>(nil)
    
    func getFilteredVenues(_ completion:@escaping (Bool)->()){
        VenueTarget.filterVenue(date: date.value , categoryId: categoryId.value, privateV: access.value, lat: lat.value, long: long.value)
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<Venues> else{
                    return
                }
                self?.interests.value = safeResponse.array
                //                self?.interests.value = safeResponse.array ?? []
                completion(true)
                }, onError: { (error) in
                    completion(false)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    

    
   
    
}
