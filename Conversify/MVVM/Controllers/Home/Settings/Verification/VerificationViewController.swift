//
//  VerificationViewController.swift
//  Conversify
//
//  Created by Harminder on 08/01/19.
//

import UIKit
import IBAnimatable
import DBAttachmentPickerController
import EZSwiftExtensions

class VerificationViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnUploadDoc: AnimatableButton!
    @IBOutlet weak var btnBack: AnimatableButton!
    @IBOutlet weak var imageVerifyEmail: UIImageView!
    @IBOutlet weak var imageVerifyMb: UIImageView!
    @IBOutlet weak var imageVerifyDoc: UIImageView!
    @IBOutlet weak var btnVerifyMobile: AnimatableButton!
    @IBOutlet weak var btnVerifyEmail: AnimatableButton!
    @IBOutlet weak var labelPhone: UILabel!
    @IBOutlet weak var labelEmail: UILabel!
    @IBOutlet weak var indicatorUpload: UIActivityIndicatorView!
    
    
    //MARK::- PROPERTIES
    var verifVM = VerificationViewModal()
    var mediaPicker: DBAttachmentPickerController?
    var mediaPickerVC: MediaPickerController?
    
    //MARK::- VIEW CYCLE
    
    override func viewWillAppear(_ animated: Bool) {
        onAppear()
        
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        verifVM.verificationInfo.value = Singleton.sharedInstance.loggedInUser
        updateUI()
        
        labelEmail.text = /Singleton.sharedInstance.loggedInUser?.email
        labelPhone.text = /Singleton.sharedInstance.loggedInUser?.countryCode + /Singleton.sharedInstance.loggedInUser?.phoneNumber
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
        verifVM.update.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.updateUI()
                }
            })<bag
        
        btnVerifyEmail.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.verifVM.email.value = /Singleton.sharedInstance.loggedInUser?.email
            self?.verifVM.getDetails()
        })<bag
        
        btnUploadDoc.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.configurePickr()
        })<bag
        
        btnVerifyMobile.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let verifyOtp = R.storyboard.main.verifyOTPViewController() else { return }
            verifyOtp.otpVM = VerifyOTPViewModal(user: Singleton.sharedInstance.loggedInUser, isVerificationProcedure: true)
            verifyOtp.otpVM.verified = { [ weak self] in
                self?.updateUI()
            }
            self?.present(verifyOtp, animated: true, completion: nil)
        })<bag
        
    }
    
    func onAppear(){
        btnVerifyEmail.setTitle(/Singleton.sharedInstance.loggedInUser?.isEmailVerified ? "Email address verified" : "Click here to verify email" , for:  .normal)
        btnVerifyMobile.setTitle(/Singleton.sharedInstance.loggedInUser?.isPhoneNumberVerified ? "Mobile number verified" : "Click here to verify mobile number" , for:  .normal)
    }
    
    func updateUI(){
        imageVerifyEmail.isHidden = !(/verifVM.verificationInfo.value?.isEmailVerified)
        imageVerifyMb.isHidden = !(/verifVM.verificationInfo.value?.isPhoneNumberVerified)
        imageVerifyDoc.isHidden = !(/verifVM.verificationInfo.value?.isUploaded)
        btnVerifyEmail.isEnabled = !(/verifVM.verificationInfo.value?.isEmailVerified)
        btnVerifyMobile.isEnabled = !(/verifVM.verificationInfo.value?.isPhoneNumberVerified)
        onAppear()
    }
    
}

//MARK: - Media Picker Delegate
extension VerificationViewController {
    
    //DBATTACHMENT PICKER
    
    func configurePickr(){
        self.btnUploadDoc.isEnabled = false
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
                                        self.btnUploadDoc.isEnabled = true
                                        self.indicatorUpload.stopAnimating()
                                        CheckPermission.shared.openAppSettings()
                                        return
                                    }, buttonCancel: { [weak self] in
                                        self?.configProceed(isVideo: true, isImage: false, isBoth: false)
                                        }, viewController: UIApplication.topViewController()!, buttonText: "Yes", cancelButtonText: "No")
                                })
                                
                                
                            }
                        })
                    }else{
                        self.configProceed(isVideo: false, isImage: true, isBoth: false)
                    }
                    
                } else {//false
                    //request access
                    ez.runThisAfterDelay(seconds: 1.0, after: {
                        UtilityFunctions.show(alert: "", message: "You need to enable camera access from app settings to share image from camera", buttonOk: {
                            self.btnUploadDoc.isEnabled = true
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
                                                self?.btnUploadDoc.isEnabled = true
                                                self?.indicatorUpload.stopAnimating()
                                                CheckPermission.shared.openAppSettings()
                                                return
                                            }, buttonCancel: { [weak self] in
                                                self?.btnUploadDoc.isEnabled = true
                                                self?.indicatorUpload.stopAnimating()
                                                self?.configProceed(isVideo: false, isImage: false, isBoth: false)
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
    
    func configProceed(isVideo: Bool?, isImage: Bool?, isBoth: Bool?){
        
        mediaPicker = DBAttachmentPickerController.init(finishPicking: { (attachement) in
            self.mediaPicked(mediaArray: attachement)
        }, cancel: {
            
        })
        if /isVideo {
            mediaPicker?.mediaType = [   .image , .video , .other]
            openDBAttachmentMediaPicker()
        }
        if /isImage {
            openCustomPhotosPicker()
        }
        if /isBoth {
            mediaPicker?.mediaType = [.image  , .video , .other]
            openDBAttachmentMediaPicker()
        }
        
        if !(/isVideo) && !(/isImage) && !(/isBoth) {
            mediaPicker?.mediaType = [ .other]
            openDBAttachmentMediaPicker()
        }
        
    }
    
    func openCustomPhotosPicker(){
        view.endEditing(true)
        btnUploadDoc.isEnabled = true
        indicatorUpload.stopAnimating()
        ez.runThisAfterDelay(seconds: 2.0, after: {
            UtilityFunctions.show(nativeActionSheet: "Select option", subTitle: "", vc: self, senders: ["Select photo", "Select document"], success: { [unowned self] (value, index) in
                switch index{
                case 0:
                    self.view.endEditing(true)
                    self.openMediaPicker()
                default:
                    self.mediaPicker?.mediaType = [ .other]
                    self.openDBAttachmentMediaPicker()
                }
            })
        })
    }
    
    func openDBAttachmentMediaPicker(){
        mediaPicker?.allowsMultipleSelection = false
        mediaPicker?.allowsSelectionFromOtherApps = true
        ez.runThisInMainThread { [ unowned self] in
            self.btnUploadDoc.isEnabled = true
            self.indicatorUpload.stopAnimating()
            guard let vc = UIApplication.topViewController() else { return }
            self.mediaPicker?.present(on: vc.self)
        }
    }
    
    
    func mediaPicked(mediaArray: [DBAttachment]) {
        
        guard let type = mediaArray.first?.mediaType else { return }
        switch type {
        case .other:
            let url = URL(fileURLWithPath: /(mediaArray.first?.originalFileResource() as? String))
            
            let documentsDirectoryURL = try! FileManager().url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            
            let fileURL = documentsDirectoryURL.appendingPathComponent("doc_\(arc4random()).pdf")
            do {
                try FileManager.default.copyItem(at: URL(fileURLWithPath: url.path), to: fileURL)
            }
            catch {
                print(error)
            }
            self.verifVM.documentUrl.value = fileURL
            self.verifVM.uploadDocuments()
            
        case .image:
            mediaArray.first?.loadOriginalImage(completion: { (img) in
                self.verifVM.docImage.value = img
                self.verifVM.uploadImage()
            })
            
        default :
            break
        }
    }
    
    
}

extension VerificationViewController : MediaPickerControllerDelegate {
    
    
    /** Pick photo **/
    func openMediaPicker() {
        self.view.endEditing(true)
        btnUploadDoc.isEnabled = true
        indicatorUpload.stopAnimating()
        mediaPickerVC = MediaPickerController(type: .imageOnly, presentingViewController: self)
        mediaPickerVC?.delegate = self
        mediaPickerVC?.show()
    }
    
    func mediaPickerControllerDidPickImage(_ image: UIImage) {
        self.verifVM.docImage.value = image
        self.verifVM.uploadImage()
    }
    
}
