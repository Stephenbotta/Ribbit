//
//  PeopleViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 22/10/18.
//

import UIKit
import XLPagerTabStrip
import RxDataSources
import EZSwiftExtensions

class PeopleViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView?.addSubview(refreshControl)
            tableView?.refreshControl = refreshControl
        }
    }
    
    @IBOutlet weak var viewNoData: UIView!
    
    //MARK::- PROPERTIES
    var peopleVM = PeopleViewModal()
    var dataSource:RxTableViewSectionedReloadDataSource<PeopleDataSec>?
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()

    }
    
    //MARK::- BINDINGS
    override func bindings() {
        
        peopleVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        peopleVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    DispatchQueue.main.async {
                        self?.refreshControl.endRefreshing()
                        self?.tableView?.layoutIfNeeded()
                        self?.viewNoData?.isHidden = self?.peopleVM.peopleData.value.count != 0
                    }
                    
                }
            })<bag
        
        dataSource = RxTableViewSectionedReloadDataSource<PeopleDataSec>(configureCell: { (_, tableView, indexPath, element) -> UITableViewCell in
            guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.discoverPeopleTableViewCell.identifier , for: indexPath) as? DiscoverPeopleTableViewCell  , let elem = element as? UserCrossed else { return UITableViewCell()}
            cell.ppl = elem
            return cell
        })
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
//            if Singleton.sharedInstance.loggedInUser?.id == self?.peopleVM.peopleData.value[/indexPath.element?.section]?.userCrossed?[/indexPath.element?.row].crossedUserId?.id{
//                return
//            }
            guard let vc = R.storyboard.home.profileViewController() else { return }
            vc.profileVM.userType = .otherUser
            vc.profileVM.userData = self?.peopleVM.peopleData.value[/indexPath.element?.section]?.userCrossed?[/indexPath.element?.row].crossedUserId
            vc.profileVM.userId = self?.peopleVM.peopleData.value[/indexPath.element?.section]?.userCrossed?[/indexPath.element?.row].crossedUserId?.id
            self?.pushVC(vc)
            }<bag

        
        guard let safeDatasource = dataSource else{return}
        peopleVM.peopleDataSectional.asObservable().bind(to: tableView.rx.items(dataSource: safeDatasource))<bag
        tableView.rx.setDelegate(self)<bag
        
    }
    
    //MARK::-= FUNCTIOMS
    func onLoad(){
        peopleVM.retrievePeopleCrossPaths()
        refreshCalled = { [weak self] in
            self?.peopleVM.retrievePeopleCrossPaths()
            self?.view.endEditing(true)
            self?.refreshControl.endRefreshing()
        }
    }
    
    
    
}

extension PeopleViewController : IndicatorInfoProvider{
    
    func indicatorInfo(for pagerTabStripController: PagerTabStripViewController) -> IndicatorInfo {
        return IndicatorInfo(title: "PEOPLE" )
    }
    
    
}

//MARK::- TABLEVIEW DELEGATE
extension PeopleViewController : UITableViewDelegate  {
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        let labelHeight = /((peopleVM.peopleDataSectional.value[section].header).locationAddress?.height(withConstrainedWidth: UIScreen.main.bounds.width - 124 , font: UIFont.boldSystemFont(ofSize: 16))) + 16
        return /labelHeight
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 40
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let headerView: PeopleHeaderView = UIView.fromNib()
        headerView.people = peopleVM.peopleDataSectional.value[section].header
        return headerView
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let footerView: PeopleFooterView = UIView.fromNib()
        return footerView
    }
    
}
