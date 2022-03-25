//
//  Post+Api.swift
//  Conversify
//
//  Created by Apple on 15/11/18.
//


import UIKit

import Moya
import RxSwift
import RxCocoa
import Foundation
import SwiftyJSON
import ObjectMapper
import EZSwiftExtensions

enum AppNotificationType: String {
    case venueRequest = "REQUEST_VENUE"
    case groupRequest = "REQUEST_GROUP"
    case likeComment = "LIKE_COMMENT"
    case likePost = "LIKE_POST"
    case likeReply = "LIKE_REPLY"
    case comment = "COMMENT"
    case reply = "REPLY"
    case groupInvite = "INVITE_GROUP"
    case venueInvite = "INVITE_VENUE"
    case venue = "VENUE"
    case group = "GROUP"
    case acceptInviteGroup = "ACCEPT_INVITE_GROUP"
    case acceptInviteVenue = "ACCEPT_INVITE_VENUE"
    case acceptRequestGroup = "ACCEPT_REQUEST_GROUP"
    case acceptRequestVenue = "ACCEPT_REQUEST_VENUE"
    case chat = "CHAT"
    case groupChat = "GROUPCHAT"
    case venueChat = "VENUECHAT"
    case follow = "FOLLOW"
    case post = "POST"
    case tagComment = "TAG_COMMENT"
    case tagReply = "TAG_REPLY"
    case joinedVenue = "JOINED_VENUE"
    case joinedGroup = "JOINED_GROUP"
    case requestFollow = "REQUEST_FOLLOW"
    case acceptRequestFollow = "ACCEPT_REQUEST_FOLLOW"
    case crossedPath = "CROSSED_PEOPLE"
    case converseNearBy = "ALERT_CONVERSE_NEARBY_PUSH"
    case lookNearBy = "ALERT_LOOK_NEARBY_PUSH"
    case callDisconnect = "Call_Disconnect"
    case receivedreddempoint = "RECEIVED_REDEEM_POINTS"
    case spendEarnPoint = "SPEND_EARNED_POINT"
}



enum HomeSearchTarget {
    
    case getMutualInterestUsers(categories: [String], pageNo: String , locationLong: String? , locationLat: String? , range: String? )
    case getNotifications(pageNo: String)
    case getNotificationCount()
    case acceptRejectRequest(userId: String?, groupId: String, groupType: String, acceptType: String, accept: Bool)
    case readNotifications(notificationId: String)
}

fileprivate let PostProvider = MoyaProvider <HomeSearchTarget>(plugins: [NetworkLoggerPlugin()])

extension HomeSearchTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
        case .getNotificationCount():
            return [:]
        case .getMutualInterestUsers( let categories, let pageNo , let locationLong , let locationLat , let range  ):
            return ["categoryIds": categories, "pageNo": pageNo , "locationLong" : /locationLong , "locationLat" : /locationLat , "range": /range ]
        case .getNotifications(let pageNo):
            return ["pageNo": pageNo]
        case .acceptRejectRequest(let userId, let groupId, let groupType, let acceptType, let accept):
            var dict =  ["userId": /userId, "acceptType": acceptType, "accept": "\(accept)"]
            if /groupType != ""{
                dict["groupType"] = groupType
            }
            if /groupId != ""{
                 dict["groupId"] = groupId
            }
            
            return dict
            
        case .readNotifications(let notificationId):
            return ["notificationId": notificationId]
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
        case .getNotificationCount():
            return APIConstants.unreadCount
        case .getNotifications(_):
            return APIConstants.getNotification
        case .getMutualInterestUsers(_):
            return APIConstants.interestMatchUsers
        case .acceptRejectRequest(_, _, _, _, _):
            return APIConstants.acceptRejectRequest
        case .readNotifications(_):
            return APIConstants.readNotifications
        }
    }
    
    public var method: Moya.Method {
        switch self {
        case .getNotificationCount():
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
            PostProvider.request(self, completion: { (result) in
                
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
            
        case .getNotificationCount():
            return nil
            
        case  .getMutualInterestUsers(_) :
            return Mapper<DictionaryResponse<UserList>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case  .getNotifications(_) :
            return Mapper<DictionaryResponse<AppNotification>>().map(JSONObject: safeResponse.dictionaryObject)
        case .acceptRejectRequest(_, _, let groupType, let acceptType, _):
            return nil
        case .readNotifications(_):
            return nil
        }
    }
}
