//
//  GiftCardsViewController.swift
//  Conversify
//
//  Created by Sagar Kumar on 08/02/21.
//

import UIKit

class GiftCardsViewController: UIViewController {
    
    @IBOutlet weak var tableView: UITableView!
    
    var surveyInfo = SurveyViewModel()

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        surveyInfo.getGiftCards { [weak self] isSuccess in
            guard let `self` = self else { return }
            
            self.tableView.reloadData()
        }
    }

    @IBAction func backButtonAction(_ sender: Any) {
        popVC()
    }
    
    @IBAction func historyButtonActon(_ sender: Any) {
        guard  let vc = R.storyboard.survey.redeemHistoryViewController() else { return }
        vc.modalPresentationStyle = .overFullScreen
        vc.modalTransitionStyle = .crossDissolve
        DispatchQueue.main.async {
            self.present(vc, animated: true, completion: nil)
        }
    }
}

extension GiftCardsViewController : UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return /surveyInfo.giftCardData.value?.brands?.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: R.reuseIdentifier.giftCardTableViewCell.identifier, for: indexPath) as? GiftCardTableViewCell else { return UITableViewCell()}
        cell.brand = surveyInfo.giftCardData.value?.brands?[indexPath.row]
        return cell
    }
}

extension GiftCardsViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        guard  let vc = R.storyboard.survey.giftCardDetailViewController() else { return }
        vc.brand = surveyInfo.giftCardData.value?.brands?[indexPath.row]
        pushVC(vc)
    }
}
