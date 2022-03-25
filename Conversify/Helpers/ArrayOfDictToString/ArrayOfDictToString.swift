//
//  ArrayOfDictToString.swift
//  Kabootz
//
//  Created by Sierra 4 on 05/06/17.
//  Copyright © 2017 Sierra 4. All rights reserved.
//

import Foundation


class ArrayOfDictToString {
    
    class func jSONOfArrayOfDict(_ value:Array<Any>) -> String
    {
        do
        {
            let jsonData: Data = try JSONSerialization.data(withJSONObject: value, options: .prettyPrinted)
            return String(data: jsonData as Data, encoding: String.Encoding(rawValue: String.Encoding.utf8.rawValue))! as String
        }
        catch
        {
            print(error.localizedDescription)
        }
        return ""
    }
    
    
}

