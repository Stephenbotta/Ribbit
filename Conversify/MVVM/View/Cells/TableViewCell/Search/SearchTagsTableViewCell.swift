//
//  SearchTagsTableViewCell.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 03/01/19.
//

import UIKit
import IBAnimatable

class SearchTagsTableViewCell: BaseTableViewCell {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var btnFollow: AnimatableButton!
    @IBOutlet weak var labelName: UILabel!
    
    //MARK::- PROPERTIES
    
    var searchTagVM = SearchTagsTableViewModal()
    var tagVal: Tags?{
        didSet{
            labelName?.text = /tagVal?.tagName
            btnFollow.isSelected = (/tagVal?.isFollowing)
        }
    }
    var updateFollow:((Int,Bool)->())?
    
    
    override func bindings() {
        
        searchTagVM.updated.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool{
                    self?.updateFollow?(/self?.btnFollow.tag, (/self?.btnFollow.isSelected))
                }
                if !bool {
                    self?.btnFollow.isSelected = !(/self?.tagVal?.isFollowing)
                }
            })<bag
        
        btnFollow.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.btnFollow.isSelected = /self?.btnFollow.isSelected.toggle()
            self?.searchTagVM.followTag(tagId: /self?.tagVal?.id , follow: String((/self?.btnFollow.isSelected)))
        })<bag
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}
