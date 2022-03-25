//
//  VenueTableViewCell.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//



import UIKit

enum RequestStatus : String{
    case pending = "PENDING"
    case rejected = "REJECTED"
    case none = "NONE"
}


class VenueTableViewCell: UITableViewCell {
    
    //MARK::- OUTLETS
    @IBOutlet weak var imgView: UIImageView!
    @IBOutlet weak var labelAddress: UILabel!
    @IBOutlet weak var labelVenueName: UILabel!
    @IBOutlet weak var labelMemberCount: UILabel!
    @IBOutlet weak var labelDistance: UILabel!
    @IBOutlet weak var imageIsPrivate: UIImageView!
    @IBOutlet weak var imageJoined: UIImageView!
    @IBOutlet weak var labelRequestPending: UILabel!
    
    //MARK::- PROPERTIES
    var section : Int?
    var venue : Any? {
        didSet {
            if let venueNearMe = venue as? Venues {
                labelVenueName?.text = /venueNearMe.venueTitle?.uppercaseFirst
                labelAddress.text = /venueNearMe.locName + ", " +  /venueNearMe.venueLocationAddress
                print(/venueNearMe.venueImageUrl?.original)
                imgView?.image(url:  /venueNearMe.venueImageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /venueNearMe.venueImageUrl?.original))
                let txt = (/venueNearMe.memberCount == 1) ? " Active Member" : " Active Members"
                labelMemberCount.text = /venueNearMe.memberCount?.toString + txt
                labelDistance.text = /venueNearMe.distance?.rounded(toPlaces: 2).toString + " Mi"
                imageIsPrivate.isHidden  = !(/venueNearMe.isPrivate)
                if /venueNearMe.adminId == /Singleton.sharedInstance.loggedInUser?.id{
                    imageJoined?.isHidden = false
                    imageJoined?.image(url:  /Singleton.sharedInstance.loggedInUser?.img?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /Singleton.sharedInstance.loggedInUser?.img?.thumbnail))
                }else{
                    imageJoined.image = R.image.ic_joined()
                }
                switch RequestStatus(rawValue: /venueNearMe.requestStatus) ?? .none{
                case .pending:
                    labelRequestPending.text = "PENDING"
                case .rejected , .none:
                    labelRequestPending.text = ""
                }
               
            }
        }
    }
    
    var isJoined: Bool?{
        didSet{
            imageJoined?.isHidden = !(/isJoined)
        }
    }
    
    
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}
