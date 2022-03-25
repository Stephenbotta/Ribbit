//
//  SubmitVenueDetailsViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit
import IBAnimatable
import IQKeyboardManagerSwift
import GooglePlacePicker
import EZSwiftExtensions
import DBAttachmentPickerController

class SubmitVenueDetailsViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var viewBorderVenueName: AnimatableView!
    @IBOutlet weak var viewBorderTags: AnimatableView!
    @IBOutlet weak var btnSelectImage: AnimatableButton!
    @IBOutlet weak var imageVenue: UIImageView!
    @IBOutlet weak var textFieldVenueName: UITextField!
    @IBOutlet weak var textFieldTags: UITextField!
    @IBOutlet weak var btnVenueLocation: UIButton!
    @IBOutlet weak var btnSelectDate: UIButton!
    @IBOutlet weak var btnUploadDoc: AnimatableButton!
    @IBOutlet weak var textFieldName: AnimatableTextField!
    @IBOutlet weak var btnCreate: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnPrivate: UIButton!
    @IBOutlet weak var labelGroupType: UILabel!
    @IBOutlet weak var imageUploaded: UIImageView!
    @IBOutlet weak var btnAddParticipant: UIButton!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var indicatorUpload: UIActivityIndicatorView!
    @IBOutlet weak var labelMemberCount: UILabel!
    @IBOutlet weak var viewBorderName: AnimatableView!
    
    //MARK::- PROPERTIES
    var submitVenueDetailsVM = SubmitVenueDetailsViewModal()
    var mediaPickerVC: MediaPickerController?
    var mediaPicker: DBAttachmentPickerController?
    var selectedDocumentUrl : URL?
    var isDocument = false
    
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        onAppear()
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        (textFieldName.rx.text <-> submitVenueDetailsVM.userName)<bag
        (textFieldVenueName.rx.text <-> submitVenueDetailsVM.venueTitle)<bag
        (textFieldTags.rx.text <-> submitVenueDetailsVM.tags)<bag
        
        submitVenueDetailsVM.selectedUsers
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupParticipantTableViewCell.identifier, cellType: GroupParticipantTableViewCell.self)) { (row,element,cell) in
                cell.user = element
            }<bag
        
        tableView.estimatedRowHeight = 60.0
        tableView.rowHeight = 48
        
        btnSelectDate.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.submitVenueDetailsVM.datePickerTapped({ (status) in
                if status{
                    if /self?.submitVenueDetailsVM.dateStr.value != ""{
                        self?.btnSelectDate.setTitleColor(#colorLiteral(red: 0.2901960784, green: 0.337254902, blue: 0.4235294118, alpha: 1), for: .normal)
                    }
                    self?.btnSelectDate.setTitle( self?.submitVenueDetailsVM.dateStr.value , for: .normal)
                }
            })
        })<bag
        
        btnCreate.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.OnSubmit()
        })<bag
        
        submitVenueDetailsVM.isValid.subscribe { (valid) in
            self.btnCreate.isEnabled = /valid.element
            self.btnCreate.alpha = /valid.element ? 1.0 : 0.4
            }<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
        btnPrivate.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.btnPrivate.isSelected = /self?.btnPrivate.isSelected.toggle()
            self?.submitVenueDetailsVM.access.value = /self?.btnPrivate.isSelected ? "1" : "2"
        })<bag
        
        btnUploadDoc.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.configurePickr()
        })<bag
        
        btnVenueLocation.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.submitVenueDetailsVM.getPlaceDetails()
        })<bag
        
        let changePhotoSignal = btnSelectImage.rx.tap
        changePhotoSignal.asDriver().drive(onNext: { [weak self] () in
            self?.openMediaPicker()
        })<bag
        
        btnAddParticipant.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            guard let vc = R.storyboard.people.addParticipantViewController() else { return }
            vc.isNew = true
            vc.participantVM.particpantsIds = { [weak self] users in
                self?.submitVenueDetailsVM.selectedUsers.value = users
                self?.tableView.reloadData()
                self?.labelMemberCount.text = "Members · " + /self?.submitVenueDetailsVM.selectedUsers.value.count.toString
            }
            self?.presentVC(vc)
        })<bag
        
    }
    
    
    func OnSubmit(){
        submitVenueDetailsVM.access.value = /btnPrivate.isSelected ? "1" : "2"
        submitVenueDetailsVM.venueImage.value =  imageVenue?.image
        validateAllDetails()
    }
    
    
    func validateAllDetails(){
        let value = Validation.shared.isValidVenueDetails(title: /textFieldVenueName.text, location: btnVenueLocation.titleLabel?.text, tags: textFieldTags.text, dateTime: btnSelectDate.titleLabel?.text)
        switch value {
        case .success:
            submitVenueDetailsVM.submitVenue { (status) in
                UtilityFunctions.makeToast(text: "Venue is created successfully", type: .success)
                print(status)
                for controller in self.navigationController?.viewControllers ?? []{
                    if controller is OnboardTabViewController{
                        (controller as? OnboardTabViewController)?.updateVenue()
                        self.navigationController?.popToViewController(controller, animated: true)
                        break
                    }
                }
            }
        case .failure:
            break
        }
    }
}


extension SubmitVenueDetailsViewController {
    
    func onLoad(){
        submitVenueDetailsVM.dateMilli.value = Date().millisecondsSince1970
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, h:mm a"
        btnSelectDate.setTitle(formatter.string(from: Date()), for: .normal)
        labelGroupType.text = submitVenueDetailsVM.category.value?.category
    }
    
    func onAppear(){
        if /submitVenueDetailsVM.venueLocName.value != ""{
            btnVenueLocation.setTitleColor(#colorLiteral(red: 0.3609120548, green: 0.414216429, blue: 0.4991951585, alpha: 1), for: .normal)
            btnVenueLocation.setTitle(/submitVenueDetailsVM.venueLocName.value, for: .normal)
        }
    }
    
}


//MARK: - Media Picker Delegate
extension SubmitVenueDetailsViewController: MediaPickerControllerDelegate {
    
    /** Pick photo **/
    func openMediaPicker() {
        self.btnUploadDoc.isEnabled = true
        indicatorUpload.stopAnimating()
        mediaPickerVC = MediaPickerController(type: .imageOnly, presentingViewController: self)
        mediaPickerVC?.delegate = self
        mediaPickerVC?.show()
    }
    
    func mediaPickerControllerDidPickImage(_ image: UIImage) {
        submitVenueDetailsVM.venueImage.value = image
        imageVenue.image = image
        if isDocument {
            imageUploaded.isHidden = false
            selectedDocumentUrl = nil
            submitVenueDetailsVM.documentUrl.value = nil
            submitVenueDetailsVM.documentImage.value = image
        }
        
    }
    
    //DBATTACHMENT PICKER
    func configurePickr(){
        
        self.btnUploadDoc.isEnabled = false
        indicatorUpload.startAnimating()
        openCustomPhotosPicker()
    }
    
    func openCustomPhotosPicker(){
        view.endEditing(true)
        btnUploadDoc?.isEnabled = true
        indicatorUpload?.stopAnimating()
        ez.runThisAfterDelay(seconds: 2.0, after: {
            UtilityFunctions.show(nativeActionSheet: "Select option", subTitle: "", vc: self, senders: ["Select photo", "Select document"], success: { [unowned self] (value, index) in
                switch index{
                case 0:
                    self.view.endEditing(true)
                    self.isDocument = true
                    self.openMediaPicker()
                default:
                    self.mediaPicker?.mediaType = [ .other]
                    self.openDBAttachmentMediaPicker()
                }
            })
        })
    }
    
    func openDBAttachmentMediaPicker(){
        mediaPicker?.allowsMultipleSelection = false
        mediaPicker?.allowsSelectionFromOtherApps = true
        ez.runThisInMainThread { [ unowned self] in
            self.btnUploadDoc.isEnabled = true
            self.indicatorUpload.stopAnimating()
            guard let vc = UIApplication.topViewController() else { return }
            self.mediaPicker?.present(on: vc.self)
        }
    }
    
    func mediaPicked(mediaArray: [DBAttachment]) {
        
        guard let type = mediaArray.first?.mediaType else { return }
        switch type {
        case .other:
            imageUploaded.isHidden = false
            let url = URL(fileURLWithPath: /(mediaArray.first?.originalFileResource() as? String))
            
            let documentsDirectoryURL = try! FileManager().url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            
            let fileURL = documentsDirectoryURL.appendingPathComponent("doc_\(arc4random()).pdf")
            do {
                try FileManager.default.copyItem(at: URL(fileURLWithPath: url.path), to: fileURL)
            }
            catch {
                print(error)
            }
            self.submitVenueDetailsVM.documentImage.value = nil
            self.selectedDocumentUrl = fileURL
            self.submitVenueDetailsVM.documentUrl.value = fileURL
            
        case .image:
            imageUploaded.isHidden = false
            mediaArray.first?.loadOriginalImage(completion: { (img) in
                self.selectedDocumentUrl = nil
                self.submitVenueDetailsVM.documentUrl.value = nil
                self.submitVenueDetailsVM.documentImage.value = img
            })
        //              mediaArray.first?.
        default :
            break
        }
    }
}
