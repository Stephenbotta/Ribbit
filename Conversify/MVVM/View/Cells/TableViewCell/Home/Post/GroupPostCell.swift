//
//  GroupPostCell.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import UIKit

class GroupPostCell: UITableViewCell {

    //MARK::- OUTLETS
    @IBOutlet weak var imgGroupView: UIImageView!
    @IBOutlet weak var lblGroupName: UILabel!
 
    var item : GroupList? {
        didSet {
            lblGroupName.text = item?.groupName?.capitalizedFirst()
            imgGroupView.image(url:  /item?.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /item?.imageUrl?.thumbnail))
        }
    }
}
