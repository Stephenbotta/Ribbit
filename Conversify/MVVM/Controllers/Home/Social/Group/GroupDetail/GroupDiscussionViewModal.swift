//
//  GroupDiscussionViewModal.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 15/11/18.
//

import UIKit
import RxSwift
import Foundation
import RxCocoa

class GroupDiscussionViewModal: BaseRxViewModel {
    
    var loadMore = false
    var page = 1
    var group = Variable<GroupsPostData?>(nil)
    var posts = Variable<[ConversData]>([])
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var groupDetail = Variable<YourGroup?>(nil)
    var backRefresh:(()->())?
    var backRefreshLeft:(()->())?
    var isDismiss = false
    var exitSuccesfully = PublishSubject<Bool>()
    
    override init() {
        super.init()
    }
    
    init(groupD: YourGroup? ) {
        super.init()
        groupDetail.value = groupD
    }
    
    func getPosts(isRefresh:Bool? = true){
        if /isRefresh{
            beginCommunication.onNext(true)
        }
        GroupTarget.getGroupsPost(groupId: groupDetail.value?.id, page: page.toString).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<GroupsPostData> else{
                    return
                }
                if self?.page == 1{
                    self?.posts.value.removeAll()
                }
                for not in safeResponse.data?.conversData ?? []{
                    self?.posts.value.append(not)
                }
                if self?.posts.value.count == 0{
                     self?.posts.value = []
                }else{
                    let postsId = self?.posts.value.map{ /$0.id }
                    self?.readPosts(groupId: /self?.groupDetail.value?.id, postId: /postsId?.toJson())
                }
                self?.loadMore = (safeResponse.data?.conversData?.count == 10)
                self?.group.value = safeResponse.data
                self?.endCommunication.onNext(true)
                }, onError: { (error) in
                    self.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
                    print(error)
            })<bag
    }
    
    func fvrtPost(row: Int , state: String){
        GroupTarget.likePost(postId: posts.value[row].id , state : state, postBy: posts.value[row].postBy?.id ).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let post = self?.posts.value[row] else { return }
                post.liked = /state == "1"
                let lastCount = /(self?.posts.value[row].likeCount)
                post.likeCount =  /state == "1" ? lastCount + 1 :  lastCount - 1
                self?.posts.value[row] = post
                self?.endCommunication.onNext(true)
                }, onError: { (error) in
                    self.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
                    print(error)
            })<bag
    }
    
    func addComment(row: Int , comment: String){
        GroupTarget.addComment(postId: posts.value[row].id, comment: comment, postUserId: /posts.value[row].postBy?.id).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let post = self?.posts.value[row] else { return }
                let commentCount = /(self?.posts.value[row].commentCount)
                post.commentCount =  commentCount + 1
                self?.posts.value[row] = post
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                   self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
                    print(error)
            })<bag
    }
    
    
    func readPosts(groupId: String?, postId: String?){
//        GroupTarget.readPosts(groupId: /groupId, postId: /postId).request(apiBarrier: false)
//            .asObservable()
//            .subscribeOn(MainScheduler.instance)
//            .subscribe(onNext: { [weak self] (response) in
//                print("done")
//                
//                }, onError: { (error) in
//                    print(error)
//                    
//            })<bag
    }
    
    func exitGroup(){
        ChatTarget.exitGroup(groupId: groupDetail.value?.id).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.exitSuccesfully.onNext(true)
                }, onError: { (error) in
                    if let err = error as? ResponseStatus {
                        self.handleError(error: err)
                    }
                    print(error)
            })<bag
    }
    
    
}
