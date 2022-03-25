//
//  GiftCardTableViewCell.swift
//  Conversify
//
//  Created by Sagar Kumar on 08/02/21.
//

import UIKit
import moa

class GiftCardTableViewCell: UITableViewCell {

    @IBOutlet weak var brandImageView: UIImageView!
    @IBOutlet weak var brandNameLabel: UILabel!
    @IBOutlet weak var rewardNameLabel: UILabel!
    @IBOutlet weak var priceNameLabel: UILabel!
    
    var brand: Brand? {
        didSet {
            guard let brand = brand else { return }
            let item = brand.items?.first
            brandNameLabel.text = item?.rewardName
            priceNameLabel.text = "\(/item?.minValue) - \(/item?.maxValue) \(/item?.currencyCode)"
            rewardNameLabel.text = brand.brandName
            brandImageView.image = nil
            if let images = brand.imageUrls?.values {
                brandImageView.image(url: "\(/Array(images).last)")
            }
        }
    }
}
