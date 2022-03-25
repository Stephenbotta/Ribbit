//
//  VenueViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit
import XLPagerTabStrip
import EZSwiftExtensions

class VenueViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView.register(UINib.init(nibName: R.nib.venueListHeaderView.name, bundle: nil), forHeaderFooterViewReuseIdentifier: R.nib.venueListHeaderView.name)
            tableView.addSubview(refreshControl)
            tableView.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var constraintWidthButtonCancel: NSLayoutConstraint!
    @IBOutlet weak var btnCancelSearch: UIButton!
    @IBOutlet weak var searchBar: UISearchBar!
    @IBOutlet weak var viewNoData: UIView!
    
    //MARK::- PROPERTIES
    var venueVM = VenueViewModal()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        
        venueVM.pickPlace.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.getPlaceDetails()
                }
            })<bag
        
        venueVM.updatedFilteredResult.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.updateHeader(filterName: /self?.venueVM.selectedFilter.value)
                    self?.viewNoData.isHidden = !(/self?.venueVM.interests.value?.yourVenueData?.count == 0 && /self?.venueVM.interests.value?.venueNearYou?.count == 0)
                    self?.tableView.reloadData()
                    if self?.venueVM.interests.value?.yourVenueData?.count != 0 || self?.venueVM.interests.value?.venueNearYou?.count != 0 {
                        self?.tableView.scrollToRow(at: IndexPath(row: 0, section: 0), at: .top, animated: true)
                    }
                    
                }
            })<bag
        
        btnCancelSearch.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.searchBar.text = ""
            self?.constraintWidthButtonCancel.constant = 0
            self?.view.layoutIfNeeded()
            self?.venueVM.isSearchEnable = false
            self?.venueVM.interests.value = self?.venueVM.apiInterests.value
            self?.tableView.reloadData()
             self?.viewNoData.isHidden = !(/self?.venueVM.interests.value?.yourVenueData?.count == 0 && /self?.venueVM.interests.value?.venueNearYou?.count == 0)
        })<bag
        
        venueVM.resetFilter.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.tableView.reloadData()
                }
            })<bag
        
        
       
        
        venueVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        venueVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    DispatchQueue.main.async {
                       
                        self?.searchBar.alpha = (/self?.venueVM.interests.value?.yourVenueData?.count == 0 && /self?.venueVM.interests.value?.venueNearYou?.count == 0) ? 0.5 : 1.0
                        self?.searchBar.isUserInteractionEnabled = !(/self?.venueVM.interests.value?.yourVenueData?.count == 0 && /self?.venueVM.interests.value?.venueNearYou?.count == 0)
                        self?.viewNoData.isHidden = !(/self?.venueVM.interests.value?.yourVenueData?.count == 0 && /self?.venueVM.interests.value?.venueNearYou?.count == 0)
                        self?.refreshControl.endRefreshing()
                        self?.tableView.reloadData()
                        self?.tableView.layoutIfNeeded()
                    }
                    
                }
            })<bag
        
    }
    
    func onLoad(){
        searchBar.delegate = self
        tableView.rx.setDelegate(self)<bag
        tableView.rx.setDataSource(self)<bag
        pickedLocation = { [weak self] (lat , long , name , adddress) in
            self?.venueVM.selectedFilter.value = /name.uppercaseFirst
            self?.venueVM.lat.value = lat
            self?.venueVM.long.value = long
            self?.venueVM.filterVc?.filterVM.selectedLocation = /name.uppercaseFirst
             self?.venueVM.filterVc?.tableView.reloadData()
        }
        refreshCalled = { [weak self] in
            if self?.venueVM.selectedFilter.value == "Suggested"{
                self?.venueVM.retrieveVenues(showLoader: false, beginComm: true, { (status) in
                })
            }else{
                self?.venueVM.getFilteredVenues()
            }
        }
        retrieve()
    }
    
    func retrieve(){
        
        venueVM.retrieveVenues(showLoader: false, beginComm: true) { [weak self] (status) in
            self?.refreshControl.endRefreshing()
            if status{
                self?.tableView.reloadData()
            }
        }
        
    }
    
    func proceedToChat(venue: Venues?){
        guard let vc = R.storyboard.chats.chatViewController() else { return }
        vc.isFromChat = true
        vc.chatingType = .venue
        vc.chatModal = ChatViewModal(membersData: nil, venueD: venue)
        vc.chatModal.groupIdToJoin.value = venueVM.groupIdToJoin.value
        vc.chatModal.exit = { [ weak self] in
            self?.retrieve()
        }
        vc.chatModal.backRefresh = { [ weak self] in
            self?.retrieve()
        }
        Singleton.sharedInstance.conversationId = /venue?.conversationId
        pushVC(vc)
    }
    
    func joinVenue(indexPath: IndexPath){
        guard let venueDetail = R.storyboard.venue.venueDetailViewController() else { return }
        venueDetail.detailVM = VenueDetailViewModal(venu: venueVM.interests.value?.venueNearYou?[indexPath.row])
        venueDetail.detailVM.refresh = { [weak self] in
            self?.venueVM.retrieveVenues(showLoader: false, beginComm: false, { (status) in })
        }
        UIApplication.topViewController()?.pushVC(venueDetail)
    }
    
}

//MARK::- TABLEVIEW DELEGATE & DATASOURCE
extension VenueViewController : UITableViewDelegate , UITableViewDataSource , DelegateFilterSelected {
    
    func filterSelected(){
        venueVM.filterOptions()
    }
    
    func updateHeader(filterName: String){
        guard let header = tableView.headerView(forSection: 0) as? VenueListHeaderView else { return }
        header.btnAction.setTitle(filterName, for: .normal)
        self.tableView.reloadData()
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        
        return (/venueVM.interests.value?.yourVenueData?.count == 0 && /venueVM.interests.value?.venueNearYou?.count == 0) ? 0 : ((/venueVM.interests.value?.yourVenueData?.count != 0 && /venueVM.interests.value?.venueNearYou?.count != 0) ? 2 : 1 )
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (/venueVM.interests.value?.yourVenueData?.count == 0 && /venueVM.interests.value?.venueNearYou?.count == 0){
            return 0
        }else if (/venueVM.interests.value?.yourVenueData?.count != 0 && /venueVM.interests.value?.venueNearYou?.count != 0){
            return (section == 0) ? /venueVM.interests.value?.yourVenueData?.count : /venueVM.interests.value?.venueNearYou?.count
        }else if /venueVM.interests.value?.yourVenueData?.count != 0{
            return /venueVM.interests.value?.yourVenueData?.count
        }else{
            return /venueVM.interests.value?.venueNearYou?.count
        }
        
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.venueTableViewCell.identifier, for: indexPath) as? VenueTableViewCell else { return UITableViewCell() }
        
        let sec = indexPath.section
        let row = indexPath.row
        cell.section = sec
        if /venueVM.interests.value?.yourVenueData?.count == 0{
            cell.venue = /venueVM.interests.value?.venueNearYou?[row]
            cell.labelDistance?.isHidden = true
            cell.isJoined = false
        }else{
            cell.isJoined = (sec == 0) ? true : false
            cell.venue = (sec == 0) ? /venueVM.interests.value?.yourVenueData?[row] : /venueVM.interests.value?.venueNearYou?[row]
            cell.labelDistance?.isHidden = (sec == 0)
        }
        
        return cell
    }
    
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if (/venueVM.interests.value?.yourVenueData?.count == 0 && /venueVM.interests.value?.venueNearYou?.count == 0){
            //do notjing
        }else if (/venueVM.interests.value?.yourVenueData?.count != 0 && /venueVM.interests.value?.venueNearYou?.count != 0){
            if indexPath.section == 0{
                venueVM.selectedVenue.value = venueVM.interests.value?.yourVenueData?[indexPath.row]
                venueVM.groupIdToJoin.value = venueVM.interests.value?.yourVenueData?[indexPath.row].groupId
                proceedToChat(venue: venueVM.interests.value?.yourVenueData?[indexPath.row])
            }else{
                venueVM.selectedVenue.value = venueVM.interests.value?.venueNearYou?[indexPath.row]
                venueVM.groupIdToJoin.value = venueVM.interests.value?.venueNearYou?[indexPath.row].groupId
                joinVenue(indexPath: indexPath)
            }
        }else if /venueVM.interests.value?.yourVenueData?.count != 0{
            venueVM.selectedVenue.value = venueVM.interests.value?.yourVenueData?[indexPath.row]
            venueVM.groupIdToJoin.value = venueVM.interests.value?.yourVenueData?[indexPath.row].groupId
            proceedToChat(venue: venueVM.interests.value?.yourVenueData?[indexPath.row])
        }else{
            venueVM.selectedVenue.value = venueVM.interests.value?.venueNearYou?[indexPath.row]
            venueVM.groupIdToJoin.value = venueVM.interests.value?.venueNearYou?[indexPath.row].groupId
            joinVenue(indexPath: indexPath)
        }
        
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableView.automaticDimension
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: HeaderFooterID.VenueListHeaderView.rawValue){
            var txt = ""
            if (/venueVM.interests.value?.yourVenueData?.count == 0 && /venueVM.interests.value?.venueNearYou?.count == 0){
                //do nothing
            }else if (/venueVM.interests.value?.yourVenueData?.count != 0 && /venueVM.interests.value?.venueNearYou?.count != 0){
                txt = (section == 0) ? "Your Venues" : ( /venueVM.selectedFilter.value == "" ? "Suggested Venues" : /venueVM.selectedFilter.value)
            }else if /venueVM.interests.value?.yourVenueData?.count != 0{
                 txt = "Your Venues"
            }else{
                txt = ( /venueVM.selectedFilter.value == "" ? "Suggested Venues" : /venueVM.selectedFilter.value)
            }
            (header as? VenueListHeaderView)?.labelTitleName.text = txt.uppercased()
            (header as? VenueListHeaderView)?.delegate = self
            return header
        }else {
            return nil
        }
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 48
    }
    
}


//MARK::- SEARCH BAR DELEGATE
extension VenueViewController : UISearchBarDelegate {
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        constraintWidthButtonCancel.constant = 60
        self.view.layoutIfNeeded()
        let txt = searchText.lowercased().trimmingCharacters(in: .whitespaces)
        if txt.isEmpty {
            self.venueVM.isSearchEnable = false
            self.venueVM.interests.value = self.venueVM.apiInterests.value
        }else {
            self.venueVM.isSearchEnable = true
            let searchListNearYou = /venueVM.apiInterests.value?.venueNearYou?.filter({ (/$0.venueTitle?.lowercased().contains(txt))})
            let serachListYourVenues = /venueVM.apiInterests.value?.yourVenueData?.filter({ (/$0.venueTitle?.lowercased().contains(txt))})
            let searchData = VenueData()
            searchData.venueNearYou = searchListNearYou
            searchData.yourVenueData = serachListYourVenues
            self.venueVM.interests.value = searchData
        }
        self.viewNoData.isHidden = !(/self.venueVM.interests.value?.yourVenueData?.count == 0 && /self.venueVM.interests.value?.venueNearYou?.count == 0)
        tableView.reloadData()
    }
    
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        
    }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        self.venueVM.interests.value = self.venueVM.apiInterests.value
        tableView.reloadData()
        self.view.endEditing(true)
    }
    
}

