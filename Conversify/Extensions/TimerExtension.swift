//
//  TimerExtension.swift
//  MAC
//
//  Created by cbl24 on 08/11/17.
//  Copyright © 2017 Codebrew. All rights reserved.
//

import UIKit
import Foundation
import MapKit

extension Timer {
    /// EZSE: Runs every x seconds, to cancel use: timer.invalidate()
    public static func runThisEvery(seconds: TimeInterval, handler: @escaping (Timer?) -> Void) -> Timer {
        let fireDate = CFAbsoluteTimeGetCurrent()
        let timer = CFRunLoopTimerCreateWithHandler(kCFAllocatorDefault, fireDate, seconds, 0, 0, handler)
        CFRunLoopAddTimer(CFRunLoopGetCurrent(), timer, CFRunLoopMode.commonModes)
        return timer!
    }
    
    /// EZSE: Run function after x seconds
    public static func runThisAfterDelay(seconds: Double, after: @escaping () -> Void) {
        runThisAfterDelay(seconds: seconds, queue: DispatchQueue.main, after: after)
    }
    
    //TODO: Make this easier
    /// EZSwiftExtensions - dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_LOW, 0)
    public static func runThisAfterDelay(seconds: Double, queue: DispatchQueue, after: @escaping () -> Void) {
        let time = DispatchTime.now() + Double(Int64(seconds * Double(NSEC_PER_SEC))) / Double(NSEC_PER_SEC)
        queue.asyncAfter(deadline: time, execute: after)
    }
}


extension String{
    
    func moveToMap(lat: Double , long: Double){
            let destLocation = CLLocationCoordinate2DMake(CLLocationDegrees(lat), CLLocationDegrees(long))
            let placeMark = MKPlacemark(coordinate: destLocation, addressDictionary: nil)
            let mapItem = MKMapItem(placemark: placeMark)
            mapItem.name = ""
            mapItem.openInMaps(launchOptions: nil)
    }
    
}

class DateConstant{
func timeGapBetweenDates(previousDate : String,currentDate : String) -> String
{
    let dateString1 = previousDate
    let dateString2 = currentDate

    let Dateformatter = DateFormatter()
    Dateformatter.dateFormat = "yyyy-MM-dd HH:mm:ss"


    let date1 = Dateformatter.date(from: dateString1)
    let date2 = Dateformatter.date(from: dateString2)


    let distanceBetweenDates: TimeInterval? = date2?.timeIntervalSince(date1!)
    let secondsInAnHour: Double = 3600
    let minsInAnHour: Double = 60
    let secondsInDays: Double = 86400
    let secondsInWeek: Double = 604800
    let secondsInMonths : Double = 2592000
    let secondsInYears : Double = 31104000

    let minBetweenDates = Int((distanceBetweenDates! / minsInAnHour))
    let hoursBetweenDates = Int((distanceBetweenDates! / secondsInAnHour))
    let daysBetweenDates = Int((distanceBetweenDates! / secondsInDays))
    let weekBetweenDates = Int((distanceBetweenDates! / secondsInWeek))
    let monthsbetweenDates = Int((distanceBetweenDates! / secondsInMonths))
    let yearbetweenDates = Int((distanceBetweenDates! / secondsInYears))
    let secbetweenDates = Int(distanceBetweenDates!)




    if yearbetweenDates > 0
    {
        return String(yearbetweenDates) + "y ago"
    }
    else if monthsbetweenDates > 0
    {
        return String(monthsbetweenDates) + "m ago"
    }
    else if weekBetweenDates > 0
    {
        return String(weekBetweenDates) + "w ago"
    }
    else if daysBetweenDates > 0
    {
        return String(daysBetweenDates) + "d ago"
    }
    else if hoursBetweenDates > 0
    {
        return String(hoursBetweenDates) + "h ago"
    }
    else if minBetweenDates > 0
    {
        return String(minBetweenDates) + "m ago"
    }
    else if secbetweenDates > 0
    {
        return String(secbetweenDates) + "s ago"
    }
    return ""
}
}

