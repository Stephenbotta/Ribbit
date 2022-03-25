//
//  ContactUsViewController.swift
//  Conversify
//
//  Created by Interns on 18/03/20.
//

import UIKit
import WebKit
import IQKeyboardManagerSwift
import RxSwift
import RxCocoa

class ContactUsViewController: UIViewController {

    //MARK: OUTLETS
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var labelHeader: UILabel!
    @IBOutlet weak var textViewQuery: IQTextView!
    
    //MARK: PROPERTIES
    var header = ""
    var ApiModel = FollowersViewModal()
    
    //MARK: VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        labelHeader.text = header
    }
    
    //MARK: ACTIONS
    @IBAction func btnGoBack(_ sender: UIButton) {
        self.popVC()
    }
    
    @IBAction func sendQuery(_ sender: Any) {
        let queryText = textViewQuery.text
        if queryText?.count != 0 {
            ApiModel.queryOfUser = queryText ?? ""
            ApiModel.submitQuery()
            textViewQuery.text = nil
            UtilityFunctions.makeToast(text: "Message sent successfully", type: .success)
            self.popVC()
        }
        else {
            UtilityFunctions.makeToast(text: "Please enter your query", type: .warning)
        }
    
}
}
