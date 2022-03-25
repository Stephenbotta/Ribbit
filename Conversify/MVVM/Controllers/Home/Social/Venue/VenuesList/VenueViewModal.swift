//
//  VenueViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class VenueViewModal: BaseRxViewModel {
    
    var isSearchEnable = false
    var interests = Variable<VenueData?>(nil)
    var apiInterests = Variable<VenueData?>(nil)
    var allVenues = Variable<[Venues]>([])
    var page = Variable<Int?>(nil)
    var categorySelected = Variable<Interests?>(nil)
    var filteredVenues = Variable<[Venues]?>([])
    var date = Variable<String?>(nil)
    var categoryId = Variable<[String]?>([])
    var access = Variable<[String]>([])
    var privacies = Variable<[String]>([])
    var lat = Variable<String?>(nil)
    var long = Variable<String?>(nil)
    var selectedFilter = Variable<String?>(nil)
    var pickPlace = PublishSubject<Bool>()
    var updatedFilteredResult = PublishSubject<Bool>()
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var resetFilter = PublishSubject<Bool>()
    var groupIdToJoin = Variable<String?>(nil)
    var alreadyReset = false
    var selectedVenue = Variable<Venues?>(nil)
    let filterVc = R.storyboard.venue.filtersViewController()
    var catNames = [String]()
    var filteredDate = Variable<String?>(nil)
    
    
    //for search
    var text = Variable<String?>(nil)
    var pageV = 1
    var loadMore = false
    var noMoreData = PublishSubject<Bool>()
    var resetTable = PublishSubject<Bool>()
    
    
    func getFilteredVenues(){
        
        var filters = [String]()
        
        if catNames.count != 0{
            catNames.forEach { (cat) in
                filters.append(cat)
            }
        }
        if /filteredDate.value != ""{
            filters.append(/filteredDate.value)
        }
        if /access.value.count != 0{
            privacies.value.forEach { (cat) in
                filters.append(cat)
            }
        }
        
        if /lat.value != ""{
            filters.append(/filterVc?.filterVM.selectedLocation)
        }
        
        selectedFilter.value = filters.joined(separator: ", ")
        
        //        beginCommunication.onNext(true)
        VenueTarget.filterVenue(date: date.value , categoryId: categoryId.value?.toJson() , privateV: access.value.toJson() , lat: /lat.value, long: /long.value)
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<Venues> else{
                    return
                }
                self?.filteredVenues.value = safeResponse.array
                let venueD = VenueData()
                venueD.venueNearYou = self?.filteredVenues.value
                self?.interests.value?.yourVenueData = self?.filteredVenues.value
                venueD.yourVenueData = []
                self?.interests.value = venueD
                self?.apiInterests.value = venueD
                self?.updatedFilteredResult.onNext(true)
                //                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.getFilteredVenues()
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    func retrieveVenues(showLoader: Bool = false , beginComm: Bool = true , _ completion:@escaping (Bool)->()){
        if beginComm{
            beginCommunication.onNext(true)
        }
        selectedFilter.value = "Suggested"
        page.value = 1
        VenueTarget.getVenueList(currentLat: /LocationManager.sharedInstance.currentLocation?.currentLat, currentLong: /LocationManager.sharedInstance.currentLocation?.currentLng, flag: /page.value?.toString).request(apiBarrier: showLoader)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<VenueData> else{
                    return
                }
                self?.apiInterests.value = safeResponse.data
                self?.interests.value = safeResponse.data
                let venueNearme = (safeResponse.data?.venueNearYou.map({$0})) ?? []
                let myVenue = (safeResponse.data?.yourVenueData.map({$0})) ?? []
                self?.allVenues.value = venueNearme + myVenue
                self?.endCommunication.onNext(true)
                completion(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.retrieveVenues(showLoader: showLoader, beginComm: beginComm, completion)
                    self?.endCommunication.onNext(true)
                    completion(false)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    func filterOptions(){
        
        
        filterVc?.filterVM.filterDateSelected = { [weak self]  (selectedFilter , date ) in
            self?.date.value = date
            self?.filteredDate.value = selectedFilter
            self?.alreadyReset = false
        }
        
        filterVc?.filterVM.applyFilters = { [ weak self] in
            self?.getFilteredVenues()
        }
        
        filterVc?.filterVM.filterInterestSelected = { [weak self] (interest) in
            self?.categoryId.value = []
            self?.alreadyReset = false
            var catIds = [String]()
            self?.catNames = []
            for intrst in interest{
                if /intrst.isSelected{
                    catIds.append(/intrst.id)
                    self?.catNames.append(/intrst.category)
                }
            }
            if self?.catNames.count != 0{
                self?.categoryId.value = catIds
            }
        }
        
        filterVc?.filterVM.filterLocationSelected = { [ weak self] in
            self?.pickPlace.onNext(true)
            self?.alreadyReset = false
        }
        
        filterVc?.filterVM.reset = { [ weak self] in
            self?.lat.value = ""
            self?.long.value = ""
            self?.categoryId.value = []
            self?.date.value = ""
            self?.access.value = []
            self?.selectedFilter.value = "Suggested"
            self?.isSearchEnable = false
            if /self?.alreadyReset{
                self?.alreadyReset = true
            }else{
                self?.alreadyReset = true
                self?.retrieveVenues(showLoader: true , beginComm: true, { (status) in
                    if status{
                        self?.resetFilter.onNext(true)
                    }
                })
            }
        }
        
        filterVc?.filterVM.filterPrivacySelected = { [weak self] privacies in
            self?.access.value = []
            self?.privacies.value = []
            for (index , privacy) in privacies.enumerated(){
                if /privacy.isSelected{
                    self?.privacies.value.append(/privacy.name)
                    if index == 0 {
                        self?.access.value.append("2")
                    }else{
                        self?.access.value.append("1")
                    }
                }
            }
            self?.alreadyReset = false
            
        }
        
        filterVc?.filterVM.back = {
            
        }
        
        UIApplication.topViewController()?.presentVC(filterVc ?? UIViewController())
        
        
    }
    
    
    //MARK::- API HANDLER FOR SEARCH
    func getVenues(){
        resetTable.onNext(true)
        VenueTarget.searchVenue(text: text.value , page: /pageV.toString ).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<Venues> else{
                    return
                }
                if self?.pageV == 1{
                    self?.allVenues.value.removeAll()
                }
                for not in safeResponse.array ?? []{
                    self?.allVenues.value.append(not)
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
