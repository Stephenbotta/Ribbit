//
//  APIConstants.swift
//  Connect
// //  Created by OSX on 02/01/18.
//  Copyright © 2018 OSX. All rights reserved.
//

import Foundation

internal struct APIConstants {

   // static let base : String = "http://100.21.168.56:8005/"
   // static let basePath:String = "http://100.21.168.56:8005/user/"

    static let base : String = "http://api-ribbit.royoapps.com"
    static let basePath:String = "http://api-ribbit.royoapps.com/user/"
    
    //Ribbit Survey APIS
    static let getTakeSurveyProperties = "getTakeSurveyProperties"
    static let getPointEarnedHistory = "getPointEarnedHistory"
    static let takeSurveyProperties = "takeSurveyProperties"
    static let getSurveyList = "getSurvey"
    static let getSurveyQuestions = "getSurveyQuestions"
    static let sumitUserSurvey = "sumitUserSurvey"
    
    static let terms = "http://api-ribbit.royoapps.com/termsandcondition"
    static let contactus = "http://api-ribbit.royoapps.com/contactUs"
    
    
    static let googlePlacesBasePath:String = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
    static let filters = "listOfFilters"
    static let getInfo = "getData"
    
    static let updateInterests = "updateUserCategories"
    static let register:String = "regEmailOrPhone"
    static let login:String = "logIn"
    static let userSignUp:String = "signUp"
    static let editUserProfile:String = "editProfile"
    static let logout:String = "logOut"
    static let verifyOTP:String = "verifyOTP"
    static let resendOTP:String = "resendOTP"
    static let createVenue: String = "addEditVenueGroup"
    static let setResetPassword : String = "forgotPassword"
    static let filterData : String = "getVenueFilter"
    static let joinVenue : String = "joinGroup"
    static let venueConversationDetails = "venueConversationDetails"
    static let getRequestCount = "requestCounts"
    static let createGroup = "addEditPostGroup"
    static let filteredGroupsBycat = "getCatPostGroups"
    static let addEditPost = "addEditPost"
    static let groupsPost = "postGroupConversation"
    static let likePost = "likeOrUnlike"
    static let joinGroup = "joinGroup"
    static let addEditComment = "addEditComment"
    static let addEditReplies = "addEditReplies"
    static let readPosts = "readGroupPosts"
    static let disableNotificationGroup = "configNotification"
    static let exitGroup = "exitGroup"
    static let venueDetails : String = "groupDetails"
    static let searchGroup : String = "homeSearchGroup"
    
    //PostDetail
    
    static let getPostWithComment = "getPostWithComment"
    static let getCommentReplies = "getCommentReplies"
    static let searchUser = "searchUser"
    static let userNameCheck = "userNameCheck"
    static let chatSummary = "chatSummary"
    
    static let peopleCrossedPaths = "crossedPeople"
    
    static let interestMatchUsers = "interestMatchUsers"
    static let getNotification = "getNotifications"
    static let acceptRejectRequest = "acceptInviteRequest"
    static let readNotifications = "readNotifications"
    static let chatConversation = "chatConversation"
    static let unreadCount = "unreadCount"
    //Profile
    static let getProfileData = "getProfileData"
    static let blockUser = "blockUser"
    static let follow = "followUnfollow"
    static let followersList = "listFollowerFollowing"
    static let updateDeviceToken = "updateDeviceToken"
    static let followersNotJoinedGroup = "addParticipantsList"
    static let addParticpant = "addParticipants"
    static let archiveGroup = "archiveGroup"
    static let topSearch = "homeSearchTop"
    static let topTags = "homeSearchTag"
    static let followUnFollowTag = "followUnfollowTag"
    static let searchPost = "homeSearchPost"
    static let verification = "settingVerification"
    static let clearNotification = "clearNotification"
    static let searchVenue = "homeSearchVenue"
    static let inviteusers = "groupInviteUsers"
    static let whoCanSeeMe = "configSetting"
    static let invitePpl = "settingInvitePeople"
    static let verifyPhone = "phoneVerification"
    static let listBlockedUsers = "listBlockedUsers"
    static let deletePost = "deletePost"
    static let listLikers = "listLikers"
    static let deleteCommntReply = "deleteCommentReply"
    static let initiateAudioCall = "callInitiate"
    static let  getChallenges = "challenges"
    static let userStats = "stats"
    static let callDisconnect = "callDisconnect"
    static let contactUsApi = "contactUs"
    static let googleKey = "AIzaSyCzwlKpNV9bny5IkpO-OTWQLPF46lq3HW4"
    static let shareTextVenue = "Please open this link to join my Check it Venue."
    static let shareTextGroup = "Please open this link to join my Check it Network."
    static let otherUserPost = "userPosts"
    static let tangoGetCatalog = "tangoGetCatalog"
    static let showRedeemHistory = "showRedeemHistory"
    static let tangoPostOrders = "tangoPostOrders"
    static let addTwitterTimming = "addTwitterTimming"
    static let showSpinWheel = "ShowSpinWheel"
    static let addSpinWheelPrize = "addSpinWheelPrize"
    static let showCharityOrgList = "showCharityOrgList"
    static let addCharityDonation = "addCharityDonation"
    static let getStories = "getStories"
    static let addStory = "addStory"
    
    static let promoteUser = "promoteUser"
  
}



enum ResponseStatus : Error {
    
    case clientError(message : String?)
    case success
    case blocked
    case missingAuthentication
    case invalidJSON
    case noInternet
    
    var serverValue : String{
        switch self {
        case .clientError:
            return "400"
        case .success:
            return "200"
        case .missingAuthentication:
            return "401"
        default:
            return ""
        }
    }
    
    static func getRawEnum (value : String) -> ResponseStatus{
        switch value {
        case "200":
            return .success
        case "400", "404":
            return .clientError(message: value)
        case "401":
            return .missingAuthentication
        default:
            return .blocked
        }
    }
}

