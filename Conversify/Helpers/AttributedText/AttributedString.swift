//
//  AttributedString.swift
//  Diet_Diary
//
//  Created by Sierra 4 on 02/03/17.
//  Copyright © 2017 Codebrew. All rights reserved.
//

import Foundation
import UIKit

class Attributed {
    
    static let shared = Attributed()
    
    func text(value: String, font: UIFont, alignment: NSTextAlignment) -> NSMutableAttributedString {
        
        let attrString = NSMutableAttributedString(string: value,
                                                   attributes: [NSAttributedString.Key.font: font])
        let style = NSMutableParagraphStyle()
        //style.lineSpacing = 8
        style.alignment = alignment
        attrString.addAttribute(NSAttributedString.Key.paragraphStyle,
                                value: style,
                                range: NSRange(location: 0, length: attrString.length))
        return attrString
        
    }
    
    func text(value: String, font: UIFont, color: UIColor) -> NSMutableAttributedString {
        
        let string = NSMutableAttributedString(string: value, attributes: [NSAttributedString.Key.font: font as Any])
        string.addAttribute(NSAttributedString.Key.foregroundColor, value: color, range: NSRange(location: 0, length: (value.count)))
        return string
        
    }

}
