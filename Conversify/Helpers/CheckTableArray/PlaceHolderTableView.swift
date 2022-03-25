////
////  TablePlaceHolder.swift
////  Bart
////
////  Created by OSX on 02/02/18.
////  Copyright © 2018 Bart. All rights reserved.
////
//
//
//import UIKit
//import Foundation
//import NVActivityIndicatorView
//
//enum TableState {
//    case loading
//    case noData(message: String?)
//    case noInternet
//    case apiError(errMessage: String?)
//    case none
//}
//
//
//class PlaceHolderTableView: UITableView {
//    
//    var tableState:TableState? = .none {
//        didSet {
//            self.setupBackgroundUI()
//        }
//    }
//    
//    override init(frame: CGRect, style: UITableViewStyle) {
//        super.init(frame: frame, style: style)
//        
//        prepareTable()
//    }
//    
//    public required init?(coder aDecoder: NSCoder) {
//        super.init(coder: aDecoder)
//        prepareTable()
//    }
//    
//    
//    fileprivate func prepareTable() {
//        self.tableState = .none
//    }
//    
//    fileprivate func setupBackgroundUI() {
//        guard let state = self.tableState else { return }
//        
//        switch state {
//        case .none: self.backgroundView = nil
//            
//        case .noInternet:
//            let placeHolderView:UIView = UIView(frame: self.bounds)
//            
////            let noInternetView:LOTAnimationView = LOTAnimationView(name: "no_internet_animation")
//            let noInternetView:UIImageView = UIImageView(image: R.image.no_internet())
//            noInternetView.frame = CGRect(x: 0, y: 0, width: 140.0, height: 140.0)
//            noInternetView.center = placeHolderView.center
//            noInternetView.contentMode = .scaleAspectFit
////            noInternetView.loopAnimation = false
////            noInternetView.play()
//            
//            let label:UILabel = UILabel()
//            label.text = AlertMessages.noInternet.value()
//            label.frame = CGRect(x: 0, y: 0, width: self.bounds.width - 16.0, height: 100.0)
//            label.numberOfLines = 0
//            label.center = CGPoint(x: placeHolderView.center.x, y: placeHolderView.center.y + 90.0)
//            label.textAlignment = .center
//            placeHolderView.addSubview(label)
//            placeHolderView.addSubview(noInternetView)
//            self.backgroundView = placeHolderView
//            
//        case .noData(let message):
//            let placeHolderView:UIView = UIView(frame: self.bounds)
//            let lblMessage:UILabel = UILabel()
//            lblMessage.frame = CGRect(x: 8.0, y: 0, width: self.bounds.width - 16.0, height: 30.0)
//            lblMessage.center = CGPoint(x: placeHolderView.center.x, y: placeHolderView.center.y + 15.0)
//            lblMessage.numberOfLines = 0
//            lblMessage.text = /message
//            lblMessage.textAlignment = .center
//            placeHolderView.addSubview(lblMessage)
//            self.backgroundView = placeHolderView
//            
//        case .apiError(let errMessage):
//            let placeHolderView:UIView = UIView(frame: self.bounds)
//            let lblMessage:UILabel = UILabel()
//            lblMessage.frame = CGRect(x: 8.0, y: 0, width: self.bounds.width - 16.0, height: 30.0)
//            lblMessage.center = CGPoint(x: placeHolderView.center.x, y: placeHolderView.center.y + 15.0)
//            lblMessage.numberOfLines = 0
//            lblMessage.text = /errMessage
//            lblMessage.textAlignment = .center
//            placeHolderView.addSubview(lblMessage)
//            self.backgroundView = placeHolderView
//            
//            
//        case .loading:
//            let placeHolderView:UIView = UIView(frame: self.bounds)
//            let loader = NVActivityIndicatorView(frame: CGRect(x: placeHolderView.center.x - 22, y: placeHolderView.center.y , w: 44, h: 44) , type: .ballSpinFadeLoader, color: Colors.yellow.color(), padding: nil)
//            placeHolderView.addSubview(loader)
//            loader.startAnimating()
//            self.backgroundView = placeHolderView
//            
//        }
//        
//        
//    }
//    
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
