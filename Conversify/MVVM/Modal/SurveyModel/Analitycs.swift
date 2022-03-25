//
//  Analitycs.swift
//  Conversify
//
//  Created by admin on 04/05/21.
//

import Foundation
import ObjectMapper

class AnalitycsData : Mappable {
    
    var points_by_dates : [PointByDates]?
    var points_earned : [PointEarned]?
    var points_redeem : [PointRedeem]?
    var messagesCount : [MessageCount]?
    var postCount_byDate : [PostByDate]?
    var commentCount_byDate : [CommentByDate]?
    var storiesCount_byDate : [StoriesByDates]?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        points_by_dates <- map["points_by_dates"]
        points_earned <- map["points_earned"]
        points_redeem <- map["points_redeem"]
        messagesCount <- map["messagesCount"]
        postCount_byDate <- map["postCount_byDate"]
        commentCount_byDate <- map["commentCount_byDate"]
        storiesCount_byDate <- map["storiesCount_byDate"]
    }
}
class PointByDates : Mappable{
    var date : String?
    var totalPointEarned : Int?
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        date <- map["date"]
        totalPointEarned <- map["totalPointEarned"]
    }
}
class PointEarned : Mappable{
    var source : String?
    var totalPointEarned : Int?
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        source <- map["source"]
        totalPointEarned <- map["totalPointEarned"]
    }
}
class PointRedeem : Mappable{
    
    var source : String?
    var totalPointRedeem : Int?
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        source <- map["source"]
        totalPointRedeem <- map["totalPointRedeem"]
    }
}
class MessageCount : Mappable{
    var date : String?
    var sentMessages : Int?
    var recieveMessages : Int?
    
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        date <- map["date"]
        sentMessages <- map["sentMessages"]
        recieveMessages <- map["recieveMessages"]
    }
}
class PostByDate : Mappable{
    var _id : String?
    var date : String?
    var count : Int?
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        _id <- map["_id"]
        date <- map["date"]
        count <- map["count"]
    }
}
class CommentByDate : Mappable{
    var _id : String?
    var date : String?
    var count : Int?
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        _id <- map["_id"]
        date <- map["date"]
        count <- map["count"]
    }
}
class StoriesByDates : Mappable{
    var _id : String?
    var date : String?
    var count : Int?
    
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        _id <- map["_id"]
        date <- map["date"]
        count <- map["count"]
    }
}
