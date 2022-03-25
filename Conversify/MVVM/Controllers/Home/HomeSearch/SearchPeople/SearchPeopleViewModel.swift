//
//  SearchPeopleViewModel.swift
//  Conversify
//
//  Created by Apple on 27/11/18.
//

import UIKit
import RxSwift
import GooglePlacePicker


class SearchPeopleViewModel: BaseRxViewModel , GMSPlacePickerViewControllerDelegate {

    var items = Variable<[UserList]>([])
    var page = 1
    var loadMore = false
    var interests = Variable<[Interests]>([])
    var locLat = Variable<String?>(nil)
    var locLng = Variable<String?>(nil)
    var locName = Variable<String?>(nil)
    var locAddress = Variable<String?>(nil)
    var locPicked = PublishSubject<Bool>()
    var range = 5
    var isValid: Observable<Bool> {
        return Observable.combineLatest(interests.asObservable(),locAddress.asObservable()) { (intrst,loc) in
            /intrst.count != 0 && self.isValidInformation(info: /loc)
        }
    }
    
    override init() {
        super.init()
        LoginTarget.getInterests()
            .request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: {(response) in
                guard let safeResponse = response as? DictionaryResponse<Interests> else{
                    return
                }
                self.interests.value = /safeResponse.array?.filter({$0.isSelected == true})
                }, onError: { (error) in
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    func getMutualInterestUsers(_ completion:@escaping (Bool)->()){
        HomeSearchTarget.getMutualInterestUsers(categories: /interests.value.map{/$0.id}, pageNo: "\(page)", locationLong: /locLng.value , locationLat: /locLat.value , range: /range.toString ).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                guard let safeResponse = response as? DictionaryResponse<UserList> else{
                    return
                }
                if self?.page == 1{
                    self?.items.value.removeAll()
                }
                for not in safeResponse.array ?? []{
                    self?.items.value.append(not)
                }
                self?.loadMore = (safeResponse.array?.count == 10)
                
                }, onError: { (error) in
                    print(error)
                    completion(false)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    
    func getPlaceDetails(){
        let config = GMSPlacePickerConfig(viewport: nil)
        let placePicker = GMSPlacePickerViewController(config: config)
        placePicker.delegate = self
        UIApplication.topViewController()?.present(placePicker, animated: true, completion: nil)
    }
    
    func placePicker(_ viewController: GMSPlacePickerViewController, didPick place: GMSPlace) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        self.locLat.value = place.coordinate.latitude.toString
        self.locLng.value = place.coordinate.longitude.toString
        self.locAddress.value = "\(/place.formattedAddress)"
        self.locName.value = "\(/place.name)"
        self.locPicked.onNext(true)
    }
    
    func placePickerDidCancel(_ viewController: GMSPlacePickerViewController) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        print("No place selected")
    }
    
    
}
