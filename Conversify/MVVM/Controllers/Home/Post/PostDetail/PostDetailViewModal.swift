//
//  PostDetailViewModal.swift
//  Conversify
//
//  Created by Apple on 19/11/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa
import RxDataSources


class PostDetailViewModal: BaseRxViewModel {
    
    var postDetail = Variable<PostList?>(nil)
    var postId = Variable<String?>(nil)
    var mediaId = Variable<String?>(nil)
    var postId1 = ""
    var arrayOfComments = Variable<[PostDetailDataSec]>([])
    
    func getPostDetail(_ completion:@escaping (Bool)->()){
       
        PostTarget.getPostWithComment(postId: String(postId.value ?? "") , mediaId: mediaId.value).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                
                
                guard let safeResponse = response as? DictionaryResponse<PostList> else{
                    return
                }
                self?.postDetail.value = safeResponse.data
                if safeResponse.data?.comment != nil {
                    guard let val = safeResponse.data?.comment?.map({ (cmnt) -> PostDetailDataSec in
                        return PostDetailDataSec(header: cmnt , items: [])
                    }) else { return }
                    self?.arrayOfComments.value.removeAll()
                    self?.arrayOfComments.value = val
                }
                completion(true)
                
                
                }, onError: { (error) in
                    print(error)
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    
    func deletePost(postId: String?, _ completion:@escaping (Bool)->()){
        PostTarget.deletePost(postId: postId).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
                    
            })<bag
    }
    
    func likeUnlikeComment(mediaId: String? , commentId : String? , action : String? ,commentBy : String? ,  _ completion:@escaping (Bool)->()){
        PostTarget.likeOrUnlikeComment(mediaId: /mediaId, commentId: commentId, action: action, commentBy: commentBy).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                }, onError: { (error) in
                    print(error)
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    
    func deleteCommentReply(mediaId : String? ,commentId: String? , replyId: String?){
        PostTarget.deleteComment(mediaId : /mediaId ,commentId: commentId, replyId: replyId).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: {  (response) in
            }, onError: { [weak self] (error) in
                print(error)
                self?.deleteCommentReply(mediaId: /mediaId, commentId: commentId , replyId: replyId)
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default : break
                }
                
            })<bag
    }
    
    func likeUnlikeReply(mediaId : String? , replyId : String? , action : String? , replyBy : String? , _ completion:@escaping (Bool)->()){
        PostTarget.likeOrUnlikeReply(mediaId: /mediaId , replyId:replyId ,action: action, replyBy: replyBy).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                completion(true)
                }, onError: { (error) in
                    print(error)
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
                    
            })<bag
    }
    
    func replyOnComment(mediaId:String? , sec : Int? , commentBy : String? , commentId : String? , userIdTag : String? , replyId : String? ,reply : String? ,   _ completion:@escaping (Bool)->()){
        
        PostTarget.addEditReplies(mediaId: /mediaId , commentId: commentId, commentBy: commentBy, replyId: replyId, userIdTag: userIdTag, reply: reply, postId: postId.value).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<ReplyList> else{
                    return
                }
                
                //                var sectionData = self?.arrayOfComments.value[/sec]
                //                var sectionHeader = sectionData?.header
                //                sectionHeader?.replyCount = /sectionHeader?.replyCount + 1
                //                self?.arrayOfComments.value[/sec] = PostDetailDataSec(header: sectionHeader, items:  [])
                
                completion(true)
                }, onError: { (error) in
                    print(error)
                    completion(false)
                    
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
            })<bag
    }
    
    func commentOnPost(mediaId : String? ,postId : String? , commentId : String? , userHashTag : String? , comment : String? ,postBy : String? , attachmentUrl: NSDictionary, _ completion:@escaping (Bool)->()){
        
        PostTarget.addEditComment(mediaId: /mediaId , postId: postId, commentId: commentId, userIdTag: userHashTag, comment: comment, postBy: postBy, attachmentUrl: attachmentUrl).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<CommentList> else{
                    return
                }
                self?.arrayOfComments.value.append(PostDetailDataSec(header: safeResponse.data , items: []))
                //                self?.postDetail.value?.commentCount = self?.arrayOfComments.value.count
                completion(true)
                }, onError: { (error) in
                    completion(false)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break
                    }
                    
            })<bag
    }
    
    func getCommentReplies(mediaId: String?, indx : Int? , commentId : String? , replyId : String? ,totalReply : String? , _ completion:@escaping (Bool)->()){
        PostTarget.getCommentReplies(mediaId: /mediaId , commentId: commentId, replyId: replyId, totalReply: totalReply).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<ReplyList> else{
                    return
                }
                if replyId == nil {
                    self?.arrayOfComments.value[/indx].items.removeAll()
                }
                
                guard let val = safeResponse.array.map({$0}) else { return }
                
                self?.arrayOfComments.value[/indx].items = val + (self?.arrayOfComments.value[/indx].items ?? [])
                
                completion(true)
                }, onError: { (error) in
                    self.getCommentReplies(mediaId: /mediaId , indx : indx , commentId : commentId , replyId : replyId ,totalReply :totalReply , completion)
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                    default : break }
                    completion(false)
            })<bag
    }
    
    func searchUsers(serachText : String?){
        PostTarget.searchUser(search: serachText).request(apiBarrier: false).asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                
                }, onError: { (error) in
                    print(error)
                    
            })<bag
    }
}

struct PostDetailDataSec {
    var header: CommentList?
    var items: [Item]
}

extension PostDetailDataSec: SectionModelType {
    
    typealias Item = ReplyList
    
    init(original: PostDetailDataSec, items: [Item]) {
        self = original
        self.items = items
    }
}
