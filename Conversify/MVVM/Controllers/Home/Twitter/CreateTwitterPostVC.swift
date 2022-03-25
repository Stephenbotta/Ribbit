//
//  CreateTwitterPostVC.swift
//  Conversify
//
//  Created by Apple on 21/05/20.
//

import UIKit
import EZSwiftExtensions
import OpalImagePicker
import Photos
import TwitterKit

class CreateTwitterPostVC: UIViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnPost: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var txtfPost: UITextView!
    @IBOutlet weak var imgPostView: UIImageView!
    @IBOutlet weak var btnCamera: UIButton!
    @IBOutlet weak var btnDelete: UIButton!
    @IBOutlet weak var collectionView: UICollectionView!
    
    //MARK::- PROPERTIES
    var mediaPickerVC: MediaPickerController?
    let imagePicker = OpalImagePickerController()
    var createPostModal = CreatePostViewModal()
    var selectedImg = UIImage()
    var client = TWTRAPIClient.withCurrentUser()
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    //MARK::- IBAction
    @IBAction func actionBackBtn(_ sender: Any) {
        self.popVC()
    }
    @IBAction func actionCameraBtn(_ sender: Any) {
        self.openMediaPicker()
    }
    
    @IBAction func actionPostBtn(_ sender: Any) {
        let  userId = UserDefaults.standard.object(forKey: "userId") as? String
        let data = selectedImg.jpegData(compressionQuality: 0.5)
        
        if txtfPost.text == "" && data == nil {
            UtilityFunctions.makeToast(text: "Please post something", type: .success)
        } else if txtfPost.text != "" {
            self.postText(tweetString:/self.txtfPost.text , withUserID: /userId)
        } else if data != nil {
            self.postImage(tweetImage: /data, withUserID: /userId)
        } else {
            self.postBoth(tweetString: /self.txtfPost.text, tweetImage: /data, withUserID: /userId)
        }
        
    }
    
    func onLoad(){
        imagePicker.allowedMediaTypes = Set([PHAssetMediaType.image , PHAssetMediaType.video])
        txtfPost.delegate = self
        //self.btnPost.isEnabled = true//valid.element
        self.btnPost.alpha = 1.0 //valid.element ? 1.0 : 0.4
    }
    
    func postText(tweetString: String,withUserID :String) {
        self.showLoader()
        let updateUrl = "https://api.twitter.com/1.1/statuses/update.json"
        let client = TWTRAPIClient.init(userID: withUserID)
        let message = ["status": tweetString]
        
        let requestUpdateUrl = client.urlRequest(withMethod: "POST", urlString: updateUrl, parameters: message, error: nil)
        
        client.sendTwitterRequest(requestUpdateUrl, completion: { (urlResponse, data, connectionError) -> Void in
            if connectionError != nil {
                self.hideLoader()
                print("Error: \(connectionError?.localizedDescription ?? "")")
            }
            do {
                if data != nil {
                    self.hideLoader()
                    self.popVC()
                    UtilityFunctions.makeToast(text: "Status uploaded successfully", type: .success)
                }
            } catch let jsonError as NSError {
                self.hideLoader()
                print("json error: \(jsonError.localizedDescription)")
            }
        })
    }
    
    func postImage(tweetImage: Data ,withUserID :String) {
        self.showLoader()
        let uploadUrl = "https://upload.twitter.com/1.1/media/upload.json"
        let imageString = tweetImage.base64EncodedString(options: NSData.Base64EncodingOptions())
        
        let client = TWTRAPIClient.init(userID: withUserID)
        
        let requestUploadUrl = client.urlRequest(withMethod: "POST", urlString: uploadUrl, parameters: ["media": imageString], error: nil)
        
        client.sendTwitterRequest(requestUploadUrl) { (urlResponse, data, connectionError) -> Void in
            
            if connectionError != nil {
                self.hideLoader()
                print("Error: \(connectionError?.localizedDescription ?? "")")
            }
            do {
                if data != nil {
                    self.hideLoader()
                    self.popVC()
                    UtilityFunctions.makeToast(text: "Image uploaded successfully", type: .success)
                }
            }catch let jsonError as NSError {
                self.hideLoader()
                print("json error: \(jsonError.localizedDescription)")
            }
        }
    }
    
    func postBoth(tweetString: String, tweetImage: Data ,withUserID :String) {
        self.showLoader()
        let uploadUrl = "https://upload.twitter.com/1.1/media/upload.json"
        let updateUrl = "https://api.twitter.com/1.1/statuses/update.json"
        let imageString = tweetImage.base64EncodedString(options: NSData.Base64EncodingOptions())
        
        let client = TWTRAPIClient.init(userID: withUserID)
        
        let requestUploadUrl = client.urlRequest(withMethod: "POST", urlString: uploadUrl, parameters: ["media": imageString], error: nil)
        
        client.sendTwitterRequest(requestUploadUrl) { (urlResponse, data, connectionError) -> Void in
            
            if connectionError != nil {
                self.hideLoader()
                print("Error: \(connectionError?.localizedDescription ?? "")")
            }
            do {
                if data != nil {
                    
                    if let mediaDict = self.nsDataToJson(data: (data! as NSData) as Data) as? [String : Any] {
                        let media_id = mediaDict["media_id_string"] as! String
                        let message = ["status": tweetString, "media_ids": media_id]
                        
                        let requestUpdateUrl = client.urlRequest(withMethod: "POST", urlString: updateUrl, parameters: message, error: nil)
                        
                        client.sendTwitterRequest(requestUpdateUrl, completion: { (urlResponse, data, connectionError) -> Void in
                            self.hideLoader()
                            if connectionError == nil {
                                if let _ = self.nsDataToJson(data: (data! as NSData) as Data) as? [String : Any] {
                                    
                                    self.popVC()
                                    print("Upload suceess to Twitter")
                                }
                            }
                        })
                    }
                }
            }catch let jsonError as NSError {
                self.hideLoader()
                print("json error: \(jsonError.localizedDescription)")
            }
        }
    }
    func nsDataToJson (data: Data) -> AnyObject? {
        do {
            return try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as AnyObject
        } catch let myJSONError {
            print(myJSONError)
        }
        return nil
    }
    func showLoader() {
        Loader.shared.start()
    }
    
    func hideLoader(){
        Loader.shared.stop()
    }
}


extension CreateTwitterPostVC : MediaPickerControllerDelegate {
    /** Pick photo **/
    func openMediaPicker() {
        mediaPickerVC = MediaPickerController(type: .imageOnly, presentingViewController: self)
        mediaPickerVC?.delegate = self
        mediaPickerVC?.show()
    }
    
    func mediaPickerControllerDidPickImage(_ image: UIImage) {
        selectedImg = image
        imgPostView.image = image
    }
    
}


extension CreateTwitterPostVC : UITextViewDelegate {
    
    
    func textViewDidBeginEditing(_ textView: UITextView) {
        if textView == txtfPost{
            txtfPost.borderColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
        }else{
            txtfPost.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textViewDidEndEditing(_ textView: UITextView) {
        if textView == txtfPost{
            txtfPost.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            txtfPost.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
}
