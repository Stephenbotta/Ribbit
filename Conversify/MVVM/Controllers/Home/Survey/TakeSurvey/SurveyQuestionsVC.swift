//
//  SurveyQuestionsVC.swift
//  Conversify
//
//  Created by Apple on 10/12/19.
//

import UIKit

typealias QuestionSubmitted = (Bool) -> ()

class SurveyQuestionsVC: BaseRxViewController {
    
    //MARK::- IBOUTLETS
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView.delegate = self
            tableView.dataSource = self
            tableView.estimatedRowHeight = 88.0
            tableView.estimatedSectionHeaderHeight = 120.0
            tableView.registerXIBForHeaderFooter("SurveyQuesHeaderView")
        }
    }
    
    @IBOutlet weak var labelSurveyName: UILabel!
    @IBOutlet weak var userImgView: UIImageView!
    @IBOutlet weak var btnNext: UIButton!
    @IBOutlet weak var labelAverageTime: UILabel!
    
    //MARK:- PROPERTIES
    var surveyModel = SurveyViewModel()
    var selectedQues = 0
    var currentQues : Int = 0
    var questionsSubmitted : QuestionSubmitted?
    var isSelectedIndex = -1
    override func viewDidLoad() {
        super.viewDidLoad()
        let userData = /Singleton.sharedInstance.loggedInUser
        userImgView?.setImage(image: userData.img?.thumbnail,placeholder: UIImage(named: "ic_account"))
        labelSurveyName.text = /surveyModel.surveyName
        let mins = Double(/surveyModel.timeSurvey)
        
        if /(mins) > 60.0 {
            var hrs = /(mins) / 60.0
            hrs = hrs.rounded(toPlaces: 2)
            labelAverageTime.text = "Average time " + hrs.toString + " hr"
        } else {
            let hrText = mins == 1 ? " min" : " mins"
            labelAverageTime.text = "Average time " + /surveyModel.timeSurvey + hrText
        }
    
        getSurveyQuestions()
    }
    
    @IBAction func btnActionCross(_ sender: UIButton) {
        popVC()
    }
    
    @IBAction func btnActionNext(_ sender: UIButton) {
         if /surveyModel.surveyQues.value?.quesInfo?.count == 0 {
            return
        }
        let answer = surveyModel.surveyQues.value?.quesInfo?[currentQues].options
        let selectedAns = answer?.filter({$0.isSelected})
        if /selectedAns?.count > 0 {
            if currentQues < selectedQues {
                currentQues += 1
                isSelectedIndex = -1
                if currentQues == (selectedQues - 1) {
                    btnNext.setTitle("Submit", for: .normal)
                }
                tableView.reloadData()
            }else if currentQues == selectedQues {
                submitQusestionsSurvey()
            }
        }else{
            UtilityFunctions.makeToast(text: "Please select answer", type: .error)
        }
    }
}

//MARK::- UITableViewDataSource , UITableViewDelegate
extension SurveyQuestionsVC : UITableViewDataSource , UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if /surveyModel.surveyQues.value?.quesInfo?.count > 0 {
        if let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: "SurveyQuesHeaderView"){
            (header as?  SurveyQuesHeaderView)?.item = surveyModel.surveyQues.value?.quesInfo?[currentQues]
            (header as?  SurveyQuesHeaderView)?.labelQuestionNumber.text = "Question No. \(currentQues + 1)"
            return header
        }else {
            return UIView()
        }
        }else {
            return UIView()
        }
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if /surveyModel.surveyQues.value?.quesInfo?.count > 0 {
             return /surveyModel.surveyQues.value?.quesInfo?[currentQues].options?.count
        }else{
            return 0
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.surveyAnswerCell.identifier, for: indexPath) as? SurveyAnswerCell else  { return UITableViewCell()}
        cell.item = surveyModel.surveyQues.value?.quesInfo?[currentQues].options?[indexPath.row]
        cell.btnCheckbox.isSelected = isSelectedIndex == indexPath.row ? true:false
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return UITableView.automaticDimension
    }
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableView.automaticDimension
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        isSelectedIndex = indexPath.row
        
      let iscellSelected = surveyModel.surveyQues.value?.quesInfo?[currentQues].options?[indexPath.row].isSelected
        surveyModel.surveyQues.value?.quesInfo?[currentQues].options?[indexPath.row].isSelected = !(iscellSelected ?? false)
        tableView.reloadData()
       
        
        
       
  
    }
}


extension SurveyQuestionsVC {
    
    //MARK::- API CALLS
    func getSurveyQuestions(){
        surveyModel.getSurveyQuestions({ (result) in
            if /self.surveyModel.surveyQues.value?.totalCount > 0 {
            let quesCount : Int = ((self.surveyModel.surveyQues.value?.totalCount ?? 0) - 1)
            self.selectedQues = quesCount
            self.tableView.reloadData()
            }
        })
    }
    
    //Submit Survey answers
    func submitQusestionsSurvey(){
        surveyModel.submitUserSurvey { (isSaved) in
            if isSaved {
                UtilityFunctions.showSingleButton(alert: "", message: "Survey submitted sucessfully. You earned \(/self.surveyModel.pointsEarned) points for completing this survey.", buttonOk: {
                    self.questionsSubmitted?(true)
                                   self.popVC()
                }, viewController: self, buttonText: "Ok")
            }
        }
    }
}
