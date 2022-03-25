//
//  AppDelegate.swift
//  BrewEstate
//
//  Created by iReaper on 03/01/17.
//  Copyright © 2017 iReaper. All rights reserved.
//

import Foundation
import SwiftMessages

extension Theme {
    var title:String {
        switch self {
        case .error: return "Error"
        case .success: return "Success"
        case .warning: return "Warning"
        case .info: return "Alert"
        }
    }
}


class UtilityFunctions {
    
    
    static func show(nativeActionSheet title : String? , subTitle : String? , vc : UIViewController? , senders : [Any] , success : @escaping (Any,Int) -> ()){
        
        let alertController =  UIAlertController(title: nil, message: nil, preferredStyle: UIAlertController.Style.actionSheet)
        
        for (index,element) in senders.enumerated() {
            alertController.addAction(UIAlertAction(title: element as? String ?? "", style: UIAlertAction.Style.default , handler: { (action) in
                success(element, index)
            }))
            
            
        }
        alertController.addAction(UIAlertAction(title: "Cancel" , style: UIAlertAction.Style.destructive, handler: nil))
        
        vc?.present(alertController, animated: true, completion: nil)
        
    }
    
    static func showWithCancel(nativeActionSheet title : String? , subTitle : String? , vc : UIViewController? , senders : [Any] , success : @escaping (Any,Int) -> (), cancel : @escaping () -> ()){
        
        let alertController =  UIAlertController(title: nil, message: nil, preferredStyle: UIAlertController.Style.actionSheet)
        
        for (index,element) in senders.enumerated() {
            alertController.addAction(UIAlertAction(title: element as? String ?? "", style: UIAlertAction.Style.default , handler: { (action) in
                success(element, index)
            }))
            
            
        }
        
        alertController.addAction(UIAlertAction(title: "Cancel", style: UIAlertAction.Style.destructive , handler: { (action) in
            cancel()
        }))
        
        
        vc?.present(alertController, animated: true, completion: nil)
        
    }
    
    static func makeToast(text : String?,type : Theme){
        let view = MessageView.viewFromNib(layout: .cardView)
        
        // Theme message elements with the warning style.
        view.configureTheme(type)
        
        // Add a drop shadow.
        view.configureDropShadow()
        
        // Set message title, body, and icon. Here, we're overriding the default warning
        // image with an emoji character.
        view.button?.isHidden = true
        view.configureContent(title: type.title , body: /text, iconImage: nil, iconText: nil, buttonImage: nil, buttonTitle: "OK") { (button) in
            SwiftMessages.hide()
        }
        SwiftMessages.defaultConfig.presentationStyle = .top
        
        SwiftMessages.defaultConfig.presentationContext = .window(windowLevel: UIWindow.Level(rawValue: UIWindow.Level.statusBar.rawValue))
    
        // Disable the default auto-hiding behavior.
        SwiftMessages.defaultConfig.duration = .seconds(seconds: 5.0)
        
        // Dim the background like a popover view. Hide when the background is tapped.
        SwiftMessages.defaultConfig.dimMode = .gray(interactive: true)
        
        // Disable the interactive pan-to-hide gesture.
        SwiftMessages.defaultConfig.interactiveHide = true
        
        // Specify a status bar style to if the message is displayed directly under the status bar.
        SwiftMessages.defaultConfig.preferredStatusBarStyle = .lightContent
        // Show message with default config.
        SwiftMessages.show(view: view)
        
        // Customize config using the default as a base.
        var config = SwiftMessages.defaultConfig
        config.duration = .forever
        
        // Show the message.
        SwiftMessages.show(config: config, view: view)
    }
    
    static func showSingleButton(alert title:String , message:String  , buttonOk: @escaping () -> () , viewController: UIViewController , buttonText: String ){
        
        let alertController = UIAlertController(title: title, message: message , preferredStyle: UIAlertController.Style.alert)
        
        alertController.addAction(UIAlertAction(title: buttonText , style: UIAlertAction.Style.destructive, handler: {  (action) in
            buttonOk()
        }))
        viewController.present(alertController, animated: true, completion: nil)
    }
    
    static func show(alert title:String , message:String  , buttonOk: @escaping () -> () , viewController: UIViewController , buttonText: String ){
        
        let alertController = UIAlertController(title: title, message: message , preferredStyle: UIAlertController.Style.alert)
        alertController.addAction(UIAlertAction(title: "Cancel" , style: UIAlertAction.Style.cancel, handler: nil))
        alertController.addAction(UIAlertAction(title: buttonText , style: UIAlertAction.Style.destructive, handler: {  (action) in
            buttonOk()
        }))
        viewController.present(alertController, animated: true, completion: nil)
    }
    
    static func show(alert title:String , message:String  , buttonOk: @escaping () -> (), buttonCancel: @escaping () -> () , viewController: UIViewController , buttonText: String , cancelButtonText: String  ){
        
        let alertController = UIAlertController(title: title, message: message , preferredStyle: UIAlertController.Style.alert)
        alertController.addAction(UIAlertAction(title: buttonText , style: UIAlertAction.Style.destructive, handler: {  (action) in
            buttonOk()
        }))
        alertController.addAction(UIAlertAction(title: cancelButtonText , style: UIAlertAction.Style.cancel, handler: {  (action) in
            buttonCancel()
        }))
        viewController.present(alertController, animated: true, completion: nil)
    }
    
    static func checkNilData(arr: Array<Any>?, tableView: UITableView?, contentMode: UIView.ContentMode , msg : String) {
        
        if /arr?.count > 0 {
            tableView?.backgroundView = nil
        } else {
            
            let noDataLabel: UILabel     = UILabel(frame: CGRect(x: 0, y: 0, width: tableView?.bounds.size.width ?? 0.0, height: tableView?.bounds.size.height ?? 0.0))
            noDataLabel.text          = /msg
            noDataLabel.font = UIFont.systemFont(ofSize: 14, weight: .medium)
            noDataLabel.textColor     = UIColor.gray
            noDataLabel.textAlignment = .center
            tableView?.backgroundView  = noDataLabel
            tableView?.separatorStyle  = .none
        }
    }
    
    static func share(mssage: String? , url: String? , image: UIImage?){
        let text = mssage
         var shareAll : [Any] = [text ]
       
        if let img = image{
            shareAll.append(img)
        }
        if let validUrl = url{
            shareAll.append(validUrl)
        }
        let activityViewController = UIActivityViewController(activityItems: shareAll, applicationActivities: nil)
        activityViewController.popoverPresentationController?.sourceView = UIApplication.shared.topMostViewController()?.view
        UIApplication.shared.topMostViewController()?.present(activityViewController, animated: true, completion: nil)
    }
    
    static func retrieveMaps(lati: String , longi: String , userLat : String , userLong: String){
        
        let actionSheetController: UIAlertController = UIAlertController(title: "Select Map" , message: nil, preferredStyle: .actionSheet)
        let action1: UIAlertAction = UIAlertAction(title:  "Google Maps" , style: .default) { action -> Void in
            guard let googleMapPresent = URL(string: "comgooglemaps://") else {return}
            if (UIApplication.shared.canOpenURL(googleMapPresent)) {
                let googleMapsURLString = "http://maps.google.com/?saddr=" + userLat  + "," + userLong + "&daddr=" + lati + "," + longi
                guard let googleUrl = URL(string:googleMapsURLString) else { return }
                UIApplication.shared.openURL(googleUrl)
            } else {
                UtilityFunctions.makeToast(text: "Your phone does not have google maps installed", type: .error)
            }
            
        }
        let action2: UIAlertAction = UIAlertAction(title:  "Maps" , style: .default) { action -> Void in
            let urlToOpen = "http://maps.apple.com/maps?saddr=" + userLat  + "," + userLong + "&daddr=" + lati + "," + longi
            guard let urlToMap = URL(string: urlToOpen) else {return}
            UIApplication.shared.openURL(urlToMap)
            
        }
        actionSheetController.addAction(UIAlertAction(title: "Cancel" , style: UIAlertAction.Style.cancel, handler: nil))
        actionSheetController.addAction(action1)
        actionSheetController.addAction(action2)
        
        UIApplication.topViewController()?.present(actionSheetController, animated: true, completion: nil)
    }
    
}
