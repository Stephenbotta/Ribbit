package com.pulse.data.remote.models

/**
 * Created by Manish Bhargav
 */
data class SearchUser(var locationLong: Double,
                      var locationLat: Double,
                      var range: Int,
                      var pageNo: Int,
                      var categoryIds: ArrayList<String>)
