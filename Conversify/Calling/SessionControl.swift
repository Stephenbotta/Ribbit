//
//  SessionControl.swift
//  gpuimagetokbox
//
//  Created by cbl20 on 6/6/17.
//  Copyright © 2017 Rajat. All rights reserved.
//

import UIKit
import OpenTok


extension VoipViewController : OTSessionDelegate {
    
    func sessionDidConnect(_ session: OTSession) {
        print("**** Session connected \(session.sessionId)")
        doPublish()
    }
    
    func sessionDidDisconnect(_ session: OTSession) {
        print("**** Session disconnected \(session.sessionId)")
    }
    
    func session(_ session: OTSession, streamCreated stream: OTStream) {
        print("**** Session streamCreated : \(stream.streamId)")
        if outGoingCall { setAudioSession() }
        doSubscribe(stream)
        startCallTimer()
    }
    
    func session(_ session: OTSession, streamDestroyed stream: OTStream) {
        print("****Session streamDestroyed with session id: \(session.sessionId) and stream id: \(stream.streamId)")
        cleanupSubscriber(stream)
    }
    
    func session(_ session: OTSession, didFailWithError error: OTError) {
        print("****session Failed to connect: \(error.localizedDescription)")
    }
    
    fileprivate func cleanupSubscriber(_ stream: OTStream) {
        UIDevice.current.isProximityMonitoringEnabled = false
        subscribers = subscribers.filter { $0.stream?.streamId != stream.streamId }
        debugPrint("**** cleanupSubscriber called ")
        if subscribers.count == 0 {
            debugPrint("**** cleanupSubscriber subscriber count == 0")
            doDisconnect()
        }
    }
    
}
