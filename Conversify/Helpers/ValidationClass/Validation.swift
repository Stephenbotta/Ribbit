//
//  ValidationClass.swift
//  MbKutz
//
//  Created by Aseem 13 on 15/12/16.
//  Copyright © 2016 Taran. All rights reserved.
//

import UIKit

enum Alert : String{
    case success = "Success"
    case oops = "Error "
    case login = "Login Successfull"
    case ok = "Ok"
    case cancel = "Cancel"
    case error = "Error"
    case newAppointment = "You have a new request for a service"
    case sorry = "Sorry"
    case emp = ""
    
}

enum Valid{
    case success
    case failure
}

class Validation: NSObject {
    
    static let shared = Validation()
    var errorMessage = ""
    
    func errorMsg(str : String) -> Valid{
        UtilityFunctions.makeToast(text: str, type: .error)
        return .failure
    }
    
    func isValidLogin(email: String? , password: String? ) -> Valid{
        if isValid(type: .userNameOrEmail, info: email) &&  isValid(type: .emailPassword, info: password){
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidPassword(oldPassword: String?  , newPassword: String? ) -> Valid{
        if isValid(type: .oldPassword, info: oldPassword) && isValid(type: .newPassword, info: newPassword) {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidProfile(name: String?  , userName: String?  ,  email: String? , phoneNum : String? ) -> Valid{
        if isValid(type: .firstName, info: name) && isValid(type: .userName, info: userName) &&  ( isValid(type: .email, info: email) &&  isValid(type: .mobile, info: phoneNum?.replacingOccurrences(of: " ", with: ""))){
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidEditProfile(name: String?  , userName: String?  ,  email: String? , phoneNum : String? ) -> Valid{
        if isValid(type: .firstName, info: name) && isValid(type: .userName, info: userName) &&  ( isValid(type: .email, info: email)){
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    private func isValid(type : FieldType , info: String?) -> Bool {
        guard let validStatus = info?.handleStatus(fieldType : type) else {
            return true
        }
        errorMessage = validStatus
        return false
    }
    
    private func isValid(image: UIImage?) -> Bool {
        if image != nil{
            return true
        }
        errorMessage = "Please upload image"
        return false
    }
    
    
    
    
    func isValid(name full:String? , password: String? , email: String?) -> Valid{
        if isValid(type: .email, info: email) && isValid(type: .firstName, info: full) && isValid(type: .password, info: password)  {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    
    
    func isValidEditProfile(name first:String? , last:String? , image: UIImage?  , userName: String? , email: String?) -> Valid{
        if isValid(image: image) && isValid(type: .firstName, info: first) &&  isValid(type: .lastName, info: last) && isValid(type: .userName , info: userName) && isValid(type: .email, info: email)  {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidFb(name first:String? , last:String? , image: UIImage?  , userName: String?, email: String? ) -> Valid{
        if isValid(type: .firstName, info: first) &&  isValid(type: .lastName, info: last) && isValid(type: .userName , info: userName) && isValid(type: .email, info: email) {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidSocialLoginInfo(first:String? , last:String? ) -> Valid{
        if isValid(type: .firstName, info: first) &&  isValid(type: .lastName, info: last){
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidAlreadyExist(phoneNum: String? , countryCode: String?) -> Valid {
        if isValid(type: .mobile, info: phoneNum)  && isValid(type: .countryCode, info: countryCode){
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidOTP(otpText:String?) -> Valid {
        if isValid(type: .otp, info: otpText) {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidForgetPassword(phone: String? ) -> Valid{
        if isValid(type: .password, info: phone){
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValid(phoneNum: String? ) -> Valid{
        if isValid(type: .mobile, info: phoneNum){
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidaComment(comment: String? ) -> Valid{
        if isValid(type: .comment, info: comment){
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidSignUp( password: String? , firstName: String? , lastName: String?) -> Valid{
        if isValid(type: .firstName, info: firstName) &&  isValid(type: .lastName, info: lastName) &&  isValid(type: .password, info: password)  {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidLogin( password: String? , phoneNumber: String? , countryCode: String?) -> Valid{
        if isValid(type: .password, info: password) && isValid(type: .mobile, info: phoneNumber)  && isValid(type: .countryCode, info: countryCode) {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    
    func isValidVehicleDetails(vehicleMake: String? , model: String? , color: String? , version: String? , year: String? , licensePlate: String? , state: String? ) -> Valid{
        if isValid(type: .vehicleMake, info: vehicleMake) && isValid(type: .model, info: model)  && isValid(type: .color, info: color) && isValid(type: .version, info: version)  && isValid(type: .year, info: year)  && isValid(type: .licencePlate, info: licensePlate) && isValid(type: .state, info: state) {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidProfile(firstName: String? , lastName: String? , email: String? , original: String? , thumbnail: String?) -> Valid{
        if isValid(type: .firstName, info: firstName) &&  isValid(type: .lastName, info: lastName)  {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
    func isValidVenueDetails(title: String? , location : String? , tags : String? , dateTime: String?) -> Valid{
        if isValid(type: .venueTitle, info: title) && isValid(type: .location, info: location)  && isValid(type: .tags, info: tags) && isValid(type: .dateTime, info: dateTime) {
            return .success
        }
        return errorMsg(str: errorMessage)
    }
    
}


import UIKit

enum FieldType : String{
    
    case firstName = "full name"
    case lastName = "Last name"
    case email = "email"
    case password = "password"
    case emailPassword = "Password "
    case oldPassword = "Old Password"
    case newPassword = "New Password"
    case info = ""
    case mobile = "Mobile Number"
    case cardNumber = "Card Number"
    case cvv = "CVV"
    case zip = "Zip Code"
    case amount = "Amount"
    case image
    case userName = "username"
    case userNameOrEmail = "email id"
    case comment = "Comment"
    case nationalId = "National ID Number"
    case address = "Address"
    case areas = "Road Number"
    case postalCode = "Postal Code"
    case city = "City"
    case state = "State"
    case country = "Country"
    case countryCode = "Country code"
    case buildingName = "Block Number"
    case apartment = "Apartment / Villa No"
    case otp = "OTP"
    case vehicleMake = "vehicle make"
    
    case model = "model"
    case color = "color"
    case version = "version"
    case year = "year"
    case licencePlate = "licence plate"
    case gas = "Gas grade"
    case frontView = "Front view Image"
    case backView = "Back View Image"
    case sideView = "Side View Image"
    
    //Venue Detail
    case venueTitle = "venue title"
    case location = "location"
    case tags = "tag"
    case dateTime = "date and time"
    
}

extension String {
    
    enum Status : String {
        
        case chooseEmpty = "Please choose "
        case empty = "Please enter "
        case allSpaces = " field should not be blank "
        case singleDot = "Only single period allowed for "
        case singleDash = "Only single dash allowed for "
        case singleSpace = "Only single space allowed for "
        case singleApostrophes = "Only single apostrophes allowed for "
        case valid
        case inValid = "Please enter a valid "
       
        case hasSpecialCharacter = " must not contain special character"
        case notANumber = " must be a number"
        case passwrdLength = " Password length must be at least 6 characters long."
        case mobileNumberLength = " Mobile Number should be 5 - 15 digits"
        case emptyCountrCode = " Choose country code"
        case containsSpace = " field should not contain space"
        case containsAtTheRateCharacter = " field should not contain @ character"
        case minimumCharacters = " field should have atleast two characters"
        case minimumUsernameCharacters = " field should have atleast three characters"
        case passwordFormat = " field should have at least one lowercase letter, at least one uppercase letter, at least one special character, at least one digit and 6 - 16 characters"
        case usernameFormat = " field should have alphabetic characters, numeric characters, underscores, periods, and dashes only"
        
        
        func message(type : FieldType) -> String? {
            
            switch self {
            case .hasSpecialCharacter , .allSpaces , .passwordFormat ,.usernameFormat : return type.rawValue + rawValue
            case .valid: return nil
            case .passwrdLength , .mobileNumberLength , .emptyCountrCode : return  rawValue
            case .containsSpace: return type.rawValue + rawValue
            case .containsAtTheRateCharacter , .minimumCharacters , .minimumUsernameCharacters : return type.rawValue + rawValue
            //            case .singleDot , .singleDash , .singleSpace , .singleApostrophes : return rawValue
            
            default: return rawValue + type.rawValue
            }
        }
    }
    
    func handleStatus(fieldType : FieldType) -> String? {
        
        switch fieldType {
        case .firstName , .lastName :
            return  isValidName.message(type: fieldType)
        case .userName:
            return  isValidUserName.message(type: fieldType)
        case .email:
            return  isValidEmail.message(type: fieldType)
        case .password , .oldPassword , .newPassword:
            return  isValid(password: 6, max: 16).message(type: fieldType)
        case .info:
            return  isValidInformation.message(type: fieldType)
        case .mobile:
            return  isValidPhoneNumber.message(type: fieldType)
        case .cardNumber:
            return  isValidCardNumber(length: 16).message(type: fieldType)
        case .cvv:
            return  isValidCVV.message(type: fieldType)
        case .zip:
            return  isValidZipCode.message(type: fieldType)
        case .amount:
            return  isValidAmount.message(type: fieldType)
        case .image:
            return "Please upload image"
        case .userNameOrEmail:
            return isValidLogin.message(type:fieldType)
            
        case .emailPassword:
            return isValidLoginPassword.message(type:fieldType)
            
        case .comment:
            return isValidComment.message(type:fieldType)
        case .nationalId:
            return  isValidInformation.message(type: fieldType)
        case .address:
            return  isValidInformation.message(type: fieldType)
        case .postalCode:
            return  isValidInformation.message(type: fieldType)
        case .city:
            return  isValidInformation.message(type: fieldType)
        case .state:
            return  isValidVehicleInformation.message(type: fieldType)
        case .country:
            return  isValidInformation.message(type: fieldType)
        case .countryCode:
            return  isValidInformation.message(type: fieldType)
        case .buildingName:
            return isValidInformation.message(type: fieldType)
        case .apartment:
            return isValidInformation.message(type: fieldType)
            
        case .areas:
            return isValidInformation.message(type: fieldType)
            
        case .otp:
            return isValidInformation.message(type: fieldType)
            
            
        case .vehicleMake :
            return isValidVehicleInformation.message(type: fieldType)
        case .model :
            return isValidVehicleInformation.message(type: fieldType)
        case .color :
            return isValidVehicleInformation.message(type: fieldType)
        case .version :
            return isValidVehicleInformation.message(type: fieldType)
        case .year :
            return isValidVehicleInformation.message(type: fieldType)
        case .licencePlate :
            return isValidInformation.message(type: fieldType)
        case .gas :
            return isValidVehicleInformation.message(type: fieldType)
        case .frontView :
            return isValidInformation.message(type: fieldType)
        case .backView :
            return isValidInformation.message(type: fieldType)
        case .sideView :
            return isValidInformation.message(type: fieldType)
        
            
        case .venueTitle :
            return isValidVehicleInformation.message(type: fieldType)
        case .location :
             return isValidVehicleInformation.message(type: fieldType)
        case .tags :
             return isValidVehicleInformation.message(type: fieldType)
        case .dateTime :
             return isValidVehicleInformation.message(type: fieldType)
            
        }
    }
    
    var isNumber : Bool {
        if let _ = NumberFormatter().number(from: self) {
            return true
        }
        return false
    }
    
    var hasSpecialCharcters : Bool {
        return rangeOfCharacter(from: CharacterSet.alphanumerics.inverted) != nil
    }
    
    var isEveryCharcterZero : Bool{
        var count = 0
        self.characters.forEach {
            if $0 == "0"{
                count += 1
            }
        }
        if count == self.characters.count{
            return true
        }else{
            return false
        }
    }
    
    public func toString(format: String , date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = format
        return formatter.string(from: date)
    }
    
    public var length: Int {
        return self.characters.count
    }
    
    public var isEmail: Bool {
        let dataDetector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue)
        let firstMatch = dataDetector?.firstMatch(in: self, options: NSRegularExpression.MatchingOptions.reportCompletion, range: NSRange(location: 0, length: length))
        return (firstMatch?.range.location != NSNotFound && firstMatch?.url?.scheme == "mailto")
    }
    
    
    public var isBlank: Bool {
        get {
            let trimmed = trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty
        }
    }
    
    public var isSingleDotOrNoDot : Bool{
        get {
            let count = self.countInstances(of: ".")
            return (count == 0 || count == 1)
        }
    }
    
    public var isSingleSpaceOrNoSpace : Bool{
        get {
            let count = self.countInstances(of: " ")
            return (count == 0 || count == 1)
        }
    }
    
    
    public var isSingleDashOrNoDash : Bool{
        get {
            let count = self.countInstances(of: "-")
            return (count == 0 || count == 1)
        }
    }
    
    
    public var isSingleApostrophesOrNoApostrophes: Bool{
        get {
            let count = self.countInstances(of: "'")
            return (count == 0 || count == 1)
        }
    }
    
    func countInstances(of stringToFind: String) -> Int {
        assert(!stringToFind.isEmpty)
        var searchRange: Range<String.Index>?
        var count = 0
        while let foundRange = range(of: stringToFind, options: .diacriticInsensitive, range: searchRange) {
            searchRange = Range(uncheckedBounds: (lower: foundRange.upperBound, upper: endIndex))
            count += 1
        }
        return count
    }
    
    
    
    var isValidUserName : Status {
        if length <= 0 { return .empty }
        if isBlank { return .allSpaces }
        if self.contains(" ") { return .containsSpace }
        if length < 6 { return .minimumUsernameCharacters }
        if hasSpecialCharcters {
            let isUsernameFormat = checkUsername(text: self)
            if !isUsernameFormat { return .usernameFormat }
        }
        if self.contains("@") { return .containsAtTheRateCharacter }
        return .valid
    }
    
    func isValid(password min: Int , max: Int) -> Status {
        if length <= 0 { return .empty }
        if isBlank  { return .allSpaces  }
        
        //let isPasswordFormat = checkPassword(text: self)
        //if !isPasswordFormat { return .passwordFormat }
        if self.characters.count >= min && self.characters.count <= max { return .valid } else{
            return .passwrdLength
        }
        return .valid
    }
    
    var isValidLoginPassword: Status {
        if length <= 0 { return .empty }
        if isBlank  { return .allSpaces  }
        return .valid
    }
    
    func checkPassword(text : String?) -> Bool{
        let regex = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&!^~-]).{8,16})"
        let isMatched = NSPredicate(format:"SELF MATCHES %@", regex).evaluate(with: text)
        if isMatched{
            return true
        }else {
            return false
        }
    }
    
    func checkUsername(text : String?) -> Bool{
        let characterSet:  NSMutableCharacterSet = NSMutableCharacterSet.alphanumeric()
        characterSet.addCharacters(in: "_-.")
        let characterSetInverted:  NSMutableCharacterSet = characterSet.inverted as! NSMutableCharacterSet
        if text?.rangeOfCharacter(from: characterSetInverted as CharacterSet) != nil {
            return false
        }else {
            return true
        }
    }
    
    var isValidComment : Status {
        if length <= 0 { return .empty }
        if isBlank { return .allSpaces }
        return .valid
    }
    
    var isValidEmail : Status {
        if length <= 0 { return .empty }
        if isBlank { return .allSpaces }
        if isEmail { return .valid }
        return .inValid
    }
    
    var isValidLogin : Status {
        if length <= 0 { return .empty }
        if isBlank { return .allSpaces }
        return .valid
    }
    
    var isValidInformation : Status {
        if length <= 0 { return .empty }
        if isBlank { return .allSpaces }
        return .valid
    }
    
    var isValidVehicleInformation : Status {
        if length <= 0 { return .chooseEmpty }
        if isBlank { return .allSpaces }
        return .valid
    }
    
    var isValidPhoneNumber : Status {
        if length < 0 { return .empty }
        if isBlank { return .allSpaces }
        if isEveryCharcterZero { return .inValid }
        if hasSpecialCharcters { return .hasSpecialCharacter }
        if characters.count >= 5 && self.characters.count <= 20 { return .valid
        }else{
            return .mobileNumberLength
        }
    }
    
    
    
    var isValidName : Status {
        if length < 0 { return .empty }
        if isBlank { return .empty }
        //        if !isSingleDotOrNoDot { return .singleDot }
        //        if !isSingleDashOrNoDash { return .singleDash }
//        if !isSingleSpaceOrNoSpace { return .singleSpace }
        //        if !isSingleApostrophesOrNoApostrophes { return .singleApostrophes }
        var newName = self.replacingOccurrences(of: "-", with: "")
        newName = newName.replacingOccurrences(of: ".", with: "")
        newName = newName.replacingOccurrences(of: "'", with: "")
        newName = newName.replacingOccurrences(of: " ", with: "")
        if newName.hasSpecialCharcters { return .hasSpecialCharacter }
//        if length < 2 { return .minimumCharacters }
        return .valid
    }
    
    func isValidCardNumber(length max:Int ) -> Status {
        if length < 0 { return .empty }
        if isBlank { return .allSpaces }
        if hasSpecialCharcters { return .hasSpecialCharacter }
        if isEveryCharcterZero { return .inValid }
        if characters.count >= 16 && characters.count <= max{
            return .valid
        }
        return .inValid
    }
    
    var isValidCVV : Status {
        if hasSpecialCharcters { return .hasSpecialCharacter }
        if isEveryCharcterZero { return .inValid }
        if isNumber{
            if self.characters.count >= 3 && self.characters.count <= 4{
                return .valid
            }else{ return .inValid }
        }else { return .notANumber }
    }
    
    var isValidZipCode : Status {
        if length < 0 { return .empty }
        if isEveryCharcterZero { return .inValid }
        if isBlank { return .allSpaces }
        if !isNumber{ return .notANumber }
        return .valid
    }
    
    var isValidAmount :  Status {
        if length < 0 { return .empty }
        if isBlank { return .allSpaces }
        if !isNumber{ return .notANumber }
        return .valid
    }
    
}


//VALIDATION OF PAYMENT
enum Fields :String
{
    case name = "([A-Z][a-z]*)([\\s\\\'-][A-Z][a-z]*)*"
    case Email = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    case cardNumber = "[0-9]"
    case cvv = "[0-9]{3}"
    
}
var count : Bool = false

public class FieldCheck
{
    static func validate(textValue:Fields, value : String) -> Bool
    {
        count = NSPredicate(format:"SELF MATCHES %@", textValue.rawValue).evaluate(with: value)
        return count
    }
}

