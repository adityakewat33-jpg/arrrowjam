package com.example.arrowescape.logic

import com.example.arrowescape.model.Direction
import com.example.arrowescape.model.GridPoint
import com.example.arrowescape.model.Level
import com.example.arrowescape.model.LongArrowConfig
import kotlin.random.Random

object LevelGenerator {
    fun generateSolvableLevel(
        id: Int = -1,
        rows: Int = 5,
        cols: Int = 5,
        targetArrowCount: Int = (rows * cols / 3).coerceAtLeast(3)
    ): Level {
        val grid = Array(rows) { BooleanArray(cols) { false } }
        val placedArrows = mutableListOf<LongArrowConfig>()
        val directions = Direction.entries.toTypedArray()

        var attempts = 0
        while (placedArrows.size < targetArrowCount && attempts < 300) {
            attempts++
            val emptyPoints = mutableListOf<GridPoint>()
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (!grid[r][c]) emptyPoints.add(GridPoint(r, c))
                }
            }
            if (emptyPoints.isEmpty()) break

            emptyPoints.shuffle()

            var placed = false
            for (head in emptyPoints) {
                val shuffledDirs = directions.clone().apply { shuffle() }
                for (dir in shuffledDirs) {
                    // Check if path from head out to board edge in `dir` is clear
                    if (isHeadPathClear(grid, head, dir, rows, cols)) {
                        // Length 2, 3, or 4
                        val length = Random.nextInt(2, 4)
                        val segments = growStraightBody(grid, head, dir, length, rows, cols)
                        if (segments.size >= 2) {
                            for (seg in segments) {
                                grid[seg.r][seg.c] = true
                            }
                            placedArrows.add(
                                LongArrowConfig(
                                    segments = segments,
                                    direction = dir,
                                    colorIndex = Random.nextInt(6)
                                )
                            )
                            placed = true
                            break
                        }
                    }
                }
                if (placed) break
            }
        }

        return Level(
            id = id,
            name = "Vortex ${rows}x${cols}",
            rows = rows,
            cols = cols,
            arrows = placedArrows,
            parMoves = placedArrows.size
        )
    }

    private fun isHeadPathClear(
        grid: Array<BooleanArray>,
        head: GridPoint,
        dir: Direction,
        rows: Int,
        cols: Int
    ): Boolean {
        var currR = head.r + dir.dr
        var currC = head.c + dir.dc
        while (currR in 0 until rows && currC in 0 until cols) {
            if (grid[currR][currC]) return false
            currR += dir.dr
            currC += dir.dc
        }
        return true
    }

    private fun growStraightBody(
        grid: Array<BooleanArray>,
        head: GridPoint,
        dir: Direction,
        length: Int,
        rows: Int,
        cols: Int
    ): List<GridPoint> {
        val segs = mutableListOf(head)
        val backDir = dir.opposite()
        for (i in 1 until length) {
            val nextR = head.r + backDir.dr * i
            val nextC = head.c + backDir.dc * i
            if (nextR in 0 until rows && nextC in 0 until cols && !grid[nextR][nextC]) {
                segs.add(GridPoint(nextR, nextC))
            } else {
                break
            }
        }
        return segs
    }
}
