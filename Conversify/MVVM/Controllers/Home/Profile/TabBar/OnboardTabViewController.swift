//
//  OnboardTabViewController.swift
//  Conversify
//
//  Created by Himanshu Upadhyay on 14/10/18.
//

import UIKit
import RxSwift
import RxCocoa

class OnboardTabViewController: UITabBarController {
    
    let bag = DisposeBag()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        retrieveVenueNotificationCount()
        updateDeviceToken()
        receiveCountFromServer()
        LocationManager.sharedInstance.startTrackingUser()
        LocationManager.sharedInstance.startSharingLocation()
        SocketIOManager.shared.addHandlers()
        // Do any additional setup after loading the view.
    }
    
    func updateDeviceToken(){
        if Singleton.sharedInstance.loggedInUser?.token != nil &&  /Singleton.sharedInstance.loggedInUserDeviceToken != ""{
            LoginTarget.updateDeviceToken().request(apiBarrier: false).asObservable()
                .subscribeOn(MainScheduler.instance)
                .subscribe(onNext: { [weak self] (response) in
                    guard let safeResponse = response as? DictionaryResponse<User> else { return }
                    Singleton.sharedInstance.loggedInUser = safeResponse.data
                    }, onError: { (error) in
                        print(error)
                })<bag
        }
    }
    
    func updateVenue(){
        guard let vc = self.viewControllers?[3] as? SocialInteractionViewController else { return }
        vc.venueVc?.venueListViewController?.retrieve()
        vc.venueVc?.mapViewController?.retrieveVenue()
    }
    
    func updatePostList(){
        guard let vc = self.viewControllers?[0] as? HomePostListViewController else { return }
        vc.onLoad()
    }
    
    func updateGroupChat(){
        guard let vc = self.viewControllers?[1] as? ChatListingViewController else { return }
        vc.getMessagesList()
        
    }
    
    func updateGroup(){
        guard let vc = self.viewControllers?[3] as? SocialInteractionViewController else { return }
//        vc.goupVc?.groupVM.retrieveGroups()
    }
    
    
    func retrieveVenueNotificationCount(){
        VenueTarget.getRequestsCount().request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<RequestCount> else{
                    return
                }
                let count = /safeResponse.data?.requestCount
                self?.updateTab(badge:  count != 0 ? (count > 9 ? "9+" : count.toString) : nil)
                }, onError: { [weak self] (error) in
                    print(error)
            })<bag
    }
    
    func receiveCountFromServer(){
        SocketIOManager.shared.requestCount { [weak self] (reqCount) in
            let count = /reqCount?.requestCount
            self?.updateTab(badge:  count != 0 ? (count > 9 ? "9+" : count.toString) : nil)
        }
    }
    
    func updateTab(badge: String?){
        guard let vc = self.viewControllers?[0] as? HomePostListViewController else { return }
        vc.labelNotificationCount?.isHidden = badge == nil
        vc.labelNotificationCount?.text = /badge
//        if let tabItems = self.tabBar.items {
//            // In this case we want to modify the badge number of the third tab:
//            let tabItem = tabItems[4]
//            tabItem.badgeValue = badge
//        }
    }
    
}
