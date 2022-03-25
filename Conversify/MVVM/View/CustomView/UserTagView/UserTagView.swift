//
//  UserTagView.swift
//  Conversify
//
//  Created by Apple on 28/11/18.
//

import UIKit

protocol DelegateUserSerachTag : class {
    func selectedData(detail : UserList? , indx : Int?)
}
typealias UserSelected = (UserList? , Int) -> ()

class UserTagView: UIView {
    
    //MARK::- OUTLETS
    @IBOutlet weak var tableView: UITableView!{
        didSet{
            tableView.delegate = self
            tableView.dataSource = self
            tableView.registerXIB(R.reuseIdentifier.usersTagCell.identifier)
        }
    }
    
    //MARK::- PROPERTIES
    var usersList : [UserList]?
    var userSelectedOnTap : UserSelected?
    var row : Int?
    var delegate : DelegateUserSerachTag?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }
    
    func setUpUI(superView: UIView? , dataList : [UserList]? , indx : Int?) {
        
        usersList?.removeAll()
        usersList = dataList
        let myViews = superView?.subviews.filter{$0 is UserTagView}
        
        if /myViews?.count == 0 {
            superView?.addSubview(self)
            superView?.layoutSubviews()
        }else if usersList?.count == 0{
            self.removeFromSuperview()
            return
        }
        row = indx
        tableView.reloadData()
        self.layoutIfNeeded()
        self.setNeedsDisplay()
        
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupView()
    }
    
    private func setupView() {
        let view = viewFromNibForClass()
        view.frame = bounds
        view.autoresizingMask = [
            UIView.AutoresizingMask.flexibleWidth,
            UIView.AutoresizingMask.flexibleHeight
        ]
        addSubview(view)
    }
    
    // Loads a XIB file into a view and returns this view.
    private func viewFromNibForClass() -> UIView {
        let bundle = Bundle(for: type(of: self))
        let nib = UINib(nibName: String(describing: type(of: self)), bundle: bundle)
        let view = nib.instantiate(withOwner: self, options: nil).first as! UIView
        return view
    }
}

//MARK::- TABLEVIEW DELEGATE AND DATA SOURCE
extension UserTagView : UITableViewDelegate , UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.usersTagCell.identifier, for: indexPath) as? UsersTagCell else { return UITableViewCell() }
//        if /usersList?.count > indexPath.row{
//            return UITableViewCell()
//        }
        cell.item = usersList?[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return usersList?.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableView.automaticDimension
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
      //  userSelectedOnTap?(usersList?[indexPath.row] ?? UserList() , /row)
        delegate?.selectedData(detail: usersList?[indexPath.row] ?? UserList() , indx: /row)
    }
    
}
