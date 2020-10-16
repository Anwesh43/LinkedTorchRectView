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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawTorchPath(size : Float, sf3 : Float, paint : Paint) {
    save()
    val path : Path = Path()
    path.moveTo(-size / 2, -size)
    path.lineTo(-size, -2 * size)
    path.lineTo(size, -2 * size)
    path.lineTo(size / 2, -size)
    path.lineTo(-size / 2, -size)
    clipPath(path)
    drawRect(RectF(-size, -size - size * sf3, size, -size), paint)
    restore()
}

fun Canvas.drawTorchRect(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val size : Float = Math.min(w, h) / sizeFactor
    save()
    translate(w / 2, h)
    drawRect(RectF(-size /2, -size * sf1, size / 2, 0f), paint)
    for (j in 0..1) {
        save()
        translate(-size / 2 + size * j, -size)
        drawLine(0f, 0f, -(1f - 2 * j) * size * 0.5f * sf2, -size * sf2, paint)
        restore()
    }
    drawTorchPath(size, sf3, paint)
    restore()
}

fun Canvas.drawTRNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    drawTorchRect(scale, w, h, paint)
}

class TorchRectView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}