//
//  SenderImgCell.swift
//  NequoreUser
//
//  Created by MAC_MINI_6 on 06/08/18.
//

import UIKit
import IBAnimatable

protocol DelegateUploadItem: class {
    func uploadItem(index: Int , sec: Int)
}

class SenderImgCell: BaseChatCell {
    
    //MARK::- OUTELTS
    @IBOutlet weak var labelPropertyName: UILabel!
    @IBOutlet weak var imgPlay: UIImageView!
    @IBOutlet weak var btnUpload: SectionalButton!
    @IBOutlet weak var imgView: AnimatableImageView!
    @IBOutlet weak var indicatorUploading: UIActivityIndicatorView!
    @IBOutlet weak var viewVideoDim: AnimatableImageView!
    
    //MARK::- PROPERTIES
    var delegate: DelegateUploadItem?
    var indexPath: IndexPath?{
        didSet{
            btnUpload?.row = indexPath?.row
            btnUpload?.section = indexPath?.section
        }
    }
    
    
    //MARK::- VIEW CYCLE
    override func setUpData() {
        super.setUpData()
        labelPropertyName?.text = ""
        imgView.setImage(image: item?.mesgDetail?.imageM?.thumbnail)
        viewVideoDim.isHidden = (item?.mesgDetail?.type == MsgType.txt.rawValue) ||  (item?.mesgDetail?.type == MsgType.img.rawValue)
        viewVideoDim.backgroundColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 0.2)
        viewVideoDim.alpha = 0.3
        btnUpload?.isEnabled = false
        indicatorUploading.isHidden = true
        indicatorUploading.stopAnimating()
        if !(/item?.mesgDetail?.isUploaded){
            if /item?.mesgDetail?.isFail{
                btnUpload.isEnabled = true
                imgPlay.isHidden = false
                imgPlay.setImage(image: R.image.ic_upload())
                indicatorUploading.stopAnimating()
                indicatorUploading.isHidden = true
            }else{
                imgPlay.isHidden = true
                indicatorUploading.startAnimating()
                indicatorUploading.isHidden = false
            }
        }else{
            imgPlay.isHidden = (item?.mesgDetail?.type == MsgType.txt.rawValue) ||  (item?.mesgDetail?.type == MsgType.img.rawValue)
            imgPlay.setImage(image: R.image.ic_play_video())
            indicatorUploading.isHidden = true
        }
        
    }
    
    override func bindings(){
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
    
    
    //MARK::- ACTION
    
    @IBAction func btnActionUpload(_ sender: UIButton) {
        delegate?.uploadItem(index: /btnUpload.row, sec: /btnUpload.section)
    }
}


class SectionalButton: UIButton {
    
    var row: Int?
    var section: Int?
    
}
