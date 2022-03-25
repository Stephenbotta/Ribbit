////
////  Notifications+API.swift
////  Connect
////
////  Created by OSX on 12/02/18.
////  Copyright © 2018 OSX. All rights reserved.
////
//
//import Moya
//import RxSwift
//import RxCocoa
//import Foundation
//import SwiftyJSON
//import ObjectMapper
//import EZSwiftExtensions
//
//
//
//
//enum NotificationsTarget {
//    
//    case getMyNotification(skip:String, limit:String)
//}
//
//fileprivate let NotificationsProvider = MoyaProvider <NotificationsTarget>(plugins: [NetworkLoggerPlugin(verbose: true, responseDataFormatter: JSONResponseDataFormatter)])
//
//extension NotificationsTarget: TargetType {
//    
//    var parameters:[String: Any] {
//        switch self {
//        case .getMyNotification(let skip, let limit):
//            return ["skip": /skip, "limit": /limit]
//        }
//    }
//    
//    
//    var task:Task {
//        switch self {
//        case .getMyNotification(_):
//            return .requestParameters(parameters: parameters, encoding: URLEncoding.default)
//        }
//        
//    }
//    
//    var headers: [String : String]?{
//        switch self {
//        default:
//            return ["Content-Type": "application/json", "authorization": token]
//        }
//    }
//    
//    public var token : String {
//        return /Singleton.sharedInstance.loggedInUser?.token
//    }
//    
//    public var baseURL: URL {
//        switch self {
//        default:
//            return URL(string: APIConstants.basePath)!
//        }
//    }
//    
//    public var path:String {
//        switch self {
//        case .getMyNotification(_):
//            return APIConstants.getMyNotification
//        }
//    }
//    
//    public var method: Moya.Method {
//        switch self {
//        default: return .get
//        }
//    }
//    
//    public var sampleData: Data { return Data() }
//    
//    func request(apiBarrier : Bool = false) -> Observable<Any?>{
//        
//        return Observable<Any?>.create { (observer) -> Disposable in
//            switch(self){
//            default:
//                if apiBarrier{
//                    self.showLoader()
//                }
//            }
//            let disposable = Disposables.create {}
//            NotificationsProvider.request(self, completion: { (result) in
//                
//                self.hideLoader()
//                switch result {
//                case let .success(moyaResponse):
//                    let data = moyaResponse.data
//                    let json = JSON(data)
//                    
//                    let status = self.handleResponse(json: json).serverValue
//                    if status == ResponseStatus.success.serverValue{
//                        observer.onNext(self.parse(response: json))
//                        observer.on(.completed)
//                    } else if status == ResponseStatus.blocked.serverValue{
//                        observer.onError(ResponseStatus.blocked)
//                        observer.on(.completed)
//                    } else if status == ResponseStatus.missingAuthentication.serverValue {
//                        UIApplication.shared.loginExpired()
//                        observer.onError(ResponseStatus.missingAuthentication)
//                        observer.on(.completed)
//                    } else{
//                        observer.onError(ResponseStatus.clientError(message: /("message" => json.dictionaryValue)))
//                        observer.on(.completed)
//                    }
//                    
//                // do something with the response data or statusCode
//                case let .failure(error):
//                    // this means there was a network failure - either the request
//                    // wasn't sent (connectivity), or no response was received (server
//                    // timed out).  If the server responds with a 4xx or 5xx error, that
//                    // will be sent as a ".success"-ful response.
//                    print(error.localizedDescription)
//                    observer.onError(ResponseStatus.noInternet)
//                    observer.on(.completed)
//                    break
//                }
//            })
//            
//            return disposable
//        }
//    }
//    
//    func showLoader() {
//        Loader.shared.start()
//    }
//    
//    func hideLoader(){
//        Loader.shared.stop()
//    }
//    
//    
//    func handleResponse(json : JSON) -> ResponseStatus{
//        switch self {
//        default:
//            return ResponseStatus.getRawEnum(value: /("status" => json.dictionaryValue))
//        }
//    }
//    
//    func parse(response : JSON?) -> Mappable? {
//        guard let safeResponse = response else { return nil }
//        switch self {
//        case .getMyNotification(_):
//            return Mapper<DictionaryResponse<GetMyNotifications>>().map(JSONObject: safeResponse.dictionaryObject)
//        }
//    }
//}
//
