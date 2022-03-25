//
//  SurveyViewModel.swift
//  Conversify
//
//  Created by Apple on 06/12/19.
//

import Foundation
import RxSwift
import RxCocoa

enum SurveyType : Int{
    case Gender = 0
    case Race = 1
    case DOB = 2
    case HouseHoldIncome = 3
    case HomeOwnership = 4
    case Education = 5
    case EmploymentStatus = 6
    case MaritalStatus = 7
    
}

class SurveyViewModel : BaseRxViewModel {
    
    var surveyBasicInfo = Variable<SurveyDataList?>(nil)
    var surveyList = Variable<SurveyListingModel?>(nil)
    var items =  Variable<SurveyModel?>(nil)
    var surveyQues = Variable<SurveyQuesModel?>(nil)
    var pageNo = 1
    var surveyId : String?
    var surveyName : String?
    var timeSurvey : String?
    var pointsEarned : String?
    var challenges = Variable<ChallengesModel?>(nil)
    var giftCardData = Variable<GiftCard?>(nil)
    var redeemHistory = Variable<[Redeem]?>(nil)
    var organizaton = Variable<[Organization]?>(nil)
    var selectedChallenge = Variable<ChallengesList?>(nil)
    
    func getSurveyValues(_ completion:@escaping (Bool)->()){
        SurveyTarget.getTakeSurveyProperties.request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<SurveyDataList> else { return }
                self.surveyBasicInfo.value = safeResponse.data
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    func submitSurveyProperties(gender : String? , race : String?, dateOfBirth : String? , houseHoldIncome : String? , homeOwnership : String? , education : String? , employementStatus : String? , maritalStatus : String? , _ completion:@escaping (Bool)->()){
        SurveyTarget.submitSurvey(gender: gender, race: race, dateOfBirth: dateOfBirth, houseHoldIncome: houseHoldIncome, homeOwnership: homeOwnership, education: education, employementStatus: employementStatus, maritalStatus: maritalStatus).request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    
    func getSurveyList(_ completion:@escaping (Bool)->()){
        SurveyTarget.getSurvey(pageNo: pageNo.toString, searchTxt: "").request(apiBarrier: true)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<SurveyListingModel> else { return }
                self.surveyList.value = safeResponse.data
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    func getSurveyQuestions(_ completion:@escaping (Bool)->()){
        SurveyTarget.getQuestions(surveyId: /surveyId).request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<SurveyQuesModel> else { return }
                self.surveyQues.value = safeResponse.data
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    func submitUserSurvey(_ completion:@escaping (Bool)->()) {
        let ques = surveyQues.value?.quesInfo
        var answersArr = [[String : Any]]()
        ques?.forEach({ (survey) in
            var options = [[String : Any]]()
            survey.options?.forEach({ (ans) in
                options.append(["optionId" : /ans._id])
            })
            let quesAns = ["questionId" : /survey.quesId , "options" : options] as [String : Any]
            answersArr.append(quesAns)
        })
        SurveyTarget.submitUserQuesSurvey(surveyId: /surveyId , questions : answersArr.toJson()).request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    //MARK::- API Challenges
    func getMyChallenges(_ completion:@escaping (Bool)->()){
           SurveyTarget.getChallenges.request(apiBarrier: false)
               .asObservable()
               .observeOn(MainScheduler.instance)
               .subscribe(onNext: { (response) in
                   guard let safeResponse = response as? DictionaryResponse<ChallengesModel> else { return }
                   self.challenges.value = safeResponse.data
                   completion(true)
               }, onError: { (error) in
                   guard let err = error as? ResponseStatus else { return }
                   switch err {
                   case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                   default: break
                   }
                   completion(false)
               })<bag
       }
       
       func getChallengeDetails(challengeId : String? ,_ completion:@escaping (Bool)->()){
           SurveyTarget.getChallengeDetail(challengeId: /challengeId).request(apiBarrier: false)
               .asObservable()
               .observeOn(MainScheduler.instance)
               .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<ChallengesList> else { return }
                self.selectedChallenge.value = safeResponse.data
                   completion(true)
               }, onError: { (error) in
                   guard let err = error as? ResponseStatus else { return }
                   switch err {
                   case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                   default: break
                   }
                   completion(false)
               })<bag
       }
    
    func startChallenges(challengeId : String? ,_ completion:@escaping (Bool)->()){
        SurveyTarget.startChallenge(challengeId: /challengeId).request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
             guard let safeResponse = response as? DictionaryResponse<ChallengesList> else { return }
             self.selectedChallenge.value = safeResponse.data
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    func getGiftCards(_ completion: @escaping (Bool)->()) {
        SurveyTarget.tangoGetCatalog.request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<GiftCard> else { return }
                self.giftCardData.value = safeResponse.data
                
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    func redeemHistory(_ completion: @escaping (Bool)->()) {
        SurveyTarget.showRedeemHistory.request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<Redeem> else { return }
                self.redeemHistory.value = safeResponse.array
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    func tangoPostOrders(faceValue: String?, utid: String?, _ completion: @escaping (Bool) -> ()){
        
        SurveyTarget.tangoPostOrders(faceValue: faceValue, utid: utid).request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: {
                
                (response) in
                UtilityFunctions.makeToast(text: "Order Placed Successfully", type: .success)
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let _):
                    UtilityFunctions.makeToast(text: "Gift Card with this value is not available", type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    func showCharityOrgList(_ completion: @escaping (Bool)->()) {
        SurveyTarget.showCharityOrgList.request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                guard let safeResponse = response as? DictionaryResponse<Organization> else { return }
                self.organizaton.value = safeResponse.array
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
    
    func addCharityDonation(organizationId: String?, givenPoint: String?, _ completion: @escaping (Bool) -> ()){
        SurveyTarget.addCharityDonation(organizationId: organizationId, givenPoint: givenPoint).request(apiBarrier: false)
            .asObservable()
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { (response) in
                
                completion(true)
            }, onError: { (error) in
                guard let err = error as? ResponseStatus else { return }
                switch err {
                case .clientError(let message): UtilityFunctions.makeToast(text: message, type: .error)
                default: break
                }
                completion(false)
            })<bag
    }
}
