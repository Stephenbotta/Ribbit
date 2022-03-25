//
//  ViewStoriesViewController.swift
//  Conversify
//
//  Created by admin on 06/04/21.
//

import UIKit
import moa
import AVKit
class ViewStoriesViewController: UIViewController {
    
    @IBOutlet weak var pageController: UIPageControl!
    @IBOutlet weak var btnback: UIButton!
    @IBOutlet weak var collectionView: UICollectionView!
    var storyData : StoriesData?
    override func viewDidLoad() {
        super.viewDidLoad()
        collectionView.delegate = self
        collectionView.dataSource = self
        pageController.hidesForSinglePage = true
        startTimer()
    }
    override func viewDidDisappear(_ animated: Bool) {
        
    }
    override func viewWillDisappear(_ animated: Bool) {
     
    }
    @IBAction func btnActionBack(_ sender: Any) {
       
        self.popVC()
    }
    
    func startTimer() {

        let timer =  Timer.scheduledTimer(timeInterval: 4.0, target: self, selector: #selector(self.scrollAutomatically), userInfo: nil, repeats: true)
    }

    @objc func scrollAutomatically(_ timer1: Timer) {

        if let coll  = collectionView {
            for cell in coll.visibleCells {
                let indexPath: IndexPath? = coll.indexPath(for: cell)
                if ((indexPath?.row)! < storyData?.stories?.count ?? 0 - 1){
                    let indexPath1: IndexPath?
                    indexPath1 = IndexPath.init(row: (indexPath?.row)! + 1, section: (indexPath?.section)!)
                    coll.scrollToItem(at: indexPath1!, at: .right, animated: true)
                    var socketAndUserId = [String: Any]()
                    socketAndUserId = ["id" : storyData?.stories?[indexPath!.row].id ?? "", "userId" : Singleton.sharedInstance.loggedInUser?.id ??
                    ""]
                    print(socketAndUserId)
                    
                    SocketIOManager.shared.viewStory( data:socketAndUserId ) { [weak self] (status)  in
                        if status{
                            print("hello socket Hit")
                        }
                    }
                }
                else{
                    self.popVC()
//                   2
                }

            }
        }
    }
}
extension ViewStoriesViewController : UICollectionViewDelegate , UICollectionViewDataSource ,UICollectionViewDelegateFlowLayout{
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {

        pageController?.currentPage = Int(scrollView.contentOffset.x) / Int(scrollView.frame.width)
    }

    func scrollViewDidEndScrollingAnimation(_ scrollView: UIScrollView) {

        pageController?.currentPage = Int(scrollView.contentOffset.x) / Int(scrollView.frame.width)
    }
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return storyData?.stories?.count ?? 0
    }
    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        
        self.pageController.numberOfPages = storyData?.stories?.count ?? 0
    }
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String.init(describing: ViewStoryCVC.self), for: indexPath)as! ViewStoryCVC
        let mediadata = storyData?.stories?[indexPath.row].media
        
        let url = mediadata?.original ?? ""
        cell.imgStoryVIew.image(url: url)
       // cell.imgStoryVIew.kf.setImage(with : URL(string: url))
        let milliseconds = storyData?.stories?[indexPath.row].createdOn ?? 0
                   let formater = DateFormatter()
                   formater.dateFormat = "yyyy-MM-dd HH:mm:ss"
                   let date1 = formater.string(from: Date(timeIntervalSince1970: (Double(milliseconds) / 1000.0)))
                   let now = Date()
                   let date2 = formater.string(from:now)
                   let obj = DateConstant()
                   let time = obj.timeGapBetweenDates(previousDate: date1, currentDate: date2)
        cell.lbltime.text = time
        if(mediadata?.mediaType  == MediaType.video.rawValue){
            print(mediadata?.videoUrl)
            cell.btnplayVideo.isHidden = false
        }
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: collectionView.frame.width, height: collectionView.frame.height)
    }
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let mediadata = storyData?.stories?[indexPath.row].media
        if(mediadata?.mediaType == MediaType.video.rawValue){
          //  vc.imageURl = mediadata?.videoUrl ?? ""
            let videoURL = URL(string: mediadata?.videoUrl ?? "" )
            let player = AVPlayer(url: videoURL!)
            let playerViewController = AVPlayerViewController()
            playerViewController.player = player
            self.present(playerViewController, animated: true) {
                playerViewController.player!.play()
            }
        }else{
            guard let vc = R.storyboard.home.viewStoryImageVC() else { return }
            vc.imageURl = mediadata?.original ?? ""
            self.pushVC(vc)
        }
        
    }
    
}
