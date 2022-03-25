//
//  EditProfileViewController.swift
//  Conversify
//
//  Created by Apple on 07/12/18.
//

import UIKit
import IBAnimatable
import RxDataSources

class EditProfileViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnProfile: UIButton!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView.registerXIBForHeaderFooter(R.nib.settingTableHeaderView.name)
        }
    }
    @IBOutlet weak var btnChangeProfile: UIButton!
    @IBOutlet weak var imgProfileView: AnimatableImageView!
    @IBOutlet weak var btnSave: UIButton!
    @IBOutlet weak var btnImge: UIButton!
    
    //MARK::- PROPERTIES
    var dataSource:RxTableViewSectionedReloadDataSource<EditProfileElements>?
    var editVM = EditProfileViewModel()
    var createProVM = CreateProfileViewModal()
    var mediaPickerVC:MediaPickerController?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        
    }
    
    override func bindings() {
        
        editVM.getEditProfileData()
        
        imgProfileView.image(url:  /Singleton.sharedInstance.loggedInUser?.img?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder")) //kf.setImage(with: URL(string: /Singleton.sharedInstance.loggedInUser?.img?.original))
        
        editVM.profilePic.value = imgProfileView.image
        
        btnImge.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.openMediaPicker()
        })<bag
        
        btnProfile.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popVC()
        })<bag
        
        btnSave.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.getProfileViewData()
        })<bag
        
        dataSource = RxTableViewSectionedReloadDataSource<EditProfileElements>(configureCell: { (_, tableView, indexPath, element) -> UITableViewCell in
            guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.editProfileCell.identifier, for: indexPath) as? EditProfileCell else { return UITableViewCell() }
            cell.textViewBio.isHidden = !(indexPath.section == 0 && indexPath.row == 3)
            cell.txtfEntryField.isHidden = (indexPath.section == 0 && indexPath.row == 3)
            cell.textFieldBgView.isHidden = (indexPath.section == 0 && indexPath.row == 3)
            cell.textViewBgView.isHidden = !(indexPath.section == 0 && indexPath.row == 3)
            cell.indexPath = indexPath
            cell.delegate = self
            cell.item = element
            return cell
        })
        
        guard let safeDatasource = dataSource else {return}
        editVM.arrayEditProfile.asObservable().bind(to: tableView.rx.items(dataSource: safeDatasource))<bag
        tableView.rx.setDelegate(self)<bag
    }
}


//MARK::- TABLEVIEW DATASOURCE AND DELEGATES
extension EditProfileViewController : UITableViewDelegate ,  DelegateUpdateUser{
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        
        return 48.0
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if indexPath.section == 0 && indexPath.row == 3{
            return 160
        }else{
            return 88
        }
        
    }
    
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: R.nib.settingTableHeaderView.name){
            (header as? SettingTableHeaderView)?.labelTitle.text = editVM.arrayEditProfile.value[section].head
            return header
        }else {
            return nil
        }
    }
    
    func updateUser(val: String? , row: Int , sec: Int){
        editVM.arrayEditProfile.value[sec].items[row].text = val
    }
    
    func validateUserName(userName :  String?) {
        let searchTxt = userName
        if /searchTxt?.count >= 6 {
            if searchTxt == Singleton.sharedInstance.loggedInUser?.userName {
                guard let cell = self.tableView.cellForRow(at: IndexPath(row: 1, section: 0)) as? EditProfileCell else { return }
                btnSave.isEnabled = true
                btnSave.alpha = 1.0
                cell.imgVerify.image =  R.image.ic_verify()
                return
            }
            checkUserName(str: searchTxt)
        }else{
            btnSave.isEnabled = false
            btnSave.alpha = 0.4
            guard let cell = self.tableView.cellForRow(at: IndexPath(row: 1, section: 0)) as? EditProfileCell else { return }
            cell.imgVerify.image =  R.image.ic_unverify()
            
        }
        
    }
}


//MARK::- CUSTOM METHODS
extension EditProfileViewController {
    
    func getProfileViewData(){
        let name = editVM.arrayEditProfile.value[0].items[0].text
        let userName = editVM.arrayEditProfile.value[0].items[1].text
        let website = editVM.arrayEditProfile.value[0].items[2].text
        
        if /website != ""{
            if !(/website?.verifyUrl()){
                UtilityFunctions.makeToast(text: "Please enter a valid website url", type: .error)
                return
            }
        }
        
        let bio = editVM.arrayEditProfile.value[0].items[3].text
        let designation = editVM.arrayEditProfile.value[1].items[0].text
        let company = editVM.arrayEditProfile.value[1].items[1].text
        let email = editVM.arrayEditProfile.value[2].items[0].text
        let phone = editVM.arrayEditProfile.value[2].items[1].text
        let gender = editVM.arrayEditProfile.value[2].items[2].text
        
        editVM.userName.value = userName
        editVM.fullName.value = name
        editVM.email.value = email
        editVM.phoneNumber.value = phone
        
        editVM.saveEditProfileData(userName: /userName, fullName: /name, bio: /bio, website: /website, email: /email, gender: gender?.uppercased(), dob: "", designation: /designation, company: /company) { [weak self](_) in
            self?.editVM.backRefresh?()
            self?.view.endEditing(true)
            self?.popVC()
        }
        
    }
    
    //MARK::- USERNAME VALIDATION
    func checkUserName(str : String?){
        if !(createProVM.isValidUserName(name: /str)){
            return
        }
        createProVM.checkIsUserNameAvailable(name: /str) { [weak self] (status, isNameAvailable) in
            if status {
                
                //
                
                if !(/self?.createProVM.phoneNumber.value?.isEmpty) && isNameAvailable && /self?.createProVM.isValidValues(){
                    self?.btnSave.isEnabled = isNameAvailable
                    self?.btnSave.alpha = isNameAvailable ? 1.0 : 0.4
                }else{
                    if /self?.createProVM.phoneNumber.value != ""  && /self?.createProVM.isValidValues() && isNameAvailable{
                        self?.btnSave.isEnabled = true
                        self?.btnSave.alpha = 1
                    }
                }
                
                guard let cell = self?.tableView.cellForRow(at: IndexPath(row: 1, section: 0)) as? EditProfileCell else { return }
                let txt = /cell.txtfEntryField.text
                cell.imgVerify.image = (isNameAvailable && /self?.createProVM.isValidUserName(name: /txt) && txt.count >= 6) ? R.image.ic_verify() : R.image.ic_unverify()
                
                if (isNameAvailable && /self?.createProVM.isValidUserName(name: /txt) && txt.count >= 6){
                    self?.btnSave.isEnabled = true
                    self?.btnSave.alpha = 1
                }
            }
        }
    }
}

extension EditProfileViewController : MediaPickerControllerDelegate {
    
    /** Pick photo **/
    func openMediaPicker() {
        mediaPickerVC = MediaPickerController(type: .imageOnly, presentingViewController: self)
        mediaPickerVC?.delegate = self
        mediaPickerVC?.show()
    }
    
    func mediaPickerControllerDidPickImage(_ image: UIImage) {
        imgProfileView.image = image
        editVM.selectedImage.value = image
    }
    
}

