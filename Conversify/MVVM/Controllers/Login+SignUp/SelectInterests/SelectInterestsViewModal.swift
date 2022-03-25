//
//  SelectInterestsViewModal.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 13/10/18.
//

import UIKit
import ADCountryPicker
import RxSwift
import Foundation
import RxCocoa

class SelectInterestsViewModal: BaseRxViewModel {
    
    var interests = Variable<[Interests]>([])
    var selectedInterests = [Interests]()
    var isFromFilter = false
    var isFromHome = false
    var isFromEditProfile = false
    var selectedInterestIndex = -1
    var selectedFilterInterest:((_ data: Interests?)->())?
    var selectedFilterInterests:((_ data: [Interests]?)->())?
    
    func retrieveInterests(_ completion:@escaping (Bool)->()){
        var apiBarrier = true
        if Singleton.sharedInstance.interests?.count != 0 || Singleton.sharedInstance.interests != nil{
            apiBarrier = false
            self.interests.value = Singleton.sharedInstance.interests ?? []
            completion(true)
        }
        LoginTarget.getInterests()
            .request(apiBarrier: apiBarrier)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<Interests> else{
                    return
                }
                let selectedInterestIds = /self?.selectedInterests.map({/$0.id})
                for response in /safeResponse.array {
                    if selectedInterestIds.contains(/response.id) {
                        response.isSelected = true
                    }
                }
                Singleton.sharedInstance.interests = safeResponse.array ?? []
                self?.interests.value = safeResponse.array ?? []
                completion(true)
                }, onError: { (error) in
                    completion(false)
            })<bag
    }
    
    func updateInterests(_ completion:@escaping (Bool)->()){
        let intrsts = interests.value.filter { return /$0.isSelected }
        let interstIds = intrsts.map { return $0.id }
        if intrsts.count <= 2{
            UtilityFunctions.makeToast(text: "Please select atleast 3 interests", type: .error)
            return
        }
        LoginTarget.updateUserInterests(interests:  interstIds.toJson() )
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: {  (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else { return }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                let user = Singleton.sharedInstance.loggedInUser
                user?.isPasswordExist = true
                user?.isVerified = true
                user?.isInterestSelected = true
                user?.isProfileComplete = true
                Singleton.sharedInstance.loggedInUser = user
                completion(true)
            }, onError: { (error) in
                completion(false)
            })<bag
    }
    
    
    func updateContact(contact:[String] ,_ completion:@escaping (Bool)->()){
        LoginTarget.promotUser(phoneNumber: contact).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: {  (response) in
                completion(true)
            }, onError: { (error) in
                completion(false)
            })<bag
                
    }
    
    func selectedInterst(){
        if selectedInterestIndex == -1 {
            UtilityFunctions.makeToast(text: "Please select 1 interest", type: .error)
            return
        }
        self.selectedFilterInterest?(interests.value[selectedInterestIndex])
    }
    
    func selectedIntersts(){
        let interests = self.interests.value.filter({$0.isSelected == true})
        if interests.count == 0 {
            UtilityFunctions.makeToast(text: "Please select atleast 1 interest", type: .error)
            return
        }
        self.selectedFilterInterests?(interests)
    }
    
}
