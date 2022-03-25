//
//  MapViewModal.swift
//  Conversify
//
//  Created by Apple on 27/10/18.
//

import Foundation
import GoogleMaps
import GooglePlaces

class MapClusteringModel: NSObject , GMUClusterItem {
    
    var view: UIView!
    var icon: UIImage!
    var position: CLLocationCoordinate2D
    
    init(position: CLLocationCoordinate2D, icon: UIImage , view : UIView) {
        self.position = position
        self.icon = icon
        self.view = view
    }
}



