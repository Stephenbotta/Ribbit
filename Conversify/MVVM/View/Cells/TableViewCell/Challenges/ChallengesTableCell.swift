//
//  ChallengesTableCell.swift
//  
//
//  Created by admin on 16/03/20.
//

import UIKit

class ChallengesTableCell: UITableViewCell {

    //MARK::- Properties
    @IBOutlet weak var labelChallengeName: UILabel?
    @IBOutlet weak var labelChallengeDate: UILabel?
    @IBOutlet weak var labelChallengeStatus: UILabel?
    
    var item : ChallengesList?{
        didSet{
            labelChallengeName?.text = item?.title
            let startDate = Date.init(fromString: /item?.startDate, format: "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            let _startDate = startDate?.toString(DateFormat.custom("MM/dd/yyyy"))
            let endDate = Date.init(fromString: /item?.endDate, format: "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            let _endDate = endDate?.toString(DateFormat.custom("MM/dd/yyyy"))
            labelChallengeDate?.text = /_startDate + " - " + /_endDate
            switch item?.status {
            case .inprogress:
                labelChallengeStatus?.text = "In Progress"
            case .notstarted :
                labelChallengeStatus?.text = "Not started yet"
            default:
                labelChallengeStatus?.text = "Completed"
            }
        }
    }
}
