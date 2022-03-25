//
//  AddStoryViewContrllerModel.swift
//  Conversify
//
//  Created by admin on 05/04/21.
//
//import UIKit
//import RxSwift
//import Foundation
//import Photos

import UIKit
import RxSwift
import Foundation
import RxCocoa
import GooglePlacePicker
import Photos
import AVKit
import MobileCoreServices
import EZSwiftExtensions
import EZSwiftExtensions
class AddStoryModel : BaseRxViewModel{
    var postImage = Variable<UIImage?>(nil)
    var assets = Variable<[Any?]>([])
    var media = Array<[String:String]>()
    var assetsPH = Variable<[PHAsset?]>([])
    var cameraImages = Variable<[UIImage?]>([])
    var videos =  Variable<[VideoData?]>([])
    var resetButton = PublishSubject<Bool>()
    var createdSuccessFully = PublishSubject<Bool>()
   
    var uploadImageCount = 0
    var videoPath : URL?
    
    
    func createPost(){
        print(media)
        ProfileTarget.addStory(media: media.toJson()).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.createdSuccessFully.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.createPost()
                    self?.resetButton.onNext(true)
            })<bag
    }
    
    func postImages(){
        if cameraImages.value.count == 0 {
            self.uploadImageCount = 0
            self.postCamVideos()
        }else{
            uploadImage(asst: assetsPH.value[uploadImageCount]) { [ weak self ] (status) in
                if /self?.uploadImageCount == /(/self?.cameraImages.value.count - 1) {
                    self?.uploadImageCount = 0
                    self?.postCamVideos()
                }else{
                    self?.uploadImageCount = /self?.uploadImageCount + 1
                    self?.postImages()
                }
            }
        }
    }
    func postCamVideos(){
        
        if videos.value.count == 0 {
            self.createPost()
        }else{
            postCameraVideosWithImages(data: videos.value[uploadImageCount]?.data, thumbnail: videos.value[uploadImageCount]?.thumbnail) { [ weak self ] (status) in
                if /self?.uploadImageCount == /(/self?.videos.value.count - 1) {
                    self?.createPost()
                }else{
                    self?.uploadImageCount = /self?.uploadImageCount + 1
                    self?.postCamVideos()
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
                        imageObj["original"] = imageUrl
                        imageObj["thumbnail"] = imageUrl
                        self?.media.append(imageObj)
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
    }
    
    func upImageOfVideo(thumbImage:UIImage , _ completion:@escaping (String)->()){
        S3.upload(image: thumbImage, success: { (thumb) in
            completion(thumb)
        }, failure: { (str) in
            print(str)
            Loader.shared.stop()
        })
    }
    
    func uploadImage(asst: PHAsset? , _ completion:@escaping (Bool)->()) {
        guard let asst = asst else { return }
        Loader.shared.start()
        if asst.mediaType == PHAssetMediaType.image{
            
            let valAsset = asst
            
            let requestOptions = PHImageRequestOptions()
            requestOptions.isSynchronous = true // adjust the parameters as you wish
        
            PHImageManager.default().requestImageData(for: valAsset, options: nil, resultHandler: { (imageData, UTI, _, _) in
                if let uti = UTI,let _ = imageData , UTTypeConformsTo(uti as CFString, kUTTypeGIF) {
                    var imageObj = [String:String]()
                    S3.uploadGif(gifData: imageData , success: { [weak self] (imageName) in
                        print(imageName)
                        
                        ez.runThisInMainThread {
                            imageObj["original"] = imageName
                            imageObj["thumbnail"] = imageName
                            imageObj["mediaType"] = "GIF"
                            self?.media.append(imageObj)
                        }
                        
                        completion(true)
                    }) { [weak self] (error) in
                        self?.resetButton.onNext(true)
                        print(error)
                        Loader.shared.stop()
                    }
                }else{
                    var imageObj = [String:String]()
                    S3.upload(image: asst.getAssetThumbnail(asset: asst) , success: { [weak self] (imageName) in
                        print(imageName)
                        ez.runThisInMainThread {
                            
                        
                        imageObj["original"] = imageName
                        imageObj["thumbnail"] = imageName
                        imageObj["mediaType"] = "IMAGE"
                        self?.media.append(imageObj)
                        }
                        completion(true)
                    }) { [weak self] (error) in
                        self?.resetButton.onNext(true)
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
                         Loader.shared.stop()
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
                        Loader.shared.stop()
                        print(error)
                    }
                } catch _ { }
            })
            
        }
        
    }

    func post(){
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
            }
        })
        if /assets.value.count == 0 {
            self.createPost()
        }else{
            if assetsPH.value.count == 0 {
                self.uploadImageCount = 0
                self.postImages()
            }else{
                uploadImage(asst: assetsPH.value[uploadImageCount]) { [ weak self ] (status) in
                    if /self?.uploadImageCount == /(/self?.assetsPH.value.count - 1) {
                        self?.uploadImageCount = 0
                        ez.runThisInMainThread {
                        self?.postImages()
                        }
                    }else{
                        self?.uploadImageCount = /self?.uploadImageCount + 1
                        ez.runThisInMainThread {
                             self?.post()
                        }
                    }
                }
            }
        }
    }
}
