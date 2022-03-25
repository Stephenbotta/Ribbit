//
//  SurveyQuesHeaderView.swift
//  Conversify
//
//  Created by Apple on 11/12/19.
//

import UIKit
import AVFoundation
import AVKit
import Lightbox

class SurveyQuesHeaderView: UITableViewHeaderFooterView {
    
    //MARK::- IBOUTLETS
    @IBOutlet weak var labelQuestionNumber: UILabel!
    @IBOutlet weak var labelQuestion: UILabel!
    @IBOutlet weak var imgView: UIImageView!
    @IBOutlet weak var btnPlay: UIButton!
    @IBOutlet weak var btnShowImageVideo: UIButton!
    
    var currentQues : Int = 0
    var item : SurveyQuestions?{
        didSet{
            btnPlay.isHidden = (item?.media?.mediaType != .video)
            imgView.isHidden = (item?.media?.mediaType == .txt)
            btnShowImageVideo.isHidden = (item?.media?.mediaType == .txt)
            
            labelQuestion.text = item?.name
           // imgView.kf.indicatorType = .activity
            imgView.setImage(image: /item?.media?.thumbnail)
            btnShowImageVideo.addTarget(self, action: #selector(showImageVideo), for: .touchUpInside)
            btnPlay.addTarget(self, action: #selector(playVideo), for: .touchUpInside)
        }
    }
    
    @objc func playVideo() {
        guard let videoURL = URL.init(string: /item?.media?.original) else { return }
        let player = AVPlayer(url: videoURL)
        let playerViewController = AVPlayerViewController()
        playerViewController.player = player
        let topVC = UIApplication.shared.topMostViewController()
        topVC?.present(playerViewController, animated: true) {
            playerViewController.player!.play()
        }
    }
    
    @objc func showImageVideo(){
        if item?.media?.mediaType == .img {
            let topVC = UIApplication.shared.topMostViewController()
            guard let imgUrl = URL.init(string: /(/item?.media?.original)) else { return }
            let controller = LightboxController(images: [LightboxImage(imageURL: imgUrl)])
            controller.dynamicBackground = true
            controller.modalPresentationStyle = .fullScreen
            topVC?.presentVC(controller)
        }else{
            playVideo()
        }
    }
    
}

