//
//  CreateVenueViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit

class CreateVenueViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var labelCreateHeader: UILabel!
    @IBOutlet weak var labelDetailHeader: UILabel!
    
    
    //MARK::- PROPERTIES
    var createVenueVM = CreateVenueViewModal()
    
    
    //MARK::- VIEW CYCLE
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        onAppear()
    }
    
    override func bindings() {
        tableView.estimatedRowHeight = 84.0
        tableView.rowHeight = 64
        
        createVenueVM.items
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.selectCategoryTableViewCell.identifier, cellType: SelectCategoryTableViewCell.self)) { (row,element,cell) in
                cell.interest = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            
            /self?.createVenueVM.isVenue ? self?.createVenue(row: indexPath.element?.row) : self?.createGroup(row: indexPath.element?.row)
            }<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
    }
    
    
    func createVenue(row:Int?){
        guard let vc = R.storyboard.venue.submitVenueDetailsViewController() else { return }
        vc.submitVenueDetailsVM = SubmitVenueDetailsViewModal(categor: createVenueVM.items.value[row ?? 0])
        self.pushVC(vc)
    }
    
    func createGroup(row:Int?){
        guard let groupVc = R.storyboard.groups.addGroupViewController() else { return }
        groupVc.addGroupVM = AddGroupViewModal(categor: createVenueVM.items.value[row ?? 0])
        self.pushVC(groupVc)
    }
    
    func onAppear(){
        labelCreateHeader?.text = /createVenueVM.isVenue ? "Create a Venue" : "Create a Network"
        labelDetailHeader?.text = /createVenueVM.isVenue ? "Venues are a fun and engaging way to meet and explore people. Create chatrooms based upon your location." : "Create networks based on your interest, location and invite your friends, family and colleagues to join!!!"
        btnBack.setTitle(/createVenueVM.isVenue ? "Venues" : "Networks", for: .normal)
    }
    
    func onLoad(){
        
        createVenueVM.retrieveCategories { (status) in
            if status{
                
            }
        }
        
        
    }
    
    
}

