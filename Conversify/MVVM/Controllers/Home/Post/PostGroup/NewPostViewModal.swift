//
//  NewPostViewModal.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class GroupListViewModal: BaseRxViewModel {

    var items = Variable<[GroupList]>([])
    var page = 1
    var loadMore = false
    
    func getGroupListing(_ completion:@escaping (Bool)->()){
        PostTarget.getGroupList(currentLat: LocationManager.sharedInstance.currentLocation?.currentLat, currentLong: LocationManager.sharedInstance.currentLocation?.currentLng, flag:  /page.toString).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
               
                guard let safeResponse = response as? DictionaryResponse<GroupList> else{
                    return
                }
                if self?.page == 1{
                    self?.items.value.removeAll()
                }
                for not in safeResponse.array ?? []{
                    self?.items.value.append(not)
                }
                self?.loadMore = (safeResponse.array?.count == 10)
                completion(true)
                }, onError: { (error) in
                    print(error)
                    completion(false)
            })<bag
    }
    
}
