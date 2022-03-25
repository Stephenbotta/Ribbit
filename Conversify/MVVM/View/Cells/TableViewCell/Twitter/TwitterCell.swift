//
//  TwitterCell.swift
//  Conversify
//
//  Created by Interns on 12/03/20.
//

import UIKit
import moa

class TwitterCell: UITableViewCell {

    //MARK: OUTLETS
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblPost: UILabel!
    @IBOutlet weak var lblFavCount: UIButton!
    @IBOutlet weak var userImage: UIImageView!
    @IBOutlet weak var lblScreenName: UILabel!
    @IBOutlet weak var lblCreatedAt: UILabel!
    @IBOutlet weak var btnShare: UIButton!
    @IBOutlet weak var btnComment: UIButton!
    @IBOutlet weak var btnRetweet: UIButton!
    
    var item: TwitterModelNew? {
        didSet {
            lblName.text = item?.name
            lblPost.text = item?.text
            lblFavCount.setTitle(" "+String(format: "%.0f" , item?.favorite_count ?? 0.0), for: .normal)
            btnRetweet.setTitle(" "+String(format: "%.0f" , item?.retweet_count ?? 0.0), for: .normal)
            userImage.image(url:  item?.profile_image_url ?? "", placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: item?.profile_image_url ?? ""))
            lblScreenName.text = "@"+(item?.screen_name ?? "")
            lblCreatedAt.text = item?.created_at
        }
    }
}
