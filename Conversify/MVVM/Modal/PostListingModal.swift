//
//  PostListingModal.swift
//  Conversify
//
//  Created by Apple on 15/11/18.
//

import Foundation
import ObjectMapper

class PostList : Mappable {
    
    var id : String?
    var imageUrl : ImageUrl?
    var postText : String?
    var likesCount : Int?
    var commentCount : Int?
    var liked : Bool?
    var postBy : PostBy?
    var hashTags : [String]?
    var postCategory : PostCategory?
    var createdOn : Double?
    var groupDetail : SuggestedGroup?
    var groupId : String?
    var postCategoryId : String?
    var comment : [CommentList]?
    var location : [Float]?
    var locationAddress : String?
    var locationName : String?
    var postType: String?
    var media : [Media]?
    
    
    init() {
        
    }
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        media <- map["media"]
        postType <- map["postType"]
        location <- map["location"]
        locationAddress <- map["locationAddress"]
        locationName <- map["locationName"]
        groupDetail <- map["groupId"]
        id <- map["_id"]
        imageUrl <- map["imageUrl"]
        postText <- map["postText"]
        likesCount <- map["likeCount"]
        commentCount <- map["commentCount"]
        liked <- map["liked"]
        postBy <- map["postBy"]
        hashTags <- map["hashTags"]
        postCategory <- map["postCategory"]
        createdOn <- map["createdOn"]
        groupId <- map["groupId"]
        postCategoryId <- map["postCategoryId"]
        comment <- map["comment"]
        if map["postCategoryId"].currentValue as? [String: Any] != nil {
            postCategory <- map["postCategoryId"]
        }
    }
}

class CommentList : Mappable {
    
    var id : String?
    var comment : String?
    var userIdTag : [String]?
    var createdOn : Double?
    var likeCount : Int?
    var replyCount : Int?
    var liked : Bool?
    var commentBy : PostBy?
    var loadedElementCount : Int? = 0
    var isHide : Bool = true
    var isIndicator: Bool = false
    var attachment : Attachment?
    required init?(map: Map){}
    
    init(comment: String) {
        
    }
    
    func mapping(map: Map)  {
        id <- map["_id"]
        comment <- map["comment"]
        userIdTag <- map["userIdTag"]
        createdOn <- map["createdOn"]
        likeCount <- map["likeCount"]
        replyCount <- map["replyCount"]
        liked <- map["liked"]
        commentBy <- map["commentBy"]
        attachment <- map["attachmentUrl"]
    }
}

class ReplyList : Mappable {
    
    var id : String?
    var reply : String?
    var userIdTag : [String]?
    var createdOn : Double?
    var likeCount : Int?
    var replyCount : Int?
    var liked : Bool?
    var replyBy : PostBy?
    
    init() {
        
    }
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        id <- map["_id"]
        reply <- map["reply"]
        userIdTag <- map["userIdTag"]
        createdOn <- map["createdOn"]
        likeCount <- map["likeCount"]
        replyCount <- map["replyCount"]
        liked <- map["liked"]
        replyBy <- map["replyBy"]
    }
}
class PostBy : Mappable {
    
    var id : String?
    var imageUrl : ImageUrl?
    var fullName : String?
    var userName : String?
    
    required init?(map: Map){}
    
    init(id: String?) {
        self.id = Singleton.sharedInstance.loggedInUser?.id
        self.imageUrl = Singleton.sharedInstance.loggedInUser?.img
        self.fullName = Singleton.sharedInstance.loggedInUser?.firstName
        self.userName = Singleton.sharedInstance.loggedInUser?.userName
    }
    
    func mapping(map: Map)  {
        id <- map["_id"]
        imageUrl <- map["imageUrl"]
        fullName <- map["fullName"]
        userName <- map["userName"]
    }
}
class Attachment : Mappable {
    
    var original : String?
    var thumbnail : String?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        original <- map["original"]
        thumbnail <- map["thumbnail"]
    }
}
class PostCategory : Mappable {
    
    var id : String?
    var categoryName : String?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        id <- map["_id"]
        categoryName <- map["categoryName"]
    }
    
}
class UserList : Mappable {
    
    var isAccountPrivate: Bool?
    var fullName : String?
    var userName : String?
    var id : String?
    var imageUrl : ImageUrl?
    var interestTags : [Interests]?
    var designation : String?
    var bio : String?
    var email : String?
    var followingCount : Int?
    var followerCount : Int?
    var conversationId : String?
    var isOnline : Bool?
    var isFollowing : Bool?
    var isBlocked: Bool?
    var isEmailVerified: Bool?
    var isPhoneNumberVerified: Bool?
    var isUploaded: Bool?
    var userType : String?
    var askForFollowBack : Bool?
    var amount : Double?
    var referralCode : String?
    required init?(map: Map){}
    init() {
        
    }
    
    
    func mapping(map: Map)  {
        referralCode <- map["referralCode"]
        askForFollowBack <- map["askForFollowBack"]
        userType <- map["userType"]
        isUploaded <- map["isUploaded"]
        isPhoneNumberVerified <- map["isPhoneNumberVerified"]
        isEmailVerified <- map["isEmailVerified"]
        isAccountPrivate <- map["isAccountPrivate"]
        isBlocked <- map["isBlocked"]
        isFollowing <- map["isFollowing"]
        isOnline <- map["isOnline"]
        amount <- map["amount"]
        conversationId <- map["conversationId"]
        followerCount <- map["followerCount"]
        followingCount <- map["followingCount"]
        email <- map["email"]
        fullName <- map["fullName"]
        userName <- map["userName"]
        id <- map["_id"]
        imageUrl <- map["imageUrl"]
        interestTags <- map["interestTags"]
        designation <- map["designation"]
        bio <- map["bio"]
    }
}

class InterestTags : Mappable {
    
    var id : String?
    var categoryName : String?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        id <- map["_id"]
        categoryName <- map["categoryName"]
    }
}

class CheckUserName : Mappable {
    
    var isNameAvailable : Bool?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        isNameAvailable <- map["type"]
        
    }
}

class Media : NSObject, NSCoding, Mappable{
    
    var id : String?
    var mediaType : String?
    var original : String?
    var thumbnail : String?
    var videoUrl : String?
    var likeCount: Int?
    var likePercent: Double?
    var isMostLiked : Bool?
    
    
    class func newInstance(map: Map) -> Mappable?{
        return Media()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        id <- map["_id"]
        mediaType <- map["mediaType"]
        original <- map["original"]
        thumbnail <- map["thumbnail"]
        videoUrl <- map["videoUrl"]
        likeCount <- map["likeCount"]
        isMostLiked <- map["isMostLiked"]
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        id = aDecoder.decodeObject(forKey: "_id") as? String
        mediaType = aDecoder.decodeObject(forKey: "mediaType") as? String
        original = aDecoder.decodeObject(forKey: "original") as? String
        thumbnail = aDecoder.decodeObject(forKey: "thumbnail") as? String
        videoUrl = aDecoder.decodeObject(forKey: "videoUrl") as? String
        
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
        if mediaType != nil{
            aCoder.encode(mediaType, forKey: "mediaType")
        }
        if original != nil{
            aCoder.encode(original, forKey: "original")
        }
        if thumbnail != nil{
            aCoder.encode(thumbnail, forKey: "thumbnail")
        }
        if videoUrl != nil{
            aCoder.encode(videoUrl, forKey: "videoUrl")
        }
        
    }
    
}

class TwitterModelNew : Codable {
    var id_str: String?
    var created_at : String?
    var favorite_count : Double?
    var text : String?
    var retweet_count : Double?
    var media_url_https: String?
    var media: [TwitterMedia]?
    var profile_image_url : String?
    var name : String?
    var screen_name : String?
    var favorited : Bool?
    var posturl: String?
    var mediaUrl :[Entities]?
    var retweeted : Bool?
    
    
    enum CodingKeys: String, CodingKey {
        case extended_entities
        case created_at
        case favorite_count
        case text
        case retweet_count
        case user
        case media
        case media_url_https
        case profile_image_url
        case name
        case screen_name
        case favorited
        case id_str
        case entities
        case mediaUrl
        case retweeted
    }
    
    
    required init(from decoder: Decoder) {
        do {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            created_at = try container.decode(String.self, forKey: .created_at)
            favorite_count = try container.decode(Double.self, forKey: .favorite_count)
            retweet_count = try container.decode(Double.self, forKey: .retweet_count)
            text = try container.decode(String.self, forKey: .text)
            //retweet_count = try container.decode(Double.self, forKey: .retweet_count)
            favorited = try container.decode(Bool.self, forKey: .favorited)
            retweeted = try container.decode(Bool.self, forKey: .retweeted)
            id_str = try container.decode(String.self, forKey: .id_str)
            
            if container.contains(.extended_entities) {
                
                let companyContainer = try container.nestedContainer(keyedBy: CodingKeys.self, forKey: .extended_entities)
                media = try companyContainer.decode([TwitterMedia].self, forKey: .media)
                
            }
            
            if container.contains(.user) {
                
                let userContainer = try container.nestedContainer(keyedBy: CodingKeys.self, forKey: .user)
                profile_image_url = try userContainer.decode(String.self, forKey: .profile_image_url)
                name = try userContainer.decode(String.self, forKey: .name)
                screen_name = try userContainer.decode(String.self, forKey: .screen_name)
                if userContainer.contains(.entities) {
                    
                    let mContainer = try container.nestedContainer(keyedBy: CodingKeys.self, forKey: .entities)
                    mediaUrl = try mContainer.decode([Entities].self, forKey: .media)
                    
                }
            }
        }
            
        catch {
            debugPrint(error.localizedDescription)
        }
        
    }
    
    func encode(to encoder: Encoder) {
        //        var container = try encoder.container(keyedBy: CodingKeys.self)
        //        do {
        //        try container.encode(extended_entities, forKey: .extended_entities)
        //            } catch {
        //                       debugPrint(error.localizedDescription)
        //                   }
    }
}
class UserObject : Codable {
    var profile_image_url : String?
    var name : String?
    var screen_name : String?
    
    enum CodingKeys: String, CodingKey {
        case profile_image_url
        case name
        case screen_name
        
    }
    required init(from decoder: Decoder) {
        do {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            profile_image_url = try container.decode(String.self, forKey: .profile_image_url)
            name = try container.decode(String.self, forKey: .name)
            screen_name = try container.decode(String.self, forKey: .screen_name)
            
        }
            
        catch {
            debugPrint(error.localizedDescription)
        }
        
    }
    
    func encode(to encoder: Encoder) {
    }
}

class ExtendedEntities : Codable {
    var media : TwitterMedia?
    
    enum CodingKeys: String, CodingKey {
        case media
        
    }
    required init(from decoder: Decoder) {
        do {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            media = try container.decode(TwitterMedia.self, forKey: .media)
            
        }
            
        catch {
            debugPrint(error.localizedDescription)
        }
        
    }
    
    func encode(to encoder: Encoder) {
    }
    
}
class Entities : Codable {
    var media : PostUrl?
    
    enum CodingKeys: String, CodingKey {
        case media
        
    }
    required init(from decoder: Decoder) {
        do {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            media = try container.decode(PostUrl.self, forKey: .media)
            
        }
            
        catch {
            debugPrint(error.localizedDescription)
        }
        
    }
    
    func encode(to encoder: Encoder) {
    }
    
}


class TwitterMedia : Codable {
    var media_url_https : String?
    
    enum CodingKeys: String, CodingKey {
        case media_url_https
        
    }
    required init(from decoder: Decoder) {
        do {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            media_url_https = try container.decode(String.self, forKey: .media_url_https)
            
        }
            
        catch {
            debugPrint(error.localizedDescription)
        }
        
    }
    
    func encode(to encoder: Encoder) {
    }
}
class PostUrl : Codable {
    
    var urls : String?
    enum CodingKeys: String, CodingKey {
        case urls
        
    }
    required init(from decoder: Decoder) {
        do {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            
            urls = try container.decode(String.self, forKey: .urls)
        }
            
        catch {
            debugPrint(error.localizedDescription)
        }
        
    }
    
    func encode(to encoder: Encoder) {
    }
}


class SearchTwitterModelNew: Codable {
    
    var searchResults : [TwitterModelNew]?
    
    enum CodingKeys: String, CodingKey {
        
        case statuses
    }
    required init(from decoder: Decoder) {
        do {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            //let mContainer = try container.nestedContainer(keyedBy: CodingKeys.self, forKey: .statuses)
           // mediaUrl = try mContainer.decode([Entities].self, forKey: .media)
            searchResults = try container.decode([TwitterModelNew].self, forKey: .statuses)
            
        }
        catch {
            debugPrint(error.localizedDescription)
        }
   
    }
    
    func encode(to encoder: Encoder) {
    }
}
