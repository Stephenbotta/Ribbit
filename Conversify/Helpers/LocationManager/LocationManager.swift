//
//  RKLocationManager.swift
//  InPerson
//
//  Created by CB Macmini_3 on 17/12/15.
//  Copyright © 2015 Rico. All rights reserved.
//

import Foundation
import CoreLocation

struct Location {
    var name: String?
    var currentLat : String?
    var currentLng : String?
    var currentFormattedAddr : String?
    var currentCity : String?
}

struct DeviceTime {
    
    var currentTime : String {
        get{
            return  localTimeFormatter.string(from: Date())
        }
    }
    
    var currentDate : String{
        get{
            return localDateFormatter.string(from: Date())
        }
    }
    
    
    func convertCreatedAt (_ createdAt : String?) -> Date?{
        return localDateTimeFormatter.date(from: createdAt ?? "")
    }
    
    
    var localTimeZoneFormatter = DateFormatter()
    var localTimeFormatter = DateFormatter()
    var localDateFormatter = DateFormatter()
    
    var localDateTimeFormatter = DateFormatter()
    
    func setUpTimeFormatters(){
        localTimeFormatter.dateFormat = "h:mm a"
        localDateFormatter.dateFormat = "MMM dd yyyy"
        localDateTimeFormatter.dateFormat = "MMM dd yyyy h:mm a Z"
    }
}

class LocationManager: NSObject, CLLocationManagerDelegate {
    
    var locationManager = CLLocationManager()
    var bookingId : String?
    var currentLocation : Location? = Location()
    var currentTime : DeviceTime? = DeviceTime()
    static let sharedInstance: LocationManager = {
        var token = 0
        let instance = LocationManager()
        return instance
    }()
    
    override init() {
        super.init()
        self.locationManager = CLLocationManager()
    }
    
    var isReachable : Bool = true
    
    var isLocationServicesEnabled : Bool {
        get {
            return CLLocationManager.locationServicesEnabled() && CLLocationManager.authorizationStatus() == .denied
        }
    }
    
    var timer : Timer?
    
    
    func startTrackingUser(){
        
        currentTime?.setUpTimeFormatters()
        // Ask for Authorisation from the User.
        self.locationManager.requestWhenInUseAuthorization()
        // For use in foreground
        //        self.locationManager.requestWhenInUseAuthorization()
        if CLLocationManager.locationServicesEnabled() {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
            locationManager.startUpdatingLocation()
        }else{
            
        }
    }
    
    @objc func update() {
        if let _ = Singleton.sharedInstance.loggedInUser?.token{
            var currenLocation = [String:String]()
            currenLocation["locationName"] = /self.currentLocation?.name
            currenLocation["locationAddress"] = /self.currentLocation?.currentFormattedAddr
            currenLocation["locationLat"] = /self.currentLocation?.currentLat
            currenLocation["locationLong"] = /self.currentLocation?.currentLng
            currenLocation["userId"] = /Singleton.sharedInstance.loggedInUser?.id
//            SocketIOManager.shared.sendCurrentLocation(data: currenLocation) { (status) in
//                print(status)
//            }
        }else{
            bookingId = nil
            timer?.invalidate()
            timer = nil
        }
        
    }
    
    
    func startSharingLocation(){
        
        SocketIOManager.shared.addHandlers()
        
        timer = Timer.scheduledTimer(timeInterval: 5 , target: self, selector: #selector(update), userInfo: nil, repeats: true)
    }
    
    func stopSharingLocation (id : String?){
        
        guard let _ = bookingId , /id == /bookingId else{
            return
        }
        bookingId = nil
        timer?.invalidate()
        timer = nil
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print(error.localizedDescription)
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        let locValue = (manager.location?.coordinate) ?? CLLocationCoordinate2D()
        self.currentLocation?.currentLat = "\(locValue.latitude)"
        self.currentLocation?.currentLng = "\(locValue.longitude)"
        
        guard let location = locations.last else{
            return
        }
        self.currentLocation?.currentLat = "\(location.coordinate.latitude)"
        self.currentLocation?.currentLng = "\(location.coordinate.longitude)"
        
        CLGeocoder().reverseGeocodeLocation(location) { (placemarks, error) -> Void in
            if let mark = placemarks?.last , let addrList = mark.addressDictionary {
                self.currentLocation?.name = /mark.name
                self.formatAddress(addrList)
            }
        }
        
    }
    
    
    fileprivate func formatAddress (_ parameters : [AnyHashable: Any]){
        
        self.currentLocation?.currentFormattedAddr = (parameters["FormattedAddressLines"] as? [String])?.joined(separator: ", ")
        
        guard let city = parameters["City"] as? String else{
            if let city = parameters["SubAdministrativeArea"] as? String{
                self.currentLocation?.currentCity = city
            }
            else if let city = parameters["State"] as? String{
                self.currentLocation?.currentCity = city
            }
            else if let city = parameters["Country"] as? String{
                self.currentLocation?.currentCity = city
            }
            return
        }
        self.currentLocation?.currentCity = city
        
    }
}
