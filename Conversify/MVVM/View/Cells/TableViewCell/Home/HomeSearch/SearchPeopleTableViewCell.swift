//
//  SearchPeopleCell.swift
//  Conversify
//
//  Created by Apple on 13/11/18.
//

import UIKit
import Tags
import IBAnimatable
import RxSwift
import EZSwiftExtensions

protocol DelegateUpdateConvoId: class {
    func updateConvoId(row: Int, convoId: String)
}

class SearchPeopleTableViewCell: BaseTableViewCell {
    
    //MARK: - Outlets
    @IBOutlet weak var lblUserName: UILabel!
    @IBOutlet weak var lblDesignation: UILabel!
    @IBOutlet weak var imgProfilePic: AnimatableImageView!
    @IBOutlet weak var viewTags: TagsView!
    
    //MARK: - View Hierarchy
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        viewTags.tagFont = UIFont.systemFont(ofSize: 14)
    }
    
    var row : Int?
    var item : UserList?{
        didSet {
            imgProfilePic.image(url:  /item?.imageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /item?.imageUrl?.original))
            viewTags.removeAll()
            viewTags.append(contentsOf: /item?.interestTags?.map({/$0.category}))
            viewTags?.redraw()
            let interests = Singleton.sharedInstance.selectedInterests?.map({/$0.category})
            for tags1 in viewTags.tagArray{
                if /interests?.contains(/tags1.titleLabel?.text){
                    tags1.borderColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
                    tags1.setTitleColor( #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1) , for: .normal)
                }
            }
            lblUserName.text = /item?.userName
            lblDesignation.text = /item?.designation
        }
    }
    
    weak var delegate:DelegateUpdateConvoId?
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    //MARK: - Button Actions
    @IBAction func startChat(_ sender: Any) {
        guard let vc = R.storyboard.chats.chatViewController() else { return }
        vc.isFromChat = false
        vc.receiverData = item
        vc.chatModal.conversationId.value = item?.conversationId
        Singleton.sharedInstance.conversationId = /item?.conversationId
        vc.chatModal.assignConvoId = { [weak self] convoId in
            self?.item?.conversationId = convoId
            self?.delegate?.updateConvoId(row: /self?.row , convoId: /convoId)
        }
        vc.chatingType = .oneToOne
        ez.topMostVC?.pushVC(vc)
    }
    
}
