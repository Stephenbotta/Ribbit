//
//  PeopleViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 28/11/18.
//

import UIKit
import RxCocoa
import RxSwift
import Foundation
import RxDataSources


class PeopleViewModal: BaseRxViewModel {
    
    var peopleData = Variable<[PeopleData?]>([])
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var peopleDataSectional = Variable<[PeopleDataSec]>([])
    
    
    func retrievePeopleCrossPaths(){
        beginCommunication.onNext(true)
        PeopleTarget.fetchPeoplePassed().request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<PeopleData> else{
                    return
                }
                
                self?.peopleData.value = safeResponse.array ?? []
                let peopleSecData = safeResponse.array?.map({ (datum) -> PeopleDataSec in
                    return PeopleDataSec(header: /datum , items: datum.userCrossed ?? [])
                })
                self?.peopleDataSectional.value = peopleSecData ?? []
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
}


struct PeopleDataSec {
    var header: PeopleData
    var items: [Item]
}

extension PeopleDataSec: SectionModelType {
    
    typealias Item = Any
    
    init(original: PeopleDataSec, items: [Item]) {
        self = original
        self.items = items
    }
}
