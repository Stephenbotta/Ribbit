//
//  GoogleAdCell.swift
//  Conversify
//
//  Created by admin on 14/04/21.
//

import UIKit
import GoogleMobileAds

class GoogleAdCell: UITableViewCell  {
    @IBOutlet var nativeAdView: GADNativeAdView!
    var adLoader: GADAdLoader!
    var heightConstraint: NSLayoutConstraint?
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
  
    
    func showNativeAd(rootVC: UIViewController) {
        adLoader = GADAdLoader(adUnitID: Keys.adUnitID,//Keys.adUnitID,
                               rootViewController: rootVC,
                               adTypes: [ GADAdLoaderAdType.native],
                               options: [])
        adLoader?.delegate = self
        adLoader?.load(GADRequest())
    }
}
extension GoogleAdCell : GADAdLoaderDelegate, GADCustomNativeAdLoaderDelegate,GADNativeAdLoaderDelegate{
    func customNativeAdFormatIDs(for adLoader: GADAdLoader) -> [String] {
        return []
    }
    
    
    
    func adLoader(_ adLoader: GADAdLoader, didReceive nativeAd: GADNativeAd) {
      print("Received native ad: \(nativeAd)")
      
      // Set ourselves as the native ad delegate to be notified of native ad events.
      nativeAd.delegate = self

      // Populate the native ad view with the native ad assets.
      // The headline and mediaContent are guaranteed to be present in every native ad.
      (nativeAdView.headlineView as? UILabel)?.text = nativeAd.headline
      nativeAdView.mediaView?.mediaContent = nativeAd.mediaContent

      // This app uses a fixed width for the GADMediaView and changes its height to match the aspect
      // ratio of the media it displays.
      if let mediaView = nativeAdView.mediaView, nativeAd.mediaContent.aspectRatio > 0 {
        let heightConstraint = NSLayoutConstraint(
          item: mediaView,
          attribute: .height,
          relatedBy: .equal,
          toItem: mediaView,
          attribute: .width,
          multiplier: CGFloat(1 / nativeAd.mediaContent.aspectRatio),
          constant: 0)
        heightConstraint.isActive = true
      }

      // These assets are not guaranteed to be present. Check that they are before
      // showing or hiding them.
      (nativeAdView.bodyView as? UILabel)?.text = nativeAd.body
      nativeAdView.bodyView?.isHidden = nativeAd.body == nil

      (nativeAdView.callToActionView as? UIButton)?.setTitle(nativeAd.callToAction, for: .normal)
      nativeAdView.callToActionView?.isHidden = nativeAd.callToAction == nil

      (nativeAdView.iconView as? UIImageView)?.image = nativeAd.icon?.image
      nativeAdView.iconView?.isHidden = nativeAd.icon == nil

      (nativeAdView.starRatingView as? UIImageView)?.image = imageOfStars(
        from: nativeAd.starRating)
      nativeAdView.starRatingView?.isHidden = nativeAd.starRating == nil

      (nativeAdView.storeView as? UILabel)?.text = nativeAd.store
      nativeAdView.storeView?.isHidden = nativeAd.store == nil

      (nativeAdView.priceView as? UILabel)?.text = nativeAd.price
      nativeAdView.priceView?.isHidden = nativeAd.price == nil

      (nativeAdView.advertiserView as? UILabel)?.text = nativeAd.advertiser
      nativeAdView.advertiserView?.isHidden = nativeAd.advertiser == nil

      // In order for the SDK to process touch events properly, user interaction should be disabled.
      nativeAdView.callToActionView?.isUserInteractionEnabled = false

      // Associate the native ad view with the native ad object. This is
      // required to make the ad clickable.
      // Note: this should always be done after populating the ad views.
      nativeAdView.nativeAd = nativeAd
    }
    
    func adLoader(_ adLoader: GADAdLoader, didReceive customNativeAd: GADCustomNativeAd) {
        print("fewrfeferf")
    }
}
extension GoogleAdCell :  GADVideoControllerDelegate, GADNativeAdDelegate{
   
    
      func adLoaderDidFinishLoading(_ adLoader: GADAdLoader) {
          // The adLoader has finished loading ads, and a new request can be sent.
      }
    
    func adLoader(_ adLoader: GADAdLoader, didFailToReceiveAdWithError error: Error) {
        
        
    }
    
    func nativeAdDidRecordImpression(_ nativeAd: GADNativeAd) {
      // The native ad was shown.
    }

    func nativeAdDidRecordClick(_ nativeAd: GADNativeAd) {
      // The native ad was clicked on.
    }

    func nativeAdWillPresentScreen(_ nativeAd: GADNativeAd) {
      // The native ad will present a full screen view.
    }

    func nativeAdWillDismissScreen(_ nativeAd: GADNativeAd) {
      // The native ad will dismiss a full screen view.
    }

    func nativeAdDidDismissScreen(_ nativeAd: GADNativeAd) {
      // The native ad did dismiss a full screen view.
    }

    func nativeAdWillLeaveApplication(_ nativeAd: GADNativeAd) {
      // The native ad will cause the application to become inactive and
      // open a new application.
    }
    

    func imageOfStars(from starRating: NSDecimalNumber?) -> UIImage? {
      guard let rating = starRating?.doubleValue else {
        return nil
      }
      if rating >= 5 {
        return UIImage(named: "stars_5")
      } else if rating >= 4.5 {
        return UIImage(named: "stars_4_5")
      } else if rating >= 4 {
        return UIImage(named: "stars_4")
      } else if rating >= 3.5 {
        return UIImage(named: "stars_3_5")
      } else {
        return nil
      }
    }
  
}
