//
//  AnaliticsViewController.swift
//  Conversify
//
//  Created by admin on 03/05/21.
//

import UIKit
import Charts

class AnaliticsViewController: UIViewController , ChartViewDelegate {

    var analyticData : AnalitycsData?
    
    
    @IBOutlet weak var pieTotalPointRedeem: PieChartView!
    @IBOutlet weak var paiTotalPointEarned: PieChartView!
    @IBOutlet weak var barChartView: BarChartView!
    @IBOutlet weak var viewPointEarned: UIView!
    @IBOutlet weak var barPostChartView: BarChartView!
    @IBOutlet weak var barPointByDate: BarChartView!
    @IBOutlet weak var barStoriesCharView: BarChartView!
    @IBOutlet weak var barCommentsByDat: BarChartView!
    override func viewDidLoad() {
        super.viewDidLoad()
        barChartView.delegate = self
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(true)
        getAnalyticdata()
    }
    
    
    
   func setChart(){
    
    
    //MARK:- Message Count Discription
    
    let count1 = analyticData?.messagesCount?.count ?? 0
    var dataEntries: [BarChartDataEntry] = []
   // var dataEntries1: [BarChartDataEntry] = []
    
    var month = [String]()
    for i in 0..<count1{
        month.append(analyticData?.messagesCount?[i].date ?? "")
    }
    barChartView.xAxis.valueFormatter = IndexAxisValueFormatter(values: month)
    barChartView.xAxis.granularity = 1
    
    
    for i in 0..<count1 {
        print(Double(i))
        let dataEntry = BarChartDataEntry(x: Double(i) , y: Double(analyticData?.messagesCount?[i].sentMessages ?? 0),data: analyticData?.messagesCount?[i].date)
            dataEntries.append(dataEntry)
       // let dataEntry1 = BarChartDataEntry(x: Double(i) , y: Double(analyticData?.messagesCount?[i].recieveMessages ?? 0))
           //  dataEntries1.append(dataEntry1)
    }
    
    let chartDataSet = BarChartDataSet(entries: dataEntries, label: "sent Messages")
   // let chartDataSet1 = BarChartDataSet(entries: dataEntries1, label: "recieve Messages")
    
   // let dataSets: [BarChartDataSet] = [chartDataSet,chartDataSet1]
    let data1 = BarChartData(dataSet: chartDataSet)
    data1.setValueFont(.systemFont(ofSize: 10, weight: .light))
    chartDataSet.colors = [UIColor(red: 230/255, green: 126/255, blue: 34/255, alpha: 1)]
    barChartView.data = data1
    
    
    
   
    
    
    //MARK:- PiaChar Details Point Earned
    
    var count2 = analyticData?.points_earned?.count ?? 0
    var dataEntries2 : [PieChartDataEntry] = []
    for i in 0..<count2 {
        let dataEntry = PieChartDataEntry(value: Double(analyticData?.points_earned?[i].totalPointEarned ?? 0), label: analyticData?.points_earned?[i].source)
        dataEntries2.append(dataEntry)
    }
    let piechardataset = PieChartDataSet(dataEntries2)
    piechardataset.sliceSpace = 2
    piechardataset.colors = ChartColorTemplates.joyful()
    let data2 = PieChartData(dataSet: piechardataset)
    paiTotalPointEarned.data = data2
    
    
    //MARK:- PiaChar Details Point Redeem
    var count3 = analyticData?.points_redeem?.count ?? 0
    var dataEntries3 : [PieChartDataEntry] = []
    for i in 0..<count3 {
        let dataEntry = PieChartDataEntry(value: Double(analyticData?.points_redeem?[i].totalPointRedeem ?? 0), label: analyticData?.points_redeem?[i].source)
        dataEntries3.append(dataEntry)
    }
    let piechardataset3 = PieChartDataSet(dataEntries3)
    piechardataset3.sliceSpace = 2
    piechardataset3.colors = ChartColorTemplates.colorful()
    let data3 = PieChartData(dataSet: piechardataset3)
    pieTotalPointRedeem.data = data3
    
    
    
    //MARK:- Post CharView
    
    
    
    var count4 = analyticData?.postCount_byDate?.count ?? 0
    
    var month1 = [String]()
    for i in 0..<count4{
        month1.append(analyticData?.postCount_byDate?[i].date ?? "")
    }
   barPostChartView.xAxis.valueFormatter = IndexAxisValueFormatter(values: month1)
   barPostChartView.xAxis.granularity = 1
    
    var dataEntries4 : [BarChartDataEntry] = []
    for i in 0..<count4 {
        let dataEntry = BarChartDataEntry(x: Double(i), y:  Double(analyticData?.postCount_byDate?[i].count ?? 0))
        dataEntries4.append(dataEntry)
    }
    let barchardataset4 = BarChartDataSet(dataEntries4)
    barchardataset4.colors = ChartColorTemplates.joyful()
    let data4 = BarChartData(dataSet: barchardataset4)
    barPostChartView.data = data4
    
    
    // MARK:- STORIES DATA
    
    
    
    var count5 = analyticData?.storiesCount_byDate?.count ?? 0
    var month2 = [String]()
    for i in 0..<count5{
        month2.append(analyticData?.storiesCount_byDate?[i].date ?? "")
    }
    barStoriesCharView.xAxis.valueFormatter = IndexAxisValueFormatter(values: month2)
    barStoriesCharView.xAxis.granularity = 1

    
    var dataEntries5 : [BarChartDataEntry] = []
    for i in 0..<count5 {
        let dataEntry = BarChartDataEntry(x: Double(i), y:  Double(analyticData?.storiesCount_byDate?[i].count ?? 0))
        dataEntries5.append(dataEntry)
    }
    let barchardataset5 = BarChartDataSet(dataEntries5)
    barchardataset5.colors = ChartColorTemplates.joyful()
    let data5 = BarChartData(dataSet: barchardataset5)
    barStoriesCharView.data = data5
    
    
    // MARK:- BAR POINTBYDATE
    let count6 = analyticData?.points_by_dates?.count ?? 0
    
    var month3 = [String]()
    for i in 0..<count6{
        month3.append(analyticData?.points_by_dates?[i].date ?? "")
    }
    barPointByDate.xAxis.valueFormatter = IndexAxisValueFormatter(values: month3)
    barPointByDate.xAxis.granularity = 1

    var dataEntries6 : [BarChartDataEntry] = []
    for i in 0..<count6 {
        let dataEntry = BarChartDataEntry(x: Double(i), y:  Double(analyticData?.points_by_dates?[i].totalPointEarned ?? 0))
        dataEntries6.append(dataEntry)
    }
    let barchardataset6 = BarChartDataSet(dataEntries6)
    barchardataset6.colors = ChartColorTemplates.joyful()
    let data6 = BarChartData(dataSet: barchardataset6)
    barPointByDate.data = data6
    
    
    var count7 = analyticData?.commentCount_byDate?.count ?? 0
    var dataEntries7 : [BarChartDataEntry] = []
    for i in 0..<count7 {
        let dataEntry = BarChartDataEntry(x: Double(i), y:  Double(analyticData?.commentCount_byDate?[i].count ?? 0))
        dataEntries7.append(dataEntry)
    }
    let barchardataset7 = BarChartDataSet(dataEntries7)
    barchardataset7.colors = ChartColorTemplates.joyful()
    let data7 = BarChartData(dataSet: barchardataset7)
    barCommentsByDat.data = data7
    }
 
    
    @IBAction func btnBack(_ sender: Any) {
        self.popVC()
    }
}
//MARK:- API Handler

extension AnaliticsViewController{
    func getAnalyticdata(){
        
        SurveyTarget.getPointEarnedHistory.request(apiBarrier: false)
            .asObservable()
            .subscribe(onNext: { [weak self] (response) in
                guard let resp = response as? DictionaryResponse<AnalitycsData> else{return}
                self?.analyticData = resp.data
                print(response)
                DispatchQueue.main.async {self?.setChart()}
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                 
                    default : break
                    }
            })
    }
    
}
