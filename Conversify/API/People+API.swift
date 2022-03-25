//
//  Chat+Api.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 24/11/18.
//

import UIKit
import Moya
import RxSwift
import RxCocoa
import Foundation
import SwiftyJSON
import ObjectMapper
import EZSwiftExtensions

enum PeopleTarget {
    case fetchPeoplePassed()
    case followPeople(follow: String? , userId: String?)
    case followedPeopleListing(flag: String?)
    case getFollowersListing(groupId: String? , venueId: String?)
    case addParticpants(participants: String? , groupId: String? , venueId: String?)
    case archiveGroup(groupId: String? , groupType: String?)
    case searchPeople(text: String? , page: String?)
    case searchTags(text: String?, page: String?)
    case followUnFollowTag(tagId: String?, follow: String?)
    case clearNotification()
}

fileprivate let PeopleTargetProvider = MoyaProvider <PeopleTarget>(plugins: [NetworkLoggerPlugin()])

extension PeopleTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
            
        case .clearNotification():
            return [:]
        case .followUnFollowTag( let tagId, let follow):
            return ["tagId" : /tagId , "follow": /follow]
            
        case .addParticpants( let participants, let groupId, let venueId):
            var dict = ["participants": /participants]
            if /groupId != ""{
                dict["groupId"] = /groupId
            }
            if /venueId != ""{
                dict["venueId"] = /venueId
            }
            return dict
            
        case .getFollowersListing(let groupId , let venueId):
            var dict = [String:String]()
            if /groupId != ""{
                dict["groupId"] = groupId
            }
            if /venueId != ""{
                dict["venueId"] = venueId
            }
            return dict
            
        case .fetchPeoplePassed():
            return   [:]
            
        case .followPeople( let follow , let  userId ):
            return ["userId" : /userId , "action": /follow]
            
        case .followedPeopleListing( let flag):
            return ["flag": flag]
            
        case .archiveGroup(let groupId , let groupType):
            return ["groupId": groupId , "groupType" : groupType]
            
        case .searchPeople( let text , let page):
            var dict = [String:String]()
            if /text != ""{
                dict["search"] = text
            }
            if /page != ""{
                dict["pageNo"] = page
            }
            return   dict
            
        case .searchTags( let text , let page):
            var dict = [String:String]()
            if /text != ""{
                dict["search"] = text
            }
            if /page != ""{
                dict["pageNo"] = page
            }
            return   dict
            
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
            
        case .clearNotification():
            return APIConstants.clearNotification
            
        case .followUnFollowTag(_):
            return APIConstants.followUnFollowTag
            
        case .searchTags(_):
            return APIConstants.topTags
            
        case .searchPeople(_):
            return APIConstants.topSearch
            
        case .archiveGroup(_):
            return APIConstants.archiveGroup
            
        case .addParticpants(_):
            return APIConstants.addParticpant
            
        case.fetchPeoplePassed(_):
            return APIConstants.peopleCrossedPaths
            
        case .followPeople(_):
            return APIConstants.follow
            
        case .followedPeopleListing(_):
            return APIConstants.followersList
            
        case .getFollowersListing(_):
            return APIConstants.followersNotJoinedGroup
            
            
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
            PeopleTargetProvider.request(self, completion: { (result) in
                
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
            
        case .clearNotification():
            return Mapper<DictionaryResponse<AppNotification>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .searchTags(_):
            return Mapper<DictionaryResponse<Tags>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .searchPeople(_):
            return Mapper<DictionaryResponse<User>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .fetchPeoplePassed(_) :
            return Mapper<DictionaryResponse<PeopleData>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .followedPeopleListing(_) , .getFollowersListing(_):
            return Mapper<DictionaryResponse<User>>().map(JSONObject: safeResponse.dictionaryObject)
            
        default :
            return nil
        }
    }
}

