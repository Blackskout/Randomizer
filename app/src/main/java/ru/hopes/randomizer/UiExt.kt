package ru.hopes.randomizer

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.COMPLEX_UNIT_SP

fun Float.toDp(): Float {
    return TypedValue.applyDimension(COMPLEX_UNIT_DIP, this, displayMetrics)
}

fun Float.toSp(): Float {
    return TypedValue.applyDimension(COMPLEX_UNIT_SP, this, displayMetrics)
}


val displayMetrics: DisplayMetrics
    get() = Resources.getSystem().displayMetrics