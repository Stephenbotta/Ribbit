//
//  ChatListViewModal.swift
//  Conversify
//
//  Created by Apple on 28/11/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa
import RxDataSources

enum chatFlag : Int {
    case individual = 1
    case group = 2
}

class ChatListViewModal: BaseRxViewModel {
    
    var individualChat = Variable<[ChatListModel]>([])
    var groupChat = Variable<[ChatListModel]>([])
    var items = Variable<[ChatListModel]>([])
    var flag : Int = 1
    var page : Int = 1
    
    func getChatList(_ completion:@escaping (Bool)->()){
        
        ChatTarget.chatSummary(flag: flag.toString).request(apiBarrier: false).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<ChatListModel> else{
                    return
                }
                if self?.flag == chatFlag.individual.rawValue {
                    self?.individualChat.value = safeResponse.array ?? []
                }else {
                    self?.groupChat.value = safeResponse.array ?? []
                }
                self?.items.value = safeResponse.array ?? []
                completion(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
                    completion(false)
                    
                    
                    
            })<bag
    }
    
}
