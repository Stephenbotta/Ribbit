//
//  EmbedsViewController.swift
//
//  Created by Evan Sobkowicz.
//  Copyright © 2019 Twitter. All rights reserved.
//

import UIKit
import WebKit
import SafariServices

let DefaultCellHeight: CGFloat = 1000
let TweetPadding: CGFloat = 20

let HtmlTemplate = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no'></head><body><div id='wrapper'></div></body></html>"

let TweetIds = ["1190287688685047808", "1190058389134610433", "1189695999633186816", "1189200245827211264", "1188969975454879747", "1187520413661892608", "1187158033077620736", "1184489710409961473", "1184260182924283906", "1182314890419146752", "1180500441764962304", "1178317279366434818", "1177241651552632833", "1176286416181108736", "1172661285877628931", "1171576431677267969"]


let HeightCallback = "heightCallback"
let ClickCallback = "clickCallback"

class EmbedsViewController: UITableViewController {
    
    let Tweets = TweetsManager.shared
    let WidgetsJs = WidgetsJsManager.shared
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Embeds"
        
        // Set up tableview
        tableView.allowsSelection = false
        tableView.estimatedRowHeight = DefaultCellHeight
        tableView.separatorStyle = .none
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "WebCell")
        
        self.initializeView(TweetIds)
    }
    
    func initializeView(_ tweetIds: [String]) {
        Tweets.initializeWithTweetIds(tweetIds)
        
        // Load widgets.js globally
        WidgetsJs.load()
        
        // Preload WebViews before they are rendered
        preloadWebviews()
        
        tableView.reloadData()
    }
    
    // WebView Management
    
    func preloadWebviews() {
        Tweets.all().forEach { tweet in
            tweet.setWebView(createWebView(idx: tweet.idx))
        }
    }
    
    func createWebView(idx: Int) -> WKWebView {
        let webView = WKWebView()
        
        // Set delegates
        webView.navigationDelegate = self
        webView.uiDelegate = self
        
        // Register callbacks
        webView.configuration.userContentController.add(self, name: ClickCallback)
        webView.configuration.userContentController.add(self, name: HeightCallback)
        
        // Set index as tag
        webView.tag = idx
        
        // Set initial frame
        webView.frame = CGRect(x: 0, y: 0, width: view.bounds.width, height: CGFloat(DefaultCellHeight))
        
        // Prevent scrolling
        webView.scrollView.isScrollEnabled = false
        
        // Load HTML template and set your domain
        webView.loadHTMLString(HtmlTemplate, baseURL: URL(string: "https://your-apps-website.com")!)
        
        return webView
    }
    
    // UITableViewController
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return Tweets.count()
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "WebCell", for: indexPath)
        if let tweet = Tweets.getByIdx(indexPath.row), let webView = tweet.webView {
            cell.contentView.addSubview(webView)
        }
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.subviews.forEach { (view) in
            view.removeFromSuperview()
        }
    }
    
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if let tweet = Tweets.getByIdx(indexPath.row) {
            return tweet.height
        }
        return DefaultCellHeight
    }
    
    // Helpers
    
    func updateHeight(idx: Int, height: String) {
        if let tweet = Tweets.getByIdx(idx) {
            if (tweet.height == DefaultCellHeight) {
                // Store the height to display the UITableViewCell at the correct height
                tweet.setHeight(stringToCGFloat(height) + TweetPadding)
                
                // Prevent UITableViewCells from jumping around and changing
                //   the scroll position as they resize
                tableView.reloadRowWithoutAnimation(IndexPath(row: idx, section: 0))
            }
        }
    }
    
    func openTweet(_ id: String) {
        if let url = URL(string: "https://twitter.com/i/status/\(id)") {
            openInSafarViewController(url)
        }
    }
    
    func openInSafarViewController(_ url: URL) {
        let vc = SFSafariViewController(url: url)
        self.showDetailViewController(vc, sender: self)
    }
    
    func stringToCGFloat (_ s: String) -> CGFloat {
        if let intHeight = Int(s) {
            return CGFloat(integerLiteral: intHeight)
        }
        return DefaultCellHeight
    }

}

extension EmbedsViewController: WKNavigationDelegate {
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        if let url = navigationAction.request.url, navigationAction.navigationType == .linkActivated {
            openInSafarViewController(url)
            decisionHandler(.cancel)
        } else {
            decisionHandler(.allow)
        }
    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        loadTweetInWebView(webView)
    }
    
    // Tweet Loader
    func loadTweetInWebView(_ webView: WKWebView) {
        if let widgetsJsScript = WidgetsJs.getScriptContent(), let tweet = Tweets.getByIdx(webView.tag) {
            webView.evaluateJavaScript(widgetsJsScript)
            webView.evaluateJavaScript("twttr.widgets.load();")
            
            // Documentation:
            // https://developer.twitter.com/en/docs/twitter-for-websites/embedded-tweets/guides/embedded-tweet-javascript-factory-function
            webView.evaluateJavaScript("""
                twttr.widgets.createTweet(
                    '\(tweet.id)',
                    document.getElementById('wrapper'),
                    { align: 'center' }
                ).then(el => {
                    window.webkit.messageHandlers.heightCallback.postMessage(el.offsetHeight.toString())
                });
            """)
        }
    }
}

extension EmbedsViewController: WKUIDelegate {
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        
        // Allow links with target="_blank" to open in SafariViewController
        //   (includes clicks on the background of Embedded Tweets
        if let url = navigationAction.request.url, navigationAction.targetFrame == nil {
            openInSafarViewController(url)
        }
        
        return nil
    }
}

extension EmbedsViewController: WKScriptMessageHandler {
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        switch message.name {
        case HeightCallback:
            updateHeight(idx: message.webView!.tag, height: message.body as! String)
        default:
            print("Unhandled callback")
        }
    }
    
}

extension UITableView {
    func reloadRowWithoutAnimation(_ indexPath: IndexPath) {
        let lastScrollOffset = contentOffset
        UIView.performWithoutAnimation {
            reloadRows(at: [indexPath], with: .none)
        }
        setContentOffset(lastScrollOffset, animated: false)
    }
}

class TweetsManager {
    static let shared = TweetsManager()
    
    var tweets: [Tweet] = []
    
    func initializeWithTweetIds(_ tweetIds: [String]) {
        tweets = buildIndexedTweets(tweetIds)
    }
    
    func count() -> Int {
        return tweets.count
    }
    
    func all() -> [Tweet] {
        return tweets
    }
    
    func getByIdx(_ idx: Int) -> Tweet? {
        return tweets.first { $0.idx == idx }
    }
    
    private func buildIndexedTweets(_ tweetIds: [String]) -> [Tweet] {
         return tweetIds.enumerated().map { (idx, id) in
            return Tweet(id: id, idx: idx)
        }
    }
}

class Tweet {
    // The Tweet ID
    var id: String
    
    // An index value we'll use to map Tweets to the WKWebView tag property and the UITableView row
    var idx: Int
    
    // The height of the WKWebView
    var height: CGFloat
    
    // The WKWebView we'll use to display the Tweet
    var webView: WKWebView?
    
    init(id: String, idx: Int) {
        self.id = id
        self.idx = idx
        self.height = DefaultCellHeight
    }
    
    func setHeight(_ value: CGFloat) {
        height = value
    }
    
    func setWebView(_ value: WKWebView) {
        webView = value
    }
}

class WidgetsJsManager {
    static let shared = WidgetsJsManager()
    
    // The contents of https://platform.twitter.com/widgets.js
    var content: String?
    
    func load() {
       do {
           content = try String(contentsOf: URL(string: "https://platform.twitter.com/widgets.js")!)
       } catch {
           print("Could not load widgets.js script")
       }
    }
    
    func getScriptContent() -> String? {
        return content
    }
}
