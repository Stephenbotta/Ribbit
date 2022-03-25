//
//  SocketIOManager.swift
//  Conversify
//
//  Created by Apple on 03/11/18.
//

import Foundation
import ObjectMapper
import SocketIO
import SwiftyJSON

typealias GetMessage = (_ message: Message?) -> ()

enum SocketEvent : String {
    
    case sendMsg = "sendMessage"
    case isTyping = "isTyping"
    case readMessage = "readMessage"
    case receiveMsg = "receiveMessage"
    case currentLocation = "currentLocation"
    case requestCount = "requestCount"
    case messageDelete = "messageDelete"
    case viewStory = "viewStory"
}
enum MsgType : String {
    case txt = "TEXT"
    case img = "IMAGE"
    case video = "VIDEO"
    case gif = "GIF"
    case audio = "AUDIO"
    
    var height: CGFloat {
        switch self {
        case .txt:
            return UITableView.automaticDimension
        case .img, .video , .gif:
            return (UIScreen.main.bounds.width) * 0.5
        case .audio :
             return UITableView.automaticDimension
        }
    }
}
enum chatGroupType : String {
    
    case venue = "VENUE"
    case group = "GROUP"
    case individual = "INDIVIDUAL"
}


class SocketIOManager: NSObject {
    
    static let shared = SocketIOManager()
    
    var socketManager: SocketManager?
    var socketClient: SocketIOClient?
    var isAuthenticated = false
    
    var msg: GetMessage?
    
    override init() {
        super.init()
        let socketUrl  = APIConstants.base
        socketManager = SocketManager.init(socketURL: URL.init(string: socketUrl)!, config: [.connectParams(["accessToken" : /Singleton.sharedInstance.loggedInUser?.token]), .forceNew(true)])
        socketClient = socketManager?.defaultSocket
    }
    
    func addHandlers() {
        if socketClient?.status == .connected {
            return
        }
        
        socketClient?.on(clientEvent: .connect, callback: { (data, ack) in
            
            print("Socket Connected" + /self.socketClient?.sid)
        })
        socketClient?.on(clientEvent: .error, callback: { (data, ack) in
            print("Error connecting")
            
            SocketIOManager.shared.connect()
        })
        socketClient?.on(clientEvent: .disconnect, callback: { (data, ack) in
            print("Socket Disconnected")
            self.isAuthenticated = false
        })
        socketClient?.connect()
    }
    
    func messageFromServer(_ completionHandler: @escaping (_ messageInfo: ChatData?) -> Void) {
        socketClient?.on(SocketEvent.receiveMsg.rawValue) {(dataArray, socketAck) in
            print(dataArray)
            let msg = Mapper<ChatData>().map(JSONObject: dataArray.first)
            completionHandler(msg)
        }
    }
    
    func messageFromServerDeleteChat(_ completionHandler: @escaping (_ messageInfo: DeleteChat?) -> Void) {
        socketClient?.on(SocketEvent.messageDelete.rawValue) {(dataArray, socketAck) in
            print(dataArray)
            let msg = Mapper<DeleteChat>().map(JSONObject: dataArray.first)
            completionHandler(msg)
        }
    }
    
    
    func requestCount(_ completionHandler: @escaping (_ count: RequestCount?) -> Void) {
        
        socketClient?.on(SocketEvent.requestCount.rawValue) {(dataArray, socketAck) in
            print(dataArray)
            let msg = Mapper<RequestCount>().map(JSONObject: dataArray.first)
            completionHandler(msg)
        }
    }
    
    func checkSocketConnection(){
        socketClient?.on("socketConnected", callback: { (dataArray, socketAck) in
            print(dataArray)
        })
    }
    
    func deleteMessage(indexPath: IndexPath ,data: [String : Any], ack : @escaping  (Bool , String) -> ()){
        print(data)
        socketClient?.emitWithAck(SocketEvent.messageDelete.rawValue, data ).timingOut(after: 1) { responseData in
            print(responseData)
            if /(responseData.first as? String) == "NO ACK"{
                ack(false , "")
                //                UtilityFunctions.makeToast(text: "Not able to delete message", type: .error)
            }else{
                ack(true , "")
            }
            debugPrint("======= Callback called with data: =======\(responseData)")
            
        }
    }
    
    func sendMessage(messageId:  String? , data: [String : Any], ack : @escaping  (Bool , ChatData) -> ()) {
        print(data)
        socketClient?.emitWithAck(SocketEvent.sendMsg.rawValue, data ).timingOut(after: 1) { responseData in
            print(responseData)
            guard let msg = Mapper<ChatData>().map(JSONObject: responseData.first) else { return }
            msg.mesgDetail?.messageID = messageId
            if /(responseData.first as? String) == "NO ACK"{
                //                UtilityFunctions.makeToast(text: "Not able to send message", type: .error)
                ack(false, ChatData())
            }else{
                ack(true , msg)
            }
            debugPrint("======= Callback called with data: =======\(responseData)")
            
        }
    }
    
    
    func viewStory(data : [String : Any] ,ack : @escaping  (Bool) -> ()){
        
        socketClient?.emitWithAck(SocketEvent.viewStory.rawValue , data).timingOut(after: 1){
            responseData in
            if(responseData.first as? String == "NO ACK"){
                ack(false)
            }else{
                ack(true)
            }
            print(responseData)
        }
    }
    func sendCurrentLocation(data: [String : Any],ack : @escaping  (Bool) -> ()) {
        //        print(data)
        socketClient?.emitWithAck(SocketEvent.currentLocation.rawValue, data ).timingOut(after: 1) { responseData in
            //            print(responseData)
            //            debugPrint("======= Callback called with data: =======\(responseData)")
            
        }
    }
    
    func disConnectMsgEvent(){
        socketClient?.off(SocketEvent.receiveMsg.rawValue)
    }
    
    func connect() {
        print("trying to connect")
        guard let socket = socketClient else { return }
        
        if socket.status == .connected {
            self.isAuthenticated = true
            return
        }
        
        if (socket.status == .disconnected || socket.status == .notConnected ) {
            print("=====Socket connected again=======")
            socket.connect()
            
            
        }
    }
    
    func disconnect() {
        guard let socket = socketClient else { return }
        if socket.status == .disconnected {
            return
        } else {
            socket.off(SocketClientEvent.disconnect.rawValue)
            socket.off(SocketClientEvent.connect.rawValue)
//            socket.off(SocketClientEvent.error.rawValue)
//            socket.off(EventName.sendMessage.rawValue)
//            socket.off(EventName.typing.rawValue)
//            socket.off(EventName.online.rawValue)
//            socket.off(EventName.messageRead.rawValue)
//            socket.disconnect()
//            if(self.socket != nil){
//
//            self.socket = nil
//            }
//            if(manager != nil){
//
//            manager.disconnect()
//            manager = nil
//            }
//
//            isListening = false
            print("disconnected")
            socket.disconnect()
        }
    }
}
