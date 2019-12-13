package com.ribbit.data.remote.models.survey


class GetSurveyList {

     val info: List<SurveyInfo>? = null


}

class SurveyInfo(){

var name:String? = null
     var totalTime:Int? = null
     var _id:String? = null

     val options:List<Options>? = null
}


class  Options{

     var name:String? = null
     var isSelected:Boolean = false

}