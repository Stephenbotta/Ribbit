//
//  ViewStoryCVC.swift
//  Conversify
//
//  Created by admin on 06/04/21.
//

import UIKit

class ViewStoryCVC: UICollectionViewCell {
    @IBOutlet weak var imgStoryVIew: UIImageView!
    @IBOutlet weak var btnplayVideo: UIButton!
    @IBOutlet weak var lbltime: UILabel!
    override func awakeFromNib() {
        imgStoryVIew.layer.cornerRadius = 8
        btnplayVideo.isHidden = true
    }
   
}

