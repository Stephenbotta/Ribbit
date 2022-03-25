//
//  Survey+Api.swift
//  Conversify
//
//  Created by Apple on 06/12/19.
//

import UIKit
import Moya
import SwiftyJSON
import ObjectMapper
import EZSwiftExtensions
import RxSwift
import RxCocoa

enum SurveyTarget {
    case submitSurvey(gender : String? , race : String?, dateOfBirth : String? , houseHoldIncome : String? , homeOwnership : String? , education : String? , employementStatus : String? , maritalStatus : String? )
    case getTakeSurveyProperties
    case getSurvey(pageNo : String? , searchTxt : String?)
    case getQuestions(surveyId : String?)
    case submitUserQuesSurvey(surveyId: String? , questions : String?)
    case getChallenges
    case getChallengeDetail(challengeId : String?)
    case startChallenge(challengeId : String?)
    case tangoGetCatalog
    case showRedeemHistory
    case tangoPostOrders(faceValue: String?, utid: String?)
    case showCharityOrgList
    case addCharityDonation(organizationId: String?, givenPoint: String?)
    case getPointEarnedHistory
}

fileprivate let SurveyTargetProvider = MoyaProvider <SurveyTarget>(plugins: [NetworkLoggerPlugin()])

extension SurveyTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
        case .getTakeSurveyProperties, .tangoGetCatalog, .showRedeemHistory, .showCharityOrgList , .getPointEarnedHistory : return [:]
        case .getSurvey(let pageNo , _) :
            return ["pageNo" : /pageNo , "limit" : "10"]
        case .submitSurvey(let gender, let race, let dateOfBirth, let houseHoldIncome,let homeOwnership, let education,let employementStatus, let maritalStatus):
            return ["gender" : /gender , "race" : /race , "dateOfBirth" : /dateOfBirth , "houseHoldIncome" : /houseHoldIncome , "homeOwnership" : /homeOwnership , "education" : /education ,"employementStatus" : /employementStatus , "maritalStatus" : /maritalStatus]
        case .getQuestions(let surveyId):
            return ["surveyId" : /surveyId]
        case .submitUserQuesSurvey(let surveyId, let questions) :
            return ["surveyId" : /surveyId ,"questions" : /questions]
        case .getChallenges , .getChallengeDetail(_) , .startChallenge(_) : return [:]
        case .tangoPostOrders(let faceValue, let utid):
            return ["faceValue" : /faceValue ,"utid" : /utid]

        case .addCharityDonation(let organizationId, let givenPoint):
            return ["organizationId" : /organizationId ,"dollars" : /givenPoint]
        }
    }
    
    var parameterEncoding: ParameterEncoding {
        switch self {
        case .getTakeSurveyProperties , .getSurvey(_,_) , .getQuestions(_) , .getChallenges , .getChallengeDetail(_) , .startChallenge(_), .tangoGetCatalog, .showRedeemHistory, .showCharityOrgList ,.getPointEarnedHistory:
            return URLEncoding.queryString
        default: return JSONEncoding.default
        }
    }
    
    
    var task:Task {
        switch self {
        case .getTakeSurveyProperties , .getChallenges , .getChallengeDetail(_) , .startChallenge(_), .tangoGetCatalog, .showRedeemHistory, .showCharityOrgList:  return .requestParameters(parameters: parameters, encoding: parameterEncoding)
        default :
            return .requestParameters(parameters: parameters, encoding: parameterEncoding)
            
        }
    }
    
    var headers: [String : String]?{
        switch self {
//        case .getTakeSurveyProperties :
//            return ["Content-type": "application/json"]
        default:
            return ["Content-type": "application/json" , "authorization" : "bearer " + /Singleton.sharedInstance.loggedInUser?.token]
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
        case .getPointEarnedHistory : return APIConstants.getPointEarnedHistory
        case .getTakeSurveyProperties : return APIConstants.getTakeSurveyProperties
        case .submitSurvey(_, _, _, _,_, _,_, _): return APIConstants.takeSurveyProperties
        case .getSurvey(_, _) : return APIConstants.getSurveyList
        case .getQuestions(_) : return APIConstants.getSurveyQuestions
        case .submitUserQuesSurvey(_, _) : return APIConstants.sumitUserSurvey
        case .getChallengeDetail(let id) : return APIConstants.getChallenges + "/\(/id)"
        case .startChallenge(let id) : return APIConstants.getChallenges + "/\(/id)" + "/start"
        case .getChallenges : return APIConstants.getChallenges
        case .tangoGetCatalog: return APIConstants.tangoGetCatalog
        case .showRedeemHistory: return APIConstants.showRedeemHistory
        case .tangoPostOrders(_): return APIConstants.tangoPostOrders
        case .showCharityOrgList: return APIConstants.showCharityOrgList
        case .addCharityDonation(_): return APIConstants.addCharityDonation
        }
    }
    
    public var method: Moya.Method {
        switch self {
        case .getTakeSurveyProperties , .getSurvey(_, _) , .getQuestions(_) , .getChallenges , .getChallengeDetail(_) , .startChallenge(_), .tangoGetCatalog, .showRedeemHistory, .showCharityOrgList  , .getPointEarnedHistory: return .get
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
            
            SurveyTargetProvider.request(self, completion: { (result) in
                
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
        print(response)
        switch self {
        
        case .getTakeSurveyProperties :
            return Mapper<DictionaryResponse<SurveyDataList>>().map(JSONObject: safeResponse.dictionaryObject)
        case .getSurvey(_,_):
            return Mapper<DictionaryResponse<SurveyListingModel>>().map(JSONObject: safeResponse.dictionaryObject)
        case .getQuestions(_):
            return Mapper<DictionaryResponse<SurveyQuesModel>>().map(JSONObject: safeResponse.dictionaryObject)
        case .getPointEarnedHistory:
            return Mapper<DictionaryResponse<AnalitycsData>>().map(JSONObject: safeResponse.dictionaryObject)
        case .getChallenges :
            return Mapper<DictionaryResponse<ChallengesModel>>().map(JSONObject: safeResponse.dictionaryObject)
        case .getChallengeDetail(_) , .startChallenge(_) :
            return Mapper<DictionaryResponse<ChallengesList>>().map(JSONObject: safeResponse.dictionaryObject)
        case .tangoGetCatalog :
            return Mapper<DictionaryResponse<GiftCard>>().map(JSONObject: safeResponse.dictionaryObject)
        case .showRedeemHistory :
            return Mapper<DictionaryResponse<Redeem>>().map(JSONObject: safeResponse.dictionaryObject)
        case .showCharityOrgList :
            let object =  Mapper<DictionaryResponse<Organization>>().map(JSON: safeResponse.dictionaryObject!)
            return object
        default : return nil
        }
    }
}
