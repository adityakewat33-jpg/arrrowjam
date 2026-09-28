package com.example.arrowescape.model

import android.graphics.PointF

data class RawArrowConfig(
    val gridPoints: List<Pair<Float, Float>>, // Relative coordinates (0f to 10f, 0f to 16f)
    val color: Int
)

data class NeonLevel(
    val id: Int,
    val name: String,
    val gridWidth: Float = 10f,
    val gridHeight: Float = 16f,
    val arrows: List<RawArrowConfig>
)
