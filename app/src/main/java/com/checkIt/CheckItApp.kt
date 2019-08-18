package com.checkIt

import android.content.Context
import android.support.multidex.MultiDexApplication
import android.support.text.emoji.EmojiCompat
import android.support.text.emoji.FontRequestEmojiCompatConfig
import android.support.v4.provider.FontRequest
import android.util.Log
import com.checkIt.data.local.PrefsManager
import com.crashlytics.android.Crashlytics
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

class CheckItApp : MultiDexApplication() {
    companion object {
        lateinit var INSTANCE: CheckItApp

        fun getApplicationContext(): Context {
            return INSTANCE
        }
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        setupTimber()
        setupThreeTen()
        setupSharedPreferences()
        setupEmojiCompat()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    private fun setupThreeTen() {
        AndroidThreeTen.init(this)
    }

    private fun setupSharedPreferences() {
        PrefsManager.initialize(this)
    }

    private fun setupEmojiCompat() {
        // Use a downloadable font for EmojiCompat
        val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)
        val config = FontRequestEmojiCompatConfig(applicationContext, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Timber.i("EmojiCompat initialized")
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Timber.e(throwable, "EmojiCompat initialization failed")
                    }
                })
        EmojiCompat.init(config)
    }

    /** A tree which logs important information for crash reporting. */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
            if (priority == Log.ERROR) {
                Crashlytics.log(priority, tag, message)
                if (throwable != null) {
                    Crashlytics.logException(throwable)
                }
            }
        }
    }
}