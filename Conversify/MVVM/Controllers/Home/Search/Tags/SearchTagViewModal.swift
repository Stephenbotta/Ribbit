//
//  SearchTagViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 03/01/19.
//

import UIKit
import RxSwift

class SearchTagViewModal: BaseRxViewModel {

    //MARK::- PROPERTIES
    var items = Variable<[Tags]>([])
    var text = Variable<String?>(nil)
    var page = 1
    var loadMore = false
    var noMoreData = PublishSubject<Bool>()
    var resetTable = PublishSubject<Bool>()
    
    //MARK::- API HANDLER
    func getTags(){
        resetTable.onNext(true)
        PeopleTarget.searchTags(text: text.value, page: /page.toString).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                
                guard let safeResponse = response as? DictionaryResponse<Tags> else{
                    return
                }
                if self?.page == 1{
                    self?.items.value.removeAll()
                }
                for not in safeResponse.array ?? []{
                    self?.items.value.append(not)
                }
                self?.loadMore = (safeResponse.array?.count == 10)
                if !(/self?.loadMore){
                    self?.noMoreData.onNext(true)
                }
                }, onError: { (error) in
                    print(error)
                    
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
}
