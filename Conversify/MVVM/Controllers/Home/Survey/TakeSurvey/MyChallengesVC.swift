//
//  MyChallengesVC.swift
//  Conversify
//
//  Created by admin on 16/03/20.
//

import UIKit

class MyChallengesVC: UIViewController {
    
    //MARK::- IBOutlets
    @IBOutlet weak var tableView: UITableView?{
        didSet{
            tableView?.delegate = self
            tableView?.dataSource = self
            tableView?.estimatedRowHeight = 90.0
        }
    }
    
    //MARK::- Properties
    var surveyInfo = SurveyViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        getMyChallenges()
    }
    
    @IBAction func btnActionBack(_ sender: UIButton) {
     popVC()
    }
    
}

extension MyChallengesVC {
    
    func getMyChallenges(){
        surveyInfo.getMyChallenges { (isSuccess) in
            self.tableView?.reloadData()
        }
    }
}

//MARK::- UITableViewDelegate , UITableViewDataSource
extension MyChallengesVC : UITableViewDelegate , UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.challengesTableCell.identifier, for: indexPath) as? ChallengesTableCell else { return UITableViewCell()}
        cell.item = surveyInfo.challenges.value?.challenges?[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return /surveyInfo.challenges.value?.challenges?.count
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableView.automaticDimension
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard let vc = R.storyboard.survey.challengeDetailVC() else { return }
        vc.surveyInfo.selectedChallenge.value = surveyInfo.challenges.value?.challenges?[indexPath.row]
        vc.rewordPoint = surveyInfo.challenges.value?.challenges?[indexPath.row].rewardPoint
        pushVC(vc)
    }
}
