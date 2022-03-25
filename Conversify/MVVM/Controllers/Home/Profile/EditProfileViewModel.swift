//
//  EditProfileViewModel.swift
//  Conversify
//
//  Created by Apple on 07/12/18.
//
import UIKit
import RxSwift
import Foundation
import RxCocoa
import RxDataSources

class EditProfileViewModel: BaseRxViewModel {
    
    var arrayEditProfile = Variable<[EditProfileElements]>([])
    var profilePic = Variable<UIImage?>(nil)
    var user: User?
    var profileUrl  = Variable<String?>(nil)
    var selectedImage = Variable<UIImage?>(nil)
    var gender = Variable<String?>(nil)
    var backRefresh: (()->())?
    var fullName = Variable<String?>(nil)
    var email = Variable<String?>(nil)
    var userName = Variable<String?>(nil)
    var phoneNumber = Variable<String?>(nil)
    
    func saveProfileData(userName : String? , fullName : String? , bio : String? , website : String? , email : String? , gender : String? , dob : String? , designation : String? , company : String? ,_ completion:@escaping (Bool)->()){
        
        
        ProfileTarget.editProfile(userName: userName, fullName: fullName, bio: bio, website: website, email: email, gender: gender, dateOfBirth: dob, designation: designation, company: company, imageOriginal: profileUrl.value , imageThumbnail : profileUrl.value).request(apiBarrier: true).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<User> else{
                    return
                }
                Singleton.sharedInstance.loggedInUser = safeResponse.data
                
                completion(true)
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    
    func saveEditProfileData(userName : String? , fullName : String? , bio : String? , website : String? , email : String? , gender : String? , dob : String? , designation : String? , company : String? ,_ completion:@escaping (Bool)->()){
        
        if !validateProfile(){
            return
        }
        
        if selectedImage.value != nil && profilePic.value != selectedImage.value {
            uploadImage { (status) in
                Loader.shared.stop()
                self.saveProfileData(userName: userName, fullName: fullName, bio: bio, website: website, email: email, gender: gender, dob: dob, designation: designation, company: company, { (finished) in
                    completion(finished)
                })
            }
        }else {
            self.saveProfileData(userName: userName, fullName: fullName, bio: bio, website: website, email: email, gender: gender, dob: dob, designation: designation, company: company, { (finished) in
                completion(finished)
            })
        }
    }
    
    func uploadImage(_ completion:@escaping (Bool)->()){
        Loader.shared.start()
        S3.upload(image: selectedImage.value , success: { (imageName) in
            print(imageName)
            self.profileUrl.value = imageName
            completion(true)
        }) { (error) in
            Loader.shared.stop()
            print(error)
        }
    }
    
    func validateProfile() -> Bool{
        let status = Validation.shared.isValidEditProfile(name: /fullName.value, userName: /userName.value, email: /email.value, phoneNum: /phoneNumber.value)
        
        switch status {
        case .success:
            return true
        case .failure:
            return false
        }
        
    }
    
    
    func getEditProfileData(){
        let user = Singleton.sharedInstance.loggedInUser
        let gender = user?.gender?.lowercased()
        arrayEditProfile.value = [ EditProfileElements(head: "", items: [ItemDetail(title: "Name", placeHolder: "Enter name here", keyBoardType: UIKeyboardType.default, text: user?.firstName) , ItemDetail(title: "Username", placeHolder: "Enter username" , keyBoardType: UIKeyboardType.default , text: user?.userName), ItemDetail(title: "Website", placeHolder: "Enter website link" , keyBoardType: UIKeyboardType.default , text: user?.website), ItemDetail(title: "Bio", placeHolder: "Enter your Bio here" ,keyBoardType: UIKeyboardType.default , text: user?.bio) ]) , EditProfileElements(head: "Professional Information", items: [ItemDetail(title: "Designation", placeHolder: "Enter designation" , keyBoardType: UIKeyboardType.default , text: user?.designation) , ItemDetail(title: "Company/WorkPlace", placeHolder: "Enter company"  , keyBoardType: UIKeyboardType.default , text: user?.company)]) ,EditProfileElements(head: "Private Information", items: [ItemDetail(title: "Email", placeHolder: "Enter email" , keyBoardType: UIKeyboardType.emailAddress , text: user?.email) ,ItemDetail(title: "Phone", placeHolder: "Enter phone no" , keyBoardType: UIKeyboardType.phonePad , text: (/user?.countryCode + /user?.phoneNumber))
            , ItemDetail(title: "Gender", placeHolder: "Select gender" , keyBoardType: UIKeyboardType.default , text: gender?.capitalizedFirst())])]
    }
    
}

struct EditProfileElements {
    var head: String
    var items: [Item]
}

struct ItemDetail {
    var title : String
    var placeHolder : String
    var keyBoardType: UIKeyboardType?
    var text : String?
}
extension EditProfileElements : SectionModelType {
    
    typealias Item = ItemDetail
    
    init(original: EditProfileElements, items: [Item]) {
        self = original
        self.items = items
    }
}

