//
//  Constants.swift
//  Connect
//
//  Created by OSX on 20/12/17.
//  Copyright © 2017 OSX. All rights reserved.
//

import UIKit
import Foundation

internal struct Keys {
    static let giphyKey = "oHExTkZIj4RHWxysSOX9yhYt28HS5iMr"
    static let tokBoxKey = "46524272"
    //"46524272"
   static let adUnitID =  "ca-app-pub-3940256099942544/3986624511"
    //static let adUnitID =  "ca-app-pub-9217858803225445/6093150349"orignl 
}


internal struct TimeFormat {
    
    static let hhmm_a = "hh:mm a"
    static let dd_MMMM_yyyy_at_hhmm_a = "dd MMMM yyyy 'at' hh:mm a"
    static let dd_MMM_at_hhmm_a = "dd MMM 'at' hh:mm a"
    static let yyyy__MM__dd = "yyyy-MM-dd"
    static let HHmmss = "HH:mm:ss"
    static let yyyy__MM__ddTHHmmssSSSZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    static let dd = "dd"
    static let MMM = "MMM"
    static let dd_MMM_yyyy = "dd MMM yyyy"
}


internal struct RegexExpresssions {
    static let UserName = "^(?=.*[a-zA-Z0-9])[ \\w_.-]*$"
    static let EmailRegex = "[A-Z0-9a-z._%+-]{1,}+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}"
    static let PhoneRegex = "[0-9]{6,14}"
    static let alphabeticRegex = "^[a-zA-Z ]{4,}$"
    static let passwordRegex = ".{6,}$"
    static let tagRegex = "(#\\w+)\\b"
}

//enum HomeTabBar: Int {
//
//    case trips = 0
//    case people
//    case createTrip
//    case notifications
//    case profile
//
//    var title: String {
//        switch self {
//        case .trips: return "Trips"
//        case .people: return "People"
//        case .createTrip: return " "
//        case .notifications: return "Notifications"
//        case .profile: return "Profile"
//        }
//    }
//
//    var image: UIImage {
//        switch self {
//        case .trips: return R.image.ic_trips_2() ?? UIImage.blankImage()
//        case .people: return R.image.ic_people_2() ?? UIImage.blankImage()
//        case .createTrip: return R.image.ic_footer_logo() ?? UIImage.blankImage()
//        case .notifications: return R.image.ic_notification_2() ?? UIImage.blankImage()
//        case .profile: return R.image.ic_profile_2() ?? UIImage.blankImage()
//        }
//    }
//
//    var selectedImage: UIImage {
//        switch self {
//        case .trips: return R.image.ic_trips() ?? UIImage.blankImage()
//        case .people: return R.image.ic_people() ?? UIImage.blankImage()
//        case .createTrip: return R.image.ic_footer_logo() ?? UIImage.blankImage()
//        case .notifications: return R.image.ic_notification() ?? UIImage.blankImage()
//        case .profile: return R.image.ic_profile() ?? UIImage.blankImage()
//        }
//    }
//
//}

struct TableCellHeight {
    
    static let profileCellHeight:CGFloat = 58.0
    static let peopleTableCell:CGFloat = 80.0
    static let notificationTableCell:CGFloat = 80.0
    static let tripCellHeight:CGFloat = 140.0
    static let sectionHeaderHeight:CGFloat = 54.0
    static let tripDetailViewCellHeight:CGFloat = 317.0
    
}


//enum ProfileTableCells: Int {
//
//    case myTrips = 0
//    case personalAssistants
//    case settings
//    case logOut
//
//    var value: String {
//        switch self {
//        case .myTrips: return "My Trips"
//        case .personalAssistants: return "Personal Assistants"
//        case .settings: return "Settings"
//        case .logOut: return "Log Out"
//        }
//    }
//
//    var image: UIImage {
//        switch self {
//        case .myTrips: return R.image.ic_mytrips() ?? UIImage.blankImage()
//        case .personalAssistants: return R.image.ic_personalassistant() ?? UIImage.blankImage()
//        case .settings: return R.image.ic_settings() ?? UIImage.blankImage()
//        case .logOut: return R.image.ic_logout() ?? UIImage.blankImage()
//        }
//    }
//
//    static let all:[ProfileTableCells] = [.myTrips,
//                                          .personalAssistants,
//                                          .settings,
//                                          .logOut]
//
//}



enum PeopleViewMoreOptions: Int {
    
    case newGroup = 0
    case invitePeople
    case refresh
    case help
    
    var title:String {
        switch self {
        case .newGroup: return "New Group"
        case .invitePeople: return "Invite People"
        case .refresh: return "Refresh"
        case .help: return "Help"
        }
    }
    
    static var all:[String] = [PeopleViewMoreOptions.newGroup.title,
                               PeopleViewMoreOptions.invitePeople.title,
                               PeopleViewMoreOptions.refresh.title,
                               PeopleViewMoreOptions.help.title]
    
}

enum TripsSegments: Int {
    
    case friendsTrip = 0
    case myTrips
    
    var title: String {
        switch self {
        case .friendsTrip: return "FRIEND'S TRIP"
        case .myTrips: return "MY TRIPS"
        }
    }
    
}


enum PeopleSegmentType: Int {
    case contacts = 0
    case groups
    case myGroups
    
    var title:String {
        switch self {
        case .contacts: return "CONTACTS"
        case .groups: return "GROUPS"
        default: return ""
        }
    }
}


//enum Settings: Int {
//    
//    case edit_profile = 0
//    case notifications
//    case change_password
//    case about_us
//    case terms_and_condition
//    case logout
//    
//    var title: String {
//        switch self {
//        case .edit_profile: return "Edit Profile"
//        case .notifications: return "Notifications"
//        case .change_password: return "Change Password"
//        case .about_us: return "About Us"
//        case .terms_and_condition: return "Terms & Condition"
//        case .logout: return "Logout"
//        }
//    }
//    
//    var image:UIImage {
//        switch self {
//        case .edit_profile: return /R.image.ic_edit_settings()
//        case .notifications: return /R.image.ic_notifications_settings()
//        case .change_password: return /R.image.ic_lock_settings()
//        case .about_us: return /R.image.ic_about_settings()
//        case .terms_and_condition: return /R.image.ic_info_settings()
//        case .logout: return /R.image.ic_logout_settings()
//        }
//    }
//    
//    static var all:[Settings] = [.edit_profile,
//                                 .notifications,
//                                 .change_password,
//                                 .about_us,
//                                 .terms_and_condition,
//                                 .logout]
//}



enum Colors: String {
    
    case yellow = "yellow"
    
    func color() -> UIColor {
        switch self {
        case .yellow: return UIColor(red:0.8, green:0.53, blue:0.1, alpha:1)
        }
    }
    
}



internal struct GlobalConstants {
    
    static let iPHONE_X:String = "iPhone X"
    static let add_friends:String = "Add Friends..."
    static let create_group:String = "Create Group"
    static let save_changes:String = "Save Changes"
    static let create_trip:String = "Create Trip"
    static let edit_trip:String = "Save Trip"
    static let matchedTrips:String = "Matched Trips"
    static let google_Api_key:String = "AIzaSyCeXi86QiLAFS6dZ7xefV2Y9rdbVR_2YfA"
    static let shared_with_groups_header:String = "Shared with groups"
    static let shared_with_friends_header:String = "Shared with friends"
    static let middle_dot:String = " · "
    static let tripsMessage:String = "Trips created by your friends will\n appear here."
    static let myTripsMessage:String = "Trips created by you will\n appear here."
    static let noInternet:String = "No internet connection"
    static let navBarHeight:CGFloat = UIDevice.current.modelName.isEqual(GlobalConstants.iPHONE_X) ? 84.0 : 64.0
    static let notifyFriends:String = "Notify Friends when you reach "
    static let about_us_url:String = "http://192.168.102.98:8000/aboutUs"
    static let terms_and_condition_url:String = "http://192.168.102.98:8000/termsAndConditions"
    static let i_accept:String = "I accept"
    static let terms_and_condition:String = " Terms & Conditions "
    static let of_connect:String = "of Connect"
    static let addToGroup:String = "Add to Group"
    static let invite:String = "Invite"
    static let cancel:String = "Cancel"
    
}


enum AlertMessages: String {
    
    case enterTripName = "Trip name is required"
    case checkInDate = "Check in date required"
    case checkInTime = "Check in time required"
    case checkOutDate = "Check out date required"
    case checkOutTime = "Checl out time required"
    case tripLocation = "Location required"
    case comment = "Comment Required"
    case loginExpired = "Sorry, your account has been logged in other device! Please login again to continue"
    case noInternet = "No Internet Connection"
    case serverConnectionError = "Could not connect to server"
    
    func value() -> String {
        return self.rawValue
    }
}



enum NotificationName:String {
    
    case refreshTripsList = "refreshTripsList"
    case refreshGroupsList = "refreshGroupsList"
    
    var observerName:NSNotification.Name {
        return NSNotification.Name(self.rawValue)
    }
}

enum HeaderFooterID : String {
    
    case VenueListHeaderView = "VenueListHeaderView"
}



enum identifireCVC : String{
    case AddStoryesImagesCollectionViewCell = "AddStoryesImagesCollectionViewCell"
    
}
