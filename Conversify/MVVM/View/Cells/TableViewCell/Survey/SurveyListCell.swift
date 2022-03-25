//
//  SurveyListCell.swift
//  Conversify
//
//  Created by Apple on 10/12/19.
//

import UIKit

class SurveyListCell: BaseTableViewCell {

    //MARK::- OUTLETS
    @IBOutlet var labelSurveyName : UILabel?
    @IBOutlet var labelSurveyHr : UILabel?
    @IBOutlet weak var clientImageView: UIImageView!
    @IBOutlet weak var labelPoints: UILabel?
    
    var item : SurveyModel? {
        didSet{
            labelSurveyName?.text = /item?.name
            labelPoints?.text = /item?.rewardPoints?.toString + " PTS"
            let mins = /item?.totalTime
            if mins > 60.0 {
                var hrs = mins / 60.0
                hrs = hrs.rounded(toPlaces: 2)
                labelSurveyHr?.text = hrs.toString + " hr"
            }else{
                let hrText = item?.totalTime == 1 ? " min" : " mins"
                labelSurveyHr?.text = /item?.totalTime?.toString + hrText
            }
            if /item?.media?.count > 0 {
                clientImageView.image(url:  /item?.media?.first?.thumbnail, placeholder: #imageLiteral(resourceName: "ic_placeholder"))
                //setImage(image: /item?.media?.first?.thumbnail,placeholder: UIImage(named: "ic_account" ))
            }else{
                clientImageView.image = UIImage(named: "ic_account")
            }
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
