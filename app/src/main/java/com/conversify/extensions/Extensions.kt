package com.conversify.extensions

/**
 * @return 1 if boolean is true else return 0
 * */
fun Boolean?.toInt(): Int {
    return if (this == true) 1 else 0
}

/**
 * @return true if integer value is 1 else return false
 * */
fun Int?.toBoolean(): Boolean {
    return this == 1
}