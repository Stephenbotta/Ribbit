//
//  SurveyListingViewController.swift
//  Conversify
//
//  Created by Apple on 10/12/19.
//

import UIKit

class SurveyListingViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView.delegate = self
            tableView.dataSource = self
            tableView.estimatedRowHeight = 64
        }
    }
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnEdit: UIButton!
    
    //MARK::- PROPERTIES
    var surveyInfo = SurveyViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        getListOfSurvey()
        // Do any additional setup after loading the view.
    }
    
    //MARK::- BINDINGS
    
    override func bindings() {
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popToRootVC()
            
        })<bag
        btnEdit.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            guard let vc = R.storyboard.survey.takeSurveyViewController() else { return }
            self?.pushVC(vc)
            
        })<bag
    }
}

//MARK::- API Calls
extension SurveyListingViewController {
    
    func getListOfSurvey(){
        surveyInfo.getSurveyList { [weak self](results) in
            self?.tableView.reloadData()
        }
    }
}

//MARK::- UITableViewDelegate , UITableViewDataSource
extension SurveyListingViewController : UITableViewDelegate , UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.surveyListCell.identifier, for: indexPath) as? SurveyListCell else { return UITableViewCell()}
        cell.item = surveyInfo.surveyList.value?.surveyList?[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return /surveyInfo.surveyList.value?.surveyList?.count
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableView.automaticDimension
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if /surveyInfo.surveyList.value?.surveyList?[indexPath.row].questionCount > 0 {
            guard let vc = R.storyboard.survey.surveyQuestionsVC() else { return }
            vc.questionsSubmitted = { (isSubmitted) in
                self.getListOfSurvey()
            }
            vc.surveyModel.pointsEarned = /surveyInfo.surveyList.value?.surveyList?[indexPath.row].rewardPoints?.toString
            vc.surveyModel.surveyName = /surveyInfo.surveyList.value?.surveyList?[indexPath.row].name
            vc.surveyModel.timeSurvey = /surveyInfo.surveyList.value?.surveyList?[indexPath.row].totalTime?.toString
            vc.surveyModel.surveyId = /surveyInfo.surveyList.value?.surveyList?[indexPath.row].surveyId
            pushVC(vc)
        }
        else{
            UtilityFunctions.makeToast(text: "This Survey has empty data", type: .error)
        }
    }
}
