//
//  CustomNavBar.swift
//  Connect
//
//  Created by OSX on 22/12/17.
//  Copyright © 2017 OSX. All rights reserved.
//


import UIKit
import Foundation
import IBAnimatable


class CustomNavBar: AnimatableView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        prepareView()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        prepareView()
    }
    
    func prepareView() {
        
        if let constraint = (self.constraints.filter{$0.firstAttribute == .height}.first) {
            constraint.constant = UIDevice.current.modelName.isEqual(GlobalConstants.iPHONE_X) ? 84.0 : 64.0
        }
        
    }
    
}
