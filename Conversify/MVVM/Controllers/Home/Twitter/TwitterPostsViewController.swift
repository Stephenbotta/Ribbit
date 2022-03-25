//
//  TwitterPostsViewController.swift
//  Conversify
//
//  Created by Interns on 12/03/20.
//

import UIKit
import TwitterKit
import RxSwift

class TwitterPostsViewController: UIViewController {
    
    //MARK: OUTLETS
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var btnHome: UIButton!
    @IBOutlet weak var btnUser: UIButton!
    @IBOutlet weak var btnLoginTwitter: UIButton!
    @IBOutlet weak var btnLogOut: UIButton!
    @IBOutlet weak var btnAdd: UIButton!
    @IBOutlet weak var vwHome: UIView!
    @IBOutlet weak var vwUser: UIView!
    @IBOutlet weak var searchBar: UISearchBar!
    var userName = ""
    //MARK: PROPERTIES
    var tweetsModel : [TwitterModelNew]?
    var client = TWTRAPIClient.withCurrentUser()
    var dd : SearchTwitterModelNew?
    var twitterPostsViewModel = TwitterPostsViewModel()
    private var timer: Timer?
    //MARK: VIEWCYCLE
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        if UserDefaults.standard.bool(forKey: "isLoggedIn") {
            vwHome.isHidden = false
            vwUser.isHidden = true
            self.enableTwitterPost()
            self.getHomeFeeds(btnHome)
        }
        else {
            self.enableLogin()
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        startTimer()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        self.invalidate()
    }
    
    func startTimer() {
        let store = TWTRTwitter.sharedInstance().sessionStore
        if let userID = store.session()?.userID, !userID.isEmpty {
            timer = Timer.runThisEvery(seconds: 5, handler: { [weak self] (timer) in
                guard let `self` = self else { return }
                print("5 second")
                self.twitterPostsViewModel.addTwitterTimming()
            })
        }
    }
    
    func invalidate() {
        if let timer = timer {
            timer.invalidate()
        }
    }
    
    //MARK: ACTIONS
    @IBAction func getHomeFeeds(_ sender: UIButton) {
        searchBar.text = ""
        vwHome.isHidden = false
        vwUser.isHidden = true
        let statusesShowEndpoint = "https://api.twitter.com/1.1/statuses/home_timeline.json"
        self.hitApi(statusesShowEndpoint, nil, isSearch: false)
    }
    
    @IBAction func getUserData(_ sender: UIButton) {
        searchBar.text = ""
        vwHome.isHidden = true
        vwUser.isHidden = false
        let statusesShowEndpoint = "https://api.twitter.com/1.1/statuses/user_timeline.json"
        self.hitApi(statusesShowEndpoint, nil, isSearch: false)
    }
    
    @IBAction func loginWithTwitter(_ sender: UIButton) {
        TWTRTwitter.sharedInstance().logIn { (session, error) in
            if session != nil{
                UserDefaults.standard.set(session?.userID, forKey: "userId")
                
                UserDefaults.standard.set(session?.userName, forKey: "userName")
                UserDefaults.standard.set(true, forKey: "isLoggedIn")
                UserDefaults.standard.set(session?.authToken, forKey: "authToken")
                self.client = TWTRAPIClient.withCurrentUser()
                let statusesShowEndpoint = "https://api.twitter.com/1.1/statuses/home_timeline.json"
                self.hitApi(statusesShowEndpoint, nil, isSearch: false)
                self.enableTwitterPost()
                self.startTimer()
            }
            else {
                print("Can't log in \(error.debugDescription)")
                self.enableLogin()
                UserDefaults.standard.set(false, forKey: "isLoggedIn")
            }
        }
    }
    
    @IBAction func logOutUser(_ sender: UIButton) {
        //        UserDefaults.standard.removePersistentDomain(forName: Bundle.main.bundleIdentifier!)
        //        UserDefaults.standard.synchronize()
        
        let store = TWTRTwitter.sharedInstance().sessionStore
        if let userID = store.session()?.userID {
            store.logOutUserID(userID)
        }
        enableLogin()
        UserDefaults.standard.set(false, forKey: "isLoggedIn")
        self.invalidate()
    }
    
    @IBAction func btnAddPost(_ sender: Any) {
        
        guard let vc = R.storyboard.post.createTwitterPostVC() else { return }
        self.pushVC(vc)
        
    }
    
    
}

extension TwitterPostsViewController : UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return tweetsModel?.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        if (/tweetsModel?[indexPath.row].media?.count) > 0{
            
            let cell = tableView.dequeueReusableCell(withIdentifier: "TwitterImageTVC", for: indexPath) as! TwitterImageTVC
            cell.item = tweetsModel?[indexPath.row]
            cell.btnShare.tag = indexPath.row
            cell.lblFavCount.tag = indexPath.row
            cell.btnRetweet.tag = indexPath.row
            cell.btnComment.tag = indexPath.row
            cell.btnRetweet.isSelected = (/tweetsModel?[indexPath.row].retweeted) ? true : false
            cell.lblFavCount.isSelected = (/tweetsModel?[indexPath.row].favorited) ? true : false
            cell.btnShare.addTarget(self, action: #selector(self.pressShareButton(_:)), for: .touchUpInside)
            cell.lblFavCount.addTarget(self, action: #selector(self.pressFavButton(_:)), for: .touchUpInside)
            cell.btnRetweet.addTarget(self, action: #selector(self.pressRetweetButton(_:)), for: .touchUpInside)
            cell.btnComment.addTarget(self, action: #selector(pressCommentButton), for: .touchUpInside)
            
            return cell
        } else{
            
            let cell = tableView.dequeueReusableCell(withIdentifier: "TwitterCell", for: indexPath) as! TwitterCell
            cell.item = tweetsModel?[indexPath.row]
            cell.btnShare.tag = indexPath.row
            cell.lblFavCount.tag = indexPath.row
            cell.btnRetweet.tag = indexPath.row
            cell.btnComment.tag = indexPath.row
             cell.btnRetweet.isSelected = (/tweetsModel?[indexPath.row].retweeted) ? true : false
            cell.lblFavCount.isSelected = (/tweetsModel?[indexPath.row].favorited) ? true : false
            cell.btnShare.addTarget(self, action: #selector(self.pressShareButton(_:)), for: .touchUpInside)
            cell.lblFavCount.addTarget(self, action: #selector(self.pressFavButton), for: .touchUpInside)
             cell.btnRetweet.addTarget(self, action: #selector(self.pressRetweetButton(_:)), for: .touchUpInside)
            cell.btnComment.addTarget(self, action: #selector(self.pressCommentButton), for: .touchUpInside)
            return cell
        }
        
        
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if (/tweetsModel?[indexPath.row].media?.count) > 0{
            
            return 316
        } else{
            return UITableView.automaticDimension
        }
        
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let a = UserDefaults.standard.value(forKey: "userName")
        let Username =  "YOUR_USERNAME_HERE"
        let appURL = NSURL(string: "twitter:///user?screen_name=\(a)")!
        let webURL = NSURL(string: "https://twitter.com/\(a)")!
        let application = UIApplication.shared
        if application.canOpenURL(appURL as URL) {
              application.open(appURL as URL)
            } else {
                // if Twitter app is not installed, open URL inside Safari
                application.open(webURL as URL)
            }
    }
    func hitApi(_ url : String?, _ params : [String : String]?, isSearch:Bool?) {
         Loader.shared.start()
        var clientError : NSError?
        let request = client.urlRequest(withMethod: "GET", urlString: url!, parameters: params, error: &clientError)
        self.client.sendTwitterRequest(request) { (response, data, connectionError) -> Void in
            Loader.shared.stop()
            if connectionError != nil {
                self.enableLogin()
                 UserDefaults.standard.set(false, forKey: "isLoggedIn")
                print("Error: \(connectionError?.localizedDescription ?? "")")
            }
            do {
                if data != nil {
                    
                    let json = try JSONSerialization.jsonObject(with: data!, options: [])
                   
                    // print("Search:- \(json["statuses"])")
                    
                    let decoder = JSONDecoder()
                    
                    if /isSearch {
                        self.dd = try decoder.decode(SearchTwitterModelNew.self, from: data!)
                        self.tweetsModel = self.dd?.searchResults
                    } else{
                        self.tweetsModel = try decoder.decode([TwitterModelNew].self, from: data!)
                        
                    }
                   
                   
                   
                    self.tableView.reloadData()
                }
                
            } catch let jsonError as NSError {
                print("json error: \(jsonError.localizedDescription)")
            }
        }
    }
    func hitFavUnfavAPi(_ url : String?,_ postId: String) {
          Loader.shared.start()
        var clientError : NSError?
        let request = client.urlRequest(withMethod: "POST", urlString: url!, parameters: ["id":postId], error: &clientError)
        
        self.client.sendTwitterRequest(request) { (response, data, connectionError) -> Void in
            Loader.shared.stop()
            if connectionError != nil {
                self.enableLogin()
                               UserDefaults.standard.set(false, forKey: "isLoggedIn")
                print("Error: \(connectionError?.localizedDescription ?? "")")
            }
            do {
                if data != nil {
                    let json = try JSONSerialization.jsonObject(with: data!, options: [])
                    print("json: \(json)")
                    self.hitApi("https://api.twitter.com/1.1/statuses/home_timeline.json", nil, isSearch: false)
                }
                
            } catch let jsonError as NSError {
                print("json error: \(jsonError.localizedDescription)")
            }
        }
    }
    
    func enableLogin() {
        self.btnLoginTwitter.isHidden = false
        self.btnHome.isHidden = true
        self.btnUser.isHidden = true
        self.btnLogOut.isHidden = true
        self.tableView.isHidden = true
        self.btnAdd.isHidden = true
        vwHome.isHidden = true
        vwUser.isHidden = true
        searchBar.isHidden = true
    }
    
    func enableTwitterPost() {
        self.btnLoginTwitter.isHidden = true
        self.btnHome.isHidden = false
        self.btnUser.isHidden = false
        self.btnLogOut.isHidden = false
        self.tableView.isHidden = false
        self.btnAdd.isHidden = false
        self.searchBar.isHidden = false
        self.tableView.reloadData()
    }
    
    @objc func pressShareButton(_ sender: UIButton){
        let uu = "https://twitter.com/\(/tweetsModel?[sender.tag].screen_name)/status/\(/tweetsModel?[sender.tag].id_str)"
        print(uu)
        guard let url = URL(string: uu) else { return }
        let items = [url]
        let ac = UIActivityViewController(activityItems: items, applicationActivities: nil)
        present(ac, animated: true)
    }
    @objc func pressFavButton(_ sender: UIButton){
        if (/tweetsModel?[sender.tag].favorited) {
            hitFavUnfavAPi("https://api.twitter.com/1.1/favorites/destroy.json", "\(/tweetsModel?[sender.tag].id_str)" )
        } else{
            hitFavUnfavAPi("https://api.twitter.com/1.1/favorites/create.json", "\(/tweetsModel?[sender.tag].id_str)" )
        }
           
       }
    @objc func pressCommentButton(_ sender: UIButton){

        guard let vc = R.storyboard.home.twitterCommentVC() else { return }
        print(self.tweetsModel?[sender.tag].id_str ?? "")
        vc.postDetailViewModal.postId.value = self.tweetsModel?[sender.tag].id_str ?? ""
        self.pushVC(vc)
       }
    @objc func pressRetweetButton(_ sender: UIButton){
        if (/tweetsModel?[sender.tag].retweeted) {
                   hitFavUnfavAPi("https://api.twitter.com/1.1/statuses/unretweet.json", "\(/tweetsModel?[sender.tag].id_str)" )
               } else{
                   hitFavUnfavAPi("https://api.twitter.com/1.1/statuses/retweet.json", "\(/tweetsModel?[sender.tag].id_str)" )
               }
                
        }
    }

extension TwitterPostsViewController : UISearchBarDelegate {
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        self.view.endEditing(true)
        var newString = /searchBar.text?.replacingOccurrences(of: "#", with: "%23")
         newString = newString.replacingOccurrences(of: " ", with: "%20")
        let statusesShowEndpoint = "https://api.twitter.com/1.1/search/tweets.json?q=\(/newString)"
        
        print("statusesShowEndpoint:\(statusesShowEndpoint)")
        self.hitApi(statusesShowEndpoint, nil, isSearch: true)
        
       }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        self.view.endEditing(true)
        self.getHomeFeeds(btnHome)
        searchBar.text = ""
    }
}

class TwitterPostsViewModel: BaseRxViewModel {
    
    func addTwitterTimming() {
        PostTarget.addTwitterTimming.request(apiBarrier: false)
        .asObservable()
        .subscribeOn(MainScheduler.instance)
        .subscribe(onNext: { response in
            print("Success")
            }, onError: { _ in
        })<bag
    }
}

