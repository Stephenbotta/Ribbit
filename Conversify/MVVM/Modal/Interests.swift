//
//  Interests.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 13/10/18.
//

import UIKit
import ObjectMapper
import RMMapper


class Interests : NSObject, NSCoding, Mappable , RMMapping {
    
    var id: String?
    var category: String?
    var img:ImageUrl?
    var isSelected: Bool?
    var imageStr: String?
    
    
    required init?(map: Map){}
    override init(){}
    
    func mapping(map: Map)
    {
        id <- map["_id"]
        category <- map["categoryName"]
        img <- map["imageUrl"]
        isSelected = false
        imageStr = img?.original
    }
    
    @objc required init(coder aDecoder: NSCoder){
        id = aDecoder.decodeObject(forKey: "_id") as? String
        category = aDecoder.decodeObject(forKey: "categoryName") as? String
        img = aDecoder.decodeObject(forKey: "imageUrl") as? ImageUrl
        imageStr = aDecoder.decodeObject(forKey: "Catimage") as? String
    }
    
    @objc func encode(with aCoder: NSCoder)
    {
        if imageStr != nil{
            aCoder.encode(imageStr, forKey: "Catimage")
        }
        
        if id != nil{
            aCoder.encode(id, forKey: "_id")
        }
        
        if category != nil{
            aCoder.encode(category, forKey: "categoryName")
        }
        if img != nil{
            aCoder.encode(img, forKey: "imageUrl")
        }
        
    }
    
}


class ImageUrl : NSObject, NSCoding , Mappable , RMMapping {

    var thumbnail: String?
    var original: String?
    var image: UIImage?
    
    required init?(map: Map){}
    override init(){}
    
    func mapping(map: Map)
    {
        thumbnail <- map["thumbnail"]
        original <- map["original"]
        
    }
    
    @objc required init(coder aDecoder: NSCoder)
    {
        thumbnail = aDecoder.decodeObject(forKey: "profilethumbnail") as? String
        original = aDecoder.decodeObject(forKey: "profileoriginal") as? String
       
    }
    
    /**
     * NSCoding required method.
     * Encodes mode properties into the decoder
     */
    @objc func encode(with aCoder: NSCoder)
    {
        if thumbnail != nil{
            aCoder.encode(thumbnail, forKey: "profilethumbnail")
        }
        if original != nil{
            aCoder.encode(original, forKey: "profileoriginal")
        }
    }
    
}
