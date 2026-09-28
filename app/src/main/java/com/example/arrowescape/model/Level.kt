package com.example.arrowescape.model

data class LongArrowConfig(
    val segments: List<GridPoint>,
    val direction: Direction,
    val colorIndex: Int = 0
)

data class Level(
    val id: Int,
    val name: String,
    val rows: Int,
    val cols: Int,
    val arrows: List<LongArrowConfig>,
    val parMoves: Int = arrows.size
)
