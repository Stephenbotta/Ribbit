//
//  Notification.swift
//  Conversify
//
//  Created by Apple on 29/11/18.
//

import UIKit
import ObjectMapper

class AppNotification: Mappable {
    
    var id : String?
    var user : UserList?
    var pushBy: User?
    var type : AppNotificationType?
    var post : PostList?
    var createdOn : Date?
    var actionPerformed = false
    var isRead = false
    var group: Group?
    var venue: Venue?
    var followedBy: User?
    var toId: String?
    var postId: String?
    var comment : CommentList?
    var reply: ReplyList?
    var locName: String?
    var locAddress : String?
    var latLong: [Any]?
    var text : String?
    
    
    init() {
        
    }
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        locAddress <- map["locationAddress"]
        locName <- map["locationName"]
        latLong <- map["location"]
        reply <- map["reply"]
        comment <- map["commentId"]
        toId <- map["toId"]
        id <- map["_id"]
        pushBy <- map["byId"]
        user <- map["byId"]
        if let notifType = map["type"].currentValue as? String {
            type = AppNotificationType(rawValue: notifType)
        }
        post <- map["postId"]
        actionPerformed <- map["actionPerformed"]
        isRead <- map["isRead"]
        group <- map["groupId"]
        venue <- map["venueId"]
        if let date = map["createdOn"].currentValue as? Double {
            createdOn = Date(milliseconds: date)
        }
        followedBy <- map["byId"]
        text <- map["text"]
    }
}

class Group: Mappable {
    
    var id : String?
    var title : String?
    var image: ImageUrl?
    var conversationId: String?
    
    init() {
        
    }
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        id <- map["_id"]
        title <- map["groupName"]
        image <- map["imageUrl"]
        conversationId <- map["conversationId"]
    }
}

class Venue: Mappable {
    
    var id : String?
    var title : String?
    var  conversationId: String?
    
    init() {
        
    }
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        id <- map["_id"]
        title <- map["venueTitle"]
        conversationId <- map["conversationId"]
    }
}


class RequestCount : NSObject, NSCoding, Mappable{
    
    var requestCount : Int?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return RequestCount()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        requestCount <- map["requestCount"]
        
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        requestCount = aDecoder.decodeObject(forKey: "requestCount") as? Int
        
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if requestCount != nil{
            aCoder.encode(requestCount, forKey: "requestCount")
        }
        
    }
    
}
