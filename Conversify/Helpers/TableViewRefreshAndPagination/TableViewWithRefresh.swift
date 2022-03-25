//
//  TableViewWithRefresh.swift
//  Connect
//
//  Created by OSX on 10/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//


import UIKit
import Foundation
import EZSwiftExtensions


//class TableViewWithRefresh: PlaceHolderTableView {
//    
//    var refreshHeader:(()->())?
//    var scrollFooter:(()->())?
//    var footer:DefaultRefreshFooter?
//    var isFooterAdded:Bool = false {
//        didSet {
////            self.addFooter(isFooterAdded)
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
//    fileprivate func prepareTable() {
//        
//        self.refreshControl = UIRefreshControl(frame: CGRect(x: self.frame.x, y: self.frame.x, w: self.frame.width, h: 20.0))
//        self.refreshControl?.backgroundColor = UIColor.white
//        self.refreshControl?.tintColor = Colors.yellow.color()
//        self.refreshControl?.addTarget(self, action: #selector(refreshHeaderAction), for: .valueChanged)
//        
//        self.isFooterAdded = true
//    }
//    
//    @objc func refreshHeaderAction() {
//        
//        self.refreshHeader?()
//        
//        ez.runThisAfterDelay(seconds: 1.5) { [weak self] in
//            self?.refreshControl?.endRefreshing()
//        }
//    }
//    
////    fileprivate func addFooter(_ bool: Bool) {
////        
////        if bool {
////            footer = DefaultRefreshFooter.footer()
////            footer?.tintColor = Colors.yellow.color()
////            footer?.refreshMode = .scroll
////            footer?.setText("Loading...", mode: .refreshing)
////            footer?.setText(" ", mode: .pullToRefresh)
////            footer?.setText(" ", mode: .tapToRefresh)
////            footer?.setText(" ", mode: .scrollAndTapToRefresh)
////            
////            self.configRefreshFooter(with: footer ?? DefaultRefreshFooter.footer(), container: nil) { [weak self] in
////                
////                self?.scrollFooter?()
////                
////                ez.runThisAfterDelay(seconds: 1.5) {
////                    self?.switchRefreshFooter(to: .normal)
////                }
////            }
////        } else {
////            if let ft = footer {
////                ft.removeFromSuperview()
////            }
////            
////        }
////        
////    }
//    
//}
