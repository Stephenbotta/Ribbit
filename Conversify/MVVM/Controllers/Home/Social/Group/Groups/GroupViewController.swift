//
//  GroupViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit
import XLPagerTabStrip

class GroupViewController: BaseRxViewController {
    
    //MARK: - OUTLETS
    @IBOutlet weak var btnCancelSearch: UIButton!
    @IBOutlet weak var constraintWidthButtonCancel: NSLayoutConstraint!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var labelSuggestedGroup: UILabel!
    @IBOutlet weak var labelYourGroup: UILabel!
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var searchBar: UISearchBar!{
        didSet{
            searchBar.delegate = self
        }
    }
    @IBOutlet weak var constraintHeightHeaderView: NSLayoutConstraint!
    @IBOutlet weak var viewNoData: UIView!
    
    //MARK::- PROPERTY
    var groupVM = GroupViewModal()
    
    
    //MARK: - View Hierarchy
    override func viewDidLoad() {
        super.viewDidLoad()
        groupVM.retrieveGroups()
        onLoad()
    }
    
    override func bindings() {
        
        groupVM.joinedSuccesfullyPrivate.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.groupVM.retrieveGroups(ifRefresh:true)
                }
            })<bag
        
        
        groupVM.joinedSuccesfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.detailGroup(group: self?.groupVM.selectedGroup.value)
                }
            })<bag
        
        groupVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        groupVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.searchBar.alpha = (self?.groupVM.yourGroups.value.count == 0 && self?.groupVM.suggestedGroups.value.count == 0) ? 0.5 : 1.0
                    self?.searchBar.isUserInteractionEnabled = !((self?.groupVM.yourGroups.value.count == 0 && self?.groupVM.suggestedGroups.value.count == 0))
                    self?.refreshUI()
                }
            })<bag
        
        tableView.estimatedRowHeight = 64
        tableView.rowHeight = 64
        
        btnCancelSearch.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.searchBar.text = ""
            self?.constraintWidthButtonCancel.constant = 0
            self?.view.layoutIfNeeded()
            self?.groupVM.yourGroups.value = self?.groupVM.groups.value?.yourGroups ?? []
            self?.groupVM.suggestedGroups.value = self?.groupVM.groups.value?.suggestedGroups ?? []
            self?.refreshUI()
        })<bag
        
        groupVM.yourGroups
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupListingTableViewCell.identifier, cellType: GroupListingTableViewCell.self)) { (row,element,cell) in
                cell.groups = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.view.endEditing(true)
            let groupVc = R.storyboard.groups.groupDiscussionViewController()
            let index_path = /indexPath.element
            groupVc?.groupDiscussVM = GroupDiscussionViewModal(groupD: self?.groupVM.yourGroups.value[index_path.row])
            groupVc?.groupDiscussVM.backRefresh = { [ weak self] in
                self?.groupVM.retrieveGroups(ifRefresh: false)
            }
            self?.pushVC(groupVc ?? UIViewController() )
            }<bag
        
        tableView.rx.setDelegate(self)<bag
        
        groupVM.suggestedGroups.asObservable()
            .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.suggestedGroupCollectionViewCell.identifier, cellType: SuggestedGroupCollectionViewCell.self)) {  (row,element,cell) in
                cell.suggested = element
                cell.row = row
                cell.likeTapped = { [weak self] (row , state)  in
                    self?.groupVM.joinGroup( row: row)
                }
            }<bag
        
        collectionView.rx.setDelegate(self)<bag
        
        groupVM.retrieveSuccessfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                self?.labelYourGroup.isHidden = /self?.groupVM.yourGroups.value.count == 0
            })<bag
        
    }
    
    //MARK: - Setup UI
    func onLoad() {
        refreshCalled = { [weak self] in
            self?.groupVM.retrieveGroups()
            self?.searchBar.text = ""
            self?.view.endEditing(true)
            self?.refreshControl.endRefreshing()
        }
    }
    
    func detailGroup(group: YourGroup?){
        let groupVc = R.storyboard.groups.groupDiscussionViewController()
        groupVc?.groupDiscussVM = GroupDiscussionViewModal(groupD: group)
        groupVc?.groupDiscussVM.backRefresh = { [ weak self] in
            self?.groupVM.retrieveGroups(ifRefresh: true)
        }
        self.pushVC(groupVc ?? UIViewController() )
    }
    
    func refreshUI(){
        DispatchQueue.main.async { [weak self] in
            self?.constraintHeightHeaderView.constant = self?.groupVM.suggestedGroups.value.count == 0 ? 0 : 162
            self?.tableView.tableHeaderView?.frame = self?.groupVM.suggestedGroups.value.count == 0 ? CGRect(x: 0, y: 0, w: UIScreen.main.bounds.width, h: 51) : CGRect(x: 0, y: 0, w: UIScreen.main.bounds.width, h: 223)
            self?.viewNoData.isHidden = self?.groupVM.yourGroups.value.count != 0
            self?.labelSuggestedGroup.isHidden = self?.groupVM.suggestedGroups.value.count == 0
            self?.refreshControl.endRefreshing()
            self?.tableView.layoutIfNeeded()
            self?.tableView.reloadData()
            self?.collectionView.reloadData()
            self?.labelSuggestedGroup.text = self?.groupVM.suggestedGroups.value.count == 0 ? "" : "SUGGESTED NETWORKS"
            self?.labelYourGroup.text = self?.groupVM.yourGroups.value.count != 0 ? "YOUR NETWORKS" : ""
            
            
        }
    }
    
}

//MARK::- XLPAGER
extension GroupViewController : IndicatorInfoProvider{
    
    func indicatorInfo(for pagerTabStripController: PagerTabStripViewController) -> IndicatorInfo {
        return IndicatorInfo(title: "NETWORKS" )
    }
}

//MARK::- SEARCH
extension GroupViewController: UISearchBarDelegate {
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        let txt = searchText.lowercased().trimmingCharacters(in: .whitespaces)
        constraintWidthButtonCancel.constant = 60
        self.view.layoutIfNeeded()
        if txt.isEmpty {
            self.labelYourGroup.text = "YOUR NETWORKS"
            self.labelSuggestedGroup.text = "SUGGESTED NETWORKS"
            self.groupVM.yourGroups.value = self.groupVM.groups.value?.yourGroups ?? []
            self.groupVM.suggestedGroups.value = self.groupVM.groups.value?.suggestedGroups ?? []
        }else {
            let searchedYourGroups = /groupVM.groups.value?.yourGroups?.filter({ (/$0.groupName?.lowercased().contains(txt))})
            let searchedSuggestedGroups = /groupVM.groups.value?.suggestedGroups?.filter({ (/$0.groupName?.lowercased().contains(txt))})
            self.groupVM.yourGroups.value = searchedYourGroups
            self.groupVM.suggestedGroups.value = searchedSuggestedGroups
            self.refreshUI()
        }
        tableView.reloadData()
    }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        self.groupVM.yourGroups.value = self.groupVM.groups.value?.yourGroups ?? []
        self.groupVM.suggestedGroups.value = self.groupVM.groups.value?.suggestedGroups ?? []
        tableView.reloadData()
        self.view.endEditing(true)
    }
    
}

extension GroupViewController: UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.groupVM.joinGroup( row: indexPath.row)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: 160  , height: 112)
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 8.0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 8.0
    }
    
}
