package com.example.arrowescape.model

import android.graphics.Color

data class Snake(
    val id: Int,
    val segments: MutableList<GridPoint>, // segments[0] is HEAD, segments.last() is TAIL
    var direction: Direction,
    val color: Int = DEFAULT_COLOR
) {
    var isSlithering: Boolean = false
    var slitherProgress: Float = 0f // In tile units traveled
    var slitherSpeed: Float = 0f
    var isEscaped: Boolean = false

    // Bump recoil
    var isBumping: Boolean = false
    var bumpOffset: Float = 0f

    // Hint
    var isHinted: Boolean = false
    var hintTime: Long = 0L

    val head: GridPoint get() = segments[0]
    val length: Int get() = segments.size

    companion object {
        val DEFAULT_COLOR = Color.parseColor("#10B981") // Emerald Snake
        val COLORS = listOf(
            Color.parseColor("#10B981"), // Emerald Green
            Color.parseColor("#6366F1"), // Indigo Viper
            Color.parseColor("#F59E0B"), // Golden Python
            Color.parseColor("#EC4899"), // Rose Cobra
            Color.parseColor("#06B6D4"), // Cyan Mamba
            Color.parseColor("#8B5CF6")  // Purple Krait
        )
    }

    fun occupies(r: Int, c: Int): Boolean {
        if (isEscaped) return false
        return segments.any { it.r == r && it.c == c }
    }
}
