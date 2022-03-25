//
//  Griup+Api.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 23/10/18.
//

import UIKit

import Moya
import RxSwift
import RxCocoa
import Foundation
import SwiftyJSON
import ObjectMapper
import EZSwiftExtensions

enum GroupTarget {
    
    case createGroup(categoryId: String? , venueImage: String? , groupName: String? ,  privacy: String?, participants: String? , description: String? )
    case getGroups()
    case getFilteredGroups(page: String, categoryId: String?)
    case getGroupsPost(groupId: String? , page: String?)
    case likePost(postId: String? , state: String? , postBy: String?)
    case joinGroup(groupId: String? , userId: String? , isPrivate: String? , adminId: String?)
    case addComment(postId: String? , comment: String? , postUserId: String?)
    case readPosts(groupId: String? , postId: String?)
    case getVenueDetail(venueId: String?)
    case searchGroup(text: String?, page: String?)
    case inviteusers(phone: String?, emails: String? , groupId: String?)
}

fileprivate let GroupTargetProvider = MoyaProvider <GroupTarget>(plugins: [NetworkLoggerPlugin()])

extension GroupTarget: TargetType {
    
    var parameters:[String: Any] {
        
        switch self {
            
        case .inviteusers( let phone, let emails, let groupId):
            var dict = [String:String]()
            if /groupId != ""{
                dict["venueId"] = groupId
            }
            
            if /phone != ""{
                dict["phoneNumberArray"] = phone
            }
            
            return dict
            
        case .searchGroup( let text , let page):
            var dict = [String:String]()
            if /text != ""{
                dict["search"] = text
            }
            if /page != ""{
                dict["pageNo"] = page
            }
            return   dict
            
        case .getVenueDetail(let venueId):
            return ["groupId": /venueId]
            
        case .readPosts(let groupId , let postId):
            return ["groupId": /groupId , "postId" : /postId ]
            
        case .addComment(let postId , let comment , let postUserId):
            return ["postId": /postId , "comment" : /comment  , "postBy" : /postUserId ]
            
        case .createGroup( let categoryId , let venueImage , let groupName , let privacy , let participants , let description):
            var dict = ["categoryId": categoryId , "groupName" :groupName , "isPrivate":privacy]
            if /venueImage != ""{
                dict["groupImageOriginal"] = /venueImage
                dict["groupImageThumbnail"] = /venueImage
            }
            if /participants != ""{
                dict["participantIds"] = /participants
            }
            if /description != ""{
                dict["description"] = /description
            }
            
            return dict
            
        case .getGroups():
            return ["flag":"3"]
            
        case .getFilteredGroups( let page , let categoryId):
            return ["pageNo": /page , "categoryId": /categoryId , "limit":"10"]
            
        case .getGroupsPost(let groupId , let page):
            return ["groupId" : /groupId , "pageNo" :  /page , "limit" : "10" ]
            
        case .likePost(let postId , let state , let postBy):
            return ["postId": /postId , "action" : /state , "postBy" : /postBy]
            
        case .joinGroup(let groupId , let userId , let isPrivate , let adminId):
            var dict = ["groupId": /groupId , "groupType" : "GROUP" , "adminId" :/adminId ]
            if /userId != ""{
                dict["userId"] = userId
            }
            if /isPrivate != ""{
                dict["isPrivate"] = isPrivate
            }
            return dict
        }
        
    }
    
    var task:Task {
        switch self {
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
        case .inviteusers(_):
            return APIConstants.inviteusers
            
        case .searchGroup(_):
            return APIConstants.searchGroup
            
        case .getVenueDetail(_):
            return APIConstants.venueDetails
            
        case .readPosts(_):
            return APIConstants.readPosts
            
        case .addComment(_):
            return APIConstants.addEditComment
            
        case .joinGroup(_):
            return APIConstants.joinGroup
            
            
        case .createGroup(_):
            return APIConstants.createGroup
            
        case .getGroups():
            return APIConstants.getInfo
            
        case .getFilteredGroups(_):
            return APIConstants.filteredGroupsBycat
            
        case .getGroupsPost(_):
            return APIConstants.groupsPost
            
        case .likePost( let postId):
            return APIConstants.likePost
            
        }
    }
    
    public var method: Moya.Method {
        switch self {
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
            GroupTargetProvider.request(self, completion: { (result) in
                
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
            
        case .inviteusers(_):
            return nil
            
        case .getVenueDetail(_):
            return Mapper<DictionaryResponse<YourGroup>>().map(JSONObject: safeResponse.dictionaryObject)
            
            
        case .createGroup(_) , .joinGroup(_):
            let count = /Singleton.sharedInstance.loggedInUser?.groupCount
            if count == 0{
                let user = Singleton.sharedInstance.loggedInUser
                user?.groupCount = /user?.groupCount + 1
                Singleton.sharedInstance.loggedInUser = user
            }
            return nil
            
        case .likePost(_)  , .addComment(_) , .readPosts(_):
            return nil
            
        case .getGroups() :
            return Mapper<DictionaryResponse<GroupData>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case  .searchGroup(_):
            return Mapper<DictionaryResponse<SuggestedGroup>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .getFilteredGroups(_) :
            return Mapper<DictionaryResponse<SuggestedGroup>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .getGroupsPost(_):
            return Mapper<DictionaryResponse<GroupsPostData>>().map(JSONObject: safeResponse.dictionaryObject)
            
            
        }
    }
}
