//
//  SubscriberControl.swift
//  gpuimagetokbox
//
//  Created by cbl20 on 6/6/17.
//  Copyright © 2017 Rajat. All rights reserved.
//

import UIKit
import OpenTok
//import ISMessages

extension VoipViewController : OTSubscriberDelegate {
    
    func subscriberDidConnect(toStream subscriberKit: OTSubscriberKit) {
        print("****Subscriber Did Connect")
        myAudioDevice?.stop()
//        subscribers.first?.view?.frame = CGRect(x: 0, y: kWidgetHeight, width: kWidgetWidth, height: kWidgetHeight)
//        if let subsView = subscribers.first?.view {
//            view.addSubview(subsView)
//        }
        //lblConnecting?.isHidden = (subscribers.count != 0)
    }
    
    func subscriber(_ subscriber: OTSubscriberKit, didFailWithError error: OTError) {
        print("****Subscriber failed: \(error.localizedDescription)")
    }
    
    func subscriberVideoDataReceived(_ subscriber: OTSubscriber) {
        print("Subscriber data receiving")
    }
    
    func subscriberDidDisconnect(fromStream subscriber: OTSubscriberKit) {
        print("**** Subscriber Did Disconnect")
        myAudioDevice?.stop()
        //lblConnecting?.isHidden = (subscribers.count != 0)
    }
    
    func subscriberVideoDisabled(_ subscriber: OTSubscriberKit, reason: OTSubscriberVideoEventReason) {
        
        /*if reason.rawValue == 1 {
            ISMessages.hideAlert(animated: true)
            
            ISMessages.showCardAlert(withTitle: "WorkCocoon", message: "Video is disabled", duration: 0.5, hideOnSwipe: true, hideOnTap: true, alertType: ISAlertType.info, alertPosition: .top, didHide: nil)
        }
        else if reason.rawValue == 3 {
            ISMessages.hideAlert(animated: true)
            
            ISMessages.showCardAlert(withTitle: "WorkCocoon", message: "Poor Connection, Video is disabled.", duration: 0.5, hideOnSwipe: true, hideOnTap: true, alertType: ISAlertType.info, alertPosition: .top, didHide: nil)
        }*/
    }
}
