 
 import AWSS3
 import Foundation
 import AVKit
 import AVFoundation
 import MobileCoreServices
 
 
 var imgAddress : URL? = nil
 var l = arc4random()
 var simg : Data?
 
 func getKey (path : String?) -> String {
    
    let today = Int(Date().timeIntervalSince1970)
    let url = "\(today)" + ".jpeg"
    
    return url
 }
 
 func getKeyVideo (path : String?) -> String {
    
    let today = Int(Date().timeIntervalSince1970)
    let url = "vid_\(today)" + ".mp4"
    
    return url
 }
 
 func getKeyAudio (path : String?) -> String {
    
    let today = Int(Date().timeIntervalSince1970)
    let url = "audio_\(today)" + ".mp3"
    
    return url
 }
 
 func mimeTypeFromFileExtension(fileName : String) -> String? {
    guard let uti: CFString = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, fileName as NSString, nil)?.takeRetainedValue() else {
        return nil
    }
    
    guard let mimeType: CFString = UTTypeCopyPreferredTagWithClass(uti, kUTTagClassMIMEType)?.takeRetainedValue() else {
        return nil
    }
    
    return mimeType as String
 }
 
 class S3 {
    
    static func upload(image : UIImage?,contentType : String = ".jpeg" , success : @escaping (String) -> () , failure : @escaping (String?) -> ())  {
        let imag = image?.resize(toWidth: 600)
        l=l+1
        let documentsDirectoryURL = try! FileManager().url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        
        let fileURL = documentsDirectoryURL.appendingPathComponent("Image"+"\(l)"+contentType)
        imgAddress = fileURL
        
        
        
        if !FileManager.default.fileExists(atPath: fileURL.path){
            do {
                try imag!.jpegData(compressionQuality: 0.25)?.write(to: URL(fileURLWithPath: fileURL.path), options: .atomic)
                
                //            print("file saved")
                let uploadRequest: AWSS3TransferManagerUploadRequest = AWSS3TransferManagerUploadRequest()
                uploadRequest.bucket = "ribbitrewardsdev"//checkits3bucket"
                uploadRequest.key = getKey(path: fileURL.path)
                uploadRequest.body = imgAddress!
                uploadRequest.contentType = contentType
                
                
                let transferManager = AWSS3TransferManager.default()
                
                transferManager.upload(uploadRequest).continueWith(executor: AWSExecutor.default(), block: { (task) -> Any? in
                    
                    if task.error != nil {
                        // Error.
                        if task.isCompleted {
                            
                            failure(task.error?.localizedDescription )
                            
                            _ =  transferManager.cancelAll()
                            
                        }
                    } else {
                        let str =  "https://ribbitrewardsdev.s3.us-west-2.amazonaws.com/" +  uploadRequest.key!
                        print(str)
                        success(str)
                    }
                    return nil
                })
            }
            catch {
                print(error)
            }
            
        }
        
    }
    
    static func uploadGif(gifData : Data?, contentType : String = ".gif" , success : @escaping (String) -> () , failure : @escaping (String?) -> ())  {
        
        l=l+1
        let documentsDirectoryURL = try! FileManager().url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        
        let fileURL = documentsDirectoryURL.appendingPathComponent("ImageGif"+"\(l)"+contentType)
        imgAddress = fileURL
        
        
        
        if !FileManager.default.fileExists(atPath: fileURL.path){
            do {
                try gifData?.write(to: URL(fileURLWithPath: fileURL.path), options: .atomic)
                
                //            print("file saved")
                let uploadRequest: AWSS3TransferManagerUploadRequest = AWSS3TransferManagerUploadRequest()
                uploadRequest.bucket = "ribbitrewardsdev"
                uploadRequest.key = getKey(path: fileURL.path)
                uploadRequest.body = imgAddress!
                uploadRequest.contentType = contentType
                
                
                let transferManager = AWSS3TransferManager.default()
                
                transferManager.upload(uploadRequest).continueWith(executor: AWSExecutor.default(), block: { (task) -> Any? in
                    
                    if task.error != nil {
                        // Error.
                        if task.isCompleted {
                            
                            failure(task.error?.localizedDescription )
                            
                            _ =  transferManager.cancelAll()
                            
                        }
                    } else {
                        let str = "https://ribbitrewardsdev.s3.us-west-2.amazonaws.com/" +  uploadRequest.key!
                        print(str)
                        success(str)
                    }
                    return nil
                })
            }
            catch {
                print(error)
            }
        }
    }
    
    static func uploadAudio(audioURL : URL?, contentType : String = ".m4a" , success : @escaping (String) -> () , failure : @escaping (String?) -> ())  {
        
        guard let audioURL = audioURL else { return }
        let contentType = mimeTypeFromFileExtension(fileName: audioURL.path.components(separatedBy: ".").last ?? "")
        
        if FileManager.default.fileExists(atPath: audioURL.path){
            //            print("file saved")
            let uploadRequest: AWSS3TransferManagerUploadRequest = AWSS3TransferManagerUploadRequest()
            uploadRequest.bucket = "ribbitrewardsdev"
            uploadRequest.key = audioURL.lastPathComponent
            uploadRequest.body = audioURL
            uploadRequest.contentType = contentType
            
            
            let transferManager = AWSS3TransferManager.default()
            
            transferManager.upload(uploadRequest).continueWith(executor: AWSExecutor.default(), block: { (task) -> Any? in
                
                if task.error != nil {
                    // Error.
                    if task.isCompleted {
                        
                        failure(task.error?.localizedDescription )
                        
                        _ =  transferManager.cancelAll()
                        
                    }
                } else {
                    let str = "https://ribbitrewardsdev.s3.us-west-2.amazonaws.com/" +  uploadRequest.key!
                    print(str)
                    success(str)
                }
                return nil
            })
        }
    }
    

 
 static func uploadChatVideo(video: URL?,contentType : String = ".mp4" ,uploadProgress : @escaping (Float,String) -> () ,success : @escaping (String,AWSS3TransferManagerUploadRequest,AWSS3TransferManager) -> () , failure : @escaping (String?) -> ())  {
    
    guard let video = video else { return }
    print(video)
    print(video.path)
    
    if FileManager.default.fileExists(atPath: video.path){
        
        let uploadRequest: AWSS3TransferManagerUploadRequest = AWSS3TransferManagerUploadRequest()
        uploadRequest.bucket = "ribbitrewardsdev"
        uploadRequest.key = getKeyVideo(path: video.path)
        uploadRequest.body = video
        uploadRequest.contentType = contentType
        let str = "https://ribbitrewardsdev.s3.us-west-2.amazonaws.com/" + uploadRequest.key!
        uploadRequest.uploadProgress = { (bytesSent, totalBytesSent, totalBytesExpectedToSend) -> Void in
            DispatchQueue.main.sync(execute: {
                print("\(totalBytesSent)/\(totalBytesExpectedToSend)")
                let progress = Float(totalBytesSent)/Float(totalBytesExpectedToSend)
                uploadProgress(Float(progress),str)
            })
        }
        
        let transferManager = AWSS3TransferManager.default()
        print(str)
        
        transferManager.upload(uploadRequest).continueWith(executor: AWSExecutor.default(), block: { (task) -> Any? in
            
            if task.error != nil {
                // Error.
                if task.isCompleted {
                    print("error")
                    failure(str)
                    _ =  transferManager.cancelAll()
                    
                }
            } else {
                success(str,uploadRequest,transferManager)
                // let str =  "https://s3-us-west-2.amazonaws.com/messago/Images/" + video.lastPathComponent
                print(str)
                // success(str)
            }
            return nil
        })
        
    }
    
 }
 
 static func upload(document: URL? ,uploadProgress : @escaping (Float,String) -> () ,success : @escaping (String,AWSS3TransferManagerUploadRequest,AWSS3TransferManager) -> () , failure : @escaping (String?) -> ())  {
    
    guard let document = document else { return }
    
    let contentType = mimeTypeFromFileExtension(fileName: document.path.components(separatedBy: ".").last ?? "")
    
    let documentsDirectoryURL = try! FileManager().url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
    
    //        let fileURL = documentsDirectoryURL.appendingPathComponent("doc11_\(arc4random()).pdf")
    //        do {
    //            try FileManager.default.copyItem(at: URL(fileURLWithPath: document.path), to: fileURL)
    //        }
    //        catch {
    //
    //        }
    
    if FileManager.default.fileExists(atPath: document.path){
        
        let uploadRequest: AWSS3TransferManagerUploadRequest = AWSS3TransferManagerUploadRequest()
        
        uploadRequest.bucket = "ribbitrewardsdev"
        uploadRequest.key = document.lastPathComponent
        uploadRequest.body = document
        uploadRequest.contentType = contentType
        
        let transferManager = AWSS3TransferManager.default()
        
        transferManager.upload(uploadRequest).continueWith(executor: AWSExecutor.default(), block: { (task) -> Any? in
            
            if task.error != nil {
                // Error.
                if task.isCompleted {
                    print("error" , task.error.debugDescription)
                    failure("Upload Failed")
                    _ =  transferManager.cancelAll()
                    
                }
            } else {
                
                //"https://s3-us-west-2.amazonaws.com/checkits3bucket/"
                let str = "https://ribbitrewardsdev.s3.us-west-2.amazonaws.com/" + uploadRequest.key!
                success(str,uploadRequest,transferManager)
                print(str)
                
            }
            return nil
        })
    }
 }
 }
