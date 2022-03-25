//
//  AddGroupViewController.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 13/11/18.
//

import UIKit

class AddGroupViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var textViewDesc: UITextView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnCreate: UIButton!
    @IBOutlet weak var btnPrivate: UIButton!
    @IBOutlet weak var textFieldGroupName: UITextField!
    @IBOutlet weak var imageGroup: UIImageView!
    @IBOutlet weak var btnAddImage: UIButton!
    @IBOutlet weak var labelCategoryName: UILabel!
    @IBOutlet weak var btnAddParticipant: UIButton!
    @IBOutlet weak var labelMemeberCount: UILabel!
    @IBOutlet weak var viewBorderTextField: UIView!
    
    //MARK::- VARIABLES
    
    var addGroupVM = AddGroupViewModal()
    var mediaPickerVC:MediaPickerController?
    
    //MARK::- VIEW CYCLE
    
    override func viewDidLoad() {
        super.viewDidLoad()
        textFieldGroupName.delegate = self
        textViewDesc.delegate = self
        labelCategoryName.text = /addGroupVM.category.value?.category?.uppercaseFirst
    }
    
    
    override func bindings() {
        (textViewDesc.rx.text  <-> addGroupVM.groupDesc)<bag
        (textFieldGroupName.rx.text <-> addGroupVM.groupTitle)<bag
        
        addGroupVM.selectedUsers
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupParticipantTableViewCell.identifier, cellType: GroupParticipantTableViewCell.self)) { (row,element,cell) in
                cell.user = element
            }<bag
        
        tableView.estimatedRowHeight = 60.0
        tableView.rowHeight = 48
        
        addGroupVM.isValid.subscribe { (valid) in
            self.btnCreate.isEnabled = /valid.element
            self.btnCreate.alpha = /valid.element ? 1.0 : 0.4
            }<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popVC()
        })<bag
        
        btnAddParticipant.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            guard let vc = R.storyboard.people.addParticipantViewController() else { return }
            vc.isNew = true
            vc.participantVM = AddParticipantViewModal(group: self?.addGroupVM.group.value?.id , venue: nil)
//            vc.participantVM.participantsList.value = self?.addGroupVM.selectedUsers.value ?? []
            vc.participantVM.particpantsIds = { [weak self] users in
                self?.addGroupVM.selectedUsers.value = users
                self?.tableView.reloadData()
                self?.labelMemeberCount.text = "Members · " + /self?.addGroupVM.selectedUsers.value.count.toString
            }
            self?.presentVC(vc)
        })<bag
        
        btnPrivate.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.btnPrivate.isSelected = /self?.btnPrivate.isSelected.toggle()
            self?.addGroupVM.access.value = /self?.btnPrivate.isSelected ? "1" : "2"
        })<bag
        
        let changePhotoSignal = btnAddImage.rx.tap
        changePhotoSignal.asDriver().drive(onNext: { [weak self] () in
            self?.openMediaPicker()
        })<bag
        
        
        btnCreate.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.OnSubmit()
        })<bag
        
    }
    
    
    func OnSubmit(){
        addGroupVM.access.value = /btnPrivate.isSelected ? "1" : "2"
        addGroupVM.groupImage.value =  imageGroup?.image
        addGroupVM.submitGroup { (status) in
            if status{
                UtilityFunctions.makeToast(text: "Network is created successfully", type: .success)
                print(status)
                for controller in self.navigationController?.viewControllers ?? []{
                    if controller is OnboardTabViewController{
                        (controller as? OnboardTabViewController)?.updateGroup()
                        self.navigationController?.popToViewController(controller, animated: true)
                        break
                    }
                }
            }
        }
    }
    
}

//MARK: - Media Picker Delegate
extension AddGroupViewController: MediaPickerControllerDelegate {
    
    /** Pick photo **/
    func openMediaPicker() {
        mediaPickerVC = MediaPickerController(type: .imageOnly, presentingViewController: self)
        mediaPickerVC?.delegate = self
        mediaPickerVC?.show()
    }
    
    func mediaPickerControllerDidPickImage(_ image: UIImage) {
        addGroupVM.groupImage.value = image
        imageGroup.image = image
        //        self.submitVenueDetailsVM.imageChanged.value = true
        //        btnSelectImage.setImage(image, for: .normal)
    }
    
    
}

extension AddGroupViewController : UITextFieldDelegate , UITextViewDelegate {
    
    func textFieldDidBeginEditing(_ textField: UITextField){
        if textField == textFieldGroupName{
            viewBorderTextField.layer.borderColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
        }else{
            viewBorderTextField.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textFieldDidEndEditing(_ textField: UITextField){
        if textField == textFieldGroupName{
            viewBorderTextField.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            viewBorderTextField.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textViewDidBeginEditing(_ textView: UITextView) {
        if textView == textViewDesc{
            textViewDesc.borderColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
        }else{
             textViewDesc.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textViewDidEndEditing(_ textView: UITextView) {
        if textView == textViewDesc{
            textViewDesc.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            textViewDesc.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
}
