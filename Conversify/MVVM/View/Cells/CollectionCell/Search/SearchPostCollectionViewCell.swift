//
//  SearchPostCollectionViewCell.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 04/01/19.
//

import UIKit

class SearchPostCollectionViewCell: UICollectionViewCell {
    
    @IBOutlet weak var image: UIImageView!
    
    var post: PostList?{
        didSet{
            image?.image(url:  /post?.media?.first?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /post?.media?.first?.original))
        }
    }
    
}
