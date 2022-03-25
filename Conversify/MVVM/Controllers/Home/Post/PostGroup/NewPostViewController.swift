//
//  NewPostViewController.swift
//  Conversify
//
//  Created by Apple on 14/11/18.
//

import UIKit
import EZSwiftExtensions

class NewPostViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var labelNoData: UILabel!
    @IBOutlet weak var btnNext: UIButton!
    
    //MARK::- PROPERTIES
    var groupListModal = GroupListViewModal()
    
    //MARK::- VC LIFE CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        getGroupListing()
        addPaging()
    }
    
    
    override func bindings() {
        tableView.estimatedRowHeight = 56.0
        tableView.rowHeight = 56.0
        
        groupListModal.items
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupPostCell.identifier, cellType: GroupPostCell.self)) { (row,element,cell) in
                cell.item = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            guard let vc = R.storyboard.post.createPostViewController() else { return }
            vc.isPostingInGroup = true
            vc.selectedGroup = self?.groupListModal.items.value[/indexPath.element?.row]
            self?.pushVC(vc)
            }<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
        btnNext.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.post.createPostViewController() else { return }
            vc.isPostingInGroup = false
            self?.pushVC(vc)
        })<bag
    }
    
    func addPaging(){
        tableView.es.addInfiniteScrolling {
            if self.groupListModal.loadMore {
                self.groupListModal.page = self.groupListModal.page + 1
                self.getGroupListing()
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
        
        tableView.es.addPullToRefresh {
            self.groupListModal.page =  1
            self.getGroupListing()
        }
    }
    
    func getGroupListing(){
        groupListModal.getGroupListing { [weak self](_) in
            self?.labelNoData.isHidden = (self?.groupListModal.items.value.count != 0)
            self?.tableView.reloadData()
            self?.tableView.es.stopPullToRefresh()
            self?.tableView.es.stopLoadingMore()
            self?.tableView.es.noticeNoMoreData()
            self?.refreshControl.endRefreshing()
        }
    }
    
}
