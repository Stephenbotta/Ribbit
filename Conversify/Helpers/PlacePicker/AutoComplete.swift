//  AutoComplete.swift
//  Connect
//
//  Created by OSX on 13/02/18.
//  Copyright © 2018 OSX. All rights reserved.
//


import UIKit
import Foundation
import GooglePlacePicker


class AutoComplete: NSObject, GMSAutocompleteViewControllerDelegate {
    
    var cancelled:(()->())?
    var locationSelected:location?
    var autoCompleteVC: GMSAutocompleteViewController?
    
    
    init(autoCompleteVC: GMSAutocompleteViewController) {
        self.autoCompleteVC = autoCompleteVC
        
        let filter = GMSAutocompleteFilter()
        filter.type = .city
        self.autoCompleteVC?.autocompleteFilter = filter
    }
    
    func show(selectedLocation: @escaping location, cancelled: @escaping ()->()) {
        
        self.cancelled = cancelled
        self.locationSelected = selectedLocation
        UIApplication.topViewController()?.presentVC(self.autoCompleteVC!)
    }
    
    // Handle the user's selection.
    func viewController(_ viewController: GMSAutocompleteViewController, didAutocompleteWith place: GMSPlace) {
        //print("Place name: \(place.name)")
        //print("Place address: \(place.formattedAddress)")
        //print("Place attributions: \(place.attributions)")
        
        let address:String = (/place.formattedAddress).replacingOccurrences(of: /place.name, with: "")
        let locationName:String = (/place.name + " " + address).trimmingCharacters(in: .whitespaces)
        
        if let block = self.locationSelected {
            block((place.coordinate.latitude,place.coordinate.longitude,locationName))
        }
        viewController.dismiss(animated: true, completion: nil)
    }
    
    func viewController(_ viewController: GMSAutocompleteViewController, didFailAutocompleteWithError error: Error) {
        // TODO: handle the error.
        print("Error: ", error.localizedDescription)
    }
    
    // User canceled the operation.
    func wasCancelled(_ viewController: GMSAutocompleteViewController) {
        if let block = self.cancelled {
            block()
        }
        
        viewController.dismiss(animated: true, completion: nil)
    }
    
    // Turn the network activity indicator on and off again.
    func didRequestAutocompletePredictions(_ viewController: GMSAutocompleteViewController) {
        UIApplication.shared.isNetworkActivityIndicatorVisible = true
    }
    
    func didUpdateAutocompletePredictions(_ viewController: GMSAutocompleteViewController) {
        UIApplication.shared.isNetworkActivityIndicatorVisible = false
    }

    
}
