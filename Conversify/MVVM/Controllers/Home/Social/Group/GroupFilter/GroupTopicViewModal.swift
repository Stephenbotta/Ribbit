//
//  GroupTopicViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 14/11/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class GroupTopicViewModal: BaseRxViewModel {
    
    var category = Variable<Interests?>(nil)
    var page = 1
    var suggestedGroups = Variable<[SuggestedGroup]>([])
    var loadMore = false
    var joinedSuccesfully = PublishSubject<Bool>()
    var selectedGroup = Variable<YourGroup?>(nil)
    var noData = PublishSubject<Bool>()
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var refresh: (() -> ())?
    
    override init() {
        super.init()
    }
    
    init(categor: Interests? ) {
        super.init()
        category.value = categor
    }
    
    func retrieveFilteredGroups(){
        beginCommunication.onNext(true)
        GroupTarget.getFilteredGroups(page: page.toString, categoryId: category.value?.id)
            .request(apiBarrier: false)
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
                self?.noData.onNext(/self?.suggestedGroups.value.count == 0)
                self?.endCommunication.onNext(true)
                }, onError: {[weak self]  (error) in
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
        
    }
    
    func joinGroup(row: Int){
        
        
        let urGroup = YourGroup()
        urGroup.id = /suggestedGroups.value[row].id
        urGroup.createdOn = 0
        urGroup.groupName = /suggestedGroups.value[row].groupName
        urGroup.imageUrl = /suggestedGroups.value[row].imageUrl
        urGroup.unReadCounts = 0
        selectedGroup.value = urGroup
        if /suggestedGroups.value[row].isMember {
            joinedSuccesfully.onNext(true)
            return
        }
        
        GroupTarget.joinGroup(groupId: /suggestedGroups.value[row].id, userId: "", isPrivate: String(/suggestedGroups.value[row].isPrivate), adminId: /suggestedGroups.value[row].adminId).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                if /self?.suggestedGroups.value[row].isPrivate{
                    UtilityFunctions.makeToast(text: "Notification sent to admin. Please wait for approval", type: .success)
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
    
    
    
    
}
