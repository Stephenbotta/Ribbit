//
//  SocialInteractionViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit
import XLPagerTabStrip
import RxSwift
import RxCocoa
import JJFloatingActionButton
import Dimmer

class SocialInteractionViewController: ButtonBarPagerTabStripViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnAdd: UIButton!
    
    //MARK::- PROPERTIES
    var socialInteractionVM = SocialInteractionViewModal()
    var goupVc = R.storyboard.home.groupViewController()
//    var goupVc = R.storyboard.venue.venueBaseViewController()
    var venueVc = R.storyboard.venue.venueBaseViewController()
    var peopleVc = R.storyboard.people.peopleViewController()
    let bag = DisposeBag()
    let actionButton = JJFloatingActionButton()
    let createVenue = R.storyboard.venue.createVenueViewController()
    let filterGroupVC = R.storyboard.groups.groupFilterViewController()
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        controllerSetUp()
        super.viewDidLoad()
        self.bindings()
        onLoad()
        addFloating()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.view.undim()
        bringToFront()
        refreshActionButtons(index: currentIndex)
    }
    
    
    
    
    // MARK: - PagerTabStripDataSource
    
    override func viewControllers(for pagerTabStripController: PagerTabStripViewController) -> [UIViewController] {
        guard let gVc = goupVc , let vVc = venueVc , let pVc = peopleVc else { return [] }
        return [gVc ]
    }
    
    func bindings(){
        LocationManager.sharedInstance.startTrackingUser()
        btnAdd.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.handleAddExplore()
        })<bag
    }
    
    
}

//MARK::- FUNCTIONS
extension SocialInteractionViewController  {
    
    func refreshActionButtons(index: Int){
        switch index{
        case 0:
            actionButton.isHidden = false
            btnAdd.isHidden = true
        case 1:
            actionButton.isHidden = true
            btnAdd.isHidden = false
        default:
            actionButton.isHidden = true
            btnAdd.isHidden = true
        }
    }
    
    func controllerSetUp(){
        
        self.settings.style.buttonBarItemBackgroundColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
        self.settings.style.buttonBarBackgroundColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
        self.settings.style.selectedBarBackgroundColor = #colorLiteral(red: 0.9999960065, green: 1, blue: 1, alpha: 1)
        self.settings.style.selectedBarHeight = 0
        self.settings.style.buttonBarItemsShouldFillAvailableWidth  = false
        
        
        changeCurrentIndexProgressive = { [weak self] (oldCell: ButtonBarViewCell?, newCell: ButtonBarViewCell?, progressPercentage: CGFloat, changeCurrentIndex: Bool, animated: Bool) -> Void in
            guard changeCurrentIndex == true else { return }
            
            oldCell?.label.font = UIFont.boldSystemFont(ofSize: 16)
            newCell?.label.font = UIFont.boldSystemFont(ofSize: 16)
            oldCell?.label.textColor = #colorLiteral(red: 0.2901960784, green: 0.337254902, blue: 0.4235294118, alpha: 1)
            newCell?.label.textColor = #colorLiteral(red: 0.9999960065, green: 1, blue: 1, alpha: 1)
            
            self?.refreshActionButtons(index: self!.currentIndex)
//            self?.refreshActionButtons(index: changeCurrentIndex)
        }
    }
    
    func onLoad(){
        btnAdd.isHidden = true
        
    }
    
    
    func bringToFront(){
        self.view.bringSubviewToFront(buttonBarView)
        self.view.bringSubviewToFront(actionButton)
    }
    
    func handleAddExplore(){
        print(self.currentIndex)
        switch currentIndex{
        case 0://group
            break
        case 1://venue
            addNewVenue()
        default://people
            break
        }
    }
    
    func addNewVenue(){
        guard let createVenue = createVenue else { return }
        createVenue.createVenueVM = CreateVenueViewModal(isVenueNav: true)
        self.pushVC(createVenue)
    }
    
    func addNewGroup(){
        guard let createVenue = createVenue else { return }
        createVenue.createVenueVM = CreateVenueViewModal(isVenueNav: false)
        self.pushVC(createVenue)
        
    }
    
    func filterByTopics(){
        self.view.dim()
        guard let filterGroupVC = filterGroupVC else { return }
        filterGroupVC.createVenueVM.back = { [ weak self] in
            self?.view.undim()
        }
        
        filterGroupVC.createVenueVM.backWithInterest = { [ weak self] category in
            guard let vc = R.storyboard.groups.groupTopicViewController() else { return }
            vc.groupTopicVM = GroupTopicViewModal(categor: category)
            vc.groupTopicVM.refresh = { [weak self] in
                (self?.viewControllers.first as? GroupViewController)?.groupVM.retrieveGroups(ifRefresh: true)
            }
            self?.pushVC(vc)
        }
        self.present(filterGroupVC, animated: false, completion: nil)
    }
    
    func addFloating(){
        actionButton.buttonImage = R.image.plus()
        actionButton.buttonColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 0)
        
        actionButton.addItem(title: "Add New Network", image: UIImage(named: "ic_add_noBg")?.withRenderingMode(.alwaysTemplate)) { item in
            self.addNewGroup()
        }
        
        actionButton.addItem(title: "Topics", image: UIImage(named: "ic_grid_noBg")?.withRenderingMode(.alwaysTemplate)) { item in
            self.filterByTopics()
        }
        
        actionButton.display(inViewController: self)
        actionButton.translatesAutoresizingMaskIntoConstraints = false
        
    }
    
    
    
}
