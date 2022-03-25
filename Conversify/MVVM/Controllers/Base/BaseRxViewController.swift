//
//  BaseRxViewController.swift
//  VipCart
//
//  Created by OSX on 02/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//

import UIKit
import RxSwift
import RxCocoa
import IBAnimatable
import FBSDKLoginKit
import GoogleSignIn
import GooglePlacePicker
import EZSwiftExtensions

class BaseTableViewCell: UITableViewCell {
    
    
    let bag = DisposeBag()
    var apiBarrier = false
    
    override func awakeFromNib() {
        super.awakeFromNib()
        bindings()
        self.selectionStyle = .none
    }
    
    func bindings(){
        
        
    }
    
}


class BaseCollectionViewCell: UICollectionViewCell {
    
    let bag = DisposeBag()
    
    override func awakeFromNib() {
        super.awakeFromNib()
        bindings()
    }
    
    func bindings(){
        
        
    }
    
    func color(_ rgbColor: Int) -> UIColor{
        return UIColor(
            red:   CGFloat((rgbColor & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbColor & 0x00FF00) >> 8 ) / 255.0,
            blue:  CGFloat((rgbColor & 0x0000FF) >> 0 ) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
    
    
}

typealias RefreshCalled = () -> ()
typealias PickedLocation = (String , String , String , String) -> ()

class BaseRxViewController: UIViewController , GMSPlacePickerViewControllerDelegate  {
    
    @IBOutlet var indicator: UIActivityIndicatorView!
    @IBOutlet var indicatorG: UIActivityIndicatorView!
    
    let bag = DisposeBag()
    var refreshControl   = UIRefreshControl()
    var apiBarrier = false
    var user =  User()
    var pickedLocation: PickedLocation?
    var refreshCalled: RefreshCalled?
    
    
    var testing = Variable<String?>(nil)
    var isLoginSignp = true
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        bindings()
        refreshControl.addTarget(self, action: #selector(refresh), for: .valueChanged)
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.setNeedsStatusBarAppearanceUpdate()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    func bindings(){
        
        
    }
    
    
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        if isLoginSignp{
            self.view.endEditing(true)
        }
        
    }
    
    @objc func refresh(_ sender: Any) {
        DispatchQueue.main.async { [weak self] in
            self?.refreshControl.endRefreshing()
        }
        refreshCalled?()
    }
    
    //MARK::- FACEBOOK
    func facebookLogin(isLogin: Bool , completion : @escaping (User?) -> ()){
        
        self.view.isUserInteractionEnabled =  false
        indicator?.startAnimating()
        LoginManager().logOut()
        FacebookManager.shared.configureLoginManager(sender: self, success: { [weak self] (fb) in
            self?.user.email = /fb.email
            self?.user.facebookId = /fb.fbId
            self?.user.googleId = ""
            self?.user.originalImage = /fb.imageUrl
            self?.user.firstName = /fb.firstName
            self?.user.lastName = /fb.lastName
            self?.indicator?.stopAnimating()
            self?.view.isUserInteractionEnabled =  true
            completion(self?.user)
            
        }) { [unowned self] (error) in
            self.view.isUserInteractionEnabled =  true
            self.indicator?.stopAnimating()
        }
    }
    
    //MARK::- MOVE TO HOME SCREEN
    func moveToHome(){
        guard let delegate = UIApplication.shared.delegate as? AppDelegate else{return}
        let navigationControl = delegate.window?.rootViewController as? NavigationController
        navigationControl?.viewControllers = []
        guard let vc = R.storyboard.home.onboardTabViewController() else { return }
        let navigationController = NavigationController(rootViewController : vc)
        navigationController.isNavigationBarHidden = true
        UIView.transition(with: delegate.window!, duration: 0.5, options: .transitionCrossDissolve , animations: { () -> Void in
            delegate.window?.rootViewController = navigationController
        }) { (completed) -> Void in
        }
    }
    
    //MARK::- COUNTRY CODE BY LOCALE
    func getCurrentCountryCode() -> [String : String] {
        let region = Locale.current.regionCode
        
        do {
            if let file = Bundle.main.url(forResource: "countryCodes", withExtension: "json") {
                let data = try Data(contentsOf: file)
                let json = try JSONSerialization.jsonObject(with: data, options: [])
                if let object = json as? [String: String] {
                    
                } else if let object = json as? [[String : String]] {
                    var selectedCountry = [String : String]()
                    object.forEach { (element) in
                        if element["code"] == region{
                            
                            selectedCountry = element
                        }
                    }
                    return selectedCountry
                } else {
                    print("JSON is invalid")
                }
            } else {
                print("no file")
            }
        } catch {
            print(error.localizedDescription)
        }
        return [:]
    }
    
    
    //MARK::- PLACE PICKER
    func getPlaceDetails(){
        let config = GMSPlacePickerConfig(viewport: nil)
        let placePicker = GMSPlacePickerViewController(config: config)
        placePicker.delegate = self
        
        UIApplication.topViewController()?.present(placePicker, animated: true, completion: nil)
        
    }
    
    func placePicker(_ viewController: GMSPlacePickerViewController, didPick place: GMSPlace) {
        // Dismiss the place picker, as it cannot dismiss itself.
        
        viewController.dismiss(animated: true, completion: nil)
        pickedLocation?(place.coordinate.latitude.toString , place.coordinate.longitude.toString , /place.name , /place.formattedAddress )
    }
    
    func placePickerDidCancel(_ viewController: GMSPlacePickerViewController) {
        // Dismiss the place picker, as it cannot dismiss itself.
        pickedLocation?("" , "" , "" , "" )
        viewController.dismiss(animated: true, completion: nil)
        print("No place selected")
    }
    
    //MARK::- SHARE LOCATION
    
    func shareLocation(){
        ez.runThisAfterDelay(seconds: 5.0) {
            
            //            LocationManager.sharedInstance.
        }
    }
    
    //MARK::- DEVICE TOKEN
    
    //    func updateDeviceToken(){
    //        if Singleton.sharedInstance.loggedInUser?.token != nil &&  /Singleton.sharedInstance.loggedInUserDeviceToken != ""{
    //            LoginTarget.updateDeviceToken().request(apiBarrier: false).asObservable()
    //                .subscribeOn(MainScheduler.instance)
    //                .subscribe(onNext: { [weak self] (response) in
    //                    guard let safeResponse = response as? DictionaryResponse<User> else { return }
    //                    Singleton.sharedInstance.loggedInUser = safeResponse.data
    //                    }, onError: { (error) in
    //                        print(error)
    //                })<bag
    //        }
    //    }
    
    func popToHome(){
        for controller in (self.navigationController?.viewControllers) ?? [UIViewController()] {
            if controller is  OnboardTabViewController {
                (controller as? OnboardTabViewController)?.updatePostList()
                self.navigationController?.popToViewController(controller, animated: true)
                break
            }
        }
    }
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
}

class BaseRxViewModel: NSObject {
    
    let bag = DisposeBag()
    
    func isValidInformation(info: String) -> Bool {
        if info.length <= 0 { return false }
        if info.isBlank { return false }
        return true
    }
    
    func handleError(error: ResponseStatus) {
        switch error {
        case .clientError(let message):
            UtilityFunctions.makeToast(text: message, type: .error)
        case .noInternet:
            UtilityFunctions.makeToast(text: AlertMessages.noInternet.value(), type: .error)
        default: return
        }
    }
    
    func isValidTag(tagV: String?) -> Bool {
        let tagArray = /tagV?.hashtagss()
        if /tagArray.count == 0{
            return false
        }
        return true
    }
    
}



