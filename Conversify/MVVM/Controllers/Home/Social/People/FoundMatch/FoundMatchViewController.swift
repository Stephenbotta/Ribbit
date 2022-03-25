//
//  FoundMatchViewController.swift
//  Conversify
//
//  Created by cbl24_Mac_mini on 24/01/19.
//

import UIKit

class FoundMatchViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    @IBOutlet weak var btnCancel: UIButton!
    @IBOutlet weak var btnOkay: UIButton!
    @IBOutlet weak var labelMessage: UILabel!
    @IBOutlet weak var imageUser: UIImageView!
    @IBOutlet weak var constraintHeightShowPost: NSLayoutConstraint!
    @IBOutlet weak var btnShowPost: UIButton!
    @IBOutlet weak var viewMap: GMSMapView!
    
    //MARK::- PROPERTIES
    var userId: String?
    var image: String?
    var message: String?
    var unDimProfile: (() -> ())?
    var unDimPost: (() -> ())?
    var unDimDismiss: (() -> ())?
    var isPost: Bool?
    var postId: String?
    var lat : Double?
    var lng : Double?
    var userName: String?
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        constraintHeightShowPost.constant =  /isPost ? 40 : 0
        loadInfo()
        
    }
   
    
    override func bindings() {
        
        btnShowPost.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.dismissVC(completion: {
                self?.unDimPost?()
            })
        })<bag
        
        btnOkay.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.dismissVC(completion: {
                self?.unDimProfile?()
            })
        })<bag
        
        btnCancel.rx.tap.asDriver().drive(onNext: { [weak self] () in
            self?.dismissVC(completion: {
                self?.unDimDismiss?()
            })
        })<bag
        
    }
    
    
    //MARK::- FUNCTIONS
    func loadInfo(){
        let position = CLLocationCoordinate2D(latitude: /lat, longitude: /lng)
        let marker = GMSMarker(position: position)
        marker.title = /userName
        marker.map = viewMap
        let camera = GMSCameraPosition.camera(withLatitude: /lat, longitude: /lng, zoom: 16.0)
        viewMap.camera = camera
        imageUser?.image(url:  /image )//.kf.setImage(with: URL(string: /image), placeholder: R.image.ic_account())
        labelMessage?.text = /message
    }
    
    func getPostDetails(){
        guard let vc = R.storyboard.post.postDetailViewController() else { return }
        vc.postDetailViewModal.postId.value = /postId
        UIApplication.topViewController()?.pushVC(vc)
    }
    
    
    
}
