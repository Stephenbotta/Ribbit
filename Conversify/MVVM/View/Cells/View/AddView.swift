//
//  AddView.swift
//  StarFlip
//
//  Created by admin on 23/07/21.
//

import UIKit

class AddView: UIView {
    
    @IBOutlet var viewContainer: UIView!
    @IBOutlet weak var viewpopup: UIView!
//    @IBOutlet weak var btnWatch: UIButton!
//    @IBOutlet weak var btnCancle: UIButton!
    @IBOutlet weak var lblChalenge: UILabel!
    @IBOutlet weak var lblrewardPoint: UILabel!
    @IBOutlet weak var lblDiscription: UILabel!
    @IBOutlet weak var btnOk: UIButton!
    override init(frame: CGRect) {
            super.init(frame: frame)
            loadViewFromNib()
        }
        required init?(coder aDecoder: NSCoder) {
            super.init(coder: aDecoder)
            loadViewFromNib()
        }
    func loadViewFromNib() {
      
        viewContainer = Bundle.main.loadNibNamed("AddView", owner: self, options: nil)?[0] as? UIView
        addSubview(viewContainer)
        viewContainer.frame = self.bounds
        
        }
}
