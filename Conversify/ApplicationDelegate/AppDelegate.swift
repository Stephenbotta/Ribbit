//
//  AppDelegate.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 04/10/18.
//

import UIKit
import CoreData
import GoogleSignIn
import FBSDKCoreKit
import FBSDKLoginKit
import Fabric
import Crashlytics
import GooglePlacePicker
import GooglePlaces
import IQKeyboardManagerSwift
import AWSS3
import AWSCore
import Firebase
import RxSwift
import Foundation
import RxCocoa
import UserNotifications
import EZSwiftExtensions
import FirebaseMessaging
import DropDown
import PushKit
import TwitterKit
import FirebaseInstanceID
import GoogleMobileAds

var typeC = "Reply"

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    let bag = DisposeBag()
    var bgTask: UIBackgroundTaskIdentifier?
    
    let pushRegistry = PKPushRegistry(queue: DispatchQueue.main)
    let gcmMessageIDKey = "gcm.message_id"
    var callManager = SpeakerboxCallManager()
    var providerDelegate: ProviderDelegate?
    var onGoingCall : SpeakerboxCall? = nil
    var caller: Caller?
    var senderPlayer: AVAudioPlayer?
    var receiverPlayer: AVAudioPlayer?
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        IQKeyboardManager.shared.enable = true
        DropDown.startListeningToKeyboard()
        
        GIDSignIn.sharedInstance().clientID = "112757870901-71qen8g4edk0b31q18voe8pvm21u9eeg.apps.googleusercontent.com"
        GMSPlacesClient.provideAPIKey("AIzaSyCzwlKpNV9bny5IkpO-OTWQLPF46lq3HW4")
        GMSServices.provideAPIKey("AIzaSyCzwlKpNV9bny5IkpO-OTWQLPF46lq3HW4")
        
        Fabric.with([Crashlytics.self])
        let credentialProvider = AWSCognitoCredentialsProvider(regionType: .USWest2, identityPoolId: "us-west-2:fd850246-9ff4-4fe1-bfec-d7019fb19ca0")
        
        //us-west-2:507740a2-4e72-40da-be87-a6fca04c6146
        let configuration = AWSServiceConfiguration(region: .USWest2, credentialsProvider: credentialProvider)
        AWSServiceManager.default().defaultServiceConfiguration = configuration
        FirebaseApp.configure()
       
        GADMobileAds.sharedInstance().start(completionHandler: nil)
       
        providerDelegate = ProviderDelegate(callManager: callManager)
        pushRegistry.desiredPushTypes = [.voIP]
        pushRegistry.delegate = self
        
        if #available(iOS 10.0, *) {
            // For iOS 10 display notification (sent via APNS)
            UNUserNotificationCenter.current().delegate = self
            
            let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
            UNUserNotificationCenter.current().requestAuthorization(
                options: authOptions,
                completionHandler: {_, _ in })
        } else {
            let settings: UIUserNotificationSettings =
                UIUserNotificationSettings(types: [.alert, .badge, .sound], categories: nil)
            application.registerUserNotificationSettings(settings)
        }
        application.registerForRemoteNotifications()
        Messaging.messaging().delegate = self
        
        InstanceID.instanceID().instanceID { (result, error) in
            if let error = error {
                print("Error fetching remote instance ID: \(error)")
            } else if let result = result {
                print("Remote instance ID token: \(result.token)")
                Singleton.sharedInstance.loggedInUserDeviceToken = "\(result.token)"
                self.updateDeviceToken()
            }
        }
        
        TWTRTwitter.sharedInstance().start(withConsumerKey: "kUqBZAk0DimwFz6TqeLIrQkwp", consumerSecret: "LpXgAtSkCR0LuH3I8J8IAJZVTtLJjY0RrGNphGzO2xFdGqje59")
        return true
    }
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        
        if GIDSignIn.sharedInstance().handle(url)  {
            return GIDSignIn.sharedInstance().handle(url)
        }
        
//        if GIDSignIn.sharedInstance().handle(url, sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String, annotation: options[UIApplication.OpenURLOptionsKey.annotation]) {
//
//            return GIDSignIn.sharedInstance().handle(url, sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String, annotation: options[UIApplication.OpenURLOptionsKey.annotation])     //return Google sign in
//        }
        if TWTRTwitter.sharedInstance().application(app, open: url, options: options) {
            return TWTRTwitter.sharedInstance().application(app, open: url, options: options)
        }
        return ApplicationDelegate.shared.application(app, open: url, options: options)     // return Facebook
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        bgTask  = application.beginBackgroundTask(expirationHandler: {() -> Void in
        })
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        print("enter foreground")
        application.endBackgroundTask(bgTask ?? UIBackgroundTaskIdentifier(rawValue: 0))
        bgTask = UIBackgroundTaskIdentifier.invalid
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        
        if Singleton.sharedInstance.loggedInUser?.token != nil {
            SocketIOManager.shared.addHandlers()
        }
        
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        // Saves changes in the application's managed object context before the application terminates.
        self.saveContext()
    }
    
    // MARK: - Core Data stack
    
    lazy var persistentContainer: NSPersistentContainer = {
        /*
         The persistent container for the application. This implementation
         creates and returns a container, having loaded the store for the
         application to it. This property is optional since there are legitimate
         error conditions that could cause the creation of the store to fail.
         */
        let container = NSPersistentContainer(name: "Conversify")
        container.loadPersistentStores(completionHandler: { (storeDescription, error) in
            if let error = error as NSError? {
                // Replace this implementation with code to handle the error appropriately.
                // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
                
                /*
                 Typical reasons for an error here include:
                 * The parent directory does not exist, cannot be created, or disallows writing.
                 * The persistent store is not accessible, due to permissions or data protection when the device is locked.
                 * The device is out of space.
                 * The store could not be migrated to the current model version.
                 Check the error message to determine what the actual problem was.
                 */
                fatalError("Unresolved error \(error), \(error.userInfo)")
            }
        })
        return container
    }()
    
    // MARK: - Core Data Saving support
    
    func saveContext () {
        let context = persistentContainer.viewContext
        if context.hasChanges {
            do {
                try context.save()
            } catch {
                // Replace this implementation with code to handle the error appropriately.
                // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
                let nserror = error as NSError
                fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
            }
        }
    }
    
    //    func updateDeviceToken(){
    //
    //    }
}


@available(iOS 10.0, *)
extension AppDelegate : UNUserNotificationCenterDelegate  , MessagingDelegate {
    
    //MARK: Remote Notification Methods // <= iOS 9.x
    
    func registerForRemoteNotification() {
        
    }
    
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data){
        
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error){
        print("Error = ",error.localizedDescription)
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        notificationActions(userInfo: userInfo)
        completionHandler(UIBackgroundFetchResult.newData)
    }
    
    
    // MARK: UNUserNotificationCenter Delegate // >= iOS 10
    
    @available(iOS 10.0, *)
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let usrInfo = notification.request.content.userInfo
        print(usrInfo)
        
        guard let type = usrInfo["TYPE"] as? String else { return  }
        let notifyType = AppNotificationType(rawValue: type) ?? .likeReply
        switch notifyType {
            
        case .requestFollow , .acceptRequestFollow:
            refreshNotification()
            completionHandler([UNNotificationPresentationOptions.alert,
                               UNNotificationPresentationOptions.sound,
                               UNNotificationPresentationOptions.badge])
            
        case .acceptRequestGroup , .acceptInviteGroup:
            refreshNotification()
            if UIApplication.topViewController() is OnboardTabViewController {
                (UIApplication.topViewController() as? OnboardTabViewController)?.updateGroup()
            }
            completionHandler([UNNotificationPresentationOptions.alert,
                               UNNotificationPresentationOptions.sound,
                               UNNotificationPresentationOptions.badge])
            
        case .acceptRequestVenue , .acceptInviteVenue :
            refreshNotification()
            if UIApplication.topViewController() is SocialInteractionViewController {
                (UIApplication.topViewController() as? SocialInteractionViewController)?.venueVc?.venueListViewController?.retrieve()
                (UIApplication.topViewController() as? SocialInteractionViewController)?.venueVc?.mapViewController?.onLoad()
            }
            completionHandler([UNNotificationPresentationOptions.alert,
                               UNNotificationPresentationOptions.sound,
                               UNNotificationPresentationOptions.badge])
            
        case .chat , .venueChat , .groupChat:
            
            if UIApplication.topViewController() is SocialInteractionViewController {
                (UIApplication.topViewController() as? SocialInteractionViewController)?.venueVc?.venueListViewController?.retrieve()
                (UIApplication.topViewController() as? SocialInteractionViewController)?.venueVc?.mapViewController?.onLoad()
            }
            if /Singleton.sharedInstance.isTopIsChatController {
                let conversationId = usrInfo["id"] as? String
                if /Singleton.sharedInstance.conversationId == /conversationId{
                    completionHandler([])
                }else{
                    completionHandler([UNNotificationPresentationOptions.alert,
                                       UNNotificationPresentationOptions.sound,
                                       UNNotificationPresentationOptions.badge])
                }
            }else{
                completionHandler([UNNotificationPresentationOptions.alert,
                                   UNNotificationPresentationOptions.sound,
                                   UNNotificationPresentationOptions.badge])
            }
        case .callDisconnect :
            providerDelegate?.endCurrentCall()
        default:
            refreshNotification()
            completionHandler([UNNotificationPresentationOptions.alert,
                               UNNotificationPresentationOptions.sound,
                               UNNotificationPresentationOptions.badge])
        }
        
        
    }
    
    @available(iOS 10.0, *)
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        print(response.notification.request.content.userInfo)
        notificationActions(userInfo: response.notification.request.content.userInfo)
        completionHandler()
    }
    
    //MARK::- HANDLE NOTIFICATIONS
    
    func refreshNotification(){
        (UIApplication.topViewController() as? NotificationsViewController)?.viewModel.page = 1
        (UIApplication.topViewController() as? NotificationsViewController)?.retrieveNotifications()
    }
    
    func notificationActions(userInfo: [AnyHashable : Any]){
        
        if UIApplication.shared.applicationState == .active || UIApplication.shared.applicationState == .background{
            self.handleNotification(userInfo: userInfo , userInteraction: true)
        }else if UIApplication.shared.applicationState == .inactive{
            let when = DispatchTime.now() + 2 // change 2 to desired number of seconds
            DispatchQueue.main.asyncAfter(deadline: when) { [weak self] in
                self?.handleNotification(userInfo: userInfo , userInteraction: true)
            }
            
        }
    }
    
    func handleNotification(userInfo: [AnyHashable : Any] , userInteraction:Bool){
        print(userInfo)
        
        if Singleton.sharedInstance.loggedInUser == nil{
            return
        }
        guard let type = userInfo["TYPE"] as? String else { return  }
        let notifyType = AppNotificationType(rawValue: type) ?? .likeReply
        
        switch notifyType {
            
        case .callDisconnect :
            providerDelegate?.endCurrentCall()
            
        case .chat:
            let conversationId = userInfo["id"] as? String
            let userName =  userInfo["userName"] as? String
            let image = userInfo["imageUrl"] as? String
            let userId = userInfo["userId"] as? String
            if UIApplication.topViewController() is ChatViewController {
                if /(UIApplication.topViewController() as? ChatViewController)?.chatModal.conversationId.value == conversationId{
                }else{
                    (UIApplication.topViewController() as? ChatViewController)?.chatModal.conversationId.value = conversationId
                    (UIApplication.topViewController() as? ChatViewController)?.chatingType = .oneToOne
                    (UIApplication.topViewController() as? ChatViewController)?.chatModal.chatId.value = ""
                    (UIApplication.topViewController() as? ChatViewController)?.onLoad(isPush: true)
                    (UIApplication.topViewController() as? ChatViewController)?.updateDetails(name: /userName , image: /image)
                }
            }else{
                
                var isExist = false
                UIApplication.topViewController()?.navigationController?.viewControllers.forEach({ (viewControl) in
                    if viewControl is ChatViewController{
                        isExist = true
                        UIApplication.topViewController()?.navigationController?.popToViewController(viewControl, animated: true)
                    }
                })
                if !isExist{
                    guard let vc = R.storyboard.chats.chatViewController() else { return }
                    Singleton.sharedInstance.conversationId =  /conversationId
                    vc.chatModal.conversationId.value = conversationId
                    vc.chatingType = .oneToOne
                    let user = User()
                    user.userName = userName
                    let img = ImageUrl()
                    img.original = image
                    img.thumbnail = image
                    user.img = img
                    user.id = userId
                    vc.isFromChat = true
                    vc.receiverData = user
                    UIApplication.topViewController()?.pushVC(vc)
                }
                
                
            }
            
        case .groupChat:
            let conversationId = userInfo["id"] as? String
            let userName =  userInfo["groupName"] as? String
            let image = userInfo["imageUrl"] as? String
            let groupId = userInfo["groupId"] as? String
            if UIApplication.topViewController() is ChatViewController {
                if /(UIApplication.topViewController() as? ChatViewController)?.chatModal.conversationId.value == conversationId{
                }else{
                    (UIApplication.topViewController() as? ChatViewController)?.chatModal.conversationId.value = conversationId
                    (UIApplication.topViewController() as? ChatViewController)?.chatingType = .oneToOne
                    (UIApplication.topViewController() as? ChatViewController)?.chatModal.groupIdToJoin.value = groupId
                    (UIApplication.topViewController() as? ChatViewController)?.chatModal.chatId.value = ""
                    (UIApplication.topViewController() as? ChatViewController)?.onLoad(isPush: true)
                    (UIApplication.topViewController() as? ChatViewController)?.updateDetails(name: /userName , image: /image)
                }
            }else{
                
                var isExist = false
                UIApplication.topViewController()?.navigationController?.viewControllers.forEach({ (viewControl) in
                    if viewControl is ChatViewController{
                        isExist = true
                        UIApplication.topViewController()?.navigationController?.popToViewController(viewControl, animated: true)
                    }
                })
                
                if !isExist{
                    let conversationId = conversationId
                    let userName =  userName
                    let groupId = groupId
                    Singleton.sharedInstance.conversationId =  /conversationId
                    guard let vc = R.storyboard.chats.chatViewController() else { return }
                    vc.chatModal = ChatViewModal(conversationId: /conversationId , chatId: "" , groupId: groupId)
                    vc.isFromChat = true
                    vc.chatingType = .oneToMany
                    let user = User()
                    user.id = groupId
                    user.userName = userName
                    let img = ImageUrl()
                    img.original = image
                    img.thumbnail = image
                    user.img = img
                    vc.receiverData = user
                    UIApplication.topViewController()?.pushVC(vc)
                }
            }
            
        case .venueChat:
            
            let conversationId = userInfo["id"] as? String
            let venueId = userInfo["venueId"] as? String
            let venueTitle = userInfo["venueTitle"] as? String
            let image = userInfo["imageUrl"] as? String
            Singleton.sharedInstance.conversationId =  /conversationId
            let venue = Venues()
            venue.groupId = venueId
            venue.venueTitle = venueTitle
            let img = ImageUrl()
            img.original = image
            img.thumbnail = image
            venue.venueImageUrl = img
            venue.isMine = true
            
            if UIApplication.topViewController() is ChatViewController {
                if /(UIApplication.topViewController() as? ChatViewController)?.chatModal.venue.value?.groupId == venueId{
                }else{
                    (UIApplication.topViewController() as? ChatViewController)?.chatModal.venue.value = venue
                    (UIApplication.topViewController() as? ChatViewController)?.chatingType = .venue
                    (UIApplication.topViewController() as? ChatViewController)?.chatModal.groupIdToJoin.value = venueId
                    (UIApplication.topViewController() as? ChatViewController)?.chatModal.chatId.value = conversationId
                    (UIApplication.topViewController() as? ChatViewController)?.onLoad(isPush: true)
                    (UIApplication.topViewController() as? ChatViewController)?.updateDetails(name: /venueTitle , image: /image)
                }
            }else{
                
                var isExist = false
                UIApplication.topViewController()?.navigationController?.viewControllers.forEach({ (viewControl) in
                    if viewControl is ChatViewController{
                        isExist = true
                        UIApplication.topViewController()?.navigationController?.popToViewController(viewControl, animated: true)
                    }
                })
                
                if !isExist{
                    guard let vc = R.storyboard.chats.chatViewController() else { return }
                    vc.isFromChat = true
                    vc.chatingType = .venue
                    vc.chatModal = ChatViewModal(membersData: nil, venueD: venue)
                    vc.chatModal.groupIdToJoin.value = venueId
                    UIApplication.topViewController()?.pushVC(vc)
                }
            }
        case .likePost,.post,.likeReply,.likeComment,.tagReply,.tagComment,.comment:
            
            if UIApplication.topViewController() is NotificationsViewController {
                (UIApplication.topViewController() as? NotificationsViewController)?.refreshRetrieval()
            }else{
                if UIApplication.topViewController() is HomePostListViewController || UIApplication.topViewController() is ChatListingViewController || UIApplication.topViewController() is SearchPeopleViewController || UIApplication.topViewController() is SocialInteractionViewController {
                    
                    ((UIApplication.topViewController() as? HomePostListViewController)?.tabBarController as? OnboardTabViewController)?.selectedIndex = 0
                    ((UIApplication.topViewController() as? ChatListingViewController)?.tabBarController as? OnboardTabViewController)?.selectedIndex = 0
                    ((UIApplication.topViewController() as? SearchPeopleViewController)?.tabBarController as? OnboardTabViewController)?.selectedIndex = 0
                    ((UIApplication.topViewController() as? SocialInteractionViewController)?.tabBarController as? OnboardTabViewController)?.selectedIndex = 0
                    
                }
            }
        default:
            print(UIApplication.topViewController())
            
            if UIApplication.topViewController() is NotificationsViewController {
                (UIApplication.topViewController() as? NotificationsViewController)?.refreshRetrieval()
            }else{
                if UIApplication.topViewController() is HomePostListViewController || UIApplication.topViewController() is ChatListingViewController || UIApplication.topViewController() is SearchPeopleViewController || UIApplication.topViewController() is SocialInteractionViewController {
                    
                    ((UIApplication.topViewController() as? HomePostListViewController)?.tabBarController as? OnboardTabViewController)?.selectedIndex = 3
                    ((UIApplication.topViewController() as? ChatListingViewController)?.tabBarController as? OnboardTabViewController)?.selectedIndex = 3
                    ((UIApplication.topViewController() as? SearchPeopleViewController)?.tabBarController as? OnboardTabViewController)?.selectedIndex = 3
                    ((UIApplication.topViewController() as? SocialInteractionViewController)?.tabBarController as? OnboardTabViewController)?.selectedIndex = 3
                    
                }
            }
        }
    }
    
    // MARK::- UPDATE DEVICE TOKEN
    
    func updateDeviceToken(){
        if Singleton.sharedInstance.loggedInUser?.token != nil && /Singleton.sharedInstance.loggedInUserDeviceToken != "" {
            LoginTarget.updateDeviceToken().request(apiBarrier: false).asObservable()
                .subscribeOn(MainScheduler.instance)
                .subscribe(onNext: { (response) in
                    print("updated device token")
                }, onError: { (error) in
                    print(error)
                })<bag
        }
    }
}

extension AppDelegate: PKPushRegistryDelegate {
    
    func pushRegistry(_ registry: PKPushRegistry, didUpdate credentials: PKPushCredentials, for type: PKPushType) {
        print("\(#function) voip token: \(credentials.token)")
        
        let deviceToken = credentials.token.reduce("", {$0 + String(format: "%02X", $1) })
        print("\(#function) token is: \(deviceToken)")
        Singleton.sharedInstance.apnsDeviceToken = deviceToken
    }
    
    
    func pushRegistry(_ registry: PKPushRegistry, didReceiveIncomingPushWith payload: PKPushPayload, for type: PKPushType) {
        
        print("\(#function) incoming voip notfication: \(payload.dictionaryPayload)")
        if let payload = payload.dictionaryPayload["data"] as? [String:Any],
            let handle = payload["callerInfo"] as? [String:Any],
            let session_id = payload["session_id"] as? String,
            let token = payload["token"] as? String {
            
            //OTAudioDeviceManager.setAudioDevice(OTDefaultAudioDevice.sharedInstance())
            guard let callerName = handle["fullName"] as? String else { return }
            guard let callerId = handle["_id"] as? String else { return }
            // display incoming call UI when receiving incoming voip notification
            let backgroundTaskIdentifier = UIApplication.shared.beginBackgroundTask(expirationHandler: nil)
            self.displayIncomingCall(uuid: UUID(), handle: callerName, session_id: session_id, token: token, callerId: /callerId) { _ in
                UIApplication.shared.endBackgroundTask(backgroundTaskIdentifier)
            }
        }
    }
    
    func pushRegistry(_ registry: PKPushRegistry, didReceiveIncomingPushWith payload: PKPushPayload, for type: PKPushType, completion: @escaping () -> Void) {
        NSLog("pushRegistry:didReceiveIncomingPushWithPayload:forType:completion:")
        print("\(#function) incoming voip notfication: \(payload.dictionaryPayload)")
        if (type == PKPushType.voIP) {
            // The Voice SDK will use main queue to invoke `cancelledCallInviteReceived:error:` when delegate queue is not passed
            if let payload = payload.dictionaryPayload["data"] as? [String:Any],
                let handle = payload["callerInfo"] as? [String:Any],
                let session_id = payload["session"] as? String,
                let token = payload["token"] as? String {
                
                //OTAudioDeviceManager.setAudioDevice(OTDefaultAudioDevice.sharedInstance())
                guard let callerName = handle["fullName"] as? String else { return }
                 guard let callerId = handle["_id"] as? String else { return }
                // display incoming call UI when receiving incoming voip notification
                let backgroundTaskIdentifier = UIApplication.shared.beginBackgroundTask(expirationHandler: nil)
                self.displayIncomingCall(uuid: UUID(), handle: callerName, session_id: session_id, token: token , callerId : callerId) { _ in
                    UIApplication.shared.endBackgroundTask(backgroundTaskIdentifier)
                }
            }
        }
        
        if let version = Float(UIDevice.current.systemVersion), version < 13.0 {
            // Save for later when the notification is properly handled.
            
        } else {
            /**
             * The Voice SDK processes the call notification and returns the call invite synchronously. Report the incoming call to
             * CallKit and fulfill the completion before exiting this callback method.
             */
            completion()
        }
    }
    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {
        print("\(#function) token invalidated")
    }
    
    /// Display the incoming call to the user
    func displayIncomingCall(uuid: UUID, handle: String, hasVideo: Bool = false, session_id: String, token:String, callerId : String?, completion: ((NSError?) -> Void)? = nil) {
        configureAudioSession()
        providerDelegate?.reportIncomingCall(uuid: uuid, handle: handle, hasVideo: hasVideo, session_id: session_id, token: token, userId: /callerId, completion: completion)
    }
    
    func hitApiConnectDisconnectCall(notify : Bool = false , connect : Bool = false ) {
        disConnectAPI()
    }
    func disConnectAPI(){
        
        ChatTarget.callDisconnected(callerId: /Singleton.sharedInstance.loggedInUser?.id, receiverId: /caller?.userId).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.providerDelegate?.endCurrentCall()
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        //                        self.handleError(error: err)
                    }
            })
        
    }
}

extension AppDelegate {
    
    static var shared: AppDelegate? {
        guard let delegate = UIApplication.shared.delegate as? AppDelegate else { return nil }
        return delegate
    }
}
