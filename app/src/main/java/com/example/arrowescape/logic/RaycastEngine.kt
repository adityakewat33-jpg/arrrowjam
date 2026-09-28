package com.example.arrowescape.logic

import com.example.arrowescape.model.Arrow
import com.example.arrowescape.model.Direction
import com.example.arrowescape.model.GridPoint

object RaycastEngine {
    data class RaycastResult(
        val canEscape: Boolean,
        val blocker: Arrow? = null,
        val stepsToBlockerOrEdge: Int = 0
    )

    fun checkEscape(
        arrow: Arrow,
        rows: Int,
        cols: Int,
        activeArrows: List<Arrow>
    ): RaycastResult {
        if (arrow.isEscaped) return RaycastResult(canEscape = false)

        val head = arrow.head
        val dir = arrow.direction

        var currR = head.r + dir.dr
        var currC = head.c + dir.dc
        var steps = 0

        val otherArrows = activeArrows.filter { it.id != arrow.id && !it.isEscaped && !it.isEscaping }

        while (currR in 0 until rows && currC in 0 until cols) {
            steps++
            for (other in otherArrows) {
                if (other.occupies(currR, currC)) {
                    return RaycastResult(canEscape = false, blocker = other, stepsToBlockerOrEdge = steps)
                }
            }
            currR += dir.dr
            currC += dir.dc
        }

        return RaycastResult(canEscape = true, stepsToBlockerOrEdge = steps + 1)
    }

    fun findAvailableHint(
        rows: Int,
        cols: Int,
        activeArrows: List<Arrow>
    ): Arrow? {
        val nonEscaped = activeArrows.filter { !it.isEscaped && !it.isEscaping }
        return nonEscaped.firstOrNull { arrow ->
            checkEscape(arrow, rows, cols, activeArrows).canEscape
        }
    }

    fun isPuzzleSolved(arrows: List<Arrow>): Boolean {
        return arrows.isNotEmpty() && arrows.all { it.isEscaped }
    }
}
