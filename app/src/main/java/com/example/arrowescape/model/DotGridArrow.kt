package com.example.arrowescape.model

import android.graphics.Color

data class GridDot(val r: Int, val c: Int)

data class DotGridArrow(
    val id: Int,
    val dots: List<GridDot>, // Ordered from TAIL to HEAD
    var color: Int
) {
    var isEscaping: Boolean = false
    var escapeProgress: Float = 0f // Dot units traveled forward
    var escapeVelocity: Float = 0f
    var isEscaped: Boolean = false

    var isBumping: Boolean = false
    var bumpOffset: Float = 0f

    var isHinted: Boolean = false
    var hintTime: Long = 0L

    val head: GridDot get() = dots.last()
    val tail: GridDot get() = dots.first()

    // Direction from penultimate dot to head
    val dr: Int
    val dc: Int
    val angle: Float

    init {
        if (dots.size >= 2) {
            val pPrev = dots[dots.size - 2]
            val pHead = dots.last()
            val dRow = pHead.r - pPrev.r
            val dCol = pHead.c - pPrev.c
            dr = dRow.coerceIn(-1, 1)
            dc = dCol.coerceIn(-1, 1)
            angle = Math.toDegrees(Math.atan2(dr.toDouble(), dc.toDouble())).toFloat()
        } else {
            dr = 0
            dc = 1
            angle = 0f
        }
    }

    fun occupies(r: Int, c: Int): Boolean {
        if (isEscaped) return false
        return dots.any { it.r == r && it.c == c }
    }

    companion object {
        val COLOR_YELLOW = 0xFFFFE600.toInt()
        val COLOR_PINK   = 0xFFFF2A85.toInt()
        val COLOR_CYAN   = 0xFF00E5FF.toInt()
        val COLOR_GREEN  = 0xFF39FF14.toInt()
        val COLOR_ORANGE = 0xFFFF6600.toInt()
        val COLOR_PURPLE = 0xFFB026FF.toInt()
        val COLOR_RED    = 0xFFFF3344.toInt()
        val COLOR_BLUE   = 0xFF3A7BFF.toInt()
    }
}
