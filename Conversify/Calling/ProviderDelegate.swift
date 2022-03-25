/*
 Copyright (C) 2016 Apple Inc. All Rights Reserved.
 See LICENSE.txt for this sample’s licensing information
 
 Abstract:
 CallKit provider delegate class, which conforms to CXProviderDelegate protocol
 */

import Foundation
import UIKit
import CallKit
import AVFoundation
import OpenTok

final class ProviderDelegate: NSObject, CXProviderDelegate {
    
    let callManager: SpeakerboxCallManager
    // private let provider: CXProvider
    let provider: CXProvider
    let callKitCallController: CXCallController
    var isVideoCall : Bool = false
    //var doctor : DoctorData?
    var currentCallUUID: UUID?
    var callHasConnected : Bool = false
    var inputAudio: AVAudioSessionPortDescription!
    var audioSession : AVAudioSession?
    var callerId : String?
    var callerName:String?
    var token:String? //OpenTok token
    var session_id:String? //OpenTok session_id
    
    init(callManager: SpeakerboxCallManager) {
        self.callManager = callManager
        provider = CXProvider(configuration: type(of: self).providerConfiguration)
        callKitCallController = CXCallController()
        super.init()
        
        provider.setDelegate(self, queue: nil)
    }
    
    deinit {
        // CallKit has an odd API contract where the developer must call invalidate or the CXProvider is leaked.
        provider.invalidate()
    }
    
    /// The app's provider configuration, representing its CallKit capabilities
    static var providerConfiguration: CXProviderConfiguration {
        let localizedName = NSLocalizedString("Ribbit", comment: "Ribbit")
        let providerConfiguration = CXProviderConfiguration(localizedName: localizedName)
        
        providerConfiguration.supportsVideo = true
        
        providerConfiguration.maximumCallsPerCallGroup = 1
        
        providerConfiguration.supportedHandleTypes = [.phoneNumber]
        
        //        providerConfiguration.iconTemplateImageData = #imageLiteral(resourceName: "ic_mic_on").pngData()
        
        providerConfiguration.ringtoneSound = "Ringtone.caf"
        
        return providerConfiguration
    }
    
    // MARK: Incoming Calls
    
    /// Use CXProvider to report the incoming call to the system
    func reportIncomingCall(uuid: UUID, handle: String, hasVideo: Bool = false, session_id: String, token:String, userId : String?, completion: ((NSError?) -> Void)? = nil) {
        // Construct a CXCallUpdate describing the incoming call, including the caller.
        let update = CXCallUpdate()
       // update.remoteHandle = CXHandle(type: .generic, value: handle)
        let callHandle = CXHandle(type: .generic, value: handle)
        let startCallAction = CXStartCallAction(call: uuid, handle: callHandle)
        let transaction = CXTransaction(action: startCallAction)
        update.remoteHandle = callHandle
        update.hasVideo = hasVideo
        update.supportsHolding = false
        update.supportsGrouping = false
        
        print(handle)
        // pre-heat the AVAudioSession
        //OTAudioDeviceManager.setAudioDevice(OTDefaultAudioDevice.sharedInstance())
        
        // Report the incoming call to the system
        
        
        //         configureAudioSession()
        
        self.token = token
        self.callerName = handle
        self.session_id = session_id
        self.callerId = userId
        provider.reportNewIncomingCall(with: uuid, update: update) { error in
            /*
             Only add incoming call to the app's list of calls if the call was allowed (i.e. there was no error)
             since calls may be "denied" for various legitimate reasons. See CXErrorCodeIncomingCallError.
             */
            if error == nil {
                let call = SpeakerboxCall(uuid: uuid, token: token, sessionId: session_id)
                call.isVideoCall = self.isVideoCall
                self.callManager.addCall(call)
                self.currentCallUUID = uuid
            }
            completion?(error as NSError?)
        }
    }
    
    // MARK: CXProviderDelegate
    
    func providerDidReset(_ provider: CXProvider) {
        print("Provider did reset")
        /*
         End any ongoing calls if the provider resets, and remove them from the app's list of calls,
         since they are no longer valid.
         */
    }
    
    var outgoingCall: SpeakerboxCall?
    
    func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
        // Create & configure an instance of SpeakerboxCall, the app's model class representing the new outgoing call.
        let call = SpeakerboxCall(uuid: action.callUUID, isOutgoing: true, token: /self.token, sessionId: /self.session_id)
        call.isVideoCall = isVideoCall
        call.handle = action.handle.value
        
        call.hasStartedConnectingDidChange = { [weak self] in
            self?.provider.reportOutgoingCall(with: call.uuid, startedConnectingAt: call.connectingDate)
        }
        
        call.hasConnectedDidChange = { [weak self] in
             self?.provider.reportOutgoingCall(with: call.uuid, connectedAt: Date())
             action.fulfill()
        }
        
        self.callHasConnected = true
        
        self.outgoingCall = call
        
        // Signal to the system that the action has been successfully performed.
        action.fulfill()
    }
    
    var answerCall: SpeakerboxCall?
    func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        
        // Retrieve the SpeakerboxCall instance corresponding to the action's call UUID
        currentCallUUID = action.callUUID
        
        guard let call = callManager.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        call.isVideoCall = isVideoCall
        AppDelegate.shared?.onGoingCall = call
        /*
         Configure the audio session, but do not start call audio here, since it must be done once
         the audio session has been activated by the system after having its priority elevated.
         */
        
        // https://forums.developer.apple.com/thread/64544
        // we can't configure the audio session here for the case of launching it from locked screen
        // instead, we have to pre-heat the AVAudioSession by configuring as early as possible, didActivate do not get called otherwise
        // please look for  * pre-heat the AVAudioSession *
        //        OTAudioDeviceManager.setAudioDevice(OTDefaultAudioDevice.sharedInstance())
        //        OTAudioDeviceManager.setAudioDevice(OTDefaultAudioDevice.sharedInstanceWithAudioSession(withCustomAudioPort: AVAudioSession.sharedInstance() , isVideoCall: isVideoCall))
        
        configureAudioSession()
        
        AppDelegate.shared?.callManager = callManager
        // Signal to the system that the action has been successfully performed.
        action.fulfill()
        
        if let vc = R.storyboard.main.voipViewController() {
            vc.caller = Caller(name: /self.callerName, token: /self.token, sessionId: /self.session_id, callUUID: action.callUUID.uuidString , userId: /self.callerId)
            vc.outGoingCall = false
            vc.modalPresentationStyle = .overFullScreen
            self.callHasConnected = true
            UIApplication.topViewController()?.present(vc, animated: true, completion: nil)
        }
        
//         configureAudioSession()
        
        if !callHasConnected {
            
            let appDelegate = UIApplication.shared.delegate as! AppDelegate
            appDelegate.hitApiConnectDisconnectCall(notify: true, connect : false)
        }
        
    }
    
    
    func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        // Retrieve the SpeakerboxCall instance corresponding to the action's call UUID
        guard let call = callManager.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        
        // Trigger the call to be ended via the underlying network service.
        call.endCall()
        
        // Signal to the system that the action has been successfully performed.
        action.fulfill()
        
        // Remove the ended call from the app's list of calls.
        callManager.removeCall(call)
        outgoingCall?.endCall()
        outgoingCall = nil
        answerCall?.endCall()
        answerCall = nil
        callManager.removeAllCalls()
        AppDelegate.shared?.caller = nil
        
        if !callHasConnected {
            let appDelegate = UIApplication.shared.delegate as! AppDelegate
            appDelegate.hitApiConnectDisconnectCall(notify: true , connect : false)
        }
        
        
    }
    
    func provider(_ provider: CXProvider, perform action: CXSetHeldCallAction) {
        // Retrieve the SpeakerboxCall instance corresponding to the action's call UUID
        guard let call = callManager.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        
        // Update the SpeakerboxCall's underlying hold state.
        call.isOnHold = action.isOnHold
        
        // Stop or start audio in response to holding or unholding the call.
        call.isMuted = call.isOnHold
        
        // Signal to the system that the action has been successfully performed.
        action.fulfill()
    }
    
    func provider(_ provider: CXProvider, perform action: CXSetMutedCallAction) {
        // Retrieve the SpeakerboxCall instance corresponding to the action's call UUID
        guard let call = callManager.callWithUUID(uuid: action.callUUID) else {
            action.fail()
            return
        }
        
        call.isMuted = action.isMuted
        
        // Signal to the system that the action has been successfully performed.
        action.fulfill()
    }
    
    func provider(_ provider: CXProvider, timedOutPerforming action: CXAction) {
        print("Timed out \(#function)")
        
        // React to the action timeout if necessary, such as showing an error UI.
    }
    
    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        
        print("Received \(#function)")
        
        // Start call audio media, now that the audio session has been activated after having its priority boosted.
        outgoingCall?.startCall(withAudioSession: audioSession) { success in
            if success {
                self.callManager.addCall(self.outgoingCall!)
                self.outgoingCall?.startAudio()
            }
        }
        
        answerCall?.answerCall(withAudioSession: audioSession) { success in
            if success {
                if !self.isVideoCall {
                    self.answerCall?.startAudio()
                }
            }
        }
    }
    
    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        print("Received \(#function)")
        
        /*
         Restart any non-call related audio now that the app's audio session has been
         de-activated after having its priority restored to normal.
         */
        outgoingCall?.endCall()
        outgoingCall = nil
        answerCall?.endCall()
        answerCall = nil
        callManager.removeAllCalls()
    }
    
    func configureAudioSession() {
        // See https://forums.developer.apple.com/thread/64544
        audioSession = AVAudioSession.sharedInstance()
        //        try? audioSession?.setCategory(.playAndRecord, mode: .default, options: .defaultToSpeaker)//(AVAudioSession.Category.playAndRecord)
        //        try? audioSession?.setMode(AVAudioSession.Mode.voiceChat)
        //        try? audioSession?.setPreferredSampleRate(44100.0)
        //        try? audioSession?.setPreferredIOBufferDuration(0.005)
        if self.isVideoCall {
            do {
                try AVAudioSession.sharedInstance().setCategory(AVAudioSession.Category.playAndRecord, mode: AVAudioSession.Mode.voiceChat, options: AVAudioSession.CategoryOptions.mixWithOthers)
                try AVAudioSession.sharedInstance().overrideOutputAudioPort(.none)
                try AVAudioSession.sharedInstance().setActive(true)
                //                try AVAudioSession.sharedInstance().setPreferredSampleRate(44100.0)
                //                try AVAudioSession.sharedInstance().setPreferredIOBufferDuration(0.005)

            }
            catch let error {
                print(error.localizedDescription)
            }
        }
        else {
            do {
                try AVAudioSession.sharedInstance().setCategory(AVAudioSession.Category.playAndRecord, mode: AVAudioSession.Mode.voiceChat, options: AVAudioSession.CategoryOptions.mixWithOthers)
                try AVAudioSession.sharedInstance().overrideOutputAudioPort(.none)
                try AVAudioSession.sharedInstance().setActive(true)
                //                try AVAudioSession.sharedInstance().setPreferredSampleRate(44100.0)
                //                try AVAudioSession.sharedInstance().setPreferredIOBufferDuration(0.005)

            }
            catch let error {
                print(error.localizedDescription)
            }
        }


    }
    
    func checkAndOverrideAudioPort() {
        
        do {
            try audioSession?.overrideOutputAudioPort(isVideoCall ? .speaker : .none)
        }
        catch let error {
            print(error.localizedDescription)
        }
        
        
        
    }
    
    
    //    func configureAudioSession(){
    //
    //        // Fetch Built in Mic
    //
    //        let session = AVAudioSession.sharedInstance()
    //
    //        do {
    //
    //            try? session.setCategory(AVAudioSessionCategoryPlayAndRecord)
    //
    //            try? session.setMode(AVAudioSessionModeDefault)
    //
    //            try? session.overrideOutputAudioPort(isVideoCall ? .speaker : .none)
    //
    //            try? session.setActive(true)
    //
    //        }catch _ {
    //
    //            debugPrint("audio port error")
    //
    //        }
    //
    //        if let availableInputs = session.availableInputs {
    //
    //            for inputSource in availableInputs {
    //
    //                if inputSource.portType == AVAudioSessionPortBuiltInMic {
    //
    //                    inputAudio = inputSource
    //
    //                    break
    //                }
    //            }
    //        }
    //
    //        do {
    //            debugPrint(inputAudio.dataSources)
    //        }
    //
    //    }
    
    
    func endCurrentCall() {
        
        let endCallAction = CXEndCallAction(call: currentCallUUID ?? UUID())
        
        guard let call = callManager.callWithUUID(uuid: endCallAction.callUUID) else {
            endCallAction.fail()
            
            return
        }
        
        // Remove the ended call from the app's list of calls.
        callManager.end(call: call)
    }
    
    }


//
////MARK:- VIDEO CALL VC DELEGATES
//extension ProviderDelegate: VideoCallVcCallStatusDelegates  {
//
//    func consultationTimeOver() {
//
//    }
//
//
//    func didDisconnectCall(callType: Int, isMissedCall: Bool) {
//
//        audioSession = nil
//
////        if let vc = UIView.vc(vcKind: VideoCallViewController.self) {
////            if let chatVC = UIView.vc(vcKind: ChatVC.self) {
////                //                let callType = (/vc.callType == CallType.Video.rawValue) ? ChatMessageType.Video.rawValue :  ChatMessageType.Audio.rawValue
////                //                chatVC.didDisconnectCall(callType: callType, isMissedCall: !vc.isCallConnected)
////                //                let appDelegate = UIApplication.shared.delegate as! AppDelegate
////                //                appDelegate.hitApiConnectDisconnectCall(notify: true)
////            }
////        }
//
//    }
//
//}
