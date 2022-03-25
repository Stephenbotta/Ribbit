//
//  WheelData.swift
//  Conversify
//
//  Created by admin on 07/04/21.
//

import Foundation
import Moya
import ObjectMapper
class WheelDetail : Mappable{
    
        var message : String?
        var data : WheelData?
    init() {
    }
    required init?(map: Map){}
        func mapping(map: Map) {
            message <- map["message"]
            data <- map["data"]
       
        }
}
class WheelData : Mappable{
    var id : String?
    var value : Int?
    var prize : [Price]?
    init(){
    }
    required init?(map: Map) {
    }
    
    func mapping(map: Map) {
        id <- map["_id"]
        prize <- map["prize"]
    }
}

class Price : Mappable{
    var id : String?
    var value : Int?
    var color : String?
    init(){
        
    }
    required init?(map: Map) {
    }
    
    func mapping(map: Map) {
        id <- map["_id"]
        value <- map["value"]
        color <- map["color"]
    }
}

class SpinWheelPrize : Mappable{
    var message : String?
    var data : String?
    var statusCode : Int?
    init(){
        
    }
    required init?(map: Map) {
    }
    
    func mapping(map: Map) {
        message <- map["message"]
        data <- map["data"]
        statusCode <- map["statusCode"]
    }
}
