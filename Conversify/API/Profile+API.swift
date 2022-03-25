//
//  Profile+API.swift
//  Connect
//
//  Created by OSX on 03/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//

import Moya
import RxSwift
import RxCocoa
import Foundation
import SwiftyJSON
import ObjectMapper
import EZSwiftExtensions


enum ProfileTarget {
    
    case getUserProfileData(userId : String?)
    case editProfile(userName : String?, fullName : String? , bio : String? , website : String? , email : String? , gender : String? , dateOfBirth : String? , designation : String? ,  company : String? , imageOriginal : String? , imageThumbnail : String?)
    case getUserNameProfileData(userName : String?)
    case blockUser(userId: String? , isBlock: Bool)
    case getStats
    case getDailyChalenge
    case getStories
    case addStory(media : String?)
}


fileprivate let ProfileProvider = MoyaProvider <ProfileTarget>(plugins: [NetworkLoggerPlugin()])


extension ProfileTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
        case .getDailyChalenge : return ["flag" : "6"]
        case .getStats : return [:]
        case .getUserProfileData(let userId):
            var dict =  ["userId":/userId]
            if userId == nil{
                dict.removeValue(forKey: "userId")
            }
            
            return dict
            
        case .getUserNameProfileData(let userName):
            return ["userName":/userName]
            
        case .blockUser(let userId , let isBlock ):
            var dict = ["userId": /userId]
            dict["action"] = isBlock ? "1" : "2"
            return dict
            
        case .getStories:
            return [:]
        case .addStory(let media):
            return ["media": media]
            
        case .editProfile(let userName, let fullName, let bio, let website, let email, let gender, let dateOfBirth, let designation, let company , let imageOriginal , let imageThumbnail):
            var dict =  ["userName" : userName, "fullName" : fullName, "bio" : /bio,  "website" : /website,  "email" : email,  "gender" : gender,  "dateOfBirth" : dateOfBirth,  "designation" : /designation,  "company" : /company , "imageOriginal" : imageOriginal , "imageThumbnail" : imageThumbnail]
            if gender == nil || gender == ""{
                dict.removeValue(forKey: "gender")
            }
            //            if bio == nil || bio == ""{
            //                dict.removeValue(forKey: "bio")
            //            }
            //            if website == nil || website == "" {
            //                dict.removeValue(forKey: "website")
            //            }
            if dateOfBirth == nil || dateOfBirth == "" {
                dict.removeValue(forKey: "dateOfBirth")
            }
            //            if designation == nil || designation == "" {
            //                dict.removeValue(forKey: "designation")
            //            }
            //            if company == nil || company == "" {
            //                dict.removeValue(forKey: "company")
            //            }
            if imageOriginal == nil || imageOriginal == "" {
                dict.removeValue(forKey: "imageOriginal")
                dict.removeValue(forKey: "imageThumbnail")
            }
            
            return dict
       
        }
    }
    
    
    var task:Task {
        
        switch self {
        case .getStats ,.getStories: return .requestParameters(parameters: parameters, encoding: URLEncoding.queryString)
        default:
            return .requestParameters(parameters: parameters, encoding: JSONEncoding.default)
        }
    }
    
    
    var headers: [String : String]?{
        switch self {
        default:
            return ["Content-type": "application/json", "authorization": "bearer " + /Singleton.sharedInstance.loggedInUser?.token]
        }
        
    }
    public var baseURL: URL {
        switch self {
            
        default:
            return URL(string: APIConstants.basePath)!
        }
    }
    
    public var path:String {
        switch self {
        case . getDailyChalenge: return APIConstants.getInfo
        case .addStory(_):
            return APIConstants.addStory
        case .getStats : return APIConstants.userStats
        case .blockUser(_):
            return APIConstants.blockUser
        case .getUserProfileData(_) , .getUserNameProfileData(_) : return APIConstants.getProfileData
        case .editProfile(_) : return APIConstants.editUserProfile
        case .getStories:
            return APIConstants.getStories
        }
    }
    
    public var method: Moya.Method {
        switch self {
        case .getStats , .getStories : return .get
        default: return .post
        }
    }
    
    public var sampleData: Data { return Data() }
    
    func request(apiBarrier : Bool = false) -> Observable<Any?>{
        
        return Observable<Any?>.create { (observer) -> Disposable in
            switch(self){
            default:
                
                if apiBarrier && (self.path != APIConstants.userStats){
                    self.showLoader()
                }
            }
            let disposable = Disposables.create {}
            ProfileProvider.request(self, completion: { (result) in
                
                self.hideLoader()
                switch result {
                case let .success(moyaResponse):
                    let data = moyaResponse.data
                    let json = JSON(data)
                    
                    let status = self.handleResponse(json: json).serverValue
                    if status == ResponseStatus.success.serverValue{
                        observer.onNext(self.parse(response: json))
                        observer.on(.completed)
                    }else if status == ResponseStatus.blocked.serverValue{
                        observer.onError(ResponseStatus.blocked)
                        observer.on(.completed)
                    }else{
                        observer.onError(ResponseStatus.clientError(message: /("message" => json.dictionaryValue)))
                        observer.on(.completed)
                    }
                    
                // do something with the response data or statusCode
                case let .failure(error):
                    // this means there was a network failure - either the request
                    // wasn't sent (connectivity), or no response was received (server
                    // timed out).  If the server responds with a 4xx or 5xx error, that
                    // will be sent as a ".success"-ful response.
                    print(error.localizedDescription)
                    observer.onError(ResponseStatus.noInternet)
                    observer.on(.completed)
                    break
                }
            })
            
            return disposable
        }
    }
    
    func showLoader() {
        Loader.shared.start()
    }
    
    func hideLoader(){
        Loader.shared.stop()
    }
    
    
    func handleResponse(json : JSON) -> ResponseStatus{
        return ResponseStatus.getRawEnum(value: /("statusCode" => json.dictionaryValue))
    }
    
    func parse(response : JSON?) -> Mappable? {
        print(response)
        guard let safeResponse = response else {return nil}
        switch self {
        case .getDailyChalenge :
            return Mapper<StoriesDetail>().map(JSONObject:safeResponse.dictionaryObject)
        case .addStory(_):
            return Mapper<DictionaryResponse<StoriesDetail>>().map(JSONObject: safeResponse.dictionaryObject)
        case .getStories:
           return Mapper<StoriesDetail>().map(JSONObject: safeResponse.dictionaryObject)
        case .blockUser(_): return nil
        case .getStats :
            return Mapper<DictionaryResponse<UserStats>>().map(JSONObject: safeResponse.dictionaryObject)
        case .getUserProfileData(_) , .getUserNameProfileData(_) :
            return Mapper<DictionaryResponse<UserList>>().map(JSONObject: safeResponse.dictionaryObject)
        case .editProfile(_):
            return Mapper<DictionaryResponse<User>>().map(JSONObject: safeResponse.dictionaryObject)
        }
    }
}


