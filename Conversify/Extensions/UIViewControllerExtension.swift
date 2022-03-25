//
//  UIViewControllerExtension.swift
//  Connect
//
//  Created by OSX on 22/12/17.
//  Copyright © 2017 OSX. All rights reserved.
//

import UIKit
import Foundation
import EZSwiftExtensions
import SystemConfiguration
import GooglePlacePicker
import RxCocoa
import RxSwift

extension UIViewController {
    func topMostViewController() -> UIViewController {
        if self.presentedViewController == nil {
            return self
        }
        if let navigation = self.presentedViewController as? UINavigationController {
            return (navigation.visibleViewController?.topMostViewController())!
        }
        if let tab = self.presentedViewController as? UITabBarController {
            if let selectedTab = tab.selectedViewController {
                return selectedTab.topMostViewController()
            }
            return tab.topMostViewController()
        }
        return self.presentedViewController!.topMostViewController()
    }
    
    /** Open Url **/
    func openUrl(_ str: String) {
        
        guard let url = URL(string: str) else { return }
        UIApplication.shared.open(url, options: [:]) { (bool) in
            
        }
        
    }
}

extension UIApplication {
        
    func topMostViewController() -> UIViewController? {
        return self.keyWindow?.rootViewController?.topMostViewController()
    }
    
    
    func logoutActions(){
        Singleton.sharedInstance.loggedInUser = nil
        SocketIOManager.shared.disconnect()
        guard let delegate = UIApplication.shared.delegate as? AppDelegate else{return}
        
        let navigationControl = delegate.window?.rootViewController as? UINavigationController
        navigationControl?.viewControllers = []
        
        guard let welcomeVC = R.storyboard.main().instantiateInitialViewController() else { return }
        
        UIView.transition(with: delegate.window!, duration: 0.5, options: .transitionCrossDissolve , animations: { () -> Void in
            delegate.window?.rootViewController = welcomeVC
        }) { (completed) -> Void in
        }
    }
    
    
    
    func loginExpired() {
        UIApplication.shared.logoutActions()
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + DispatchTimeInterval.seconds(2)) {
            UtilityFunctions.makeToast(text: AlertMessages.loginExpired.value(), type: .info)
        }
        
    }
}

extension UIView {
    class func fromNib<T: UIView>() -> T {
        return Bundle.main.loadNibNamed(String(describing: T.self), owner: nil, options: nil)![0] as! T
    }
}
extension UIView {
    
    public func performSpringAnimation(completion: ((Bool) -> Swift.Void)? = nil) {
        
        UIView.animate(withDuration: 0.7, delay: 0, usingSpringWithDamping: 0.5, initialSpringVelocity: 1, options: .curveEaseOut, animations: { () -> Void in
            
            let bounceAnimation = CAKeyframeAnimation(keyPath: Constants.scale)
            bounceAnimation.values = [1.0 ,1.4, 0.9, 1.15, 0.95, 1.02, 1.0]
            bounceAnimation.duration = TimeInterval(0.5)
            bounceAnimation.calculationMode = CAAnimationCalculationMode.linear
            
            self.layer.add(bounceAnimation, forKey: nil)
            
        }, completion: { (complete) in
            
            Timer.runThisAfterDelay(seconds: 0.5, after: {
                completion?(complete)
            })
        })
        
        
    }
    
    func roundCorners(_ corners: UIRectCorner, radius: CGFloat) {
        let path = UIBezierPath(roundedRect: self.bounds, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        let mask = CAShapeLayer()
        mask.path = path.cgPath
        self.layer.masksToBounds = true
        self.layer.mask = mask
    }
    
}
public class Constants {
    static let scale = "transform.scale"
}
