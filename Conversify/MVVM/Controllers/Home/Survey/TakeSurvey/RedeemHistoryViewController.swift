//
//  RedeemHistoryViewController.swift
//  Conversify
//
//  Created by Sagar Kumar on 08/02/21.
//

import UIKit

class RedeemHistoryViewController: UIViewController , UITextFieldDelegate{
    enum ScreenType {
        case redeemHistory
        case wantToDonate
    }
    var profileVM = ProfileDetailViewModal()
    var totalusd : Double?
    var surveyInfo = SurveyViewModel()
    var screenType = ScreenType.redeemHistory
    @IBOutlet weak var tableView: UITableView! {
        didSet {
            tableView.tableFooterView = UIView()
        }
    }
    @IBOutlet weak var historyButton: UIButton!
    @IBOutlet weak var donateButton: UIButton!
    @IBOutlet weak var searchBar: UISearchBar!
    @IBOutlet weak var tableTopConatrint: NSLayoutConstraint!
    @IBOutlet weak var selectAmountView: UIView! {
        didSet {
            selectAmountView.isHidden = true
        }
    }
    @IBOutlet weak var amountLabel: UILabel! {
        didSet {
//            let pointEarned = /Singleton.sharedInstance.loggedInUser?.pointEarned
//            let totalprice = Double(pointEarned)/Double(125)
//            amountLabel.text = "You Have  \(pointEarned.toString) points and you can donate upto \(totalprice.toString) USD "
        }
    }
    
    @IBAction func pointTextField(_ sender: Any) {
    }
    
    @IBOutlet weak var pointTextField: UITextField!
    
    @IBOutlet weak var amountTextField: UITextField! {
        didSet {
//            let pointEarned = /Singleton.sharedInstance.loggedInUser?.pointEarned
//            let totalprice = Double(pointEarned)/Double(125)
//            amountTextField.placeholder = "Enter Amount upto \(totalprice.toString) USD"
        }
    }
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var doneButton: UIButton!
    
   
    
    override func viewDidLoad() {
        super.viewDidLoad()
        pointTextField.delegate = self
        amountTextField.delegate = self
        if screenType == .wantToDonate {
            surveyInfo.showCharityOrgList { [weak self] isSuccess in
                guard let `self` = self else { return }
                self.tableView.reloadData()
            }
            tableTopConatrint.constant = 56
            donateButton.isHidden = false
            searchBar.isHidden = false
            historyButton.setTitle("Select Organisation", for: .normal)
        } else {
            surveyInfo.redeemHistory { [weak self] isSuccess in
                guard let `self` = self else { return }
                self.tableView.reloadData()
            }
            tableTopConatrint.constant = 0
            donateButton.isHidden = true
            searchBar.isHidden = true
            historyButton.setTitle("History", for: .normal)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        profileVM.getUserStats {_ in
            let pointEarned = self.profileVM.stats.value?.pointEarned ?? 0.0
            let amount = Double(self.profileVM.stats.value?.pointEarned ?? 0.0)/Double(125)
            self.amountLabel.text = "You Have  \(pointEarned.toString) points and you can donate upto \(amount.toString) USD "
            self.amountTextField.placeholder = "Enter Amount upto \(amount.toString) USD"
            self.pointTextField.placeholder = "Enter Point upto \(pointEarned.toString) Points"
        }
    }
    @IBAction func cross(_ sender: Any) {
        view.endEditing(true)
        self.dismissVC(completion: nil)
    }
    
    @IBAction func donate(_ sender: Any) {
        view.endEditing(true)
        selectAmountView.isHidden = false

    }
    
    @IBAction func cancel(_ sender: Any) {
        selectAmountView.isHidden = true
    }
    
    @IBAction func done(_ sender: Any) {
//        guard !(amountTextField.text ?? "").isEmpty else {
//            UtilityFunctions.makeToast(text: "Please enter Amount", type: .error)
//            return
//        }
        if((amountTextField.text ?? "").isEmpty && (pointTextField.text ?? "").isEmpty){
            UtilityFunctions.makeToast(text: "Please enter amount ", type: .error)
            return
        }
        if amountTextField.text == ""{
           // totalusd = Double(/pointTextField.text) ?? 0.0/Double(125)
            let a = Double(/pointTextField.text) ?? 0.0
            totalusd = a/125.0
        }else{
            totalusd = Double(/amountTextField.text)
        }
        
        if /totalusd  < 1.0 {
            UtilityFunctions.makeToast(text: "Amount should be greater than or equal to 1", type: .error)
            return
        }
        
        surveyInfo.addCharityDonation(organizationId: surveyInfo.organizaton.value?.filter({/$0.isSelected}).first?._id, givenPoint: String(totalusd ?? 0.0)) { (isSuccess) in
            if isSuccess {
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "PeformAfterPresenting"), object: nil)
                self.dismissVC(completion: nil)
                
            }
        }
    }
}

extension RedeemHistoryViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return screenType == .wantToDonate ? /surveyInfo.organizaton.value?.count : /surveyInfo.redeemHistory.value?.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if screenType == .wantToDonate {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.organisationTableViewCell.identifier, for: indexPath) as? OrganisationTableViewCell else { return UITableViewCell()}
            let org = surveyInfo.organizaton.value?[indexPath.row]
            cell.orgNameLabel.text = org?.organizationName
            cell.checkButton.isSelected = /org?.isSelected
            return cell
        } else {
            guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.redeemHistoryTableViewCell.identifier, for: indexPath) as? RedeemHistoryTableViewCell else { return UITableViewCell()}
            cell.redeem = surveyInfo.redeemHistory.value?[indexPath.row]
            return cell
        }
    }
}

extension RedeemHistoryViewController: UITableViewDelegate {
    func textFieldDidBeginEditing(_ textField: UITextField) {
        if (textField == pointTextField){
            amountTextField.text = ""
        }
        if( textField == amountTextField ){
            pointTextField.text = ""
        }
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        if (textField == pointTextField){
            if let text = textField.text as NSString?{
            let txtAfterUpdate = text.replacingCharacters(in: range, with: string)
            let a = Double(/txtAfterUpdate) ?? 0.0
            amountTextField.text = (a/125.0).toString
            }
        }else if textField == amountTextField{
            let newString = (textField.text! as NSString).replacingCharacters(in: range, with: string)
            let decimalRegex = try! NSRegularExpression(pattern: "^\\d*\\.?\\d{0,2}$", options: [])
            let matches = decimalRegex.matches(in: newString, options: [], range: NSMakeRange(0, newString.count))
            if matches.count == 1
                {
                  return true
                }
                return false
        }
        return true
    }
}

extension RedeemHistoryViewController{
   
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if screenType == .wantToDonate {
            _ = surveyInfo.organizaton.value?.map({$0.isSelected = false})
            let org = surveyInfo.organizaton.value?[indexPath.row]
            org?.isSelected = !(/org?.isSelected)
            tableView.reloadData()
        }
    }
}
