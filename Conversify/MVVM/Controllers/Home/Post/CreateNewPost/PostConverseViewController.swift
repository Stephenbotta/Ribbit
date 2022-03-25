//
//  PostConverseViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 15/01/19.
//

import UIKit

class PostConverseViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var btnLocation: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnCreate: UIButton!
    @IBOutlet weak var btnChangeExpiryTime: UIButton!
    @IBOutlet weak var btnChangeSelectedTime: UIButton!
    @IBOutlet weak var labelExpiryTime: UILabel!
    @IBOutlet weak var labelSelectedTime: UILabel!
    
    //MARK::- PROPERTIES
    var converseVM = ConverseNearByViewModal()
    
    //MARK::- VIEW CYCLE
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    
    //MARK::- BINDINGS
    
    override func bindings() {
        if /converseVM.type == 1{
            self.btnCreate.isEnabled = true
            self.btnCreate.alpha = 1.0
        }
        converseVM.gotTime.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                }
            })<bag
        
        
        converseVM.createdSuccessFully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.popToHome()
                }
            })<bag
        
        converseVM.resetButton.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.btnCreate.isEnabled = true
                    self?.btnCreate.alpha = 1.0
                }
            })<bag
        
        converseVM.gotLocation.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.btnCreate.isEnabled = true
                    self?.btnCreate.alpha = 1.0
                    self?.btnLocation.setTitle( /self?.converseVM.locName.value  + " " + /self?.converseVM.locAddress.value , for: .normal)
                }
            })<bag
        
        
        btnCreate.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.btnCreate.isEnabled = false
//            if self?.converseVM.assets.value.count == 0 || self?.converseVM.assets.value.count == 2 || self?.converseVM.assets.value.count == 3 || self?.converseVM.assets.value.count == 4 {
//                
//            }else{
//                UtilityFunctions.makeToast(text: "Selected media count should be greater that 1 and less than 5 ", type: .error)
//                return
//            }
            self?.converseVM.post()
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
        btnLocation.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.converseVM.getPlaceDetails()
        })<bag
        
        
        
        labelExpiryTime.addTapGesture { [weak self] (gesture) in
            self?.converseVM.datePickerTapped(isExpire: true , { [weak self] (status) in
                if status{
                    if /self?.converseVM.expireTimeStr.value != ""{
                        self?.labelExpiryTime.text = self?.converseVM.expireTimeStr.value
                    }
                }
            })
        }
        
        btnChangeExpiryTime.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.converseVM.datePickerTapped(isExpire: true , { [weak self] (status) in
                if status{
                    if /self?.converseVM.expireTimeStr.value != ""{
                        self?.labelExpiryTime.text = self?.converseVM.expireTimeStr.value
                    }
                }
            })
        })<bag
        
        labelSelectedTime.addTapGesture { [weak self] (gesture) in
            
            self?.converseVM.datePickerTapped(isExpire: false , { [weak self] (status) in
                if status{
                    if /self?.converseVM.meetingStr.value != ""{
                        self?.labelSelectedTime.text = self?.converseVM.meetingStr.value
                    }
                }
            })
        }
        
        btnChangeSelectedTime.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.converseVM.datePickerTapped(isExpire: false , { [weak self] (status) in
                if status{
                    if /self?.converseVM.meetingStr.value != ""{
                        self?.labelSelectedTime.text = self?.converseVM.meetingStr.value
                    }
                }
            })
        })<bag
        
        
    }
    
    
}


