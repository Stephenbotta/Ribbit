//
//  AddParticipantTableViewCell.swift
//  Conversify
//
//  Created by Harminder on 05/12/18.
//

import UIKit
import IBAnimatable

class AddParticipantTableViewCell: UITableViewCell {
    
    //MARK:- OUTLETS
    
    @IBOutlet weak var labelInterests: UILabel!
    @IBOutlet weak var imagePeople: AnimatableImageView!
    @IBOutlet weak var labelName: UILabel!
    @IBOutlet weak var imageSelected: UIImageView!
    
    //MARK::- PROPERTIES
    
    var user: User?{
        didSet{
            labelName?.text = /user?.userName
            let interests = user?.intersts?.map({ (interest) -> String in
                return /interest.category
            })
            labelInterests.text = interests?.joined(separator: " , ")
            imagePeople.image(url:  /user?.image?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /user?.image?.thumbnail))
            imageSelected.isHidden = !(/user?.selectedForGroup)
            
        }
    }
    //MARK::- VIEW CYC LE

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
