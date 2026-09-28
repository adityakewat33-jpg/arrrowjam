package com.example.arrowescape.model

enum class Direction(val dr: Int, val dc: Int, val angle: Float) {
    UP(-1, 0, 0f),
    RIGHT(0, 1, 90f),
    DOWN(1, 0, 180f),
    LEFT(0, -1, 270f);

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }

    fun rotateClockwise(): Direction = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }

    companion object {
        fun fromChar(c: Char): Direction = when (c) {
            'U', 'u', '^' -> UP
            'R', 'r', '>' -> RIGHT
            'D', 'd', 'v', 'V' -> DOWN
            'L', 'l', '<' -> LEFT
            else -> UP
        }
    }
}
