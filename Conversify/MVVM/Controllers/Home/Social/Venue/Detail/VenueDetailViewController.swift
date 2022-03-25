//
//  VenueDetailViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 19/12/18.
//

import UIKit

class VenueDetailViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var labelVenueName: UILabel!
    @IBOutlet weak var imageVenue: UIImageView!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnNavigate: UIButton!
    @IBOutlet weak var labelLocAddress: UILabel!
    @IBOutlet weak var labelLocName: UILabel!
    @IBOutlet weak var labelDateTime: UILabel!
    @IBOutlet weak var labelTags: UILabel!
    @IBOutlet weak var btnJoin: UIButton!
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView.addSubview(refreshControl)
            tableView.refreshControl = refreshControl
        }
    }
    
    @IBOutlet weak var viewTableHeader: UIView!
    
    //MARK::- PROPERTIES
    var detailVM = VenueDetailViewModal()
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    
    override func bindings() {
        
        tableView.estimatedRowHeight = 60.0
        tableView.rowHeight = UITableView.automaticDimension
        
        detailVM.privateJoinedSuccessfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.btnJoin.isEnabled = false
                    self?.btnJoin.alpha = 0.7
                }
            })<bag
        
        detailVM.groupJoinesSuccessfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.detailVM.refreshJoined?()
                    self?.btnJoin.isEnabled = false
                    self?.btnJoin.alpha = 0.7
                    self?.proceedToChat(venue:  self?.detailVM.venue.value)
                }
            })<bag
        
        detailVM.groupMembers
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupMemberTableViewCell.identifier, cellType: GroupMemberTableViewCell.self)) { (row,element,cell) in
                cell.members = element
            }<bag
        
        btnJoin.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            UtilityFunctions.show(alert: "", message: "Do you want to join this venue?", buttonOk: {
                self?.joinVenue()
            }, viewController: self!, buttonText: "Yes")
            
        })<bag
        
        btnNavigate.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            guard let lat = self?.detailVM.venue.value?.latLong?.last as? Double , let lng = self?.detailVM.venue.value?.latLong?.first as? Double else { return }
            UtilityFunctions.retrieveMaps(lati: String(lat) , longi: String(lng) , userLat: /LocationManager.sharedInstance.currentLocation?.currentLat, userLong: /LocationManager.sharedInstance.currentLocation?.currentLng)
            
        })<bag
        
        detailVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        detailVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    DispatchQueue.main.async {
                        self?.btnNavigate.isEnabled = true
                        self?.refreshControl.endRefreshing()
                        self?.btnJoin.isEnabled = true
                        self?.btnJoin.alpha = 1
                        self?.updateDetails()
                        self?.calcHeight()
                    }
                }
            })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.detailVM.refresh?()
            self?.popVC()
        })<bag
        
    }
    
    
    func onLoad(){
        btnJoin.isEnabled = false
        btnJoin.alpha = 0.7
        btnNavigate.isEnabled = false
        updateDetails()
        refreshCalled = { [weak self] in
            
        }
        detailVM.retrieveVenues()
    }
    
    func calcHeight(){
        let height1 = 268 + UIScreen.main.bounds.width + labelLocAddress.getEstimatedHeight() + labelLocName.getEstimatedHeight()
        viewTableHeader.frame = CGRect(x: 0 , y: 0, w: UIScreen.main.bounds.width, h: height1)
        tableView.tableHeaderView = viewTableHeader
        tableView.layoutIfNeeded()
        tableView.reloadData()
    }
    
    
}

//MARK::- FUNCTIONS
extension VenueDetailViewController  {
    
    func proceedToChat(venue: Venues?){
        guard let vc = R.storyboard.chats.chatViewController() else { return }
        vc.isFromChat = false
        vc.chatingType = .venue
        vc.chatModal = ChatViewModal(membersData: nil, venueD: venue)
        vc.chatModal.groupIdToJoin.value = /venue?.groupId
        vc.chatModal.exit = { [ weak self] in
           
        }
        vc.chatModal.backRefresh = { [ weak self] in
            
        }
        Singleton.sharedInstance.conversationId = /venue?.conversationId
        pushVC(vc)
    }
    
    func joinVenue(){
        let groupId = detailVM.venue.value?.groupId
        detailVM.joinVenue(groupId: /groupId, isPrivate: String(/detailVM.venue.value?.isPrivate), adminId: /detailVM.venue.value?.adminId)
    }
    
    func updateDetails(){
        let venueData = detailVM.venue.value
        labelVenueName?.text = /venueData?.venueTitle?.uppercaseFirst
        labelLocAddress?.text = venueData?.venueLocationAddress
        labelLocName?.text = venueData?.locName
        labelDateTime?.text = /Date(milliseconds: Double(/venueData?.venueTime)).toString(format: "MMM d, yyyy h:mm a")
        let tags = venueData?.venueTags?.map({ (tag) -> String in
            return "#" + /tag
        })
       // imageVenue?.kf.setImage(with: URL(string: /venueData?.venueImageUrl?.original))
        
        imageVenue?.image(url:  /venueData?.venueImageUrl?.original,placeholder: #imageLiteral(resourceName: "ic_placeholder"))
        labelTags?.text = tags?.joined(separator:", ")
        switch RequestStatus(rawValue: /venueData?.requestStatus) ?? .none{ 
        case .pending:
            btnJoin.isEnabled = false
            btnJoin.alpha = 0.7
        case .rejected , .none:
            btnJoin.isEnabled = true
        }
    }
    
}
