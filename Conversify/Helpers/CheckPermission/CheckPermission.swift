//
//  CheckPermission.swift
//  Kabootz
//
//  Created by Sierra 4 on 13/06/17.
//  Copyright © 2017 Sierra 4. All rights reserved.
//

import UIKit
import Photos
import Contacts
import Foundation
import AddressBook
import SystemConfiguration
import EZSwiftExtensions


enum PermissionType {
    
    case camera
    case photos
    case locationInUse
    case contacts
    case microphone
    
    var message:String {
        switch self {
        case .camera: return "for camera access"
        case .photos: return ""
        case .locationInUse: return ""
        case .contacts: return ""
        case .microphone: return ""
        }
    }
    
}


class CheckPermission {
    
    static let shared = CheckPermission()
    
    
    //MARK: - Check Permission
    func permission(_ For : PermissionType, openSettingsAlert: Bool? = true, requestAccess: Bool? = true, completion: @escaping (Bool) -> () ) {
        
        switch status(For: For) {
        case 1,2:
            
            self.alert(show: openSettingsAlert ?? true, permissionType: For)
            completion(false)
        default:
            
            switch For {
            case .camera:
                AVCaptureDevice.requestAccess(for: AVMediaType.video,
                                              completionHandler: { granted in
                                                completion(granted)
                })
                
            default: completion(true)
            }
        }
        
    }
    
    fileprivate func alert(show: Bool, permissionType: PermissionType) {
        if show {
            let actionSheetController = UIAlertController(title: "Permission Required", message: permissionType.message, preferredStyle: .alert)
            
            let cancelActionButton = UIAlertAction(title: "Cancel", style: .cancel) { action -> Void in
                print("Cancel")
            }
            actionSheetController.addAction(cancelActionButton)
            
            let openSettingActionButton = UIAlertAction(title: "Open Settings", style: .default, handler: { (action) in
                self.openAppSettings()
            })
            actionSheetController.addAction(openSettingActionButton)
            DispatchQueue.main.async {
                ez.topMostVC?.presentVC(actionSheetController)
            }
            
        }
    }
    
    //MARK: - Open App Settings
    func openAppSettings() {
        guard let settingsUrl = URL(string: UIApplication.openSettingsURLString) else { return }
        
        if UIApplication.shared.canOpenURL(settingsUrl) {
            
            if #available(iOS 10.0, *) {
                UIApplication.shared.open(settingsUrl, options: [:], completionHandler: { (success) in
                    print("Settings opened: \(success)")
                })
            } else {
                // Fallback on earlier versions
            }
        }
    }
    
    
    //MARK: - Check Status
    func status(For: PermissionType) -> Int {
        
        switch For {
        case .camera:
            return AVCaptureDevice.authorizationStatus(for: AVMediaType.video).rawValue
            
        case .contacts:
            return CNContactStore.authorizationStatus(for: .contacts).rawValue
            
        case .locationInUse:
            guard CLLocationManager.locationServicesEnabled() else { return 2 }
            return Int(CLLocationManager.authorizationStatus().rawValue)
            
        case .photos:
            return PHPhotoLibrary.authorizationStatus().rawValue
            
        case .microphone:
            let recordPermission = AVAudioSession.sharedInstance().recordPermission
            return Int(recordPermission.rawValue)
        
        }
        
    }
    
    
    //MARK: - Check Internet Connection
    func connectedToNetwork() -> Bool {
        
        var zeroAddress = sockaddr_in()
        zeroAddress.sin_len = UInt8(MemoryLayout<sockaddr_in>.size)
        zeroAddress.sin_family = sa_family_t(AF_INET)
        
        guard let defaultRouteReachability = withUnsafePointer(to: &zeroAddress, {
            $0.withMemoryRebound(to: sockaddr.self, capacity: 1) {
                SCNetworkReachabilityCreateWithAddress(nil, $0)
            }
        }) else {
            return false
        }
        
        var flags: SCNetworkReachabilityFlags = []
        if !SCNetworkReachabilityGetFlags(defaultRouteReachability, &flags) {
            return false
        }
        
        let isReachable = flags.contains(.reachable)
        let needsConnection = flags.contains(.connectionRequired)
        
        return (isReachable && !needsConnection)
    }
    
    
    
    
}
