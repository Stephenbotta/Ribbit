//
//    Data.swift
//
//    Create by Himanshu Upadhyay on 14/11/2018
//    Copyright © 2018. All rights reserved.
//    Model file generated using JSONExport: https://github.com/Ahmed-Ali/JSONExport

import Foundation
import ObjectMapper


class GroupData : NSObject, NSCoding, Mappable{
    
    var suggestedGroups : [SuggestedGroup]?
    var yourGroups : [YourGroup]?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return GroupData()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        suggestedGroups <- map["suggestedGroups"]
        yourGroups <- map["yourGroups"]
        
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        suggestedGroups = aDecoder.decodeObject(forKey: "suggestedGroups") as? [SuggestedGroup]
        yourGroups = aDecoder.decodeObject(forKey: "yourGroups") as? [YourGroup]
        
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if suggestedGroups != nil{
            aCoder.encode(suggestedGroups, forKey: "suggestedGroups")
        }
        if yourGroups != nil{
            aCoder.encode(yourGroups, forKey: "yourGroups")
        }
        
    }
    
}


//
//    SuggestedGroup.swift
//



class SuggestedGroup : NSObject, NSCoding, Mappable{
    
    var id : String?
    var groupName : String?
    var imageUrl : ImageUrl?
    var isMember : Bool?
    var isPrivate : Bool?
    var memberCounts : Int?
    var adminId : String?
    var conversationId: String?
    var requestStatus: String?
    var createdBy: String?
    
    class func newInstance(map: Map) -> Mappable?{
        return SuggestedGroup()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        createdBy <- map["createdBy"]
        id <- map["_id"]
        groupName <- map["groupName"]
        imageUrl <- map["imageUrl"]
        isMember <- map["isMember"]
        isPrivate <- map["isPrivate"]
        memberCounts <- map["memberCounts"]
        adminId <- map["adminId"]
        conversationId <- map["conversationId"]
        requestStatus <- map["requestStatus"]
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        id = aDecoder.decodeObject(forKey: "_id") as? String
        groupName = aDecoder.decodeObject(forKey: "groupName") as? String
        imageUrl = aDecoder.decodeObject(forKey: "imageUrl") as? ImageUrl
        isMember = aDecoder.decodeObject(forKey: "isMember") as? Bool
        isPrivate = aDecoder.decodeObject(forKey: "isPrivate") as? Bool
        memberCounts = aDecoder.decodeObject(forKey: "memberCounts") as? Int
        
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if id != nil{
            aCoder.encode(id, forKey: "_id")
        }
        if groupName != nil{
            aCoder.encode(groupName, forKey: "groupName")
        }
        if imageUrl != nil{
            aCoder.encode(imageUrl, forKey: "imageUrl")
        }
        if isMember != nil{
            aCoder.encode(isMember, forKey: "isMember")
        }
        if isPrivate != nil{
            aCoder.encode(isPrivate, forKey: "isPrivate")
        }
        if memberCounts != nil{
            aCoder.encode(memberCounts, forKey: "memberCounts")
        }
        
    }
    
}

//
class YourGroup : NSObject, NSCoding, Mappable{
    
    var id : String?
    var createdOn : Int?
    var groupName : String?
    var imageUrl : ImageUrl?
    var unReadCounts : Int?
    var adminId: String?
    var conversationId: String?
    var membersList : [Members]?
    var notification : Bool?
    var memberCounts : Int?
    var createdBy : String?
    var desc: String?
    var isPrivate: Bool?
    
    class func newInstance(map: Map) -> Mappable?{
        return YourGroup()
    }
    required init?(map: Map){}
    private override init(){
        super.init()
    }
    
    init(id: String? , createOn: Int? , groupName: String? , image: ImageUrl? , adminId: String? , conversationId: String? , memberList: [Members]? , notification: Bool? , memberCounts: Int , createdby: String , desc: String? , isPrivate: Bool , unReadCount: Int?  ) {
        super.init()
        self.isPrivate = isPrivate
        self.id  = id
        self.createdOn  = createOn
        self.groupName  = groupName
        self.imageUrl  = image
        self.unReadCounts  = unReadCount
        self.adminId  = adminId
        self.conversationId  = conversationId
        self.memberCounts  = memberCounts
        self.createdBy  = createdby
        self.desc  = desc
        self.notification  = notification
    }
    
    func mapping(map: Map)
    {
        isPrivate <- map["isPrivate"]
        id <- map["_id"]
        createdOn <- map["createdOn"]
        groupName <- map["groupName"]
        imageUrl <- map["imageUrl"]
        unReadCounts <- map["unReadCounts"]
        adminId <- map["adminId"]
        conversationId <- map["conversationId"]
        membersList <- map["membersList"]
        memberCounts <- map["memberCounts"]
        createdBy <- map["createdBy"]
        desc <- map["description"]
        notification <- map["notification"]
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        id = aDecoder.decodeObject(forKey: "_id") as? String
        createdOn = aDecoder.decodeObject(forKey: "createdOn") as? Int
        groupName = aDecoder.decodeObject(forKey: "groupName") as? String
        imageUrl = aDecoder.decodeObject(forKey: "imageUrl") as? ImageUrl
        unReadCounts = aDecoder.decodeObject(forKey: "unReadCounts") as? Int
        
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if id != nil{
            aCoder.encode(id, forKey: "_id")
        }
        if createdOn != nil{
            aCoder.encode(createdOn, forKey: "createdOn")
        }
        if groupName != nil{
            aCoder.encode(groupName, forKey: "groupName")
        }
        if imageUrl != nil{
            aCoder.encode(imageUrl, forKey: "imageUrl")
        }
        if unReadCounts != nil{
            aCoder.encode(unReadCounts, forKey: "unReadCounts")
        }
        
    }
    
}



//GROUPS'S POST


//
//    Data.swift
//
//    Create by cbl24_Mac_mini on 15/11/2018
//    Copyright © 2018. All rights reserved.
//    Model file generated using JSONExport: https://github.com/Ahmed-Ali/JSONExport

import Foundation
import ObjectMapper


class GroupsPostData : NSObject, NSCoding, Mappable{
    
    var conversData : [ConversData]?
    var groupName : String?
    var isMember : Bool?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return GroupsPostData()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        conversData <- map["conversData"]
        groupName <- map["groupName"]
        isMember <- map["isMember"]
        
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        conversData = aDecoder.decodeObject(forKey: "conversData") as? [ConversData]
        groupName = aDecoder.decodeObject(forKey: "groupName") as? String
        isMember = aDecoder.decodeObject(forKey: "isMember") as? Bool
        
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if conversData != nil{
            aCoder.encode(conversData, forKey: "conversData")
        }
        if groupName != nil{
            aCoder.encode(groupName, forKey: "groupName")
        }
        if isMember != nil{
            aCoder.encode(isMember, forKey: "isMember")
        }
        
    }
    
}




class ConversData : NSObject, NSCoding, Mappable{
    
    var id : String?
    var commentCount : Int?
    var createdOn : Int?
    var imageUrl : ImageUrl?
    var likeCount : Int?
    var liked : Bool?
    var postBy : PostBy?
    var postText : String?
    var media: [Media]?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return ConversData()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        id <- map["_id"]
        media <- map["media"]
        commentCount <- map["commentCount"]
        createdOn <- map["createdOn"]
        imageUrl <- map["imageUrl"]
        likeCount <- map["likeCount"]
        liked <- map["liked"]
        postBy <- map["postBy"]
        postText <- map["postText"]
        
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        id = aDecoder.decodeObject(forKey: "_id") as? String
        commentCount = aDecoder.decodeObject(forKey: "commentCount") as? Int
        createdOn = aDecoder.decodeObject(forKey: "createdOn") as? Int
        imageUrl = aDecoder.decodeObject(forKey: "imageUrl") as? ImageUrl
        likeCount = aDecoder.decodeObject(forKey: "likeCount") as? Int
        liked = aDecoder.decodeObject(forKey: "liked") as? Bool
        postBy = aDecoder.decodeObject(forKey: "postBy") as? PostBy
        postText = aDecoder.decodeObject(forKey: "postText") as? String
        
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if id != nil{
            aCoder.encode(id, forKey: "_id")
        }
        if commentCount != nil{
            aCoder.encode(commentCount, forKey: "commentCount")
        }
        if createdOn != nil{
            aCoder.encode(createdOn, forKey: "createdOn")
        }
        if imageUrl != nil{
            aCoder.encode(imageUrl, forKey: "imageUrl")
        }
        if likeCount != nil{
            aCoder.encode(likeCount, forKey: "likeCount")
        }
        if liked != nil{
            aCoder.encode(liked, forKey: "liked")
        }
        if postBy != nil{
            aCoder.encode(postBy, forKey: "postBy")
        }
        if postText != nil{
            aCoder.encode(postText, forKey: "postText")
        }
        
    }
    
}


//class PostBy : NSObject, NSCoding, Mappable{
//
//    var id : String?
//    var fullName : String?
//    var imageUrl : ImageUrl?
//
//
//    class func newInstance(map: Map) -> Mappable?{
//        return PostBy()
//    }
//    required init?(map: Map){}
//    private override init(){}
//
//    func mapping(map: Map)
//    {
//        id <- map["_id"]
//        fullName <- map["fullName"]
//        imageUrl <- map["imageUrl"]
//
//    }
//
//    /**
//     * NSCoding required initializer.
//     * Fills the data from the passed decoder
//     */
//    @objc required init(coder aDecoder: NSCoder)
//    {
//        id = aDecoder.decodeObject(forKey: "_id") as? String
//        fullName = aDecoder.decodeObject(forKey: "fullName") as? String
//        imageUrl = aDecoder.decodeObject(forKey: "imageUrl") as? ImageUrl
//
//    }
//
//    /**
//     * NSCoding required method.
//     * Encodes mode properties into the decoder
//     */
//    @objc func encode(with aCoder: NSCoder)
//    {
//        if id != nil{
//            aCoder.encode(id, forKey: "_id")
//        }
//        if fullName != nil{
//            aCoder.encode(fullName, forKey: "fullName")
//        }
//        if imageUrl != nil{
//            aCoder.encode(imageUrl, forKey: "imageUrl")
//        }
//
//    }
//
//}


class Verification : NSObject, NSCoding, Mappable{
    
    var id : String?
    var isEmailVerified : Bool?
    var isPassportVerified : Bool?
    var isPhoneNumberVerified : Bool?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return Verification()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map){
        id <- map["_id"]
        isEmailVerified <- map["isEmailVerified"]
        isPassportVerified <- map["isPassportVerified"]
        isPhoneNumberVerified <- map["isPhoneNumberVerified"]
        
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder){
        id = aDecoder.decodeObject(forKey: "_id") as? String
        isEmailVerified = aDecoder.decodeObject(forKey: "isEmailVerified") as? Bool
        isPassportVerified = aDecoder.decodeObject(forKey: "isPassportVerified") as? Bool
        isPhoneNumberVerified = aDecoder.decodeObject(forKey: "isPhoneNumberVerified") as? Bool
        
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder){
        if id != nil{
            aCoder.encode(id, forKey: "_id")
        }
        if isEmailVerified != nil{
            aCoder.encode(isEmailVerified, forKey: "isEmailVerified")
        }
        if isPassportVerified != nil{
            aCoder.encode(isPassportVerified, forKey: "isPassportVerified")
        }
        if isPhoneNumberVerified != nil{
            aCoder.encode(isPhoneNumberVerified, forKey: "isPhoneNumberVerified")
        }
        
    }
    
}
