//
//  AddParticipantViewController.swift
//  Conversify
//
//  Created by Harminder on 05/12/18.
//

import UIKit
import IBAnimatable

class AddParticipantViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var viewNoData: UIView!
    @IBOutlet weak var btnContinue: AnimatableButton!
    
    //MARK::- PROPERTIES
    var participantVM = AddParticipantViewModal()
    var isNew = false
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        
        participantVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
//                   DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
//                        self?.tableView.refreshControl?.beginRefreshing()
                        self?.refreshControl.beginRefreshing()
//                    }
                }
            })<bag
        
        participantVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    DispatchQueue.main.async {
                        self?.btnContinue.alpha = self?.participantVM.participantsList.value.count == 0 ? 0.5 : 1
                        self?.btnContinue.isEnabled = !(self?.participantVM.participantsList.value.count == 0)
                        self?.viewNoData.isHidden = self?.participantVM.participantsList.value.count != 0
                        self?.refreshControl.endRefreshing()
                        self?.tableView.layoutIfNeeded()
                    }
                }
            })<bag
        
        tableView.estimatedRowHeight = 60.0
        tableView.rowHeight = 64
        
        btnContinue.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            
            let particpants = self?.participantVM.participantsList.value.filter { /$0?.selectedForGroup }
            self?.participantVM.particpantsIds?(particpants as? [User] ?? [])
            self?.dismissVC(completion: nil)
        })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.dismissVC(completion: nil)
        })<bag
        
        participantVM.participantsList
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.addParticipantTableViewCell.identifier, cellType: AddParticipantTableViewCell.self)) { (row,element,cell) in
                cell.user = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.participantVM.participantsList.value[/indexPath.element?.row]?.selectedForGroup = /self?.participantVM.participantsList.value[/indexPath.element?.row]?.selectedForGroup.toggle()
            self?.tableView.reloadData()
            }<bag
        
        
    }
    
    func onLoad(){
        
        isNew ? participantVM.retrieveAllFollowers() :  participantVM.retrieveFollowers()
        refreshCalled = { [weak self] in
            /self?.isNew ? self?.participantVM.retrieveAllFollowers() :  self?.participantVM.retrieveFollowers()
            self?.view.endEditing(true)
            self?.refreshControl.endRefreshing()
        }
    }
    
    
}
