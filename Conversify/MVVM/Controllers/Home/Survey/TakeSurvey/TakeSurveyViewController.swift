//
//  TakeSurveyViewController.swift
//  Conversify
//
//  Created by Apple on 06/12/19.
//

import UIKit
import Sheeeeeeeeet

class TakeSurveyViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnAcceptTerms: UIButton!
    @IBOutlet var btnSurveyTxt: [UIButton]!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnSubmit: UIButton!
    
    //MARK::- PROPERTIES
    var surveyInfo = SurveyViewModel()
    var profileModel = ProfileDetailViewModal()
    var dobMillisec : String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setInitialValues()
    }
    
    override func bindings() {
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popVC()
        })<bag
        btnSubmit.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            if /self?.checkIfValid(){
                if !(/self?.btnAcceptTerms.isSelected) {
                    UtilityFunctions.makeToast(text: "Please aceept terms and conditions!", type: .error)
                    return
                }
                self?.submitSurveyProperties()
            }
        })<bag
        btnAcceptTerms.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.btnAcceptTerms.isSelected = !(/self?.btnAcceptTerms.isSelected)
        })<bag
    }
    
    //MARK::- Button Actions
    @IBAction func btnSurveyFieldActions(_ sender: UIButton) {
        if let type = SurveyType.init(rawValue: sender.tag){
            if type == .DOB {
                openDatePicker()
                return
            }
            let actionSheet = self.showStandardActionSheet(type: type)
            actionSheet.present(in: self, from: sender)
        }
    }
}

extension TakeSurveyViewController {
    
    //MARK::- Get Survey Properties
    func setInitialValues(){
        surveyInfo.getSurveyValues {[weak self](result) in
            if /Singleton.sharedInstance.loggedInUser?.isTakeSurvey && result {
                
                self?.btnSurveyTxt.forEachEnumerated { (indx, btn) in
                    
                    let type = SurveyType(rawValue: indx)
                    var selectedText : String = ""
                    switch type {
                    case .DOB :
                        self?.dobMillisec = /self?.surveyInfo.surveyBasicInfo.value?.dateOfBirth?.toString
                        let dOB = Date.init(milliseconds: /self?.surveyInfo.surveyBasicInfo.value?.dateOfBirth)
                        selectedText = dOB.toString(DateFormat.custom("MMM d yyyy"))
                    case .Gender :
                        selectedText = /self?.surveyInfo.surveyBasicInfo.value?.gender?.first(where: {$0.isSelected == 1})?.keyName
                    case .Race :
                        selectedText = /self?.surveyInfo.surveyBasicInfo.value?.race?.first(where: {$0.isSelected == 1})?.keyName
                    case .HomeOwnership :
                        selectedText = /self?.surveyInfo.surveyBasicInfo.value?.homeOwnership?.first(where: {$0.isSelected == 1})?.keyName
                    case .HouseHoldIncome :
                        selectedText = /self?.surveyInfo.surveyBasicInfo.value?.houseHoldIncome?.first(where: {$0.isSelected == 1})?.keyName
                    case .Education :
                        selectedText = /self?.surveyInfo.surveyBasicInfo.value?.education?.first(where: {$0.isSelected == 1})?.keyName
                    case .EmploymentStatus :
                        selectedText = /self?.surveyInfo.surveyBasicInfo.value?.employementStatus?.first(where: {$0.isSelected == 1})?.keyName
                    case .MaritalStatus :
                        selectedText = /self?.surveyInfo.surveyBasicInfo.value?.maritalStatus?.first(where: {$0.isSelected == 1})?.keyName
                    default : break
                    }
                    btn.setTitle(/selectedText, for: .normal)
                }
            }
        }
    }
    
    //Set Properties Value
    func showStandardActionSheet(type : SurveyType) -> ActionSheet {
        
        var arrList  = [SurveyValues()]
        switch type {
        case .Gender :
            arrList = surveyInfo.surveyBasicInfo.value?.gender ?? [SurveyValues()]
        case .Race :
            arrList =  surveyInfo.surveyBasicInfo.value?.race ?? [SurveyValues()]
        case .HomeOwnership :
            arrList =  surveyInfo.surveyBasicInfo.value?.homeOwnership ?? [SurveyValues()]
        case .HouseHoldIncome :
            arrList =  surveyInfo.surveyBasicInfo.value?.houseHoldIncome ?? [SurveyValues()]
        case .Education :
            arrList =  surveyInfo.surveyBasicInfo.value?.education ?? [SurveyValues()]
        case .EmploymentStatus :
            arrList =  surveyInfo.surveyBasicInfo.value?.employementStatus ?? [SurveyValues()]
        case .MaritalStatus :
            arrList =  surveyInfo.surveyBasicInfo.value?.maritalStatus ?? [SurveyValues()]
        default : break
        }
        
        var actionSheetArr = [MenuItem]()
        arrList.forEachEnumerated({ (indx, value) in
            let item = MenuItem.init(title: /value.keyName)
            actionSheetArr.append(item)
        })
        return ActionSheet(menu: Menu(title: "", items: actionSheetArr)) { [weak self] sheet, item in
            self?.view.endEditing(true)
            if let value = item.title as? String {
                let indx = type.rawValue
                self?.btnSurveyTxt[indx].setTitle(value, for: .normal)
            }
        }
    }
    
    //MARK::- Submit Properties
    func submitSurveyProperties(){
        surveyInfo.submitSurveyProperties(gender: btnSurveyTxt?[0].currentTitle, race: btnSurveyTxt?[1].currentTitle, dateOfBirth: dobMillisec, houseHoldIncome: btnSurveyTxt?[3].currentTitle, homeOwnership: btnSurveyTxt?[4].currentTitle, education: btnSurveyTxt?[5].currentTitle, employementStatus: btnSurveyTxt?[6].currentTitle, maritalStatus: btnSurveyTxt?[7].currentTitle) { [weak self] (result) in
            if result {
                let user = Singleton.sharedInstance.loggedInUser
                user?.isTakeSurvey = true
                Singleton.sharedInstance.loggedInUser = user
                guard let vc = R.storyboard.survey.surveyListingViewController() else { return }
                self?.pushVC(vc)
            }
        }
    }
    
    //MARK::- Validate Fields
    func checkIfValid() -> Bool {
        
        var type = SurveyType(rawValue: 0)
        var isAllFieldValid : Bool = false
        for btn in btnSurveyTxt {
            let btnTitle = btn.currentTitle ?? "Select"
            if btnTitle == "Select" {
                isAllFieldValid = false
                type = SurveyType(rawValue: btn.tag)
                switch type {
                case .Gender :
                    UtilityFunctions.makeToast(text: "Please select gender", type: .error)
                case .Race :
                    UtilityFunctions.makeToast(text: "Please select race", type: .error)
                case .HomeOwnership :
                    UtilityFunctions.makeToast(text: "Please select house ownership", type: .error)
                case .HouseHoldIncome :
                    UtilityFunctions.makeToast(text: "Please select house hold income", type: .error)
                case .Education :
                    UtilityFunctions.makeToast(text: "Please select education", type: .error)
                case .EmploymentStatus :
                    UtilityFunctions.makeToast(text: "Please select employment status", type: .error)
                case .MaritalStatus :
                    UtilityFunctions.makeToast(text: "Please select marital status", type: .error)
                case .DOB :
                    UtilityFunctions.makeToast(text: "Please select date of birth", type: .error)
                default : break
                }
                return isAllFieldValid
            }
        }
        return true
    }
    
    //MARK::- Date Picker
    func openDatePicker(){
        
        let calendar = Calendar(identifier: .gregorian)
        let currentDate = Date()
        var components = DateComponents()
        components.calendar = calendar
        components.year = -10
        
        let maxDate = calendar.date(byAdding: components, to: currentDate)!
        
        let datePicker = DatePickerDialog(textColor: .black,
                                          buttonColor: .black,
                                          font: UIFont.systemFont(ofSize: 14, weight: .medium),
                                          showCancelButton: true)
        
        datePicker.show("", doneButtonTitle: "Done",cancelButtonTitle: "Cancel", minimumDate: Date(), maximumDate: maxDate,  datePickerMode: .date) { [weak self] (date) in
            if let dt = date {
                let dob = dt.toString(DateFormat.custom("MMM d yyyy"))
                self?.dobMillisec = dt.millisecondsSince1970
                self?.btnSurveyTxt[2].setTitle(dob, for: .normal)
            }
        }
    }
}
