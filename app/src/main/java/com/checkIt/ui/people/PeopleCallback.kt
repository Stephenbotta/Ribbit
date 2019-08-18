package com.checkIt.ui.people

/**
 * Created by Manish Bhargav
 */
interface PeopleCallback {

    fun onClickItem()

    fun onClickItem(position: Int)

    fun onClickItem(position: Int, isDetailShow: Boolean)
}