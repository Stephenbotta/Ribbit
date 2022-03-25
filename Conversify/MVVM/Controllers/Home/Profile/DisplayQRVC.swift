//
//  DisplayQRVC.swift
//  Conversify
//
//  Created by Apple on 13/12/19.
//

import UIKit

class DisplayQRVC: UIViewController {

    //MARK::- IBOUTLETS
    @IBOutlet weak var labelEmail: UILabel!
    @IBOutlet weak var labelPhoneNumber: UILabel!
    @IBOutlet weak var labelUserName: UILabel!
    @IBOutlet weak var userImgView: UIImageView!
    @IBOutlet weak var userQRView: UIImageView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        let userData = Singleton.sharedInstance.loggedInUser
        labelEmail.text = /userData?.email
        labelUserName.text = /userData?.firstName
        labelPhoneNumber.text = /userData?.phoneNumber
        userImgView.setImage(image: /userData?.img?.thumbnail)
        
        let encodedImageData = /userData?.qrCodeStr
    
        let results = encodedImageData.matches(for: "data:image\\/([a-zA-Z]*);base64,([^\\\"]*)")
        for imageString in results {
            autoreleasepool {
                let image = imageString.base64ToImage()
                userQRView.image = image
            }
        }
    }
    
    @IBAction func btnActionBack(_ sender: UIButton) {
        popVC()
    }
}
extension String {
    func base64ToImage() -> UIImage? {
        if let url = URL(string: self),let data = try? Data(contentsOf: url),let image = UIImage(data: data) {
            return image
        }
        return nil
    }
    
    func matches(for regex: String) -> [String] {
           do {
               let regex = try NSRegularExpression(pattern: regex)
               let results = regex.matches(in: self, range:  NSRange(self.startIndex..., in: self))
               return results.map {
                   //self.substring(with: Range($0.range, in: self)!)
                   String(self[Range($0.range, in: self)!])
               }
           } catch let error {
               print("invalid regex: \(error.localizedDescription)")
               return []
           }
       }
}
