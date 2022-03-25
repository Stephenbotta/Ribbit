//
//  RewardsViewController.swift
//  Conversify
//
//  Created by Apple on 05/12/19.
//

import UIKit

class RewardsViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var labelRedeemed: UILabel!
    @IBOutlet weak var labelPointsEarned: UILabel!
    @IBOutlet weak var labelTotalSurvey: UILabel!
    @IBOutlet weak var userProfileImage: UIImageView?
    @IBOutlet weak var labelDesignation: UILabel!
    @IBOutlet weak var labelUserName: UILabel!
    @IBOutlet weak var lblDailyChallenge: UILabel!
    @IBOutlet var circularSurveyView: [UIView]!{
        didSet{
            circularSurveyView.forEach { $0.layer.cornerRadius = ($0.frame.width / 2.0) + 1 }
        }
    }
    @IBOutlet weak var btnTakeSurvey: UIButton!
    @IBOutlet weak var btnMyChallenges: UIButton!
    @IBOutlet weak var buyGiftCardButton: UIButton!
    @IBOutlet weak var wantToDonateButton: UIButton!
    @IBOutlet weak var btnAnalitics: UIButton!
    
    var profileVM = ProfileDetailViewModal()
    let myView = AddView()
    override func viewDidLoad() {
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self, selector: #selector(testFunc), name: NSNotification.Name(rawValue:  "PeformAfterPresenting"), object: nil)
        setUpViewData()
                  
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        setUpViewData()
        getDailyChailenge()
    }
    //MARK::- BINDINGS
    
    @IBAction func btnChailenge(_ sender: Any) {
        myView.frame = CGRect(x: 0, y: 0, width: self.view.frame.width, height: self.view.frame.height - 200)
        
        myView.btnOk.addTarget(self, action: #selector(btnActionOK), for: .touchUpInside)
        myView.lblChalenge.text = /self.profileVM.daylyChalengeData.value?.daiLyChailenge?[0].title
        myView.lblDiscription.text = /self.profileVM.daylyChalengeData.value?.daiLyChailenge?[0].description
        myView.lblrewardPoint.text = "Reward Point :- " + String(/self.profileVM.daylyChalengeData.value?.daiLyChailenge?[0].rewardPoint)
        self.tabBarController?.tabBar.isHidden = true
        self.view.addSubview(myView)
    }
    @objc func btnActionOK(sender: UIButton!) {

        self.myView.removeFromSuperview()
        self.tabBarController?.tabBar.isHidden = false
       }

    override func bindings() {
        btnTakeSurvey.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            let isSurveyTaken = /Singleton.sharedInstance.loggedInUser?.isTakeSurvey
            
            if isSurveyTaken {
                guard  let vc = R.storyboard.survey.surveyListingViewController() else { return }
                self?.pushVC(vc)
            }else{
                guard let vc = R.storyboard.survey.takeSurveyViewController() else { return }
                self?.pushVC(vc)
            }
        })<bag
        btnAnalitics.rx.tap.asDriver().drive(onNext:{
            guard let vc = R.storyboard.survey.analiticsViewController() else{return}
            self.pushVC(vc)
        })
        btnMyChallenges.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            guard  let vc = R.storyboard.survey.myChallengesVC() else { return }
            self?.pushVC(vc)
            
        })<bag
        
        buyGiftCardButton.rx.tap.asDriver().drive(onNext: {
            guard  let vc = R.storyboard.survey.giftCardsViewController() else { return }
            self.pushVC(vc)
        })<bag
        
        wantToDonateButton.rx.tap.asDriver().drive(onNext: {
            guard  let vc = R.storyboard.survey.redeemHistoryViewController() else { return }
            vc.modalPresentationStyle = .overFullScreen
            vc.modalTransitionStyle = .crossDissolve
            //vc.modalPresentationStyle = .fullScreen
            vc.screenType = .wantToDonate
            DispatchQueue.main.async {
                self.present(vc, animated: true, completion: nil)
            }
            
        })<bag
    }
    
    //SET UP DATA
    func setUpViewData(){
        let userData = /Singleton.sharedInstance.loggedInUser
        labelUserName.text = /userData.firstName
        labelDesignation.text  = /userData.designation
        labelRedeemed.text = Int(/userData.pointRedeemed).toString
        labelPointsEarned.text = Int(/userData.pointEarned).toString
        labelTotalSurvey.text = "0"
        userProfileImage?.setImage(image: userData.img?.thumbnail, placeholder: R.image.ic_a())
        profileVM.getUserStats { (_) in
            self.labelRedeemed.text = Int(/self.profileVM.stats.value?.pointRedeemed).toString
            self.labelPointsEarned.text = Int(/self.profileVM.stats.value?.pointEarned).toString
            self.labelTotalSurvey.text = Int(/self.profileVM.stats.value?.totalSurveyGiven).toString
        }
    }
    
    @objc func testFunc() {
         setUpViewData()
       }
    func getDailyChailenge(){
        profileVM.dailyChalenge { (_) in
         //   print(Int(/self.profileVM.daylyChalengeData.value?.title)?.toString)
            self.lblDailyChallenge.text = /self.profileVM.daylyChalengeData.value?.daiLyChailenge?[0].title
//            self.labelPointsEarned.text = Int(/self.profileVM.stats.value?.pointEarned).toString
//            self.labelTotalSurvey.text = Int(/self.profileVM.stats.value?.totalSurveyGiven).toString
        }
    }
}
