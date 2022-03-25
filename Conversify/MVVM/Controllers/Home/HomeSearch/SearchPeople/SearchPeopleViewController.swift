//
//  SearchPeopleViewController.swift
//  Conversify
//
//  Created by Apple on 13/11/18.
//

import UIKit
import Tags
import RxSwift
import RxCocoa
import RxDataSources
import ESPullToRefresh
import EZSwiftExtensions
import IBAnimatable
import moa

class SearchPeopleViewController: BaseRxViewController {
    
    //MARK: - Outlets
    @IBOutlet weak var lblNoDataFound: UILabel!
    @IBOutlet weak var imageProfile: UIImageView!
    @IBOutlet weak var btnNotification: UIButton!
    @IBOutlet weak var lblTitle: UILabel!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var labelIsNotificationExist: AnimatableLabel!
    @IBOutlet weak var viewGo: UIView!
    @IBOutlet weak var btnGoBig: UIButton!
    @IBOutlet weak var btnGoSmall: UIButton!
    @IBOutlet weak var viewTags: TagsView!
    @IBOutlet weak var btnArrow: UIButton!
    @IBOutlet weak var viewTopBar: UIView!
    @IBOutlet weak var btnEnableLocation: UIButton!
    @IBOutlet weak var constraintArrowTop: NSLayoutConstraint!
    @IBOutlet weak var constraintArrowBottom: NSLayoutConstraint!
    @IBOutlet weak var labelSelectedLoc: UILabel!
    @IBOutlet weak var btnSelectLocation: UIButton!
    @IBOutlet weak var sliderRange: UISlider!
    @IBOutlet weak var labelSelectedRange: UILabel!
    
    //MARK::- PROPERTIES
    
    var rippleView: SMRippleView?
    let viewModel = SearchPeopleViewModel()
    
    //MARK::- VIEW CYCLE
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupView()
        print(viewTags.size.height)
    }
    
   
    override func viewWillAppear(_ animated: Bool) {
        let user = Singleton.sharedInstance.loggedInUser
       // imageProfile.kf.setImage(with: URL(string: /user?.img?.original), placeholder: R.image.ic_account(), options: nil, progressBlock: nil, completionHandler: nil)
         //imageProfile.kf.setImage(with: URL(string: /user?.img?.original), placeholder: R.image.ic_account())
        
        
        imageProfile.image(url:  /user?.img?.original ,placeholder: R.image.ic_account() ?? #imageLiteral(resourceName: "ic_account"))
//        imageProfile.kf.setImage(with: )
        CheckPermission.shared.permission(.locationInUse, openSettingsAlert: false) { [weak self] (bool) in
            self?.btnEnableLocation.isHidden = bool
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        if rippleView == nil {
            let fillColor = UIColor(red: 0.0/255.0, green: 105.0/255.0, blue: 225.0/255.0, alpha: 1)
            rippleView = SMRippleView(frame: btnGoBig.frame, rippleColor: fillColor.withAlphaComponent(0.2), rippleThickness: 1, rippleTimer: 1.2, fillColor: fillColor.withAlphaComponent(0.1), animationDuration: 4, parentFrame: CGRect.init(x: 0, y: 0, width: viewGo.bounds.maxY, height: viewGo.bounds.maxY))
            self.viewGo.insertSubview(rippleView ?? UIView() , belowSubview: btnGoBig)
            self.rippleView?.stopAnimation()
        }
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        
        viewModel.isValid.subscribe { [unowned self] (valid) in
            self.btnGoBig.isEnabled = /valid.element
            self.btnGoBig.alpha = /valid.element ? 1.0 : 0.4
            self.btnGoSmall.isEnabled = /valid.element
            self.btnGoSmall.alpha = /valid.element ? 1.0 : 0.4
            }<bag
        
        viewModel.locPicked.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.labelSelectedLoc.text = self?.viewModel.locAddress.value
                }
            })<bag
        
        viewModel.interests.asObservable().subscribe({ [weak self] (interests) in
            self?.viewTags.removeAll()
            self?.viewTags?.append(contentsOf: /interests.element?.map({/$0.category}))
            self?.viewTags?.redraw()
        })<bag
        
        viewModel.items
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: "SearchPeopleTableViewCell", cellType: SearchPeopleTableViewCell.self)) { (row,element,cell) in
                cell.row = row
                cell.item = element
            }<bag
        
        tableView.rx.setDelegate(self)<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.profileVM.userData = self?.viewModel.items.value[/indexPath.element?.row]
            vc.profileVM.userId = self?.viewModel.items.value[/indexPath.element?.row].id
            self?.pushVC(vc)
            }<bag
        
        btnArrow.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let `self` = self else { return }
            self.editInterests(hide: self.btnArrow.isSelected)
        })<bag
        
        btnEnableLocation.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            UIApplication.topViewController()?.view.dim()
            guard let vc = R.storyboard.venue.locationPopViewController() else { return }
            vc.back = { status in
                if status{
                    UIApplication.topViewController()?.view.undim()
                    self?.view.undim()
                    ez.runThisAfterDelay(seconds: 1.0, after: {
                        CheckPermission.shared.openAppSettings()
                    })
                }else{
                    UIApplication.topViewController()?.view.undim()
                    self?.view.undim()
                }
               
                
            }
            UIApplication.topViewController()?.presentVC(vc)
        })<bag
        
        
        btnGoBig.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            if self?.viewModel.interests.value.count == 0 {
                return
            }
            self?.startSearching(self?.btnGoBig)
        })<bag
        
        btnGoSmall.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            if /self?.viewModel.interests.value.count == 0 {
                return
            }
            self?.refreshSearch(self?.btnGoSmall)
        })<bag
        
        btnNotification.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .loggedInUser
            vc.profileVM.userId = /Singleton.sharedInstance.loggedInUser?.id
            UIApplication.topViewController()?.pushVC(vc)
            
        })<bag
        
        btnSelectLocation.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.viewModel.getPlaceDetails()
        })<bag
        
        
    }
    
    //MARK::- ACTIONS
    
    @IBAction func actionSlider(_ sender: UISlider) {
        print(sender.value)
        let range = Int(sender.value)
        viewModel.range = /range
        labelSelectedRange?.text = /range.toString + " mi"
        
    }
    
    //MARK::- VIEW SETUP
    func setupView() {
        if Singleton.sharedInstance.interests?.count != 0{
            viewModel.interests.value = /Singleton.sharedInstance.selectedInterests
        }
        addPaging()
        // required for paging to work properly
        tableView.estimatedRowHeight = UIScreen.main.bounds.height + 100
        tableView.rowHeight = UITableView.automaticDimension
        tableView.isHidden = true
        viewGo.isHidden = false
        viewTopBar.isHidden = false
        btnGoSmall.isHidden = true
        btnArrow.isHidden = true
        viewTags.delegate = self
        viewTags.tagFont =  UIFont.systemFont(ofSize: 14)
        
    }
    
    func editInterests(hide: Bool) {
        btnArrow.isSelected = !hide
        btnArrow.rotate(hide ? 0 : .pi)
        constraintArrowTop.isActive = hide
        constraintArrowBottom.isActive = !hide
        UIView.animate(withDuration: 0.3) {
            self.view.layoutIfNeeded()
        }
    }
    
    @IBAction func startSearching(_ sender: Any?) {
        btnGoBig.isEnabled = false
        rippleView?.startRipple()
        lblTitle.text = "Finding matches..."
        btnGoBig.setImage(R.image.ic_cross_big(), for: .normal)
        viewTopBar.isHidden = true
        ez.runThisAfterDelay(seconds: 3.0) { [weak self] in
            self?.retrievePeople()
        }
        
    }
    
    @IBAction func refreshSearch(_ sender: Any?) {
        lblTitle.text = "Finding matches..."
        self.tableView.es.startPullToRefresh()
    }
    
    func retrievePeople() {
        viewModel.getMutualInterestUsers { [weak self] (completed) in
            guard let `self` = self else { return }
            ez.runThisInMainThread {
                self.lblNoDataFound.isHidden = (self.viewModel.items.value.count != 0)
                self.tableView.es.stopPullToRefresh()
                self.tableView.es.stopLoadingMore()
                self.refreshControl.endRefreshing()
            }
            if self.viewGo.isHidden == true {
                self.editInterests(hide: true)
            }
            else {
                self.viewGo.isHidden = true
                self.tableView.isHidden = false
                self.viewTopBar.isHidden = false
                self.btnGoSmall.isHidden = false
                self.btnArrow.isHidden = false
                self.rippleView?.stopAnimation()
                self.rippleView?.removeFromSuperview()
                self.rippleView = nil
            }
            self.lblTitle.text = ""
        }
    }
}

extension SearchPeopleViewController {
    
    func addPaging(){
        tableView.es.addInfiniteScrolling { [weak self] in
            guard let `self` = self else { return }
            if self.viewModel.loadMore {
                self.viewModel.page = self.viewModel.page + 1
                self.retrievePeople()
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
        
        tableView.es.addPullToRefresh { [weak self] in
            guard let `self` = self else { return }
            self.viewModel.page =  1
            self.retrievePeople()
        }
    }
}


extension SearchPeopleViewController: TagsDelegate {
    
    func tagsTouchAction(_ tagsView: TagsView, tagButton: TagButton) {
        
    }
    
    func tagsLastTagAction(_ tagsView: TagsView, tagButton: TagButton) {
        guard let interestVc = R.storyboard.main.selectInterestsViewController() else { return }
        interestVc.interestsVM.isFromHome = true
        interestVc.interestsVM.selectedInterests = viewModel.interests.value
        interestVc.interestsVM.selectedFilterInterests = { [weak self] (interests) in
            self?.viewModel.interests.value = /interests
        }
        self.present(interestVc, animated: true, completion: nil)
    }
    
}

extension SearchPeopleViewController: UITableViewDelegate , DelegateUpdateConvoId {
    
    func updateConvoId(row: Int, convoId: String){
        viewModel.items.value[row].conversationId = convoId
    }
}

extension UIView {
    
    func rotate(_ toValue: CGFloat, duration: CFTimeInterval = 0.2) {
        let animation = CABasicAnimation(keyPath: "transform.rotation")
        animation.toValue = toValue
        animation.duration = duration
        animation.isRemovedOnCompletion = false
        animation.fillMode = CAMediaTimingFillMode.forwards
        self.layer.add(animation, forKey: nil)
    }
    
}
