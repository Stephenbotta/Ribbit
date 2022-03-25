//
//  GroupLocationCell.swift
//  Conversify
//
//  Created by Apple on 12/11/18.
//

import UIKit

protocol DelegateAddParticpant : class {
    func addParticipant()
}

class GroupLocationTableViewCell: UITableViewCell {

    //MARK: - Outlets
    @IBOutlet weak var lblLocationName: UILabel!
    @IBOutlet weak var lblLocationAddress: UILabel!
    @IBOutlet weak var btnAddParticipant: UIButton!
    @IBOutlet weak var labelNumberOfMembers: UILabel!
    
    //MARK::- PROPERTIES
    weak var delegate: DelegateAddParticpant?
    var lat: String?
    var long: String?
    
    //MARK: - View Hiererchy
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    //MARK: - Button Actions
    
    @IBAction func btnActionAddParticipant(_ sender: UIButton) {
        delegate?.addParticipant()
    }
    
    @IBAction func directions(_ sender: Any) {
        if /lat == "" && /long == ""{
            return
        }
        UtilityFunctions.retrieveMaps(lati: /lat , longi: /long , userLat: /LocationManager.sharedInstance.currentLocation?.currentLat, userLong: /LocationManager.sharedInstance.currentLocation?.currentLng)
    }
    
}
