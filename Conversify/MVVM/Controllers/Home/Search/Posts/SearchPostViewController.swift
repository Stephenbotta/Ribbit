//
//  SearchPostViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 03/01/19.
//

import UIKit
import XLPagerTabStrip
import SNCollectionViewLayout

class SearchPostViewController: BaseRxViewController {
    
    //MARK::- COLLECTIONVIEW
    
    @IBOutlet weak var collectionView: UICollectionView!
    
    //MARK::- PROPERTIES
    var searchPostVM = SearchPostViewModal()
    
    
    //MARK:- VIEW CYCLE
    
    
    override func bindings() {
        searchPostVM.resetTable.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (status) in
                if status{
                    self?.collectionView.es.resetNoMoreData()
                }
            })<bag
        searchPostVM.noMoreData.filter { [weak self] (_) -> Bool in
            self?.collectionView.es.noticeNoMoreData()
            self?.collectionView.es.stopPullToRefresh()
            return true
            }.subscribe(onNext: { [weak self] (status) in
                if status{
                    self?.collectionView.es.noticeNoMoreData()
                    self?.collectionView.es.stopPullToRefresh()
                }
            })<bag
        let snCollectionViewLayout = SNCollectionViewLayout()
        snCollectionViewLayout.fixedDivisionCount = 3 // Columns for .vertical, rows for .horizontal
        
        snCollectionViewLayout.delegate = self
        collectionView.collectionViewLayout = snCollectionViewLayout
        addPaging()
        searchPostVM.getPosts()
        searchPostVM.items.asObservable()
            .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.searchPostCollectionViewCell.identifier, cellType: SearchPostCollectionViewCell.self)) {  (row,element,cell) in
                cell.post = element
            }<bag
        collectionView.rx.setDelegate(self)<bag
        collectionView.rx.itemSelected.subscribe { [weak self] (indexPath) in
            self?.getPostDetails(id: /self?.searchPostVM.items.value[/indexPath.element?.row].id)
            }<bag
        
        searchPostVM.hideCollection.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                self?.collectionView.isHidden = bool
                
            })<bag
    }
    
    
    
    func addPaging(){
        collectionView.es.addInfiniteScrolling {
            if self.searchPostVM.loadMore {
                self.searchPostVM.page = self.searchPostVM.page + 1
                self.searchPostVM.getPosts()
            }else {
                self.collectionView.es.stopLoadingMore()
                self.collectionView.es.noticeNoMoreData()
            }
        }
    }
    
    func getPostDetails(id: String){
        guard let vc = R.storyboard.post.postDetailViewController() else { return }
        vc.postDetailViewModal.postId.value = /id
        UIApplication.topViewController()?.pushVC(vc)
    }
    
}

extension SearchPostViewController: UICollectionViewDelegateFlowLayout , SNCollectionViewLayoutDelegate  , UICollectionViewDelegate {
    
    func scaleForItem(inCollectionView collectionView: UICollectionView, withLayout layout: UICollectionViewLayout, atIndexPath indexPath: IndexPath) -> UInt {
        if indexPath.row % 4 == 0{
            return 2
        }
        return 1
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 4
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 4
    }
    
}


extension SearchPostViewController : IndicatorInfoProvider{
    
    func indicatorInfo(for pagerTabStripController: PagerTabStripViewController) -> IndicatorInfo {
        return IndicatorInfo(title: "POSTS" )
    }
    
    
}
