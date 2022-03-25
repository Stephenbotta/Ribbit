//
//  VenueGroupChatDetailViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 19/11/18.
//

import UIKit

class VenueGroupChatDetailViewController: BaseRxViewController {
    
    //MARK: - Outlets
    @IBOutlet weak var labelDate: UILabel!
    @IBOutlet weak var imgGroup: UIImageView!
    @IBOutlet weak var viewNavigationBar: UIView!
    @IBOutlet weak var lblGroupTitle: UILabel!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView.dataSource = self
            tableView.delegate = self
        }
    }
    @IBOutlet weak var textFieldVenueName: UITextField!
    @IBOutlet var headerLocation: UIView!
    @IBOutlet var headerMembers: UIView!
    @IBOutlet weak var btnNotification: UIButton!
    @IBOutlet weak var btnEdit: UIButton!
    @IBOutlet weak var btnExitGroup: UIButton!
    @IBOutlet weak var lblMembers: UILabel!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnMore: UIButton!
    @IBOutlet weak var btnShare: UIButton!
    @IBOutlet weak var btnArchiveGroup: UIButton!
    
    
    //MARK::- PROPERTIES
    var chatVM = ChatViewModal()
    
    //MARK: - View Hierarchy
    override func viewDidLoad() {
        super.viewDidLoad()
        setupView()
    }
    
    func setupView() {
        if let view = tableView.tableHeaderView {
            var frame = view.frame
            frame.size.height = UIScreen.main.bounds.width + 50 + 60
            view.frame = frame
            tableView.tableHeaderView = view
        }
        tableView.reloadData()
        imgGroup?.image(url:  /chatVM.groupChatData.value?.grpImg?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /chatVM.groupChatData.value?.grpImg?.original))
        
        
        lblGroupTitle?.text = /chatVM.groupChatData.value?.venueTitle?.uppercaseFirst
        btnNotification?.isSelected = /chatVM.groupChatData.value?.notification
        btnEdit?.isHidden = !(chatVM.venue.value?.adminId == Singleton.sharedInstance.loggedInUser?.id)
        btnExitGroup.setTitle((chatVM.venue.value?.adminId == Singleton.sharedInstance.loggedInUser?.id) ? "Delete Venue" : "Exit Venue", for: .normal)
        labelDate?.text = /Date(milliseconds: Double(/chatVM.venue.value?.venueTime)).toString(format: "MMM d, yyyy  h:mm a")
        
    }
    
    override func bindings() {
        (textFieldVenueName.rx.text <-> chatVM.venueName)<bag
        
        chatVM.updateNameSuccessfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.lblGroupTitle?.text = self?.chatVM.venue.value?.venueTitle?.uppercaseFirst
                }
            })<bag
        
        chatVM.exitSuccesfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.navigationController?.viewControllers.forEach({ (controller) in
                        if controller is OnboardTabViewController{
                            self?.chatVM.exit?()
                            (controller as? OnboardTabViewController)?.updateVenue()
                            self?.navigationController?.popToViewController(controller, animated: true)
                            return
                        }
                    })
                }
            })<bag
        
        chatVM.notificationOn.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if !bool {
                    self?.btnNotification.isSelected = /self?.chatVM.groupChatData.value?.notification
                }else{
                    self?.chatVM.groupChatData.value?.notification = /self?.btnNotification.isSelected
                }
            })<bag
        
        btnShare.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            UtilityFunctions.share(mssage: APIConstants.shareTextVenue, url: nil, image: nil)
        })<bag
        
        btnNotification.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.btnNotification.isSelected = /self?.btnNotification.isSelected.toggle()
            self?.chatVM.notificationSettings(action: /self?.btnNotification.isSelected ? "true" :"false")
        })<bag
        
        btnMore.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            UtilityFunctions.show(nativeActionSheet: "Select option", subTitle: "", vc: self, senders: ["Invite to Venue"], success: { (value, index) in
                switch index{
                case 0://invite
                    let contactPickerScene = EPContactsPicker(delegate: self, multiSelection:true, subtitleCellType: SubtitleCellValue.email)
                    contactPickerScene.multiSelectEnabled = true
                    let navigationController = UINavigationController(rootViewController: contactPickerScene)
                    self?.present(navigationController, animated: true, completion: nil)
                default:
                    break
                }
            })
        })<bag
        
        btnExitGroup.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.exitGroup()
        })<bag
        
        btnArchiveGroup.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.archiveVenue()
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.chatVM.updateVenueDetails?(self?.chatVM.venue.value)
            self?.popVC()
        })<bag
        
        btnEdit.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.btnEdit.isSelected = /self?.btnEdit.isSelected.toggle()
            self?.lblGroupTitle?.isHidden = /self?.lblGroupTitle?.isHidden.toggle()
            self?.textFieldVenueName?.isHidden = /self?.textFieldVenueName?.isHidden.toggle()
            if /self?.textFieldVenueName.text != ""{
                self?.chatVM.addEditGroupName()
                self?.textFieldVenueName.text = ""
            }
        })<bag
        
    }
    
    func exitGroup(){
        
        UtilityFunctions.show(alert: "Are you sure you want to " + (/(chatVM.venue.value?.adminId == Singleton.sharedInstance.loggedInUser?.id) ? "delete" : "exit") + " this venue?", message: "", buttonOk: { [weak self] in
            self?.chatVM.exitGroup()
            }, viewController: self, buttonText: "Yes")
    }
    
    func archiveVenue(){
        
        UtilityFunctions.show(alert: "Are you sure you want to archive this venue?", message: "", buttonOk: { [weak self] in
            self?.chatVM.archiveVenue()
            }, viewController: self, buttonText: "Yes")
    }
    
    
}


extension VenueGroupChatDetailViewController: UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return section == 0 ? 1 : /chatVM.groupChatData.value?.groupMembers?.count
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.section == 0{
            "".moveToMap(lat: /(chatVM.venue.value?.latLong?.last as? Double) , long: /(chatVM.venue.value?.latLong?.first as? Double))
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        switch indexPath.section {
            
        case 0:
            guard let cell = tableView.dequeueReusableCell(withIdentifier: String(describing: GroupLocationTableViewCell.self), for: indexPath) as? GroupLocationTableViewCell else { return UITableViewCell() }
            cell.delegate = self
            cell.lblLocationName?.text =  chatVM.groupChatData.value?.venueLocationName
            let lat = chatVM.groupChatData.value?.venueLoc?.last as? Double
            let lng = chatVM.groupChatData.value?.venueLoc?.first as? Double
            cell.lat = lat?.toString
            cell.long = lng?.toString
            cell.lblLocationAddress?.text =  chatVM.groupChatData.value?.venueLocationAddress
            cell.labelNumberOfMembers?.text = "Members - " + (/chatVM.groupChatData.value?.groupMembers?.count).toString
            return cell
            
        default:
            
            guard let cell = tableView.dequeueReusableCell(withIdentifier: String(describing: GroupMemberTableViewCell.self), for: indexPath) as? GroupMemberTableViewCell else { return UITableViewCell() }
            cell.members = chatVM.groupChatData.value?.groupMembers?[indexPath.row]
            return cell
        }
    }
}

extension VenueGroupChatDetailViewController: UITableViewDelegate , DelegateAddParticpant {
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableView.automaticDimension
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return section == 0 ? 32 : 0
        
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        switch section {
        case 0:
            return headerLocation
        default:
            return UIView()
        }
    }
    
    func addParticipant(){
        view.endEditing(true)
        guard let vc = R.storyboard.people.addParticipantViewController() else { return }
        vc.isNew = false
        vc.participantVM = AddParticipantViewModal(group: nil , venue: /chatVM.venue.value?.groupId)
        vc.participantVM.particpantsIds = { [weak self] users in
            var memebers = [Members]()
            let userIds = users.map({ (user) -> String in
                return /user.id
            })
            if users.count != 0 && users != nil{
                self?.chatVM.addMoreParticipants(particpants: userIds.toJson() , groupId: "", venueId: /self?.chatVM.venue.value?.groupId)
            }
           
            self?.tableView.reloadData()
        }
        self.presentVC(vc)
    }
    
}

//MARK: EPContactsPicker delegates
extension VenueGroupChatDetailViewController : EPPickerDelegate{
    
    func epContactPicker(_: EPContactsPicker, didContactFetchFailed error : NSError){
        print("Failed with error \(error.description)")
    }
    
    func epContactPicker(_: EPContactsPicker, didSelectContact contact : EPContact){
        print("Contact \(contact.displayName()) has been selected")
    }
    
    func epContactPicker(_: EPContactsPicker, didCancel error : NSError){
        print("User canceled the selection");
    }
    
    func epContactPicker(_ picker: EPContactsPicker, didSelectMultipleContacts contacts: [EPContact]) {
        print("The following contacts are selected")
        for contact in contacts {
            print("\(contact.displayName())")
        }
        
        var contacNum = [String]()
        var emails = [String]()
        if contacts.count != 0{
            contacts.forEach { (contac) in
                contac.emails.forEach({ (mailID,lbl) in
                    emails.append(/mailID)
                })
                contac.phoneNumbers.forEach({ (phn,lbl) in
                    contacNum.append(/phn)
                })
            }
        }
        
        if emails.count > 0 || contacNum.count > 0{
            chatVM.inviteContacts(phone: contacNum.toJson(), groupId: /chatVM.venue.value?.groupId, email: emails.toJson(), isGroup: false)
        }
        
        picker.dismissVC(completion: nil)
        
    }
}
