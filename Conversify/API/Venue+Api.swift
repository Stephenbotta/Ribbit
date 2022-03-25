//
//  Venue+Api.swift
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

enum VenueTarget {
    
    case createVenue(categoryId: String? , venueImage: String? , venueDoc: String? , venueTitle: String? , venueLocationLat: String? , userName: String? , date: String? , venueLocName: String? , venueLocationLng: String? , tag: String? , privacy: String? , locAddress: String? , participantIds: String?)
    case getVenueList(currentLat :String? , currentLong :String? , flag: String?)
    case getFilters()
    case filterVenue(date: String? , categoryId: String? , privateV: String? , lat: String? , long: String?)
    case joinVenue(groupId: String? , userId: String? , isPrivate: String? , adminId : String? )
    case venueConversationDetails(groupId : String? , chatId : String?)
    case getVenueDetail(venueId: String?)
    case getRequestsCount()
    case searchVenue(text: String?, page: String?)
    case inviteusers(phone: String?, emails: String? , groupId: String? , isGroup: Bool)
}

fileprivate let VenueProvider = MoyaProvider <VenueTarget>(plugins: [NetworkLoggerPlugin()])

extension VenueTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
            
        case .inviteusers( let phone, let emails, let groupId , let isGroup):
            var dict = [String:String]()
            
            if /groupId != ""{
                dict[ isGroup ? "groupId" : "venueId"] = groupId
            }
            if /emails != ""{
                dict["emailArray"] = emails
            }
            
            if /phone != ""{
                dict["phoneNumberArray"] = phone
            }
            
            return dict
            
        case .searchVenue( let text , let page):
            var dict = [String:String]()
            if /text != ""{
                dict["search"] = text
            }
            if /LocationManager.sharedInstance.currentLocation?.currentLat != ""{
                dict["currentLat"] = /LocationManager.sharedInstance.currentLocation?.currentLat
                dict["currentLong"] = /LocationManager.sharedInstance.currentLocation?.currentLng
            }
            if /page != ""{
                dict["pageNo"] = page
            }
            return   dict
            
        case .getVenueDetail(let venueId):
            return ["venueId": /venueId]
            
        case .joinVenue( let groupId , let userId , let isPrivate , let adminId):
            var dict = ["groupId": groupId , "groupType": "VENUE" , "adminId" : /adminId ]
            if /userId != ""{
                dict["userId"] = userId
            }
            if /isPrivate != ""{
                dict["isPrivate"] = isPrivate
            }
            return dict
            
        case .filterVenue( let date, let categoryId, let privateV , let lat, let long):
            var dict = [String:String]()
            if /date != ""{
                dict["date"] = date
            }
            if /categoryId != ""{
                dict["categoryId"] = categoryId
            }
            if /privateV != ""{
                dict["private"] = privateV
            }
            if /lat != ""{
                dict["locationLong"] = long
                dict["locationLat"] = lat
            }
            return dict
            
        case .getFilters():
            return ["flag" : "1"]
            
        case .createVenue( let categoryId , let venueImage , let venueDoc , let venueTitle , let venueLocation , let userName , let date , let venueLocName , let venueLocationLng , let tag , let privacy , let address , let participantIds):
            var dict = ["categoryId" : /categoryId , "venueTitle": /venueTitle , "venueLocationLat": /venueLocation  , "venueTime" : /date , "venueLocationName" : /venueLocName , "venueLocationLong": /venueLocationLng ]
            if /address != ""{
                dict["venueLocationAddress"] = /address
            }
            if /userName != ""{
                dict["venueAdditionalDetailsName"] = /userName
            }
            if /venueDoc != ""{
                dict["venueAdditionalDetailsDocs"] = /venueDoc
            }
            
            if /venueImage != ""{
                dict["groupImageOriginal"] = /venueImage
                dict["groupImageThumbnail"] = /venueImage
            }
            if /tag != ""{
                dict["venueTags"] = /tag
            }
            if /privacy != ""{
                dict["isPrivate"] = /privacy
            }
            if /participantIds != ""{
                dict["participantIds"] = /participantIds
            }
            
            
            return dict
            
        case .getVenueList(let currentLat , let  currentLong, let flag):
            var dict = ["flag":"2" ]
            if /currentLat != ""{
                dict["currentLat"] = /currentLat
                dict["currentLong"] = /currentLong
            }
            return dict
            
        case .getRequestsCount():
            return [:]
            
        case .venueConversationDetails(let groupId , let chatId):
            var dict = ["groupId" : /groupId , "chatId" : chatId]
            if /chatId == ""{
                dict.removeValue(forKey: "chatId")
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
            
        case .searchVenue(_):
            return APIConstants.searchVenue
            
        case .getVenueDetail(_):
            return APIConstants.venueDetails
            
        case .joinVenue(_):
            return APIConstants.joinVenue
            
        case .filterVenue(_):
            return APIConstants.filterData
            
        case .getFilters():
            return APIConstants.filters
            
        case .getVenueList(_):
            return APIConstants.getInfo
            
        case .createVenue(_):
            return APIConstants.createVenue
            
        case .venueConversationDetails(_):
            return APIConstants.venueConversationDetails
            
        case .getRequestsCount():
            return APIConstants.getRequestCount
            
        }
    }
    
    public var method: Moya.Method {
        switch self {
        case .getRequestsCount():
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
            VenueProvider.request(self, completion: { (result) in
                
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
            
        case .inviteusers(_):
            return nil
            
        case .getRequestsCount():
            return  Mapper<DictionaryResponse<RequestCount>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .createVenue(_) , .getFilters(_) , .joinVenue(_) :
            return nil
            
        case .filterVenue(_) , .searchVenue(_):
            return Mapper<DictionaryResponse<Venues>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .getVenueDetail(_):
            return Mapper<DictionaryResponse<Venues>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .getVenueList(_) :
            return Mapper<DictionaryResponse<VenueData>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .venueConversationDetails(_):
            return Mapper<DictionaryResponse<GroupConvoList>>().map(JSONObject: safeResponse.dictionaryObject)
        }
    }
}
