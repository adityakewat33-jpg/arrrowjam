package com.example.arrowescape.logic

import android.graphics.PointF
import com.example.arrowescape.model.NeonArrow
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

object NeonRaycastEngine {

    data class EscapeCheckResult(
        val canEscape: Boolean,
        val blocker: NeonArrow? = null
    )

    fun checkEscape(
        arrow: NeonArrow,
        boardWidth: Float,
        boardHeight: Float,
        activeArrows: List<NeonArrow>
    ): EscapeCheckResult {
        if (arrow.isEscaped) return EscapeCheckResult(canEscape = false)

        val head = arrow.head
        val dx = arrow.headDirX
        val dy = arrow.headDirY

        // Start ray slightly ahead of head so it doesn't collide with its own tip
        val margin = 8f
        val rayStartX = head.x + dx * margin
        val rayStartY = head.y + dy * margin

        // Ray extends far off the board
        val rayLength = max(boardWidth, boardHeight) * 2f
        val rayEndX = rayStartX + dx * rayLength
        val rayEndY = rayStartY + dy * rayLength

        val otherArrows = activeArrows.filter { it.id != arrow.id && !it.isEscaped && !it.isEscaping }

        for (other in otherArrows) {
            for (i in 0 until other.points.size - 1) {
                val p1 = other.points[i]
                val p2 = other.points[i + 1]
                if (lineSegmentsIntersect(rayStartX, rayStartY, rayEndX, rayEndY, p1.x, p1.y, p2.x, p2.y)) {
                    return EscapeCheckResult(canEscape = false, blocker = other)
                }
            }
        }

        return EscapeCheckResult(canEscape = true)
    }

    private fun lineSegmentsIntersect(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float, x4: Float, y4: Float
    ): Boolean {
        fun ccw(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Boolean {
            return (cy - ay) * (bx - ax) > (by - ay) * (cx - ax)
        }

        val a = ccw(x1, y1, x3, y3, x4, y4)
        val b = ccw(x2, y2, x3, y3, x4, y4)
        val c = ccw(x1, y1, x2, y2, x3, y3)
        val d = ccw(x1, y1, x2, y2, x4, y4)

        return (a != b) && (c != d)
    }

    fun findTappedArrow(
        touchX: Float,
        touchY: Float,
        activeArrows: List<NeonArrow>,
        threshold: Float = 48f
    ): NeonArrow? {
        var closestArrow: NeonArrow? = null
        var minDistance = Float.MAX_VALUE

        val nonEscaped = activeArrows.filter { !it.isEscaped && !it.isEscaping }

        for (arrow in nonEscaped) {
            for (i in 0 until arrow.points.size - 1) {
                val p1 = arrow.points[i]
                val p2 = arrow.points[i + 1]
                val dist = distanceToSegment(touchX, touchY, p1.x, p1.y, p2.x, p2.y)
                if (dist < threshold && dist < minDistance) {
                    minDistance = dist
                    closestArrow = arrow
                }
            }
        }

        return closestArrow
    }

    private fun distanceToSegment(
        px: Float, py: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0f) return hypot((px - x1).toDouble(), (py - y1).toDouble()).toFloat()

        val t = (((px - x1) * dx + (py - y1) * dy) / lenSq).coerceIn(0f, 1f)
        val projX = x1 + t * dx
        val projY = y1 + t * dy
        return hypot((px - projX).toDouble(), (py - projY).toDouble()).toFloat()
    }

    fun findAvailableHint(
        boardWidth: Float,
        boardHeight: Float,
        activeArrows: List<NeonArrow>
    ): NeonArrow? {
        val nonEscaped = activeArrows.filter { !it.isEscaped && !it.isEscaping }
        return nonEscaped.firstOrNull { arrow ->
            checkEscape(arrow, boardWidth, boardHeight, activeArrows).canEscape
        }
    }

    fun isPuzzleSolved(arrows: List<NeonArrow>): Boolean {
        return arrows.isNotEmpty() && arrows.all { it.isEscaped }
    }
}
