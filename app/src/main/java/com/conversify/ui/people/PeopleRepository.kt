package com.conversify.ui.people

import android.app.Application

/**
 * Created by Manish Bhargav on 3/1/19.
 */
class PeopleRepository private constructor(val application: Application) {

    companion object {
        private var peopleRepository: PeopleRepository? = null

        fun getInstance(application: Application): PeopleRepository? {
            if (peopleRepository == null) {
                synchronized(this) {
                    peopleRepository = PeopleRepository(application)
                }
            }
            return peopleRepository
        }
    }


}