//
//  Post+Api.swift
//  Conversify
//
//  Created by Apple on 15/11/18.
//


import UIKit

import Moya
import RxSwift
import RxCocoa
import Foundation
import SwiftyJSON
import ObjectMapper
import EZSwiftExtensions

enum PostTarget {
    case deletePost(postId: String?)
    case deleteComment(mediaId : String? ,commentId: String?, replyId: String?)
    case getGroupList(currentLat :String? , currentLong :String? , flag: String?)
    case addEditPost(postId : String? , groupId : String? , postText : String? , postImageVideoOriginal : String? , postImageVideoThumbnail : String? , hashTags : String? , locationLong:  String? , locationLat: String? , locationAddress: String? , locationName: String? , postType: String? , media:String?)
    case getPostListing(currentLat :String? , currentLong :String? , flag: String?)
    case likeOrUnlikePost(mediaId:String? , postId : String? , action : String? , postBy : String?)
    case likeOrUnlikeComment( mediaId:String? , commentId : String? , action : String? , commentBy : String?)
    case likeOrUnlikeReply(mediaId:String? , replyId : String? , action : String? , replyBy : String?)
    case addEditComment(mediaId:String? , postId : String?,commentId : String?, userIdTag : String? , comment : String? , postBy : String?, attachmentUrl: NSDictionary)
    case createConverse( text: String?  , postingIn: String? , selectedPeople: String? , selectedInterests: String? , expireTime: String? , meetingTime: String? , locationLong:  String? , locationLat: String? , locationAddress: String? , locationName: String? , hasTags: String? , media: String?)
    
    //PostDetail
    case getPostWithComment(postId : String? , mediaId: String?)
    case addEditReplies(mediaId:String? ,commentId : String?, commentBy : String? ,replyId : String? , userIdTag : String? , reply : String? , postId : String?)
    case getCommentReplies(mediaId:String? ,commentId : String? , replyId : String? , totalReply : String?)
    case searchUser(search : String?)
    case searchPosts(text: String?, page: String?)
    case otherUserPost(pageNo: String?, id: String?)
    case addTwitterTimming
    case showSpinWheel
    case spinWheelPrize(value : Int?)
}

fileprivate let PostProvider = MoyaProvider <PostTarget>(plugins: [NetworkLoggerPlugin()])

extension PostTarget: TargetType {
    
    var parameters:[String: Any] {
        switch self {
            
        case .deleteComment(let mediaId ,let commentId, let replyId):
            var dict = [String: String]()
            if /commentId != ""{
                dict["commentId"] = commentId
            }
            if /replyId != ""{
                dict["replyId"] = replyId
            }
            if /mediaId != ""{
                dict["mediaId"] = mediaId
            }
            return dict
            
        case .deletePost(let postId):
            return ["postId":/postId]
            
        case .createConverse(let text, let postingIn,let  selectedPeople, let selectedInterests, let expireTime, let meetingTime, let locationLong, let locationLat, let locationAddress, let locationName, let hasTags , let media):
            var dict = [String:String]()
            if /text != ""{
                dict["postText"] = text
            }
            if /media != ""{
                dict["media"] = media
            }
            if /postingIn != ""{
                dict["postingIn"] = postingIn
            }
            if /selectedPeople != ""{
                dict["selectedPeople"] = selectedPeople
            }
            if /selectedInterests != ""{
                dict["selectInterests"] = selectedInterests
            }
            if /expireTime != ""{
                dict["expirationTime"] = expireTime
            }
            if /meetingTime != ""{
                dict["meetingTime"] = meetingTime
            }
            if /locationLong != ""{
                dict["locationLong"] = locationLong
                dict["locationLat"] = locationLat
                dict["locationAddress"] = locationAddress
                dict["locationName"] = locationName
            }
            if /hasTags != ""{
                dict["hashTags"] = hasTags
            }
            return dict
            
        case .searchPosts( let text , let page):
            var dict = [String:String]()
            if /text != ""{
                dict["search"] = text
            }
            if /page != ""{
                dict["pageNo"] = page
            }
            return   dict
            
        case .getGroupList(let currentLat , let  currentLong, let flag):
            var dict =  ["flag":"5" , "currentLat": /currentLat , "currentLong" : /currentLong ]
            if currentLat == nil{
                dict.removeValue(forKey: "currentLat")
                dict.removeValue(forKey: "currentLong")
            }
            return dict
            
        case .getPostListing(let currentLat , let  currentLong, let flag):
            var dict = ["flag":"4" , "currentLat": /currentLat , "currentLong" : /currentLong , "pageNo" : flag , "limit" : "10"]
            if currentLat == nil{
                dict.removeValue(forKey: "currentLat")
                dict.removeValue(forKey: "currentLong")
            }
            return dict
            
        case .otherUserPost(let pageNo, let id):
            let dict = ["pageNo" : pageNo , "limit" : "10", "id":id]
            return dict
            
        case .addEditPost(let postId, let groupId, let postText, let postImageVideoOriginal,let  postImageVideoThumbnail, let hashTags , let locationLong, let locationLat, let locationAddress, let locationName , let postType , let media) :
            
            var dict = ["postId" : postId , "groupId" : groupId , "postText" : /postText , "hashTags" : hashTags , "postType" : /postType ]
            if /locationName != ""{
                dict["locationLong"] = locationLong
                dict["locationLat"] = locationLat
                dict["locationAddress"] = locationAddress
                dict["locationName"] = locationName
            }
            if /media != ""{
                dict["media"] = media
            }
            if postId == nil{
                dict.removeValue(forKey: "postId")
            }
            //            if postText == nil || postText == ""  {
            //                dict.removeValue(forKey: "postText")
            //            }
            if hashTags == nil {
                dict.removeValue(forKey: "hashTags")
            }
            if groupId == nil{
                dict.removeValue(forKey: "groupId")
            }
            if postImageVideoOriginal == nil{
                dict.removeValue(forKey: "imageOriginal")
                dict.removeValue(forKey: "imageThumbnail")
            }
            
            return dict
            
        case .likeOrUnlikePost(let mediaId , let postId, let action , let postBy):
            var dict = ["postId" : postId , "action" : action , "postBy" : postBy]
            if /mediaId != ""{
                dict["mediaId"] = /mediaId
            }
            return dict
            
        case .likeOrUnlikeComment(let mediaId, let commentId, let action , let commentBy):
            var dict = ["commentId" : commentId , "action" : action , "commentBy" : commentBy]
            if /mediaId != ""{
                dict["mediaId"] = /mediaId
            }
            return dict
            
        case .likeOrUnlikeReply(let mediaId , let replyId, let action , let replyBy):
            var dict = ["replyId" : replyId , "action" : action , "replyBy" : replyBy]
            if /mediaId != ""{
                dict["mediaId"] = /mediaId
            }
            return dict
            
        case .addEditComment(let mediaId , let postId, let commentId,let  userIdTag, let comment , let postBy, let attachmentUrl):
            var dict =  ["postId" : postId,  "commentId" : commentId ,"userIdTag" : userIdTag ,"comment" : comment , "postBy" : postBy, "attachmentUrl": attachmentUrl] as [String : Any]
            if userIdTag == nil {
                dict.removeValue(forKey: "userIdTag")
            }
            if commentId == nil{
                dict.removeValue(forKey: "commentId")
            }
            if /mediaId != ""{
                dict["mediaId"] = /mediaId
            }
            if attachmentUrl == nil {
               dict.removeValue(forKey: "attachmentUrl")
            }
            return dict
            
        case .getPostWithComment(let postId , let mediaId):
            var dict = ["postId" : postId]
            if /mediaId != ""{
                dict["mediaId"] = /mediaId
            }
            return dict as [String : Any]
            
        case .addEditReplies(let mediaId ,let commentId, let commentBy,let replyId, let userIdTag, let reply , let postId):
            
            var dict = ["commentId" : commentId, "commentBy" : commentBy,  "reply" : reply , "postId" : postId , "userIdTag" : userIdTag]
            
            if userIdTag == nil {
                dict.removeValue(forKey: "userIdTag")
            }
            if /mediaId != ""{
                dict["mediaId"] = /mediaId
            }
            return dict
            //  ["commentId" : commentId, "commentBy" : commentBy, "replyId" : replyId,  "userIdTag" : userIdTag, "reply" : reply ]
            
        case .getCommentReplies(let mediaId ,let commentId, let replyId, let totalReply):
            
            var dict =  ["commentId" : commentId, "replyId" : replyId,  "totalReply" : totalReply ]
            if replyId == nil{
                dict.removeValue(forKey: "replyId")
            }
            if /mediaId != ""{
                dict["mediaId"] = /mediaId
            }
            return dict
            
        case .searchUser(let search):
            return ["search" : search]
            
        case .addTwitterTimming:
            return [:]
        case .showSpinWheel:
            return [:]
        case .spinWheelPrize(let value):
            return ["value":value]
        }
    }
    
    var task:Task {
        switch self {
        case .showSpinWheel:
        return .requestParameters(parameters: parameters, encoding: URLEncoding.queryString)
        default :
            return .requestParameters(parameters: parameters, encoding: JSONEncoding.default)
            
        }
    }
    
    var headers: [String : String]?{
        switch self {
        default:
            return ["Content-type": "application/json", "authorization": "bearer " + /Singleton.sharedInstance.loggedInUser?.token]
        }
        
    }
    
    var multipartBody: [MultipartFormData]? {
        switch self {
        default:
            return nil
        }
    }
    
    public var baseURL: URL {
        switch self {
            
        default:
            return URL(string: APIConstants.basePath)!
        }
    }
    
    public var path:String {
        switch self {
            
        case .deleteComment(_):
            return APIConstants.deleteCommntReply
            
        case .deletePost(_):
            return APIConstants.deletePost
            
        case .createConverse(_):
            return APIConstants.addEditPost
            
        case .searchPosts(_):
            return APIConstants.searchPost
            
        case .getGroupList(_) , .getPostListing(_):
            return APIConstants.getInfo
            
        case .addEditPost(_):
            return APIConstants.addEditPost
            
        case .likeOrUnlikeReply(_) , .likeOrUnlikeComment(_) , .likeOrUnlikePost(_):
            return APIConstants.likePost
            
        case .addEditComment(_):
            return APIConstants.addEditComment
            
        case .getPostWithComment(_):
            return APIConstants.getPostWithComment
            
        case .addEditReplies(_):
            return APIConstants.addEditReplies
            
        case .getCommentReplies(_):
            return APIConstants.getCommentReplies
            
        case .searchUser(_):
            return APIConstants.searchUser
            
        case .otherUserPost(_):
            return APIConstants.otherUserPost
            
        case .addTwitterTimming:
            return APIConstants.addTwitterTimming
            
        case .showSpinWheel:
            return APIConstants.showSpinWheel
        case .spinWheelPrize(_):
            return APIConstants.addSpinWheelPrize
        }
    }
    
    public var method: Moya.Method {
        switch self {
        case .addTwitterTimming , .spinWheelPrize: return .put
        case .showSpinWheel: return .get
        default: return .post
        }
    }
    
    public var sampleData: Data { return Data() }
    
    func request(apiBarrier : Bool = false) -> Observable<Any?>{
        
        return Observable<Any?>.create { (observer) -> Disposable in
            switch(self){
            default:
                if apiBarrier{
                    self.showLoader()
                }
            }
            let disposable = Disposables.create {}
            PostProvider.request(self, completion: { (result) in
                
                self.hideLoader()
                switch result {
                case let .success(moyaResponse):
                    let data = moyaResponse.data
                    let json = JSON(data)
                    
                    let status = self.handleResponse(json: json).serverValue
                    if status == ResponseStatus.success.serverValue{
                        observer.onNext(self.parse(response: json))
                        observer.on(.completed)
                    }else if status == ResponseStatus.missingAuthentication.serverValue{
                        UIApplication.shared.loginExpired()
                    }else{
                        observer.onError(ResponseStatus.clientError(message: /("message" => json.dictionaryValue)))
                        observer.on(.completed)
                    }
                    
                // do something with the response data or statusCode
                case let .failure(error):
                    // this means there was a network failure - either the request
                    // wasn't sent (connectivity), or no response was received (server
                    // timed out).  If the server responds with a 4xx or 5xx error, that
                    // will be sent as a ".success"-ful response.
                    print(error.localizedDescription)
                    observer.onError(ResponseStatus.noInternet)
                    observer.on(.completed)
                    break
                }
            })
            
            return disposable
        }
    }
    
    func showLoader() {
        Loader.shared.start()
    }
    
    func hideLoader(){
        Loader.shared.stop()
    }
    
    
    func handleResponse(json : JSON) -> ResponseStatus{
        return ResponseStatus.getRawEnum(value: /("statusCode" => json.dictionaryValue))
    }
    
    
    
    func parse(response : JSON?) -> Mappable? {
        guard let safeResponse = response else {return nil}
        print(response)
        switch self {
            
        case  .addEditPost(_) , .createConverse(_) , .deletePost(_) , .deleteComment(_):
            return nil
        case .showSpinWheel:
            return  Mapper<WheelDetail>().map(JSONObject: safeResponse.dictionaryObject)
        case .spinWheelPrize:
            return  Mapper<SpinWheelPrize>().map(JSONObject: safeResponse.dictionaryObject)
        case .getPostListing(_) , .getPostWithComment(_) , .searchPosts(_), .otherUserPost(_):
            return Mapper<DictionaryResponse<PostList>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .getGroupList(_):
            return Mapper<DictionaryResponse<GroupList>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .getCommentReplies(_) , .addEditReplies(_):
            return Mapper<DictionaryResponse<ReplyList>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .addEditComment(_):
            return Mapper<DictionaryResponse<CommentList>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .searchUser(_):
            return Mapper<DictionaryResponse<UserList>>().map(JSONObject: safeResponse.dictionaryObject)
            
        case .likeOrUnlikePost(_):
            
            UIApplication.topViewController()?.navigationController?.viewControllers.forEach({ (vc) in
                if vc is OnboardTabViewController{
                    
                    if UIApplication.topViewController() is HomePostListViewController{
                        
                    }else{
                        ((vc as?  OnboardTabViewController)?.viewControllers?[0] as? HomePostListViewController )?.onLoad()
                    }
                    
                }
                
                if vc is PostMediaDetailViewController {
                    (vc as? PostMediaDetailViewController)?.collectionView.reloadData()
                }
                
                
            })
            
            return nil
            
        default :
            return nil
        }
    }
}

