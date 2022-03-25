//
//  VenueBaseViewController.swift
//  Conversify
//
//  Created by Apple on 27/10/18.
//

import UIKit
import  XLPagerTabStrip
import IBAnimatable

class VenueBaseViewController: UIViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var viewParentContainer: UIView!
    @IBOutlet weak var btnFilter: UIButton!
    @IBOutlet weak var btnMap: UIButton!
    @IBOutlet weak var viewBottomFilter: AnimatableView!
    @IBOutlet weak var viewTopFilter: AnimatableView!
    
    //MARK::- PROPERTIES
    var mapViewController : MapViewController?
    var venueListViewController : VenueViewController?
    
    var isMap : Bool = false
    var venueBaseVM = VenueBaseViewModal()
    
    
    var activeViewController: UIViewController? {
        didSet {
            removeInactiveViewController(oldValue)
            updateActiveViewController()
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        venueListViewController = R.storyboard.venue.venueViewController()
        mapViewController = R.storyboard.venue.mapViewController()
        activeViewController = venueListViewController
    }
    
    @IBAction func btnActionMapFilter(_ sender: UIButton) {
        let tag = sender.tag
        switch tag {
        case 0: //0 - Map , 1 - Filter
            if isMap {
                isMap = false
                btnMap.setImage(R.image.ic_map(), for: .normal)
                btnMap.setTitle("Map", for: .normal)
                //                venueListViewController = R.storyboard.venue.venueViewController()
                activeViewController = venueListViewController
                viewTopFilter.isHidden = true
                viewBottomFilter.isHidden = false
            }else {
                isMap = true
                btnMap.setImage(R.image.ic_list(), for: .normal)
                btnMap.setTitle("List", for: .normal)
                //                mapViewController = R.storyboard.venue.mapViewController()
                mapViewController?.venueData = (venueListViewController?.venueVM.interests.value?.venueNearYou ?? []) + (venueListViewController?.venueVM.interests.value?.yourVenueData ?? [])
                activeViewController = mapViewController
                viewTopFilter.isHidden = false
                viewBottomFilter.isHidden = true
            }
            
        default:
            if isMap {
                mapViewController?.searchBar.text = ""
                 mapViewController?.venueVM.filterOptions()
            }else{
                venueListViewController?.venueVM.filterOptions()
                venueListViewController?.searchBar.text = ""
                self.view.endEditing(true)
            }
            
        }
    }
    
    func scrollToIndex(){
        if venueListViewController?.venueVM.interests.value?.yourVenueData?.count != 0 && venueListViewController?.venueVM.interests.value?.venueNearYou?.count != 0{
            self.venueListViewController?.tableView.scrollToRow(at: IndexPath(row: 0, section: 0), at: .top, animated: true)
        }
        venueListViewController?.tableView.reloadData()
    }
    
}


extension VenueBaseViewController : IndicatorInfoProvider{
    
    func indicatorInfo(for pagerTabStripController: PagerTabStripViewController) -> IndicatorInfo {
        return IndicatorInfo(title: "VENUES" )
    }
}



//MARK: ---- Container Driven Methods
extension VenueBaseViewController {
    
    fileprivate func removeInactiveViewController(_ inactiveViewController: UIViewController?) {
        if let inActiveVC = inactiveViewController {
            inActiveVC.willMove(toParent: nil)
            
            inActiveVC.view.removeFromSuperview()
            
            inActiveVC.removeFromParent()
        }
    }
    
    fileprivate func updateActiveViewController() {
        if let activeVC = activeViewController {
            addChild(activeVC)
            
            activeVC.view.frame = viewParentContainer?.bounds ?? .zero
            viewParentContainer?.addSubview(activeVC.view)
            activeVC.didMove(toParent: self)
        }
    }
}
