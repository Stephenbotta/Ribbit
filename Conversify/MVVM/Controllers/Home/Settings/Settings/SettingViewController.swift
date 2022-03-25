//
//  SettingViewController.swift
//  Conversify
//
//  Created by Apple on 04/12/18.
//

import UIKit
import IBAnimatable
import RxDataSources
import EmailPicker
import MessageUI

class SettingViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var labelAlertSettings: UILabel!
    @IBOutlet weak var labelPushSettings: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var btnPushNotification: UIButton!
    @IBOutlet weak var labelPrivacy: UILabel!
    @IBOutlet weak var labelVerification: UILabel!
    @IBOutlet weak var btnBack: AnimatableButton!
    @IBOutlet weak var labelLogout: UILabel!
    @IBOutlet weak var labelInvitePeople: UILabel!
    @IBOutlet weak var labelBlockUser: UILabel!
    @IBOutlet weak var labelShareContactDetails: UILabel!
    @IBOutlet weak var labelAccessLocation: UILabel!
    @IBOutlet weak var labelContactUs: UILabel!
    @IBOutlet weak var labelTerms: UILabel!
    @IBOutlet weak var btnAlertNotification: UIButton!
    
    //MARK::- PROPERTIES
    
    var settingVM = SettingViewModel()
    
    //MARK::- VIEW CYCLE
    override func viewWillAppear(_ animated: Bool) {
        settingVM.gatherContactDetails()
        self.btnAlertNotification.isSelected = /Singleton.sharedInstance.loggedInUser?.isAlert
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        
        settingVM.alertUpdated.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
//                    let user = Singleton.sharedInstance.loggedInUser
//                    let alert : Bool = /user?.isAlert
//                    user?.isAlert = !alert
//                    Singleton.sharedInstance.loggedInUser = user
                }else{
                    self?.btnAlertNotification.isSelected = /Singleton.sharedInstance.loggedInUser?.isAlert
                }
            })<bag
        
        labelAccessLocation.addTapGesture { (gesture) in
            CheckPermission.shared.openAppSettings()
        }
        
        labelPushSettings.addTapGesture { (gesture) in
            CheckPermission.shared.openAppSettings()
        }
        
        labelAlertSettings.addTapGesture { (gesture) in
            UtilityFunctions.show(alert: "", message: "Do you want to get alerts for Look neaby posts?", buttonOk: { [ weak self] in
                self?.btnAlertNotification.isSelected = true
                //                if !(/Singleton.sharedInstance.loggedInUser?.isAlert){
                self?.settingVM.handleAlert(action: true)
                //                }
                }, buttonCancel: { [ weak self] in
                    self?.btnAlertNotification.isSelected = false
                    //                    if (/Singleton.sharedInstance.loggedInUser?.isAlert){
                    self?.settingVM.handleAlert(action: false)
                    //                    }
                }, viewController: self, buttonText: "Yes", cancelButtonText: "No")
        }
        
        labelTerms.addTapGesture { [weak self] (gesture) in
            guard let vc = R.storyboard.settings.termsAndConditionsViewController() else { return }
            vc.header = "Terms and Conditions"
            vc.webUrls = APIConstants.terms
            self?.pushVC(vc)
        }
        
        labelContactUs.addTapGesture { [weak self] (gesture) in
            guard let vc = R.storyboard.settings.contactUsViewController() else { return }
            self?.pushVC(vc)
            //guard let vc = R.storyboard.settings.termsAndConditionsViewController()
            /*guard let vc = R.storyboard.settings.termsAndConditionsViewController() else { return }
            vc.header = "Contact us"
            vc.webUrls = APIConstants.contactus
            self?.pushVC(vc)*/
        }
        
        labelShareContactDetails.addTapGesture { [weak self] (gesture) in
            UtilityFunctions.share(mssage: /self?.settingVM.contactDetails , url: nil, image: nil)
        }
        
        labelBlockUser.addTapGesture { [weak self] (gesture) in
            guard let vc = R.storyboard.settings.blockUsersViewController() else { return }
            vc.followerVM.type = 0
            self?.pushVC(vc)
        }
        
        labelVerification.addTapGesture { [weak self] (gesture) in
            guard let vc = R.storyboard.settings.verificationViewController() else { return }
            self?.pushVC(vc)
        }
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
        labelLogout.addTapGesture { [weak self] (gesture) in
            self?.logoutConfirmation()
        }
        
        labelPrivacy.addTapGesture { [weak self] (gesture) in
            guard let vc = R.storyboard.settings.privacyViewController() else { return }
            self?.pushVC(vc)
        }
        
        labelInvitePeople.addTapGesture { [weak self] (gesture) in
            UtilityFunctions.show(nativeActionSheet: "", subTitle: "", vc: self, senders: ["Mail","Message","More"], success: { [weak self] (element, index) in
                switch index{
                case 0:
                    self?.openEmailPicker()
                case 1:
                    guard let slf = self else { return }
                    let contactPickerScene = EPContactsPicker(delegate: slf, multiSelection:true, subtitleCellType: SubtitleCellValue.email)
                    contactPickerScene.multiSelectEnabled = true
                    let navigationController = UINavigationController(rootViewController: contactPickerScene)
                    self?.present(navigationController, animated: true, completion: nil)
                //                    UIApplication.shared.open(URL(string: "sms:")!, options: [:], completionHandler: nil)
                default:
                    let user = Singleton.sharedInstance.loggedInUser
                    UtilityFunctions.share(mssage: "\(/user?.userName) sent you an invitation to join Ribbit app.", url: nil, image: nil)
                }
            })
            
        }
    }
    
    //MARK::- FUNCTIONS
    
    func openEmailPicker(){
        let textToShow = "To share this app, please type their emails or select their names from the list. Enjoy!"
        let picker = EmailPickerViewController(infoText: textToShow, doneButtonTitle: "Send", completion: {(result) in
            switch result {
            case .cancelled(let vc):
                vc.dismiss(animated: true) {
                    
                }
                
            case .selected(let vc, let emails):
                vc.dismiss(animated: true) { [ weak self ] in
                    print(emails)
                    self?.sendEmail(emaild:emails)
                }
            }
        })
        picker.view.tintColor = .red
        present(UINavigationController(rootViewController: picker), animated: true, completion: nil)
    }
    
    func logoutConfirmation(){
        UtilityFunctions.show(alert: "Are you sure you want to logout?", message: "", buttonOk: { [weak self] in
            self?.settingVM.logout({ (status) in
                if status{
                    UIApplication.shared.logoutActions()
                }
            })
            }, viewController: self, buttonText: "Yes")
    }
    
    func sendEmail(emaild:[String]) {
        let user = Singleton.sharedInstance.loggedInUser
        if MFMailComposeViewController.canSendMail() {
            let mail = MFMailComposeViewController()
            mail.mailComposeDelegate = self
            settingVM.selectedEmails = emaild
            mail.setToRecipients(emaild)
            mail.setMessageBody("<p>\(/user?.userName) sent you an invitation to join Ribbit app</p>", isHTML: true)
            //UIApplication.topViewController()?.presentVC(mail)
            self.present(mail, animated: true, completion: nil)
        } else {
            UtilityFunctions.makeToast(text: "This device is not configured to send email. Please set up an email account.", type: .error)
        }
    }
    
}


extension SettingViewController : MFMailComposeViewControllerDelegate{
    
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        
      switch result.rawValue {
        case MFMailComposeResult.sent.rawValue:
           controller.dismiss(animated: true)
           settingVM.invitePpl(emails: settingVM.selectedEmails, phonNum: [])
      default:
           controller.dismiss(animated: true)
        }
        
        
    }
    
    
    
}

//MARK: EPContactsPicker delegates
extension SettingViewController : EPPickerDelegate , MFMessageComposeViewControllerDelegate{
    
    func epContactPicker(_: EPContactsPicker, didContactFetchFailed error : NSError){
        print("Failed with error \(error.description)")
    }
    
    func epContactPicker(_: EPContactsPicker, didSelectContact contact : EPContact){
        print("Contact \(contact.displayName()) has been selected")
    }
    
    func epContactPicker(_: EPContactsPicker, didCancel error : NSError){
        print("User canceled the selection");
    }
    
    func epContactPicker(_: EPContactsPicker, didSelectMultipleContacts contacts: [EPContact]) {
        print("The following contacts are selected")
        
        var contacNum = [String]()
        if contacts.count != 0{
            contacts.forEach { (contac) in
                contac.phoneNumbers.forEach({ (phn,lbl) in
                    contacNum.append(/phn)
                })
            }
        }
        if contacNum.count != 0{
            sendMessage(phnNumbers: contacNum)
            settingVM.invitePpl(emails: [], phonNum: contacNum)
        }
        
    }
    
    func sendMessage(phnNumbers: [String]){
        let user = Singleton.sharedInstance.loggedInUser
        if (MFMessageComposeViewController.canSendText()) {
            let controller = MFMessageComposeViewController()
            controller.body = "\(/user?.userName) sent you an invitation to join Ribbit app." //"To share this app, please type their emails or select their names from the list. Enjoy!"
            controller.recipients = phnNumbers
            controller.messageComposeDelegate = self
            self.present(controller, animated: true, completion: nil)
        }else{
            print("Error")
        }
        
    }
    
    func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult){
        print("invited successfully")
        controller.dismissVC(completion: nil)
    }
}
