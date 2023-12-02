package com.example.catsonactivity.tesutils.extensions

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import org.robolectric.android.controller.ActivityController

fun <T : Activity> ActivityController<T>.require(): T {
    return get()!!
}


fun <T : Activity> ActivityScenario<T>.withActivityScope(block: T.() -> Unit){
    onActivity(block)
}