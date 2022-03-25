//
//  PrivacyViewController.swift
//  Conversify
//
//  Created by Harminder on 09/01/19.
//

import UIKit
import IBAnimatable

class PrivacyViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var btnPrivacy: UIButton!
    @IBOutlet weak var btnPrivacyPersonalInfo: UIButton!
    @IBOutlet weak var btnSelectedPicCount: UIButton!
    @IBOutlet weak var btnPrivacyProfilePic: UIButton!
    @IBOutlet weak var btnBack: AnimatableButton!
    @IBOutlet weak var btnPrivacyMessageMe: UIButton!
    @IBOutlet weak var btnPrivacyUserName: UIButton!
    @IBOutlet weak var btnPrivacyUserNameCount: UIButton!
    @IBOutlet weak var btnPrivacyPrivateInfoCount: UIButton!
    @IBOutlet weak var btnPrivacyMessageMeCount: UIButton!
    
    //MARK::- PROPERTIES
    
    var privacyVM = SettingViewModel()
    
    override func viewWillAppear(_ animated: Bool) {
        updateUI()
    }
    
    //MARK::- BINDINGS
    
    override func bindings() {
        
        privacyVM.updated.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.updateUI()
                }
            })<bag
        
        btnPrivacy.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.btnPrivacy.isSelected = /self?.btnPrivacy.isSelected.toggle()
            self?.privacyVM.updatePrivacy(permission: /self?.btnPrivacy.isSelected)
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: { [weak self] () in
            
            self?.popVC()
        })<bag
        
        
        btnPrivacyProfilePic.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.update(selected: .profilePic)
        })<bag
        
        btnPrivacyPersonalInfo.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.update(selected: .privateInfo)
        })<bag
        
        btnPrivacyMessageMe.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.update(selected: .messageTag)
        })<bag
        
        btnPrivacyUserName.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.update(selected: .userName)
           
        })<bag
        
        
    }
    
    func update(selected: PrivacySelectedWhoCanSeeFlags?){
        guard let vc = R.storyboard.settings.followerViewController() else { return }
        vc.isFollower = nil
        vc.followerVM = FollowersViewModal(selectedPrivacy: selected)
        vc.followerVM.isProfile = false
        vc.followerVM.selectedUsers = { [weak self] users in
            if /users.count != 0{
                switch selected ?? .profilePic {
                case .profilePic :
                    self?.privacyVM.updateProfilePicSettings(everyOne: false, isMyFollowers: false, followers: users.map{ return /$0.id })
                case .privateInfo :
                    self?.privacyVM.updatePrivateInfoSettings(everyOne: false, permission: false, followers: users.map{ return /$0.id })
                case .userName :
                    self?.privacyVM.updateUserNameSettings(everyOne: false, nameVisibility: false, followers: users.map{ return /$0.id })
                case .messageTag :
                    self?.privacyVM.updateTagSettings(everyOne: false, tagging: false, followers: users.map{ return /$0.id })
                default: break
                }
                
            }
        }
        vc.followerVM.onlyFollowers = { [weak self] in
            switch selected ?? .profilePic {
            case .profilePic :
                self?.privacyVM.updateProfilePicSettings(everyOne: false, isMyFollowers: true, followers: [])
            case .privateInfo :
                self?.privacyVM.updatePrivateInfoSettings(everyOne: false, permission: true, followers: [])
            case .userName :
                self?.privacyVM.updateUserNameSettings(everyOne: false, nameVisibility: true, followers: [])
            case .messageTag :
                self?.privacyVM.updateTagSettings(everyOne: false, tagging: true, followers: [])
                default: break
            }
            
        }
        
        vc.followerVM.everyOne = { [weak self] in
            switch selected ?? .profilePic {
            case .profilePic :
                self?.privacyVM.updateProfilePicSettings(everyOne: true, isMyFollowers: false, followers: [])
            case .privateInfo :
                self?.privacyVM.updatePrivateInfoSettings(everyOne: true, permission: false, followers: [])
                //break
            case .userName :
                self?.privacyVM.updateUserNameSettings(everyOne: true, nameVisibility: false, followers: [])
            case .messageTag :
                self?.privacyVM.updateTagSettings(everyOne: true, tagging: false, followers: [])
            default: break
            }
            
        }
        
        self.pushVC(vc)
    }
    
    func updateUI(){
        let user = Singleton.sharedInstance.loggedInUser
        btnPrivacy.isSelected = /user?.isAccountPrivate
        btnSelectedPicCount.setTitle(/user?.imageVisibilityForFollowers ? ("My followers") : ( (/user?.imageVisibilityForEveryone ? "Everyone" :  /user?.imageVisibility?.count.toString + " people")) , for: .normal)
        
        btnPrivacyPrivateInfoCount.setTitle(/user?.personalInfoVisibilityForFollowers ? ("My followers") : ((/user?.infoVisibilityForEveryone ? "Everyone" :   /user?.personalInfoVisibility?.count.toString + " people")) , for: .normal)
        
        btnPrivacyUserNameCount.setTitle(/user?.nameVisibilityForFollowers ? "My followers" : ( (/user?.nameVisibilityForEveryone ? "Everyone" :  /user?.nameVisibility?.count.toString + " people"))    , for: .normal)
        
        
        btnPrivacyMessageMeCount.setTitle(/user?.tagPermissionForFollowers ? "My followers" : ( (/user?.tagPermissionForEveryone ? "Everyone" :  /user?.tagPermission?.count.toString + " people")) , for: .normal)
        
    }
    
}
