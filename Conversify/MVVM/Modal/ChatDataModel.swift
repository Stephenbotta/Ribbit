//
//  ChatDataModel.swift
//  Conversify
//
//  Created by Apple on 02/11/18.
//

import Foundation
import ObjectMapper

enum MessageType: Int {
    case Text = 1
    case Image = 2
    case Video = 3
    case Attachement = 4
    case Property = 5
    case Audio = 6
    
    func cellID(isOwn: Bool) -> String {
        switch self {
        case .Text:
            return isOwn ? "SenderTxtCell" : "ReceiverTxtCell"
        case .Image, .Video , .Property:
            return isOwn ? "SenderImgCell" : "ReceiverImgCell"
        case .Attachement:
            return isOwn ? "SenderAtchCell" : "ReceiverAtchCell"
        case .Audio :
            return isOwn ? "SenderAudioCell" : "ReceiverAudioCell"
        }
    }
    
    var height: CGFloat {
        switch self {
        case .Text, .Attachement , .Audio:
            return UITableView.automaticDimension
        case .Image, .Video , .Property:
            return UIScreen.main.bounds.size.width * 0.5
        }
    }
}

class VenueGroupChat : Mappable {
    
    var msgData : GroupConvoList?
    
    required init?(map: Map){}
    
    
    func mapping(map: Map)  {
        msgData <- map["data"]
    }
}

class GroupConvoList : Mappable {
    
    var chatData : [ChatData]?
    var groupMembers : [Members]?
    var venueTitle : String?
    var grpImg : ImageUrl?
    var venueLocationName : String?
    var venueLoc: [Any]?
    var notification: Bool?
    var venueLocationAddress: String?
    var groupName : String?
    var adminId : String?
    var groupId : String?
    var desc : String?
    
    required init?(map: Map){}
    init() {
        
    }
    
    
    func mapping(map: Map)  {
        chatData <- map["chatData"]
        groupMembers <- map["groupData"]
        venueTitle <- map["venueTitle"]
        grpImg <- map["imageUrl"]
        venueLocationName <- map["venueLocationName"]
        venueLoc <- map["venueLocation"]
        notification <- map["notification"]
        venueLocationAddress <- map["venueLocationAddress"]
        groupName <- map["groupName"]
        adminId <- map["adminId"]
        groupId <- map["groupId"]
        desc <- map["description"]
    }
}


class Members : Mappable {
    
    var isAdmin : Bool?
    var user : User?
    
    init() {
        
    }
    
    required init?(map: Map) {
    }
    
    func mapping(map: Map)  {
        isAdmin <- map["isAdmin"]
        user <- map["userId"]
        
    }
}

class ChatData : Mappable {
    
    var id : String?
    var mesgDetail : Message?
    var isOwnMessage: Bool? = false
    var senderId : User?
    var createdDate: Double?
    var date: String?
    var msgDate : String?
    var conversationId: String?
    var groupId : String?
    
    init() {
        
    }
    
    required init?(map: Map){}
    
    init(msgObj : Message) {
        self.senderId = Singleton.sharedInstance.loggedInUser
        self.mesgDetail = msgObj
        createdDate = Double(Date().millisecondsSince1970)
        date = Date().toString(format: "EEEE . MMM d yyyy")
        isOwnMessage = true
        msgDate = Date().toString(format: "h:mm a")
    }
    
    
    func mapping(map: Map)  {
        groupId  <- map["groupId"]
          conversationId  <- map["conversationId"]
        id <- map["_id"]
        mesgDetail <- map["chatDetails"]
        senderId <- map["senderId"]
        createdDate <- map["createdDate"]
        isOwnMessage = (/senderId?.id == Singleton.sharedInstance.loggedInUser?.id)
        let dateD = Date(milliseconds: /createdDate)
        date = dateD.toString(format: "EEEE . MMM d yyyy")
        msgDate = dateD.toString(format: "h:mm a")
    }
}


class Message : Mappable {
    
    var type : String?
    var typeOfMsg: MsgType?
    var message : String?
    var groupType : String?
    var senderId : String?
    var groupId : String?
    var createdDate : String?
    var messageID: String?
    var imageM: MessageImage?
    var video: MessageVideo?
    var audioMsg  : MessageImage?
    var audioDuration : Int?
    var isUploaded = true
    var isFail = false
    var receiverId : String?
    
    
    init() {
        
    }
    required init?(map: Map){}
    
    init(msg : String? , msgtype : String? , grpType : String? , grpId : String? ,  image: MessageImage? , video: MessageVideo? , messageID: String? = "", audioDuration: Int? = 0) {
        self.message = msg
        self.type = msgtype
        self.groupType = grpType
        self.groupId = grpId
        self.messageID = messageID
        self.senderId = Singleton.sharedInstance.loggedInUser?.id
        self.imageM = image
        self.video = video
        self.typeOfMsg = MsgType(rawValue: /msgtype)
        self.isUploaded = false
        self.isFail = false
        self.audioDuration = audioDuration
        self.createdDate = /Date().millisecondsSince1970
    }
    
    init(msg : String? , msgtype : String? , receiverId : String? , image: MessageImage? , video: MessageVideo? ,audioUrl : MessageImage? = nil , messageID: String? = "", audioDuration: Int? = 0) {
        self.message = msg
        self.type = msgtype
        self.messageID = messageID
        self.senderId = Singleton.sharedInstance.loggedInUser?.id
        self.imageM = image
        self.video = video
        self.typeOfMsg = MsgType(rawValue: /msgtype)
        self.isUploaded = false
        self.isFail = false
        self.receiverId = receiverId
        self.audioMsg = audioUrl
        self.createdDate = /Date().millisecondsSince1970
        self.audioDuration = audioDuration
    }
    
    func mapping(map: Map)  {
        type <- map["type"]
        message <- map["message"]
        groupType <- map["groupType"]
        senderId <- map["senderId"]
        groupId <- map["groupId"]
        createdDate <- map["createdDate"]
        imageM <- map["imageUrl"]
        audioMsg <- map["audioUrl"]
        video <- map["videoUrl"]
        typeOfMsg = MsgType(rawValue: /type)
        audioDuration <- map["audioDuration"]
    }
}

class MessageImage  : Mappable {
    
    var thumbnail : Any?
    var original : String?
    var audioUrl : Any?
    
    required init?(map: Map){}
    
    
    func mapping(map: Map){
        thumbnail <- map["thumbnail"]
        original <- map["original"]
        audioUrl <- map["original"]
    }
    
    init(image: Any?) {
        self.thumbnail = image
    }
    init(audioUrl : Any?) {
        self.audioUrl = audioUrl
    }
}

class MessageVideo  : Mappable {
    
    var data: Data?
    var thumbnail: Any?
    var original : String?
    var url: String?
    var imgUrl: String?
    
    required init?(map: Map){}
    
    
    func mapping(map: Map){
        thumbnail <- map["thumbnail"]
        original <- map["original"]
        
    }
    
    init(url: String?, data: Data?, thumb: Any?) {
        self.url = url
        self.data = data
        self.thumbnail = thumb
    }
}

class SectionalMessageArray {
    
    var date: String?
    var messages: [Message]?
    
}


class ChatListModel : Mappable {
    
    var createdDate : Double?
    var _id : String?
    var unreadCount : Int?
    var lastChatDetails : Message?
    var conversationId : String?
    var senderId : User?
    var isGroupChat : Bool = false
    var groupId : UserList?
    
    required init?(map: Map){}
    
    
    func mapping(map: Map){
        createdDate <- map["createdDate"]
        _id  <- map["_id"]
        unreadCount <- map["unreadCount"]
        lastChatDetails <- map["lastChatDetails"]
        conversationId <- map["conversationId"]
        senderId <- map["senderId"]
        self.isGroupChat = ( senderId == nil)
        if senderId == nil{
            senderId <- map["groupId"]
        }
        
    }
}



class DeleteChat : NSObject, NSCoding, Mappable{
    
    var groupId : String?
    var messageId : String?
    var type : String?
    var receiverId : String?
    var senderId : String?
    
    class func newInstance(map: Map) -> Mappable?{
        return DeleteChat()
    }
    required init?(map: Map){}
    private override init(){}
    
    func mapping(map: Map)
    {
        groupId <- map["groupId"]
        messageId <- map["messageId"]
        type <- map["type"]
        receiverId <- map["receiverId"]
        senderId <- map["senderId"]
    }
    
    /**
     * NSCoding required initializer.
     * Fills the data from the passed decoder
     */
    @objc required init(coder aDecoder: NSCoder)
    {
        groupId = aDecoder.decodeObject(forKey: "groupId") as? String
        messageId = aDecoder.decodeObject(forKey: "messageId") as? String
        type = aDecoder.decodeObject(forKey: "type") as? String
        receiverId = aDecoder.decodeObject(forKey: "receiverId") as? String
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if groupId != nil{
            aCoder.encode(groupId, forKey: "groupId")
        }
        if messageId != nil{
            aCoder.encode(messageId, forKey: "messageId")
        }
        if type != nil{
            aCoder.encode(type, forKey: "type")
        }
        if receiverId != nil{
            aCoder.encode(receiverId, forKey: "receiverId")
        }
        
    }
    
}

class MakeCallModel : Mappable{
    
    var token:String?
    var session_id:String?
    
    init(token: String?, session_id: String?) {
        self.token = token
        self.session_id = session_id
    }
    
    required init?(map: Map){ }
    
    func mapping(map: Map) {
        token <- map["token"]
        session_id <- map["session"]
        
        token <- map["data.token"]
        session_id <- map["data.session"]
    }
   
}
