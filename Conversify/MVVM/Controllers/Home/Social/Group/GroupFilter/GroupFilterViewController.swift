//
//  GroupFilterViewController.swift
//  Conversify
//
//  Created by Apple on 12/11/18.
//

import UIKit
import ESPullToRefresh

class GroupFilterViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var trailingTableView: NSLayoutConstraint!
    @IBOutlet weak var viewBg: UIView!
    
    //MARK::- PROPERTIES
    var visibleWidth: CGFloat {
        return (UIScreen.main.bounds.width/25)
    }
    var createVenueVM = CreateVenueViewModal()
    
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
        
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        createVenueVM.back?()
        self.dismiss(animated: false, completion: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        UIView.animate(withDuration: 1, delay: 0, options: .curveEaseOut, animations: {
            self.trailingTableView.constant = 0
            self.viewBg.alpha = 1
        }, completion: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        UIView.animate(withDuration: 1, animations: {
            self.trailingTableView.constant = -self.visibleWidth
            self.viewBg.alpha = 0
        }) { (completed) in
            
        }
    }
    
    
    override func bindings() {
        
        tableView.estimatedRowHeight = 84.0
        tableView.rowHeight = 48
        
        createVenueVM.items
            .asObservable()
            .bind(to: tableView.rx.items(cellIdentifier: R.reuseIdentifier.groupFilterTableViewCell.identifier, cellType: GroupFilterTableViewCell.self)) { (row,element,cell) in
                cell.interest = element
            }<bag
        
        tableView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.dismissVC(completion: {
                  self?.createVenueVM.backWithInterest?(self?.createVenueVM.items.value[indexPath.element?.row ?? 0])
            })
            }<bag
        
    }
    
    func onLoad(){
        trailingTableView.constant = -self.visibleWidth
        self.viewBg.alpha = 0
        createVenueVM.retrieveCategories { (statu) in
            print("status")
        }
    }
    
}

