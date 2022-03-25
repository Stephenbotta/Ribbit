//
//  ViewStoryImageViewController.swift
//  Conversify
//
//  Created by admin on 06/04/21.
//

import UIKit
import moa

class ViewStoryImageViewController: UIViewController {

    var imageURl = ""
    @IBOutlet weak var imgView: UIImageView!
    @IBOutlet weak var btnback: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        imgView.image(url:imageURl)
    }
    @IBAction func btnBackAction(_ sender: Any) {
        self.popVC()
    }
}
