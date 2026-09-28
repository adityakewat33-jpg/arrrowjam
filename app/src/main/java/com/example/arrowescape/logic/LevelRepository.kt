package com.example.arrowescape.logic

import com.example.arrowescape.model.Direction
import com.example.arrowescape.model.GridPoint
import com.example.arrowescape.model.Level
import com.example.arrowescape.model.LongArrowConfig

object LevelRepository {
    val levels: List<Level> by lazy {
        listOf(
            // Level 1: Long Arrows Intro (4x4, straight 2-cell long arrows)
            Level(
                id = 1,
                name = "Long Arrows Intro",
                rows = 4,
                cols = 4,
                arrows = listOf(
                    LongArrowConfig(
                        segments = listOf(GridPoint(1, 2), GridPoint(1, 1)),
                        direction = Direction.RIGHT,
                        colorIndex = 0
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(2, 1), GridPoint(3, 1)),
                        direction = Direction.UP,
                        colorIndex = 1
                    )
                )
            ),

            // Level 2: Intersecting Spears (4x4, four 2-cell arrows)
            Level(
                id = 2,
                name = "Intersecting Spears",
                rows = 4,
                cols = 4,
                arrows = listOf(
                    LongArrowConfig(
                        segments = listOf(GridPoint(0, 1), GridPoint(1, 1)),
                        direction = Direction.UP,
                        colorIndex = 0
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(1, 3), GridPoint(1, 2)),
                        direction = Direction.RIGHT,
                        colorIndex = 1
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(3, 2), GridPoint(2, 2)),
                        direction = Direction.DOWN,
                        colorIndex = 2
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(2, 0), GridPoint(2, 1)),
                        direction = Direction.LEFT,
                        colorIndex = 3
                    )
                )
            ),

            // Level 3: Triple Beams (5x5, 3-cell long arrows)
            Level(
                id = 3,
                name = "Triple Beams",
                rows = 5,
                cols = 5,
                arrows = listOf(
                    LongArrowConfig(
                        segments = listOf(GridPoint(0, 2), GridPoint(1, 2), GridPoint(2, 2)),
                        direction = Direction.UP,
                        colorIndex = 0
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(2, 4), GridPoint(2, 3), GridPoint(2, 2)), // blocked by first
                        direction = Direction.RIGHT,
                        colorIndex = 1
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(4, 1), GridPoint(4, 2), GridPoint(4, 3)),
                        direction = Direction.LEFT,
                        colorIndex = 2
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(3, 0), GridPoint(3, 1)),
                        direction = Direction.LEFT,
                        colorIndex = 3
                    )
                )
            ),

            // Level 4: The Matrix (5x5, mixed 2 and 3-cell arrows)
            Level(
                id = 4,
                name = "Arrow Matrix",
                rows = 5,
                cols = 5,
                arrows = listOf(
                    LongArrowConfig(
                        segments = listOf(GridPoint(1, 0), GridPoint(1, 1), GridPoint(1, 2)),
                        direction = Direction.LEFT,
                        colorIndex = 0
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(0, 3), GridPoint(1, 3), GridPoint(2, 3)),
                        direction = Direction.UP,
                        colorIndex = 1
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(3, 4), GridPoint(3, 3), GridPoint(3, 2)),
                        direction = Direction.RIGHT,
                        colorIndex = 2
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(4, 1), GridPoint(3, 1), GridPoint(2, 1)),
                        direction = Direction.DOWN,
                        colorIndex = 3
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(2, 2), GridPoint(2, 1)),
                        direction = Direction.RIGHT,
                        colorIndex = 4
                    )
                )
            ),

            // Level 5: Lance Formation (6x6, long 3-cell & 4-cell arrows)
            Level(
                id = 5,
                name = "Lance Formation",
                rows = 6,
                cols = 6,
                arrows = listOf(
                    LongArrowConfig(
                        segments = listOf(GridPoint(0, 2), GridPoint(1, 2), GridPoint(2, 2), GridPoint(3, 2)),
                        direction = Direction.UP,
                        colorIndex = 0
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(2, 5), GridPoint(2, 4), GridPoint(2, 3)),
                        direction = Direction.RIGHT,
                        colorIndex = 1
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(5, 3), GridPoint(4, 3), GridPoint(3, 3), GridPoint(2, 3)),
                        direction = Direction.DOWN,
                        colorIndex = 2
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(3, 0), GridPoint(3, 1), GridPoint(3, 2)),
                        direction = Direction.LEFT,
                        colorIndex = 3
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(1, 4), GridPoint(1, 3)),
                        direction = Direction.UP,
                        colorIndex = 4
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(4, 1), GridPoint(4, 2)),
                        direction = Direction.DOWN,
                        colorIndex = 5
                    )
                )
            ),

            // Level 6: Grand Concourse (7x7, intricate long arrows)
            Level(
                id = 6,
                name = "Grand Concourse",
                rows = 7,
                cols = 7,
                arrows = listOf(
                    LongArrowConfig(
                        segments = listOf(GridPoint(0, 3), GridPoint(1, 3), GridPoint(2, 3), GridPoint(3, 3)),
                        direction = Direction.UP,
                        colorIndex = 0
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(3, 6), GridPoint(3, 5), GridPoint(3, 4)),
                        direction = Direction.RIGHT,
                        colorIndex = 1
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(6, 3), GridPoint(5, 3), GridPoint(4, 3)),
                        direction = Direction.DOWN,
                        colorIndex = 2
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(3, 0), GridPoint(3, 1), GridPoint(3, 2)),
                        direction = Direction.LEFT,
                        colorIndex = 3
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(1, 1), GridPoint(2, 1)),
                        direction = Direction.UP,
                        colorIndex = 4
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(5, 5), GridPoint(4, 5)),
                        direction = Direction.DOWN,
                        colorIndex = 5
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(1, 5), GridPoint(1, 4)),
                        direction = Direction.RIGHT,
                        colorIndex = 0
                    ),
                    LongArrowConfig(
                        segments = listOf(GridPoint(5, 1), GridPoint(5, 2)),
                        direction = Direction.LEFT,
                        colorIndex = 1
                    )
                )
            )
        )
    }

    fun getLevel(id: Int): Level {
        return levels.find { it.id == id } ?: levels.first()
    }

    val totalLevels: Int get() = levels.size
}
