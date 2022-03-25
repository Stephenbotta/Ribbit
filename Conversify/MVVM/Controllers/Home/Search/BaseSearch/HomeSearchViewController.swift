//
//  HomeSearchViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 03/01/19.
//

import UIKit
import XLPagerTabStrip
import RxSwift
import RxCocoa
import JJFloatingActionButton
import Dimmer

class HomeSearchViewController: ButtonBarPagerTabStripViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnCancel: UIButton!
    @IBOutlet weak var textFieldSearch: UITextField!
    
    //MARK::- PROPERTIES
    let bag = DisposeBag()
    let topVc = R.storyboard.search.searchTopViewController()
    let tagsVc = R.storyboard.search.searchTagsViewController()
    let postsVc = R.storyboard.search.searchPostViewController()
    let groupVc = R.storyboard.search.searchGroupViewController()
    let venueVc = R.storyboard.search.searchVenueViewController()
    
    var searchText = Variable<String?>(nil)
    
    override func viewDidLoad() {
        controllerSetUp()
        super.viewDidLoad()
        bind()
    }
    
    // MARK: - PagerTabStripDataSource
    
    override func viewControllers(for pagerTabStripController: PagerTabStripViewController) -> [UIViewController] {
        guard let topVc = topVc , let tagsVc = tagsVc , let postsVc = postsVc  , let groupVc = groupVc , let venueVc = venueVc  else { return [] }
        return [topVc , tagsVc , postsVc  ]
    }
    
    
    func controllerSetUp(){
        
        self.settings.style.buttonBarItemBackgroundColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 0)
        self.settings.style.buttonBarBackgroundColor = #colorLiteral(red: 0, green: 0, blue: 0, alpha: 0)
        self.settings.style.selectedBarBackgroundColor = #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1)
        self.settings.style.selectedBarHeight = 2
        self.settings.style.buttonBarItemsShouldFillAvailableWidth  = true
        
        changeCurrentIndexProgressive = { [weak self] (oldCell: ButtonBarViewCell?, newCell: ButtonBarViewCell?, progressPercentage: CGFloat, changeCurrentIndex: Bool, animated: Bool) -> Void in
            guard changeCurrentIndex == true else { return }
            
            self?.textFieldSearch.text = ""

            oldCell?.label.font = UIFont.systemFont(ofSize: 14)
            newCell?.label.font = UIFont.systemFont(ofSize: 14)
            oldCell?.label.textColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 0.5)
            newCell?.label.textColor = #colorLiteral(red: 1, green: 0.8745098039, blue: 0.09411764706, alpha: 1)
        }
    }
    
    func bind(){
        (textFieldSearch.rx.text <-> searchText)<bag
        self.searchText.asObservable().subscribe { [weak self] (event) in
            self?.updateSearches(text: /self?.searchText.value)
        }
        
        btnBack.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
        btnCancel.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)
            self?.textFieldSearch.text = ""
            self?.updateSearches(text: "")
        })<bag
        
        
    }
    
    
}

extension HomeSearchViewController{
    
    func updateSearches(text: String?){
        switch /self.currentIndex {
        case 0://top
            topVc?.searchTopVM.text.value = /text
            topVc?.searchTopVM.page = 1
            topVc?.searchTopVM.getPeople()
        case 1://tags
            tagsVc?.searchTopVM.text.value = /text
            tagsVc?.searchTopVM.page = 1
            tagsVc?.searchTopVM.getTags()
        case 2://posts
            postsVc?.searchPostVM.page = 1
            postsVc?.searchPostVM.text.value = /text
            postsVc?.searchPostVM.getPosts()
            
        case 3://groups
            groupVc?.searchGroupVM.text.value = /text
            groupVc?.searchGroupVM.page = 1
            groupVc?.searchGroupVM.getGroups()
        default://venue
            venueVc?.searchVenueVM.text.value = /text
            venueVc?.searchVenueVM.pageV = 1
            venueVc?.searchVenueVM.getVenues()
        }
    }
    
}
