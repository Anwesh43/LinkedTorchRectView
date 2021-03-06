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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean  = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TRNode(var i : Int, val state : State = State()) {

        private var next : TRNode? = null
        private var prev : TRNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = TRNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTRNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TRNode {
            var curr : TRNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class TorchRect(var i : Int) {

        private var curr : TRNode = TRNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TorchRectView) {

        private val animator : Animator = Animator(view)
        private val tr : TorchRect = TorchRect(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            tr.draw(canvas, paint)
            animator.animate {
                tr.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            tr.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity)  : TorchRectView {
            val view : TorchRectView = TorchRectView(activity)
            activity.setContentView(view)
            return view
        }
    }
}