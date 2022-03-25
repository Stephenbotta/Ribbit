//
//  CreatePostModal.swift
//  Conversify
//
//  Created by Apple on 15/11/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa
import Photos
import MobileCoreServices
import EZSwiftExtensions

class CreatePostViewModal: BaseRxViewModel {
    
    var postImage = Variable<UIImage?>(nil)
    var postImgeUrl = Variable<String?>(nil)
    var lat = Variable<String?>(nil)
    var long = Variable<String?>(nil)
    var locName = Variable<String?>(nil)
    var postText = Variable<String?>(nil)
    var locAddress = Variable<String?>(nil)
    var isImageSelected : Bool = false
    var isInsideGroup = false
    var helpBool = Variable<Bool?>(true)
    var postInfo  = Variable<PostList?>(nil)
    var isEditPost = false
    var assets = Variable<[Any?]>([])
    var assetsPH = Variable<[PHAsset?]>([])
    var media = Array<[String:String]>()
    var videoPath : URL?
    var uploadImageCount = 0
    
    var cameraImages = Variable<[UIImage?]>([])
    var videos =  Variable<[VideoData?]>([])
    
    override init() {
        super.init()
    }
    
    init(postInfo:PostList? ) {
        self.postInfo.value = postInfo
        self.assets.value = postInfo?.media ?? []
        self.isEditPost = true
    }
    
    var isValid: Observable<Bool> {
        return Observable.combineLatest(postText.asObservable(), helpBool.asObservable() , postImage.asObservable() ) { (postText,phone, image) in
            (self.isValidInformation(info: /postText)) || self.isValidImage()
        }
    }
    
    func isValidImage() -> Bool {
        if (postImage.value == nil && /postImgeUrl.value == ""){
            return false
        }
        return true
    }
    
    func uploadPost(postType: String? , postId : String? , grpId : String? , postText : String? , postOrgImg : String? , postThumbanilImg : String? , hashTag : String? ,_ completion:@escaping (Bool)->()){
        PostTarget.addEditPost(postId: postId, groupId: grpId, postText: postText, postImageVideoOriginal: postOrgImg, postImageVideoThumbnail: postThumbanilImg, hashTags: hashTag, locationLong: /long.value , locationLat: /lat.value , locationAddress: /locAddress.value , locationName:/locName.value, postType: /postType, media: /media.toJson() ).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                guard let safeResponse = response as? DictionaryResponse<GroupList> else{
                    return
                }
                
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
                    
            })<bag
    }
    
    func uploadImage(image: UIImage? , _ completion:@escaping (Bool)->()){
        var imageObj = [String:String]()
        S3.upload(image: image , success: { [weak self] (imageName) in
            print("========== image url =========" , imageName)
             ez.runThisInMainThread {
            imageObj["original"] = imageName
            imageObj["thumbnail"] = imageName
            imageObj["mediaType"] = "IMAGE"
            self?.media.append(imageObj)
            }
            completion(true)
        }) { [weak self] (error) in
            print(error)
        }
    }
    
    func postImages(postType: String? , postId : String? , grpId : String? , postText : String? , type : String? , hashTag : String? ,_ completion:@escaping (Bool)->()){
        
        
        if cameraImages.value.count == 0 {
            self.uploadImageCount = 0
            self.postCamVideos(postType: postType, postId: postId, grpId: grpId, postText: postText, type: type, hashTag: hashTag, completion)
        }else{
            uploadImage(image: cameraImages.value[uploadImageCount]) { [ weak self ] (status) in
                if /self?.uploadImageCount == /(/self?.cameraImages.value.count - 1) {
                    self?.uploadImageCount = 0
                    self?.postCamVideos(postType: postType, postId: postId, grpId: grpId, postText: postText, type: type, hashTag: hashTag, completion)
                }else{
                    self?.uploadImageCount = /self?.uploadImageCount + 1
                    self?.postImages(postType: postType, postId: postId, grpId: grpId, postText: postText, type: type, hashTag: hashTag, completion)
                }
                
            }
        }
        
        
    }
    
    func postCamVideos(postType: String? , postId : String? , grpId : String? , postText : String? , type : String? , hashTag : String? ,_ completion:@escaping (Bool)->()){
        
        if /videos.value.count == 0 {
            self.uploadPost(postType: postType , postId: postId, grpId: grpId, postText: postText, postOrgImg: /self.postImgeUrl.value == "" ? nil : self.postImgeUrl.value, postThumbanilImg:  /self.postImgeUrl.value == "" ? nil : self.postImgeUrl.value, hashTag: hashTag, { (finish) in
                completion(finish)
            })
        } else{
            postCameraVideosWithImages(data: videos.value[uploadImageCount]?.data, thumbnail: videos.value[uploadImageCount]?.thumbnail) { [ weak self ] (status) in
                if /self?.uploadImageCount == /(/self?.videos.value.count - 1) {
                    self?.uploadPost(postType: postType , postId: postId, grpId: grpId, postText: postText, postOrgImg: /self?.postImgeUrl.value == "" ? nil : self?.postImgeUrl.value, postThumbanilImg:  /self?.postImgeUrl.value == "" ? nil : self?.postImgeUrl.value, hashTag: hashTag, { (finish) in
                        completion(finish)
                    })
                    //                    self?.submitPost(postType: postType, postId: postId, grpId: grpId, postText: postText, type: type, hashTag: hashTag, completion)
                }else{
                    self?.uploadImageCount = /self?.uploadImageCount + 1
                    self?.postCamVideos(postType: postType, postId: postId, grpId: grpId, postText: postText, type: type, hashTag: hashTag, completion)
                }
                
            }
        }
    }
    
    func postCameraVideosWithImages(data:Data?, thumbnail:UIImage? , _ completion:@escaping (Bool)->() ){
        let videoData = data
        let path = try! FileManager.default.url(for: FileManager.SearchPathDirectory.documentDirectory, in: FileManager.SearchPathDomainMask.userDomainMask, appropriateFor: nil, create: false)
        let newPath = path.appendingPathComponent("/videoFileName.mp4")
        do {
            try videoData?.write(to: newPath)
            self.videoPath = newPath
            var imageObj = [String:String]()
            S3.uploadChatVideo(video: self.videoPath , uploadProgress: { [weak self] (val, valS) in
                print(val)
                }, success: { [weak self] (str, req, manager) in
                    
                    imageObj["videoUrl"] = str
                    imageObj["mediaType"] = "VIDEO"
                    guard let thumbImage = thumbnail as? UIImage else { return }
                    self?.upImageOfVideo(thumbImage: thumbImage, { (imageUrl) in
                         ez.runThisInMainThread {
                        imageObj["original"] = imageUrl
                        imageObj["thumbnail"] = imageUrl
                        self?.media.append(imageObj)
                        }
                        completion(true)
                    })
            }) { [weak self] (error) in
                print(error)
            }
        } catch {
            print(error)
        }
    }
    
    func submitPost(postType: String? , postId : String? , grpId : String? , postText : String? , type : String? , hashTag : String? ,_ completion:@escaping (Bool)->()){
        
        Loader.shared.start()
        
        assetsPH.value = []
        cameraImages.value = []
        videos.value = []
        assets.value.forEach({ (element) in
            if let elem = element as? PHAsset{
                assetsPH.value.append(elem)
            }else if let video = element as? VideoData{
                videos.value.append(video)
            }else if let img = element as? UIImage{
                cameraImages.value.append(img)
            }else if let media = element as? Media{
                switch media.mediaType {
                case "VIDEO":
                    var imageObj = [String:String]()
                    imageObj["original"] =  media.original
                    imageObj["thumbnail"] =  media.thumbnail
                    imageObj["videoUrl"] =  media.videoUrl
                    imageObj["mediaType"] = /media.mediaType
                    self.media.append(imageObj)
                case "IMAGE":
                    var imageObj = [String:String]()
                    imageObj["original"] = media.original
                    imageObj["thumbnail"] =  media.thumbnail
                    imageObj["mediaType"] = /media.mediaType
                    self.media.append(imageObj)
                case "GIF":
                    var imageObj = [String:String]()
                    imageObj["original"] =  media.original
                    imageObj["thumbnail"] =  media.thumbnail
                    imageObj["mediaType"] = /media.mediaType
                    self.media.append(imageObj)
                default:
                    break
                }
                
            }
        })
        
        if /assets.value.count != 0 {
            
            if assetsPH.value.count == 0 {
                self.uploadImageCount = 0
                self.postImages(postType: postType, postId: postId, grpId: grpId, postText: postText, type: type, hashTag: hashTag, completion)
            }else{
                upAssetsPH(postType: postType, postId: postId, grpId: grpId, postText: postText, type: type, hashTag: hashTag, completion)
            }
            
        }else {
            self.uploadPost(postType: postType , postId: postId, grpId: grpId, postText: postText, postOrgImg: /self.postImgeUrl.value == "" ? nil : self.postImgeUrl.value, postThumbanilImg:  /self.postImgeUrl.value == "" ? nil : self.postImgeUrl.value, hashTag: hashTag, { (finish) in
                completion(finish)
            })
        }
    }
    
    func upAssetsPH(postType: String? , postId : String? , grpId : String? , postText : String? , type : String? , hashTag : String? ,_ completion:@escaping (Bool)->()){
        uploadImage(asst: assetsPH.value[uploadImageCount]) { [ weak self ] (status) in
            if /self?.uploadImageCount == /(/self?.assetsPH.value.count - 1) {
                self?.uploadImageCount = 0
                self?.postImages(postType: postType, postId: postId, grpId: grpId, postText: postText, type: type, hashTag: hashTag, completion)
                //
            }else{
                self?.uploadImageCount = /self?.uploadImageCount + 1
                self?.upAssetsPH(postType: /postType , postId : /postId , grpId : /grpId , postText : /postText , type : /type , hashTag : /hashTag ,{ (finish) in
                    completion(finish)
                })
            }
            
        }
    }
    
    func uploadImage(asst: PHAsset? , _ completion:@escaping (Bool)->()){
        guard let asst = asst else { return }
        Loader.shared.start()
        if asst.mediaType == PHAssetMediaType.image{
            //            if(asst.mediaSubtypes == PHAssetMediaSubtype.photoPanorama){
            //                // this is a Live Photo
            //            }
            //
            //            if #available(iOS 9.1, *) {
            //                if(asst.mediaSubtypes == PHAssetMediaSubtype.photoLive){
            //                    // this is a Time-lapse
            //                }
            //            } else {
            //                // Fallback on earlier versions
            //            }
            //
            //            if(asst.mediaSubtypes == PHAssetMediaSubtype.videoTimelapse){
            //
            //            }
            
            let valAsset = asst
            
            let requestOptions = PHImageRequestOptions()
            requestOptions.isSynchronous = true // adjust the parameters as you wish
            let option = PHImageRequestOptions()
            option.isSynchronous = false
            option.isNetworkAccessAllowed = true
            option.resizeMode = .exact
            option.deliveryMode = .highQualityFormat
            option.version = .original
            PHImageManager.default().requestImageData(for: valAsset, options: option, resultHandler: { (imageData, UTI, _, _) in
                if let uti = UTI,let _ = imageData , UTTypeConformsTo(uti as CFString, kUTTypeGIF) {
                    var imageObj = [String:String]()
                    S3.uploadGif(gifData: imageData , success: { [weak self] (imageName) in
                         ez.runThisInMainThread {
                        print(imageName)
                        imageObj["original"] = imageName
                        imageObj["thumbnail"] = imageName
                        imageObj["mediaType"] = "GIF"
                        self?.media.append(imageObj)
                        }
                        completion(true)
                    }) { [weak self] (error) in
                        print(error)
                        Loader.shared.stop()
                    }
                }else{
                    var imageObj = [String:String]()
                    S3.upload(image: asst.getAssetThumbnail(asset: asst) , success: { [weak self] (imageName) in
                        ez.runThisInMainThread {
                            print(imageName)
                            imageObj["original"] = imageName
                            imageObj["thumbnail"] = imageName
                            imageObj["mediaType"] = "IMAGE"
                            self?.media.append(imageObj)
                            
                        }
                            completion(true)
                        
                        
                    }) { [weak self] (error) in
                        print(error)
                        Loader.shared.stop()
                    }
                }
            })
            
            
            
        }else if asst.mediaType == PHAssetMediaType.video{
            let phAsset = asst
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
                        Loader.shared.stop()
                        return
                    }
                    var imageObj = [String:String]()
                    let videoModel = MessageVideo.init(url: asset?.url.absoluteString, data: data, thumb: (asset?.url)!.generateThumbnail())
                    
                    let videoData = videoModel.data
                    let path = try! FileManager.default.url(for: FileManager.SearchPathDirectory.documentDirectory, in: FileManager.SearchPathDomainMask.userDomainMask, appropriateFor: nil, create: false)
                    let newPath = path.appendingPathComponent("/videoFileName.mp4")
                    do {
                        try videoData?.write(to: newPath)
                        self.videoPath = newPath
                        S3.uploadChatVideo(video: self.videoPath , uploadProgress: { [weak self] (val, valS) in
                            print(val)
                            }, success: { [weak self] (str, req, manager) in
                                
                                imageObj["videoUrl"] = str
                                imageObj["mediaType"] = "VIDEO"
                                guard let thumbImage = videoModel.thumbnail as? UIImage else { return }
                                self?.upImageOfVideo(thumbImage: thumbImage, { (imageUrl) in
                                     ez.runThisInMainThread {
                                    imageObj["original"] = imageUrl
                                    imageObj["thumbnail"] = imageUrl
                                    self?.media.append(imageObj)
                                    }
                                    completion(true)
                                })
                        }) { [weak self] (error) in
                            print(error)
                            Loader.shared.stop()
                        }
                    } catch {
                        print(error)
                        Loader.shared.stop()
                    }
                } catch _ { }
            })
            
        }
        
    }
    
    func upImageOfVideo(thumbImage:UIImage , _ completion:@escaping (String)->()){
        S3.upload(image: thumbImage, success: { (thumb) in
            completion(thumb)
        }, failure: { (str) in
            print(str)
            Loader.shared.stop()
        })
    }
    
    
    func uploadImage(_ completion:@escaping (Bool)->()){
        Loader.shared.start()
        S3.upload(image: postImage.value , success: { [weak self](imageName) in
            print(imageName)
            ez.runThisInMainThread {
                self?.postImgeUrl.value = imageName
            }
            completion(true)
        }) { (error) in
            Loader.shared.stop()
            print(error)
        }
    }
}
