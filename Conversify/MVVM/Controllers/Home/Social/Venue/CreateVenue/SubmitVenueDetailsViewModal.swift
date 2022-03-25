//
//  SubmitVenueDetailsViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 24/10/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa
import GooglePlacePicker

class SubmitVenueDetailsViewModal: BaseRxViewModel {
    
    var category = Variable<Interests?>(nil)
    var venueImage = Variable<UIImage?>(nil)
    var venueImageStr = Variable<String?>(nil)
    var venueDoc = Variable<Any?>(nil)
    var venueTitle = Variable<String?>(nil)
    var venueLocLat = Variable<String?>(nil)
    var venueLocLng = Variable<String?>(nil)
    var userName = Variable<String?>(nil)
    var dateMilli = Variable<String?>(nil)
    var dateStr = Variable<String?>(nil)
    var venueLocName = Variable<String?>(nil)
    var venueLocAddress = Variable<String?>(nil)
    var tags = Variable<String?>(nil)
    var tagsArray = Variable<String?>(nil)
    var access = Variable<String?>(nil)
    var documentUrl = Variable<URL?>(nil)
    var documentImage = Variable<UIImage?>(nil)
    var uploadedDocUrl = Variable<String?>(nil)
    var selectedUsers = Variable<[User]>([])
    
    var isValid: Observable<Bool> {
        return Observable.combineLatest(venueTitle.asObservable(), venueLocName.asObservable() , dateMilli.asObservable(), tags.asObservable() ) { (venueTitle,venueLocName , date , tags) in
            self.isValidInformation(info: /venueTitle) && self.isValidInformation(info: /venueLocName) && self.isValidInformation(info: /date) && self.isValidInformation(info: /tags)
        }
    }
    
    override init() {
        super.init()
    }
    
    init(categor: Interests? ) {
        super.init()
        category.value = categor
    }
    
    
    
    
}

extension SubmitVenueDetailsViewModal : GMSPlacePickerViewControllerDelegate{
    
    func datePickerTapped(_ completion:@escaping (Bool)->()) {
        
        let currentDate = Date()
        var dateComponents = DateComponents()
        dateComponents.year = 2
        let next2Year = Calendar.current.date(byAdding: dateComponents, to: currentDate)
        
        let datePicker = DatePickerDialog(textColor: .black,
                                          buttonColor: .black,
                                          font: UIFont.systemFont(ofSize: 14, weight: .medium) ,
                                          showCancelButton: true)
        datePicker.show("Select Date & Time for venue",
                        doneButtonTitle: "Done",
                        cancelButtonTitle: "Cancel",
                        minimumDate: Date(),
                        maximumDate: next2Year,
                        datePickerMode: .dateAndTime) { [weak self] (date) in
                            if let dt = date {
                                let formatter = DateFormatter()
                                formatter.dateFormat = "MMM d yyyy, h:mm a"
                                self?.dateMilli.value = dt.millisecondsSince1970
                                self?.dateStr.value = formatter.string(from: dt)
                                completion(true)
                            }
        }
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
        self.venueLocLat.value = place.coordinate.latitude.toString
        self.venueLocLng.value = place.coordinate.longitude.toString
        self.venueLocAddress.value = "\(/place.formattedAddress)"
        self.venueLocName.value = "\(/place.name)"
        
    }
    
    func placePickerDidCancel(_ viewController: GMSPlacePickerViewController) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        
        print("No place selected")
    }
    
}

//MARK::- API HANDLER
extension SubmitVenueDetailsViewModal {
    
    
    func differentiateTags(){
        let tagArray = tags.value?.components(separatedBy: "#")
        var tagss = [String]()
        tagArray?.forEach({ (tags) in
            if /tags.replacingOccurrences(of: " ", with: "") != "" {
                tagss.append(tags)
            }
        })
        self.tagsArray.value = tagss.toJson()
    }
    
    func submitVenue(_ completion:@escaping (Bool)->()){
        differentiateTags()
        
        if venueImage.value != nil && venueImage.value != R.image.ic_add_img(){
            uploadImage { (status) in
                if status{
                    if self.documentUrl.value != nil || self.documentImage.value != nil{
                        self.uploadDocuments({ (completed) in
                            if completed  {
                                self.createVenue { (status) in
                                    if status{
                                        completion(true)
                                    }
                                }
                            }
                        })
                    }else{
                        self.createVenue { (status) in
                            if status{
                                completion(true)
                            }
                        }
                    }
                }
            }
        }else if self.documentUrl.value != nil || self.documentImage.value != nil{
            self.uploadDocuments({ (completed) in
                if completed  {
                    self.createVenue { (status) in
                        if status{
                            completion(true)
                        }
                    }
                }
            })
        }else{
            createVenue { (status) in
                if status{
                    completion(true)
                }
            }
        }
        
    }
    
    func uploadImage(_ completion:@escaping (Bool)->()){
        Loader.shared.start()
        S3.upload(image: venueImage.value , success: { (imageName) in
            print(imageName)
            self.venueImageStr.value = imageName
            completion(true)
        }) { (error) in
            print(error)
        }
    }
    
    func uploadDocuments(_ completion:@escaping (Bool)->()){
        if self.documentUrl.value != nil{
            Loader.shared.start()
            S3.upload(document: documentUrl.value, uploadProgress: { (val, _) in
                print(val)
            }, success: { (str, _, _) in
                print(str)
                self.uploadedDocUrl.value = str
                completion(true)
            }) { (str) in
                print("=== Error ==== " , str)
            }
        }else if self.documentImage.value != nil{
            Loader.shared.start()
            S3.upload(image: documentImage.value , success: { (imageName) in
                print(imageName)
                self.uploadedDocUrl.value = imageName
                completion(true)
            }) { (error) in
                print(error)
                
            }
        }
       
        
    }
    
    func createVenue(_ completion:@escaping (Bool)->()){
        
        if !(self.isValidTag(tagV: self.tags.value)){
            UtilityFunctions.makeToast(text: "Please add at least one hash tag", type: .error)
            return 
        }
        
        let participantIds =  selectedUsers.value.map({ (user) -> String in
            return /user.id
        })
        
        VenueTarget.createVenue(categoryId: /category.value?.id, venueImage:  self.venueImageStr.value , venueDoc: self.uploadedDocUrl.value , venueTitle: /venueTitle.value, venueLocationLat: /self.venueLocLat.value, userName: /userName.value , date: /self.dateMilli.value, venueLocName: /self.venueLocName.value, venueLocationLng: /self.venueLocLng.value , tag: self.tagsArray.value , privacy: self.access.value , locAddress: self.venueLocAddress.value , participantIds: participantIds.toJson() ).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                completion(true)
            }, onError: { (error) in
                print(error)
                completion(false)
                if let err = error as? ResponseStatus {
                    self.handleError(error: err)
                }
            })<bag
    }
    
    
}
