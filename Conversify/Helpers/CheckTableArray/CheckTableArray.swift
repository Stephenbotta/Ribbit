//
//  CheckTableArray.swift
//  Connect
//
//  Created by OSX on 29/12/17.
//  Copyright © 2017 OSX. All rights reserved.
//


import UIKit
import Foundation


class Table {
    
    static let shared = Table()
    
    func checkNilData(arr: [Any]?, tableView: UITableView?, placeHolderImage:UIImage?, message:String? = nil, btnTitle: String? = nil, btnHide: Bool? = false ,pressRetry: @escaping (() -> Void)) {
        
        switch /(arr?.count) > 0 {
        case true:
            tableView?.backgroundView = nil
        default:
            if tableView?.backgroundView == nil {
                
                let placeHolder:TablePlaceHolder = TablePlaceHolder(frame: (tableView?.bounds)!)
                if let pdImage = placeHolderImage {
                    placeHolder.placeHolderImage.image = pdImage
                } else {
                    placeHolder.placeHolderImage.image = nil
                }
                
                if let msg = message {
                    placeHolder.lblMessage.text = msg
                } else {
                    placeHolder.lblMessage.text = ""
                }
                
                if let hide = btnHide {
                    placeHolder.btnPlaceHolderAction.isHidden = hide
                }
                
                if let btnTitle = btnTitle {
                    DispatchQueue.main.async {
                        placeHolder.btnPlaceHolderAction.setTitle(btnTitle, for: .normal)
                    }
                }
                
                placeHolder.buttonAction = {
                    pressRetry()
                }
                tableView?.backgroundView = placeHolder
                UIView.animate(withDuration: 0.8, delay: 0, options: .curveEaseInOut, animations: {
                    placeHolder.alpha = 1.0
                }, completion: { _ in })
            }
        }
    }
    
    
}
