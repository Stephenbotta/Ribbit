//
//  ExpandedGifViewController.swift
//  Conversify
//
//  Created by Harminder on 31/07/19.
//

import UIKit
import moa
class ExpandedGifViewController: UIViewController {
    
    @IBOutlet weak var imageGif: UIImageView!
    
    var imageUrl: URL?

    override func viewDidLoad() {
        super.viewDidLoad()
        guard let img = imageUrl else { return }
       // imageGif.kf.indicatorType = .activity
//        imageGif.kf.setImage(
//            with: img,
//            placeholder: R.image.ic_placeholder(),
//            options: [.scaleFactor(UIScreen.main.scale),
//                .transition(.fade(1)),
//                .cacheOriginalImage
//            ])
//        {
//            result in
//            switch result {
//            case .success(let value):
//                print("Task done for: \(value.source.url?.absoluteString ?? "")")
//            case .failure(let error):
//                print("Job failed: \(error.localizedDescription)")
//            }
//        }
        imageGif.setImage(image: img, placeholder: R.image.ic_placeholder())
      //  imageGif.kf.setImage(with: img, placeholder: R.image.ic_placeholder() , options: nil , progressBlock: { (value, val) in
//
//        }) { (image) in
//
//        }
//        imageGif.kf.setImage(with: img)
    }
    

    @IBAction func btnActionBack(_ sender: UIButton) {
        self.dismissVC(completion: nil)
    }
    
}
