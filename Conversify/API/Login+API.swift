//
//  Login+API.swift
//  Connect
//
//  Created by OSX on 01/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//

import Moya
import RxSwift
import RxCocoa
import Foundation
import SwiftyJSON
import ObjectMapper
import EZSwiftExtensions


func JSONResponseDataFormatter(_ data: Data) -> Data {
    do {
        let dataAsJSON = try JSONSerialization.jsonObject(with: data)
        let prettyData =  try JSONSerialization.data(withJSONObject: dataAsJSON, options: .prettyPrinted)
        return prettyData
    } catch {
        return data // fallback to original data if it can't be serialized.
    }
}


enum LoginTarget {
    
    case registerEmailNPhone(phoneNumber: String?, email:String?, countryCode: String?)
    case login(userCredentials: String? , phoneNumber: String?, countryCode: String? , email:String?, password: String? , facebookId: String? , googleId: String?, platform: String?)
    case signUp(userType:String? , fullName: String?, email:String?, phoneNumber: String?,password:String?, countryCode: String?, userName: String? , facebookId : String? , googleId: String? , flag: String?, platform: String? ,referralCode : String?)
    case verifyOTP(phoneNumber:String?, otpCode:String?, email:String?, countryCode: String?)
    case phoneVerification(otpCode:String?)
    case resendOTP(phoneNumber: String?, email:String?, countryCode: String?)
    case logout()
    case forgotPassword(phoneNumber: String?, email:String?, countryCode: String?)
    case getUserDetails(userCredentials: String?)
    case getInterests()
    case updateUserInterests(interests: String?)
    case userNameCheck(userName : String?)
    case updateDeviceToken()
    case promotUser(phoneNumber:[String])
    
}


fileprivate let LoginProvider = MoyaProvider <LoginTarget>(plugins: [NetworkLoggerPlugin()])


extension LoginTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
            
        case .phoneVerification(let otpCode):
            return ["OTPcode": /otpCode ]
            
        case .updateDeviceToken():
            return ["deviceToken": /Singleton.sharedInstance.loggedInUserDeviceToken, "apnsDeviceToken": /Singleton.sharedInstance.apnsDeviceToken]
            
        case .updateUserInterests( let interests):
            return ["categoryArray": interests]
            
        case .getInterests():
            return ["flag" : "1"]
            
        case .registerEmailNPhone(let phoneNumber, let email, let countryCode):
            if /email != ""{
                return ["email": /email ]
            }else{
                let formattedPhoneValue = (((phoneNumber?.replacingOccurrences(of: " ", with: ""))?.replacingOccurrences(of: "-", with: ""))?.replacingOccurrences(of: "(", with: ""))?.replacingOccurrences(of: ")", with: "")
                
                return ["phoneNumber": /formattedPhoneValue, "countryCode": /countryCode?.replacingOccurrences(of: " ", with: "") ]
            }
            
        case .login(let userCredentials , let phoneNumber, let countryCode , let email, let password , let facebookId , let googleId, let platform):
            var dict = [String: String]()
            
            if /password != ""{
                dict["password"] = password
            }
            
            if /userCredentials != ""{
                dict["userCredentials"] = userCredentials
            }
            
            //            if /email != ""{
            //                dict["email"] = email
            //            }
            //
            //            if /phoneNumber != ""{
            //                dict["phoneNumber"] = /phoneNumber?.replacingOccurrences(of: " ", with: "")
            //                dict["countryCode"] = /countryCode?.replacingOccurrences(of: " ", with: "")
            //            }
            
            if /facebookId != ""{
                dict["facebookId"] = facebookId
            }
            
            if /googleId != ""{
                dict["googleId"] = googleId
            }
            
           
            dict["platform"] = "ios"
            
            dict["deviceToken"] = /Singleton.sharedInstance.loggedInUserDeviceToken
            dict["apnsDeviceToken"] = /Singleton.sharedInstance.apnsDeviceToken
            return dict
            
        case .signUp(let userType , let fullName ,let email,let phoneNumber,let password, let countryCode, let userName , let facebookId , let googleId , let flag, let platform , let referralCode):
            
            var dict =  [String:String]()
            
            if /userType != ""{
                dict["userType"] = userType
            }
            if /fullName != ""{
                dict["fullName"] = fullName
            }
            if /userName != ""{
                dict["userName"] = userName
            }
            
            if /flag != ""{
                dict["flag"] = flag
            }
            
            if /password != ""{
                dict["password"] = password
            }
            if referralCode != ""{
                dict["referralCode"] = referralCode
            }
            if /email != ""{
                dict["email"] = email
            }
            dict["deviceId"] = /UIDevice.current.identifierForVendor?.uuidString
            if /phoneNumber != ""{
                let formattedPhoneValue = (((phoneNumber?.replacingOccurrences(of: " ", with: ""))?.replacingOccurrences(of: "-", with: ""))?.replacingOccurrences(of: "(", with: ""))?.replacingOccurrences(of: ")", with: "")
                dict["phoneNumber"] = /formattedPhoneValue
                dict["countryCode"] = /countryCode?.replacingOccurrences(of: " ", with: "")
            }
            
            if /facebookId != ""{
                dict["facebookId"] = facebookId
            }
            
            if /googleId != ""{
                dict["googleId"] = googleId
            }
           
            dict["platform"] = "ios"
            
            dict["deviceToken"] = /Singleton.sharedInstance.loggedInUserDeviceToken
            dict["apnsDeviceToken"] = /Singleton.sharedInstance.apnsDeviceToken
            
            return dict
            
        case .verifyOTP(let phoneNumber, let otpCode , let email , let countryCode):
            if /email != ""{
                return ["email": /email, "otp": /otpCode ]
            }else{
                let formattedPhoneValue = (((phoneNumber?.replacingOccurrences(of: " ", with: ""))?.replacingOccurrences(of: "-", with: ""))?.replacingOccurrences(of: "(", with: ""))?.replacingOccurrences(of: ")", with: "")
                var dict = ["phoneNumber": /formattedPhoneValue , "otp": /otpCode ]
                if  /countryCode?.replacingOccurrences(of: " ", with: "") != ""{
                    dict["countryCode"] = /countryCode?.replacingOccurrences(of: " ", with: "")
                }
                return dict
            }
            
        case .resendOTP(let phoneNumber, let email, let countryCode):
            if /email != ""{
                return ["email": /email ]
            }else{
                let formattedPhoneValue = (((phoneNumber?.replacingOccurrences(of: " ", with: ""))?.replacingOccurrences(of: "-", with: ""))?.replacingOccurrences(of: "(", with: ""))?.replacingOccurrences(of: ")", with: "")
                var dict = ["phoneNumber": /formattedPhoneValue  ]
                if  /countryCode?.replacingOccurrences(of: " ", with: "") != ""{
                    dict["countryCode"] = /countryCode?.replacingOccurrences(of: " ", with: "")
                }
                return dict
            }
            
        case .logout():
            return [:]
            
        case .forgotPassword(let phoneNumber, let email, let countryCode):
            if /email != ""{
                return ["email": /email ]
            }else{
                let formattedPhoneValue = (((phoneNumber?.replacingOccurrences(of: " ", with: ""))?.replacingOccurrences(of: "-", with: ""))?.replacingOccurrences(of: "(", with: ""))?.replacingOccurrences(of: ")", with: "")
                return ["phoneNumber": /formattedPhoneValue , "countryCode": /countryCode?.replacingOccurrences(of: " ", with: "") ]
            }
            
        case .getUserDetails(let userCredentials):
            let phone = /(((userCredentials?.replacingOccurrences(of: " ", with: ""))?.replacingOccurrences(of: "-", with: ""))?.replacingOccurrences(of: "(", with: ""))?.replacingOccurrences(of: ")", with: "")
            return ["userCredentials": /phone , "platform": "ios", "deviceToken" : /Singleton.sharedInstance.loggedInUserDeviceToken, "apnsDeviceToken" : /Singleton.sharedInstance.apnsDeviceToken]
            
        case .userNameCheck(let userName):
            return ["userName" : /userName]
        case .promotUser(let phoneNumber):
            return ["phoneNumbers" : phoneNumber,"platform" : "ios"]
        }
    }
    
    var task:Task {
        switch self {
        case .login(_) , .registerEmailNPhone(_) , .getInterests() , .updateUserInterests(_) , .getUserDetails(_) , .userNameCheck(_) , .updateDeviceToken() , .phoneVerification(_),.promotUser(_):
            return .requestParameters(parameters: parameters, encoding: JSONEncoding.default)
        case .signUp(_), .verifyOTP(_), .resendOTP(_), .forgotPassword(_):
            return .requestParameters(parameters: parameters, encoding: JSONEncoding.default)
        case .logout():
            return .requestParameters(parameters: parameters, encoding: JSONEncoding.default)
        }
    }
    
    var headers: [String : String]?{
        switch self {
        case .logout() , .getInterests() , .updateUserInterests(_) , .updateDeviceToken() , .phoneVerification(_),.promotUser(_):
            return ["Content-type": "application/json", "authorization": "bearer " + /Singleton.sharedInstance.loggedInUser?.token]
        default:
            return ["Content-type": "application/json"]
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
        case .phoneVerification(_):
            return APIConstants.verifyPhone
        case .updateDeviceToken():
            return APIConstants.updateDeviceToken
        case .getUserDetails(_):
            return APIConstants.login
        case .updateUserInterests(_):
            return APIConstants.updateInterests
        case .getInterests():
            return APIConstants.getInfo
        case .registerEmailNPhone(_):
            return APIConstants.register
        case .login(_):
            return APIConstants.login
        case .signUp(_):
            return APIConstants.userSignUp
        case .logout():
            return APIConstants.logout
        case .verifyOTP(_):
            return APIConstants.verifyOTP
        case .resendOTP(_):
            return APIConstants.resendOTP
        case .forgotPassword(_):
            return APIConstants.setResetPassword
        case .userNameCheck(_):
            return APIConstants.userNameCheck
        case .promotUser(_):
            return APIConstants.promoteUser
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
            LoginProvider.request(self, completion: { (result) in
                
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
        case .getInterests():
            return Mapper<DictionaryResponse<Interests>>().map(JSONObject: safeResponse.dictionaryObject)
        case .login(_), .verifyOTP(_) , .registerEmailNPhone(_) , .getUserDetails(_) , .updateUserInterests(_) , .updateDeviceToken() , .phoneVerification(_):
            return Mapper<DictionaryResponse<User>>().map(JSONObject: safeResponse.dictionaryObject)
        case .signUp(_):
            return Mapper<DictionaryResponse<User>>().map(JSONObject: safeResponse.dictionaryObject)
        case .logout(), .resendOTP(_) , .forgotPassword(_),.promotUser(_):
            return nil
        case .userNameCheck(_):
            return Mapper<DictionaryResponse<CheckUserName>>().map(JSONObject: safeResponse.dictionaryObject)
        }
    }
}
