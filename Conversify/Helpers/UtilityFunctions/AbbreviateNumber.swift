import UIKit
import Foundation


func abbreviateNumber(num: Double) -> String {
    // less than 1000, no abbreviation
    if num < 1000 {
        return "\(num)"
    }
    
    // less than 1 million, abbreviate to thousands
    if num < 1000000 {
        var n = Double(num);
        n = Double( floor(n/100)/10 )
        let nInt = Int(n)
        return "\(nInt.description)K"
    }
    
    // more than 1 million, abbreviate to millions
    var n = Double(num)
    n = Double( floor(n/100000)/10 )
    let nInt = Int(n)
    return "\(nInt.description)M"
}
