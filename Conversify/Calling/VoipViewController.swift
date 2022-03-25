//
//  VoipViewController.swift
//  Buraq24
//
//  Created by Paradox on 13/08/19.
//  Copyright © 2019 CodeBrewLabs. All rights reserved.
//

import UIKit
import OpenTok
import CallKit
import AVFoundation
import RxSwift
import Foundation
import RxCocoa
import RxDataSources

class VoipViewController: UIViewController {
    
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblCallStatus: UILabel!
    @IBOutlet weak var imgView:UIImageView!
    
    var caller : Caller?
    let kWidgetHeight = 240
    let kWidgetWidth = 320
    var outGoingCall:Bool = true
    
    var session: OTSession?
    var publisher: OTPublisher?
    var stream : OTStream?
    var subscribers: [OTSubscriber] = []
    var myAudioDevice : OTAudioDeviceRingtone?
    var callTimer : Timer?
    var isSpeakerPhoneMode = false
    var isMicMute = false
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setupUI()
        if outGoingCall {
            makeCallRequest(caller: caller)
        } else {
            createOpenTokSession(caller: caller)
        }
        lblCallStatus.text = outGoingCall ? "Calling..." : "Incoming..."
    }
    
    //MARK: - Disconnect Call
    @IBAction func btnDismissCall(_ sender: UIButton) {
        //doDisconnect()
        disConnectAPI()
    }
    
    //MARK: - Setup UI
    func setupUI() {
        guard let caller = caller else { return }
        lblName.text = /caller.name
        imgView.image = caller.img

    }
    
    //MARK: - Call Request API
    func makeCallRequest(caller: Caller?) {
        
        guard let caller = caller else { return }
        
        ChatTarget.initiateCall(userId: /caller.userId).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                
                guard let resp = (response as? DictionaryResponse<MakeCallModel>) else { return }
                let data = resp.data
                self?.caller?.sessionId = data?.session_id
                self?.caller?.token = data?.token
                self?.createOpenTokSession(caller: caller)
                
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        //                        self.handleError(error: err)
                    }
            })
        
        //        chatEp.request(apiBarrier: false)
        //            .asObservable()
        //            .subscribeOn(MainScheduler.instance)
        //            .subscribe(onNext: { [weak self] (response) in
        //
        //                }, onError: { (error) in
        //                    print(error)
        //                    if let err = error as? ResponseStatus {
        //                        self.handleError(error: err)
        //                    }
        //            })<bag
    }
    
    //MARK: - Create OpenTok Session
    func createOpenTokSession(caller: Caller?) {
        
        publisher = initPublishser()
        publisher?.publishAudio = true
        publisher?.publishVideo = false
        guard let caller = caller else { return }
        
        session = OTSession(apiKey: Keys.tokBoxKey, sessionId: /caller.sessionId, delegate: self)
        
        var error: OTError?
        defer {
            process(error: error)
        }
        
        session?.connect(withToken: /caller.token, error: &error)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(60)) { [weak self] in
            guard let self = self else { return }
            if self.subscribers.count > 0 {
                debugPrint("*******VOIP Call Connected******")
            } else {
                self.disconnectWhenNoOnePicked()
            }
        }
    }
    
    fileprivate func process(error err: OTError?) {
        if let e = err {
            print("======" , e.localizedDescription)
        }
    }
    
    //MARK: - Init Publishser
    func initPublishser() -> OTPublisher? {
        let settings = OTPublisherSettings()
        settings.name = UIDevice.current.name
        settings.audioTrack = true
        settings.videoTrack = false
        settings.audioBitrate =  128000
        guard let publisher = OTPublisher(delegate: self, settings: settings) else { return nil }
        return publisher
    }
    
    func startCallTimer() {

        if callTimer != nil {return}

        var seconds = 0

        callTimer = Timer.runThisEvery(seconds: 1, handler: { [weak self] (timer) in

            seconds = seconds + 1

            self?.lblCallStatus?.text = self?.timeString(time:  Double(seconds))
        })

    }
    
    func timeString(time:TimeInterval) -> String {
       // let hours = Int(time) / 3600
        let minutes = Int(time) / 60 % 60
        let seconds = Int(time) % 60
       // return String(format:"%02i:%02i:%02i", hours, minutes, seconds)
        return String(format:"%02i:%02i", minutes, seconds)
    }
    
    
    
    @IBAction func btnToggleAudioPressed(_ sender: Any) {
        guard let button = sender as? UIButton else {return}
        
        isSpeakerPhoneMode = !isSpeakerPhoneMode
        button.setImage(isSpeakerPhoneMode ?  #imageLiteral(resourceName: "ic_speaker_on") : #imageLiteral(resourceName: "ic_speaker_off") , for: .normal)
        button.performSpringAnimation()
        
        DispatchQueue.global(qos: .background).async { [weak self] in
            
            if /self?.isSpeakerPhoneMode {
                do {
                    try AVAudioSession.sharedInstance().setCategory(.playAndRecord, mode: .default, options: .defaultToSpeaker)
                    try AVAudioSession.sharedInstance().overrideOutputAudioPort(.speaker)
                }
                catch let error {
                    print(error.localizedDescription)
                }
            }
            else {
                do {
                    try AVAudioSession.sharedInstance().setCategory(.playAndRecord, mode: .default, options: [])
                    try AVAudioSession.sharedInstance().overrideOutputAudioPort(.none)
                }
                catch let error {
                    print(error.localizedDescription)
                }
            }
        }
        
    }
    
    @IBAction func btnToggleMicPRessed(_ sender: Any) {
        guard let button = sender as? UIButton else {return}
        button.performSpringAnimation()
        publisher?.publishAudio = isMicMute
        isMicMute = !isMicMute
        button.setImage(isMicMute ? #imageLiteral(resourceName: "ic_mic_off") :#imageLiteral(resourceName: "ic_mic_on") , for: .normal)
    }
}


extension VoipViewController {
    
    /**
     * Sets up an instance of OTPublisher to use with this session. OTPubilsher
     * binds to the device camera and microphone, and will provide A/V streams
     * to the OpenTok session.
     */
    func doPublish() {
        var error: OTError?
        guard let publisher = initPublishser() else {
            print("________________Error while Publishing_________________")
            return
        }
        publisher.publishAudio = true
        publisher.publishVideo = false
        
        lblCallStatus.text = "Connected"
        self.session?.publish(publisher, error: &error)
        if error != nil {
            print("****Subscriber failed: \(/error?.localizedDescription)")
        }
       
    }
    
    /**
     * Instantiates a subscriber for the given stream and asynchronously begins the
     * process to begin receiving A/V content for this stream. Unlike doPublish,
     * this method does not add the subscriber to the view hierarchy. Instead, we
     * add the subscriber only after it has connected and begins receiving data.
     */
    func doSubscribe(_ stream: OTStream) {
        var error: OTError?
        guard let subscriber = OTSubscriber(stream: stream, delegate: self) else {
            print("Error while subscribing")
            return
        }
        
        session?.subscribe(subscriber, error: &error)
        subscribers.append(subscriber)
        lblCallStatus.text = "Connected"
        if error != nil {
            print("****Subscriber failed: \(/error?.localizedDescription)")
        }
    }
    
    /** Terminate on-going call, Disconnect OpenTok Session & dismiss view */
    func doDisconnect() {
        debugPrint("**** do disconnect called")
        lblCallStatus.text = "Disconnect"
        var error: OTError?
        if let publisher = publisher {
            publisher.publishVideo = false
            session?.unpublish(publisher, error: &error)
        }
        session?.disconnect(&error)
        
        dismiss(animated: true, completion: nil)
        
        AppDelegate.shared?.caller?.sessionId = nil
        guard let call = AppDelegate.shared?.onGoingCall, let callManager = AppDelegate.shared?.callManager else { return }
        callManager.end(call: call)
        if error != nil {
            print("****Subscriber failed: \(/error?.localizedDescription)")
        }
    }
    
    
    func disConnectAPI(){
        
        ChatTarget.callDisconnected(callerId: /Singleton.sharedInstance.loggedInUser?.id, receiverId: /caller?.userId).request(apiBarrier: true)
            .asObservable()
            .subscribeOn(MainScheduler.instance)
            .subscribe(onNext: { [weak self] (response) in
                self?.doDisconnect()
                }, onError: { (error) in
                    print(error)
                    if let err = error as? ResponseStatus {
                        //                        self.handleError(error: err)
                    }
            })

    }
    
    func apiDisconnectCall() {
        debugPrint("**** do disconnect api hit ")
    }
    
    /** Diconnect OpenTok session and dismiss view */
    func disconnectWhenNoOnePicked() {
        debugPrint("**** disconnectWhenNoOnePicked called")
        lblCallStatus.text = "No one Answered"
        var error: OTError?
        if let publisher = publisher {
            publisher.publishVideo = false
            session?.unpublish(publisher, error: &error)
        }
        session?.disconnect(&error)
        dismiss(animated: true, completion: nil)
        if error != nil {
            print("****Subscriber failed: \(/error?.localizedDescription)")
        }
    }
}


extension VoipViewController {
    
    //MARK: - Direct Audio to ear-piece
    func setAudioSession() {
        
        let audioSession = AVAudioSession.sharedInstance()
        
        do {
            try audioSession.setCategory(AVAudioSession.Category.playAndRecord, mode: AVAudioSession.Mode.voiceChat, options: [])
            try audioSession.overrideOutputAudioPort(.none)
            try audioSession.setActive(true)
        } catch {
            debugPrint(error.localizedDescription)
        }
        
        switch audioSession.category {
        case AVAudioSession.Category.playAndRecord:
            debugPrint("**************** output -> earpiece")
        default:
            debugPrint("**************** output -> speaker")
        }
    }
}

