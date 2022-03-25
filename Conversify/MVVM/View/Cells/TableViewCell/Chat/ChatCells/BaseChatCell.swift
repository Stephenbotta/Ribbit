//
//  BaseChatCell.swift
//  NequoreUser
//
//  Created by MAC_MINI_6 on 04/08/18.
//

import UIKit
import IBAnimatable

class BaseChatCell: BaseTableViewCell {
    
    @IBOutlet weak var lblTime: UILabel!
    @IBOutlet weak var lblaudioTime: UILabel!
    
    weak var delegatePlay: DelegateImagePlay?
    
    var row: Int?
    var sec: Int?
    var item: ChatData? {
        didSet {
            setUpData()
        }
    }
    
    func setUpData() {
        
        lblTime?.text = item?.msgDate
        
        let dd = Int(/item?.mesgDetail?.audioDuration)
        lblaudioTime?.text = secondsToHoursMinutesSeconds(milliseconds:dd)
        
        //secondsToHoursMinutesSeconds(milliseconds: /item?.mesgDetail?.audioDuration)
        print("audioDuration:- \(/item?.mesgDetail?.audioDuration)")
    }
    

    func secondsToHoursMinutesSeconds (milliseconds : Int) -> String {
        var secondsValue = "00"
        var minutesValue = "00"
        let seconds = (milliseconds / 1000)
        let minutes = (seconds / 60)
        
        if seconds <= 9  {
            secondsValue = "0\(seconds)"
        } else{
            secondsValue = "\(seconds)"
        }
        if minutes <= 9  {
            minutesValue = "0\(minutes)"
            
        } else{
            minutesValue = "\(minutes)"
        }
        return "\(minutesValue):\(secondsValue)"
        //return "\(minutesValue):\(secondsValue)"
    }
}
extension CGFloat {
    func fromatSecondsFromTimer() -> String {
        let minutes = Int(self) / 60 % 60
        let seconds = Int(self) % 60
        return String(format: "%02i:%02i", minutes, seconds)
    }
}
