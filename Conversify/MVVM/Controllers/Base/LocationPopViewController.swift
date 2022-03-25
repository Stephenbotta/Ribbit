//
//  LocationPopViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/11/18.
//

import UIKit

class LocationPopViewController: UIViewController {
    
    //MARK::- PROPERTIES
    var back:((Bool)->())?

    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    
    //MARK::- ACTIONS
    @IBAction func btnActionGotIt(_ sender: UIButton) {
        self.dismiss(animated: true) { [ weak self] in
            self?.back?(true)
        }
    }
    
    @IBAction func btnActionCancel(_ sender: UIButton) {
        self.dismiss(animated: true) { [ weak self] in
            self?.back?(false)
        }
    }
}
