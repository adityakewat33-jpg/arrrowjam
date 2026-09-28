package com.example.arrowescape.model

import android.graphics.Color
import android.graphics.PointF

data class NeonArrow(
    val id: Int,
    val points: List<PointF>, // Ordered from TAIL (index 0) to HEAD (last index)
    val color: Int
) {
    var isEscaping: Boolean = false
    var distanceTraveled: Float = 0f // Pixels traveled along the forward track
    var escapeSpeed: Float = 0f
    var isEscaped: Boolean = false

    var isBumping: Boolean = false
    var bumpOffset: Float = 0f

    var isHinted: Boolean = false
    var hintTime: Long = 0L

    val head: PointF get() = points.last()
    val tail: PointF get() = points.first()

    // Direction vector of the head (from penultimate point to head)
    val headDirX: Float
    val headDirY: Float
    val headAngle: Float

    init {
        if (points.size >= 2) {
            val pPrev = points[points.size - 2]
            val pHead = points.last()
            val dx = pHead.x - pPrev.x
            val dy = pHead.y - pPrev.y
            val len = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat().coerceAtLeast(0.001f)
            headDirX = dx / len
            headDirY = dy / len
            headAngle = Math.toDegrees(Math.atan2(headDirY.toDouble(), headDirX.toDouble())).toFloat()
        } else {
            headDirX = 1f
            headDirY = 0f
            headAngle = 0f
        }
    }

    // Total length of the arrow along its path
    val totalLength: Float by lazy {
        var len = 0f
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            len += Math.hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
        }
        len
    }

    companion object {
        val COLOR_YELLOW = Color.parseColor("#FFE600") // Neon Yellow
        val COLOR_PINK   = Color.parseColor("#FF2A85") // Neon Pink/Magenta
        val COLOR_CYAN   = Color.parseColor("#00E5FF") // Neon Cyan
        val COLOR_GREEN  = Color.parseColor("#39FF14") // Neon Lime Green
        val COLOR_ORANGE = Color.parseColor("#FF6600") // Neon Orange
        val COLOR_PURPLE = Color.parseColor("#B026FF") // Neon Purple
        val COLOR_RED    = Color.parseColor("#FF3344") // Neon Red
        val COLOR_BLUE   = Color.parseColor("#3A7BFF") // Electric Blue

        val PALETTE = listOf(
            COLOR_YELLOW, COLOR_PINK, COLOR_CYAN, COLOR_GREEN,
            COLOR_ORANGE, COLOR_PURPLE, COLOR_RED, COLOR_BLUE
        )
    }
}
