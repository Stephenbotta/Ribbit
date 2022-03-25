//
//  GiftCard.swift
//  Conversify
//
//  Created by Sagar Kumar on 08/02/21.
//

import Foundation
import ObjectMapper

class GiftCardModel : Mappable {
    
    var data: GiftCard?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        data <- map["data"] 
    }
}

class GiftCard: Mappable {
    
    var catalogName: String?
    var brands: [Brand]?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        catalogName <- map["catalogName"]
        brands <- map["brands"]
    }
}

class Brand: Mappable {
    
    var lastUpdateDate : String?
    var status : String?
    var brandName : String?
    var brandKey : String?
    var disclaimer : String?
    var description : String?
    var terms : String?
    var shortDescription : String?
    var imageUrls : [String: String]?
    var brandRequirements : [String: Any]?
    var items: [Item]?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        lastUpdateDate <- map["lastUpdateDate"]
        status <- map["status"]
        brandName <- map["brandName"]
        brandKey <- map["brandKey"]
        disclaimer <- map["disclaimer"]
        description <- map["description"]
        terms <- map["terms"]
        shortDescription <- map["shortDescription"]
        brandRequirements <- map["brandRequirements"]
        imageUrls <- map["imageUrls"]
        items <- map["items"]
    }
}

class Item: Mappable {
    
    var lastUpdateDate : String?
    var createdDate: String?
    var status : String?
    var currencyCode : String?
    var minValue : Double?
    var countries : [String]?
    var maxValue : Double?
    var valueType : String?
    var isWholeAmountValueRequired : Bool?
    var rewardType : String?
    var rewardName : String?
    var utid: String?
    var redemptionInstructions: String?

    required init?(map: Map){}
    
    func mapping(map: Map)  {
        lastUpdateDate <- map["lastUpdateDate"]
        createdDate <- map["createdDate"]
        status <- map["status"]
        currencyCode <- map["currencyCode"]
        minValue <- map["minValue"]
        countries <- map["countries"]
        maxValue <- map["maxValue"]
        valueType <- map["valueType"]
        isWholeAmountValueRequired <- map["isWholeAmountValueRequired"]
        rewardType <- map["rewardType"]
        rewardName <- map["rewardName"]
        utid <- map["utid"]
        redemptionInstructions <- map["redemptionInstructions"]

    }
}

class Redeem: Mappable {
    
    var point: Double?
    var _id : String?
    var redeemType : String?
    var name : String?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        point <- map["point"]
        _id <- map["_id"]
        redeemType <- map["redeemType"]
        name <- map["name"]
    }
}

class Organization: Mappable {
    
    var _id : String?
    var organizationName : String?
    var isSelected: Bool?
    
    required init?(map: Map){}
    
    func mapping(map: Map)  {
        _id <- map["_id"]
        organizationName <- map["organizationName"]
    }
}
