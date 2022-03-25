

import UIKit
import AVFoundation
import MobileCoreServices
import Photos
import EZSwiftExtensions

public enum MediaPickerControllerType {
    case imageOnly
    case imageAndVideo
}

@objc public protocol MediaPickerControllerDelegate {
    @objc optional func mediaPickerControllerDidPickImage(_ image: UIImage)
    @objc optional func mediaPickerControllerDidPickVideo(url: URL, data: Data, thumbnail: UIImage)
    @objc optional func mediaPickerControllerDidPickAsset(asset:PHAsset?)
    
}

open class MediaPickerController: NSObject {
    
    // MARK: - Public
    
    open weak var delegate: MediaPickerControllerDelegate?
    var isAllowExisting = true
    public init(type: MediaPickerControllerType, presentingViewController controller: UIViewController) {
        self.type = type
        self.presentingController = controller
        self.mediaPicker = UIImagePickerController()
        super.init()
        self.mediaPicker.delegate = self
    }
    
    open func show() {
        let actionSheet = self.optionsActionSheet
        self.presentingController.present(actionSheet, animated: true, completion: nil)
    }
    
    // MARK: - Private
    
    fileprivate let presentingController: UIViewController
    fileprivate let type: MediaPickerControllerType
    fileprivate let mediaPicker: UIImagePickerController
    
}

extension MediaPickerController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    // MARK: - UIImagePickerControllerDelegate
    
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        
        let mediaType = info[UIImagePickerController.InfoKey.mediaType] as! NSString
        
        if mediaType.isEqual(to: kUTTypeImage as NSString as String) {
            // Is Image
            let chosenImage = info[UIImagePickerController.InfoKey.originalImage] as! UIImage
            self.delegate?.mediaPickerControllerDidPickImage?(chosenImage)
            let asset = info[UIImagePickerController.InfoKey.phAsset] as? PHAsset
            self.delegate?.mediaPickerControllerDidPickAsset?(asset: asset)
        } else if mediaType.isEqual(to: kUTTypeMovie as NSString as String) {
            // Is Video
            let url: URL = info[UIImagePickerController.InfoKey.mediaURL] as! URL
            let chosenVideo = info[UIImagePickerController.InfoKey.mediaURL] as! URL
            let videoData = try! Data(contentsOf: chosenVideo, options: [])
            let thumbnail = url.generateThumbnail()
            self.delegate?.mediaPickerControllerDidPickVideo?(url: url, data: videoData, thumbnail: thumbnail)
            let asset = info[UIImagePickerController.InfoKey.phAsset] as? PHAsset
            self.delegate?.mediaPickerControllerDidPickAsset?(asset: asset)
        }
        self.dismiss()
    }
    
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        self.dismiss()
    }
    
    // MARK: - UINavigationControllerDelegate
    
    public func navigationController(_ navigationController: UINavigationController, willShow viewController: UIViewController, animated: Bool) {
        UIApplication.shared.statusBarStyle = .lightContent
    }
    
}

// MARK: - Private

private extension MediaPickerController {
    
    var optionsActionSheet: UIAlertController {
        let actionSheet = UIAlertController(title: Strings.Title, message: nil, preferredStyle: UIAlertController.Style.actionSheet)
        if isAllowExisting {
            self.addChooseExistingMediaActionToSheet(actionSheet)
        }
        
        
        if UIImagePickerController.isSourceTypeAvailable(.camera) {
            self.addTakePhotoActionToSheet(actionSheet)
            if self.type == .imageAndVideo {
                self.addTakeVideoActionToSheet(actionSheet)
            }
        }else{
            
        }
        self.addCancelActionToSheet(actionSheet)
        return actionSheet
    }
    
    func addChooseExistingMediaActionToSheet(_ actionSheet: UIAlertController) {
        let chooseExistingAction = UIAlertAction(title: self.chooseExistingText, style: UIAlertAction.Style.default) { (_) -> Void in
            let photos = PHPhotoLibrary.authorizationStatus()
            if photos == .notDetermined || photos == .denied{
                PHPhotoLibrary.requestAuthorization({status in
                    if status == .authorized{
                        
                        ez.runThisInMainThread { [weak self] in
                            self?.mediaPicker.sourceType = UIImagePickerController.SourceType.photoLibrary
                            self?.mediaPicker.mediaTypes = self?.chooseExistingMediaTypes ?? [kUTTypeImage as String]
                            self?.presentingController.present(self?.mediaPicker ?? UIViewController() , animated: true, completion: nil)
                        }
                    } else if status == .notDetermined{
                        
                    } else {
                        ez.runThisAfterDelay(seconds: 0.2, after: {
                            UtilityFunctions.show(alert: "", message: "You need to enable photos access from app settings", buttonOk: {
                                CheckPermission.shared.openAppSettings()
                            }, viewController: UIApplication.topViewController()! , buttonText: "OK")
                        })
                        
                        
                    }
                })
            }else{
                
                ez.runThisInMainThread { [weak self] in
                    self?.mediaPicker.sourceType = UIImagePickerController.SourceType.photoLibrary
                    self?.mediaPicker.mediaTypes = self?.chooseExistingMediaTypes ?? [kUTTypeImage as String]
                    self?.presentingController.present(self?.mediaPicker ?? UIViewController() , animated: true, completion: nil)
                }
            }
            
        }
        actionSheet.addAction(chooseExistingAction)
    }
    
    func addTakePhotoActionToSheet(_ actionSheet: UIAlertController) {
        let takePhotoAction = UIAlertAction(title: Strings.TakePhoto, style: UIAlertAction.Style.default) { (_) -> Void in
            self.mediaPicker.sourceType = UIImagePickerController.SourceType.camera
            self.mediaPicker.mediaTypes = [kUTTypeImage as String]
            self.presentingController.present(self.mediaPicker ?? UIViewController() , animated: true, completion: nil)
            //            ez.runThisInMainThread { [weak self] in
            //                self?.presentingController.present(self?.mediaPicker ?? UIViewController() , animated: true, completion: nil)
            //            }
            //            AVCaptureDevice.requestAccess(for: AVMediaType.video) { response in
            //                if response {
            //
            //
            //                } else {
            //                    ez.runThisAfterDelay(seconds: 2.0, after: {
            //                        UtilityFunctions.show(alert: "", message: "You need to enable camera access from app settings", buttonOk: {
            //                            CheckPermission.shared.openAppSettings()
            //                        }, viewController: UIApplication.topViewController()! , buttonText: "OK")
            //                    })
            //
            //                }
            //            }
            
        }
        actionSheet.addAction(takePhotoAction)
    }
    
    func addTakeVideoActionToSheet(_ actionSheet: UIAlertController) {
        let takeVideoAction = UIAlertAction(title: Strings.TakeVideo, style: UIAlertAction.Style.default) { (_) -> Void in
            self.mediaPicker.sourceType = UIImagePickerController.SourceType.camera
            self.mediaPicker.mediaTypes = [kUTTypeMovie as String]
            self.presentingController.present(self.mediaPicker ?? UIViewController() , animated: true, completion: nil)
            //            AVCaptureDevice.requestAccess(for: AVMediaType.video) { response in
            //                if response {
            //
            //                } else {
            //                    UtilityFunctions.show(alert: "", message: "You need to enable camera access from app settings", buttonOk: {
            //                        CheckPermission.shared.openAppSettings()
            //                    }, viewController: UIApplication.topViewController()! , buttonText: "OK")
            //                }
            //            }
            
        }
        actionSheet.addAction(takeVideoAction)
    }
    
    func addCancelActionToSheet(_ actionSheet: UIAlertController) {
        let cancel = Strings.Cancel
        let cancelAction = UIAlertAction(title: cancel, style: UIAlertAction.Style.cancel, handler: nil)
        actionSheet.addAction(cancelAction)
    }
    
    func dismiss() {
        DispatchQueue.main.async {
            self.presentingController.dismiss(animated: true, completion: nil)
        }
    }
    
    var chooseExistingText: String {
        switch self.type {
        case .imageOnly: return Strings.ChoosePhoto
        case .imageAndVideo: return Strings.ChoosePhotoOrVideo
        }
    }
    
    var chooseExistingMediaTypes: [String] {
        switch self.type {
        case .imageOnly: return [kUTTypeImage as String]
        case .imageAndVideo: return [kUTTypeImage as String, kUTTypeMovie as String]
        }
    }
    
    // MARK: - Constants
    
    struct Strings {
        static let Title = NSLocalizedString("Attach", comment: "Title for a generic action sheet for picking media from the device.")
        static let ChoosePhoto = NSLocalizedString("Choose photo from gallery", comment: "Text for an option that lets the user choose an existing photo in a generic action sheet for picking media from the device.")
        static let ChoosePhotoOrVideo = NSLocalizedString("Choose existing photo or video", comment: "Text for an option that lets the user choose an existing photo or video in a generic action sheet for picking media from the device.")
        static let TakePhoto = NSLocalizedString("Take a photo", comment: "Text for an option that lets the user take a picture with the device camera in a generic action sheet for picking media from the device.")
        static let TakeVideo = NSLocalizedString("Take a video", comment: "Text for an option that lets the user take a video with the device camera in a generic action sheet for picking media from the device.")
        static let Cancel = NSLocalizedString("Cancel", comment: "Text for the 'cancel' action in a generic action sheet for picking media from the device.")
    }
    
}

