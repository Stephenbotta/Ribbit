//
//  ConverseNearByViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 15/01/19.
//

import UIKit
import Tags
import Sheeeeeeeeet
import OpalImagePicker
import Photos
import IBAnimatable
import DropDown

class ConverseNearByViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var constraintHeightCollectionView: NSLayoutConstraint!
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var btnSelectImage: UIButton!
    @IBOutlet weak var textView: UITextView!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var imageSelected: UIImageView!
    @IBOutlet weak var btnNext: UIButton!
    @IBOutlet weak var labelSelectCategory: UILabel!
    @IBOutlet weak var btnPrivacy: UIButton!
    // @IBOutlet weak var viewTags: TagsView!
    @IBOutlet weak var labelPostingIn: UILabel!
    @IBOutlet weak var btnLocation: UIButton!
    @IBOutlet weak var labelLocation: UILabel!
    @IBOutlet weak var labelUserName: UILabel!
    @IBOutlet weak var userImage: UIImageView!
    @IBOutlet weak var btnPublicPosting: UIButton!
    @IBOutlet weak var btnFollowerPosting: UIButton!
    @IBOutlet weak var viewPostingOptions: AnimatableView!
    @IBOutlet weak var lblPostingType: UILabel!
    @IBOutlet weak var viewIntrestTblContainer: AnimatableView!
    @IBOutlet weak var tblViewInterst: UITableView!
    @IBOutlet weak var btnInterst: UIButton!
    @IBOutlet weak var lblInterest: UILabel!
    
    
    //MARK::- PROPERTIES
    var converseVM = ConverseNearByViewModal()
    var mediaPickerVC: MediaPickerController?
   
    var interestsVM = SelectInterestsViewModal()
    var intrestArray  = [Interests]()
    var selectedInterestArray = [Interests]()
    var dropDown = DropDown()
    var dropDown2 = DropDown()
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    //MARK::- BINDINGS
    
    override func bindings() {
        converseVM.createdSuccessFully.filter { (_) -> Bool in
            return true
        }.subscribe(onNext: { [weak self] (bool) in
            if bool {
                self?.popToHome()
            }
        })<bag
       
        btnLocation.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.converseVM.getPlaceDetails()
        })<bag
        
        converseVM.gotLocation.filter { (_) -> Bool in
            return true
        }.subscribe(onNext: { [weak self] (bool) in
            if bool {
                self?.labelLocation?.text = /self?.converseVM.locName.value
            }
        })<bag
        
        converseVM.assets
            .asObservable()
            .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.selectedImageCollectionViewCell.identifier, cellType: SelectedImageCollectionViewCell.self)) { [weak self] (row,element,cell) in
                cell.tableRow = row
                cell.isFromPost = true
                if let elem = element as? PHAsset{
                    cell.asset = elem
                }

              
                cell.imageCross?.tag = row
                cell.imageCross?.onTap({ (gesture) in
                    self?.converseVM.assets.value.remove(at: /cell.imageCross?.tag)
                })
            }<bag
        
        collectionView.rx.setDelegate(self)<bag
        (textView.rx.text <-> converseVM.postText)<bag
        
        converseVM.isValid.subscribe { [weak self] (valid) in
            self?.btnNext.isEnabled = /valid.element
            self?.btnNext.alpha = /valid.element ? 1.0 : 0.4
            }<bag

        
        btnSelectImage.rx.tap.asDriver().drive(onNext: {  [weak self] () in
   
            DispatchQueue.main.async {
                CheckPermission.shared.permission(.camera, completion: { (status) in
                    if status{
                        CheckPermission.shared.permission(.photos, completion: { (status) in
                            if status{
                                self?.openMediaPicker(isCam: true, isGal: true)
                            }else{
                                self?.openMediaPicker(isCam: true, isGal: false)
                            }
                        })
                    }else{
                        CheckPermission.shared.permission(.photos, completion: { (status) in
                            if status{
                                self?.openMediaPicker(isCam: false, isGal: true)
                            }else{
                                return
                            }
                        })
                    }
                    
                })
            }
            
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.popVC()
        })<bag
        
        btnNext.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            if /self?.converseVM.assets.value.count <= 4 {
                
            }else{
                UtilityFunctions.makeToast(text: "Media count should be less than 5 ", type: .error)
                return
            }
  
            Loader.shared.start()
            self?.converseVM.post()
     
        })<bag
        
        btnPrivacy.rx.tap.asDriver().drive(onNext: {  [unowned self] () in
            self.view.endEditing(true)
            self.setUpPolicies()

            
        })<bag
        
        btnPublicPosting.rx.tap.asDriver().drive(onNext: {  [unowned self] () in
            self.view.endEditing(true)
            self.viewPostingOptions.isHidden = true
            self.publicPosting()
            //            let actionSheet = self.showStandardActionSheet()
            //            actionSheet.present(in: self, from: self.btnPrivacy)
            
        })<bag
        btnFollowerPosting.rx.tap.asDriver().drive(onNext: {  [unowned self] () in
            self.view.endEditing(true)
            self.viewPostingOptions.isHidden = true
            
            self.followerPosting()
            //            let actionSheet = self.showStandardActionSheet()
            //            actionSheet.present(in: self, from: self.btnPrivacy)
            
        })<bag
        btnInterst.rx.tap.asDriver().drive(onNext: {  [unowned self] () in
            self.view.endEditing(true)
            
            let intrsts = self.intrestArray.map({/$0.category?.capitalizedFirst()})
            self.setUpDropDown(list: intrsts)
            // self.setUpDropDown()
            //            self.viewIntrestTblContainer.isHidden = !self.viewIntrestTblContainer.isHidden
            //            self.viewPostingOptions.isHidden = true
            //            let actionSheet = self.showStandardActionSheet()
            //            actionSheet.present(in: self, from: self.btnPrivacy)
            
        })<bag
        labelPostingIn.addTapGesture { [unowned self] (gesture) in
            self.view.endEditing(true)
            let actionSheet = self.showStandardActionSheet()
            actionSheet.present(in: self, from: self.btnPrivacy)
        }
        
        
        
    }
    
    //MARK::- FUNCTIONS
    func onLoad(){
        let userData = Singleton.sharedInstance.loggedInUser
        userImage.setImage(image: /userData?.img?.thumbnail)
        labelUserName.text = /userData?.firstName
        //imagePicker.allowedMediaTypes = Set([PHAssetMediaType.image , PHAssetMediaType.video])
        textView.delegate = self
        //        labelSelectCategory.isHidden = converseVM.type == 2
        //        viewTags.isHidden = converseVM.type == 2
        converseVM.interests.value = Singleton.sharedInstance.selectedInterests ?? []
        loadInterests()
        //  viewTags.delegate = self
        interestsVM.retrieveInterests { (status) in
            
            self.intrestArray = self.interestsVM.interests.value
            self.tblViewInterst.reloadData()
        }
        displayInterest()
    }
    
    func loadInterests(){
        selectedInterestArray = /converseVM.interests.value
        print(selectedInterestArray)
        //     viewTags.removeAll()
        //    viewTags?.append(contentsOf: /converseVM.interests.value.map({/$0.category}))
        //  viewTags?.redraw()
    }
    
    
    func publicPosting(){
        
        self.converseVM.postingIn = "PUBLICILY"
        lblPostingType.text = "Publicily"
        //self.btnPrivacy.setImage(R.image.ic_public(), for: .normal)
        //self.btnPrivacy.setTitle("Publicily", for: .normal)
    }
    
    
    func followerPosting(){
        
        self.update(selected: .posting)
    }
    
    
    func showStandardActionSheet() -> ActionSheet {
        
        let item1 = MenuItem(title: "Publicily" , value: 1 , image: R.image.ic_public()?.tint(with: #colorLiteral(red: 0.2549019754, green: 0.2745098174, blue: 0.3019607961, alpha: 1)))
        let item2 = MenuItem(title: "Followers/Selected People" , value: 2 , image: R.image.ic_group()?.tint(with: #colorLiteral(red: 0.2549019754, green: 0.2745098174, blue: 0.3019607961, alpha: 1)))
        
        return ActionSheet(menu: Menu(title: "Select a type", items: [item1, item2])) { [weak self] sheet, item in
            self?.view.endEditing(true)
            if let value = item.value as? Int {
                switch /value{
                case 1:
                    self?.converseVM.postingIn = "PUBLICILY"
                    self?.btnPrivacy.setImage(R.image.ic_public(), for: .normal)
                    self?.btnPrivacy.setTitle("Publicily", for: .normal)
                case 2:
                    self?.update(selected: .posting)
                    
                default:
                    break
                }
            } 
        }
        
    }
    
    
    func update(selected: PrivacySelectedWhoCanSeeFlags?){
        guard let vc = R.storyboard.settings.followerViewController() else { return }
        vc.isFollower = nil
        vc.followerVM = FollowersViewModal(selectedPrivacy: selected)
        vc.followerVM.isProfile = false
        vc.followerVM.usersSelectedForPostModules.value = converseVM.selectedPeople.value
        vc.followerVM.selectedUsers = { [weak self] users in
            if /users.count != 0{
                self?.converseVM.selectedPeople.value = users
                self?.converseVM.postingIn = "SELECTED_PEOPLE"
                // self?.btnPrivacy.setImage(R.image.ic_friendsExcept(), for: .normal)
                // self?.btnPrivacy.setTitle( /users.count.toString +  " people", for: .normal)
                self?.lblPostingType.text = /users.count.toString +  " people"
            }
        }
        vc.followerVM.onlyFollowers = { [weak self] in
            self?.converseVM.selectedPeople.value = []
            self?.converseVM.postingIn = "FOLLOWERS"
            //self?.btnPrivacy.setImage(R.image.ic_group(), for: .normal)
            //self?.btnPrivacy.setTitle("Followers", for: .normal)
            self?.lblPostingType.text = "Followers"
        }
        self.pushVC(vc)
    }
    
    func showInterests(){
        guard let interestVc = R.storyboard.main.selectInterestsViewController() else { return }
        interestVc.interestsVM.isFromHome = true
        interestVc.interestsVM.selectedInterests = self.converseVM.interests.value ?? []
        interestVc.interestsVM.selectedFilterInterests = { [weak self] (interests) in
            self?.converseVM.interests.value = interests ?? []
            self?.loadInterests()
        }
        self.present(interestVc, animated: true, completion: nil)
    }
    
    func displayInterest(){
        
        self.converseVM.interests.value = selectedInterestArray
        var interestStr = ""
        for intrestStr in selectedInterestArray{
            
            if interestStr == ""{
                interestStr = intrestStr.category?.capitalizedFirst() ?? ""
            }
            else{
                
                interestStr = interestStr + "," + (intrestStr.category?.capitalizedFirst() ?? "")
            }
            
        }
        
        lblInterest.text = interestStr
        
    }
}


extension ConverseNearByViewController: TagsDelegate {
    
    func tagsTouchAction(_ tagsView: TagsView, tagButton: TagButton) {
        
    }
    
    func tagsLastTagAction(_ tagsView: TagsView, tagButton: TagButton) {
        showInterests()
    }
    
}

extension ConverseNearByViewController : MediaPickerControllerDelegate {
    
    func getImage(){
        
        
    }
    
    /** Pick photo **/
    func openMediaPicker(isCam:Bool , isGal:Bool) {
        
        var senders = [String]()
        if isCam{
            senders.append("Camera")
        }
        if isGal{
            senders.append("Gallery")
            
        }
        
        DispatchQueue.main.async {
            UtilityFunctions.show(nativeActionSheet: "", subTitle: "", vc: self, senders: senders) { [unowned self] (value, index) in
                switch /(value as? String){
                case "Camera":
                    self.mediaPickerVC = MediaPickerController(type: .imageAndVideo , presentingViewController: self)
                    self.mediaPickerVC?.isAllowExisting = false
                    self.mediaPickerVC?.delegate = self
                    self.mediaPickerVC?.show()
                default:
                    self.presentOpal()
                }
            }
        }
        
        
    }
    
    func presentOpal(){
        let imagePicker = OpalImagePickerController()
        imagePicker.allowedMediaTypes = Set([PHAssetMediaType.image , PHAssetMediaType.video])
        presentOpalImagePickerController(imagePicker, animated: true, select: { [weak self] (assets)  in
            //Save Images, update UI
            print(assets)
            assets.forEach({ (asset) in
                //                self?.converseVM.assetsPH.value.append(asset)
                self?.converseVM.assets.value.append(asset)
            })
            
            
            assets.forEach({ (asst) in
                
                
                
                if asst.mediaType == PHAssetMediaType.image{
                    if(asst.mediaSubtypes == PHAssetMediaSubtype.photoPanorama){
                        // this is a Live Photo
                    }
                    
                    if #available(iOS 9.1, *) {
                        if(asst.mediaSubtypes == PHAssetMediaSubtype.photoLive){
                            // this is a Time-lapse
                        }
                    } else {
                        // Fallback on earlier versions
                    }
                    
                    if(asst.mediaSubtypes == PHAssetMediaSubtype.videoTimelapse){
                        
                    }
                }else if asst.mediaType == PHAssetMediaType.video{
                    
                } else if asst.mediaType == PHAssetMediaType.audio{
                    
                }
            })
            
            //Dismiss Controller
            imagePicker.dismiss(animated: true, completion: nil)
            }, cancel: {
                //Cancel action?
                
        })
    }
    
    func mediaPickerControllerDidPickImage(_ image: UIImage) {
        //        converseVM.cameraImages.value.append(image)
        converseVM.assets.value.append(image)
    }
    
    func mediaPickerControllerDidPickVideo(url: URL, data: Data, thumbnail: UIImage){
        let video = VideoData()
        video.thumbnail = thumbnail
        video.data = data
        converseVM.assets.value.append(video)
        //        converseVM.videos.value.append(video)
    }
    
    func mediaPickerControllerDidPickAsset(asset:PHAsset?){
        print(asset)
        //        converseVM.assets.value.append(asset)
    }
    
    
    func setUpDropDown( list : [String]){
        
        dropDown.anchorView = btnInterst
//        let selectedIntrst = selectedInterestArray.map({/$0.category?.capitalizedFirst()})
//        if selectedIntrst.count > 0 {
//            dropDown.sele = selectedIntrst
//        }
        DropDown.appearance().selectionBackgroundColor = #colorLiteral(red: 0.8431372549, green: 0.8431372549, blue: 0.8431372549, alpha: 1)
        DropDown.appearance().selectedTextColor = #colorLiteral(red: 0.3333333433, green: 0.3333333433, blue: 0.3333333433, alpha: 1)
        dropDown.multiSelectionAction = { [weak self] (index: [Int], item: [String]) in
            
            self?.selectedInterestArray.removeAll()
            item.forEachEnumerated { (_, category) in
                let selectedIntrst = self?.intrestArray.first(where: {$0.category?.capitalizedFirst() == category})
                self?.selectedInterestArray.append(selectedIntrst ?? Interests())
            }
            self?.displayInterest()
        }
        dropDown.direction = .bottom
        dropDown.isMultipleTouchEnabled = true
        dropDown.dataSource = list
        dropDown.dismissMode = .onTap
        dropDown.show()
    }
    
    
    func setUpPolicies(){
        dropDown2.clearSelection()
        dropDown2.selectionAction = { [weak self] (index: Int, item: String) in
        
            switch index{
            case 0:
                self?.converseVM.postingIn = "PUBLICILY"
                self?.lblPostingType.text = item
//                self?.btnPrivacy.setImage(R.image.ic_public(), for: .normal)
//                self?.btnPrivacy.setTitle("Publicily", for: .normal)
            case 1:
//                self?.dropDown2.hide()
                self?.update(selected: .posting)
            default:
                break
            }
        }
        dropDown2.anchorView = btnPrivacy
        dropDown2.direction  = .bottom
        dropDown2.dismissMode = .onTap
        dropDown2.dataSource = ["Publicily" ,"Followers/Selected People"]
        dropDown2.show()
    }
}


extension ConverseNearByViewController: UITextViewDelegate {
    
    func textViewDidBeginEditing(_ textView: UITextView) {
        if textView == textView{
            textView.borderColor = #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1)
        }else{
            textView.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
    
    func textViewDidEndEditing(_ textView: UITextView) {
        if textView == textView{
            textView.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }else{
            textView.layer.borderColor = #colorLiteral(red: 0.9279510379, green: 0.9279728532, blue: 0.9279610515, alpha: 1)
        }
    }
}

//MARK: - Collection View Delegates
extension ConverseNearByViewController: UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: 80 , height:80)
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 4
    }    
}

extension ConverseNearByViewController:UITableViewDataSource,UITableViewDelegate{
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.intrestArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = tblViewInterst.dequeueReusableCell(withIdentifier: "IntrestTblCell") as! IntrestTblCell
        cell.interest = self.intrestArray[indexPath.row]
        
        if selectedInterestArray.contains(where: {$0.id == self.intrestArray[indexPath.row].id}) {
            cell.imgView.image = UIImage(named:"ic_checkbox_active")
        } else {
            cell.imgView.image = nil
        }
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 35
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if let index = selectedInterestArray.firstIndex(where: {$0.id == self.intrestArray[indexPath.row].id}){
            selectedInterestArray.remove(at: index)
        } else {
            selectedInterestArray.append(self.intrestArray[indexPath.row])
        }
        displayInterest()
        self.tblViewInterst.reloadData()
    }
}
