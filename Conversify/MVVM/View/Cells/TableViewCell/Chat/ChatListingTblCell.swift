//
//  ChatListingTblCell.swift
//  Conversify
//
//  Created by Apple on 23/11/18.
//

import UIKit

class ChatListingTblCell: BaseTableViewCell {
    
    //MARK::- OUTLETS
    @IBOutlet weak var imgUserPic: UIImageView!
    @IBOutlet weak var labelFullName: UILabel!
    @IBOutlet weak var labelLastMsg: UILabel!
    @IBOutlet weak var labelTime: UILabel!
    
    //MARK::- PROPERTIES
    var item : ChatListModel? {
        didSet{
            
            labelFullName.text = /item?.isGroupChat ? item?.senderId?.groupName : item?.senderId?.userName
            imgUserPic.image(url:  /item?.senderId?.img?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /item?.senderId?.img?.thumbnail))
            let postdate = Date(milliseconds: /item?.createdDate)
            labelTime.text = postdate.toTimePassedFormat()
            switch item?.lastChatDetails?.typeOfMsg ?? .txt {
            case .txt:
                labelLastMsg.text = item?.lastChatDetails?.message
            case .video:
                labelLastMsg.text = "Video"
            case .img:
                labelLastMsg.text = "Photo"
            case .gif:
                labelLastMsg.text = "Gif"
            case .audio :
                labelLastMsg.text = "Audio"
            }
        }
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
