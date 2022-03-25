//
//  PreviewMediaViewController.swift
//  Conversify
//
//  Created by Harminder on 31/07/19.
//

import UIKit
import Lightbox

class PreviewMediaViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var collectionView: UICollectionView!
    
    //MARK::- PROPERTIES
    var previewVM = PreviewMediaViewModal()
    
    //MARK::- VIEW CYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
    }
    
    //MARK::- BINDINGS
    
    override func bindings() {
        previewVM.media
            .asObservable()
            .bind(to: collectionView.rx.items(cellIdentifier: R.reuseIdentifier.selectedImageCollectionViewCell.identifier, cellType: SelectedImageCollectionViewCell.self)) {  (row,element,cell) in
                cell.tableRow = row
                cell.media = element
                cell.imageCross?.tag = row
            }<bag
        
        collectionView.rx.setDelegate(self)<bag
        
        btnBack.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.dismissVC(completion: nil)
        })<bag
        
    }
    
    //PREVIEW
    func showLightbox(media: Media , isVideo:Bool) {
        var data = [LightboxImage]()
        if isVideo{
            guard let imgUrl = URL.init(string: /media.thumbnail) , let videoUrl = URL.init(string: /media.videoUrl) else { return }
            data = [ LightboxImage(
                imageURL: imgUrl ,
                text: "",
                videoURL: videoUrl)
            ]
        }else{
            guard let imgUrl = URL.init(string: /media.thumbnail) else { return }
            data = [LightboxImage(imageURL: imgUrl)]
        }
        
        let controller = LightboxController(images: data )
        controller.dynamicBackground = true
        controller.modalPresentationStyle = .fullScreen
        UIApplication.topViewController()?.presentVC(controller)
        
    }
    
}

extension PreviewMediaViewController : UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let media = previewVM.media.value[indexPath.row] as? Media else { return }
        switch /media.mediaType{
        case "VIDEO":
            showLightbox(media: media, isVideo: true)
        case "IMAGE":
            showLightbox(media: media, isVideo: false)
        case "GIF":
            guard let gifUrl = URL(string: /media.original), let vc = R.storyboard.home.expandedGifViewController() else { return }
            vc.imageUrl = gifUrl
            UIApplication.topViewController()?.presentVC(vc)
        default:
            break
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: UIScreen.main.bounds.width  , height: UIScreen.main.bounds.width)
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0.0
    }
    
    func collectionView(_ collectionView: UICollectionView, layout
        collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 0.0
    }
    
}
