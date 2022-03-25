//
//  SplashViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 08/10/18.
//

import UIKit
import RxSwift
import GestureRecognizerClosures

class SplashViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var labelTermsAndConditions: UILabel!
    @IBOutlet weak var btnGetStarted: UIButton!
    @IBOutlet weak var labelAlreadyAMember: UILabel!
    
    //MARK::- PROPERTIES
    var viewModal = SplashViewModal()
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    
    
    //MARK::- BINDINGS
    override func bindings() {
        super.bindings()
        labelTermsAndConditions.addTapGesture { (gesture) in
            guard let url = URL(string: APIConstants.terms) else { return }
            UIApplication.shared.open(url)
        }
        btnGetStarted.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let registerVc = R.storyboard.main.registerViewController() else { return }
            self?.pushVC(registerVc)
//            self?.present(registerVc, animated: true, completion: nil)
        })<bag
        onLoad()
    }
    
}


//MARK::- FUNCTIONS
extension SplashViewController {
    
    func onLoad(){
        
        if /Singleton.sharedInstance.loggedInUser?.token != "" {
            if /Singleton.sharedInstance.loggedInUser?.isInterestSelected && /Singleton.sharedInstance.loggedInUser?.isProfileComplete && /Singleton.sharedInstance.loggedInUser?.isVerified && /Singleton.sharedInstance.loggedInUser?.isPasswordExist{
                moveToHome()
            }else if /Singleton.sharedInstance.loggedInUser?.isInterestSelected && /Singleton.sharedInstance.loggedInUser?.isProfileComplete && /Singleton.sharedInstance.loggedInUser?.isVerified && (/Singleton.sharedInstance.loggedInUser?.facebookId != "" || /Singleton.sharedInstance.loggedInUser?.googleId != ""){
                moveToHome()
            }
            else{
                guard let user = Singleton.sharedInstance.loggedInUser else { return }
                if !(/user.isProfileComplete){
                    guard let vc = R.storyboard.main.createProfileViewController() else { return }
                    vc.createProVM = CreateProfileViewModal(user: user , countryDet: nil)
                    self.pushVC(vc)
//                    self.present(vc, animated: true, completion: nil)
                }else {
                        moveToSelectInterest()
                }
            }
        }
        
        labelTermsAndConditions.attributedText = NSMutableAttributedString().specifyAttributes("By Signing in you are agree to Check It’s " , font: UIFont.systemFont(ofSize: 12)).specifyAttributes("Terms & conditions", font: UIFont.boldSystemFont(ofSize: 12)).specifyAttributes(" and ", font: UIFont.systemFont(ofSize: 12)).specifyAttributes("Privacy policy.", font: UIFont.boldSystemFont(ofSize: 12))
        
        labelAlreadyAMember.attributedText = NSMutableAttributedString().specifyAttributes("Already a member? " , font: UIFont.systemFont(ofSize: 16)).specifyAttributes("Sign in", font: UIFont.boldSystemFont(ofSize: 16))
        
        labelAlreadyAMember.onTap { [weak self] (tap) in
            guard let logVc = R.storyboard.main.logViewController() else { return }
            self?.pushVC(logVc)
//            self?.present(logVc, animated: true, completion: nil)
        }
        
    }
    
    func moveToSelectInterest(){
        
        guard let interestVc = R.storyboard.main.selectInterestsViewController() else { return }
        self.pushVC(interestVc)
//        self.navigationController?.present(interestVc, animated: true, completion: nil)
    }
}
