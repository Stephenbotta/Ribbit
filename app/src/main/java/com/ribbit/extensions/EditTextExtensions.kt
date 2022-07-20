package com.ribbit.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.widget.AppCompatSpinner
import com.ribbit.R
import com.ribbit.data.remote.models.survey.KeyData

fun EditText.setSelectionAtEnd() {
    setSelection(text?.length ?: 0)
}


fun  AppCompatSpinner.setArrayAdapter(list: List<KeyData>?) {
    val mList = list?.map { it.key }
    val adapter = ArrayAdapter<String>(context, R.layout.item_spinner_wrap, mList ?: emptyList())
    adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)


    var selected = list?.indexOf(list.find { it.isSelected == 1  })
    setAdapter(adapter)

    if (selected == -1){
        selected = 0
    }

    val position = adapter.getPosition(list?.get(selected ?: 0)?.key ?: "")

    setSelection(position)

}

inline fun <reified T : Any> Activity.launchActivity(
        requestCode: Int = -1,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivityForResult(intent, requestCode, options)
    } else {
        startActivityForResult(intent, requestCode)
    }

}

inline fun <reified T : Any> Context.launchActivity(
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivity(intent, options)
    } else {
        startActivity(intent)
    }
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
        Intent(context, T::class.java)
