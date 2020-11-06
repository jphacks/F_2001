package com.example.newsee

import android.content.Context


fun dpTopx(dp: Int, context: Context): Float {
    val metrics = context.resources.displayMetrics
    return dp * metrics.density
}

fun pxToDp(px: Int, context: Context): Float {
    val metrics = context.resources.displayMetrics
    return px / metrics.density
}
