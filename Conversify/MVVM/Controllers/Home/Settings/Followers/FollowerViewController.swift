//
//  FollowerViewController.swift
//  Conversify
//
//  Created by Harminder on 09/01/19.
//

import UIKit
import IBAnimatable

class FollowerViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    @IBOutlet weak var searchBar: UISearchBar!{
        didSet{
            searchBar.delegate = self
        }
    }
    @IBOutlet weak var constraintHeightSelectedUsers: NSLayoutConstraint!
    @IBOutlet weak var constraintHeightEveryOne: NSLayoutConstraint!
    @IBOutlet weak var contraintHeightFollower: NSLayoutConstraint!
    @IBOutlet weak var btnBack: AnimatableButton!
    @IBOutlet weak var imageRadio: UIImageView!
    @IBOutlet weak var imageSelectedRadio: UIImageView!
    @IBOutlet weak var labelSelectedUsers: UILabel!
    @IBOutlet weak var labelYourFollowers: UILabel!
    @IBOutlet weak var labelEveryOne: UILabel!
    @IBOutlet weak var imageRadioEveryOne: UIImageView!
    
    //MARK::- PROPERTIES
    var followerVM = FollowersViewModal()
    var isFollower : Bool?
    var isPost = false
    
    //MARK::- VIEW CYCLE
    override func viewWillAppear(_ animated: Bool) {
        
        if /followerVM.isProfile{
            if isFollower != nil{
                /isFollower ? followerVM.retrieveFollowers() : followerVM.retrieveFollowing()
                tableView.isHidden = false
                constraintHeightEveryOne.constant = 0
                constraintHeightSelectedUsers.constant = 0
                contraintHeightFollower.constant = 0
            }
        }else{
            if isPost{
                constraintHeightEveryOne.constant = 0
                constraintHeightSelectedUsers.constant = 0
                contraintHeightFollower.constant = 0
                tableView.isHidden = false
                followerVM.retrieveLikes()
            }else{
                onAppear()
                followerVM.retrieveFollowers()
            }
            
        }
        
    }
    
    func onAppear(){
        constraintHeightEveryOne.constant = ((followerVM.selectedPrivacy ?? .profilePic) == .posting || (followerVM.selectedPrivacy ?? .profilePic) == .likes  ) ? 0 : 48
        if /Singleton.sharedInstance.loggedInUser?.isAccountPrivate {
            constraintHeightEveryOne.constant = 0
        }
    }
    
    //MARK::- BINDINGS
    override func bindings() {
        searchBar
            .rx.text
            .orEmpty
            .subscribe(onNext: { [unowned self] query in
                print(self.followerVM.allMembers.value)
                print(self.followerVM.groupMembers.value)
                self.followerVM.groupMembers.value = self.followerVM.allMembers.value.filter{ /$0?.userName?.lowercased().hasPrefix(/query.lowercased()) }
                print(self.followerVM.groupMembers.value)
                self.tableView.reloadData()
            })<bag
        
        labelSelectedUsers.isUserInteractionEnabled = false
        
        refreshCalled = { [weak self] in
            self?.followerVM.retrieveFollowers()
            self?.view.endEditing(true)
        }
        updateSelection()
        
        labelSelectedUsers.addTapGesture { [weak self] (gesture) in
            self?.tableView.isHidden = false
            self?.imageRadio.image = R.image.ic_radioOnButtonCopy()
            self?.imageSelectedRadio.image =  R.image.ic_radio_button_color()
            self?.imageRadioEveryOne.image =  R.image.ic_radioOnButtonCopy()
        }
        
        labelEveryOne.addTapGesture { [weak self] (gesture) in
            self?.followerVM.groupMembers.value.forEach({ (user) in
                user?.isSelected = false
            })
            self?.tableView.reloadData()
            self?.tableView.isHidden = true
            self?.imageRadio.image = R.image.ic_radioOnButtonCopy()
            self?.imageSelectedRadio.image =  R.image.ic_radioOnButtonCopy()
            self?.imageRadioEveryOne.image =  R.image.ic_radio_button_color()
        }
        
        
        labelYourFollowers.addTapGesture { [weak self] (gesture) in
            self?.followerVM.groupMembers.value.forEach({ (user) in
                user?.isSelected = false
            })
            self?.tableView.reloadData()
            self?.tableView.isHidden = true
            self?.imageSelectedRadio.image =  R.image.ic_radioOnButtonCopy()
            self?.imageRadio.image =  R.image.ic_radio_button_color()
            self?.imageRadioEveryOne.image =  R.image.ic_radioOnButtonCopy()
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
                    self?.labelSelectedUsers.isUserInteractionEnabled = self?.followerVM.groupMembers.value.count != 0
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
            if /self?.followerVM.isProfile  || /self?.isPost {
                
            }else{
                self?.followerVM.groupMembers.value[/indexPath.element?.row]?.isSelected =  /self?.followerVM.groupMembers.value[/indexPath.element?.row]?.isSelected?.toggle()
                self?.imageRadio.image = R.image.ic_radioOnButtonCopy()
                self?.imageSelectedRadio.image =  R.image.ic_radio_button_color()
                self?.imageRadioEveryOne.image =  R.image.ic_radioOnButtonCopy()
                self?.tableView.reloadData()
            }
            
            
            }<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.view.endEditing(true)

            if /self?.followerVM.isProfile  || /self?.isPost {
                
            }else{
                let users = self?.followerVM.groupMembers.value.filter{ return /$0?.isSelected }
                guard let members = users as? [User]? else { return }
                
                if self?.imageSelectedRadio.image ==  R.image.ic_radio_button_color() && members?.count == 0{
                    self?.imageRadio.image =  R.image.ic_radioOnButtonCopy()
                    self?.imageRadioEveryOne.image =  R.image.ic_radio_button_color()
                    self?.imageSelectedRadio.image =  R.image.ic_radioOnButtonCopy()
                    UtilityFunctions.makeToast(text: "Info is limited to " +  (/Singleton.sharedInstance.loggedInUser?.isAccountPrivate ? "Followes" : "everyone"), type: .info)
                }
                
                if self?.imageSelectedRadio.image == R.image.ic_radio_button_color(){
                    self?.followerVM.selectedUsers?(members ?? [])
                }else{
                    if self?.imageRadioEveryOne.image ==  R.image.ic_radio_button_color(){
                        if /Singleton.sharedInstance.loggedInUser?.isAccountPrivate{
                            self?.followerVM.onlyFollowers?()
                        }else{
                            self?.followerVM.everyOne?()
                        }
                    }else{
                        self?.followerVM.onlyFollowers?()
                    }
                }
            }
            
            self?.popVC()
        })<bag
        
    }
    
    func updateSelection(){
        var usersSelected = [User]()
        switch followerVM.selectedPrivacy ?? .profilePic {
        case .profilePic:
            usersSelected = Singleton.sharedInstance.loggedInUser?.imageVisibility ?? []
        case .privateInfo :
            usersSelected = Singleton.sharedInstance.loggedInUser?.personalInfoVisibility ?? []
        case .userName :
            usersSelected = Singleton.sharedInstance.loggedInUser?.nameVisibility ?? []
        case .messageTag:
            usersSelected = Singleton.sharedInstance.loggedInUser?.tagPermission ?? []
        case .posting:
            usersSelected = (self.followerVM.usersSelectedForPostModules.value as? [User]) ?? []
        case .likes:
            usersSelected = (self.followerVM.usersSelectedForPostModules.value as? [User]) ?? []
        }
        tableView.isHidden = usersSelected.count == 0
        if usersSelected.count != 0 {
            imageSelectedRadio.image = R.image.ic_radio_button_color()
            imageRadio.image =  R.image.ic_radioOnButtonCopy()
            imageRadioEveryOne.image = R.image.ic_radioOnButtonCopy()
        }else{
            let user = Singleton.sharedInstance.loggedInUser
            switch followerVM.selectedPrivacy ?? .profilePic {
            case .profilePic:
                if /user?.imageVisibilityForEveryone {
                    imageSelectedRadio.image = R.image.ic_radioOnButtonCopy()
                    imageRadio.image =  R.image.ic_radioOnButtonCopy()
                    imageRadioEveryOne.image = R.image.ic_radio_button_color()
                }else{
                    imageSelectedRadio.image = R.image.ic_radioOnButtonCopy()
                    imageRadio.image =  R.image.ic_radio_button_color()
                    imageRadioEveryOne.image = R.image.ic_radioOnButtonCopy()
                }
                
            case .privateInfo :
                
                imageSelectedRadio.image = R.image.ic_radioOnButtonCopy()
                imageRadio.image =  R.image.ic_radio_button_color()
                imageRadioEveryOne.image = R.image.ic_radioOnButtonCopy()
                
            case .userName :
                
                if /user?.nameVisibilityForEveryone {
                    imageSelectedRadio.image = R.image.ic_radioOnButtonCopy()
                    imageRadio.image =  R.image.ic_radioOnButtonCopy()
                    imageRadioEveryOne.image = R.image.ic_radio_button_color()
                }else{
                    imageSelectedRadio.image = R.image.ic_radioOnButtonCopy()
                    imageRadio.image =  R.image.ic_radio_button_color()
                    imageRadioEveryOne.image = R.image.ic_radioOnButtonCopy()
                }
                
            case .messageTag:
                
                if /user?.tagPermissionForEveryone {
                    imageSelectedRadio.image = R.image.ic_radioOnButtonCopy()
                    imageRadio.image =  R.image.ic_radioOnButtonCopy()
                    imageRadioEveryOne.image = R.image.ic_radio_button_color()
                }else{
                    imageSelectedRadio.image = R.image.ic_radioOnButtonCopy()
                    imageRadio.image =  R.image.ic_radio_button_color()
                    imageRadioEveryOne.image = R.image.ic_radioOnButtonCopy()
                }
                
            case .posting , .likes:
                
                imageSelectedRadio.image = R.image.ic_radioOnButtonCopy()
                imageRadio.image =  R.image.ic_radio_button_color()
                imageRadioEveryOne.image = R.image.ic_radioOnButtonCopy()
            }
            
        }
        
    }
    
}


extension FollowerViewController : UISearchBarDelegate{
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar){
        self.view.endEditing(true)
        self.searchBar.text = ""
        self.followerVM.groupMembers.value = self.followerVM.allMembers.value.filter{ /$0?.userName?.hasPrefix("") }
        self.tableView.reloadData()
    }
}
