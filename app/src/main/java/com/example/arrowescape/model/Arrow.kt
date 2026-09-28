package com.example.arrowescape.model

import android.graphics.Color

data class Arrow(
    val id: Int,
    val segments: List<GridPoint>, // segments[0] is the HEAD, segments.last() is the TAIL
    val direction: Direction,
    val color: Int = DEFAULT_COLOR
) {
    var isEscaping: Boolean = false
    var escapeOffset: Float = 0f // Pixel offset along direction
    var escapeVelocity: Float = 0f
    var isEscaped: Boolean = false

    // Bump recoil when blocked
    var isBumping: Boolean = false
    var bumpOffset: Float = 0f

    // Hint glow
    var isHinted: Boolean = false
    var hintTime: Long = 0L

    val head: GridPoint get() = segments[0]
    val length: Int get() = segments.size

    companion object {
        val DEFAULT_COLOR = Color.parseColor("#4F46E5") // Indigo
        val COLORS = listOf(
            Color.parseColor("#4F46E5"), // Electric Indigo
            Color.parseColor("#06B6D4"), // Neon Cyan
            Color.parseColor("#10B981"), // Emerald Green
            Color.parseColor("#F59E0B"), // Amber Gold
            Color.parseColor("#EC4899"), // Radiant Pink
            Color.parseColor("#8B5CF6")  // Vivid Violet
        )
    }

    fun occupies(r: Int, c: Int): Boolean {
        if (isEscaped) return false
        return segments.any { it.r == r && it.c == c }
    }
}
