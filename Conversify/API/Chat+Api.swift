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

enum ChatTarget {
    
    case enableNotificationForVenue(venueId: String? , action: String?)
    case enableNotificationForGroupChat(groupId: String? , action: String?)
    case exitVenueGroup(venueId: String?)
    case exitGroup(groupId: String?)
    case editGroupName(groupName: String? , postGroupId: String?)
    case chatConversation(conversationId : String? , chatId : String?)
    case chatSummary(flag : String?)
    case addEditPostGroupName(groupName: String? , postGroupId: String?)
    case initiateCall(userId: String?)
    case callDisconnected(callerId : String? , receiverId : String?)

}

fileprivate let PostProvider = MoyaProvider <ChatTarget>(plugins: [NetworkLoggerPlugin()])

extension ChatTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
            
        case .enableNotificationForGroupChat(let groupId , let action) :
            return ["groupId": groupId , "action" : action]
            
        case .enableNotificationForVenue(let venueId , let  action):
            return   ["venueId": /venueId , "action": /action ]
            
        case .exitVenueGroup(let venueId):
            return   ["venueId": /venueId]
            
        case .exitGroup(let groupId):
            return   ["groupId": /groupId]
        
        case .initiateCall(let userId):
            return   ["callToUserId": /userId]
            
        case .editGroupName(let groupName , let  postGroupId):
            return   ["venueTitle": /groupName , "venueGroupId": /postGroupId ]
            
        case .addEditPostGroupName(let groupName , let  postGroupId):
            return   ["groupName": /groupName , "postGroupId": /postGroupId ]
            
        case .chatConversation(let conversationId, let chatId):
            var dict = ["conversationId" : conversationId , "chatId" : chatId]
            if conversationId == nil || conversationId == ""{
                dict.removeValue(forKey: "conversationId")
            }
            if /chatId == ""{
                dict.removeValue(forKey: "chatId")
            }
            return dict
            
            
        case .chatSummary(let flag):
            return ["flag" : flag ]
        case .callDisconnected(let callerId, let receiverId) :
            return ["callerId" : /callerId , "receiverId" : /receiverId]
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
            
        case.editGroupName(_):
            return APIConstants.createVenue
            
        case .enableNotificationForVenue(_) , .enableNotificationForGroupChat(_):
            return APIConstants.disableNotificationGroup
            
        case .exitVenueGroup(_) , .exitGroup(_):
            return APIConstants.exitGroup
            
        case .chatConversation(_):
            return APIConstants.chatConversation
            
        case .chatSummary(_) :
            return APIConstants.chatSummary
            
        case .addEditPostGroupName(_):
            return APIConstants.createGroup
            
        case .initiateCall(_):
            return APIConstants.initiateAudioCall
        case .callDisconnected(_, _) : return APIConstants.callDisconnect
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
            
        case .exitVenueGroup(_) , .enableNotificationForVenue(_) , .exitGroup(_) , .addEditPostGroupName(_) , .enableNotificationForGroupChat(_):
            return nil
            
        case .initiateCall(_):
            return Mapper<DictionaryResponse<MakeCallModel>>().map(JSONObject: safeResponse.dictionaryObject)

        case .chatConversation(_):
            return Mapper<DictionaryResponse<GroupConvoList>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .chatSummary(_):
            return Mapper<DictionaryResponse<ChatListModel>>().map(JSONObject: safeResponse.dictionaryObject)
            
        default :
            return nil
        }
    }
}

