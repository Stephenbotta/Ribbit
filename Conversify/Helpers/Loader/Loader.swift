//
//  Loader.swift
//  Connect
//
//  Created by OSX on 02/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//

import Foundation
import UIKit
import Lottie

enum LoaderTypeSelect : String{
    case map = "bouncy_mapmaker"
    case facebook = "abc"
}

class Loader: NSObject {
    
    static let shared = Loader()
    
    let viewTemp = UIView(frame: UIScreen.main.bounds)
    var animatedView: AnimationView = AnimationView(name: "preloader")
  
    
    func start(isLocation: Bool = false) {
        self.animatedView.frame = CGRect(x: 0, y: 0, width: 200, height: 200)
        guard let keyWindow = UIApplication.shared.keyWindow else { return }
        
        self.animatedView.center = self.viewTemp.center
        self.animatedView.contentMode = .scaleAspectFill
        self.animatedView.animationSpeed = 1.5
        self.animatedView.loopMode = .loop
        self.viewTemp.addSubview(self.animatedView)
        self.animatedView.play()
        
        keyWindow.addSubview(viewTemp)
        keyWindow.bringSubviewToFront(viewTemp)
        self.viewTemp.backgroundColor = UIColor.black.withAlphaComponent(0.54)
        
        self.viewTemp.isHidden = false
        
    }
    
    func stop() {
        
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + DispatchTimeInterval.milliseconds(1500) ) {
            self.animatedView.stop()
            self.viewTemp.isHidden = true
        }
        
    }
    
    func startSpecificLoader(){
        
    }
    
    
    
}

