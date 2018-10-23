package com.conversify

import android.content.Context
import android.support.multidex.MultiDexApplication
import com.conversify.data.local.PrefsManager
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

class ConversifyApp : MultiDexApplication() {
    companion object {
        lateinit var INSTANCE: ConversifyApp

        fun getApplicationContext(): Context {
            return INSTANCE
        }
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        AndroidThreeTen.init(this)

        PrefsManager.initialize(this)
    }
}