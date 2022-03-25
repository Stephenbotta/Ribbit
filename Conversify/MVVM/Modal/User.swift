//
//  User.swift
//  Connect
//
//  Created by OSX on 02/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//


import ObjectMapper
import RMMapper
import Foundation


class User : NSObject, NSCoding, Mappable , RMMapping {
    
    var groupName : String?
    var name : String?
    var token:String?
    var firstName:String?
    var lastName:String?
    var phoneNumber:String?
    var countryCode:String?
    var about:String?
    var thumbnailImage:String?
    var originalImage:String?
    var email:String?
    var fullPhone:String?
    var user_id:Int?
    var id:String?
    var distance:Double?
    var otp:String?
    var facebookId: String?
    var googleId: String?
    var isVerified: Bool?
    var isPasswordExist : Bool?

    var password: String?
    var img: ImageUrl?
    var isProfileComplete: Bool?
    var userName: String?
    var isInterestSelected : Bool?
    var intersts: [Interests]?
    var image: ImageUrl?
    var interestTags : [Interests]?
    var selectedForGroup = false
    var website : String?
    var designation : String?
    var bio : String?
    var followingCount : Int?
    var followerCount : Int?
    var referralCode : String?
    var company : String?
    var gender : String?
    var conversationId: String?
    var groupCount: Int?
    var isSelected : Bool?
    var isEmailVerified : Bool?
    var isPassportVerified : Bool?
    var isPhoneNumberVerified : Bool?
    var isUploaded:Bool?
    var imageVisibilityForFollowers: Bool?
    var imageVisibility : [User]?
    var nameVisibilityForFollowers: Bool?
    var nameVisibility: [User]?
    var tagPermissionForFollowers : Bool?
    var tagPermission: [User]?
    var personalInfoVisibilityForFollowers: Bool?
    var personalInfoVisibility: [User]?
    var isBlocked : Bool?
    var isAlert: Bool?
    var isAccountPrivate: Bool?
    var imageVisibilityForEveryone : Bool?
    var nameVisibilityForEveryone : Bool?
    var tagPermissionForEveryone : Bool?
    var userType : String?
    var isTakeSurvey : Bool?
    var pointRedeemed : Double?
    var pointEarned : Double?
    var amount : Double?
    var totalSurveys : Int?
    var fullphoneNumber : String?
    var askForFollowBack : Bool?
    var infoVisibilityForEveryone: Bool?
    var qrCodeStr : String?
    var totalSurveyGiven : Int?
  
    
    class func newInstance(map: Map) -> Mappable?{
        return User()
    }
    required init?(map: Map){}
    override init(){}
    
    func mapping(map: Map)  {
        infoVisibilityForEveryone <- map["personalInfoVisibilityForEveryone"]
        askForFollowBack <- map["askForFollowBack"]
        fullphoneNumber <- map["fullphoneNumber"]
        pointRedeemed <- map["pointRedeemed"]
        pointEarned <- map["pointEarned"]
        totalSurveys <- map["totalSurveys"]
        totalSurveyGiven <- map["totalSurveyGiven"]
        amount <- map["amount"]
        userType <- map["userType"]
        imageVisibilityForEveryone <- map["imageVisibilityForEveryone"]
        nameVisibilityForEveryone <- map["nameVisibilityForEveryone"]
        tagPermissionForEveryone <- map["tagPermissionForEveryone"]
        isTakeSurvey <- map["isTakeSurvey"]
        isAccountPrivate <- map["isAccountPrivate"]
        isAlert <- map["alertNotifications"]
        isBlocked <- map["isBlocked"]
        imageVisibilityForFollowers <- map["imageVisibilityForFollowers"]
        nameVisibilityForFollowers <- map["nameVisibilityForFollowers"]
        tagPermissionForFollowers <- map["tagPermissionForFollowers"]
        personalInfoVisibilityForFollowers <- map["personalInfoVisibilityForFollowers"]
        imageVisibility <- map["imageVisibility"]
        nameVisibility <- map["nameVisibility"]
        tagPermission <- map["tagPermission"]
        personalInfoVisibility <- map["personalInfoVisibility"]
        isUploaded <- map["isUploaded"]
        isEmailVerified <- map["isEmailVerified"]
        isPassportVerified <- map["isPassportVerified"]
        isPhoneNumberVerified <- map["isPhoneNumberVerified"]
        isSelected = false
        groupCount <- map["groupCount"]
        conversationId <- map["conversationId"]
        gender <- map["gender"]
        company <- map["company"]
        qrCodeStr <- map["QRCode"]
        website <- map["website"]
        followerCount <- map["followerCount"]
        followingCount <- map["followingCount"]
        designation <- map["designation"]
        bio <- map["bio"]
        groupName <- map["groupName"]
        name <- map["name"]
        token <- map["accessToken"]
        firstName <- map["fullName"]
        lastName <- map["lastName"]
        phoneNumber <- map["phoneNumber"]
        countryCode <- map["countryCode"]
        about <- map["about"]
        thumbnailImage <- map["thumbnailImage"]
        originalImage <- map["originalImage"]
        email <- map["email"]
        fullPhone <- map["fullPhone"]
        id <- map["_id"]
        user_id <- map["user_id"]
        distance <- map["distance"]
        otp <- map["OTPcode"]
        googleId <- map["googleId"]
        facebookId <- map["facebookId"]
        isVerified <- map["isVerified"]
        isProfileComplete <- map["isProfileComplete"]
        img <- map["imageUrl"]
        userName <- map["userName"]
        isInterestSelected <- map["isInterestSelected"]
        isPasswordExist <- map["isPasswordExist"]
        intersts <- map["interestTags"]
        referralCode <- map["referralCode"]
        if Singleton.sharedInstance.loggedInUser == nil || /Singleton.sharedInstance.loggedInUser?.id == id && /intersts?.count > 2{
            Singleton.sharedInstance.selectedInterests = intersts ?? []
        }
        image <- map["imageUrl"]
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        askForFollowBack = aDecoder.decodeObject(forKey: "askForFollowBack") as? Bool
        fullphoneNumber = aDecoder.decodeObject(forKey: "fullphoneNumber") as? String
        pointEarned = aDecoder.decodeObject(forKey: "pointEarned") as? Double
        pointRedeemed = aDecoder.decodeObject(forKey: "pointRedeemed") as? Double
        totalSurveys = aDecoder.decodeObject(forKey: "totalSurveys") as? Int
        userType = aDecoder.decodeObject(forKey: "userType") as? String
        imageVisibilityForEveryone = aDecoder.decodeObject(forKey: "imageVisibilityForEveryone") as? Bool
        infoVisibilityForEveryone = aDecoder.decodeObject(forKey: "personalInfoVisibilityForEveryone") as? Bool
        nameVisibilityForEveryone = aDecoder.decodeObject(forKey: "nameVisibilityForEveryone") as? Bool
        tagPermissionForEveryone = aDecoder.decodeObject(forKey: "tagPermissionForEveryone") as? Bool
        isTakeSurvey = aDecoder.decodeObject(forKey: "isTakeSurvey") as? Bool
        isAccountPrivate = aDecoder.decodeObject(forKey: "isAccountPrivate") as? Bool
        isAlert = aDecoder.decodeObject(forKey: "alertNotifications") as? Bool
        imageVisibilityForFollowers = aDecoder.decodeObject(forKey: "imageVisibilityForFollowers") as? Bool
        nameVisibilityForFollowers = aDecoder.decodeObject(forKey: "nameVisibilityForFollowers") as? Bool
        tagPermissionForFollowers = aDecoder.decodeObject(forKey: "tagPermissionForFollowers") as? Bool
        personalInfoVisibilityForFollowers = aDecoder.decodeObject(forKey: "personalInfoVisibilityForFollowers") as? Bool
        imageVisibility = aDecoder.decodeObject(forKey: "imageVisibility") as? [User]
        nameVisibility = aDecoder.decodeObject(forKey: "nameVisibility") as? [User]
        tagPermission = aDecoder.decodeObject(forKey: "tagPermission") as? [User]
        personalInfoVisibility = aDecoder.decodeObject(forKey: "personalInfoVisibility") as? [User]
        isUploaded = aDecoder.decodeObject(forKey: "isUploaded") as? Bool
        isEmailVerified = aDecoder.decodeObject(forKey: "isEmailVerified") as? Bool
        isPassportVerified = aDecoder.decodeObject(forKey: "isPassportVerified") as? Bool
        isPhoneNumberVerified = aDecoder.decodeObject(forKey: "isPhoneNumberVerified") as? Bool
        groupCount = aDecoder.decodeObject(forKey: "groupCount") as? Int
        gender = aDecoder.decodeObject(forKey: "gender") as? String
        company = aDecoder.decodeObject(forKey: "company") as? String
        website = aDecoder.decodeObject(forKey: "website") as? String
        userName = aDecoder.decodeObject(forKey: "userName") as? String
        token = aDecoder.decodeObject(forKey: "token") as? String
        firstName = aDecoder.decodeObject(forKey: "firstName") as? String
        lastName = aDecoder.decodeObject(forKey: "lastName") as? String
        phoneNumber = aDecoder.decodeObject(forKey: "phoneNumber") as? String
        countryCode = aDecoder.decodeObject(forKey: "countryCode") as? String
        about = aDecoder.decodeObject(forKey: "about") as? String
        thumbnailImage = aDecoder.decodeObject(forKey: "thumbnailImage") as? String
        originalImage = aDecoder.decodeObject(forKey: "originalImage") as? String
        email = aDecoder.decodeObject(forKey: "email") as? String
        otp = aDecoder.decodeObject(forKey: "otpCode") as? String
        id = aDecoder.decodeObject(forKey: "_id") as? String
        distance = aDecoder.decodeObject(forKey: "distance") as? Double
        isVerified = aDecoder.decodeObject(forKey: "isVerified") as? Bool
        googleId = aDecoder.decodeObject(forKey: "googleId") as? String
        facebookId = aDecoder.decodeObject(forKey: "facebookId") as? String
        isPasswordExist = aDecoder.decodeObject(forKey: "isPasswordExist") as? Bool
        isProfileComplete = aDecoder.decodeObject(forKey: "isProfileComplete") as? Bool
        isInterestSelected = aDecoder.decodeObject(forKey: "isInterestSelected") as? Bool
        img = aDecoder.decodeObject(forKey: "imageUrl") as? ImageUrl
        designation = aDecoder.decodeObject(forKey: "designation") as? String
        bio  = aDecoder.decodeObject(forKey: "bio") as? String
        followingCount  = aDecoder.decodeObject(forKey: "followingCount") as? Int
        followerCount = aDecoder.decodeObject(forKey: "followerCount") as? Int
        interestTags = aDecoder.decodeObject(forKey: "interestTags") as? [Interests]
        
         qrCodeStr = aDecoder.decodeObject(forKey: "QRCode") as? String
        
        totalSurveyGiven = aDecoder.decodeObject(forKey: "totalSurveyGiven") as? Int
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder){
        
        if askForFollowBack != nil{
            aCoder.encode(askForFollowBack, forKey: "askForFollowBack")
        }
        if fullphoneNumber != nil{
            aCoder.encode(fullphoneNumber, forKey: "fullphoneNumber")
        }
        if totalSurveys != nil{
            aCoder.encode(totalSurveys, forKey: "totalSurveys")
        }
        if pointEarned != nil{
            aCoder.encode(pointEarned, forKey: "pointEarned")
        }
        if pointRedeemed != nil{
            aCoder.encode(pointRedeemed, forKey: "pointRedeemed")
        }
        if isTakeSurvey != nil{
            aCoder.encode(isTakeSurvey, forKey: "isTakeSurvey")
        }
        if imageVisibilityForEveryone != nil{
            aCoder.encode(imageVisibilityForEveryone, forKey: "imageVisibilityForEveryone")
        }
        if nameVisibilityForEveryone != nil{
            aCoder.encode(nameVisibilityForEveryone, forKey: "nameVisibilityForEveryone")
        }
        if tagPermissionForEveryone != nil{
            aCoder.encode(tagPermissionForEveryone, forKey: "tagPermissionForEveryone")
        }
         if infoVisibilityForEveryone != nil{
             aCoder.encode(infoVisibilityForEveryone, forKey: "personalInfoVisibilityForEveryone")
        }

        if isAccountPrivate != nil{
            aCoder.encode(isAccountPrivate, forKey: "isAccountPrivate")
        }
        if isAlert != nil{
            aCoder.encode(isAlert, forKey: "alertNotifications")
        }
        if imageVisibility != nil{
            aCoder.encode(imageVisibility, forKey: "imageVisibility")
        }
        if nameVisibility != nil{
            aCoder.encode(nameVisibility, forKey: "nameVisibility")
        }
        if tagPermission != nil{
            aCoder.encode(tagPermission, forKey: "tagPermission")
        }
        if personalInfoVisibilityForFollowers != nil{
            aCoder.encode(personalInfoVisibilityForFollowers, forKey: "personalInfoVisibilityForFollowers")
        }
        if imageVisibilityForFollowers != nil{
            aCoder.encode(imageVisibilityForFollowers, forKey: "imageVisibilityForFollowers")
        }
        if nameVisibilityForFollowers != nil{
            aCoder.encode(nameVisibilityForFollowers, forKey: "nameVisibilityForFollowers")
        }
        if tagPermissionForFollowers != nil{
            aCoder.encode(tagPermissionForFollowers, forKey: "tagPermissionForFollowers")
        }
        if personalInfoVisibility != nil{
            aCoder.encode(personalInfoVisibility, forKey: "personalInfoVisibility")
        }
        
        if isPhoneNumberVerified != nil{
            aCoder.encode(isPhoneNumberVerified, forKey: "isPhoneNumberVerified")
        }
        if isPhoneNumberVerified != nil{
            aCoder.encode(isPhoneNumberVerified, forKey: "isPhoneNumberVerified")
        }
        if isPhoneNumberVerified != nil{
            aCoder.encode(isPhoneNumberVerified, forKey: "isPhoneNumberVerified")
        }
        if isPhoneNumberVerified != nil{
            aCoder.encode(isPhoneNumberVerified, forKey: "isPhoneNumberVerified")
        }
        
        if isPhoneNumberVerified != nil{
            aCoder.encode(isPhoneNumberVerified, forKey: "isPhoneNumberVerified")
        }
        if isEmailVerified != nil{
            aCoder.encode(isEmailVerified, forKey: "isEmailVerified")
        }
        if isPassportVerified != nil{
            aCoder.encode(isPassportVerified, forKey: "isPassportVerified")
        }
        if isUploaded != nil{
            aCoder.encode(isUploaded, forKey: "isUploaded")
        }
        if groupCount != nil{
            aCoder.encode(groupCount, forKey: "groupCount")
        }
        if company != nil{
            aCoder.encode(company, forKey: "company")
        }
        if gender != nil{
            aCoder.encode(gender, forKey: "gender")
        }
        
        if userName != nil{
            aCoder.encode(userName, forKey: "userName")
        }
        
        if website != nil{
            aCoder.encode(website, forKey: "website")
        }
        if isPasswordExist != nil{
            aCoder.encode(isPasswordExist, forKey: "isPasswordExist")
        }
        
        if isInterestSelected != nil{
            aCoder.encode(isInterestSelected, forKey: "isInterestSelected")
        }
        if isProfileComplete != nil{
            aCoder.encode(isProfileComplete, forKey: "isProfileComplete")
        }
        if isVerified != nil{
            aCoder.encode(isVerified, forKey: "isVerified")
        }
        if googleId != nil{
            aCoder.encode(googleId, forKey: "googleId")
        }
        if facebookId != nil{
            aCoder.encode(facebookId, forKey: "facebookId")
        }
        
        if token != nil{
            aCoder.encode(token, forKey: "token")
        }
        
        if firstName != nil{
            aCoder.encode(firstName, forKey: "firstName")
        }
        if lastName != nil{
            aCoder.encode(lastName, forKey: "lastName")
        }
        if phoneNumber != nil{
            aCoder.encode(phoneNumber, forKey: "phoneNumber")
        }
        if countryCode != nil{
            aCoder.encode(countryCode, forKey: "countryCode")
        }
        if about != nil{
            aCoder.encode(about, forKey: "about")
        }
        if thumbnailImage != nil{
            aCoder.encode(thumbnailImage, forKey: "thumbnailImage")
        }
        if originalImage != nil{
            aCoder.encode(originalImage, forKey: "originalImage")
        }
        if email != nil{
            aCoder.encode(email, forKey: "email")
        }
        if otp != nil {
            aCoder.encode(otp, forKey: "otpCode")
        }
        if id != nil {
            aCoder.encode(id, forKey: "_id")
        }
        if distance != nil {
            aCoder.encode(distance, forKey: "distance")
        }
        if img != nil {
            aCoder.encode(img, forKey: "imageUrl")
        }
        if designation != nil{
            aCoder.encode(designation, forKey: "designation")
        }
        if bio != nil{
            aCoder.encode(bio, forKey: "bio")
        }
        if followingCount != nil{
            aCoder.encode(followingCount, forKey: "followingCount")
        }
        if followerCount != nil{
            aCoder.encode(followerCount, forKey: "followerCount")
        }
        
        if interestTags != nil{
            aCoder.encode(interestTags, forKey: "interestTags")
        }
        
        if userType != nil{
            aCoder.encode(imageVisibilityForEveryone, forKey: "userType")
        }
        
        
        if qrCodeStr != nil {
            aCoder.encode(qrCodeStr, forKey: "QRCode")
        }
        
        if totalSurveyGiven != nil {
            aCoder.encode(totalSurveyGiven, forKey: "totalSurveyGiven")
        }
    }
    
    
}
