//
//  HomePostTableCell.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import UIKit
import GrowingTextView
import RxCocoa
import RxSwift
import Photos
import Lightbox
import SDWebImage

protocol PostCellDelegates : class {
    func likePost(indx : Int?)
    func commentOnPost(indx : Int? , cmmnt : String?)
    func showUserTags(searchText : String? , indx : Int?)
    func removeSearchView()
    func showPost(indx : Int?)
}

enum PostTypes: String{
    case regular = "REGULAR"
    case lookNearby = "LOOK_NEARBY"
    case converseNearBy = "CONVERSE_NEARBY"
}

class HomePostTableCell: BaseTableViewCell {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var constraintCollectionView: NSLayoutConstraint!
    @IBOutlet weak var collectionView: UICollectionView!{
        didSet{
            collectionView.delegate = self
            collectionView.dataSource = self
        }
    }
    @IBOutlet weak var constraintTopText: NSLayoutConstraint!
    @IBOutlet weak var pager: UIPageControl!
    @IBOutlet weak var imagePostType: UIImageView!
    @IBOutlet weak var imgUserPic: UIImageView!
    @IBOutlet weak var labelName: ActiveLabel!
    @IBOutlet weak var imgPicture: UIImageView!
    @IBOutlet weak var labelTime: UILabel!
    @IBOutlet weak var labelPS: ActiveLabel!
    @IBOutlet weak var btnLikes: UIButton!
    @IBOutlet weak var btnReplies: UIButton!
    @IBOutlet weak var btnLike: SparkButton!
    @IBOutlet weak var txtfComments: GrowingTextView!
    @IBOutlet weak var btnSend: UIButton!
    @IBOutlet weak var btnComment: UIButton!
    @IBOutlet weak var labelLocationName: UILabel!
    
    //MARK::- PROPERTIES
    var searchTimer: Timer?
    
    var delegate : PostCellDelegates?
    var row: Int?
    var selectedTableRow = -1
    var selectedCollectionIndex: Int?
    var item : PostList? {
        didSet {
            //Set Label Name in Category
            
            switch PostTypes(rawValue: /item?.postType) ?? .regular{
            case .regular:
                imagePostType.isHidden = true
            case .lookNearby:
                imagePostType.isHidden = false
                imagePostType.image = R.image.ic_binoculars()
            case .converseNearBy:
                imagePostType.isHidden = false
                imagePostType.image = R.image.ic_converseNerby()
            }
            labelLocationName?.text = /item?.locationName == "" ? "" : /item?.locationName //+ " " + /item?.locationAddress
            let customType = ActiveType.custom(pattern: /item?.groupDetail?.groupName)
            let customType3 = ActiveType.custom(pattern: /item?.postCategory?.categoryName)
            let customType1 = ActiveType.custom(pattern: " in ")//Regex that looks for "with"
            let customType2 = ActiveType.custom(pattern: /item?.postBy?.userName)//Regex that looks for "with"
            labelName.enabledTypes = [.mention, .hashtag, .url, customType , customType1 , customType2 , customType3 ,]
            labelName.customColor[customType] = #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1)
            labelName.customColor[customType3] =  #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1)
            labelName.customColor[customType1] =  #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
            labelName.customColor[customType2] =  #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1)
            labelName.handleCustomTap(for: customType) { element in
                print("Custom type tapped: \(element)")
            }
            labelName.handleCustomTap(for: customType2) {  [weak self] element in
                self?.endEditing(true)
                //                if Singleton.sharedInstance.loggedInUser?.id == /self?.item?.postBy?.id{
                //                    return
                //                }
                guard let vc = R.storyboard.home.profileViewController() else { return }
                vc.profileVM.userType = .otherUser
                vc.profileVM.userId = /self?.item?.postBy?.id
                UIApplication.topViewController()?.pushVC(vc)
            }
            if item?.postCategory != nil{
                labelName.text = /item?.postBy?.userName + " in " + /item?.groupDetail?.groupName + " (" + /item?.postCategory?.categoryName + ")"
            } else {
                labelName.text = item?.postBy?.userName
            }
            txtfComments.delegate = self
            
            //Set Label Post Text
            let postText = item?.postText?.capitalizedFirst()
            labelPS.highlightFontSize = 16
            labelPS.handleMentionTap { [weak self] (mention) in
                self?.endEditing(true)
                print("Success. You just tapped the \(mention) mention")
                guard let vc = R.storyboard.home.profileViewController() else { return  }
                vc.profileVM.userType = .otherUser
                vc.userName = mention
                vc.isMentioning = true
                UIApplication.topViewController()?.pushVC(vc)
                
            }
            
            labelPS.enabledTypes = [.mention, .hashtag, .url]
            labelPS.text = postText
            
            constraintCollectionView?.constant = /item?.media?.count == 0 ? 0 : UIScreen.main.bounds.width
             btnLikes.setTitle( /item?.likesCount?.toString + (/item?.likesCount == 0 ? " Like" :  " Likes"), for: .normal)
            var totalLikes = 0

            for media in item?.media ?? [] {
                totalLikes = totalLikes + /media.likeCount
            }

            item?.media?.forEach({ (media) in
                media.likePercent = Double(/media.likeCount) / Double(totalLikes)
            })
            
//            if /totalLikes == 0 {
////
//                btnLikes.setTitle("", for: .normal)
//
//            }else{
//                 let sortedLikeCount = item?.media?.sorted(by: { /$0.likePercent > /$1.likePercent })
//                let likeTxt = /String(/sortedLikeCount?.first?.likePercent?.rounded(toPlaces: 1) * 100)
//
//                let likeTxt1 = (Int(likeTxt) == 1) ? (likeTxt + "% Like") : (/likeTxt + "% Likes")
//
//                btnLikes.setTitle(likeTxt1, for: .normal)
//            }
            let cmtTxt = (item?.commentCount == 1) ? (/item?.commentCount?.toString + (/totalLikes == 0 ? " Reply · " : " Reply · ")) : (/item?.commentCount?.toString + (/totalLikes == 0 ? " Replies · " : " Replies · "))
            btnReplies.setTitle(cmtTxt, for: .normal)
            let postdate = Date(milliseconds: /item?.createdOn)
            labelTime.text = postdate.timePassed()
            
            
            
            imgUserPic.image(url:  /item?.postBy?.imageUrl?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))
            imgPicture.image(url:  /item?.media?.first?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))
            //imgPicture.kf.setImage(with: URL(string: /item?.media?.first?.thumbnail), placeholder: #imageLiteral(resourceName: "ic_placeholder"))
            imgPicture.isHidden = (/item?.media?.first?.thumbnail == "")
            btnLike.isSelected = /item?.liked
            //            vm.assets
            //                .asObservable()
            //                .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.selectedImageCollectionViewCell.identifier, cellType: SelectedImageCollectionViewCell.self)) { [weak self] (row,element,cell) in
            //                    cell.media = element
            //                    cell.imageCross.tag = row
            //                }<bag
            //
            //            collectionView.rx.setDelegate(self)<bag
            constraintTopText?.constant = (/item?.media?.count == 0 || /item?.media?.count == 1) ? 8 : 24
            pager.isHidden = (item?.media?.count == 0 || item?.media?.count == 1)
            pager.numberOfPages = /item?.media?.count
            collectionView.reloadData()
        }
    }
    
    
    override func awakeFromNib() {
        
        btnReplies.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.delegate?.showPost(indx: self?.btnComment.tag)
        })<bag
        
        btnLikes.rx.tap.asDriver().drive(onNext: { [weak self] () in
            if /self?.item?.likesCount != 0{
                guard let vc = R.storyboard.settings.followerViewController() else { return }
                vc.isFollower = nil
                vc.isPost = true
                vc.followerVM.isProfile = false
                vc.followerVM.selectedPrivacy = .likes
                vc.followerVM.postId = /self?.item?.id
                UIApplication.topViewController()?.pushVC(vc)
            }
            
        })<bag
        
        imgUserPic.addTapGesture { [weak self] (gesture) in
            self?.endEditing(true)
            //            if Singleton.sharedInstance.loggedInUser?.id == /self?.item?.postBy?.id{
            //                return
            //            }
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.profileVM.userId = /self?.item?.postBy?.id
            UIApplication.topViewController()?.pushVC(vc)
        }
        
        btnComment.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.delegate?.showPost(indx: self?.btnComment.tag)
        })<bag
        
        btnSend.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            if /self?.txtfComments.text.trimmed().isEmpty {
                UtilityFunctions.makeToast(text: "Please enter your comment", type: .error)
                return
            }
            self?.delegate?.commentOnPost(indx: self?.row, cmmnt: self?.txtfComments.text.trimmed())
            self?.txtfComments.text = nil
        })<bag
        
        btnLike.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.btnLike.isSelected = /self?.btnLike.isSelected.toggle()
            if /self?.btnLike.isSelected{
                self?.btnLike.likeBounce(0.6)
                self?.btnLike.animate()
            }else{
                self?.btnLike.unLikeBounce(0.4)
            }
            var likeCount = /self?.item?.likesCount
            likeCount = /self?.item?.liked ? ( likeCount - 1)  : ( likeCount + 1)
            let likeTxt = (likeCount == 1) ? (/likeCount.toString + " Like") : (/likeCount.toString + " Likes")
            
            self?.btnLikes.setTitle(likeTxt, for: .normal)
            self?.delegate?.likePost(indx: self?.row)
            
        })<bag
        NotificationCenter.default.addObserver(self, selector:  #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        
    }
    
    @objc func keyboardWillHide(_ notification: NSNotification) {
        if /self.txtfComments.text.trimmed().isEmpty {
            return
        }
        self.delegate?.commentOnPost(indx: self.row, cmmnt: self.txtfComments.text.trimmed())
        self.txtfComments.text = nil
        delegate?.removeSearchView()
    }
    
    
    
}


//MARK:- Growing Text View Delegate
extension HomePostTableCell :  GrowingTextViewDelegate  {
    
    func textViewDidChangeHeight(_ textView: GrowingTextView, height: CGFloat) {
        //        UIView.animate(withDuration: 0.2) { [weak self] in
        //            self?.superview?.layoutIfNeeded()
        //        }
    }
    func textViewDidChange(_ textView: UITextView) {
        btnSend.isHidden = (textView.text.trimmed().isEmpty) ? true : false
        btnLike.isHidden = !btnSend.isHidden
    }
    
    func textViewDidChangeSelection(_ textView: UITextView) {
        
        btnSend.isHidden = (textView.text.trimmed().isEmpty) ? true : false
        btnLike.isHidden = !btnSend.isHidden
        
        if searchTimer != nil {
            searchTimer?.invalidate()
            searchTimer = nil
        }
        searchTimer = Timer.scheduledTimer(timeInterval: 0.2, target: self, selector: #selector(searchForKeyword(_:)), userInfo: /textView.text, repeats: false)
        
    }
    
    @objc func searchForKeyword(_ timer: Timer) {
        guard let range = txtfComments.selectedTextRange else {
            return
        }
        let cursorPosition = txtfComments.offset(from: txtfComments.beginningOfDocument, to: range.start)
        let str = /txtfComments.text
        let index = str.index(str.startIndex, offsetBy: cursorPosition)
        let textbefore = str[..<index]
        let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if hasTag?.hasPrefix("@") == true {
            if searchUserText().isEmpty {
                delegate?.removeSearchView()
                return
            }else {
                delegate?.showUserTags(searchText: searchUserText(), indx: /row)
            }
        }
        else {
            delegate?.removeSearchView()
        }
    }
    
    //SEARCH USER TO TAG IN COMMENT
    func searchUserText() -> String {
        guard let range = txtfComments.selectedTextRange else {
            return ""
        }
        let cursorPosition = txtfComments.offset(from: txtfComments.beginningOfDocument, to: range.start)
        let str = /txtfComments.text
        let index = str.index(str.startIndex, offsetBy: cursorPosition)
        let textbefore = str[..<index]
        let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if hasTag?.hasPrefix("@") == true {
            return /hasTag?.replacingOccurrences(of: "@", with: "")
        }
        return ""
    }
    
    // REPLACE SEARCH STRING WITH USER_NAME
    func setTag(member: UserList) {
        guard let range = txtfComments.selectedTextRange else {
            return
        }
        let cursorPosition = txtfComments.offset(from: txtfComments.beginningOfDocument, to: range.start)
        var str = /txtfComments.text
        let index = str.index(str.startIndex, offsetBy: cursorPosition)
        let textbefore = str[..<index]
        let hasTag = textbefore.components(separatedBy: CharacterSet.init(charactersIn: " \n")).last
        if hasTag?.hasPrefix("@") == true {
            let endIndex = str.index(str.startIndex, offsetBy: cursorPosition)
            var length = 0
            for character in String(textbefore).reversed() {
                if character == "@" {
                    break
                }
                length += 1
            }
            let startIndex = str.index(str.startIndex, offsetBy: cursorPosition - length)
            str.replaceSubrange(startIndex..<endIndex, with: /member.userName + " ")
            txtfComments.text = str
            
        }
    }
    
    //PREVIEW
    func showLightbox(media: Media , isVideo:Bool) {
        var data = [LightboxImage]()
        if isVideo{
            guard let imgUrl = URL.init(string: /media.thumbnail) , let videoUrl = URL.init(string: /media.videoUrl) else { return }
            data = [ LightboxImage(
                imageURL: imgUrl ,
                text: "",
                videoURL: videoUrl)
            ]
        }else{
            guard let imgUrl = URL.init(string: /media.thumbnail) else { return }
            data = [LightboxImage(imageURL: imgUrl)]
        }
        
        let controller = LightboxController(images: data )
        controller.dynamicBackground = true
        controller.modalPresentationStyle = .fullScreen
        UIApplication.topViewController()?.presentVC(controller)
        
    }
}

extension HomePostTableCell: UICollectionViewDataSource  , UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return /item?.media?.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell{
        //        pager.currentPage = /indexPath.row
        let identifier = R.reuseIdentifier.selectedImageCollectionViewCell.identifier
        guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: identifier , for: indexPath) as? SelectedImageCollectionViewCell else { return UICollectionViewCell() }
        
        cell.tableRow = /row
        cell.collectionIndex = indexPath.row
        cell.media = item?.media?[indexPath.row]
        cell.selectedIndex = selectedTableRow == /row ? /selectedCollectionIndex : -1
        cell.btnPlayVideo?.tag = indexPath.row
        cell.btnPlayVideo?.addTarget(self, action: #selector(actionPlayVide(_:)), for: .touchUpInside)
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let media = item?.media?[indexPath.row] , let vc = R.storyboard.post.postMediaDetailViewController() else { return }
        
        switch /media.mediaType{
//        case "IMAGE":
//            showLightbox(media: media, isVideo: false)
        default:
            vc.postDetailViewModal.postId.value = /item?.id
            UIApplication.topViewController()?.pushVC(vc)
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        
        switch /item?.media?.count{
        case 1:
            return CGSize(width: UIScreen.main.bounds.width  , height: UIScreen.main.bounds.width)
        case 2:
            return CGSize(width: UIScreen.main.bounds.width/2  , height: UIScreen.main.bounds.width)
        case 3:
            switch indexPath.row {
            case 0:
                return
                    CGSize(width: UIScreen.main.bounds.width/2  , height:  UIScreen.main.bounds.width)
            case 1:
                 return  CGSize(width: UIScreen.main.bounds.width/2  , height: UIScreen.main.bounds.width/2)
            default:
                return CGSize(width: UIScreen.main.bounds.width/2  , height:  UIScreen.main.bounds.width/2)
            }
        case 4:
            return CGSize(width: UIScreen.main.bounds.width/2  , height: UIScreen.main.bounds.width/2)
        default:
            return CGSize(width: collectionView.frame.size.width  , height: collectionView.frame.size.width)
        }
        
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0.0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 0.0
    }
    
    @objc func actionPlayVide(_ sender: UIButton)  {
        print(selectedTableRow)
        selectedTableRow = /row
        selectedCollectionIndex = sender.tag
        collectionView.reloadData()
    }
    
//    func scrollViewDidEndDragging(_ scrollView: UIScrollView, willDecelerate decelerate: Bool) {
//        //        if !decelerate {
//
//        //        }
//    }
//
//    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
//        pager.currentPage = /collectionView.indexPathsForVisibleItems.last?.row
//    }
//
//
//
}
