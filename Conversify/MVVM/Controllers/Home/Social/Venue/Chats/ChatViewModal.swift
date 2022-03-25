//
//  ChatViewModal.swift
//  Conversify
//
//  Created by Apple on 02/11/18.
//


import UIKit
import RxCocoa
import RxSwift
import Foundation
import RxDataSources

class ChatGroup {
    var date: String?
    var messages: [ChatData]?
    
    init(_ _date: String?, _ _messages: [ChatData]?) {
        date = _date
        messages = _messages
    }
}

class ChatViewModal: BaseRxViewModel {
    
    var chatData = Variable<[ChatData]>([])
    var groupIdToJoin = Variable<String?>(nil)
    var groupChatData = Variable<GroupConvoList?>(nil)
    var backRefresh:(()->())?
    var arrayOfChat = Variable<[ChatDataSec]>([])
    var chatImage = Variable<UIImage?>(nil)
    var chatImageUrl = Variable<String?>(nil)
    var videoUrl = Variable<String?>(nil)
    var venue = Variable<Venues?>(nil)
    var notificationOn = PublishSubject<Bool>()
    var exitSuccesfully = PublishSubject<Bool>()
    var venueName = Variable<String?>(nil)
    var groupName = Variable<String?>(nil)
    var updateNameSuccessfully = PublishSubject<Bool>()
    var updateVenueDetails:((Venues?)->())?
    var updateGroupDetails:((GroupConvoList?)->())?
    var conversationId = Variable<String?>(nil)
    var chatId = Variable<String?>(nil)
    var groupMembers = Variable<[User?]>([])
    var loadMore : Bool = true
    var exit:(()->())?
    var assignConvoId:((String)->())?
    var particpantAdded = PublishSubject<Bool>()
    
    override init() {
        super.init()
    }
    
    init(conversationId: String? , chatId: String? , groupId: String?){
        super.init()
        self.conversationId.value = conversationId
        self.chatId.value = chatId
        self.groupIdToJoin.value = groupId
    }
    
    init(membersData: GroupConvoList? , venueD:Venues? ) {
        super.init()
        self.groupChatData.value = membersData
        self.venue.value = venueD
        self.groupIdToJoin.value = venueD?.groupId
    }
    
    init(members : [User?]){
        
        self.groupMembers.value = members 
    }
    
    func getVenueConversation(isPush: Bool? , _ completion:@escaping (Bool)->()){
        VenueTarget.venueConversationDetails(groupId: groupIdToJoin.value,chatId : chatId.value).request(apiBarrier: (/chatId.value == "") ?true : false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<GroupConvoList> else{
                    return
                }
                if /self?.chatId.value == "" || /isPush{
                    self?.arrayOfChat.value.removeAll()
                }
                self?.loadMore = (safeResponse.data?.chatData?.count == 40)
                self?.groupChatData.value = safeResponse.data
                let chats = safeResponse.data?.chatData
                
                self?.chatData.value = (chats ?? [] ) + (self?.chatData.value ?? [])
                
                self?.chatData.value =  self?.chatData.value.sorted(by: { (h1, h2) -> Bool in
                                return /Date(fromString: /h1.date, format: "EEEE . MMM d") > /Date(fromString: /h2.date, format: "EEEE . MMM d")
                               }) ?? []
                
                let sectionedChat = self?.chatData.value.groupBy { $0.date }
                guard let val = (sectionedChat?.map({ (date, chat) -> ChatDataSec in
                    return ChatDataSec(header: /date , items: chat)
                })) else { return }
                
                self?.arrayOfChat.value = val.sorted(by: { (h1, h2) -> Bool in
                    return /Date(fromString: h1.header, format: "EEEE . MMM d") > /Date(fromString: h2.header, format: "EEEE . MMM d")
                })
                
               // self?.arrayOfChat.value = val
                
                completion(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    func getChatConversation(isPush: Bool? , _ completion:@escaping (Bool)->()){
        ChatTarget.chatConversation(conversationId: conversationId.value, chatId: chatId.value).request(apiBarrier: (/chatId.value == "") ? true : false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                print("Chat:- \(response)")
                guard let safeResponse = response as? DictionaryResponse<GroupConvoList> else{
                    return
                }
                if /self?.chatId.value == "" || /isPush{
                    self?.arrayOfChat.value.removeAll()
                    self?.chatData.value.removeAll()
                }
                self?.loadMore = (safeResponse.data?.chatData?.count == 40)
                self?.groupChatData.value = safeResponse.data
                self?.groupMembers.value = self?.groupChatData.value?.groupMembers?.map({$0.user}) ?? []
                let chats = safeResponse.data?.chatData
                self?.chatData.value = (chats ?? [] ) + (self?.chatData.value ?? [])
                
                let sectionedChat = self?.chatData.value.groupBy { $0.date }
                
           
                guard let val = (sectionedChat?.map({ (date, chat) -> ChatDataSec in
                    return ChatDataSec(header: /date , items: chat)
                })) else { return }
                
                
                let sortedArray = val.sorted(by: { (h1, h2) -> Bool  in
                    return /Date(fromString: h1.header, format: "EEEE . MMM d yyyy") < /Date(fromString: h2.header, format: "EEEE . MMM d yyyy")
                })
                
                
                 self?.arrayOfChat.value = sortedArray
//                val.sorted(by: { (h1, h2) -> Bool in
//                    print(Date(fromString: h1.header, format: "EEEE . MMM d"))
//                    return /Date(fromString: h1.header, format: "EEEE . MMM d") > /Date(fromString: h2.header, format: "EEEE . MMM d")
//                })
//                //
//                self?.arrayOfChat.value = val
                
                completion(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    
    func uploadImage(){
        Loader.shared.start()
        S3.upload(image: chatImage.value , success: { (imageName) in
            print(imageName)
            self.chatImageUrl.value = imageName
        }) { (error) in
            print(error)
        }
    }
    
    func uploadVideo(){
        
    }
    
    //MARK::- GROUP DETAILS
    
    //For Editing Chat Group Name
    func addEditPostGroupName(groupID: String? = ""){
        ChatTarget.addEditPostGroupName(groupName: groupName.value, postGroupId: /groupChatData.value?.groupId == "" ? groupID : groupChatData.value?.groupId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.groupChatData.value?.groupName = self?.groupName.value
                self?.updateNameSuccessfully.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    
    //For Editing Venue Name
    func addEditGroupName(){
        ChatTarget.editGroupName(groupName: venueName.value, postGroupId: venue.value?.groupId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.venue.value?.venueTitle = /self?.venueName.value
                self?.updateNameSuccessfully.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    //Exit Group
    func exitPostGroup(){
        ChatTarget.exitGroup(groupId: groupChatData.value?.groupId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.exitSuccesfully.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    func archivePostGroup(){
        PeopleTarget.archiveGroup(groupId: groupChatData.value?.groupId , groupType: "GROUP").request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.exitSuccesfully.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    //Exit Venue
    func exitGroup(){
        
        ChatTarget.exitVenueGroup(venueId: venue.value?.groupId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.exitSuccesfully.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    func archiveVenue(){
        
        PeopleTarget.archiveGroup(groupId: venue.value?.groupId, groupType: "VENUE").request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.exitSuccesfully.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    
    func notificationSettings(action: String?){
        ChatTarget.enableNotificationForVenue(venueId: venue.value?.groupId, action: /action).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.notificationOn.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    func notificationGroupChat(action: String?){
        ChatTarget.enableNotificationForGroupChat(groupId: groupChatData.value?.groupId, action: /action).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.notificationOn.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    
    func addMoreParticipants(particpants: String, groupId: String , venueId: String){
        PeopleTarget.addParticpants(participants: particpants, groupId: groupId, venueId: venueId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                UtilityFunctions.makeToast(text: "Request has been sent to the participants. They will get added once they accept", type: .info)
                self?.particpantAdded.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    func inviteContacts(phone:String , groupId: String? , email: String? , isGroup: Bool){
        VenueTarget.inviteusers(phone: phone, emails: email, groupId: groupId , isGroup: isGroup).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                UtilityFunctions.makeToast(text: "Invitation has been sent successfully", type: .info)
                self?.particpantAdded.onNext(true)
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
            })<bag
    }
    
    
    
}

struct ChatDataSec {
    var header: String
    var createdDate : Double = 0.0
    var items: [Item]
}

extension ChatDataSec: SectionModelType {
    
    typealias Item = Any
    
    init(original: ChatDataSec, items: [Item]) {
        self = original
        self.items = items
    }
}
