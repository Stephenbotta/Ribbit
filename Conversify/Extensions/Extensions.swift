//
//  Extensions.swift
//  Connect
//
//  Created by OSX on 19/12/17.
//  Copyright © 2017 OSX. All rights reserved.
//


import Material
import Foundation
import EZSwiftExtensions
import UIKit
import AVFoundation
import MobileCoreServices
import Photos
import moa



extension Date {
    
    var fixError:Date {
        return self.dateBySubtractingMinutes(330)
    }
    
}


//extension Button {
//
//    func set(title: String) {
//        self.setTitle(title, for: .normal)
//    }
//
//}

extension UIView {
    
    func animateBubble() {
        self.transform = CGAffineTransform(scaleX: 0.1, y: 0.1)
        
        UIView.animate(withDuration: 1.0,
                       delay: 0,
                       usingSpringWithDamping: 0.2,
                       initialSpringVelocity: 5.0,
                       options: .allowUserInteraction,
                       animations: {
                        self.transform = .identity
        },
                       completion: nil)
    }
    
    func removeView(_ complete: ((Bool)->())? = nil) {
        self.animate(duration: 0.4, animations: {
            self.alpha = 0.0
        }) { (bool) in
            complete?(bool)
            self.removeFromSuperview()
        }
    }
    
    func hide() {
        ez.runThisInMainThread {
            self.isHidden = self.isHidden.toggle()
        }
    }
    
    func set(constraint: NSLayoutConstraint.Attribute, value: CGFloat) {
        
        if let attribute = self.constraints.filter({ $0.firstAttribute == constraint }).first {
            attribute.constant = value
        }
    }
}


extension String {
    
    func verifyUrl() -> Bool {
        if self.contains("@"){
            return false
        }
        let detector = try! NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue)
        if let match = detector.firstMatch(in: self, options: [], range: NSRange(location: 0, length: self.endIndex.encodedOffset)) {
            // it is a link, if the match covers the whole string
            return match.range.length == self.endIndex.encodedOffset
        } else {
            return false
        }
        
        
    }
    
    func hashtagss() -> [String]{
        if let regex = try? NSRegularExpression(pattern: "#[a-z0-9]+", options: .caseInsensitive)
        {
            let string = self as NSString
            
            return regex.matches(in: self, options: [], range: NSRange(location: 0, length: string.length)).map {
                string.substring(with: $0.range).replacingOccurrences(of: "#", with: "").lowercased()
            }
        }
        
        return []
    }
    
    func hasUserName() -> [String]{
        if let regex = try? NSRegularExpression(pattern: "@[a-z0-9]+", options: .caseInsensitive)
        {
            let string = self as NSString
            
            return regex.matches(in: self, options: [], range: NSRange(location: 0, length: string.length)).map {
                string.substring(with: $0.range).replacingOccurrences(of: "@", with: "").lowercased()
            }
        }
        
        return []
    }
    
    
    var digits: String {
        return components(separatedBy: CharacterSet.decimalDigits.inverted)
            .joined()
    }
    
    var millisecondToTime: String {
        
        let milliSeconds = Int64((self))
        
        guard let _ = milliSeconds else {
            return self
        }
        let time = Date(timeIntervalSince1970: TimeInterval(milliSeconds!/1000))
        return (/String(describing: time.toString(format: "hh:mm aa")))
    }
    
    var first: String {
        return String(characters.prefix(1))
    }
    var last: String {
        return String(characters.suffix(1))
    }
    var uppercaseFirst: String {
        return first.uppercased() + String(characters.dropFirst())
    }
    
    func height(withConstrainedWidth width: CGFloat, font: UIFont) -> CGFloat {
        let constraintRect = CGSize(width: width, height: .greatestFiniteMagnitude)
        let boundingBox = self.boundingRect(with: constraintRect, options: .usesLineFragmentOrigin, attributes: [NSAttributedString.Key.font: font], context: nil)
        return ceil(boundingBox.height)
    }
}


extension Array where Element : Any{
    
    func toJson() -> String {
        do {
            let data = self
            
            let jsonData = try JSONSerialization.data(withJSONObject: data, options: JSONSerialization.WritingOptions.prettyPrinted)
            var string = NSString(data: jsonData, encoding: String.Encoding.utf8.rawValue) ?? ""
            string = string.replacingOccurrences(of: "\n", with: "") as NSString
            debugPrint(string)
            string = string.replacingOccurrences(of: "\\", with: "") as NSString
            debugPrint(string)
            string = string.replacingOccurrences(of: " ", with: "") as NSString
            debugPrint(string)
            return string as String
        }
        catch let error as NSError{
            debugPrint(error.description)
            return ""
        }
    }
}







extension UILabel {
    /** Add tap Gesture for substring on UILabel **/
    func tapActionFor(subString: String, _ action: @escaping (()->Void)) {
        self.addTapGesture { (gesture) in
            guard let range = self.text?.range(of: subString)?.nsRange else { return }
            
            if gesture.didTapAttributedTextInLabel(label: self, inRange: range) {
                debugPrint("===================== tapped \(subString)")
                action()
            }
        }
    }
    
}


extension UITapGestureRecognizer {
    
    func didTapAttributedTextInLabel(label: UILabel, inRange targetRange: NSRange) -> Bool {
        // Create instances of NSLayoutManager, NSTextContainer and NSTextStorage
        let layoutManager = NSLayoutManager()
        let textContainer = NSTextContainer(size: CGSize.zero)
        let textStorage = NSTextStorage(attributedString: label.attributedText!)
        
        // Configure layoutManager and textStorage
        layoutManager.addTextContainer(textContainer)
        textStorage.addLayoutManager(layoutManager)
        
        // Configure textContainer
        textContainer.lineFragmentPadding = 0.0
        textContainer.lineBreakMode = label.lineBreakMode
        textContainer.maximumNumberOfLines = label.numberOfLines
        let labelSize = label.bounds.size
        textContainer.size = labelSize
        
        // Find the tapped character location and compare it to the specified range
        let locationOfTouchInLabel = self.location(in: label)
        let textBoundingBox = layoutManager.usedRect(for: textContainer)
        
        let textContainerOffset = CGPoint(x: (labelSize.width - textBoundingBox.size.width) * 0.5 - textBoundingBox.origin.x, y: (labelSize.height - textBoundingBox.size.height) * 0.5 - textBoundingBox.origin.y)
        
        let locationOfTouchInTextContainer = CGPoint(x: locationOfTouchInLabel.x - textContainerOffset.x, y: locationOfTouchInLabel.y - textContainerOffset.y)
        let indexOfCharacter = layoutManager.characterIndex(for: locationOfTouchInTextContainer, in: textContainer, fractionOfDistanceBetweenInsertionPoints: nil)
        return NSLocationInRange(indexOfCharacter, targetRange)
    }
    
}

extension Range where Bound == String.Index {
    var nsRange:NSRange {
        return NSRange(location: self.lowerBound.encodedOffset,
                       length: self.upperBound.encodedOffset -
                        self.lowerBound.encodedOffset)
    }
}


extension NSMutableAttributedString {
    
    @discardableResult func specifyAttributes(_ text: String , font: UIFont?) -> NSMutableAttributedString {
        var attributes = [NSAttributedString.Key: Any]()
        attributes[NSAttributedString.Key.foregroundColor] = (text == "Sign in") ? UIColor.init(hexString: "#FFDF18") : UIColor.white
        attributes[NSAttributedString.Key.font] =  font ?? UIFont.systemFont(ofSize: 12)
        let boldString = NSMutableAttributedString(string:text, attributes: attributes)
        append(boldString)
        return self
    }
    
    @discardableResult func setAttributes(_ text: String , attributes: [NSAttributedString.Key: Any]) -> NSMutableAttributedString {
        let boldString = NSMutableAttributedString(string:text, attributes: attributes)
        append(boldString)
        return self
    }
}







//TextField Character limit
var __maxLengths = [UITextField: Int]()

extension UITextField {
    @IBInspectable var maxLength: Int {
        get {
            guard let l = __maxLengths[self] else {
                return 150
            }
            return l
        }
        set {
            __maxLengths[self] = newValue
            addTarget(self, action: #selector(fix), for: .editingChanged)
        }
    }
    
    @objc func fix(textField: UITextField) {
        let t = textField.text
        textField.text = t?.safelyLimitedTo(length: maxLength)
    }
}

//MARK:- String
extension String {
    
    func safelyLimitedTo(length n: Int)->String {
        let c = self
        if (c.count <= n) { return self }
        return String( Array(c).prefix(upTo: n) )
    }
}



extension Date {
    
    var millisecondsSince1970: String {
        
        return String((self.timeIntervalSince1970 * 1000.0).rounded())
    }
    
    
    
    
    init(milliseconds:Double) {
        self = Date(timeIntervalSince1970: TimeInterval(milliseconds / 1000))
    }
    
    
    
}

public extension URL {
    
    func generateThumbnail() -> UIImage {
        let asset = AVAsset(url: self)
        let generator = AVAssetImageGenerator(asset: asset)
        generator.appliesPreferredTrackTransform = true
        var time = asset.duration
        time.value = 0
        let imageRef = try? generator.copyCGImage(at: time, actualTime: nil)
        let thumbnail = UIImage(cgImage: imageRef!)
        return thumbnail
    }
    
}

extension NSMutableAttributedString {
    
    @discardableResult func bold(_ text: String , size: CGFloat) -> NSMutableAttributedString {
        let attrs: [NSAttributedString.Key: Any] = [.font: UIFont.systemFont(ofSize: size)]
        let boldString = NSMutableAttributedString(string:text, attributes: attrs)
        append(boldString)
        
        return self
    }
    
    @discardableResult func normal(_ text: String) -> NSMutableAttributedString {
        let normal = NSAttributedString(string: text)
        append(normal)
        
        return self
    }
}


extension String {
    
    func hashtags() -> [String]
    {
        if let regex = try? NSRegularExpression(pattern: "#[a-z0-9]+", options: .caseInsensitive)
        {
            let string = self as NSString
            
            return regex.matches(in: self, options: [], range: NSRange(location: 0, length: string.length)).map {
                string.substring(with: $0.range).replacingOccurrences(of: "#", with: "").lowercased()
            }
        }
        
        return []
    }
    
    func usertags() -> [String]{
        if let regex = try? NSRegularExpression(pattern: "@[a-z0-9._]+", options: .caseInsensitive)
        {
            let string = self as NSString
            
            return regex.matches(in: self, options: [], range: NSRange(location: 0, length: string.length)).map {
                string.substring(with: $0.range).replacingOccurrences(of: "@", with: "")
            }
        }
        
        return []
    }
    
    //    func mimeTypeFromFileExtension() -> String? {
    //        guard let uti: CFString = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, self as NSString, nil)?.takeRetainedValue() else {
    //            return nil
    //        }
    //        
    //        guard let mimeType: CFString = UTTypeCopyPreferredTagWithClass(uti, kUTTagClassMIMEType)?.takeRetainedValue() else {
    //            return nil
    //        }
    //        
    //        return mimeType as String
    //    }
    
    //    func widthOfString(usingFont font: UIFont) -> CGFloat {
    //        let fontAttributes = [NSAttributedStringKey.font: font]
    //        let size = self.size(withAttributes: fontAttributes)
    //        return size.width
    //    }
    //    
    //    func heightOfString(usingFont font: UIFont) -> CGFloat {
    //        let fontAttributes = [NSAttributedStringKey.font: font]
    //        let size = self.size(withAttributes: fontAttributes)
    //        return size.height
    //    }
}


extension Sequence {
    
    func groupBy<G: Hashable>(closure: (Iterator.Element)->G) -> [G: [Iterator.Element]] {
        var results = [G: Array<Iterator.Element>]()
        
        forEach {
            let key = closure($0)
            
            if var array = results[key] {
                array.append($0)
                results[key] = array
            }
            else {
                results[key] = [$0]
            }
        }
        
        return results
    }
}


extension PHAsset {
    func getAssetThumbnail(asset: PHAsset) -> UIImage {
        let manager = PHImageManager.default()
        let option = PHImageRequestOptions()
        var thumbnail = UIImage()
        option.isSynchronous = true
        manager.requestImage(for: asset, targetSize: CGSize(width: 1000, height: 1000), contentMode: .aspectFit, options: option, resultHandler: {(result, info)->Void in
            thumbnail = result ?? UIImage()
        })
        return thumbnail
    }
}


extension UIImageView {
    
    func getAssetThumbnail(asset: PHAsset) -> UIImage {
        let manager = PHImageManager.default()
        let option = PHImageRequestOptions()
        var thumbnail = UIImage()
        option.isSynchronous = true
        manager.requestImage(for: asset, targetSize: CGSize(width: 1000, height: 1000), contentMode: .aspectFit, options: option, resultHandler: {(result, info)->Void in
            thumbnail = result ?? UIImage()
        })
        return thumbnail
    }
    
    //    func setImageWithText(image: Any?, font: UIFont, name: String) {
    //        if let labelView = self.subviews.filter({$0.isKind(of: UILabel.self)}).first as? UILabel {
    //            labelView.removeFromSuperview()
    //        }
    //        self.backgroundColor = Color.blue.withAlphaComponent(0.5)
    //        let label = UILabel()
    //        label.font = font
    //        label.frame = self.bounds
    //        label.textColor = UIColor.white
    //        label.textAlignment = .center
    //        self.addSubview(label)
    //        let nameWords = Array(name.components(separatedBy: " ").prefix(2))
    //        label.text = nameWords.initialsText
    //        
    //        if let image: UIImage = image as? UIImage {
    //            label.removeFromSuperview()
    //            self.image = image
    //        } else {
    //            selfimage(url:  URL.init(string: /(image as? String)), placeholder: nil, options: nil, progressBlock: nil) { (imageFetched, _, _, _) in
    //                if let _ = imageFetched {
    //                    label.removeFromSuperview()
    //                }
    //            }
    //            
    //        }
    //    }
    
    func setImage(image: Any? , placeholder : UIImage? = nil) {
        if let image: UIImage = image as? UIImage {
            self.image = image
        } else {
            //self.image(url:  URL.init(string: /(image as? String)), placeholder: placeholder, options: nil, progressBlock: nil, completionHandler: nil)
            if let _place = placeholder {
                self.image(url: /(image as? String),placeholder: _place)
            }else{
                self.image(url:  /(image as? String))
            }
           
        }
    }
}

extension UIApplication {
    var isKeyboardPresented: Bool {
        if let keyboardWindowClass = NSClassFromString("UIRemoteKeyboardWindow"), self.windows.contains(where: { $0.isKind(of: keyboardWindowClass) }) {
            return true
        } else {
            return false
        }
    }
}

extension UIButton {
    
    @IBInspectable
    open var exclusiveTouchEnabled : Bool {
        get {
            return self.isExclusiveTouch
        }
        set(value) {
            self.isExclusiveTouch = value
        }
    }
}

extension UITextView {
    
    func resolveTags(userName: String){
        let nsText:NSString = userName + "\n" + self.text as NSString
        let words:[String] = nsText.components(separatedBy: " ")
        
        let attrs = [
            NSAttributedString.Key.font : UIFont.systemFont(ofSize: 14, weight: .medium),
            NSAttributedString.Key.foregroundColor : #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
            ] as [NSAttributedString.Key : Any]
        
        let attrString  = NSMutableAttributedString(string: nsText as String, attributes:attrs)
        
        if nsText.contains(userName){
            let matchRange = nsText.range(of: userName)
            attrString.addAttribute(NSAttributedString.Key.link, value: "user://\(userName)", range: matchRange)
            attrString.addAttribute(NSAttributedString.Key.foregroundColor, value: #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1) , range: matchRange)
            attrString.addAttribute(NSAttributedString.Key.font, value: UIFont.boldSystemFont(ofSize: 14) , range: matchRange)
        }
        
        for word in words {
            if word.hasPrefix("#") {
                let matchRange:NSRange = nsText.range(of: word as String)
                var stringifiedWord:String = word as String
                stringifiedWord = String(stringifiedWord.dropFirst())
                attrString.addAttribute(NSAttributedString.Key.link, value: "hash://\(stringifiedWord)", range: matchRange)
                attrString.addAttribute(NSAttributedString.Key.foregroundColor, value: #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1) , range: matchRange)
            }
        }
        
        for word in words {
            if word.hasPrefix("@") {
                let matchRange:NSRange = nsText.range(of: word as String)
                var stringifiedWord:String = word as String
                stringifiedWord = String(stringifiedWord.dropFirst())
                attrString.addAttribute(NSAttributedString.Key.link, value: "mentioning://\(stringifiedWord)", range: matchRange)
                attrString.addAttribute(NSAttributedString.Key.foregroundColor, value: #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1) , range: matchRange)
            }
        }
        self.attributedText = attrString
    }
    
    func resolvePostTags(userName: String){
        let nsText:NSString = userName + "\n" + self.text as NSString
        let words:[String] = nsText.components(separatedBy: " ")
        
        let attrs = [
            NSAttributedString.Key.font : UIFont.systemFont(ofSize: 14, weight: .medium),
            NSAttributedString.Key.foregroundColor : #colorLiteral(red: 0, green: 0, blue: 0, alpha: 1)
            ] as [NSAttributedString.Key : Any]
        
        let attrString  = NSMutableAttributedString(string: nsText as String, attributes:attrs)
        
        if nsText.contains(userName){
            let matchRange = nsText.range(of: userName)
            attrString.addAttribute(NSAttributedString.Key.link, value: "user://\(userName)", range: matchRange)
            attrString.addAttribute(NSAttributedString.Key.foregroundColor, value: #colorLiteral(red: 0, green: 0, blue: 0, alpha: 1) , range: matchRange)
            attrString.addAttribute(NSAttributedString.Key.font, value: UIFont.boldSystemFont(ofSize: 14) , range: matchRange)
        }
        
        for word in words {
            if word.hasPrefix("#") {
                let matchRange:NSRange = nsText.range(of: word as String)
                var stringifiedWord:String = word as String
                stringifiedWord = String(stringifiedWord.dropFirst())
                attrString.addAttribute(NSAttributedString.Key.link, value: "hash://\(stringifiedWord)", range: matchRange)
                attrString.addAttribute(NSAttributedString.Key.foregroundColor, value: #colorLiteral(red: 0, green: 0, blue: 0, alpha: 1) , range: matchRange)
            }
        }
        
        for word in words {
            if word.hasPrefix("@") {
                let matchRange:NSRange = nsText.range(of: word as String)
                var stringifiedWord:String = word as String
                stringifiedWord = String(stringifiedWord.dropFirst())
                attrString.addAttribute(NSAttributedString.Key.link, value: "mentioning://\(stringifiedWord)", range: matchRange)
                attrString.addAttribute(NSAttributedString.Key.foregroundColor, value: #colorLiteral(red: 0, green: 0, blue: 0, alpha: 1) , range: matchRange)
            }
        }
        self.attributedText = attrString
    }
}

class NonEditableTextView: UITextView {
    override func canPerformAction(_ action: Selector, withSender sender: Any?) -> Bool {
        if action == #selector(UIResponderStandardEditActions.copy(_:)) ||
            action == #selector(UIResponderStandardEditActions.cut(_:)) ||
            action == #selector(UIResponderStandardEditActions.select(_:)) ||
            action == #selector(UIResponderStandardEditActions.selectAll(_:)) ||
            action == #selector(UIResponderStandardEditActions.delete(_:)) {
            
            return false
        }
        return super.canPerformAction(action, withSender: sender)
    }
}


