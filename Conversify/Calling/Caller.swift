//
//  Caller.swift
//  Buraq24
//
//  Created by Paradox on 13/08/19.
//  Copyright © 2019 CodeBrewLabs. All rights reserved.
//

import Foundation

class Caller {
    
    var name: String?
    var sessionId: String?
    var token: String?
    var udid: String?
    var outGoing: Bool = true
    var callUUID : String?
    
    var userId: String?
    var img : UIImage?
    
    init() {}
    
    init(name: String, userId: String , userImg : UIImage?) {
        self.name = name
        self.userId = userId
        self.img = userImg
    }
    
    init(name: String, token: String, sessionId: String, callUUID: String? = nil , userId : String?) {
        self.name = name
        self.token = token
        self.sessionId = sessionId
        self.callUUID = callUUID
        self.userId = userId
    }
}
//class Caller {
//
//    var name: String?
//    var sessionId: String?
//    var token: String?
//    var udid: String?
//    var outGoing: Bool = true
//    var callUUID : String?
//
//    var userId: String?
//
//    init() {}
//
//    init(name: String, userId: String) {
//        self.name = name
//        self.userId = userId
//    }
//
//    init(name: String, token: String, sessionId: String, callUUID: String? = nil) {
//        self.name = name
//        self.token = token
//        self.sessionId = sessionId
//        self.callUUID = callUUID
//
//    }
//}
