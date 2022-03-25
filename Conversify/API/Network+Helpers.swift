//
//  Network+Helpers.swift
//  WithinEarthAgent
//
//  Created by iReaper on 12/10/17.
//  Copyright © 2017 Within Earth. All rights reserved.
//

import Foundation
import RxCocoa
import RxSwift
import SwiftyJSON
import ObjectMapper
import Moya

public func url(_ route: TargetType) -> String {
    return route.baseURL.appendingPathComponent(route.path).absoluteString
}

class DictionaryResponse <T : Mappable> :  Mappable{
    
    var message: String?
    var data : T?
    var success: String?
    var array : [T]?
    
    required init?(map: Map){
    }
    func mapping(map: Map){
        message <- map["message"]
        data <- map["data"]
        success <- map["success"]
        array <- map["data"]
        if (array?.count ?? 0) == 0 {
            array <- map["results"]
        }
    }
}

extension Moya.Response {
    func mapNSArray() throws -> NSArray {
        let any = try self.mapJSON()
        guard let array = any as? NSArray else {
            throw MoyaError.jsonMapping(self)
        }
        return array
    }
}
