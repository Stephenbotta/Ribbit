//
//  SenderAudioCell.swift
//  Conversify
//
//  Created by Apple on 12/12/19.
//

import UIKit

class SenderAudioCell: BaseChatCell {
    
    //MARK::- IBOutlets
    @IBOutlet weak var audioProgressBar: UISlider?
    @IBOutlet weak var viewBase: UIView!
    @IBOutlet weak var btnPlay: UIButton?
    @IBOutlet weak var lblAudioTime: UILabel!
    
    
    
    @IBAction func btnActionPlay(_ sender: UIButton) {
        
        if /AppDelegate.shared?.senderPlayer?.isPlaying{
            AppDelegate.shared?.senderPlayer?.stop()
        }else {
        playAudio()
        Timer.scheduledTimer(timeInterval:1, target: self, selector: #selector(self.updateAudioProgressView), userInfo: nil, repeats: true)
        }
    }
    
    func playAudio(){
        print(item?.mesgDetail?.audioMsg?.audioUrl)//"https://ribbitrewardsdev.s3.us-west-2.amazonaws.com/REC_8d3cd167-e912-4195-b5b0-dfa32e9545ec.m4a""
        
        if let url = item?.mesgDetail?.audioMsg?.audioUrl as? String {
            guard  let url = URL.init(string: url) else {
                return
            }
            downloadFileFromURL(url: url)
        }
//        else if let url = item?.mesgDetail?.audioMsg?.audioUrl as? URL{
//            do {
//                player = try AVAudioPlayer(contentsOf: url)
//                player?.play()
//            }catch {
//                
//            }
//        }
        
    }
    
    func downloadFileFromURL(url:URL){
        
//        var downloadTask:URLSessionDownloadTask
//        downloadTask = URLSession.shared.downloadTask(with: url, completionHandler: { (url, response, error) in
//            if let url = url {
//                self.play(url: url)
//
//            }
//        })
//
//        downloadTask.resume()
//
        
        let fileManager = FileManager.default
        let name = "\(url.lastPathComponent)"

        do {
            let documentDirectory = try fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor:nil, create:false)
            let fileURL = documentDirectory.appendingPathComponent(name)
            
            if FileManager.default.fileExists(atPath: fileURL.path) {
                do {
                    let data = try Data(contentsOf: fileURL, options: [.dataReadingMapped, .uncached])
                    self.play(url: fileURL, data: data)
                    return
                } catch {
                    print(error)
                }
            }
            
        } catch {
            print(error)
        }
        let task = URLSession.shared.downloadTask(with: url) { localURL, urlResponse, error in
            if let localURL = localURL {
                
                do {
                    
                    let documentDirectory = try fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor:nil, create:false)
                    let fileURL = documentDirectory.appendingPathComponent(name)

                    do {
                        let data = try Data(contentsOf: localURL, options: [.dataReadingMapped, .uncached])
                        try data.write(to: fileURL)
                        self.play(url: fileURL, data: data)

                    } catch {
                        print(error)
                    }
                } catch {
                    print(error)
                }
            }
        }

        task.resume()
    }
    
    func play(url:URL, data: Data) {
        print("playing \(url)")
        
        do {
            
            AppDelegate.shared?.senderPlayer = try AVAudioPlayer.init(data: data)
            //self.player = try AVAudioPlayer(contentsOf: url)
            AppDelegate.shared?.senderPlayer?.prepareToPlay()
            AppDelegate.shared?.senderPlayer?.volume = 1.0
            AppDelegate.shared?.senderPlayer?.play()
       
            DispatchQueue.main.async {
                self.audioProgressBar?.maximumValue = Float(/self.item?.mesgDetail?.audioDuration/1000)
            }
//           audioProgressBar.
//        setProgress(Float(player?.currentTime/player?.duration), animated: false)
        } catch let error as NSError {
            //self.player = nil
            print(error.localizedDescription)
        } 
        
    }
    
    @objc func updateAudioProgressView()
    {
        if /AppDelegate.shared?.senderPlayer?.isPlaying
        {
            // Update progress
            audioProgressBar?.value = Float(/AppDelegate.shared?.senderPlayer?.currentTime)
            btnPlay?.setImage(#imageLiteral(resourceName: "pause-button"), for: .normal)
        }else {
            audioProgressBar?.value = 0
            btnPlay?.setImage(#imageLiteral(resourceName: "ic_audioPlay"), for: .normal)
        }
    }
    
}

