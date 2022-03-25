//
//  ReceiverImgCell.swift
//  NequoreUser
//
//  Created by MAC_MINI_6 on 06/08/18.
//

import UIKit
import IBAnimatable

protocol DelegateImagePlay : class{
    func playVideo(sec: Int , row: Int)
}

class ReceiverImgCell: BaseChatCell {
    
    //MARK::- OUTLETS
    @IBOutlet weak var constraintWidthImg: NSLayoutConstraint!
    @IBOutlet weak var leadingConstraintImg: NSLayoutConstraint!
    @IBOutlet weak var constraintHeightImg: NSLayoutConstraint!
    @IBOutlet weak var labelPropertyName: UILabel!
    @IBOutlet weak var imageUser: AnimatableImageView!
    @IBOutlet weak var imgView: AnimatableImageView!
    @IBOutlet weak var viewVideoDim: AnimatableImageView!
    @IBOutlet weak var imgPlay: UIImageView!
    
    //MARK::- FUNCTIONS
    override func setUpData() {
        super.setUpData()
        labelPropertyName?.text = ""
        viewVideoDim.isHidden = (item?.mesgDetail?.type == MsgType.txt.rawValue) ||  (item?.mesgDetail?.type == MsgType.img.rawValue)
        viewVideoDim.backgroundColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 0.2)
        viewVideoDim.alpha = 0.3
        imgView.setImage(image: item?.mesgDetail?.imageM?.thumbnail)
        imgPlay.isHidden = (item?.mesgDetail?.type == MsgType.txt.rawValue) ||  (item?.mesgDetail?.type == MsgType.img.rawValue)
        imageUser.image(url:  /item?.senderId?.img?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /item?.senderId?.img?.thumbnail))
        imageUser.isUserInteractionEnabled = true
    }
    
    override func bindings(){
        imageUser.onTap { [weak self] (gesture) in
            
        }
        imgView.addTapGesture { [weak self] (gesture) in
            self?.delegatePlay?.playVideo(sec: /self?.sec , row: /self?.row)
        }
        viewVideoDim.addTapGesture { [weak self] (gesture) in
            self?.delegatePlay?.playVideo(sec: /self?.sec , row: /self?.row)
        }
        
        imgPlay.addTapGesture { [weak self] (gesture) in
            self?.delegatePlay?.playVideo(sec: /self?.sec , row: /self?.row)
        }
        
    }
    
    
    
}
