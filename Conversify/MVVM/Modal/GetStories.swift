//
//  GetStories.swift
//  Conversify
//
//  Created by admin on 02/04/21.
//

import Foundation
import Moya
import ObjectMapper
class StoriesDetail : Mappable{
    var message : String?
    var data : [StoriesData]?
    var daiLyChailenge : [DailyChalengeData]?
    init() {
        
    }
    required init?(map: Map){}
    func mapping(map: Map) {
        message <- map["message"]
    data <- map["data"]
        daiLyChailenge <- map["data"]
    }
}
class StoriesData: Mappable{
    var id : String?
    var firstName : String?
    var lastName : String?
    var imageUrl : StoriesImage?
    var stories : [Stories]?
    
init() {
}
required init?(map: Map){}
    
    
func mapping(map: Map) {
    id <- map["_id"]
    firstName <- map["firstName"]
    lastName <- map["lastName"]
    imageUrl <- map["imageUrl"]
    stories <- map["stories"]
}
}



class StoriesImage : Mappable{
    var original : String?
    var thumbnail : String?
init() {
    
}
required init?(map: Map){}
    func mapping(map: Map) {
        original <- map["original"]
        thumbnail <- map["thumbnail"]
    }
   
}
class Stories : Mappable{
    var id : String?
    var media : StoriesMedia?
    var createdOn : Int?
    var isDeleted : Bool?
    var isBlocked : Bool?
    var postBy : String?
    var expirationTime : Int?
    var viewBy : [ViewUser]?
    var isSeen : Int?
init() {
    
}
required init?(map: Map){}
    func mapping(map: Map) {
        id <- map["_id"]
        media <- map["media"]
        createdOn <- map["createdOn"]
        isDeleted <- map["isDeleted"]
        isBlocked <- map["isBlocked"]
        postBy <- map["postBy"]
        expirationTime <- map["expirationTime"]
        viewBy <- map["viewBy"]
        isSeen <- map["isSeen"]
    }
}
class  ViewUser : Mappable{
    var name : Int?
    init(){
    }
    required init?(map: Map) {
    }
    
    func mapping(map: Map) {
        name <- map["name"]
    }
    
    
}
class StoriesMedia : Mappable{
    var original : String?
    var thumbnail : String?
    var mediaType : String?
    var videoUrl : String?
init() {
    
}
required init?(map: Map){}
    func mapping(map: Map) {
        original <- map["original"]
        thumbnail <- map["thumbnail"]
        mediaType <- map["mediaType"]
        videoUrl <- map["videoUrl"]
    }
}
class DailyChalengeData : Mappable{
   var  _id : String?
    var  title : String?
    var description : String?
    var rewardPoint : Int?
    var type: String?
    var isDeleted: Bool?
    var isBlocked: Bool?
    var isActive: Bool?
    var createdAt: String?
    var updatedAt: String?
    var __v :Int?
    init() {
        
    }
    required init?(map: Map){}
        func mapping(map: Map) {
            _id <- map["_id"]
            title <- map["title"]
            description <- map["description"]
            rewardPoint <- map["rewardPoint"]
            type <- map["type"]
            isDeleted <- map["isDeleted"]
            isBlocked <- map["isBlocked"]
            isActive <- map["isActive"]
            createdAt <- map["createdAt"]
            updatedAt <- map["updatedAt"]
            __v <- map["__v"]
          
        }
}
