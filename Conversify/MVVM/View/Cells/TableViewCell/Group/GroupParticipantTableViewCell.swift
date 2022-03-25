//
//  GroupParticipantTableViewCell.swift
//  Conversify
//
//  Created by Harminder on 05/12/18.
//

import UIKit

class GroupParticipantTableViewCell: BaseTableViewCell {
    
    //MARK::- OUTLETS
    @IBOutlet weak var imageUser: UIImageView!
    @IBOutlet weak var labelName: UILabel!
    @IBOutlet weak var labelIsAdmin: UILabel?
    @IBOutlet weak var imageRadio: UIImageView!
    
    //MATK::- PROPERTIES
    var userName : String?
    var user: User?{
        didSet{
            userName = /user?.id
            labelName?.text = /user?.userName
            imageUser.image(url:  /user?.image?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /user?.image?.thumbnail))
        }
    }
    
    var members: Members?{
        didSet{
            userName = /members?.user?.id
            labelName?.text = /members?.user?.userName
            imageUser?.image(url:  /members?.user?.img?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /members?.user?.img?.original))
            labelIsAdmin?.isHidden = !(/members?.isAdmin)
        }
    }
    
    var mmembers: User?{
        didSet{
            userName = /mmembers?.id
            labelName?.text = /mmembers?.userName
            imageUser?.image(url:  /mmembers?.image?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /mmembers?.image?.thumbnail))
            labelName?.textColor = /mmembers?.isSelected ? #colorLiteral(red: 1, green: 0.8666666667, blue: 0.08235294118, alpha: 1)  : #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
            imageRadio?.image = /mmembers?.isSelected ?  R.image.ic_radio_button_color() : R.image.ic_radioOnButtonCopy()
        }
    }
    
    //MARK::- VIEW CYCLE
    
    override func bindings() {
        
        imageUser.addTapGesture { [weak self] (gesture) in
            self?.leadToUserDetail()
        }
        
        labelName.addTapGesture { [weak self] (gesture) in
            self?.leadToUserDetail()
            
        }
    }
    
    func leadToUserDetail(){
        self.endEditing(true)
        guard let vc = R.storyboard.home.profileViewController() else { return }
        vc.profileVM.userType = .otherUser
        vc.profileVM.userId = /userName
        UIApplication.topViewController()?.pushVC(vc)
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}
