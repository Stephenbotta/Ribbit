package com.ribbit.data.remote.models.survey

data class Questions(var question: String?,var isImage:Boolean=false,var isVideo:Boolean=false,var optionList:List<Options>?)

data class OptionsList(var option:String,var isSelected:Boolean = false)

data class QuestionList(var list:List<Questions>)
