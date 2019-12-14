package com.ribbit.data.remote.models.survey

class RequestSubmitSurvey {

 var questions = mutableListOf<Ques>()
}

class Ques{
var questionId:String?= null
var options  = mutableListOf<OptionsList>()
}

class OptionsList{
    var optionId:String? = null
}
