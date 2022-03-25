//
//  GroupTopicViewController.swift
//  Conversify
//
//  Created by Apple on 12/11/18.
//

import UIKit


class GroupTopicViewController: BaseRxViewController {
    
    //MARK: - Outlets
    @IBOutlet weak var labelNoData: UILabel!
    @IBOutlet weak var labelCatgoryName: UILabel!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var collectionView: UICollectionView!{
        didSet{
            collectionView?.addSubview(refreshControl)
            collectionView?.refreshControl = refreshControl
        }
    }
    
    //MARK::- PROPERTIS
    var groupTopicVM = GroupTopicViewModal()
    
    //MARK: - View Hierarchy
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
        // Do any additional setup after loading the view.
    }
    
    override func bindings() {
        
        
        groupTopicVM.beginCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.beginRefreshing()
                }
            })<bag
        
        groupTopicVM.endCommunication.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.refreshControl.endRefreshing()
                    self?.collectionView.es.stopLoadingMore()
                }
            })<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.groupTopicVM.refresh?()
            self?.popVC()
        })<bag
        
        
        groupTopicVM.suggestedGroups
            .asObservable()
            .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.groupTopicCollectionViewCell.identifier, cellType: GroupTopicCollectionViewCell.self)) { (row,element,cell) in
                cell.group = element
                
            }<bag
        collectionView.rx.setDelegate(self)<bag
        
        groupTopicVM.joinedSuccesfully.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.detailGroup(group: self?.groupTopicVM.selectedGroup.value)
                }
            })<bag
        
        groupTopicVM.noData.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                self?.labelNoData.isHidden = !bool
            })<bag
    }
    
    func onLoad(){
        addPaging()
        labelCatgoryName.text = /groupTopicVM.category.value?.category?.uppercaseFirst
        groupTopicVM.retrieveFilteredGroups()
        refreshCalled = { [weak self] in
            self?.groupTopicVM.retrieveFilteredGroups()
            self?.view.endEditing(true)
            self?.refreshControl.endRefreshing()
        }
    }
    
    func addPaging(){
        collectionView.es.addInfiniteScrolling {
            if self.groupTopicVM.loadMore {
                self.groupTopicVM.page = self.groupTopicVM.page + 1
                self.groupTopicVM.retrieveFilteredGroups()
            }else {
                self.collectionView.es.stopLoadingMore()
                self.collectionView.es.noticeNoMoreData()
            }
        }
    }
    
    func detailGroup(group: YourGroup?){
        let groupVc = R.storyboard.groups.groupDiscussionViewController()
        groupVc?.groupDiscussVM = GroupDiscussionViewModal(groupD: group)
        groupVc?.groupDiscussVM.isDismiss = true
        groupVc?.groupDiscussVM.backRefresh = { [ weak self] in
            self?.groupTopicVM.retrieveFilteredGroups()
        }
        self.pushVC(groupVc ?? UIViewController())
    }
    
}

extension GroupTopicViewController: UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.groupTopicVM.joinGroup(row: indexPath.row)
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize.init(width: (collectionView.frame.size.width-16)/2, height: 120)
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 16
    }
    
}
