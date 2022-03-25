//
//  TwitterImageTVC.swift
//  Conversify
//
//  Created by Apple on 22/05/20.
//

import UIKit
import moa


class TwitterImageTVC: UITableViewCell {
    @IBOutlet weak var postImageVw: UIImageView!
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
            userImage.image(url:  item?.profile_image_url ?? "", placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: item?.profile_image_url ?? ""))
            lblScreenName.text = "@"+(item?.screen_name ?? "")
            lblCreatedAt.text = item?.created_at
            postImageVw.image(url:  item?.media?[0].media_url_https ?? "", placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: item?.media?[0].media_url_https ?? ""))
        }
    }

}
