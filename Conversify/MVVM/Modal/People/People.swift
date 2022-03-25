//
//  People.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 28/11/18.
//
//    Model file generated using JSONExport: https://github.com/Ahmed-Ali/JSONExport

import Foundation
import ObjectMapper



class PeopleData : NSObject, NSCoding, Mappable{
    
    var locationName : String?
    var timestamp : Int?
    var userCrossed : [UserCrossed]?
     var locationAddress: String?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return PeopleData()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        locationName <- map["locationName"]
        timestamp <- map["timestamp"]
        userCrossed <- map["userCrossed"]
        locationAddress <- map["locationAddress"]
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        locationName = aDecoder.decodeObject(forKey: "locationName") as? String
        timestamp = aDecoder.decodeObject(forKey: "timestamp") as? Int
        userCrossed = aDecoder.decodeObject(forKey: "userCrossed") as? [UserCrossed]
        
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if locationName != nil{
            aCoder.encode(locationName, forKey: "locationName")
        }
        if timestamp != nil{
            aCoder.encode(timestamp, forKey: "timestamp")
        }
        if userCrossed != nil{
            aCoder.encode(userCrossed, forKey: "userCrossed")
        }
        
    }
    
}


//
//    UserCrossed.swift
//
//    Create by cbl24_Mac_mini on 29/11/2018
//    Copyright © 2018. All rights reserved.
//    Model file generated using JSONExport: https://github.com/Ahmed-Ali/JSONExport



class UserCrossed : NSObject, NSCoding, Mappable{
    
    var id : String?
    var conversationId : String?
    var crossedUserId : UserList?
    var location : [Float]?
    var locationAddress : String?
    var locationName : String?
    var time : Int?
    
    class func newInstance(map: Map) -> Mappable?{
        return UserCrossed()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        id <- map["_id"]
        
        conversationId <- map["conversationId"]
        crossedUserId <- map["crossedUserId"]
        location <- map["location"]
        locationAddress <- map["locationAddress"]
        locationName <- map["locationName"]
        time <- map["time"]
        
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        id = aDecoder.decodeObject(forKey: "_id") as? String
        conversationId = aDecoder.decodeObject(forKey: "conversationId") as? String
        crossedUserId = aDecoder.decodeObject(forKey: "crossedUserId") as? UserList
        location = aDecoder.decodeObject(forKey: "location") as? [Float]
        locationAddress = aDecoder.decodeObject(forKey: "locationAddress") as? String
        locationName = aDecoder.decodeObject(forKey: "locationName") as? String
        time = aDecoder.decodeObject(forKey: "time") as? Int
        
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
        if conversationId != nil{
            aCoder.encode(conversationId, forKey: "conversationId")
        }
        if crossedUserId != nil{
            aCoder.encode(crossedUserId, forKey: "crossedUserId")
        }
        if location != nil{
            aCoder.encode(location, forKey: "location")
        }
        if locationAddress != nil{
            aCoder.encode(locationAddress, forKey: "locationAddress")
        }
        if locationName != nil{
            aCoder.encode(locationName, forKey: "locationName")
        }
        if time != nil{
            aCoder.encode(time, forKey: "time")
        }
        
    }
    
}


//
//    CrossedUserId.swift
//
//    Create by cbl24_Mac_mini on 29/11/2018
//    Copyright © 2018. All rights reserved.
//    Model file generated using JSONExport: https://github.com/Ahmed-Ali/JSONExport

import Foundation
import ObjectMapper


class CrossedUserId : NSObject, NSCoding, Mappable{
    
    var id : String?
    var bio : String?
    var imageUrl : ImageUrl?
    var fullName: String?
    var userName: String?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return CrossedUserId()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        id <- map["_id"]
        bio <- map["bio"]
        imageUrl <- map["imageUrl"]
        fullName <- map["fullName"]
        userName <- map["userName"]
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        id = aDecoder.decodeObject(forKey: "_id") as? String
        bio = aDecoder.decodeObject(forKey: "bio") as? String
        imageUrl = aDecoder.decodeObject(forKey: "imageUrl") as? ImageUrl
        
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
        if bio != nil{
            aCoder.encode(bio, forKey: "bio")
        }
        if imageUrl != nil{
            aCoder.encode(imageUrl, forKey: "imageUrl")
        }
        
    }
    
}






class Tags : NSObject, NSCoding, Mappable{
    
    var id : String?
    var imageUrl : ImageUrl?
    var isFollowing : Bool?
    var tagName : String?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return Tags()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        id <- map["_id"]
        imageUrl <- map["imageUrl"]
        isFollowing <- map["isFollowing"]
        tagName <- map["tagName"]
        
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        id = aDecoder.decodeObject(forKey: "_id") as? String
        imageUrl = aDecoder.decodeObject(forKey: "imageUrl") as? ImageUrl
        isFollowing = aDecoder.decodeObject(forKey: "isFollowing") as? Bool
        tagName = aDecoder.decodeObject(forKey: "tagName") as? String
        
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
        if imageUrl != nil{
            aCoder.encode(imageUrl, forKey: "imageUrl")
        }
        if isFollowing != nil{
            aCoder.encode(isFollowing, forKey: "isFollowing")
        }
        if tagName != nil{
            aCoder.encode(tagName, forKey: "tagName")
        }
        
    }
    
}
