//
//  GiftCardDetailViewController.swift
//  Conversify
//
//  Created by Sagar Kumar on 09/02/21.
//

import UIKit

class GiftCardDetailViewController: UIViewController, UITextFieldDelegate {
    
    var profileVM = ProfileDetailViewModal()
    var totalusd : Double?
    var brand: Brand?
    var surveyInfo = SurveyViewModel()
    override func viewDidLoad() {
        super.viewDidLoad()
        pointTextField.delegate = self
        amountTextField.delegate = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        profileVM.getUserStats {_ in
            let pointEarned = self.profileVM.stats.value?.pointEarned ?? 0.0
            let amount = Double(self.profileVM.stats.value?.pointEarned ?? 0.0)/Double(125)
            self.amountLabel.text = "You have \(pointEarned.toString) points and you can buy gift card upto \(amount.toString) USD"
            self.amountTextField.placeholder = "Enter amount upto \(amount.toString) USD"
            self.pointTextField.placeholder = "Enter Point upto \(pointEarned.toString) Points"
        }
    }
    
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
    
    
    @IBOutlet weak var brandImageView: UIImageView! {
        didSet {
            if let images = brand?.imageUrls?.values {
                brandImageView.image(url: "\(/Array(images).last)")
            }
        }
    }
    @IBOutlet weak var brandNameLabel: UILabel! {
        didSet {
            brandNameLabel.text = brand?.brandName
        }
    }
    @IBOutlet weak var priceLabel: UILabel! {
        didSet {
            let item = brand?.items?.first
            priceLabel.text = "\(/item?.minValue) - \(/item?.maxValue) \(/item?.currencyCode)"
        }
    }
    @IBOutlet weak var descLabel: UILabel! {
        didSet {
            guard let htmlData = brand?.description?.data(using: String.Encoding.unicode) else { return }
            do {
                let attributedText = try NSAttributedString(data: htmlData,
                                                            options: [.documentType: NSAttributedString.DocumentType.html], documentAttributes: nil)
                descLabel.attributedText = attributedText
            } catch let e as NSError {
                print("Couldn't translate \(/brand?.description): \(e.localizedDescription) ")
            }
        }
    }
    
    @IBOutlet weak var selectAmountView: UIView! {
        didSet {
            selectAmountView.isHidden = true
        }
    }
    
    
    @IBOutlet weak var amountLabel: UILabel! {
        didSet {
//           let a =  self.profileVM.stats.value?.pointRedeemed?.toString
//            print(a)
//            let userData = /Singleton.sharedInstance.loggedInUser
//            let pointEarned = userData.pointEarned
//            let amount = Double(pointEarned ?? 0.0)/Double(125)
//            amountLabel.text = "You have \(pointEarned?.toString) points and you can buy gift card upto \(amount.toString) USD"
        }
    }
    
    @IBOutlet weak var pointTextField: UITextField!{
        didSet{
            
        }
    }
    
    
    @IBOutlet weak var amountTextField: UITextField! {
        didSet {
           
//            let userData = /Singleton.sharedInstance.loggedInUser
//            let pointEarned = userData.pointEarned
//            let amount = Double(pointEarned ?? 0)/Double(125)
//            amountTextField.placeholder = "Enter amount upto \(amount.toString) USD"
        }
    }
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var doneButton: UIButton!


    @IBAction func backButtonAction(_ sender: Any) {
        popVC()
    }
    
    @IBAction func redeem(_ sender: Any) {
        selectAmountView.isHidden = false
    }
    
    @IBAction func cancel(_ sender: Any) {
        selectAmountView.isHidden = true
    }
    
    @IBAction func done(_ sender: Any) {
//        guard !(amountTextField.text ?? "").isEmpty  else {
//            UtilityFunctions.makeToast(text: "Please enter amount", type: .error)
//            return
//        }
        if((amountTextField.text ?? "").isEmpty && (pointTextField.text ?? "").isEmpty){
            UtilityFunctions.makeToast(text: "Please enter amount ", type: .error)
            return
        }
        if amountTextField.text == ""{
            let a = Double(/pointTextField.text) ?? 0.0
            totalusd = a/125.0
        }else{
            totalusd = Double(/amountTextField.text)
        }
        
        if /totalusd  < 25.0 {
            UtilityFunctions.makeToast(text: "Amount should be greater than or equal to 25", type: .error)
            return
        }
        
        surveyInfo.tangoPostOrders(faceValue: String(totalusd ?? 0.0), utid: brand?.items?.first?.utid) {
            
            isSuccess in
            if isSuccess {
                
                self.popVC()
            }
        }
    }
}
