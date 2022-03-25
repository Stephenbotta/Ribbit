
import UIKit
import RxSwift
import Foundation
import RxCocoa

class AddGroupViewModal: BaseRxViewModel {
    
    var category = Variable<Interests?>(nil)
    var groupImage = Variable<UIImage?>(nil)
    var groupImageStr = Variable<String?>(nil)
    var groupTitle = Variable<String?>(nil)
    var groupDesc = Variable<String?>(nil)
    var access = Variable<String?>(nil)
    var helpBool =  Variable<Bool?>(true)
    var isValid: Observable<Bool> {
        return Observable.combineLatest(groupTitle.asObservable(),helpBool.asObservable()) { (name,helpBool) in
            self.isValidName(name: /name) && /helpBool
        }
    }
    var selectedUsers = Variable<[User]>([])
    var groupMembers = Variable<[Members?]>([])
    var beginCommunication = PublishSubject<Bool>()
    var endCommunication = PublishSubject<Bool>()
    var group = Variable<YourGroup?>(nil)
    var updateYourGroupDetails:((YourGroup?)->())?
    
    override init() {
        super.init()
    }
    
    init(categor: Interests? ) {
        super.init()
        category.value = categor
    }
    
    init(categor: YourGroup? ) {
        super.init()
        group.value = categor
    }
    
    func uploadImage(_ completion:@escaping (Bool)->()){
        Loader.shared.start()
        S3.upload(image: groupImage.value , success: { (imageName) in
            print(imageName)
            self.groupImageStr.value = imageName
            completion(true)
        }) { (error) in
            Loader.shared.stop()
            print(error)
            
        }
    }
    
    func createGroup(_ completion:@escaping (Bool)->()){
        let participants = self.selectedUsers.value.map { (user) -> String in
            return /user.id
        }
        GroupTarget.createGroup(categoryId: category.value?.id , venueImage: groupImageStr.value, groupName: groupTitle.value, privacy: access.value , participants:participants.toJson() , description: groupDesc.value)
            .request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                completion(true)
            }, onError: { (error) in
                print(error)
                completion(false)
                if let err = error as? ResponseStatus {
                    self.handleError(error: err)
                }
            })<bag
    }
    
    func submitGroup(_ completion:@escaping (Bool)->()){
        
        if groupImage.value != nil && groupImage.value != R.image.camera(){
            uploadImage { (status) in
                if status{
                    self.createGroup { (status) in
                        if status{
                            completion(true)
                        }
                    }
                }
            }
        }else{
            createGroup { (status) in
                if status{
                    completion(true)
                }
            }
        }
        
    }
    
    func isValidName(name: String) -> Bool {
        if name.length < 0 { return false }
        if name.isBlank { return false }
        return true
    }
    
    
    func retrieveDetail(){
        beginCommunication.onNext(true)
        GroupTarget.getVenueDetail(venueId: /group.value?.id).request(apiBarrier: false)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                guard let safeResponse = response as? DictionaryResponse<YourGroup> else{
                    return
                }
                self?.group.value = safeResponse.data
                self?.groupMembers.value = self?.group.value?.membersList ?? []
                self?.endCommunication.onNext(true)
                }, onError: { [weak self] (error) in
                    print(error)
                    self?.endCommunication.onNext(true)
                    if let err = error as? ResponseStatus {
                        self?.handleError(error: err)
                    }
            })<bag
    }
    
    
}
