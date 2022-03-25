//
//  SelectInterestsViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 10/10/18.
//

import UIKit
import RxCocoa
import RxSwift
import IBAnimatable
import ContactsUI

typealias SelectedInterest = (Interests) -> ()

class SelectInterestsViewController: BaseRxViewController {
    
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var labelHeader: AnimatableLabel!
    @IBOutlet weak var labelHeader2: AnimatableLabel!
    @IBOutlet weak var btnContinue: AnimatableButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var collectionView: UICollectionView!
    
    //MARK::- PROPERTIES
    var interestsVM = SelectInterestsViewModal()
    var selectedInterest: SelectedInterest?
    var contactStore = CNContactStore()
    var contacts = [String]()
    
    
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        labelHeader.text = interestsVM.isFromFilter ? "Select category" : "Tell us about your interests"
        if /interestsVM.isFromEditProfile{
            btnBack.isHidden = false
        }else{
            btnBack.isHidden = !(/interestsVM.isFromFilter || /interestsVM.isFromHome)
        }
        labelHeader2.text = (interestsVM.isFromFilter || interestsVM.isFromHome) ? "" : "Choose atleast 3 interests"
        btnContinue.setTitle((interestsVM.isFromFilter || interestsVM.isFromHome) ? "Select" : "Continue", for: .normal)
        let interests = interestsVM.interests.value.filter({$0.isSelected == true})
        if /interestsVM.isFromHome{
            btnContinue.isEnabled = /interests.count > 0
            btnContinue.alpha = /interests.count > 0 ? 1 : 0.5
        }else{
            btnContinue.isEnabled = /interests.count > 2
            btnContinue.alpha = /interests.count > 2 ? 1 : 0.5
        }
        interestsVM.retrieveInterests { (status) in
            if status{
                
            }
        }
    }
    
    //MARK::- BINDINGD
    
    override func bindings() {
        interestsVM.interests
            .asObservable()
            .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.selectInterestCollectionViewCell.identifier, cellType: SelectInterestCollectionViewCell.self)) { [weak self] (row,element,cell) in
                cell.interest = element
                if /self?.interestsVM.isFromFilter{
                    cell.selectedIndex = self?.interestsVM.selectedInterestIndex == row
                }
            }<bag
        
        collectionView.rx.setDelegate(self)<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            (/self?.interestsVM.isFromEditProfile || /self?.interestsVM.isFromHome) ? self?.dismissVC(completion: nil):  self?.popVC()
        })<bag
        
        btnContinue.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            if /self?.interestsVM.isFromHome{
                self?.interestsVM.selectedIntersts()
                self?.dismiss(animated: true, completion: nil)
            }
            else if /self?.interestsVM.isFromFilter{
                self?.interestsVM.selectedInterst()
                self?.dismiss(animated: true, completion: nil)
            }else{
                self?.interestsVM.updateInterests({ [weak self] (status) in
                    if status{
                        if /self?.interestsVM.isFromEditProfile {
                            self?.interestsVM.selectedIntersts()
                            self?.dismiss(animated: true, completion: nil)
                        } else {
                            self?.syncContact()
                        }
                    }
                })
            }
        })<bag
        
    }
}

extension SelectInterestsViewController:CNContactPickerDelegate{
    
    func syncContact(){
        let alert = UIAlertController(title: "Refer", message: "Refer to your friends and get 200 Points free every time they sign up with your refer Code", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "INVITE", style: .default, handler: {_ in
            self.requestForAccess(completionHandler: { status in
                if status {
                    self.onClickPickContact()
                }else{
                    self.moveToHome()
                }
            })
        }))
        
        alert.addAction(UIAlertAction(title: "CANCEL", style: .cancel, handler: {_ in
            self.moveToHome()
        }))

        self.present(alert, animated: true)
    }
    
    func onClickPickContact(){
        DispatchQueue.main.async {
            let contactPicker = CNContactPickerViewController()
            contactPicker.delegate = self
            contactPicker.displayedPropertyKeys =
                [CNContactGivenNameKey
                    , CNContactPhoneNumbersKey]
            
            self.present(contactPicker, animated: true, completion: nil)
        }
        }
    
    fileprivate func requestForAccess(completionHandler: @escaping (_ accessGranted: Bool) -> Void) {
        let authorizationStatus = CNContactStore.authorizationStatus(for: CNEntityType.contacts)
        switch authorizationStatus {
        case .authorized:
            completionHandler(true)
        case .notDetermined:
            self.contactStore.requestAccess(for: CNEntityType.contacts, completionHandler: { (access, accessError) -> Void in
                if access {
                    completionHandler(access)
                }
                else {
                    completionHandler(false)
                }
            })
        default:
            completionHandler(false)
        }
    }

    func contactPicker(_ picker: CNContactPickerViewController, didSelect contacts: [CNContact]) {
        self.contacts = []
        for item in contacts{
            for phone in item.phoneNumbers {
                self.contacts.append(phone.value.stringValue)
            }                      
        }
        if self.contacts.count > 0 {
        interestsVM.updateContact(contact: self.contacts, { status in
            self.moveToHome()
        })
        }else{
            moveToHome()
        }
    }
    
    func contactPicker(_ picker: CNContactPickerViewController, didSelect contact: CNContact){
        
    }
}


//MARK: - Collection View Delegates
extension SelectInterestsViewController: UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if interestsVM.isFromFilter{
            self.interestsVM.selectedInterestIndex = indexPath.row
        }else{
            //  if self.selectedIndex == indexPath.row{return}
            interestsVM.interests.value[indexPath.row].isSelected = interestsVM.interests.value[indexPath.row].isSelected?.toggle()
        }
        let interests = interestsVM.interests.value.filter({$0.isSelected == true})
        if /interestsVM.isFromHome{
            btnContinue.isEnabled = /interests.count > 0
            btnContinue.alpha = /interests.count > 0 ? 1 : 0.5
        }else{
            btnContinue.isEnabled = /interests.count > 2
            btnContinue.alpha = /interests.count > 2 ? 1 : 0.5
        }
        collectionView.reloadData()
        
    }
    
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: ((self.collectionView.bounds.width - 20 ) / 2 ) , height: self.collectionView.bounds.height/3.5)
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 16
    }
    
}
