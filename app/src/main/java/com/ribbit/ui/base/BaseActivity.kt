package com.ribbit.ui.base

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    abstract fun onSavedInstance(outState:Bundle?,outPersisent:PersistableBundle?)
}