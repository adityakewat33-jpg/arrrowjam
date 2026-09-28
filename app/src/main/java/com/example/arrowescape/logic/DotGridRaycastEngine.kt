package com.example.arrowescape.logic

import com.example.arrowescape.model.DotGridArrow
import kotlin.math.hypot

object DotGridRaycastEngine {

    data class Result(
        val canEscape: Boolean,
        val blocker: DotGridArrow? = null
    )

    fun checkEscape(
        arrow: DotGridArrow,
        rows: Int,
        cols: Int,
        activeArrows: List<DotGridArrow>
    ): Result {
        if (arrow.isEscaped) return Result(canEscape = false)

        val head = arrow.head
        val dr = arrow.dr
        val dc = arrow.dc

        var currR = head.r + dr
        var currC = head.c + dc

        // Build quick lookup map for fast O(1) collision testing on large 100x100 boards
        val gridLookup = HashMap<Long, DotGridArrow>()
        for (other in activeArrows) {
            if (other.id != arrow.id && !other.isEscaped && !other.isEscaping) {
                for (d in other.dots) {
                    val key = (d.r.toLong() shl 32) or (d.c.toLong() and 0xFFFFFFFFL)
                    gridLookup[key] = other
                }
            }
        }

        while (currR in 0 until rows && currC in 0 until cols) {
            val key = (currR.toLong() shl 32) or (currC.toLong() and 0xFFFFFFFFL)
            val blocker = gridLookup[key]
            if (blocker != null) {
                return Result(canEscape = false, blocker = blocker)
            }
            currR += dr
            currC += dc
        }

        return Result(canEscape = true)
    }

    fun findTappedArrow(
        touchX: Float,
        touchY: Float,
        boardLeft: Float,
        boardTop: Float,
        dotSpacing: Float,
        activeArrows: List<DotGridArrow>
    ): DotGridArrow? {
        var closestArrow: DotGridArrow? = null
        var minDistance = Float.MAX_VALUE
        val threshold = dotSpacing * 0.85f

        val nonEscaped = activeArrows.filter { !it.isEscaped && !it.isEscaping }

        // Fast candidate prune near touch point
        val approxC = ((touchX - boardLeft) / dotSpacing).toInt()
        val approxR = ((touchY - boardTop) / dotSpacing).toInt()

        for (arrow in nonEscaped) {
            for (dot in arrow.dots) {
                if (kotlin.math.abs(dot.r - approxR) > 3 || kotlin.math.abs(dot.c - approxC) > 3) {
                    continue
                }
                val dx = boardLeft + dot.c * dotSpacing
                val dy = boardTop + dot.r * dotSpacing
                val dist = hypot((touchX - dx).toDouble(), (touchY - dy).toDouble()).toFloat()
                if (dist < threshold && dist < minDistance) {
                    minDistance = dist
                    closestArrow = arrow
                }
            }
        }

        return closestArrow
    }

    fun findAvailableHint(
        rows: Int,
        cols: Int,
        activeArrows: List<DotGridArrow>
    ): DotGridArrow? {
        val nonEscaped = activeArrows.filter { !it.isEscaped && !it.isEscaping }
        return nonEscaped.firstOrNull { arrow ->
            checkEscape(arrow, rows, cols, activeArrows).canEscape
        }
    }

    fun isPuzzleSolved(arrows: List<DotGridArrow>): Boolean {
        return arrows.isNotEmpty() && arrows.all { it.isEscaped }
    }
}
