
//
//  SearchTagsTableViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 04/01/19.
//

import UIKit
import RxSwift
import RxCocoa

class SearchTagsTableViewModal: BaseRxViewModel {
    
    var updated = PublishSubject<Bool>()
    
    func followTag(tagId: String? , follow: String?){
        
        PeopleTarget.followUnFollowTag(tagId: /tagId, follow: /follow).request(apiBarrier: false).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.updated.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.updated.onNext(false)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
                    
            })<bag
    }
    
    
}
