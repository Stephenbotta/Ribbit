//
//  FacebookManager.swift
//  ProSwingsUser
//
//  Created by cblmacmini on 4/27/16.
//  Copyright © 2016 Gagan. All rights reserved.
//

import UIKit
import FBSDKLoginKit

typealias FacebookCallback = (_ facebook : Facebook) -> ()
typealias FacebookCallbackError = (_ error : String) -> ()

class FacebookManager: NSObject {
    
    var viewController : UIViewController?
    let permissions = ["public_profile","email"]
    
    var facebookCallback : FacebookCallback?
    var facebookCallbackError: FacebookCallbackError?
    
    static let shared = FacebookManager()
    
    override init() {
        super.init()
    }
    
    
    func configureLoginManager(sender : UIViewController, success : @escaping FacebookCallback , failure: @escaping FacebookCallbackError ){
        facebookCallbackError = failure
        facebookCallback = success
        self.viewController = sender
        let loginManager = LoginManager()
        loginManager.logOut()
        loginManager.logIn(permissions: permissions, from: viewController) { [weak self] (result, error) in
            if let err = error {
                failure(err.localizedDescription)
            }else if (result?.isCancelled)! {
                failure("Cancelled")
                print("Cancelled")
            }else{
                self?.sendGraphRequest()
            }
        }
    }
    
    
    func sendGraphRequest(){
        
        GraphRequest(graphPath: "me", parameters: ["fields":"first_name,last_name,picture.type(large),email , gender , birthday , hometown"]).start { (connection, result, error) in
            print(result)
            if let err = error {
                if let errorBlock = self.facebookCallbackError{
//                     Alerts.shared.show(alert: .oops, message:  err.localizedDescription , type: .error)
                    errorBlock(err.localizedDescription)
                }
            }else if let block = self.facebookCallback {
                let fbProfile = Facebook(result: result as AnyObject?)
                block(fbProfile)
            }
        }
    }
}

class Facebook : NSObject {
    
    var fbId : String?
    var firstName : String?
    var lastName : String?
    var imageUrl : String?
    var email : String?
    
    
    init(result : AnyObject?) {
        super.init()
        guard let result = result as? [String: AnyObject] else { return }
        fbId = AccessToken.current?.userID
        firstName = result["first_name"] as? String
        lastName =  result["last_name"] as? String
        let width = Int(UIScreen.main.bounds.size.width)
        imageUrl = "https://graph.facebook.com/\(String(describing: AccessToken.current?.userID))".appending("/picture??width=" + "\(width)" + "&height=362")
        email = result["email"] as? String
        
    }
    
    override init() {
        super.init()
    }
}



