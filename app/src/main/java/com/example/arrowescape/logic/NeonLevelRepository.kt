package com.example.arrowescape.logic

import com.example.arrowescape.model.NeonArrow
import com.example.arrowescape.model.NeonLevel
import com.example.arrowescape.model.RawArrowConfig

object NeonLevelRepository {

    val levels: List<NeonLevel> by lazy {
        listOf(
            // Level 1: Gentle Glow (Simple 3 arrows)
            NeonLevel(
                id = 1,
                name = "First Spark",
                gridWidth = 10f,
                gridHeight = 14f,
                arrows = listOf(
                    // Pink U-shape pointing UP
                    RawArrowConfig(
                        gridPoints = listOf(2f to 7f, 2f to 2f, 5f to 2f, 5f to 5f),
                        color = NeonArrow.COLOR_PINK
                    ),
                    // Yellow pointing RIGHT (can escape first)
                    RawArrowConfig(
                        gridPoints = listOf(5f to 8f, 9f to 8f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Cyan L-shape pointing LEFT
                    RawArrowConfig(
                        gridPoints = listOf(8f to 2f, 8f to 5f, 3f to 5f),
                        color = NeonArrow.COLOR_CYAN
                    )
                )
            ),

            // Level 2: Neon Flow (5 winding arrows)
            NeonLevel(
                id = 2,
                name = "Neon Crossroads",
                gridWidth = 10f,
                gridHeight = 14f,
                arrows = listOf(
                    // Arrow 1: Top Cyan winding
                    RawArrowConfig(
                        gridPoints = listOf(2f to 4f, 2f to 2f, 7f to 2f, 7f to 4f),
                        color = NeonArrow.COLOR_CYAN
                    ),
                    // Arrow 2: Pink straight RIGHT
                    RawArrowConfig(
                        gridPoints = listOf(1f to 6f, 5f to 6f),
                        color = NeonArrow.COLOR_PINK
                    ),
                    // Arrow 3: Green pointing UP
                    RawArrowConfig(
                        gridPoints = listOf(3f to 10f, 3f to 7f),
                        color = NeonArrow.COLOR_GREEN
                    ),
                    // Arrow 4: Yellow L pointing DOWN
                    RawArrowConfig(
                        gridPoints = listOf(6f to 5f, 8f to 5f, 8f to 12f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Arrow 5: Purple pointing LEFT
                    RawArrowConfig(
                        gridPoints = listOf(7f to 8f, 1f to 8f),
                        color = NeonArrow.COLOR_PURPLE
                    )
                )
            ),

            // Level 3: Circuit Weave (8 winding arrows)
            NeonLevel(
                id = 3,
                name = "Circuit Weave",
                gridWidth = 10f,
                gridHeight = 16f,
                arrows = listOf(
                    RawArrowConfig(
                        gridPoints = listOf(1f to 1f, 8f to 1f, 8f to 3f),
                        color = NeonArrow.COLOR_PINK
                    ),
                    RawArrowConfig(
                        gridPoints = listOf(2f to 4f, 5f to 4f, 5f to 2f),
                        color = NeonArrow.COLOR_CYAN
                    ),
                    RawArrowConfig(
                        gridPoints = listOf(1f to 8f, 1f to 5f, 4f to 5f),
                        color = NeonArrow.COLOR_GREEN
                    ),
                    RawArrowConfig(
                        gridPoints = listOf(3f to 7f, 7f to 7f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    RawArrowConfig(
                        gridPoints = listOf(9f to 5f, 7f to 5f, 7f to 11f),
                        color = NeonArrow.COLOR_ORANGE
                    ),
                    RawArrowConfig(
                        gridPoints = listOf(2f to 11f, 5f to 11f, 5f to 9f),
                        color = NeonArrow.COLOR_PURPLE
                    ),
                    RawArrowConfig(
                        gridPoints = listOf(1f to 14f, 8f to 14f),
                        color = NeonArrow.COLOR_RED
                    ),
                    RawArrowConfig(
                        gridPoints = listOf(8f to 13f, 8f to 8f, 3f to 8f),
                        color = NeonArrow.COLOR_BLUE
                    )
                )
            ),

            // Level 4: The Reference Masterpiece (Directly recreating the screenshot!)
            NeonLevel(
                id = 4,
                name = "No Stress Maze",
                gridWidth = 10f,
                gridHeight = 16f,
                arrows = listOf(
                    // Top-Left Pink long outer hook: starts at (1, 6), goes to (1, 1), turns right to (5, 1) -> pointing RIGHT
                    RawArrowConfig(
                        gridPoints = listOf(1f to 6f, 1f to 1f, 5f to 1f),
                        color = NeonArrow.COLOR_PINK
                    ),
                    // Top-center Cyan snake: (4, 3) -> (2, 3) -> (2, 4) -> (6, 4) -> (6, 2) -> pointing UP
                    RawArrowConfig(
                        gridPoints = listOf(4f to 3f, 2f to 3f, 2f to 4f, 6f to 4f, 6f to 2f),
                        color = NeonArrow.COLOR_CYAN
                    ),
                    // Top Green Arrow: (2, 5) -> (2, 4) -> (2, 3.5f) pointing UP
                    RawArrowConfig(
                        gridPoints = listOf(2f to 5f, 2f to 3.5f),
                        color = NeonArrow.COLOR_GREEN
                    ),
                    // Top-Right Yellow Spiral: (8f to 1f, 9.2f to 1f, 9.2f to 3f, 7.5f to 3f, 7.5f to 2f) pointing UP
                    RawArrowConfig(
                        gridPoints = listOf(9.2f to 3f, 9.2f to 1.5f, 7.5f to 1.5f, 7.5f to 2.5f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Top-Right Red Hook: (8f to 2f, 8f to 4f, 9.5f to 4f) pointing RIGHT
                    RawArrowConfig(
                        gridPoints = listOf(8f to 2f, 8f to 3.5f, 9.5f to 3.5f),
                        color = NeonArrow.COLOR_RED
                    ),
                    // Upper-Right Yellow Flow: (6.5f to 4f, 7f to 4f, 7f to 5.5f, 9.5f to 5.5f, 9.5f to 4f)
                    RawArrowConfig(
                        gridPoints = listOf(7f to 4.5f, 7f to 6f, 9.2f to 6f, 9.2f to 4.5f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Pink middle pointer (finger target in screenshot): (5f to 5f, 7.5f to 5f) pointing RIGHT
                    RawArrowConfig(
                        gridPoints = listOf(5f to 5f, 7.5f to 5f),
                        color = NeonArrow.COLOR_PINK
                    ),
                    // Green small arrow pointing UP: (4f to 6f, 4f to 4.5f)
                    RawArrowConfig(
                        gridPoints = listOf(4f to 6f, 4f to 4.8f),
                        color = NeonArrow.COLOR_GREEN
                    ),
                    // Yellow straight arrow pointing RIGHT: (2.5f to 6.2f, 5f to 6.2f)
                    RawArrowConfig(
                        gridPoints = listOf(2.5f to 6.2f, 5f to 6.2f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Left Yellow Hook: (2f to 9f, 1f to 9f, 1f to 7.5f, 4f to 7.5f)
                    RawArrowConfig(
                        gridPoints = listOf(1f to 9f, 1f to 7f, 4.5f to 7f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Magenta Center Hook: (4.5f to 6f, 6.5f to 6f, 6.5f to 7.5f, 7f to 7.5f)
                    RawArrowConfig(
                        gridPoints = listOf(4.5f to 6f, 6.5f to 6f, 6.5f to 7.5f),
                        color = NeonArrow.COLOR_PINK
                    ),
                    // Pink Horseshoe Loop: (3.5f to 7.5f, 2.5f to 7.5f, 2.5f to 8.5f, 3.5f to 8.5f)
                    RawArrowConfig(
                        gridPoints = listOf(3.5f to 7.5f, 2.5f to 7.5f, 2.5f to 8.5f, 3.5f to 8.5f),
                        color = NeonArrow.COLOR_PINK
                    ),
                    // Red Hook pointing RIGHT: (3f to 7.5f, 5f to 7.5f)
                    RawArrowConfig(
                        gridPoints = listOf(3f to 7.5f, 5f to 7.5f),
                        color = NeonArrow.COLOR_RED
                    ),
                    // Center-Right Yellow Dual Upwards: (8f to 9f, 8f to 6.5f)
                    RawArrowConfig(
                        gridPoints = listOf(8f to 8.5f, 8f to 6.5f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Center-Right Yellow Second Upward: (9f to 9f, 9f to 6.5f)
                    RawArrowConfig(
                        gridPoints = listOf(9f to 8.5f, 9f to 6.5f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Blue down arrow: (4f to 9f, 4f to 10.5f)
                    RawArrowConfig(
                        gridPoints = listOf(4f to 9.5f, 4f to 11f),
                        color = NeonArrow.COLOR_BLUE
                    ),
                    // Yellow up arrow: (5.5f to 11.5f, 5.5f to 10f)
                    RawArrowConfig(
                        gridPoints = listOf(5.5f to 11.5f, 5.5f to 10f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Pink winding mid-lower: (2f to 11.5f, 2f to 11f, 5f to 11f, 5f to 12.5f)
                    RawArrowConfig(
                        gridPoints = listOf(2f to 11.5f, 2f to 10.5f, 5f to 10.5f),
                        color = NeonArrow.COLOR_PINK
                    ),
                    // Right Red Box-Loop: (7f to 13.5f, 7f to 10f, 9.5f to 10f, 9.5f to 11.5f, 8f to 11.5f)
                    RawArrowConfig(
                        gridPoints = listOf(7f to 13.5f, 7f to 10f, 9.5f to 10f, 9.5f to 11.5f, 7.5f to 11.5f),
                        color = NeonArrow.COLOR_RED
                    ),
                    // Red Bottom-Right Flow: (8f to 13.5f, 9.5f to 13.5f)
                    RawArrowConfig(
                        gridPoints = listOf(8f to 13.5f, 9.5f to 13.5f),
                        color = NeonArrow.COLOR_RED
                    ),
                    // Yellow Right-Bottom hook: (8f to 12.5f, 9f to 12.5f, 9f to 12f)
                    RawArrowConfig(
                        gridPoints = listOf(8f to 12.5f, 9f to 12.5f, 9f to 12f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Red Long Lower Track: (3f to 12.5f, 5f to 12.5f, 5.5f to 12f, 6.5f to 12f)
                    RawArrowConfig(
                        gridPoints = listOf(2f to 12.5f, 4.5f to 12.5f, 5.5f to 12f, 6.5f to 12f),
                        color = NeonArrow.COLOR_RED
                    ),
                    // Yellow Bottom-Left hook: (1f to 13.5f, 2f to 13.5f)
                    RawArrowConfig(
                        gridPoints = listOf(1f to 14f, 2.5f to 14f),
                        color = NeonArrow.COLOR_YELLOW
                    ),
                    // Orange Bottom Long Track: (1f to 15.2f, 3f to 15.2f, 3f to 14.5f, 7f to 14.5f)
                    RawArrowConfig(
                        gridPoints = listOf(1f to 15f, 3f to 15f, 3f to 14.2f, 7f to 14.2f),
                        color = NeonArrow.COLOR_ORANGE
                    ),
                    // Bottom Red Bar: (4f to 15.2f, 7.5f to 15.2f)
                    RawArrowConfig(
                        gridPoints = listOf(4f to 15.2f, 7.5f to 15.2f),
                        color = NeonArrow.COLOR_RED
                    ),
                    // Bottom-most Yellow Left-pointing Arrow: (9f to 15.5f, 1f to 15.5f)
                    RawArrowConfig(
                        gridPoints = listOf(9f to 15.5f, 1f to 15.5f),
                        color = NeonArrow.COLOR_YELLOW
                    )
                )
            )
        )
    }

    fun getLevel(id: Int): NeonLevel {
        return levels.find { it.id == id } ?: levels.first()
    }

    val totalLevels: Int get() = levels.size
}
