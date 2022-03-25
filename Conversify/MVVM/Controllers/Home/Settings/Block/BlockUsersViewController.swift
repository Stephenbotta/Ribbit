//
//  BlockUsersViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 15/01/19.
//

import UIKit
import IBAnimatable

class BlockUsersViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var llabelHeader: UILabel!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var btnBack: AnimatableButton!
    @IBOutlet weak var viewNoData: UIView!
    
    //MARK::- PROPERTIES
    var followerVM = FollowersViewModal()
    
    //MARK::- VIEW CYCLE
    
    override func viewWillAppear(_ animated: Bool) {
        switch followerVM.type {
        case 0:
            llabelHeader?.text = "Blocked users"
            followerVM.retrieveBlockedUsers()
        case 1:
            llabelHeader?.text = "Following"
        default:
            llabelHeader?.text = "Followers"
        }
        
    }
    
    //MARK::- BINDINGS
    
    
    override func bindings() {
        refreshCalled = { [weak self] in
            self?.followerVM.retrieveBlockedUsers()
            self?.view.endEditing(true)
        }
        
        followerVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        followerVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.endRefreshing()
                    self?.viewNoData.isHidden = self?.followerVM.groupMembers.value.count != 0
                }
            })<bag
        
        tableView.estimatedRowHeight = 60.0
        tableView.rowHeight = 56
        followerVM.groupMembers
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupParticipantTableViewCell.identifier, cellType: GroupParticipantTableViewCell.self)) { (row,element,cell) in
                cell.mmembers = element
                
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.view.endEditing(true)
            if self?.followerVM.type != 0{
                
            }else{
//                if Singleton.sharedInstance.loggedInUser?.id == self?.followerVM.groupMembers.value[/indexPath.element?.row]?.id{
//                    return
//                }
                guard let vc = R.storyboard.home.profileViewController() else { return }
                
                vc.profileVM.userType = .otherUser
                vc.profileVM.userId = self?.followerVM.groupMembers.value[/indexPath.element?.row]?.id
                UIApplication.topViewController()?.pushVC(vc)
            }
            
            }<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
    }
    
    
    
}
