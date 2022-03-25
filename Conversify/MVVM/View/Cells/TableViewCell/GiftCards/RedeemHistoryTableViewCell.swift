//
//  RedeemHistoryTableViewCell.swift
//  Conversify
//
//  Created by Sagar Kumar on 08/02/21.
//

import UIKit

class RedeemHistoryTableViewCell: UITableViewCell {

    @IBOutlet weak var brandImageView: UIImageView!
    @IBOutlet weak var brandNameLabel: UILabel!
    @IBOutlet weak var pointLabel: UILabel!
    
    var redeem: Redeem? {
        didSet {
            guard let redeem = redeem else { return }
            brandNameLabel.text = redeem.name
            pointLabel.text = "\(/redeem.point) points by \(/redeem.redeemType)"
            brandImageView.image = (/redeem.redeemType).lowercased() == "giftcard" ? #imageLiteral(resourceName: "giftCard") : #imageLiteral(resourceName: "donation")
        }
    }
}
