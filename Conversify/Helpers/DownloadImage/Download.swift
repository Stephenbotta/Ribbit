//
//  Download.swift
//  Connect
//
//  Created by OSX on 04/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//


import UIKit
import moa
import Foundation


class Download: NSObject {
    
    static let shared = Download()
    
    func image(url:String?, imageView: UIImageView,loader: Bool? = true, placeholder: UIImage? = nil) {
        
       // imageView.moa.indicatorType = /loader ? .activity : .none
        imageView.image(url: /url,
                        placeholder: placeholder ?? #imageLiteral(resourceName: "ic_placeholder"))
        
    }
    
    
}
