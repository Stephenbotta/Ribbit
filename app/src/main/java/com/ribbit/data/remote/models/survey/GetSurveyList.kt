package com.ribbit.data.remote.models.survey

import com.google.gson.annotations.Expose
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto


class GetSurveyList {

     val info: List<SurveyInfo>? = null

}

class SurveyInfo{

var name:String? = null
     var totalTime:Int? = null
     var _id:String? = null

     val options:List<Options>? = null


     val media: Any?=null


}


class  Options{

     var name:String? = null
     var _id:String? = null
     var isSelected:Boolean = false

}