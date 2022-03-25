//
//  FiltersViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/11/18.
//

import UIKit

class FiltersViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var btnReset: UIButton!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var tableViewFilterType: UITableView!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.delegate = self
            tableView?.dataSource = self
        }
    }
    @IBOutlet weak var btnApply: UIButton!
    
    //MARK::- PROPERTIES
    var filterVM = FiltersViewModal()
    
    //MARK::- BINDINGS
    
    override func bindings() {
        
        filterVM.retrieveInterests { [weak self] (status) in
            self?.tableView.reloadData()
        }
        
        let f1 = Privacy(name: "Public")
        let f2 = Privacy(name: "Private")
        filterVM.privacies.value = [f1,f2]
        
        tableViewFilterType.estimatedRowHeight = 60.0
        tableViewFilterType.rowHeight = 48
        
        
        filterVM.datePicked.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.tableView.reloadData()
                }
            })<bag
        
        btnApply.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.filterVM.filterPrivacySelected?(self?.filterVM.privacies.value ?? [])
            self?.filterVM.filterInterestSelected?(self?.filterVM.interests.value ?? [])
            self?.filterVM.applyFilters?()
            self?.dismissVC(completion: nil)
        })<bag
        
        btnReset.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.filterVM.interests.value.forEach({ (interest) in
                interest.isSelected = false
            })
            self?.filterVM.privacies.value.forEach({ (interest) in
                interest.isSelected = false
            })
            self?.filterVM.selectedFilterDate.value = ""
            self?.filterVM.selectedFilterPrivacy.value = ""
            self?.filterVM.selectedFilterLocation.value = ""
            self?.filterVM.selectedFilterCategory.value = []
            self?.filterVM.selectedLocation = ""
            self?.tableView.reloadData()
            self?.tableViewFilterType.reloadData()
            self?.filterVM.reset?()
            self?.dismissVC(completion: nil)
        })<bag
        
        filterVM.dismissController.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.dismiss(animated: true, completion: nil)
                }
            })<bag
        
        filterVM.filterTypes.asObservable().bind(to: tableViewFilterType.rx.items(cellIdentifier: R.reuseIdentifier.filtersTableViewCell.identifier, cellType: FiltersTableViewCell.self)) { [weak self] (row,element,cell) in
            cell.backgroundColor = /self?.filterVM.selectedFilterIndex == row ? #colorLiteral(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0) : #colorLiteral(red: 0.9764705882, green: 0.9568627451, blue: 0.9921568627, alpha: 1)
            cell.title = element
            
            }<bag
        
        tableViewFilterType.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.filterVM.selectedFilterIndex = /indexPath.element?.row
            self?.tableView.reloadData()
            self?.tableViewFilterType.reloadData()
            }<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.filterVM.back?()
            self?.dismissVC(completion: nil)
        })<bag
        
    }
    
}


extension FiltersViewController : UITableViewDelegate , UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        switch /filterVM.selectedFilterIndex {
        case 0:
            filterVM.interests.value[indexPath.row].isSelected =  filterVM.interests.value[indexPath.row].isSelected?.toggle()
            self.tableView.reloadData()
            
        case 1:
            filterVM.datePickerTapped()
            
        case 2:
            filterVM.privacies.value[indexPath.row].isSelected = filterVM.privacies.value[indexPath.row].isSelected?.toggle()
            self.tableView.reloadData()
        default:
            self.filterVM.filterLocationSelected?()
        }
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch /filterVM.selectedFilterIndex {
        case 0:
            return filterVM.interests.value.count
        case 1:
            return 1
        case 2:
            return 2
        default:
            return 1
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = self.tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.filtersTableViewCell.identifier) as? FiltersTableViewCell else {
            return UITableViewCell()
        }
        cell.labelTitle.textColor =  #colorLiteral(red: 0.2901960784, green: 0.337254902, blue: 0.4235294118, alpha: 1)
        switch /filterVM.selectedFilterIndex {
        case 0:
            cell.title = /filterVM.interests.value[indexPath.row].category
            cell.labelTitle.textColor = /filterVM.interests.value[indexPath.row].isSelected ? #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1) : #colorLiteral(red: 0.2901960784, green: 0.337254902, blue: 0.4235294118, alpha: 1)
        case 1:
            cell.title = /self.filterVM.selectedFilterDate.value == "" ? "Select Date" : /self.filterVM.selectedFilterDate.value
            
        case 2:
            cell.title = /filterVM.privacies.value[indexPath.row].name
            cell.labelTitle.textColor = /filterVM.privacies.value[indexPath.row].isSelected ? #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1) : #colorLiteral(red: 0.2901960784, green: 0.337254902, blue: 0.4235294118, alpha: 1)
            
        default:
            cell.title = /self.filterVM.selectedLocation == "" ? "Select Location" : /self.filterVM.selectedLocation
        }
        return cell
    }
    
    
    
}
