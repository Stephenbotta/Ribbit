//
//  GroupDataModal.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import Foundation
import ObjectMapper

class GroupList : Mappable {
    
    var id : String?
    var imageUrl : ImageUrl?
    var groupName : String?
    
    required init?(map: Map){}
    
    init(idV: String , image: ImageUrl , name: String){
        id = idV
        imageUrl = image
        groupName = name
    }
    
    func mapping(map: Map)  {
        id <- map["_id"]
        imageUrl <- map["imageUrl"]
        groupName <- map["groupName"]
    }
}
