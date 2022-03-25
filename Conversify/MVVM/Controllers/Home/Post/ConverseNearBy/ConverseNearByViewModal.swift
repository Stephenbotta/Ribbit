//
//  ConverseNearByViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 15/01/19.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa
import GooglePlacePicker
import Photos
import AVKit
import MobileCoreServices
import EZSwiftExtensions

class VideoData : NSObject {
    
    var thumbnail:UIImage?
    var data: Data?
    
}

class ConverseNearByViewModal: BaseRxViewModel {
    
    var interests = Variable<[Interests]>([])
    var postImage = Variable<UIImage?>(nil)
    var postImgeUrl = Variable<String?>(nil)
    var postText = Variable<String?>(nil)
    var type = 1
    var selectedPeople = Variable<[User]>([])
    var expireTime = Variable<String?>(nil)
    var meeting = Variable<String?>(nil)
    var expireTimeStr = Variable<String?>(nil)
    var meetingStr = Variable<String?>(nil)
    var lat = Variable<String?>(nil)
    var long = Variable<String?>(nil)
    var locName = Variable<String?>(nil)
    var locAddress = Variable<String?>(nil)
    var hasTags = Variable<[String]>([])
    var createdSuccessFully = PublishSubject<Bool>()
    var gotLocation = PublishSubject<Bool>()
    var postingIn = "PUBLICILY"
    var isValid: Observable<Bool> {
        return Observable.combineLatest(postText.asObservable(), postImage.asObservable(), interests.asObservable() , assets.asObservable()) { (postText,postImage,interests,asst) in
            self.isValidInformation(info: /postText) || asst.count > 0
        }
    }
    var resetButton = PublishSubject<Bool>()
    var assets = Variable<[Any?]>([])
    var assetsPH = Variable<[PHAsset?]>([])
    var media = Array<[String:String]>()
    var videoPath : URL?
    var uploadImageCount = 0
    
    var cameraImages = Variable<[UIImage?]>([])
    var videos =  Variable<[VideoData?]>([]) 
    
    var isValidForLook: Observable<Bool> {
        return Observable.combineLatest(postText.asObservable(), postImage.asObservable()) { (postText,postImage) in
            self.isValidInformation(info: /postText)
        }
    }
    lazy var placesSearchController: GooglePlacesSearchController = {
        let controller = GooglePlacesSearchController(delegate: self,
                                                      apiKey: APIConstants.googleKey,
                                                      placeType: .address
            // Optional: coordinate: CLLocationCoordinate2D(latitude: 55.751244, longitude: 37.618423),
            // Optional: radius: 10,
            // Optional: strictBounds: true,
            // Optional: searchBarPlaceholder: "Start typing..."
        )
        //Optional: controller.searchBar.isTranslucent = false
        //Optional: controller.searchBar.barStyle = .black
        //Optional: controller.searchBar.tintColor = .white
        //Optional: controller.searchBar.barTintColor = .black
        return controller
    }()
    var gotTime = PublishSubject<Bool>()
    
    override init() {
        super.init()
    }
    
    init( type: Int ){
        super.init()
        self.type = type
    }
    
    init(postImage: UIImage? , interests:[Interests]? , postText: String? , type: Int , selectedPeople: [User] , postingIn: String){
        super.init()
        self.postImage.value = postImage
        self.interests.value = interests ?? []
        self.postText.value = postText
        self.type = type
        self.selectedPeople.value = selectedPeople
        self.postingIn = postingIn
    }
    
    func createPost(){
        print(media)
        //        if /expireTime.value != "" && /meeting.value != ""{
        //            if /meeting.value?.toDouble() > /expireTime.value?.toDouble() || /expireTime.value == /meeting.value || /expireTimeStr.value == /meetingStr.value {
        //                self.resetButton.onNext(true)
        //                UtilityFunctions.makeToast(text: "Expiration time should be greater than meeting time", type: .error)
        //                return
        //            }
        //        }
        
        //        if type != 1{
        if /lat.value == ""{
            lat.value = /LocationManager.sharedInstance.currentLocation?.currentLat
            long.value = /LocationManager.sharedInstance.currentLocation?.currentLng
        }
        //            interests.value = []
        //        }
        
        
        PostTarget.createConverse(text: postText.value, postingIn: /postingIn , selectedPeople : (selectedPeople.value.map{ $0.id }).toJson() , selectedInterests: (interests.value.map{ $0.id }).toJson(), expireTime: expireTime.value , meetingTime: meeting.value , locationLong: long.value, locationLat: lat.value , locationAddress: locAddress.value , locationName: locName.value , hasTags: hasTags.value.toJson() , media: /media.toJson()).request(apiBarrier: true)
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
    
    func postImages(){
        if cameraImages.value.count == 0 {
            self.uploadImageCount = 0
            self.postCamVideos()
        }else{
            uploadImage(image: cameraImages.value[uploadImageCount]) { [ weak self ] (status) in
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
    
    func uploadImage(image: UIImage? , _ completion:@escaping (Bool)->()){
        var imageObj = [String:String]()
        S3.upload(image: image , success: { [weak self] (imageName) in
            print(imageName)
            imageObj["original"] = imageName
            imageObj["thumbnail"] = imageName
            imageObj["mediaType"] = "IMAGE"
            self?.media.append(imageObj)
            completion(true)
        }) { [weak self] (error) in
            self?.resetButton.onNext(true)
            print(error)
            Loader.shared.stop()
        }
    }
    
    
    func uploadImage(asst: PHAsset? , _ completion:@escaping (Bool)->()){
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
    
    func upImageOfVideo(thumbImage:UIImage , _ completion:@escaping (String)->()){
        S3.upload(image: thumbImage, success: { (thumb) in
            completion(thumb)
        }, failure: { (str) in
            print(str)
            Loader.shared.stop()
        })
    }
    
    
    func datePickerTapped(isExpire: Bool , _ completion:@escaping (Bool)->()) {
        
        let currentDate = Date()
        var dateComponents = DateComponents()
        dateComponents.year = 2
        let next2Year = Calendar.current.date(byAdding: dateComponents, to: currentDate)
        
        let datePicker = DatePickerDialog(textColor: .black,
                                          buttonColor: .black,
                                          font: UIFont.systemFont(ofSize: 14, weight: .medium),
                                          showCancelButton: true)
        datePicker.show("Select Date & Time ",
                        doneButtonTitle: "Done",
                        cancelButtonTitle: "Cancel",
                        minimumDate: Date(),
                        maximumDate: next2Year,
                        datePickerMode: .dateAndTime) { [weak self] (date) in
                            if let dt = date {
                                let formatter = DateFormatter()
                                formatter.dateFormat = "MMM d yyyy, h:mm a"
                                if isExpire{
                                    self?.expireTime.value = dt.millisecondsSince1970
                                    self?.expireTimeStr.value = formatter.string(from: dt)
                                }else{
                                    self?.meeting.value = dt.millisecondsSince1970
                                    self?.meetingStr.value = formatter.string(from: dt)
                                }
                                self?.gotTime.onNext(true)
                                completion(true)
                            }
        }
    }
    
}

extension ConverseNearByViewModal : GMSPlacePickerViewControllerDelegate{
    
    func getPlaceDetails(){
        UIApplication.topViewController()?.present(placesSearchController, animated: true, completion: nil)
//        let config = GMSPlacePickerConfig(viewport: nil)
//        let placePicker = GMSPlacePickerViewController(config: config)
//        placePicker.delegate = self
//        UIApplication.topViewController()?.present(placePicker, animated: true, completion: nil)
    }
    
    func placePicker(_ viewController: GMSPlacePickerViewController, didPick place: GMSPlace) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        self.lat.value = place.coordinate.latitude.toString
        self.long.value = place.coordinate.longitude.toString
    
        self.locName.value = "\(/place.formattedAddress)"
     
        self.locAddress.value = "\(/place.name)"
        self.gotLocation.onNext(true)
    }
    
    func placePickerDidCancel(_ viewController: GMSPlacePickerViewController) {
        // Dismiss the place picker, as it cannot dismiss itself.
        viewController.dismiss(animated: true, completion: nil)
        
        print("No place selected")
    }
    
}

extension ConverseNearByViewModal: GooglePlacesAutocompleteViewControllerDelegate {
    func viewController(didAutocompleteWith place: PlaceDetails) {
        print(place.description)
        placesSearchController.isActive = false
        self.lat.value = place.coordinate?.latitude.toString
        self.long.value = place.coordinate?.longitude.toString
        self.locName.value = "\(/place.formattedAddress)"
        self.locAddress.value = "\(/place.name)"
        self.gotLocation.onNext(true)
    }
}

