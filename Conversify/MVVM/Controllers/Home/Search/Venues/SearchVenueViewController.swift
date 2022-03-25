
//
//  SearchVenueViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 03/01/19.
//

import UIKit
import XLPagerTabStrip

class SearchVenueViewController: BaseRxViewController {
    
    //MARK::- OUTLETS

    @IBOutlet weak var tableView: UITableView!
    
    //MARK::- PROPERTIES
    var searchVenueVM = VenueViewModal()
    
    //MARK::- BINDINGS
    
    override func bindings() {
        searchVenueVM.resetTable.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (status) in
                if status{
                    
                    self?.tableView.es.resetNoMoreData()
                }
            })<bag
        searchVenueVM.noMoreData.filter { [weak self] (_) -> Bool in
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
        searchVenueVM.getVenues()
        tableView.estimatedRowHeight = 56
        tableView.rowHeight = UITableView.automaticDimension
        searchVenueVM.allVenues
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.venueTableViewCell.identifier, cellType: VenueTableViewCell.self)) { (row,element,cell) in
                cell.venue = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.view.endEditing(true)
             self?.searchVenueVM.selectedVenue.value = self?.searchVenueVM.allVenues.value[/indexPath.element?.row]
            self?.searchVenueVM.groupIdToJoin.value = self?.searchVenueVM.allVenues.value[/indexPath.element?.row].groupId
            if /self?.searchVenueVM.allVenues.value[/indexPath.element?.row].isMember{
                self?.proceedToChat(venue: self?.searchVenueVM.allVenues.value[/indexPath.element?.row])
            }else{
                self?.joinVenue(indexPath: indexPath.element)
            }
            
            }<bag
    }
    
    func addPaging(){
        tableView.es.addInfiniteScrolling {
            if /self.searchVenueVM.loadMore {
                self.searchVenueVM.pageV = self.searchVenueVM.pageV + 1
                self.searchVenueVM.getVenues()
            }else {
                self.tableView.es.stopLoadingMore()
                self.tableView.es.noticeNoMoreData()
            }
        }
    }
    
    func proceedToChat(venue: Venues?) {
        guard let vc = R.storyboard.chats.chatViewController() else { return }
        vc.isFromChat = true
        vc.chatingType = .venue
        vc.chatModal = ChatViewModal(membersData: nil, venueD: venue)
        vc.chatModal.groupIdToJoin.value = searchVenueVM.groupIdToJoin.value
        Singleton.sharedInstance.conversationId = /venue?.conversationId
        pushVC(vc)
    }
    
    func joinVenue(indexPath: IndexPath?){
        guard let venueDetail = R.storyboard.venue.venueDetailViewController() else { return }
        venueDetail.detailVM = VenueDetailViewModal(venu: searchVenueVM.allVenues.value[/indexPath?.row])
        venueDetail.detailVM.refreshJoined = { [weak self] in
            self?.searchVenueVM.allVenues.value[/indexPath?.row].isMember = true
        }
        UIApplication.topViewController()?.pushVC(venueDetail)
    }
   

}

extension SearchVenueViewController : IndicatorInfoProvider{
    
    func indicatorInfo(for pagerTabStripController: PagerTabStripViewController) -> IndicatorInfo {
        return IndicatorInfo(title: "VENUES" )
    }
    
    
}
