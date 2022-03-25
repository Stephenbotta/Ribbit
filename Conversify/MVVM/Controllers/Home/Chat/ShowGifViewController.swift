//
//  ShowGifViewController.swift
//  Conversify
//
//  Created by Apple on 23/08/19.
//

import UIKit

class ShowGifViewController: UIViewController {

    @IBOutlet weak var btnClose: UIButton!
    @IBOutlet weak var imgAppear: UIImageView!
    
    
    var imgUrl: URL?
    
    override func viewDidLoad() {
        super.viewDidLoad()
      
        showData()
        
    }
    

    func showData(){
        imgAppear.sd_setImage(with: imgUrl, placeholderImage: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: imgUrl )

    }
    
    @IBAction func btnClose(_ sender: UIButton) {
        dismiss(animated: false, completion: nil)
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
