package com.pulse.utils

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import timber.log.Timber

class FragmentSwitcher(private val fragmentManager: FragmentManager,
                       @IdRes private val containerViewId: Int) {
    private var currentFragmentTag: String? = null

    fun fragmentExist(tag: String): Boolean {
        var fragmentExist = false

        val fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            Timber.d("Fragment exists : $tag")

            fragmentExist = true

            val fragmentTransaction = fragmentManager.beginTransaction()

            // Hide current fragment
            val currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag)
            if (currentFragment != null) {
                fragmentTransaction.hide(currentFragment)
            }

            // If fragment exists then show it
            fragmentTransaction
                    .show(fragment)
                    .commit()

            setCurrentFragmentTag(tag)
        }

        return fragmentExist
    }

    fun addFragment(fragment: Fragment, tag: String) {
        Timber.d("Fragment added : $tag")

        val fragmentTransaction = fragmentManager.beginTransaction()

        // Hide current fragment
        val currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag)
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment)
        }

        fragmentTransaction
                .add(containerViewId, fragment, tag)
                .commit()

        setCurrentFragmentTag(tag)
    }

    fun setCurrentFragmentTag(tag: String?) {
        currentFragmentTag = tag
    }

    fun getCurrentFragmentTag(): String? = currentFragmentTag
}