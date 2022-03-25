//
//  SearchTopViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 03/01/19.
//

import UIKit
import XLPagerTabStrip

class SearchTopViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var labelRecent: UILabel!
    @IBOutlet weak var tableView: UITableView!
    
    //MARK::- PROPERTIES
    var searchTopVM = SearchTopViewModal()
    
    //MARK::- BINDINGS
   
    override func bindings() {
        addPaging()
        searchTopVM.getPeople()
        tableView.estimatedRowHeight = 56
        tableView.rowHeight = 56
        searchTopVM.resetTable.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (status) in
                if status{
                    
                    self?.tableView.es.resetNoMoreData()
                }
            })<bag
        searchTopVM.noMoreData.filter { [weak self] (_) -> Bool in
            self?.tableView.es.noticeNoMoreData()
            self?.tableView.es.stopPullToRefresh()
            return true
            }.subscribe(onNext: { [weak self] (status) in
                if status{
                    self?.tableView.es.noticeNoMoreData()
                    self?.tableView.es.stopPullToRefresh()
                }
            })<bag
        
        searchTopVM.items
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupMemberTableViewCell.identifier, cellType: GroupMemberTableViewCell.self)) { (row,element,cell) in
                cell.user = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.view.endEditing(true)
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.profileVM.userId = /self?.searchTopVM.items.value[/indexPath.element?.row].id
            UIApplication.topViewController()?.pushVC(vc)
            }<bag
  
    }
    
    func addPaging(){
        tableView.es.addInfiniteScrolling {
            if self.searchTopVM.loadMore {
                self.searchTopVM.page = self.searchTopVM.page + 1
                self.searchTopVM.getPeople()
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
    }
    
    
}

extension SearchTopViewController : IndicatorInfoProvider{
    
    func indicatorInfo(for pagerTabStripController: PagerTabStripViewController) -> IndicatorInfo {
        return IndicatorInfo(title: "TOP" )
    }
    
    
}
