//
//  FiltersViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/11/18.
//

import UIKit
import RxCocoa
import RxSwift
import Foundation


class FiltersViewModal: BaseRxViewModel {
    
    var filterTypes = Variable<[String]>(["Category","Date","Privacy","Location"])
    var interests = Variable<[Interests]>([])
    var privacies = Variable<[Privacy]>([])
    var selectedFilterIndex : Int? = 0
    var filterDateSelected :((String , String)->())?
    var filterLocationSelected:(()->())?
    var back:(()->())?
    var reset:(()->())?
    var filterInterestSelected:(([Interests])->())?
    var filterPrivacySelected:(([Privacy])->())?
    var selectedFilterDate = Variable<String?>(nil)
    var selectedFilterPrivacy = Variable<String?>(nil)
    var selectedFilterLocation = Variable<String?>(nil)
    var selectedFilterCategory = Variable<[String]?>([])
    var date = Variable<String?>(nil)
    var dismissController = PublishSubject<Bool>()
    var selectedLocation = ""
    var applyFilters : (()-> ())?
    var datePicked = PublishSubject<Bool>()
    
    
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
                Singleton.sharedInstance.interests = safeResponse.array ?? []
                self?.interests.value = safeResponse.array ?? []
                completion(true)
                }, onError: { (error) in
                    completion(false)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    func datePickerTapped() {
        
        let currentDate = Date()
        var dateComponents = DateComponents()
        dateComponents.year = 2
        let next2Year = Calendar.current.date(byAdding: dateComponents, to: currentDate)
        
        let datePicker = DatePickerDialog(textColor: .black,
                                          buttonColor: .black,
                                          font:  UIFont.systemFont(ofSize: 14, weight: .medium),
                                          showCancelButton: true)
        datePicker.show("Select date for filtering",
                        doneButtonTitle: "Done",
                        cancelButtonTitle: "Cancel",
                        minimumDate: Date(),
                        maximumDate: next2Year,
                        datePickerMode: .date) { [weak self] (date) in
                            if let dt = date {
                                let formatter1 = DateFormatter()
                                formatter1.dateFormat = "MMM d, yyyy"
                                self?.selectedFilterDate.value = formatter1.string(from: dt)
                                let formatter = DateFormatter()
                                formatter.dateFormat = "MM-dd-yyyy"
                                self?.date.value = formatter.string(from: dt)
                                self?.filterDateSelected?( /self?.selectedFilterDate.value , /self?.date.value)
                            }else{
                                self?.filterDateSelected?("" , "")
                                self?.selectedFilterDate.value = ""
                                self?.date.value = ""
                            }
                            self?.datePicked.onNext(true)
        }
    }
    
}

class Privacy {
    var isSelected: Bool?
    var name: String?
    
    init(name: String){
        self.name = name
        self.isSelected = false
    }
}
