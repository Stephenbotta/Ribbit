//
//  Settings+Api.swift
//  Conversify
//
//  Created by Harminder on 08/01/19.
//

import UIKit

import UIKit
import Moya
import RxSwift
import RxCocoa
import Foundation
import SwiftyJSON
import ObjectMapper
import EZSwiftExtensions

enum SettingsTarget {
    case verification(email: String? , phone: String? , docUrl: String?)
    case listFollowers()
    case listFollowing()
    case whoCanSeeMyProfilePic(everyOne: Bool , imageVisibilityForFollowers: Bool? , userIds: String?)
    case whoCanSeeMyUserName(everyOne: Bool ,nameVisibility: Bool? , userIds: String?)
    case whoCanMessageMe(everyOne: Bool ,tagPermission: Bool? , userIds: String?)
    case whoCanSeePrivateInfo(everyOne: Bool ,permission: Bool? , userIds: String?)
    case invitePpl(email: String , phoneNum: String)
    case getBlockedUsers()
    case getListLikers(postId: String?)
    case blockAction(userId: String? , action: String?)
    case alertSettings( action: String?)
    case updateAccountType( action: String?)
    //ADDED
    case query(message : String?)
}

fileprivate let SettingsTargetProvider = MoyaProvider <SettingsTarget>(plugins: [NetworkLoggerPlugin()])

extension SettingsTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
            
        case .getListLikers(let postId):
            return ["postId": /postId]
            
        case .updateAccountType(let action):
            return ["flag": "2" , "action" : /action]
            
        case .alertSettings(let action):
            return ["flag": "8" , "action" : /action]
            
        case .blockAction( let userId, let action):
            return ["userId": /userId , "action" : /action]
            
        case .getBlockedUsers():
            return [:]
            
        case .invitePpl( let email, let phoneNum):
            var dict = [String: String]()
            if /email != ""{
                dict["emailArray"] = email
                return dict
            }
            if /phoneNum != ""{
                dict["phoneNumberArray"] = phoneNum
                return dict
            }
            return dict
            
        case .whoCanSeePrivateInfo( let everyOne , let permission, let userIds):
            var dict = [String: String]()
            dict["flag"] = "7"
            if /everyOne{
                dict["personalInfoVisibilityForEveryone"] = "true"
                return dict
            }else{
                dict["personalInfoVisibilityForEveryone"] = "false"
            }
            
            if /permission{
                dict["personalInfoVisibilityForFollowers"] = "true"
                return dict
            }else{
                dict["personalInfoVisibilityForFollowers"] = "false"
            }
            dict["userIds"] = userIds
            return dict
            
        case .whoCanMessageMe( let everyOne , let tagPermission, let userIds):
            var dict = [String: String]()
            dict["flag"] = "6"
            
            if /everyOne{
                dict["tagPermissionForEveryone"] = "true"
                return dict
            }else{
                dict["tagPermissionForEveryone"] = "false"
            }
            if /tagPermission{
                dict["tagPermissionForFollowers"] = "true"
                return dict
            }else{
                dict["tagPermissionForFollowers"] = "false"
            }
            dict["userIds"] = userIds
            return dict
            
        case .whoCanSeeMyUserName(let everyOne , let nameVisibility , let userIds):
            var dict = [String: String]()
            dict["flag"] = "4"
            
            if /everyOne{
                dict["nameVisibilityForEveryone"] = "true"
                return dict
            }else{
                dict["nameVisibilityForEveryone"] = "false"
            }
            
            if /nameVisibility{
                dict["nameVisibilityForFollowers"] = "true"
                return dict
            }else{
                dict["nameVisibilityForFollowers"] = "false"
            }
            dict["userIds"] = userIds
            return dict
            
        case .whoCanSeeMyProfilePic( let everyOne , let imageVisibilityForFollowers,  let userIds):
            var dict = [String: String]()
            dict["flag"] = "3"
            
            if /everyOne{
                dict["imageVisibilityForEveryone"] = "true"
                return dict
            }else{
                dict["imageVisibilityForEveryone"] = "false"
            }
            
            if /imageVisibilityForFollowers{
                dict["imageVisibilityForFollowers"] = "true"
                return dict
            }else{
                dict["imageVisibilityForFollowers"] = "false"
            }
            dict["userIds"] = userIds
            return dict
            
        case .listFollowers():
            return ["flag":"1"]
            
        case .listFollowing():
            return ["flag":"2"]
            
        case .verification( let email, let phone , let docUrl):
            var dict = [String: String]()
            if /email != ""{
                dict["email"] = /email
            }
            if /phone != ""{
                dict["phoneNumber"] = /phone
            }
            if /docUrl != ""{
                dict["passportDocUrl"] = /docUrl
            }
            return dict
            
        case .query(let message):
            return ["message" : /message]
            
            
            
            
        }
    }
    
    var task:Task {
        switch self {
        case .getBlockedUsers():
            return .requestParameters(parameters: parameters, encoding: URLEncoding.queryString)
        default :
            return .requestParameters(parameters: parameters, encoding: JSONEncoding.default)
        
            
        }
    }
    
    var headers: [String : String]?{
        switch self {
        default:
            return ["Content-type": "application/json", "authorization": "bearer " + /Singleton.sharedInstance.loggedInUser?.token]
        }
        
    }
    
    var multipartBody: [MultipartFormData]? {
        switch self {
        default:
            return nil
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
            
        case .getListLikers(_):
            return APIConstants.listLikers
            
        case .updateAccountType(_):
            return APIConstants.whoCanSeeMe
            
        case .alertSettings(_):
            return APIConstants.whoCanSeeMe
            
        case .blockAction(_):
            return APIConstants.blockUser
            
        case .getBlockedUsers():
            return APIConstants.listBlockedUsers
            
        case .invitePpl(_):
            return APIConstants.invitePpl
            
        case .whoCanSeeMyProfilePic(_) , .whoCanSeeMyUserName(_) , .whoCanMessageMe(_) , .whoCanSeePrivateInfo(_):
            return APIConstants.whoCanSeeMe
            
        case .listFollowers() , .listFollowing():
            return APIConstants.followersList
            
        case .verification(_):
            return APIConstants.verification
            
        case .query(_):
            return APIConstants.contactUsApi
        
        }
    }
    
    public var method: Moya.Method {
        switch self {
        case .getBlockedUsers():
            return .get
        default: return .post
        }
    }
    
    public var sampleData: Data { return Data() }
    
    func request(apiBarrier : Bool = false) -> Observable<Any?>{
        
        return Observable<Any?>.create { (observer) -> Disposable in
            switch(self){
            default:
                if apiBarrier{
                    self.showLoader()
                }
            }
            let disposable = Disposables.create {}
            SettingsTargetProvider.request(self, completion: { (result) in
                
                self.hideLoader()
                switch result {
                case let .success(moyaResponse):
                    let data = moyaResponse.data
                    let json = JSON(data)
                    
                    let status = self.handleResponse(json: json).serverValue
                    if status == ResponseStatus.success.serverValue{
                        observer.onNext(self.parse(response: json))
                        observer.on(.completed)
                    }else if status == ResponseStatus.missingAuthentication.serverValue{
                        UIApplication.shared.loginExpired()
                    }else{
                        observer.onError(ResponseStatus.clientError(message: /("message" => json.dictionaryValue)))
                        UtilityFunctions.makeToast(text: /("message" => json.dictionaryValue), type: .error)
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
        guard let safeResponse = response else {return nil}
        switch self {
            
        case .query(_):
            return nil
            
        case .blockAction(_):
            return nil
            
        case .invitePpl(_) :
            return nil
            
        case .listFollowers() , .getBlockedUsers() , .updateAccountType(_) , .listFollowing() , .getListLikers(_):
            return Mapper<DictionaryResponse<User>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .verification(_) , .whoCanSeeMyProfilePic(_) , .whoCanSeeMyUserName(_) , .whoCanMessageMe(_) , .whoCanSeePrivateInfo(_) , .alertSettings(_):
            
            return Mapper<DictionaryResponse<User>>().map(JSONObject: safeResponse.dictionaryObject)
            
        }
    }
}


