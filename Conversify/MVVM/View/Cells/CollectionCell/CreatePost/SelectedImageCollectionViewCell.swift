//
//  SelectedImageCollectionViewCell.swift
//  Conversify
//
//  Created by Gurleen on 30/07/19.
//

import UIKit
import Photos
import VersaPlayer
import SDWebImage

class SelectedImageCollectionViewCell: UICollectionViewCell {
    
    //MARK::- OUTLETS
    @IBOutlet weak var imageSelected: UIImageView?
    @IBOutlet weak var imageCross: UIImageView?
    @IBOutlet weak var viewGradient: VersaPlayerView?
    @IBOutlet weak var imageVideoPlay: UIImageView?
    @IBOutlet weak var imageMostLiked: UIImageView?
    
    @IBOutlet var btnPlayVideo: UIButton?
    
    //   @IBOutlet weak var playerView: VersaPlayerView!
    @IBOutlet weak var controls: VersaPlayerControls!

    //MARK::- PROPERTIES
    
    override class func awakeFromNib() {
        
    }
    override func awakeFromNib() {
        imageSelected?.layer.cornerRadius = 4
    }
    var asset: PHAsset? {
        didSet {
            guard let asset = asset else { return }
            imageSelected?.image = imageSelected?.getAssetThumbnail(asset: asset)
            //viewGradient?.isHidden = asset.mediaType == PHAssetMediaType.image
            imageVideoPlay?.isHidden = asset.mediaType == PHAssetMediaType.image
        }
    }
    var selectedIndex = -1
    var tableRow : Int?
    var collectionIndex: Int?
    
    var isFromPost : Bool? = false
    
    var media: Media? {
        didSet{
            guard let media = media else { return }
            imageCross?.isHidden = true
            //imageSelected?.kf.setImage(with: URL(string: /media.thumbnail))
            
            imageSelected?.image(url:  /media.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))
            viewGradient?.isHidden = media.mediaType != "VIDEO"
            imageVideoPlay?.isHidden = media.mediaType != "VIDEO"
            btnPlayVideo?.isHidden = media.mediaType != "VIDEO"
            imageMostLiked?.isHidden = !(/media.isMostLiked)
            controls?.isHidden = media.mediaType != "VIDEO"
            viewGradient?.use(controls: controls)

            if /isFromPost {
                controls?.isHidden = true
                return
            }
            if /selectedIndex == /collectionIndex {
                viewGradient?.isHidden = false
                imageVideoPlay?.isHidden = true
                btnPlayVideo?.isHidden = true
                if media.mediaType == "VIDEO" {
                    if let url = URL(string: /media.videoUrl) {
                        let item = VersaPlayerItem(url: url)
                        self.viewGradient?.set(item: item)
                        self.viewGradient?.play()
                    }
                }
            } else {
                viewGradient?.isHidden = true
                self.viewGradient?.pause()
            }
        }
    }
}
