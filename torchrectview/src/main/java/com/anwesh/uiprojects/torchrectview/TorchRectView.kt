package com.anwesh.uiprojects.torchrectview

/**
 * Created by anweshmishra on 17/10/20.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Path

val colors : Array<Int> = arrayOf(
        "#F44336",
        "#673AB7",
        "#4CAF50",
        "#FF9800",
        "#2196F3"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 3
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 3.4f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
