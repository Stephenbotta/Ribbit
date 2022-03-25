//
//  GroupMemberCell.swift
//  Conversify
//
//  Created by Apple on 12/11/18.
//

import UIKit
import IBAnimatable

class GroupMemberTableViewCell: BaseTableViewCell {
    
    //MARK: - Properties
    @IBOutlet weak var imgProfilePic: AnimatableImageView!
    @IBOutlet weak var lblUserName: UILabel!
    @IBOutlet weak var lblAdmin: UILabel!
    @IBOutlet weak var imgIsPrivate: UIImageView!
    
    
    //MARK::- PROPERTY
    var userName : String?
    
    var members : Members?{
        didSet{
            userName = /members?.user?.id
            lblUserName?.text = /members?.user?.userName
            imgProfilePic?.image(url:  /members?.user?.img?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /members?.user?.img?.original))
            lblAdmin?.isHidden = !(/members?.isAdmin)
            imgIsPrivate?.isHidden = true
        }
    }
    
    var user: User?{
        didSet{
            userName = /user?.id
            imgIsPrivate?.isHidden = !(/user?.isAccountPrivate)
            lblUserName?.text = /user?.userName
            imgProfilePic?.image(url:  /user?.image?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//kf.setImage(with: URL(string: /user?.image?.original))
        }
    }
    
    override func bindings() {
        imgProfilePic.addTapGesture { [weak self] (gesture) in
            self?.leadToUserDetail()
        }
        
        lblUserName.addTapGesture { [weak self] (gesture) in
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
    
    //MARK: - View Hiererchy
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}
