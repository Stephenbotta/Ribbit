//
//  CommentAccessory.swift
//  Conversify
//
//  Created by Apple on 20/11/18.
//

import UIKit
import GrowingTextView
import EZSwiftExtensions
import GiphyUISDK
//import GiphyCoreSDK
import OpalImagePicker
import Photos

protocol DelegateCommentAccessory : class {
    func sendComment(cmnt : String?, attachmentUrl: NSDictionary)
    func likePost()
    func showUserTags(searchText : String?)
    func removeSearchView()
    func removeSelectedTag()
}
class CommentAccessory: UIView {
    
    //MARK::- OUTLETS
    @IBOutlet weak var txtfComments: GrowingTextView!
    @IBOutlet weak var constraintHeightViewReply: NSLayoutConstraint!
    @IBOutlet weak var btnSend: UIButton!
    @IBOutlet weak var btnLike: SparkButton!
    @IBOutlet weak var btnRemoveMentioning: UIButton!
    @IBOutlet weak var btnReplyTo: UILabel!
    @IBOutlet weak var btnCamera: UIButton!
    
    //MARK::- PROPERTIES
    var mediaPickerVC:MediaPickerController?
    var mediaPicker: OpalImagePickerController?
    var delegate : DelegateCommentAccessory?
    var searchTimer: Timer?
    var selectedImage: UIImage?
    var videoPath : URL?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupView()
    }
    
    //MARK:- for iPhoneX Spacing bottom
    override func didMoveToWindow() {
        super.didMoveToWindow()
        if #available(iOS 11.0, *) {
            if let window = self.window {
                self.bottomAnchor.constraint(lessThanOrEqualToSystemSpacingBelow: window.safeAreaLayoutGuide.bottomAnchor, multiplier: 1.0).isActive = true
            }
        }
    }
    
    private func setupView() {
        let view = viewFromNibForClass()
        view.frame = bounds
        view.autoresizingMask = [
            UIView.AutoresizingMask.flexibleWidth,
            UIView.AutoresizingMask.flexibleHeight
        ]
        addSubview(view)
        txtfComments.delegate = self
        NotificationCenter.default.addObserver(self, selector:  #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector:  #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    // Loads a XIB file into a view and returns this view.
    private func viewFromNibForClass() -> UIView {
        let bundle = Bundle(for: type(of: self))
        let nib = UINib(nibName: String(describing: type(of: self)), bundle: bundle)
        let view = nib.instantiate(withOwner: self, options: nil).first as! UIView
        return view
    }
    
    
    
    //Mark::- Button Actions
    @IBAction func btnRemoveMentioning(_ sender: UIButton) {
        constraintHeightViewReply.constant = 0
        btnCamera.isHidden = false
        txtfComments.text = nil
        delegate?.removeSelectedTag()
        self.layoutIfNeeded()
    }
    
    @IBAction func btnActionSend(_ sender: UIButton) {
        let txt =  txtfComments.text.trimmed()
        if txt.isEmpty {
            return
        }
        delegate?.sendComment(cmnt: txt, attachmentUrl: NSDictionary())
        txtfComments.text = nil
    }
    
    @IBAction func btnActionLike(_ sender: UIButton) {
        btnLike.isSelected = /btnLike.isSelected.toggle()
        if /btnLike.isSelected{
            btnLike.likeBounce(0.6)
            btnLike.animate()
        }else{
            btnLike.unLikeBounce(0.4)
        }
        delegate?.likePost()
    }
    @IBAction func btnCameraAction(_ sender: Any) {
        
        let actionSheetController = UIAlertController(title: "Comment on post", message: "", preferredStyle: .alert)
        txtfComments.endEditing(true)
        typeC = "Comment"
        let cancelActionButton = UIAlertAction(title: "Cancel", style: .cancel) { action -> Void in
            print("Cancel")
        }
        actionSheetController.addAction(cancelActionButton)
        
        let openCamera = UIAlertAction(title: "Take Image", style: .default, handler: { (action) in
            self.mediaPickerVC = MediaPickerController(type: .imageAndVideo , presentingViewController: UIApplication.topViewController() ?? UIViewController())
            self.mediaPickerVC?.isAllowExisting = false
            self.mediaPickerVC?.delegate = self
            self.mediaPickerVC?.show()
            ez.runThisInMainThread { [ unowned self] in
                //self.btnAttach.isEnabled = true
                //self.indicatorUpload.stopAnimating()
            }
        })
        let openGallery = UIAlertAction(title: "Gallery", style: .default, handler: { (action) in
            ez.runThisInMainThread { [ unowned self] in
                //self.btnAttach.isEnabled = true
                self.mediaPicker = OpalImagePickerController.init()
                //self.indicatorUpload.stopAnimating()
                self.mediaPicker?.modalPresentationStyle = .overFullScreen
                self.mediaPicker?.imagePickerDelegate = self
                ez.topMostVC?.presentVC(/self.mediaPicker)
                
            }
        })
        let openGif = UIAlertAction(title: "gif", style: .default, handler: { (action) in
            let giphy = GiphyViewController()
            giphy.delegate = self
            giphy.theme = GPHTheme(type: .dark)
            
            if let vc = ez.topMostVC as? PostDetailViewController {
                vc.present(giphy, animated: true, completion: nil)
            }
            
            if let vc = ez.topMostVC as? PostMediaDetailViewController {
                vc.present(giphy, animated: true, completion: nil)
            }
        })
        actionSheetController.addAction(openCamera)
        actionSheetController.addAction(openGallery)
        actionSheetController.addAction(openGif)
        DispatchQueue.main.async {
            ez.topMostVC?.presentVC(actionSheetController)
        }
    }
    @objc func keyboardWillHide(_ notification: NSNotification) {
        delegate?.removeSearchView()
    }
    
    @objc func keyboardWillShow(_ notification: NSNotification) {
        
        //        if let keyboardHeight = (notification.userInfo?[UIKeyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue.height {
        //            tableView.contentInset = UIEdgeInsetsMake(0, 0, keyboardHeight, 0)
        //        }
    }
    
    
    
}
extension CommentAccessory : OpalImagePickerControllerDelegate {
    //    func imagePicker(_ picker: OpalImagePickerController, didFinishPickingImages images: [UIImage]){
    //      presentOpal(mediaArray: images)
    //    }
    func imagePicker(_ picker: OpalImagePickerController, didFinishPickingAssets assets: [PHAsset]){
        presentOpal(mediaArray: assets)
    }
    func imagePickerDidCancel(_ picker: OpalImagePickerController){
        
    }
    func getAssetThumbnail(asset: PHAsset) -> UIImage {
        let manager = PHImageManager.default()
        let option = PHImageRequestOptions()
        var thumbnail = UIImage()
        option.isSynchronous = true
        manager.requestImage(for: asset, targetSize: CGSize(width: 100, height: 100), contentMode: .aspectFit, options: option, resultHandler: {(result, info)->Void in
            thumbnail = result!
        })
        return thumbnail
    }
    
    func getOriginalImage(asset: PHAsset) -> UIImage? {
        
        var img: UIImage?
        let manager = PHImageManager.default()
        let options = PHImageRequestOptions()
        options.version = .original
        options.isSynchronous = true
        manager.requestImageData(for: asset, options: options) { data, _, _, _ in
            
            if let data = data {
                img = UIImage(data: data)
            }
        }
        return img
    }
    
    func presentOpal(mediaArray: [PHAsset]){
        
        //Save Images, update UI
        print(mediaArray)
        
        mediaArray.forEach({ (asst) in
            asst.burstIdentifier
            
            if asst.mediaType == PHAssetMediaType.image{
                
                if asst.playbackStyle == .imageAnimated{
                    
                    asst.requestContentEditingInput(with: PHContentEditingInputRequestOptions()) { (input, _) in
                        let url = input?.fullSizeImageURL
                        do{
                            let data = try? Data(contentsOf: (url ?? URL(fileURLWithPath: "") ))
                            
                            let img = self.getAssetThumbnail(asset: /mediaArray.first)
                            
                            let imgData = img.jpegData(compressionQuality:1)?.count
                            let mbs = /imgData?.toDouble / (1048576.0)
                            if mbs > 25.0 {
                                UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
                                return
                            }
                            
                            self.uploadImageGif(data: data)
                            
                        }
                        catch _ { }
                    }
                    //                    let img = getAssetThumbnail(asset: /mediaArray.first)
                    
                    debugPrint("Animated")
                    
                }
                    
                    
                else if asst.playbackStyle == .image{
                    
                    guard let img = getOriginalImage(asset: asst) else {return}
                    let imgData = img.jpegData(compressionQuality:1)?.count
                    let mbs = /imgData?.toDouble / (1048576.0)
                    if mbs > 25.0 {
                        UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
                        return
                    }
                    
                    self.uploadImage(image: [img ?? UIImage() as? UIImage ?? UIImage() ])
                }
                
                
            }
        })
        
        //Dismiss Controller
        self.mediaPicker?.dismiss(animated: true, completion: nil)
    }
}
//MARK:- Growing TextView Delegate
extension CommentAccessory: GrowingTextViewDelegate {
    
    func textViewDidChangeHeight(_ textView: GrowingTextView, height: CGFloat) {
        // invalidateIntrinsicContentSize()
        // https://developer.apple.com/documentation/uikit/uiview/1622457-invalidateintrinsiccontentsize
        // to reflect height changes
        textView.invalidateIntrinsicContentSize()
    }
    
    func textViewDidChange(_ textView: UITextView) {
        btnSend.isHidden = (textView.text.trimmed().isEmpty) ? true : false
        btnLike.isHidden = !btnSend.isHidden
    }
    
    func textViewDidChangeSelection(_ textView: UITextView) {
        
        btnSend.isHidden = (textView.text.trimmed().isEmpty) ? true : false
        btnLike.isHidden = !btnSend.isHidden
        
        if searchTimer != nil {
            searchTimer?.invalidate()
            searchTimer = nil
        }
        
        
        if (textView.text.trimmed().isEmpty) {
            delegate?.removeSelectedTag()
            searchTimer?.invalidate()
            searchTimer = nil
            return
        }
        
        searchTimer = Timer.scheduledTimer(timeInterval: 0, target: self, selector: #selector(searchForKeyword(_:)), userInfo: /textView.text, repeats: false)
        
    }
    
    @objc func searchForKeyword(_ timer: Timer) {
        guard let range = txtfComments.selectedTextRange else {
            return
        }
        let cursorPosition = txtfComments.offset(from: txtfComments.beginningOfDocument, to: range.start)
        let str = /txtfComments.text
        let index = str.index(str.startIndex, offsetBy: cursorPosition)
        let textbefore = str[..<index]
        let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if hasTag?.hasPrefix("@") == true {
            if searchUserText().isEmpty {
                delegate?.removeSearchView()
                delegate?.showUserTags(searchText: searchUserText())
            } else {
                delegate?.showUserTags(searchText: searchUserText())
            }
        }
        else {
            delegate?.removeSearchView()
        }
    }
    
    //SEARCH USER TO TAG IN COMMENT
    func searchUserText() -> String {
        guard let range = txtfComments.selectedTextRange else {
            return ""
        }
        let cursorPosition = txtfComments.offset(from: txtfComments.beginningOfDocument, to: range.start)
        let str = /txtfComments.text
        let index = str.index(str.startIndex, offsetBy: cursorPosition)
        let textbefore = str[..<index]
        let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if hasTag?.hasPrefix("@") == true {
            return /hasTag?.replacingOccurrences(of: "@", with: "")
        }
        return ""
    }
    
    // REPLACE SEARCH STRING WITH USER_NAME
    func setTag(member: UserList) {
        guard let range = txtfComments.selectedTextRange else {
            return
        }
        let cursorPosition = txtfComments.offset(from: txtfComments.beginningOfDocument, to: range.start)
        var str = /txtfComments.text
        let index = str.index(str.startIndex, offsetBy: cursorPosition)
        let textbefore = str[..<index]
        let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if hasTag?.hasPrefix("@") == true {
            let endIndex = str.index(str.startIndex, offsetBy: cursorPosition)
            var length = 0
            for character in String(textbefore).reversed() {
                if character == "@" {
                    break
                }
                length += 1
            }
            let startIndex = str.index(str.startIndex, offsetBy: cursorPosition - length)
            str.replaceSubrange(startIndex..<endIndex, with: /member.userName + " ")
            txtfComments.text = str
        }
    }
    
}
//extension CommentAccessory : MediaPickerControllerDelegate {
//
//    /** Pick photo **/
//    func openMediaPicker() {
//        ez.runThisInMainThread { [ unowned self] in
//            //self.btnAttach.isEnabled = true
//            //self.indicatorUpload.stopAnimating()
//        }
//        mediaPickerVC = MediaPickerController(type: .imageOnly, presentingViewController: UIApplication.topViewController() ?? UIViewController())
//
//        mediaPickerVC?.delegate = self
//        mediaPickerVC?.show()
//    }
//
//    func mediaPickerControllerDidPickImage(_ image: UIImage) {
//        selectedImage = image
//    }
//
//}


extension CommentAccessory {
    func uploadImageGif(data: Data?,  isFromVideo: Bool = false){
        S3.uploadGif(gifData: data, success: { (imageUrl) in
            let dict = ["original":imageUrl,
                        "thumbnail":imageUrl]
            self.delegate?.sendComment(cmnt: "Thanks", attachmentUrl: dict as NSDictionary)
        })
        { [weak self] (error) in
            print(error)
            //message.isUploaded = false
            // message.isFail = true
            //Loader.shared.stop()
            UtilityFunctions.makeToast(text: error, type: .error)
        }
    }
    
    
    
    func uploadImage(image:[UIImage]){
        S3.upload(image: image.first , success: { [weak self] (imageName) in
            let dict = ["original":imageName,
                        "thumbnail":imageName]
            self?.delegate?.sendComment(cmnt: "Thanks" , attachmentUrl: dict as NSDictionary)
        }) { [weak self] (error) in
            print(error)
            
            UtilityFunctions.makeToast(text: error, type: .error)
        }
    }
    
    
}
extension CommentAccessory: GiphyDelegate {
    
    func didSelectMedia(giphyViewController: GiphyViewController, media: GPHMedia)   {
       
        if let gifURL = media.url(rendition: .fixedWidth, fileType: .gif) {
            if let url = URL(string: gifURL) {
                URLSession.shared.dataTask(with: url) { data, response, error in
                    if let data = data {
                        
                        let imgData = data.count
                        let mbs = /imgData.toDouble / (1048576.0)
                        if mbs > 25.0 {
                            //Loader.shared.stop()
                            UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
                            return
                        }
                        self.uploadImageGif(data: data)
                        
                    }
                }.resume()
            }
        }
        // your user tapped a GIF!
        giphyViewController.dismiss(animated: true, completion: nil)
    }
    
    func didDismiss(controller: GiphyViewController?) {
        // your user dismissed the controller without selecting a GIF.
    }
}
extension CommentAccessory: MediaPickerControllerDelegate {
    /** Pick photo **/
    func openMediaPicker() {
        
        ez.runThisInMainThread { [ unowned self] in
            //self.btnAttach.isEnabled = true
            // self.indicatorUpload.stopAnimating()
        }
        mediaPickerVC = MediaPickerController(type: .imageOnly, presentingViewController: UIApplication.topViewController() ?? UIViewController())
        
        mediaPickerVC?.delegate = self
        mediaPickerVC?.show()
    }
    
    func mediaPickerControllerDidPickImage(_ img: UIImage) {
        ez.runThisInMainThread { [ unowned self] in
            //self.btnAttach.isEnabled = true
            // self.indicatorUpload.stopAnimating()
        }
        let imgData = img.jpegData(compressionQuality:1)?.count
        let mbs = /imgData?.toDouble / (1048576.0)
        if mbs > 25.0 {
            UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
            return
        }
        self.uploadImage(image: [img ?? UIImage()])
    }
    
    
}

