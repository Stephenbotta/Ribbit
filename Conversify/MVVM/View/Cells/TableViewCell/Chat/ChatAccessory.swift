//
//  ChatAccessory.swift
//  NequoreUser
//
//  Created by MAC_MINI_6 on 04/08/18.
//

import UIKit
import IBAnimatable
//import DBAttachmentPickerController
import OpalImagePicker
import GrowingTextView
import Photos
import EZSwiftExtensions
import GiphyUISDK
//import GiphyCoreSDK
import iRecordView
import AVFoundation

protocol SendMessagedDelegate: class {
    func updateMessage(with message: ChatData)
    func appendNewMsg(msgObj : ChatData?)
    func updateUploadingCompleted(with message: ChatData)
    func updateUploadingFail(with message: ChatData)
    func showUsersTable()
    func hideUsersTable()
}



class ChatAccessory: UIView {
    
    //MARK:- IBOutlets
    @IBOutlet weak var btnChat: AnimatableButton!
    @IBOutlet weak var textView: GrowingTextView!{
        didSet {
            textView.delegate = self
        }
    }
    
    @IBOutlet weak var btnGallery: UIButton!
    @IBOutlet weak var btnCamera: UIButton!
    @IBOutlet weak var btnGif: UIButton!
    @IBOutlet weak var indicatorUpload: UIActivityIndicatorView!
    @IBOutlet weak var voiceRecordHUD: VoiceRecordHUD!
    
    @IBOutlet weak var stackVw: UIStackView!
    @IBOutlet weak var vwChat: AnimatableView!
    //MARK:- PROPERTIES
    weak var delegate: SendMessagedDelegate?
    var mediaPicker: OpalImagePickerController?
    var groupId : String?
    var videoPath : URL?
    var selectedTagUsers: [Members] = []
    var typeOfChat : chatType?
    var receiverData : Any?
    var gotConvoId : ((ChatData) -> ())?
    var mediaPickerVC: MediaPickerController?
    var recording: Recording?
    var recordDuration = 0
    let recordButton = RecordButton()
    let recordView = RecordView()
    var player: AVAudioPlayer?
    
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
    
    
    @IBAction func actionGif(_ sender: Any) {
        let giphy = GiphyViewController()
        giphy.delegate = self
        giphy.theme = GPHTheme(type: .dark)
        
        if let vc = UIApplication.topViewController() as? ChatViewController {
            vc.present(giphy, animated: true, completion: nil)
        }
    }
    @IBAction func actionCamera(_ sender: Any) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
         
        self.mediaPickerVC = MediaPickerController(type: .imageAndVideo , presentingViewController: UIApplication.topViewController() ?? UIViewController())
        self.mediaPickerVC?.isAllowExisting = false
        self.mediaPickerVC?.delegate = self
        self.mediaPickerVC?.show()
        ez.runThisInMainThread { [ unowned self] in
            //self.btnAttach.isEnabled = true
            self.indicatorUpload.stopAnimating()
        } }
    }
    
    @IBAction func actionGallery(_ sender: Any) {
        self.openOpalMediaPicker()
    }
    
    
    @IBAction func btnActionRecordAudio(_ sender: UIButton) {
        
    }
    
    //MARK::- FUNCTIONS
    
    func configurePickr(){
        //self.btnAttach.isEnabled = false
        indicatorUpload.startAnimating()
        
        
        if  PHPhotoLibrary.authorizationStatus() == .authorized &&
            CheckPermission.shared.status(For: .camera) == 3 {
            //both allow
            self.configProceed(isVideo: false, isImage: false, isBoth: true)
        }else{
            //check for camera
            AVCaptureDevice.requestAccess(for: AVMediaType.video) { response in
                if response { //true
                    //check for photos
                    let photos = PHPhotoLibrary.authorizationStatus()
                    if photos == .notDetermined || photos == .denied{
                        PHPhotoLibrary.requestAuthorization({status in
                            if status == .authorized{
                                self.configProceed(isVideo: false, isImage: false, isBoth: true)
                            } else {
                                ez.runThisAfterDelay(seconds: 2.0, after: {
                                    UtilityFunctions.show(alert: "", message: "You need to enable photos access from app settings to share image", buttonOk: {
                                        //self.btnAttach.isEnabled = true
                                        self.indicatorUpload.stopAnimating()
                                        CheckPermission.shared.openAppSettings()
                                        return
                                    }, buttonCancel: { [weak self] in
                                        self?.configProceed(isVideo: true, isImage: false, isBoth: false)
                                        }, viewController: UIApplication.topViewController()!, buttonText: "Yes", cancelButtonText: "No")
                                })
                            }
                        })
                    } else {
                        self.configProceed(isVideo: false, isImage: true, isBoth: false)
                    }
                    
                } else {//false
                    //request access
                    ez.runThisAfterDelay(seconds: 1.0, after: {
                        UtilityFunctions.show(alert: "", message: "You need to enable camera access from app settings to share image from camera", buttonOk: {
                            //self.btnAttach.isEnabled = true
                            self.indicatorUpload.stopAnimating()
                            CheckPermission.shared.openAppSettings()
                            return
                        }, buttonCancel: { [weak self] in
                            let photos = PHPhotoLibrary.authorizationStatus()
                            if photos == .notDetermined || photos == .denied{
                                PHPhotoLibrary.requestAuthorization({ [weak self]  status in
                                    if status == .authorized{
                                        self?.configProceed(isVideo: false, isImage: true, isBoth: false)
                                    } else {
                                        
                                        ez.runThisAfterDelay(seconds: 1.0, after: {
                                            
                                            UtilityFunctions.show(alert: "", message: "Kindly give permissions from app settings to share media", buttonOk: {
                                                //self?.btnAttach.isEnabled = true
                                                self?.indicatorUpload.stopAnimating()
                                                CheckPermission.shared.openAppSettings()
                                                return
                                            }, buttonCancel: { [weak self] in
                                                //self?.btnAttach.isEnabled = true
                                                self?.indicatorUpload.stopAnimating()
                                                
                                                
                                                UtilityFunctions.show(alert: "", message: "Kindly give permissions from app settings to share media" , buttonOk: {
                                                    UtilityFunctions.makeToast(text: "", type: .info)
                                                }, viewController: UIApplication.topViewController() ?? UIViewController(), buttonText: "Ok")
                                                
                                                //                                                self?.configProceed(isVideo: false, isImage: false, isBoth: false)
                                                }, viewController: UIApplication.topViewController() ?? UIViewController(), buttonText: "Ok", cancelButtonText: "Cancel")
                                        })
                                    }
                                })
                            }else{
                                self?.configProceed(isVideo: false, isImage: true, isBoth: false)
                            }
                            
                            }, viewController: UIApplication.topViewController()!, buttonText: "Yes", cancelButtonText: "No")
                    })
                }
            }
        }
    }
    
    func openCustomPhotosPicker(){
        //  btnAttach.isEnabled = true
        indicatorUpload.stopAnimating()
        openMediaPicker()
        //        ez.runThisAfterDelay(seconds: 2.0, after: {
        //            UtilityFunctions.show(nativeActionSheet: "Select option", subTitle: "", vc: UIApplication.topViewController() ?? UIViewController() , senders: ["Select photo", "Select document"], success: { [unowned self] (value, index) in
        //                switch index{
        //                case 0:
        //                    self.openMediaPicker()
        //                default:
        //                    self.mediaPicker?.mediaType = [ .other]
        //                    self.openDBAttachmentMediaPicker()
        //                }
        //            })
        //        })
    }
    
    func configProceed(isVideo: Bool?, isImage: Bool?, isBoth: Bool?){
        
        mediaPicker = OpalImagePickerController.init()
        mediaPicker?.imagePickerDelegate = self
        
        //        mediaPicker = DBAttachmentPickerController.init(finishPicking: { (attachement) in
        //            self.mediaPicked(mediaArray: attachement)
        //        }, cancel: {
        //
        //        })
        if /isVideo {
            mediaPicker?.allowedMediaTypes = [   .image , .video]
            //            openMediaPicker(isCam:true , isGal:true)
            openOpalMediaPicker()
        }
        if /isImage {
            openCustomPhotosPicker()
        }
        if /isBoth {
            mediaPicker?.allowedMediaTypes = [.image  , .video]
            openMediaPicker(isCam:true , isGal:true)
            //            openOpalMediaPicker()
        }
        
        if !(/isVideo) && !(/isImage) && !(/isBoth) {
            //            mediaPicker?.mediaType = [ .other]
            //            openDBAttachmentMediaPicker()
        }
        
    }
    
    func openMediaPicker(isCam:Bool , isGal:Bool) {
        
        var senders = [String]()
        if isCam{
            senders.append("Camera")
        }
        if isGal{
            senders.append("Gallery")
            
        }
        
        DispatchQueue.main.async {
            
            
            UtilityFunctions.showWithCancel(nativeActionSheet: "", subTitle: "", vc: UIApplication.topViewController() ?? UIViewController(), senders: senders, success: { [unowned self] (value, index) in
                switch /(value as? String){
                case "Camera":
                    self.mediaPickerVC = MediaPickerController(type: .imageAndVideo , presentingViewController: UIApplication.topViewController() ?? UIViewController())
                    self.mediaPickerVC?.isAllowExisting = false
                    self.mediaPickerVC?.delegate = self
                    self.mediaPickerVC?.show()
                    ez.runThisInMainThread { [ unowned self] in
                        //self.btnAttach.isEnabled = true
                        self.indicatorUpload.stopAnimating()
                    }
                default:
                    self.openOpalMediaPicker()
                }
            }) {
                ez.runThisInMainThread { [ unowned self] in
                    //self.btnAttach.isEnabled = true
                    self.indicatorUpload.stopAnimating()
                    
                }
                
            }
            
            
            //            show(nativeActionSheet: "", subTitle: "", vc: UIApplication.topViewController() ?? UIViewController() , senders: senders) { [unowned self] (value, index) in
            //                switch /(value as? String){
            //                case "Camera":
            //                    self.mediaPickerVC = MediaPickerController(type: .imageAndVideo , presentingViewController: UIApplication.topViewController() ?? UIViewController())
            //                    self.mediaPickerVC?.isAllowExisting = false
            //                    self.mediaPickerVC?.delegate = self
            //                    self.mediaPickerVC?.show()
            //                    ez.runThisInMainThread { [ unowned self] in
            //                        //self.btnAttach.isEnabled = true
            //                        self.indicatorUpload.stopAnimating()
            //                    }
            //                default:
            //                    self.openOpalMediaPicker()
            //                }
            //            }
        }
        
        
    }
    
    //    func openOpalMediaPicker(){
    //        //        mediaPicker?.allowsMultipleSelection = false
    //        //        mediaPicker?.allowsSelectionFromOtherApps = true
    //        ez.runThisInMainThread { [ unowned self] in
    //            //self.btnAttach.isEnabled = true
    //            self.indicatorUpload.stopAnimating()
    //            self.mediaPicker?.modalPresentationStyle = .overFullScreen
    //            ez.topMostVC?.presentVC(/self.mediaPicker)
    //
    //        }
    //    }
    func openOpalMediaPicker(){
        //        mediaPicker?.allowsMultipleSelection = false
        //        mediaPicker?.allowsSelectionFromOtherApps = true
        ez.runThisInMainThread { [ unowned self] in
            //self.btnAttach.isEnabled = true
            self.mediaPicker = OpalImagePickerController.init()
            self.indicatorUpload.stopAnimating()
            self.mediaPicker?.modalPresentationStyle = .overFullScreen
            self.mediaPicker?.imagePickerDelegate = self
            ez.topMostVC?.presentVC(/self.mediaPicker)
            
        }
    }
    
    func sendInfo(_ sender: UIButton){
        switch sender.tag {
        case 0: //Attach
            textView.resignFirstResponder()
            configurePickr()
            
        case 1: //Send Txt Message
            if (/textView.text).trimmingCharacters(in: .whitespacesAndNewlines) != "" {
                let txtMsg = textView.text.trimmed()
                if txtMsg.isEmpty{return }
                var msg = Message()
                switch typeOfChat ?? .none {
                case .venue:
                    msg = Message(msg: txtMsg, msgtype: MsgType.txt.rawValue, grpType: chatGroupType.venue.rawValue , grpId: groupId, image: nil , video: nil , messageID: UUID().uuidString )
                case .oneToOne :
                    if let receiverData = receiverData as? UserList {
                        msg = Message(msg: txtMsg, msgtype: MsgType.txt.rawValue,receiverId : receiverData.id ,image: nil , video: nil, messageID:UUID().uuidString)
                    }else if let receiverData = receiverData as? User {
                        msg = Message(msg: txtMsg, msgtype: MsgType.txt.rawValue,receiverId : receiverData.id ,image: nil , video: nil, messageID: UUID().uuidString)
                    }
                case .oneToMany:
                    msg = Message(msg: txtMsg, msgtype: MsgType.txt.rawValue, grpType: chatGroupType.group.rawValue , grpId: groupId, image: nil , video: nil , messageID: UUID().uuidString)
                default :
                    break
                }
                
                let obj = ChatData.init(msgObj: msg)
                delegate?.appendNewMsg(msgObj: obj)
                (typeOfChat == .oneToOne ) ? sendOneToOneMsgToSocket(msgObj: obj) : sendMessageInfo(msgObj: obj)
                textView.text = ""
                self.textView.text = nil
            }
            
        default: break
        }
    }
    
    //MARK:- IBAction
    @IBAction func btnAction(_ sender: UIButton) {
        sendInfo(sender)
    }
}

//MARK:- GrowingTextView Delegate for dynamic height increase according to text
extension ChatAccessory: GrowingTextViewDelegate {
    func textViewDidChangeHeight(_ textView: GrowingTextView, height: CGFloat) {
        // invalidateIntrinsicContentSize()
        // https://developer.apple.com/documentation/uikit/uiview/1622457-invalidateintrinsiccontentsize
        // to reflect height changes
          textView.invalidateIntrinsicContentSize()
    }
}

//MARK:- ChatAccessory Functions

extension ChatAccessory : OpalImagePickerControllerDelegate {
    //    func imagePicker(_ picker: OpalImagePickerController, didFinishPickingImages images: [UIImage]){
    //      presentOpal(mediaArray: images)
    //    }
    func imagePicker(_ picker: OpalImagePickerController, didFinishPickingAssets assets: [PHAsset]){
        presentOpal(mediaArray: assets)
    }
    func imagePickerDidCancel(_ picker: OpalImagePickerController){
        
    }
}
extension ChatAccessory {
    
    //    //MARK:- Media Selected
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
                            let imageModel = MessageImage.init(image: url?.absoluteString)
                            
                            var msg = Message()
                            switch self.typeOfChat ?? .none {
                            case .venue:
                                msg = Message.init(msg: nil, msgtype: MsgType.img.rawValue, grpType: chatGroupType.venue.rawValue, grpId: self.groupId, image: imageModel, video: nil, messageID:UUID().uuidString)
                            case .oneToOne :
                                if let receiverData = self.receiverData as? UserList {
                                    msg = Message(msg: nil, msgtype: MsgType.gif.rawValue,receiverId : receiverData.id ,image: imageModel , video: nil)
                                }else if let receiverData = self.receiverData as? User {
                                    msg = Message(msg: nil, msgtype: MsgType.gif.rawValue,receiverId : receiverData.id ,image: imageModel , video: nil)
                                }
                            case .oneToMany:
                                msg = Message(msg: nil, msgtype: MsgType.gif.rawValue, grpType: chatGroupType.group.rawValue , grpId: self.groupId, image: imageModel , video: nil )
                                
                            default :
                                break
                            }
                            let obj = ChatData.init(msgObj: msg)
                            self.delegate?.appendNewMsg(msgObj: obj)
                            
                            self.uploadImageGif(data: data, message: msg)
                            
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
                    let imageModel = MessageImage.init(image: img)
                    
                    var msg = Message()
                    switch self.typeOfChat ?? .none {
                    case .venue:
                        msg = Message.init(msg: nil, msgtype: MsgType.img.rawValue, grpType: chatGroupType.venue.rawValue, grpId: self.groupId, image: imageModel, video: nil, messageID:UUID().uuidString)
                    case .oneToOne :
                        if let receiverData = self.receiverData as? UserList {
                            msg = Message(msg: nil, msgtype: MsgType.img.rawValue,receiverId : receiverData.id ,image: imageModel , video: nil)
                        }else if let receiverData = self.receiverData as? User {
                            msg = Message(msg: nil, msgtype: MsgType.img.rawValue,receiverId : receiverData.id ,image: imageModel , video: nil)
                        }
                    case .oneToMany:
                        msg = Message(msg: nil, msgtype: MsgType.img.rawValue, grpType: chatGroupType.group.rawValue , grpId: self.groupId, image: imageModel , video: nil )
                        
                    default :
                        break
                    }
                    let obj = ChatData.init(msgObj: msg)
                    self.delegate?.appendNewMsg(msgObj: obj)
                    self.uploadImage(image: [img ?? UIImage() as? UIImage ?? UIImage() ], message: msg)
                }
                
            }
                
            else if asst.mediaType == PHAssetMediaType.video{
                if let phAsset = mediaArray.first /*= mediaArray.first?.originalFileResource() as? PHAsset*/  {
                    PHCachingImageManager().requestAVAsset(forVideo: phAsset, options: PHVideoRequestOptions(), resultHandler: { (asset, audiomix, info) in
                        let asset = asset as? AVURLAsset
                        do {
                            guard let urlExisting = asset?.url else {
                                UtilityFunctions.makeToast(text: "This media file you have selected is not supported", type: .error)
                                return }
                            let data = try Data(contentsOf: (urlExisting))
                            
                            let videoMb = data.count.toDouble / (1048576.0)
                            print("=======Video Size======" , videoMb)
                            if videoMb > 25.0 {
                                DispatchQueue.main.async {
                                    UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
                                }
                                
                                return
                            }
                            let videoModel = MessageVideo.init(url: asset?.url.absoluteString, data: data, thumb: (asset?.url)!.generateThumbnail())
                            let imageModel = MessageImage.init(image: (asset?.url)!.generateThumbnail())
                            
                            
                            
                            var msg = Message()
                            switch self.typeOfChat ?? .none {
                            case .venue:
                                msg = Message.init(msg: nil, msgtype: MsgType.video.rawValue, grpType: chatGroupType.group.rawValue, grpId: self.groupId, image: imageModel, video: videoModel, messageID:UUID().uuidString)
                            case .oneToOne :
                                if let receiverData = self.receiverData as? UserList {
                                    msg = Message(msg: nil, msgtype: MsgType.video.rawValue,receiverId : receiverData.id ,image: imageModel , video: videoModel)
                                }else if let receiverData = self.receiverData as? User {
                                    msg = Message(msg: nil, msgtype: MsgType.video.rawValue,receiverId : receiverData.id ,image: imageModel , video: videoModel)
                                }
                            case .oneToMany:
                                msg = Message(msg: nil, msgtype: MsgType.video.rawValue, grpType: chatGroupType.group.rawValue , grpId: self.groupId, image: imageModel , video: videoModel )
                            default :
                                break
                            }
                            let obj = ChatData.init(msgObj: msg)
                            self.delegate?.appendNewMsg(msgObj: obj)
                            self.uploadVideo(message: msg)
                            
                        } catch _ { }
                    })
                }
//                else if let url: URL = asset.image { // URL(fileURLWithPath: (mediaArray.first?.originalFileResource() as? String ?? "")) {
//                    do {
//                        let data = try? Data(contentsOf: url)
//                        let videoMb = /data?.count.toDouble / (1048576.0)
//                        print("=======Document Size======" , videoMb)
//                        if videoMb > 25.0 {
//                            UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
//                            return
//                        }
//                        let videoModel = MessageVideo.init(url: url.absoluteString , data: data, thumb: url.generateThumbnail())
//                        let imageModel = MessageImage.init(image:url.generateThumbnail() )
//
//                        var msg = Message()
//                        switch self.typeOfChat ?? .none {
//                        case .venue:
//                            msg = Message.init(msg: nil, msgtype: MsgType.video.rawValue, grpType: chatGroupType.group.rawValue, grpId: self.groupId, image: imageModel, video: videoModel, messageID:UUID().uuidString)
//                        case .oneToOne :
//                            if let receiverData = self.receiverData as? UserList {
//                                msg = Message(msg: nil, msgtype: MsgType.video.rawValue,receiverId : receiverData.id ,image: imageModel , video: videoModel)
//                            }else if let receiverData = self.receiverData as? User {
//                                msg = Message(msg: nil, msgtype: MsgType.video.rawValue,receiverId : receiverData.id ,image: imageModel , video: videoModel)
//                            }
//                        case .oneToMany:
//                            msg = Message(msg: nil, msgtype: MsgType.video.rawValue, grpType: chatGroupType.group.rawValue , grpId: self.groupId, image: imageModel , video: videoModel )
//                        default :
//                            break
//                        }
//                        let obj = ChatData.init(msgObj: msg)
//                        self.delegate?.appendNewMsg(msgObj: obj)
//                        self.uploadVideo(message: msg)
//                    } catch _ { }
//                }
//            }
               
        }
        else if asst.mediaType == PHAssetMediaType.audio{
        }
        })
        //Dismiss Controller
        self.mediaPicker?.dismiss(animated: true, completion: nil)
    }
    
    // Performs the initial setup.
    private func setupView() {
        let view = viewFromNibForClass()
        view.frame = bounds
        view.autoresizingMask = [
            UIView.AutoresizingMask.flexibleWidth,
            UIView.AutoresizingMask.flexibleHeight
        ]
        
        btnChat.isHidden = textView.text.count == 0
        recordButton.translatesAutoresizingMaskIntoConstraints = false
        
        
        recordView.translatesAutoresizingMaskIntoConstraints = false
        recordView.backgroundColor = UIColor.red
        view.addSubview(recordButton)
        view.addSubview(recordView)
        
        recordButton.widthAnchor.constraint(equalToConstant: 48).isActive = true
        recordButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        recordButton.setCornerRadius(radius: 24)
        recordButton.clipsToBounds = true
        recordButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -8).isActive = true
        recordButton.bottomAnchor.constraint(equalTo: view.safeBottomAnchor, constant: -59).isActive = true
     //  recordButton.topAnchor.constraint(equalTo: view.safeTopAnchor, constant: 16).isActive = true

        recordView.trailingAnchor.constraint(equalTo: recordButton.leadingAnchor, constant: -50).isActive = true
        recordView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10).isActive = true
        recordView.bottomAnchor.constraint(equalTo: recordButton.bottomAnchor).isActive = true
        recordView.delegate = self
        recordView.isSoundEnabled = true
        recordView.slideToCancelTextColor = .white
        recordView.durationTimerColor = .white
        //        recordView.slideToCancelArrowImage = #imageLiteral(resourceName: "cm_arrow_back_white")
        recordView.backgroundColor = #colorLiteral(red: 0.2705882353, green: 0.2705882353, blue: 0.2705882353, alpha: 1)
        recordButton.backgroundColor = #colorLiteral(red: 0.2705882353, green: 0.2705882353, blue: 0.2705882353, alpha: 1)
        recordButton.recordView = recordView
        recordButton.setImage(UIImage.init(named: "ic_recoard"), for: .normal)
        
        
        addSubview(view)
        
        // to dynamically increase height of text view
        // http://ticketmastermobilestudio.com/blog/translating-autoresizing-masks-into-constraints
        //if textView.translatesAutoresizingMaskIntoConstraints = true then height will not increase automatically
        // translatesAutoresizingMaskIntoConstraints default = true
        textView.translatesAutoresizingMaskIntoConstraints = false
        
    }
    
    // Loads a XIB file into a view and returns this view.
    private func viewFromNibForClass() -> UIView {
        let bundle = Bundle(for: type(of: self))
        let nib = UINib(nibName: String(describing: type(of: self)), bundle: bundle)
        let view = nib.instantiate(withOwner: self, options: nil).first as! UIView
        return view
    }
}


extension ChatAccessory : MediaPickerControllerDelegate {
    
    /** Pick photo **/
    func openMediaPicker() {
        
        ez.runThisInMainThread { [ unowned self] in
            //self.btnAttach.isEnabled = true
            self.indicatorUpload.stopAnimating()
        }
        mediaPickerVC = MediaPickerController(type: .imageOnly, presentingViewController: UIApplication.topViewController() ?? UIViewController())
        
        mediaPickerVC?.delegate = self
        mediaPickerVC?.show()
    }
    
    func mediaPickerControllerDidPickImage(_ img: UIImage) {
        ez.runThisInMainThread { [ unowned self] in
            //self.btnAttach.isEnabled = true
            self.indicatorUpload.stopAnimating()
        }
        let imgData = img.jpegData(compressionQuality:1)?.count
        let mbs = /imgData?.toDouble / (1048576.0)
        if mbs > 25.0 {
            UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
            return
        }
        let imageModel = MessageImage.init(image: img)
        
        var msg = Message()
        switch self.typeOfChat ?? .none {
        case .venue:
            msg = Message.init(msg: nil, msgtype: MsgType.img.rawValue, grpType: chatGroupType.venue.rawValue, grpId: self.groupId, image: imageModel, video: nil, messageID:UUID().uuidString)
        case .oneToOne :
            if let receiverData = self.receiverData as? UserList {
                msg = Message(msg: nil, msgtype: MsgType.img.rawValue,receiverId : receiverData.id ,image: imageModel , video: nil)
            }else if let receiverData = self.receiverData as? User {
                msg = Message(msg: nil, msgtype: MsgType.img.rawValue,receiverId : receiverData.id ,image: imageModel , video: nil)
            }
        case .oneToMany:
            msg = Message(msg: nil, msgtype: MsgType.img.rawValue, grpType: chatGroupType.group.rawValue , grpId: self.groupId, image: imageModel , video: nil )
            
        default :
            break
        }
        let obj = ChatData.init(msgObj: msg)
        self.delegate?.appendNewMsg(msgObj: obj)
        self.uploadImage(image: [img ?? UIImage() ], message: msg)
    }
    
    func mediaPickerControllerDidPickVideo(url: URL, data: Data, thumbnail: UIImage){
        //        let video = VideoData()
        //        video.thumbnail = thumbnail
        //        video.data = data
        
        ez.runThisInMainThread { [ unowned self] in
            //self.btnAttach.isEnabled = true
            self.indicatorUpload.stopAnimating()
        }
        
        let videoMb = /data.count.toDouble / (1048576.0)
        print("=======Document Size======" , videoMb)
        if videoMb > 25.0 {
            UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
            return
        }
        let videoModel = MessageVideo.init(url: url.absoluteString , data: data, thumb: url.generateThumbnail())
        let imageModel = MessageImage.init(image:url.generateThumbnail() )
        
        var msg = Message()
        switch self.typeOfChat ?? .none {
        case .venue:
            msg = Message.init(msg: nil, msgtype: MsgType.video.rawValue, grpType: chatGroupType.group.rawValue, grpId: self.groupId, image: imageModel, video: videoModel, messageID:UUID().uuidString)
        case .oneToOne :
            if let receiverData = self.receiverData as? UserList {
                msg = Message(msg: nil, msgtype: MsgType.video.rawValue,receiverId : receiverData.id ,image: imageModel , video: videoModel)
            }else if let receiverData = self.receiverData as? User {
                msg = Message(msg: nil, msgtype: MsgType.video.rawValue,receiverId : receiverData.id ,image: imageModel , video: videoModel)
            }
        case .oneToMany:
            msg = Message(msg: nil, msgtype: MsgType.video.rawValue, grpType: chatGroupType.group.rawValue , grpId: self.groupId, image: imageModel , video: videoModel )
        default :
            break
        }
        let obj = ChatData.init(msgObj: msg)
        self.delegate?.appendNewMsg(msgObj: obj)
        self.uploadVideo(message: msg)
        
        
    }
}


//MARK::- API HANDLER
extension ChatAccessory {
    func currentTimeMillis() -> Int64 {
        return Int64(Date().timeIntervalSince1970 * 1000)
    }
    
    func uploadAudio(data: URL?, message: Message , isFromVideo: Bool = false){
        
        if data != nil {
            let audioData = try? Data(contentsOf: data!)

            let path = try! FileManager.default.url(for: FileManager.SearchPathDirectory.documentDirectory, in: FileManager.SearchPathDomainMask.userDomainMask, appropriateFor: nil, create: false)
            let newPath = path.appendingPathComponent("/\(currentTimeMillis())_audioRecordin.m4a")
            do {
                try audioData?.write(to: newPath)
                S3.uploadAudio(audioURL: newPath, success: { (audioUrl) in
                    let audio = message.audioMsg
                    audio?.audioUrl = audioUrl
                    let newMsg = message
                    newMsg.audioMsg = audio
                    
                    let obj1 = ChatData.init(msgObj: newMsg)
                    self.delegate?.updateUploadingCompleted(with: ChatData.init(msgObj: message))
                    (self.typeOfChat == .oneToOne ) ? self.sendOneToOneMsgToSocket(msgObj: obj1 , imageUrl: audioUrl, audioDuration: message.audioDuration) : self.sendMessageInfo(msgObj: obj1, imageUrl: audioUrl, audioDuration: message.audioDuration)
                })
                { [weak self] (error) in
                    
                    message.isUploaded = false
                    message.isFail = true
                    let obj = ChatData.init(msgObj: message)
                    self?.delegate?.updateUploadingFail(with: obj)
                }
            }catch {
                print(error)
            }
        }
        
        
        
    }
    
    func uploadImageGif(data: Data?, message: Message , isFromVideo: Bool = false){
        S3.uploadGif(gifData: data, success: { (imageUrl) in
            let image = message.imageM
            image?.original = imageUrl
            let newMsg = message
            newMsg.imageM = image
            let obj1 = ChatData.init(msgObj: newMsg)
            self.delegate?.updateUploadingCompleted(with: ChatData.init(msgObj: message))
            (self.typeOfChat == .oneToOne ) ? self.sendOneToOneMsgToSocket(msgObj: obj1 , imageUrl: imageUrl) : self.sendMessageInfo(msgObj: obj1, imageUrl: imageUrl)
        })
        { [weak self] (error) in
            print(error)
            message.isUploaded = false
            message.isFail = true
            let obj = ChatData.init(msgObj: message)
            self?.delegate?.updateUploadingFail(with: obj)
        }
    }
    
    
    
    func uploadImage(image:[UIImage], message: Message , isFromVideo: Bool = false){
        S3.upload(image: image.first , success: { [weak self] (imageName) in
            if isFromVideo{
                let video = message.video
                video?.imgUrl = imageName
                let newMsg = message
                newMsg.video = video
                let obj1 = ChatData.init(msgObj: newMsg)
                self?.delegate?.updateUploadingCompleted(with: ChatData.init(msgObj: message))
                (self?.typeOfChat == .oneToOne ) ? self?.sendOneToOneMsgToSocket(msgObj: obj1 , imageUrl: imageName) : self?.sendMessageInfo(msgObj: obj1, imageUrl: imageName)
            }else{
                let image = message.imageM
                image?.original = imageName
                let newMsg = message
                newMsg.imageM = image
                let obj1 = ChatData.init(msgObj: newMsg)
                self?.delegate?.updateUploadingCompleted(with: ChatData.init(msgObj: message))
                (self?.typeOfChat == .oneToOne ) ? self?.sendOneToOneMsgToSocket(msgObj: obj1 , imageUrl: imageName) : self?.sendMessageInfo(msgObj: obj1, imageUrl: imageName)
                
            }
            
        }) { [weak self] (error) in
            print(error)
            message.isUploaded = false
            message.isFail = true
            let obj = ChatData.init(msgObj: message)
            self?.delegate?.updateUploadingFail(with: obj)
        }
    }
    
    //API HIT TO SEND MESSAGE
    
    func uploadVideo( message: Message){
        guard let thumbImage = message.video?.thumbnail as? UIImage else { return }
        let videoData = message.video?.data
        let path = try! FileManager.default.url(for: FileManager.SearchPathDirectory.documentDirectory, in: FileManager.SearchPathDomainMask.userDomainMask, appropriateFor: nil, create: false)
        let newPath = path.appendingPathComponent("/videoFileName.mp4")
        do {
            try videoData?.write(to: newPath)
            self.videoPath = newPath
            S3.uploadChatVideo(video: self.videoPath , uploadProgress:  { (_, _) in
//                print(val)
            } , success: { (str, request, manager) in
                let video = message.video
                video?.url = str
                let newMsg = message
                newMsg.video = video
                self.uploadImage(image: [thumbImage] , message: newMsg, isFromVideo:true)
            }, failure: { (error) in
                print(error)
                message.isUploaded = false
                message.isFail = true
                let obj = ChatData.init(msgObj: message)
                self.delegate?.updateUploadingFail(with: obj)
            })

            
//            S3.uploadChatVideo(video: self.videoPath , uploadProgress: {  (val, valS) in
//                print(val)
//                }, success: { [weak self] (str, req, manager) in
//                    let video = message.video
//                    video?.url = str
//                    let newMsg = message
//                    newMsg.video = video
//                    self?.uploadImage(image: [thumbImage] , message: newMsg, isFromVideo:true)
//            }) { [weak self] (error) in
//                print(error)
//                message.isUploaded = false
//                message.isFail = true
//                let obj = ChatData.init(msgObj: message)
//                self?.delegate?.updateUploadingFail(with: obj)
//            }
        } catch {
            print(error)
        }
        
        
    }
    
    
    func sendOneToOneMsgToSocket(msgObj : ChatData? , imageUrl: String? = "" , videoUrl: String? = "", audioDuration: Int? = 0){
        var msgToSocket = [String: Any]()
        var receiversId : String?
        if let receiverData = receiverData as? UserList {
            receiversId =  receiverData.id
        }else if let receiverData = receiverData as? User {
            receiversId =  receiverData.id
        }
        switch MsgType(rawValue: /msgObj?.mesgDetail?.type) ?? .txt {
        case .img:
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.img.rawValue, "receiverId": /receiversId, "imageUrl": /imageUrl ]
        case .txt:
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.txt.rawValue,
                           "message": /msgObj?.mesgDetail?.message,  "receiverId": /receiversId]
        case .video:
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.video.rawValue,
                           "imageUrl": /(msgObj?.mesgDetail?.video?.imgUrl as? String) , "videoUrl" : /msgObj?.mesgDetail?.video?.url ,  "receiverId": /receiversId]
        case .gif:
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.gif.rawValue, "receiverId": /receiversId, "imageUrl": /imageUrl ]
            
        case .audio :
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.audio.rawValue, "receiverId": /receiversId, "audioUrl": /imageUrl, "audioDuration": /audioDuration]
            
            
            
        }
        
        SocketIOManager.shared.sendMessage(messageId: /msgObj?.mesgDetail?.messageID , data: msgToSocket) { [weak self] (status , convoId) in
            if status{
                self?.gotConvoId?(convoId)
                self?.delegate?.updateMessage(with: convoId)
            }
        }
    }
    
    func sendMessageInfo(msgObj : ChatData? , imageUrl: String? = "" , videoUrl: String? = "", audioDuration: Int? = 0){
        var msgToSocket = [String: Any]()
        var grpType = (typeOfChat == .venue) ? chatGroupType.venue.rawValue : chatGroupType.group.rawValue
        switch MsgType(rawValue: /msgObj?.mesgDetail?.type) ?? .txt {
        case .img:
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.img.rawValue, "groupType": /grpType,
                           "imageUrl": /imageUrl , "groupId": /msgObj?.mesgDetail?.groupId ]
        case .txt:
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.txt.rawValue, "groupType": /grpType,
                           "message": /msgObj?.mesgDetail?.message, "groupId": /msgObj?.mesgDetail?.groupId, "userIdTags": mentionedIdsTextView(),]
        case .video:
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.video.rawValue, "groupType": /grpType,
                           "imageUrl": /(msgObj?.mesgDetail?.video?.imgUrl as? String) , "videoUrl" : /msgObj?.mesgDetail?.video?.url , "groupId": /msgObj?.mesgDetail?.groupId]
        case .gif:
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.gif.rawValue, "groupType": /grpType,
                           "imageUrl": /imageUrl , "groupId": /msgObj?.mesgDetail?.groupId ]
        case .audio :
            msgToSocket = ["conversationId": /Singleton.sharedInstance.conversationId ,"senderId": /Singleton.sharedInstance.loggedInUser?.id, "type": MsgType.audio.rawValue, "groupType": /grpType,  "groupId": /msgObj?.mesgDetail?.groupId, "audioUrl": /imageUrl, "audioDuration": /audioDuration]
        }
        
        SocketIOManager.shared.sendMessage(messageId: /msgObj?.mesgDetail?.messageID ,data: msgToSocket) { [weak self] (status,convoId)  in
            if status{
                self?.delegate?.updateMessage(with: convoId)
            }
        }
    }
}

extension ChatAccessory {
    
    func mentionedIdsTextView() -> [String] {
        let mentions = self.mentionsInTextView()
        let users = selectedTagUsers.filter { (member) -> Bool in
            return mentions.contains(/member.user?.userName)
        }.map({"\(/$0.user?.id)"})
        selectedTagUsers = []
        return users
    }
    
    func mentionsInTextView() -> [String] {
        guard let textString = textView.text else { return []}
        let textLength = textString.utf16.count
        let textRange = NSRange(location: 0, length: textLength)
        let matches = RegexParser.getElements(from: textString, with: ActiveType.mention.pattern, range: textRange)
        let nsstring = textString as NSString
        var arr:[String] = []
        for match in matches where match.range.length > 2 {
            let word = nsstring.substring(with: match.range)
                .trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
            arr.append(word.replacingOccurrences(of: "@", with: ""))
        }
        return arr
    }
    
    func searchUserText() -> String {
        guard let range = textView.selectedTextRange else {
            return ""
        }
        let cursorPosition = textView.offset(from: textView.beginningOfDocument, to: range.start)
        let str = /textView.text
        let index = str.index(str.startIndex, offsetBy: cursorPosition)
        let textbefore = str[..<index]
        let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if hasTag?.hasPrefix("@") == true {
            return /hasTag?.replacingOccurrences(of: "@", with: "")
        }
        return ""
    }
    
    func setTag(member: Members) {
        guard let user = member.user else {
            return
        }
        guard let range = textView.selectedTextRange else {
            return
        }
        let cursorPosition = textView.offset(from: textView.beginningOfDocument, to: range.start)
        var str = /textView.text
        let index = str.index(str.startIndex, offsetBy: cursorPosition)
        let textbefore = str[..<index]
        let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if hasTag?.hasPrefix("@") == true {
            if !selectedTagUsers.contains(where: {$0.user?.userName == /member.user?.userName}) {
                selectedTagUsers.append(member)
            }
            let endIndex = str.index(str.startIndex, offsetBy: cursorPosition)
            var length = 0
            for character in String(textbefore).reversed() {
                if character == "@" {
                    break
                }
                length += 1
            }
            let startIndex = str.index(str.startIndex, offsetBy: cursorPosition - length)
            str.replaceSubrange(startIndex..<endIndex, with: /user.userName + " ")
            textView.text = str
        }
    }
    
    func showUsersTable() {
        delegate?.showUsersTable()
    }
    
    func hideUserstable() {
        delegate?.hideUsersTable()
    }
}

extension ChatAccessory: UITextViewDelegate {
    
    func textViewDidChangeSelection(_ textView: UITextView) {
        
        guard let range = textView.selectedTextRange else {
            return
        }
        btnChat.isHidden = textView.text.count == 0
        recordButton.isHidden = textView.text.count != 0
        recordView.isHidden = textView.text.count != 0
        let cursorPosition = textView.offset(from: textView.beginningOfDocument, to: range.start)
        let str = /textView.text
        if textView.text.contains("@"){
            let index = str.index(str.startIndex, offsetBy: cursorPosition)
            let textbefore = str[..<index]
            let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
            if hasTag?.hasPrefix("@") == true {
                showUsersTable()
            } else {
                hideUserstable()
            }
        }
    }
    
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        let hasTag = /textView.text.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if text == "@" || (hasTag.hasPrefix("@") && !(text == "" && hasTag.length == 1)){
            showUsersTable()
        }
        else {
            hideUserstable()
        }
        return true
    }
}

extension ChatAccessory: GiphyDelegate {
    
    func didSelectMedia(giphyViewController: GiphyViewController, media: GPHMedia)   {
        
        if let gifURL = media.url(rendition: .fixedWidth, fileType: .gif) as? String {
            if let url = URL(string: gifURL) {
                URLSession.shared.dataTask(with: url) { data, response, error in
                    if let data = data {
                        
                        let imgData = data.count
                        let mbs = /imgData.toDouble / (1048576.0)
                        if mbs > 25.0 {
                            UtilityFunctions.makeToast(text: "The file you have selected is too large. The maximum size is 25MB.", type: .error)
                            return
                        }
                        let imageModel = MessageImage.init(image: url.absoluteString)
                        
                        var msg = Message()
                        switch self.typeOfChat ?? .none {
                        case .venue:
                            msg = Message.init(msg: nil, msgtype: MsgType.img.rawValue, grpType: chatGroupType.venue.rawValue, grpId: self.groupId, image: imageModel, video: nil, messageID:UUID().uuidString)
                        case .oneToOne :
                            if let receiverData = self.receiverData as? UserList {
                                msg = Message(msg: nil, msgtype: MsgType.gif.rawValue,receiverId : receiverData.id ,image: imageModel , video: nil)
                            }else if let receiverData = self.receiverData as? User {
                                msg = Message(msg: nil, msgtype: MsgType.gif.rawValue,receiverId : receiverData.id ,image: imageModel , video: nil)
                            }
                        case .oneToMany:
                            msg = Message(msg: nil, msgtype: MsgType.gif.rawValue, grpType: chatGroupType.group.rawValue , grpId: self.groupId, image: imageModel , video: nil )
                            
                        default :
                            break
                        }
                        let obj = ChatData.init(msgObj: msg)
                        self.delegate?.appendNewMsg(msgObj: obj)
                        
                        self.uploadImageGif(data: data, message: msg)
                        
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

//MARK::- AUDIO RECORDING
extension ChatAccessory  : RecordViewDelegate , RecorderDelegate {
    func onStart() {
                
        vwChat.isHidden = true
        stackVw.isHidden = true
         playSoundEffect()
        startRecording()
    }
    
    func onCancel() {
        vwChat.isHidden = false
        stackVw.isHidden = false
       playSoundEffect()
        stop()
    }
    
    func onFinished(duration: CGFloat) {
        print("====" , duration)
        vwChat.isHidden = false
        stackVw.isHidden = false
        playSoundEffect()
        
        recording?.stop()

        if duration == 0.0 {
            return
        }
        
        
        let ll = Int(duration * 1000)
        let imageModel = MessageImage.init(audioUrl: recording?.url)
        var msg = Message()
        if let receiverData = receiverData as? UserList {
            msg = Message.init(msg: "", msgtype: MsgType.audio.rawValue, receiverId: receiverData.id, image: nil, video: nil, audioUrl: imageModel, messageID: UUID().uuidString, audioDuration: ll)

        }else if let receiverData = receiverData as? User {
            msg = Message(msg: nil, msgtype: MsgType.audio.rawValue,receiverId : receiverData.id ,image: nil , video: nil, audioUrl: imageModel ,messageID: UUID().uuidString, audioDuration: ll)
        }
        let obj = ChatData.init(msgObj: msg)
        self.delegate?.appendNewMsg(msgObj: obj)
        self.uploadAudio(data: recording?.url, message: msg)
        recordDuration = 0

        
        //{"chatDetails":{"audioUrl":{"original":"https://ribbitrewardsdev.s3.us-west-2.amazonaws.com/REC_a5f0553c-5ff3-4fd2-aade-fddc4eb75a3b.m4a"},"audioDuration":6420,"type":"AUDIO"}
        
        
    }
    
    open func createRecorder() {
        recording = Recording(to: "\(Date().timeIntervalSince1970)_recording.m4a")
        recording?.delegate = self
        DispatchQueue.global().async {
            // Background thread
            do {
                try self.recording?.prepare()
            } catch {
                print(error)
            }
        }
    }
    
    open func startRecording() {
        recordDuration = 0
        
        do {
            try recording?.record()
        } catch {
            print(error)
        }
    }
    
    func stop() {
        recordDuration = 0
        recording?.stop()
        //           voiceRecordHUD.update(0.0)
    }
    
    func audioMeterDidUpdate(_ db: Float) {
        
        self.recording?.recorder?.updateMeters()
        let ALPHA = 0.05
        let peakPower = pow(10, (ALPHA * Double((self.recording?.recorder?.peakPower(forChannel: 0))!)))
        var rate: Double = 0.0
        if (peakPower <= 0.2) {
            rate = 0.2
        } else if (peakPower > 0.9) {
            rate = 1.0
        } else {
            rate = peakPower
        }
        
        
        recordDuration = 12//Int(recordView.duration)
        print("recordDuration \(recordDuration)")
    }
}

extension ChatAccessory: AVAudioPlayerDelegate {
    
    func playSoundEffect() {
        guard let url = Bundle.main.url(forResource: "done-for-you", withExtension: "mp3") else { return }
        
        do {
            
            self.player = try AVAudioPlayer(contentsOf: url, fileTypeHint: AVFileType.mp3.rawValue)
            self.player?.delegate = self
            guard let aPlayer = self.player else { return }
            aPlayer.play()
            
        } catch let error {
            print(error.localizedDescription)
        }
        
    }
    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        
        self.player?.pause()
       
    }
}


extension PHAsset {

    var image : UIImage {
        var thumbnail = UIImage()
        let imageManager = PHCachingImageManager()
        imageManager.requestImage(for: self, targetSize: CGSize(width: 100, height: 100), contentMode: .aspectFit, options: nil, resultHandler: { image, _ in
            thumbnail = image!
        })
        return thumbnail
    }
}

