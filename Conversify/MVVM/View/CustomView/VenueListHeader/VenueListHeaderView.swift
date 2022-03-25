//
//  VenueListHeaderView.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 25/10/18.
//

import UIKit

protocol DelegateFilterSelected : class {
    func filterSelected()
}

class VenueListHeaderView: UITableViewHeaderFooterView {

    //MARK::- OUTLETS
    @IBOutlet weak var labelTitleName: UILabel!
    @IBOutlet weak var btnAction: UIButton!
    
    weak var delegate: DelegateFilterSelected?
    
    
    @IBAction func btnActionFilter(_ sender: UIButton) {
        delegate?.filterSelected()
        
    }
    
}
