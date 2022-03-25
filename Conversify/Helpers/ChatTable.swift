//
//  ChatTable.swift
//  Conversify
//
//  Created by Apple on 02/11/18.
//

import UIKit

class ChatTable: UITableView {
    
    lazy var inputAccessory: ChatAccessory = {
        let rect = CGRect(x: 0,y: 0, width: UIScreen.main.bounds.width, height: 0) //119
        let inputAccessory = ChatAccessory(frame: rect)
        return inputAccessory
    }()
    
    
    override var inputAccessoryView: UIView? {
        
        return inputAccessory
    }
    
    override var canBecomeFirstResponder: Bool {
        return true
    }
    
    override func awakeFromNib() {
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector:  #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        self.keyboardDismissMode = .interactive
    }
    
    @objc func keyboardWillShow(_ notification: Notification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            let keyboardHeight = keyboardRectangle.height
            self.contentInset.bottom = keyboardHeight
            
            if keyboardHeight > 100 {
                let numberOfSection = self.numberOfSections
                if numberOfSection > 0 {
                    let numberOfRows = self.numberOfRows(inSection: numberOfSections - 1)
                    self.scrollToRow(at: IndexPath.init(row: numberOfRows - 1, section: numberOfSection - 1), at: .bottom, animated: true)
                }
                //                scrollToBottom()
            }
        }
    }
    
    @objc func keyboardWillHide(_ notification: NSNotification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            let keyboardHeight = keyboardRectangle.height
            self.contentInset.bottom = keyboardHeight
            self.inputAccessory.textView.invalidateIntrinsicContentSize()
        }
    }
}

class CommentTable: UITableView {
    
    var keyBoardHeigh : CGFloat = 0
    lazy var inputAccessory: CommentAccessory = {
       
            let rect = CGRect(x: 0,y: 0, width: UIScreen.main.bounds.width, height: 82)
            let inputAccessory = CommentAccessory(frame: rect)
            return inputAccessory

        
    }()
    
    override var inputAccessoryView: UIView? {
        
        return  inputAccessory
    }
    
    override var canBecomeFirstResponder: Bool {
        return true
    }
    
    override func awakeFromNib() {
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector:  #selector(keyboardWillHide(_:)), name: UIResponder.keyboardWillHideNotification, object: nil)
        self.keyboardDismissMode = .interactive
    }
    
    @objc func keyboardWillShow(_ notification: Notification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            let keyboardHeight = keyboardRectangle.height
            self.contentInset.bottom = keyboardHeight
            self.keyBoardHeigh = keyboardHeight
            if keyboardHeight > 100 {
                let numberOfSection = self.numberOfSections
                if numberOfSection > 0 {
                    let numberOfRows = self.numberOfRows(inSection: numberOfSections - 1)
                    if numberOfRows > 0 {
                        self.scrollToRow(at: IndexPath.init(row: 0, section: numberOfSection - 1), at: .bottom, animated: true)
                    }
                    //
                }
                //    scrollToBottom()
            }
        }
    }
    
    func updateContentView( indexPath: IndexPath , height: CGFloat){
      
        let ht = UIScreen.main.bounds.height - keyBoardHeigh
        if ht > keyBoardHeigh{
             self.contentInset.bottom = keyBoardHeigh - height/3
        }else{
             self.contentInset.bottom = keyBoardHeigh + height/3
        }
        print(self.contentInset.bottom)
        self.scrollToRow(at: indexPath, at: .bottom, animated: true)
        self.layoutIfNeeded()
    }
    
   
    
    @objc func keyboardWillHide(_ notification: NSNotification) {
        if let keyboardFrame: NSValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue {
            let keyboardRectangle = keyboardFrame.cgRectValue
            let keyboardHeight = keyboardRectangle.height
            self.contentInset.bottom = keyboardHeight
            self.inputAccessory.txtfComments.invalidateIntrinsicContentSize()
        }
    }
}

//MARK:- Scroll to bottom function
extension UITableView {
    
    func scrollToBottom(animated: Bool = true, scrollPostion: UITableView.ScrollPosition = .bottom) {
        let sec = self.numberOfSections
        let no = self.numberOfRows(inSection: 0)
        if no > 0 {
            let index = IndexPath(row: no - 1, section: sec == 0 ? 0 : sec - 1)
            scrollToRow(at: index, at: scrollPostion, animated: animated)
        }
    }
    
    func registerXIB(_ nibName: String) {
        self.register(UINib.init(nibName: nibName, bundle: nil), forCellReuseIdentifier: nibName)
    }
    
    func registerXIBForHeaderFooter(_ nibName: String) {
        self.register(UINib.init(nibName: nibName, bundle: nil), forHeaderFooterViewReuseIdentifier: nibName)
    }
    
    func sizeHeaderToFit() {
        
        let headerView = self.tableHeaderView
        headerView?.autoresizingMask = [.flexibleHeight]
        headerView?.setNeedsLayout()
        headerView?.layoutIfNeeded()
        let height = headerView?.systemLayoutSizeFitting(UIView.layoutFittingCompressedSize).height
        var frame = headerView?.frame
        frame?.size.height = (height  ?? 0.0) 
        headerView?.frame = frame ?? CGRect.init()
        //headerView?.translatesAutoresizingMaskIntoConstraints = true
        self.tableHeaderView = headerView
       
    }
    
    func sizeFooterToFit() {
           
           let headerView = self.tableFooterView
           headerView?.autoresizingMask = [.flexibleHeight]
           headerView?.setNeedsLayout()
           headerView?.layoutIfNeeded()
           let height = headerView?.systemLayoutSizeFitting(UIView.layoutFittingCompressedSize).height
           var frame = headerView?.frame
           frame?.size.height = (height  ?? 0.0)
           headerView?.frame = frame ?? CGRect.init()
           //headerView?.translatesAutoresizingMaskIntoConstraints = true
           self.tableFooterView = headerView
          
       }
}
