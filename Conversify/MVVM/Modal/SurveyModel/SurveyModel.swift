//
//  SurveyModel.swift
//  Conversify
//
//  Created by Apple on 06/12/19.
//

import Foundation
import ObjectMapper

enum MediaType : String{
    case txt = "TEXT"
    case video = "VIDEO"
    case img = "IMAGE"
}

enum ChallengeStatus : String {
    case notstarted = "notstarted"
    case inprogress = "inprogress"
    case completed = "completed"
}

class SurveyDataList : Mappable {
    
    var dateOfBirth : Double?
    var gender : [SurveyValues]?
    var race : [SurveyValues]?
    var houseHoldIncome : [SurveyValues]?
    var homeOwnership : [SurveyValues]?
    var education : [SurveyValues]?
    var employementStatus : [SurveyValues]?
    var maritalStatus : [SurveyValues]?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        dateOfBirth <- map["dateOfBirth"]
        gender <- map["gender"]
        race <- map["race"]
        houseHoldIncome <- map["houseHoldIncome"]
        homeOwnership <- map["homeOwnership"]
        education <- map["education"]
        employementStatus <- map["employementStatus"]
        maritalStatus <- map["maritalStatus"]
    }
}

class SurveyValues : Mappable {
    var keyName : String?
    var isSelected : Int?
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        keyName <- map["key"]
        isSelected <- map["isSelected"]
    }
    init(){
        
    }
}
class SurveyListingModel : Mappable {
    
    var surveyList : [SurveyModel]?
    var currentPage : Int?
    var pages : Int?
    var totalCount : Int?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        surveyList <- map["info"]
        currentPage <- map["currentPage"]
        pages <- map["pages"]
        totalCount <- map["totalCount"]
    }
}

class SurveyModel : Mappable {
    
    var questionCount : Int?
    var name : String?
    var description : String?
    var expiryDate : Double?
    var totalTime: Double?
    var rewardPoints : Int?
    var surveyId : String?
    var _id : String?
    var media : [MediaModel]?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        questionCount <- map["questionCount"]
        media <- map["media"]
        rewardPoints <- map["rewardPoints"]
        totalTime <- map["totalTime"]
        name <- map["name"]
        description <- map["description"]
        expiryDate <- map["expiryDate"]
        surveyId <- map["id"]
        _id <- map["_id"]
    }
}

class MediaModel : Mappable{
    
    var thumbnail : String?
    var original : String?
    var videoUrl : String?
    var mediaType : MediaType?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        videoUrl <- map["videoUrl"]
        original <- map["original"]
        thumbnail <- map["thumbnail"]
        mediaType <- (map["mediaType"],EnumTransform<MediaType>())
    }
}

class SurveyQuesModel : Mappable {
    
    var quesInfo : [SurveyQuestions]?
    var totalCount : Int?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        quesInfo <- map["info"]
        totalCount <- map["totalCount"]
    }
}

class SurveyQuestions : Mappable {
    
    var surveyId : String?
    var name : String?
    var questionType : Int?
    var options : [SurveyAnswer]?
    var media : MediaModel?
    var quesId : String?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        quesId <- map["_id"]
        surveyId <- map["surveyId"]
        name <- map["name"]
        questionType <- map["questionType"]
        options <- map["options"]
        media <- map["media"]
    }
    
}

class SurveyAnswer : Mappable {
    
    var name : String?
    var _id : String?
    var isSelected : Bool = false
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        name <- map["name"]
        _id <- map["_id"]
    }
}

class ChallengesModel :  Mappable {
    
    var challenges : [ChallengesList]?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        challenges <- map["challenges"]
    }
}

class ChallengesList : Mappable {
    
    var _id : String?
    var imgUrl : ImageUrl?
    var title : String?
    var challengeDescription : String?
    var challengeType : String?
    var quantity : Int?
    var rewardPoint : Int?
    var startDate : String?
    var endDate : String?
    var status : ChallengeStatus?
    var otherChallengeInProgress : ChallengeInProgressModel?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        imgUrl <- map["imageUrl"]
        _id <- map["_id"]
        title <- map["title"]
        challengeDescription <- map["description"]
        rewardPoint <- map["rewardPoint"]
        startDate <- map["startDate"]
        endDate <- map["endDate"]
        quantity <- map["quantity"]
        challengeType <- map["challengeType"]
        status <- (map["status"], EnumTransform<ChallengeStatus>())
        otherChallengeInProgress <- map["otherChallengeInProgress"]
    }
}
class ChallengeInProgressModel : Mappable {
    var status : ChallengeStatus?
    var _id : String?
    var challenge_id : String?
    var otherChallengeInProgress : String?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        status <- (map["status"], EnumTransform<ChallengeStatus>())
        _id <- map["_id"]
        challenge_id <- map["challenge_id"]
    }
}
 

class UserStats : Mappable {
    
    var pointRedeemed : Double?
    var totalSurveyGiven : Double?
    var pointEarned : Double?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        pointRedeemed <- map["pointRedeemed"]
        totalSurveyGiven <- map["totalSurveyGiven"]
        pointEarned <- map["pointEarned"]
    }
}
