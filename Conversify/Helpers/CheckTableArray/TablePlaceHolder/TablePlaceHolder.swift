//
//  TablePlaceHolder.swift
//  Connect
//
//  Created by OSX on 29/12/17.
//  Copyright © 2017 OSX. All rights reserved.
//


import UIKit
import RxSwift
import RxCocoa
import Material
import Foundation


class TablePlaceHolder: UIView {
    
    @IBOutlet weak var placeHolderImage: UIImageView!
    @IBOutlet weak var lblMessage: UILabel!
    @IBOutlet weak var btnPlaceHolderAction: Button!
    
    let bag = DisposeBag()
    var buttonAction:(()->())?
    
    // MARK: - Initializers
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupView()
    }
    
    // MARK: - Private Helper Methods
    
    // Performs the initial setup.
    private func setupView() {
        let view = viewFromNibForClass()
        view.frame = bounds
        
        // Auto-layout stuff.
        view.autoresizingMask = [
            UIView.AutoresizingMask.flexibleWidth,
            UIView.AutoresizingMask.flexibleHeight
        ]
        
        self.bindings()
        
        // Show the view.
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

extension TablePlaceHolder {
    
    func bindings() {
        
        let tapAction = self.btnPlaceHolderAction.rx.tap
        tapAction.asDriver().drive(onNext: { [weak self] () in
            self?.buttonAction?()
        })<bag
        
    }
    
}

