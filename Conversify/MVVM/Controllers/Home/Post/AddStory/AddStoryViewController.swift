//
//  AddStoryViewController.swift
//  Conversify
//
//  Created by admin on 02/04/21.
//

import UIKit
import OpalImagePicker
import Photos
import moa
class AddStoryViewController: BaseRxViewController, MediaPickerControllerDelegate {

    @IBOutlet weak var btnpost: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    
    
    @IBOutlet weak var imgProfile: UIImageView!
    @IBOutlet weak var lbluserName: UILabel!
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var btnCamera: UIButton!

    var username = ""
    var profilePic : UIImage?
    var mediaPickerVC: MediaPickerController?
    var addStoriVM = AddStoryModel()
   
    override func viewDidLoad() {
        super.viewDidLoad()
        collectionView.layer.cornerRadius = 4
        collectionView.clipsToBounds = true
        imgProfile.layer.cornerRadius = 25
        imgProfile.clipsToBounds = true
        
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        let user = Singleton.sharedInstance.loggedInUser
        imgProfile.image(url: /user?.img?.original)
        lbluserName.text = user?.userName
    }
    override func bindings(){
        
        addStoriVM.createdSuccessFully.filter { (_) -> Bool in
            return true
        }.subscribe(onNext: { [weak self] (bool) in
            if bool {
                self?.popToHome()
            }
        })<bag
    btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
        self?.view.endEditing(true)
        self?.popVC()
    })<bag
        
        
        btnpost.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            
            if /self?.addStoriVM.assets.value.count > 0 {
                
            }else{
                UtilityFunctions.makeToast(text: "Media count should be greater than 0 ", type: .info)
                return
            }
         
            Loader.shared.start()
            self?.addStoriVM.post()
            
        })<bag

    btnCamera.rx.tap.asDriver().drive(onNext:{  [weak self] () in
        DispatchQueue.main.async {
            CheckPermission.shared.permission(.camera, completion: { (status) in
                if status{
                    CheckPermission.shared.permission(.photos, completion: { (status) in
                        if status{
                            self?.openMediaPicker(isCam: true, isGal: true)
                        }else{
                            self?.openMediaPicker(isCam: true, isGal: false)
                        }
                    })
                }else{
                    CheckPermission.shared.permission(.photos, completion: { (status) in
                        if status{
                            self?.openMediaPicker(isCam: false, isGal: true)
                        }else{
                            return
                        }
                    })
                }
                
            })
        }
        
    })<bag
    
        
        
        
        
        addStoriVM.assets
            .asObservable()
            .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.selectedImageCollectionViewCell.identifier, cellType: SelectedImageCollectionViewCell.self)) { [weak self] (row,element,cell) in
                cell.tableRow = row
                cell.isFromPost = true
                if let elem = element as? PHAsset{
                    cell.asset = elem
                }
//                else if let video = element as? VideoData{
//                    cell.imageSelected?.image = video.thumbnail
//                    cell.imageVideoPlay?.isHidden = false
//                }else if let media = element as? Media{
//                    cell.imageSelected?.setImage(image: media.thumbnail)
//                    cell.imageVideoPlay?.isHidden = media.mediaType != "VIDEO"
//                }else{
//                    cell.imageVideoPlay?.isHidden = true
//                    cell.imageSelected?.image = element as? UIImage
//                }
                cell.imageCross?.tag = row
                cell.imageCross?.onTap({ (gesture) in
                    self?.addStoriVM.assets.value.remove(at: /cell.imageCross?.tag)
                })
            }<bag
        collectionView.rx.setDelegate(self)<bag
        
      
        
        
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
            UtilityFunctions.show(nativeActionSheet: "", subTitle: "", vc: self, senders: senders) { [unowned self] (value, index) in
                switch /(value as? String){
                case "Camera":
                    self.mediaPickerVC = MediaPickerController(type: .imageAndVideo , presentingViewController: self)
                    self.mediaPickerVC?.isAllowExisting = false
                    self.mediaPickerVC?.delegate = self
                    self.mediaPickerVC?.show()
                default:
                    self.presentOpal()
                }
            }
        }
    }
  func presentOpal()  {
        let imagePicker = OpalImagePickerController()
        imagePicker.allowedMediaTypes = Set([PHAssetMediaType.image , PHAssetMediaType.video])
        presentOpalImagePickerController(imagePicker, animated: true, select: { [weak self] (assets)  in
            //Save Images, update UI
            print(assets)
            assets.forEach({ (asset) in
                //               
                self?.addStoriVM.assets.value.append(asset)
            })
            
            assets.forEach({ (asst) in
            
                if asst.mediaType == PHAssetMediaType.image{
                    if(asst.mediaSubtypes == PHAssetMediaSubtype.photoPanorama){
                        // this is a Live Photo
                    }
                    
                    if #available(iOS 9.1, *) {
                        if(asst.mediaSubtypes == PHAssetMediaSubtype.photoLive){
                            // this is a Time-lapse
                        }
                    } else {
                        // Fallback on earlier versions
                    }
                    
                    if(asst.mediaSubtypes == PHAssetMediaSubtype.videoTimelapse){
                        
                    }
                }else if asst.mediaType == PHAssetMediaType.video{
                    
                } else if asst.mediaType == PHAssetMediaType.audio{
                    
                }
            })
            
            //Dismiss Controller
            imagePicker.dismiss(animated: true, completion: nil)
            }, cancel: {
                //Cancel action?
                
        })
    }
    
    func popVCon(){
        self.popVC()
    }
    
}

extension AddStoryViewController: UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: 80 , height:80)
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 4
    }
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    //    addStoriVM.assets.value.remove(at: indexPath.row)
    }
}
