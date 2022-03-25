//
//  PlacePicker.swift
//  Connect
//
//  Created by OSX on 06/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//


import UIKit
import Foundation
import GooglePlacePicker

typealias location = (((lat:Double,long:Double,locationName:String)) ->())

class PlacePickerDataSource: NSObject, GMSPlacePickerViewControllerDelegate {
    
    var cancelled:(()->())?
    var locationSelected:location?
    var placePicker:GMSPlacePickerViewController?
    
    init(placePikcerVC : GMSPlacePickerViewController) {
        self.placePicker = placePikcerVC
    }
    
    func show(selectedLocation: @escaping location, cancelled: @escaping ()->()) {
        self.cancelled = cancelled
        self.locationSelected = selectedLocation
        UIApplication.topViewController()?.presentVC(self.placePicker!)
    }
    
    func placePicker(_ viewController: GMSPlacePickerViewController, didPick place: GMSPlace) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        
        print("Place name \(place.name)")
        print("Place address \(place.formattedAddress)")
        print("Place attributions \(place.attributions)")
        
        
        if let block = self.locationSelected {
            block((place.coordinate.latitude,place.coordinate.longitude,/place.name + " " + /place.formattedAddress))
        }
        
    }
    
    func placePickerDidCancel(_ viewController: GMSPlacePickerViewController) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        if let block = self.cancelled {
            block()
        }
        print("No place selected")
    }
    
}
