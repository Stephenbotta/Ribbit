package com.ribbit.data.local

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import java.util.concurrent.atomic.AtomicBoolean

class PrefsManager private constructor(context: Context) {
    private val gson = Gson()
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        const val PREF_FIRST_APP_LAUNCH = "PREF_FIRST_APP_LAUNCH"
        const val PREF_ACCESS_TOKEN = "PREF_ACCESS_TOKEN"
        const val PREF_USER_PROFILE = "PREF_USER_PROFILE"
        const val PREF_USER_ID = "PREF_USER_ID"
        const val PREF_GROUP_COUNT = "PREF_GROUP_COUNT"
        const val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"
        const val PREF_LATITUDE = "PREF_LATITUDE"
        const val PREF_LONGITUDE = "PREF_LONGITUDE"
        const val PREF_LOCATION_UPDATE_MILLIS = "PREF_LOCATION_UPDATE_MILLIS"
        const val PREF_DEVICE_TOKEN = "PREF_DEVICE_TOKEN"
        const val PREF_CHAT_TYPE = "PREF_CHAT_TYPE"
        const val PREF_CONVERSATION_ID = "PREF_CONVERSATION_ID"

        private lateinit var instance: PrefsManager
        private val isInitialized = AtomicBoolean()     // To check if instance was previously initialized or not

        fun initialize(context: Context) {
            if (!isInitialized.getAndSet(true)) {
                instance = PrefsManager(context.applicationContext)
            }
        }

        fun get(): PrefsManager = instance
    }

    fun registerOnSharedPreferenceChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangedListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun save(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun save(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun save(key: String, value: Float) {
        preferences.edit().putFloat(key, value).apply()
    }

    fun save(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun save(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    fun save(key: String, `object`: Any) {
        // Convert the provided object to JSON string
        save(key, gson.toJson(`object`))
    }

    fun getString(key: String, defValue: String): String = preferences.getString(key, defValue)
            ?: ""

    fun getInt(key: String, defValue: Int): Int = preferences.getInt(key, defValue)

    fun getBoolean(key: String, defValue: Boolean): Boolean = preferences.getBoolean(key, defValue)

    fun getFloat(key: String, defValue: Float): Float = preferences.getFloat(key, defValue)

    fun getLong(key: String, defValue: Long): Long = preferences.getLong(key, defValue)

    fun <T> getObject(key: String, objectClass: Class<T>): T? {
        val jsonString = preferences.getString(key, null)
        return if (jsonString == null || jsonString.isEmpty()) {
            null
        } else {
            try {
                gson.fromJson(jsonString, objectClass)
            } catch (e: Exception) {
                throw IllegalArgumentException("Object stored with key $key is instance of other class")
            }
        }
    }

    fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    fun removeAll() {
        preferences.edit().clear().apply()
    }
}