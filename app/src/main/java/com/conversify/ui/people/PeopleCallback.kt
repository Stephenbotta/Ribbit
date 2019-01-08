package com.conversify.ui.people

/**
 * Created by Manish Bhargav on 8/1/19
 */
interface PeopleCallback {

    fun onClickItem()

    fun onClickItem(position: Int)

    fun onClickItem(position: Int, isDetailShow: Boolean)
}