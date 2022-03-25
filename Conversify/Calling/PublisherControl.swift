//
//  PublisherControl.swift
//  gpuimagetokbox
//
//  Created by cbl20 on 6/6/17.
//  Copyright © 2017 Rajat. All rights reserved.
//

import UIKit
import OpenTok

extension VoipViewController : OTPublisherDelegate {
    
    func publisher(_ publisher: OTPublisherKit, streamCreated stream: OTStream) {
       print("Stream Publishing....")
        
    }
    
    func subscriberDidReconnect(toStream subscriber: OTSubscriberKit) {
         print("Stream Reconnected: ")
    }
    func publisher(_ publisher: OTPublisherKit, streamDestroyed stream: OTStream) {
         print("Stream Destroyed: ")
    }
    func publisher(_ publisher: OTPublisherKit, didFailWithError error: OTError) {
         print("Subscriber failed: \(error.localizedDescription)")
    }
}

