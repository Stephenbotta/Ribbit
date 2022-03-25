//
//  CreatePostViewController.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import UIKit
import EZSwiftExtensions
import OpalImagePicker
import Photos

enum postType : String {
    case img = "IMAGE"
    case video = "VIDEO"
    case none = "none"
}

class CreatePostViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnPost: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var txtfPost: UITextView!
    @IBOutlet weak var imgPostView: UIImageView!
    @IBOutlet weak var btnCamera: UIButton!
    @IBOutlet weak var lblGroupName: UILabel!
    @IBOutlet weak var imgGroupView: UIImageView!
    @IBOutlet weak var btnDelete: UIButton!
    @IBOutlet weak var viewGroupDetail: UIView!
    @IBOutlet weak var labelLocationName: UILabel!
    @IBOutlet weak var btnSelectLocation: UIButton!
    @IBOutlet weak var collectionView: UICollectionView!
    
    //MARK::- PROPERTIES
    var mediaPickerVC: MediaPickerController?
    var createPostModal = CreatePostViewModal()
    var isPostingInGroup : Bool = false
    var selectedGroup : GroupList?
    var selectedPostType : postType = .none
    let imagePicker = OpalImagePickerController()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    override func bindings() {
        createPostModal.assets
            .asObservable()
            .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.selectedImageCollectionViewCell.identifier, cellType: SelectedImageCollectionViewCell.self)) { [weak self] (row,element,cell) in
                cell.tableRow = row
                if let elem = element as? PHAsset{
                    cell.asset = elem
                }else if let video = element as? VideoData{
                    cell.imageSelected?.image = video.thumbnail
                    cell.imageVideoPlay?.isHidden = false
                }else if let media = element as? Media{
                    cell.imageSelected?.setImage(image: media.thumbnail)
                    cell.imageVideoPlay?.isHidden = media.mediaType != "VIDEO"
                }else{
                    cell.imageVideoPlay?.isHidden = true
                    cell.imageSelected?.image = element as? UIImage
                }
                
                cell.imageCross?.tag = row
                cell.imageCross?.onTap({ (gesture) in
                    self?.createPostModal.assets.value.remove(at: /cell.imageCross?.tag)
                })
            }<bag
        
        collectionView.rx.setDelegate(self)<bag
        (txtfPost.rx.text <-> createPostModal.postText)<bag
        createPostModal.isValid.subscribe { [unowned self] (valid) in
            self.btnPost.isEnabled = /valid.element
            self.btnPost.alpha = /valid.element ? 1.0 : 0.4
            }<bag
        btnSelectLocation.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.getPlaceDetails()
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
        
        //        btnDelete.rx.tap.asDriver().drive(onNext: {  [weak self] () in
        //            if /self?.createPostModal.isImageSelected {
        //                self?.imgPostView.contentMode = .center
        //                self?.imgPostView.image = R.image.ic_camera()
        //                self?.createPostModal.postImage.value = nil
        //                self?.createPostModal.postImgeUrl.value = ""
        //                self?.btnDelete.setImage(nil, for: .normal)
        //                self?.createPostModal.isImageSelected = false
        //            }else {
        //                self?.openMediaPicker()
        //            }
        //        })<bag
        //
        btnPost.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            if self?.createPostModal.assets.value.count != 0 && /self?.createPostModal.assets.value.count <= 4 {

            }else{
                UtilityFunctions.makeToast(text: "Media count should not be less than 5", type: .error)
                return
            }
            self?.txtfPost.endEditing(true)
            let hashtags = self?.txtfPost.text.trimmed().hashtags()
            
            self?.createPostModal.submitPost(postType: /self?.createPostModal.postInfo.value?.postType == "" ? "REGULAR" : /self?.createPostModal.postInfo.value?.postType , postId: /self?.createPostModal.postInfo.value?.id == "" ? nil : /self?.createPostModal.postInfo.value?.id , grpId: /self?.isPostingInGroup ? self?.selectedGroup?.id : nil, postText: self?.txtfPost.text.trimmed(), type: self?.selectedPostType.rawValue, hashTag: (hashtags?.count == 0) ? nil : hashtags?.toJson(), { (status) in
                if status {
                    self?.popToHomeFeedVC()
                    UtilityFunctions.makeToast(text: "Post uploaded successfully", type: .success)
                }
            })
        })<bag
        
        btnCamera.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            //            if /self?.btnSelectImage.isSelected{
            //                self?.imageSelected.image = UIImage()
            //                self?.converseVM.postImage.value = nil
            //            }else{
            self?.view.endEditing(true)
            
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
            
            
            
            //            }
            
            
        })<bag
        
    }
    
    func popToHomeFeedVC(){
        if /createPostModal.isInsideGroup{
            self.popVC()
        }else{
            for controller in (self.navigationController?.viewControllers) ?? [UIViewController()] {
                if controller is  OnboardTabViewController {
                    (controller as? OnboardTabViewController)?.updatePostList()
                    self.navigationController?.popToViewController(controller, animated: true)
                    break
                }
            }
        }
    }
    
    func onLoad(){
        imagePicker.allowedMediaTypes = Set([PHAssetMediaType.image , PHAssetMediaType.video])
        txtfPost.delegate = self
        viewGroupDetail.isHidden = !isPostingInGroup
        lblGroupName.text = selectedGroup?.groupName?.capitalizedFirst()
        imgGroupView.image(url:  /selectedGroup?.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /selectedGroup?.imageUrl?.thumbnail))
        pickedLocation = { [weak self] ( lat , long , name , adddress) in
            self?.createPostModal.lat.value = lat
            self?.createPostModal.long.value = long
            self?.createPostModal.locName.value = /name.uppercaseFirst
            self?.createPostModal.locAddress.value = /adddress.uppercaseFirst
            self?.labelLocationName.text = /lat == "" ? "Check in" : (/name.uppercaseFirst + " " + /adddress.uppercaseFirst)
        }
        
        if self.createPostModal.isEditPost {
            if /lblGroupName?.text != ""{
                viewGroupDetail?.isHidden = false
            }
            lblGroupName?.text = createPostModal.postInfo.value?.groupDetail?.groupName
            txtfPost?.text = createPostModal.postInfo.value?.postText
            createPostModal.postText.value = createPostModal.postInfo.value?.postText
            if /createPostModal.postInfo.value?.groupDetail?.imageUrl?.thumbnail != ""{
                imgGroupView.image(url:  /createPostModal.postInfo.value?.groupDetail?.imageUrl?.thumbnail, placeholder:#imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /createPostModal.postInfo.value?.groupDetail?.imageUrl?.thumbnail))
            }
            if /createPostModal.postInfo.value?.media?.count  != 0{
                collectionView.reloadData()
//                imgPostView.kf.setImage(with: URL(string: /createPostModal.postInfo.value?.imageUrl?.thumbnail))
//                self.createPostModal.postImgeUrl.value = /createPostModal.postInfo.value?.imageUrl?.original
//                createPostModal.isImageSelected = true
//                btnDelete.isHidden = false
//                btnDelete.setImage(R.image.ic_delete(), for: .normal)
                
                
                
            }
            labelLocationName.text = /createPostModal.postInfo.value?.locationAddress == "" ? "Check in" : /createPostModal.postInfo.value?.locationAddress
            
            //for location
            createPostModal.lat.value = String(createPostModal.postInfo.value?.location?.last ?? 0)
            createPostModal.long.value = String(createPostModal.postInfo.value?.location?.first ?? 0)
            createPostModal.locName.value = /createPostModal.postInfo.value?.locationName
            createPostModal.locAddress.value = /createPostModal.postInfo.value?.locationAddress
            
        }
        
        
    }
    
}


extension CreatePostViewController : MediaPickerControllerDelegate {
    
    /** Pick photo **/
    func openMediaPicker(isCam:Bool , isGal:Bool) {
        
        var senders = [String]()
        if isCam{
            senders.append("Camera")
        }
        if isGal{
            senders.append("Gallery")
            
        }
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
    
    func presentOpal(){
        presentOpalImagePickerController(imagePicker, animated: true, select: { [weak self] (assets)  in
            //Save Images, update UI
            print(assets)
            assets.forEach({ (asset) in
//                self?.createPostModal.assetsPH.value.append(asset)
                self?.createPostModal.assets.value.append(asset)
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
            self?.imagePicker.dismiss(animated: true, completion: nil)
            }, cancel: {
                //Cancel action?
                
        })
    }
    
    
    func mediaPickerControllerDidPickImage(_ image: UIImage) {
//        createPostModal.cameraImages.value.append(image)
        createPostModal.assets.value.append(image)
    }
    
    func mediaPickerControllerDidPickVideo(url: URL, data: Data, thumbnail: UIImage){
        let video = VideoData()
        video.thumbnail = thumbnail
        video.data = data
        createPostModal.assets.value.append(video)
//        createPostModal.videos.value.append(video)
    }
    
    
}


extension CreatePostViewController : UITextViewDelegate {
    
    
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

//MARK: - Collection View Delegates
extension CreatePostViewController: UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
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
    
}
