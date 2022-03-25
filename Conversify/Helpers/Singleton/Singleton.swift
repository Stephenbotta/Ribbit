//  Signleton.swift
//  Connect
//
//  Created by OSX on 02/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//


import RMMapper
import Foundation


class Singleton {
    
    private init() { }
    
    //MARK: Shared Instance    
    static let sharedInstance: Singleton = Singleton()
    
    var isChatOn = false
    var conversationId = ""
    var isTopIsChatController = false
    var interests : [Interests]?{
        get{
            var user : [Interests]?
            if let data = UserDefaults.standard.rm_customObject(forKey: "ConversifyInterests") as? [Interests]{
                user = data
            }
            return user
        }
        set{
            let defaults = UserDefaults.standard
            if let value = newValue{
                defaults.rm_setCustomObject(value, forKey: "ConversifyInterests")
            }
            else{
                defaults.removeObject(forKey: "ConversifyInterests")
            }
        }
    }
    
    var selectedInterests: [Interests]?{
        get{
            var user : [Interests]?
            if let data = UserDefaults.standard.rm_customObject(forKey: "ConversifySelectedInterests") as? [Interests]{
                user = data
            }
            return user
        }
        set{
            let defaults = UserDefaults.standard
            if let value = newValue{
                defaults.rm_setCustomObject(value, forKey: "ConversifySelectedInterests")
            }
            else{
                defaults.removeObject(forKey: "ConversifySelectedInterests")
            }
        }
    }
    
    var loggedInUser : User?{
        get{
            var user : User?
            if let data = UserDefaults.standard.rm_customObject(forKey: "profile") as? User{
                user = data
            }
            return user
        }
        set{
            let defaults = UserDefaults.standard
            if let value = newValue{
                defaults.rm_setCustomObject(value, forKey: "profile")
            }
            else{
                defaults.removeObject(forKey: "profile")
            }
        }
    }
    
    var loggedInUserDeviceToken: String? {
        get{
            guard let userData = UserDefaults.standard.data(forKey: "ConversifyDeviceToken" ) else { return "djfgjdgsfjgdjsgfj" }
            return (NSKeyedUnarchiver.unarchiveObject(with: userData) as? String)
        }
        set{
            if let value = newValue{
                let val = NSKeyedArchiver.archivedData(withRootObject: value)
                UserDefaults.standard.set(val, forKey: "ConversifyDeviceToken")
            }else{
                UserDefaults.standard.set(nil, forKey: "ConversifyDeviceToken")
            }
        }
    }
    
    var apnsDeviceToken: String? {
           get{
               guard let userData = UserDefaults.standard.data(forKey: "ConversifyApnsDeviceToken" ) else { return "dgfjgdsjfgjdsgj" }
               return (NSKeyedUnarchiver.unarchiveObject(with: userData) as? String)
           }
           set{
               if let value = newValue{
                   let val = NSKeyedArchiver.archivedData(withRootObject: value)
                   UserDefaults.standard.set(val, forKey: "ConversifyApnsDeviceToken")
               }else{
                   UserDefaults.standard.set(nil, forKey: "ConversifyApnsDeviceToken")
               }
           }
       }
    
    func takeScreenshot(view: UIView) -> UIImage? {
        
        UIGraphicsBeginImageContext(view.frame.size)
        
        view.layer.render(in: UIGraphicsGetCurrentContext()!)
        
        let image = UIGraphicsGetImageFromCurrentImageContext()
        
        UIGraphicsEndImageContext()
        return image
    }
    
  
}















