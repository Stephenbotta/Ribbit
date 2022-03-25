//
//  SpinWheelVC.swift
//  Conversify
//
//  Created by Apple on 16/12/19.
//

import UIKit
import SwiftFortuneWheel

class SpinWheelVC: UIViewController   {
    
    //MARK::- IBOUTLETS
    var index = 0
    var items = WheelData()
    var slices =  [Slice]()
    @IBOutlet weak var wheelBackgroundView: UIView! {
        didSet {
            wheelBackgroundView.layer.cornerRadius = wheelBackgroundView.bounds.width / 2
        }
    }
    override func viewDidLayoutSubviews() {
          super.viewDidLayoutSubviews()
          wheelBackgroundView.layer.cornerRadius = wheelBackgroundView.bounds.width / 2
      }
    @IBOutlet weak var btnclamRewared: UIButton!
    @IBOutlet weak var btnspintext: UIButton!
    @IBOutlet weak var btnSpin: UIButton!
    var prizes = [(name: "90 $", color: #colorLiteral(red: 0.5854218602, green: 0.8424194455, blue: 0.2066773474, alpha: 1))]
    @IBOutlet weak var wheelControl: SwiftFortuneWheel! {
        didSet {
            wheelControl.configuration = .variousWheelJackpotConfiguration
            wheelControl.spinImage = "redCenterImage"
            wheelControl.pinImage = "redArrow"
            wheelControl.isSpinEnabled = false
            wheelControl.pinImageViewCollisionEffect = CollisionEffect(force: 15, angle: 30)
            wheelControl.edgeCollisionDetectionOn = true
        }
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        let count = items.prize?.count ?? 0
        prizes.removeAll()
        for i in 0..<count{
            prizes.append((name: String(items.prize?[i].value ?? 0) + "pts", color: UIColor.init(hexString: String(items.prize?[i].color ?? ""))))
        }
        btnclamRewared.isHidden = true
        for prize in prizes {
            let sliceContent = [Slice.ContentType.text(text: prize.name, preferences: .variousWheelJackpotText)]
            let slice = Slice(contents: sliceContent, backgroundColor: prize.color)
            slices.append(slice)
        }
        wheelControl.slices = slices
        }
    @IBAction func rotateTap(_ sender: Any) {
    let random = Int.random(in: 0..<prizes.count)
        wheelControl.startRotationAnimation(finishIndex: random, continuousRotationTime: 1) { (finished) in
            print(finished)
            self.index = random
            self.btnspintext.isUserInteractionEnabled = false
            self.btnclamRewared.isHidden = false
            
        }
    }
    @IBAction func btnClamReward(_ sender: Any) {
        SpinWheelvalue(value: items.prize?[index].value)
    }
}
extension SpinWheelVC {
    func SpinWheelvalue(value : Int?){
        PostTarget.spinWheelPrize(value: value).request(apiBarrier: false)
            .asObservable()
            .subscribe(onNext: { [weak self] (response) in
                guard let resp = response as? SpinWheelPrize else{return}
                print(response)
                DispatchQueue.main.async {
                    let alert = UIAlertController(title: "Success", message: resp.data, preferredStyle: UIAlertController.Style.alert)
                    alert.addAction(UIAlertAction(title: "ok", style: UIAlertAction.Style.destructive, handler: { action in
                        self?.dismiss(animated: true, completion: nil)
                    }))
                    self?.present(alert, animated: true, completion: nil)

                }
                }, onError: { (error) in
                    guard let err = error as? ResponseStatus else { return }
                    switch err {
                    default : break
                    }
            })
    }
}


public extension SFWConfiguration {
    static var variousWheelJackpotConfiguration: SFWConfiguration {
        let anchorImage = SFWConfiguration.AnchorImage(imageName: "blueAnchorImage", size: CGSize(width: 12, height: 12), verticalOffset: -22)
        
        let pin = SFWConfiguration.PinImageViewPreferences(size: CGSize(width: 13, height: 40), position: .top, verticalOffset: -25)
        
        let spin = SFWConfiguration.SpinButtonPreferences(size: CGSize(width: 20, height: 20))
        
        let sliceColorType = SFWConfiguration.ColorType.customPatternColors(colors: nil, defaultColor: .white)
        
        let slicePreferences = SFWConfiguration.SlicePreferences(backgroundColorType: sliceColorType, strokeWidth: 0, strokeColor: .white)
        
        let circlePreferences = SFWConfiguration.CirclePreferences(strokeWidth: 15, strokeColor: .black)
        
        var wheelPreferences = SFWConfiguration.WheelPreferences(circlePreferences: circlePreferences, slicePreferences: slicePreferences, startPosition: .top)
        
        wheelPreferences.centerImageAnchor = anchorImage
        
        let configuration = SFWConfiguration(wheelPreferences: wheelPreferences, pinPreferences: pin, spinButtonPreferences: spin)
        
        return configuration
    }
}

public extension TextPreferences {
    static var variousWheelJackpotText: TextPreferences {
        
        var font =  UIFont.systemFont(ofSize: 13, weight: .bold)
        var horizontalOffset: CGFloat = 0
        
        if let customFont = UIFont(name: "System", size: 13) {
            font = customFont
            horizontalOffset = 2
        }
        
        var textPreferences = TextPreferences(textColorType: SFWConfiguration.ColorType.customPatternColors(colors: nil, defaultColor: .white),
                                              font: font,
                                              verticalOffset: 5)
        
        textPreferences.horizontalOffset = horizontalOffset
        textPreferences.orientation = .vertical
        textPreferences.alignment = .right
        
        return textPreferences
    }
}
extension UIColor {
    public convenience init?(hex: String) {
        let r, g, b, a: CGFloat

        if hex.hasPrefix("#") {
            let start = hex.index(hex.startIndex, offsetBy: 1)
            let hexColor = String(hex[start...])

            if hexColor.count == 8 {
                let scanner = Scanner(string: hexColor)
                var hexNumber: UInt64 = 0

                if scanner.scanHexInt64(&hexNumber) {
                    r = CGFloat((hexNumber & 0xff000000) >> 24) / 255
                    g = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
                    b = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
                    a = CGFloat(hexNumber & 0x000000ff) / 255

                    self.init(red: r, green: g, blue: b, alpha: a)
                    return
                }
            }
        }

        return nil
    }
}

