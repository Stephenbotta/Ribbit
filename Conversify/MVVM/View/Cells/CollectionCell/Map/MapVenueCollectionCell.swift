//
//  MapVenueCollectionCell.swift
//  Conversify
//
//  Created by Apple on 27/10/18.
//

import UIKit

class MapVenueCollectionCell: UICollectionViewCell {
    
    //MARK::- Outlets
    @IBOutlet weak var imgView: UIImageView!
    @IBOutlet weak var labelVenueName: UILabel!
    @IBOutlet weak var labelDistance: UILabel!
    @IBOutlet weak var labelMemberCount: UILabel!
    @IBOutlet weak var labelAddress: UILabel!
    
    
    var venue : Venues? {
        didSet{
            labelVenueName.text = venue?.venueTitle
            labelAddress.text = venue?.locName
            imgView?.image(url:  /venue?.venueImageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /venue?.venueImageUrl?.original))
            let txt = (/venue?.memberCount == 1) ? " Active Member" : " Active Members"
            labelMemberCount.text = /venue?.memberCount?.toString + txt
            
            labelDistance.text = /venue?.distance?.rounded(toPlaces: 2).toString + " Mi"
        }
    }
}
