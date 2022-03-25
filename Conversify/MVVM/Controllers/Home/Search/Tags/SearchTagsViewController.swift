
//
//  SearchTagsViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 03/01/19.
//

import UIKit
import XLPagerTabStrip

class SearchTagsViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var tableView: UITableView!
    
    //MARK::- PROPERTIES
    var searchTopVM = SearchTagViewModal()
    
    //MARK::- BINDINGS
    
   
    override func bindings() {
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
        addPaging()
        searchTopVM.getTags()
        tableView.estimatedRowHeight = 56
        tableView.rowHeight = 56
        searchTopVM.items
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.searchTagsTableViewCell.identifier, cellType: SearchTagsTableViewCell.self)) { (row,element,cell) in
                cell.tagVal = element
                cell.btnFollow?.tag = row
                cell.updateFollow = { [weak self] row, isFollow in
                    self?.searchTopVM.items.value[row].isFollowing = isFollow
                    self?.tableView.reloadData()
                }
            }<bag
        
        
//        _ = searchTopVM.text.asObservable().subscribe { [weak self] (event) in
//
//            self?.searchTopVM.getTags()
//        }
    }
    
    
    func addPaging(){
        tableView.es.addInfiniteScrolling {
            if self.searchTopVM.loadMore {
                self.searchTopVM.page = self.searchTopVM.page + 1
                self.searchTopVM.getTags()
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
    }
    
}

extension SearchTagsViewController : IndicatorInfoProvider{
    
    func indicatorInfo(for pagerTabStripController: PagerTabStripViewController) -> IndicatorInfo {
        return IndicatorInfo(title: "TAGS" )
    }
    
    
}
