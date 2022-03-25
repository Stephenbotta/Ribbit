//
//  SearchPostViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 04/01/19.
//

import UIKit
import RxSwift

class SearchPostViewModal: BaseRxViewModel {
    
    //MARK::- PROPERTIES
    enum CellType {
        case normal
        case expanded
    }
    var items = Variable<[PostList]>([])
    var text = Variable<String?>(nil)
    var page = 1
    var loadMore = false
    var layoutValues = [CellType]()
    var hideCollection = PublishSubject<Bool>()
    var noMoreData = PublishSubject<Bool>()
    var resetTable = PublishSubject<Bool>()

    //MARK::- API HANDLER
    func getPosts(){
        resetTable.onNext(true)
        PostTarget.searchPosts(text: text.value , page: /page.toString ).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<PostList> else{
                    return
                }
               
                self?.hideCollection.onNext(safeResponse.array?.count == 0)
                if safeResponse.array?.count == 0{
                   return
                }
                if self?.page == 1{
                    self?.layoutValues = []
                    self?.items.value.removeAll()
                }
                
                for (index,not) in (safeResponse.array?.enumerated())! {
                    self?.items.value.append(not)
                    if index % 4 == 0{
                        self?.layoutValues.append(.expanded)
                    }else{
                        self?.layoutValues.append(.normal)
                    }
                }
                
                if safeResponse.array?.count == 0{
                     self?.layoutValues = []
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
