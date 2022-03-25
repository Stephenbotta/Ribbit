//
//  PeopleHeaderView.swift
//  Conversify
//
//  Created by Apple on 13/11/18.
//

import UIKit

class PeopleHeaderView: UIView {

    //MARK::- OUTLETS
    @IBOutlet weak var labelTime: UILabel!
    @IBOutlet weak var labelLocAddress: UILabel!
    @IBOutlet weak var labelLocName: UILabel!
    
    //MARK::- VARIABLES
    
    var people: PeopleData?{
        didSet{
            labelTime?.text = /Date(milliseconds: Double(/people?.timestamp)).toString(format: "MMM d, yyyy")
            labelLocAddress.text = /people?.locationAddress
            labelLocName.text = /people?.locationName
        }
    }
    
    
}
