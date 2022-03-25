
//
//  Venues.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 24/10/18.
//

import UIKit
import ObjectMapper
import RMMapper

class VenueData: NSObject, Mappable , RMMapping {
    
    var yourVenueData: [Venues]?
    var venueNearYou: [Venues]?
    
    required init?(map: Map){}
    override init(){}
    
    func mapping(map: Map)  {
        yourVenueData <- map["yourVenueData"]
        venueNearYou <- map["venueNearYou"]
        
    }
}

class Venues: NSObject, Mappable , RMMapping {
    
    var groupId: String?
    var venueTitle: String?
    var venueImageUrl:ImageUrl?
    var isPrivate: Bool?
    var memberCount: Int?
    var distance : Double?
    var locName : String?
    var latLong: [Any]?
    var isMine = false
    var adminId: String?
    var venueLocationAddress: String?
    var venueTime: Int?
    var venueTags: [String]?
    var createdBy: String?
    var requestStatus: String?
    var membersList : [Members]?
    var isMember : Bool?
    var conversationId: String?
    
    required init?(map: Map){}
    override init(){}
    
    func mapping(map: Map) {
        conversationId <- map["conversationId"]
        groupId <- map["groupId"]
        venueTitle <- map["venueTitle"]
        venueImageUrl <- map["imageUrl"]
        isPrivate <- map["isPrivate"]
        memberCount <- map["memberCount"]
        distance <- map["distance"]
        locName <- map["venueLocationName"]
        latLong <- map["venueLocation"]
        adminId <- map["adminId"]
        venueLocationAddress <- map["venueLocationAddress"]
        venueTime <- map["venueTime"]
        venueTags <- map["venueTags"]
        createdBy <- map["createdBy"]
        requestStatus <- map["requestStatus"]
        membersList <- map["membersList"]
        isMember <- map["isMember"]
        
    }
    
}
