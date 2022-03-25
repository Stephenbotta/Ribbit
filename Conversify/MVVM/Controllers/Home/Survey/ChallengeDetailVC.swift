//
//  ChallengeDetailVC.swift
//  Conversify
//
//  Created by admin on 16/03/20.
//

import UIKit

class ChallengeDetailVC: BaseRxViewController {
    
    //MARK::- IBOutlets
    @IBOutlet weak var challengeView: UIImageView!
    @IBOutlet weak var labelChallengeStatus: UILabel!
    @IBOutlet weak var labelChallengeDAte: UILabel!
    @IBOutlet weak var labelChallengeDesc: UILabel!
    @IBOutlet weak var labelChallengename: UILabel!
    @IBOutlet weak var btnStatus: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    
    //MARK::- Properties
    var surveyInfo = SurveyViewModel()
    var rewordPoint: Int?
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setUpChallengeData()
        surveyInfo.getChallengeDetails(challengeId: /surveyInfo.selectedChallenge.value?._id) { (isSuccess) in
            self.setUpChallengeData()
        }
    }
    //MARK::- BINDINGS
    override func bindings() {
        
        btnStatus.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            
            self?.surveyInfo.startChallenges(challengeId: /self?.surveyInfo.selectedChallenge.value?._id, { (isSuccess) in
                
                if isSuccess {
                    let points = "\(/self?.rewordPoint)"
                    UtilityFunctions.showSingleButton(alert: "", message: "Your challenge started sucessfully. You will earn \(points) points after completing this challenge.", buttonOk: {
                        let challnge = self?.surveyInfo.selectedChallenge.value
                        challnge?.status = .inprogress
                        self?.surveyInfo.selectedChallenge.value = challnge
                        self?.popVC()
                    }, viewController: /self, buttonText: "Ok")
                }
                
            })
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popVC()
        })<bag
    }
    
    func setUpChallengeData(){
        let challnge = surveyInfo.selectedChallenge.value
        labelChallengename.text = challnge?.title
        let url = URL(string: "")
        challengeView.image(url: challnge?.imgUrl?.thumbnail ?? "")
//        challengeView.image(url: challnge?.imgUrl?.thumbnail ?? )
        labelChallengeDesc.text = challnge?.challengeDescription
        labelChallengeStatus.text = (/challnge?._id == /challnge?.otherChallengeInProgress?.challenge_id) ? /challnge?.otherChallengeInProgress?.status?.rawValue : ""
        btnStatus.isHidden = challnge?.otherChallengeInProgress != nil
        
        
        let startDate = Date.init(fromString: /challnge?.startDate, format: "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        let _startDate = startDate?.toString(DateFormat.custom("MM/dd/yyyy"))
        let endDate = Date.init(fromString: /challnge?.endDate, format: "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        let _endDate = endDate?.toString(DateFormat.custom("MM/dd/yyyy"))
        labelChallengeDAte?.text = /_startDate + " - " + /_endDate
        if /endDate < Date() {
            btnStatus.setTitle("Expired", for: .normal)
            btnStatus.isUserInteractionEnabled = false
        }
    }
    
}
