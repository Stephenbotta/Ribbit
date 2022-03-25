//
//  ConverseOptionsViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 04/02/19.
//

import UIKit

class ConverseOptionsViewController: BaseRxViewController {

    //MARK::- OUTLETS
    
    @IBOutlet weak var labelCrossPath: UILabel!
    @IBOutlet weak var labelConverse: UILabel!
    @IBOutlet weak var imageFindSomOne: UIButton!
    @IBOutlet weak var imageNearBy: UIImageView!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnCossedPathWith: UIButton!
    @IBOutlet weak var btnFindSomeNearBy: UIButton!
    
    //MARK::- BINDINGS
    
    override func bindings() {
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)

            self?.popVC()
        })<bag
        
        btnFindSomeNearBy.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            guard let vc = R.storyboard.post.converseNearByViewController() else { return }
            vc.converseVM = ConverseNearByViewModal(type: 2)
            self?.pushVC(vc)
        })<bag
        
        btnCossedPathWith.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            
            guard let vc = R.storyboard.post.converseNearByViewController() else { return }
            vc.converseVM = ConverseNearByViewModal(type: 1)
            self?.pushVC(vc)
           
        })<bag
    }
    
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    

   

}
