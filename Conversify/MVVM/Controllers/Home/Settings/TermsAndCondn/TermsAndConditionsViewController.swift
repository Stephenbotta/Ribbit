//
//  TermsAndConditionsViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 15/01/19.
//

import UIKit
import WebKit

class TermsAndConditionsViewController: BaseRxViewController {

    
    //MARK::- OUTLETS
    
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var webView: WKWebView!
    @IBOutlet weak var labelHeader: UILabel!
    
    //MARK::- PROPERTIES
    var webUrls = ""
    var header = ""
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        labelHeader.text = header
        let url = URL(string: /webUrls)!
        webView.load(URLRequest(url: url))
        webView.allowsBackForwardNavigationGestures = true
        
    }
    
    override func bindings() {
        btnBack.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.popVC()
        })<bag
    }
 

}
