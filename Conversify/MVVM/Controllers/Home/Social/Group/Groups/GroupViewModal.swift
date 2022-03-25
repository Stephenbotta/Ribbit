//
//  GroupViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 13/11/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class GroupViewModal: BaseRxViewModel {
    
    var category = Variable<Interests?>(nil)
    var groups = Variable<GroupData?>(nil)
    var suggestedGroups = Variable<[SuggestedGroup]>([])
    var yourGroups = Variable<[YourGroup]>([])
    var retrieveSuccessfully = PublishSubject<Bool>()
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var joinedSuccesfully = PublishSubject<Bool>()
    var joinedSuccesfullyPrivate = PublishSubject<Bool>()
    var selectedGroup = Variable<YourGroup?>(nil)
    var selectedSearchIndex = -1
    
    //for search
    var text = Variable<String?>(nil)
    var page = 1
    var loadMore = false
    var noMoreData = PublishSubject<Bool>()
    var resetTable = PublishSubject<Bool>()
    
    override init() {
        super.init()
    }
    
    init(categor: Interests? ) {
        super.init()
        category.value = categor
    }
    
    
    func retrieveGroups(ifRefresh: Bool = false){
        if !ifRefresh{
            beginCommunication.onNext(true)
        }
        GroupTarget.getGroups().request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<GroupData> else{
                    return
                }
                self?.groups.value = safeResponse.data
                self?.suggestedGroups.value = self?.groups.value?.suggestedGroups ?? []
                self?.yourGroups.value = self?.groups.value?.yourGroups ?? []
                self?.retrieveSuccessfully.onNext(true)
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                     self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    func joinGroup(row: Int){
        selectedSearchIndex = row
        let urGroup = YourGroup()
        urGroup.id = /suggestedGroups.value[row].id
        urGroup.createdOn = 0
        urGroup.groupName = /suggestedGroups.value[row].groupName
        urGroup.imageUrl = /suggestedGroups.value[row].imageUrl
        urGroup.unReadCounts = 0
        
        selectedGroup.value = urGroup
        
        GroupTarget.joinGroup(groupId: /suggestedGroups.value[row].id, userId: "", isPrivate: String(/suggestedGroups.value[row].isPrivate), adminId: /suggestedGroups.value[row].adminId).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                if /self?.suggestedGroups.value[row].isPrivate{
                    UtilityFunctions.makeToast(text: "Notification sent to admin. Please wait for approval", type: .success)
                    self?.joinedSuccesfullyPrivate.onNext(true)
                    self?.selectedSearchIndex = -1
                }else{
                   self?.joinedSuccesfully.onNext(true)
                }
                
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    //MARK::- API HANDLER FOR SEARCH
    func getGroups(){
        resetTable.onNext(true)
        GroupTarget.searchGroup(text: text.value , page: /page.toString ).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<SuggestedGroup> else{
                    return
                }
                if self?.page == 1{
                    self?.suggestedGroups.value.removeAll()
                }
                for not in safeResponse.array ?? []{
                    self?.suggestedGroups.value.append(not)
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
