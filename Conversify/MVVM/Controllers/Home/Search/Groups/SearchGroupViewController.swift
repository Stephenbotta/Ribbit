//
//  SearchGroupViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 03/01/19.
//

import UIKit
import XLPagerTabStrip

class SearchGroupViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var labelHeader: UILabel!
    
    
    //MARK::- PROPERTIES
    var searchGroupVM = GroupViewModal()
    
    //MARK::- BINDINGS
    
    //MARK: - View Hierarchy
    override func viewDidLoad() {
        super.viewDidLoad()
//        addPaging()
        searchGroupVM.getGroups()
    }
    
    override func bindings() {
        
        searchGroupVM.resetTable.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (status) in
                if status{
                    self?.tableView.es.resetNoMoreData()
                }
            })<bag

        searchGroupVM.noMoreData.filter { [weak self] (_) -> Bool in
            self?.tableView.es.noticeNoMoreData()
            self?.tableView.es.stopPullToRefresh()
            return true
            }.subscribe(onNext: { [weak self] (status) in
                if status{
                    self?.tableView.es.noticeNoMoreData()
                    self?.tableView.es.stopPullToRefresh()
                }
            })<bag
        
        searchGroupVM.joinedSuccesfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.detailGroup(group: self?.searchGroupVM.selectedGroup.value)
                }
            })<bag
        
        
        tableView.estimatedRowHeight = 56
        tableView.rowHeight = 56
        searchGroupVM.suggestedGroups
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupListingTableViewCell.identifier, cellType: GroupListingTableViewCell.self)) { (row,element,cell) in
                cell.group = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.view.endEditing(true)
            let group = self?.searchGroupVM.suggestedGroups.value[/indexPath.element?.row]
            let myGroup = YourGroup(id: group?.id, createOn: 0, groupName: group?.groupName, image: group?.imageUrl, adminId: group?.adminId, conversationId: group?.conversationId, memberList: [], notification: false , memberCounts: /group?.memberCounts, createdby: /group?.createdBy, desc: group?.description, isPrivate: /group?.isPrivate, unReadCount: 0)
            self?.searchGroupVM.selectedGroup.value = myGroup
            if !(/self?.searchGroupVM.suggestedGroups.value[/indexPath.element?.row].isMember){
                self?.searchGroupVM.joinGroup( row: /indexPath.element?.row)
            }else{
                self?.detailGroup(group: self?.searchGroupVM.selectedGroup.value)
            }
            
            }<bag
       
    }
    
    func addPaging(){
        tableView.es.addInfiniteScrolling {
            
            if /self.searchGroupVM.loadMore {
                self.searchGroupVM.page = self.searchGroupVM.page + 1
                self.searchGroupVM.getGroups()
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
    }
    
    //MARK::- GET DETAIL
    func detailGroup(group: YourGroup? , index: Int? = 0){
        let groupVc = R.storyboard.groups.groupDiscussionViewController()
        groupVc?.groupDiscussVM = GroupDiscussionViewModal(groupD: group)
        groupVc?.groupDiscussVM.isDismiss = true
        groupVc?.groupDiscussVM.backRefreshLeft = { [ weak self] in
            self?.searchGroupVM.selectedSearchIndex = -1
            self?.tableView.reloadData()
        }
        groupVc?.groupDiscussVM.backRefresh = { [ weak self] in
            if self?.searchGroupVM.selectedSearchIndex != -1{
                self?.searchGroupVM.suggestedGroups.value[/self?.searchGroupVM.selectedSearchIndex].isMember = true
            }
            self?.searchGroupVM.selectedSearchIndex = -1
            self?.tableView.reloadData()
        }
        self.pushVC(groupVc ?? UIViewController())
    }
    
}

extension SearchGroupViewController : IndicatorInfoProvider{
    
    func indicatorInfo(for pagerTabStripController: PagerTabStripViewController) -> IndicatorInfo {
        return IndicatorInfo(title: "NETWORKS" )
    }
    
    
}
