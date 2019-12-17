package com.ribbit.data.remote.models.survey


class GetSurveyProperties {

     val gender: List<KeyData>? = null

     val race: List<KeyData>? = null

     val houseHoldIncome: List<KeyData>? = null

     val homeOwnership: List<KeyData>? = null

     val education: List<KeyData>? = null

     val employementStatus: List<KeyData>? = null

     val maritalStatus: List<KeyData>? = null

     val dateOfBirth: Long? = null
}

open class KeyData{
     var key :String = ""
     var isSelected = 0
}