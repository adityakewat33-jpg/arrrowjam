package com.example.arrowescape.logic

import com.example.arrowescape.model.DotGridArrow
import com.example.arrowescape.model.DotGridLevel
import com.example.arrowescape.model.RawDotArrow
import kotlin.random.Random

object DotGridLevelGenerator {

    private val colors = listOf(
        DotGridArrow.COLOR_YELLOW, DotGridArrow.COLOR_PINK,
        DotGridArrow.COLOR_CYAN, DotGridArrow.COLOR_GREEN,
        DotGridArrow.COLOR_ORANGE, DotGridArrow.COLOR_PURPLE,
        DotGridArrow.COLOR_RED, DotGridArrow.COLOR_BLUE
    )

    private val cardinals = listOf(
        -1 to 0, 1 to 0, 0 to -1, 0 to 1
    )

    data class LevelSpec(
        val name: String,
        val rows: Int,
        val cols: Int,
        val clusters: List<List<Pair<Int, Int>>>
    )

    enum class ShapeScale {
        SMALL,   // Decreased area (~13 to 24 dots)
        MEDIUM,  // Standard area (~28 to 52 dots)
        LARGE    // Increased area (~40 to 76 dots)
    }

    // --- Distinct Multi-Scale Shape Builders (Sculpted with Dots) ---

    // 1. Heart Shape ❤️
    fun buildHeart(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                listOf(1 to 1, 3 to 3),
                listOf(0 to 4),
                listOf(0 to 4),
                listOf(1 to 3),
                listOf(2 to 2)
            )
            ShapeScale.LARGE -> listOf(
                listOf(1 to 3, 5 to 7),
                listOf(0 to 8),
                listOf(0 to 8),
                listOf(0 to 8),
                listOf(1 to 7),
                listOf(2 to 6),
                listOf(3 to 5),
                listOf(4 to 4)
            )
            ShapeScale.MEDIUM -> listOf(
                listOf(1 to 2, 4 to 5),
                listOf(0 to 6),
                listOf(0 to 6),
                listOf(1 to 5),
                listOf(2 to 4),
                listOf(2 to 3)
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            for ((c1, c2) in spans[r]) {
                for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
            }
        }
        return dots
    }

    // 2. Diamond / Gem 💎
    fun buildDiamond(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                2 to 2, 1 to 3, 0 to 4, 1 to 3, 2 to 2
            )
            ShapeScale.LARGE -> listOf(
                4 to 5, 3 to 6, 2 to 7, 1 to 8, 0 to 9,
                0 to 9, 1 to 8, 2 to 7, 3 to 6, 4 to 5
            )
            ShapeScale.MEDIUM -> listOf(
                3 to 4, 2 to 5, 1 to 6, 0 to 7,
                0 to 7, 1 to 6, 2 to 5, 3 to 4
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            val (c1, c2) = spans[r]
            for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
        }
        return dots
    }

    // 3. Kite Shape 🪁
    fun buildKite(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                2 to 2, 1 to 3, 0 to 4, 1 to 3, 2 to 2, 2 to 2
            )
            ShapeScale.LARGE -> listOf(
                3 to 4, 2 to 5, 1 to 6, 0 to 7, 0 to 7,
                1 to 6, 2 to 5, 2 to 5, 3 to 4, 3 to 4
            )
            ShapeScale.MEDIUM -> listOf(
                2 to 3, 1 to 4, 0 to 5, 0 to 5,
                1 to 4, 1 to 4, 2 to 3, 2 to 3
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            val (c1, c2) = spans[r]
            for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
        }
        return dots
    }

    // 4. Cross / Plus ➕
    fun buildCross(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val dots = mutableListOf<Pair<Int, Int>>()
        when (scale) {
            ShapeScale.SMALL -> {
                for (r in 0 until 5) {
                    for (c in 0 until 5) {
                        if (r == 2 || c == 2) dots.add((r0 + r) to (c0 + c))
                    }
                }
            }
            ShapeScale.LARGE -> {
                for (r in 0 until 10) {
                    for (c in 0 until 10) {
                        if ((r in 3..6) || (c in 3..6)) dots.add((r0 + r) to (c0 + c))
                    }
                }
            }
            ShapeScale.MEDIUM -> {
                for (r in 0 until 8) {
                    for (c in 0 until 8) {
                        if ((r in 2..5) || (c in 2..5)) dots.add((r0 + r) to (c0 + c))
                    }
                }
            }
        }
        return dots
    }

    // 5. Shield / Crest 🛡️
    fun buildShield(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                0 to 4, 0 to 4, 0 to 4, 1 to 3, 2 to 2
            )
            ShapeScale.LARGE -> listOf(
                0 to 9, 0 to 9, 0 to 9, 0 to 9, 0 to 9,
                1 to 8, 2 to 7, 2 to 7, 3 to 6, 4 to 5
            )
            ShapeScale.MEDIUM -> listOf(
                0 to 7, 0 to 7, 0 to 7, 0 to 7,
                1 to 6, 2 to 5, 2 to 5, 3 to 4
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            val (c1, c2) = spans[r]
            for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
        }
        return dots
    }

    // 6. Hourglass ⏳
    fun buildHourglass(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                0 to 4, 1 to 3, 2 to 2, 1 to 3, 0 to 4
            )
            ShapeScale.LARGE -> listOf(
                0 to 9, 0 to 9, 1 to 8, 2 to 7, 3 to 6,
                3 to 6, 2 to 7, 1 to 8, 0 to 9, 0 to 9
            )
            ShapeScale.MEDIUM -> listOf(
                0 to 7, 0 to 7, 1 to 6, 2 to 5,
                2 to 5, 1 to 6, 0 to 7, 0 to 7
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            val (c1, c2) = spans[r]
            for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
        }
        return dots
    }

    // 7. Starburst ⭐
    fun buildStar(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                2 to 2, 1 to 3, 0 to 4, 1 to 3, 2 to 2
            )
            ShapeScale.LARGE -> listOf(
                4 to 5, 3 to 6, 2 to 7, 1 to 8, 0 to 9,
                0 to 9, 1 to 8, 2 to 7, 3 to 6, 4 to 5
            )
            ShapeScale.MEDIUM -> listOf(
                3 to 4, 2 to 5, 1 to 6, 0 to 7,
                0 to 7, 1 to 6, 2 to 5, 3 to 4
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            val (c1, c2) = spans[r]
            for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
        }
        return dots
    }

    // 8. Royal Crown 👑
    fun buildCrown(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                listOf(0 to 0, 2 to 2, 4 to 4),
                listOf(0 to 4),
                listOf(0 to 4),
                listOf(0 to 4)
            )
            ShapeScale.LARGE -> listOf(
                listOf(0 to 1, 4 to 5, 8 to 9),
                listOf(0 to 9),
                listOf(0 to 9),
                listOf(0 to 9),
                listOf(0 to 9),
                listOf(0 to 9),
                listOf(0 to 9)
            )
            ShapeScale.MEDIUM -> listOf(
                listOf(0 to 1, 3 to 3, 5 to 6),
                listOf(0 to 6),
                listOf(0 to 6),
                listOf(0 to 6),
                listOf(0 to 6),
                listOf(0 to 6)
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            for ((c1, c2) in spans[r]) {
                for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
            }
        }
        return dots
    }

    // 9. Lightning Bolt ⚡
    fun buildLightning(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                listOf(2 to 3),
                listOf(1 to 2),
                listOf(0 to 3),
                listOf(0 to 2),
                listOf(1 to 2),
                listOf(2 to 2)
            )
            ShapeScale.LARGE -> listOf(
                listOf(5 to 7),
                listOf(4 to 6),
                listOf(3 to 5),
                listOf(2 to 4),
                listOf(1 to 8),
                listOf(0 to 5),
                listOf(1 to 4),
                listOf(2 to 5),
                listOf(3 to 6),
                listOf(4 to 5)
            )
            ShapeScale.MEDIUM -> listOf(
                listOf(4 to 6),
                listOf(3 to 5),
                listOf(2 to 4),
                listOf(1 to 6),
                listOf(0 to 4),
                listOf(1 to 3),
                listOf(2 to 4),
                listOf(3 to 4)
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            for ((c1, c2) in spans[r]) {
                for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
            }
        }
        return dots
    }

    // 10. Vortex / Spiral 🌀
    fun buildVortex(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                listOf(0 to 5),
                listOf(0 to 1, 4 to 5),
                listOf(0 to 1, 2 to 5),
                listOf(0 to 3, 4 to 5),
                listOf(0 to 1, 4 to 5),
                listOf(0 to 5)
            )
            ShapeScale.LARGE -> listOf(
                listOf(0 to 9),
                listOf(0 to 1, 8 to 9),
                listOf(0 to 1, 4 to 9),
                listOf(0 to 1, 4 to 5, 8 to 9),
                listOf(0 to 1, 4 to 5, 8 to 9),
                listOf(0 to 1, 4 to 5, 8 to 9),
                listOf(0 to 5, 8 to 9),
                listOf(0 to 1, 8 to 9),
                listOf(0 to 9)
            )
            ShapeScale.MEDIUM -> listOf(
                listOf(0 to 7),
                listOf(0 to 1, 6 to 7),
                listOf(0 to 1, 3 to 7),
                listOf(0 to 1, 3 to 4, 6 to 7),
                listOf(0 to 1, 3 to 4, 6 to 7),
                listOf(0 to 4, 6 to 7),
                listOf(0 to 1, 6 to 7),
                listOf(0 to 7)
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            for ((c1, c2) in spans[r]) {
                for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
            }
        }
        return dots
    }

    // 11. Infinity ♾️
    fun buildInfinity(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                listOf(1 to 2, 4 to 5),
                listOf(0 to 1, 2 to 4, 5 to 6),
                listOf(0 to 1, 2 to 4, 5 to 6),
                listOf(1 to 2, 4 to 5)
            )
            ShapeScale.LARGE -> listOf(
                listOf(1 to 4, 7 to 10),
                listOf(0 to 2, 4 to 7, 9 to 11),
                listOf(0 to 2, 4 to 7, 9 to 11),
                listOf(0 to 2, 4 to 7, 9 to 11),
                listOf(0 to 2, 4 to 7, 9 to 11),
                listOf(1 to 4, 7 to 10)
            )
            ShapeScale.MEDIUM -> listOf(
                listOf(1 to 3, 5 to 7),
                listOf(0 to 1, 3 to 5, 7 to 8),
                listOf(0 to 1, 3 to 5, 7 to 8),
                listOf(0 to 1, 3 to 5, 7 to 8),
                listOf(1 to 3, 5 to 7)
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            for ((c1, c2) in spans[r]) {
                for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
            }
        }
        return dots
    }

    // 12. Anchor ⚓
    fun buildAnchor(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                listOf(1 to 3),
                listOf(1 to 1, 3 to 3),
                listOf(1 to 3),
                listOf(0 to 4),
                listOf(2 to 2),
                listOf(0 to 0, 2 to 2, 4 to 4),
                listOf(0 to 4)
            )
            ShapeScale.LARGE -> listOf(
                listOf(3 to 5),
                listOf(3 to 3, 5 to 5),
                listOf(3 to 5),
                listOf(0 to 8),
                listOf(4 to 4),
                listOf(4 to 4),
                listOf(4 to 4),
                listOf(0 to 1, 4 to 4, 7 to 8),
                listOf(0 to 8)
            )
            ShapeScale.MEDIUM -> listOf(
                listOf(2 to 4),
                listOf(2 to 2, 4 to 4),
                listOf(2 to 4),
                listOf(0 to 6),
                listOf(3 to 3),
                listOf(3 to 3),
                listOf(0 to 1, 3 to 3, 5 to 6),
                listOf(0 to 6)
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            for ((c1, c2) in spans[r]) {
                for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
            }
        }
        return dots
    }

    // 13. Planet Saturn 🪐
    fun buildPlanet(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                listOf(2 to 4),
                listOf(0 to 6),
                listOf(0 to 6),
                listOf(2 to 4)
            )
            ShapeScale.LARGE -> listOf(
                listOf(4 to 6),
                listOf(3 to 7),
                listOf(0 to 10),
                listOf(0 to 10),
                listOf(0 to 10),
                listOf(3 to 7),
                listOf(4 to 6)
            )
            ShapeScale.MEDIUM -> listOf(
                listOf(3 to 5),
                listOf(2 to 6),
                listOf(0 to 8),
                listOf(0 to 8),
                listOf(2 to 6),
                listOf(3 to 5)
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            for ((c1, c2) in spans[r]) {
                for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
            }
        }
        return dots
    }

    // 14. Crossed Swords ⚔️
    fun buildSwords(r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): List<Pair<Int, Int>> {
        val spans = when (scale) {
            ShapeScale.SMALL -> listOf(
                listOf(0 to 1, 4 to 5),
                listOf(1 to 2, 3 to 4),
                listOf(2 to 3),
                listOf(1 to 2, 3 to 4),
                listOf(0 to 2, 3 to 5)
            )
            ShapeScale.LARGE -> listOf(
                listOf(0 to 1, 8 to 9),
                listOf(1 to 2, 7 to 8),
                listOf(2 to 3, 6 to 7),
                listOf(3 to 6),
                listOf(4 to 5),
                listOf(3 to 6),
                listOf(0 to 3, 6 to 9),
                listOf(0 to 1, 8 to 9)
            )
            ShapeScale.MEDIUM -> listOf(
                listOf(0 to 1, 6 to 7),
                listOf(1 to 2, 5 to 6),
                listOf(2 to 5),
                listOf(3 to 4),
                listOf(2 to 5),
                listOf(0 to 2, 5 to 7),
                listOf(0 to 0, 7 to 7)
            )
        }
        val dots = mutableListOf<Pair<Int, Int>>()
        for (r in spans.indices) {
            for ((c1, c2) in spans[r]) {
                for (c in c1..c2) dots.add((r0 + r) to (c0 + c))
            }
        }
        return dots
    }

    fun getShapeByIndex(index: Int, r0: Int, c0: Int, scale: ShapeScale = ShapeScale.MEDIUM): Pair<String, List<Pair<Int, Int>>> {
        val scaleSuffix = when (scale) {
            ShapeScale.SMALL -> " (Mini)"
            ShapeScale.LARGE -> " (Grand)"
            ShapeScale.MEDIUM -> ""
        }
        val (baseName, dots) = when (Math.floorMod(index, 14)) {
            0 -> "Heart" to buildHeart(r0, c0, scale)
            1 -> "Diamond" to buildDiamond(r0, c0, scale)
            2 -> "Kite" to buildKite(r0, c0, scale)
            3 -> "Cross" to buildCross(r0, c0, scale)
            4 -> "Shield" to buildShield(r0, c0, scale)
            5 -> "Hourglass" to buildHourglass(r0, c0, scale)
            6 -> "Star" to buildStar(r0, c0, scale)
            7 -> "Crown" to buildCrown(r0, c0, scale)
            8 -> "Lightning" to buildLightning(r0, c0, scale)
            9 -> "Vortex" to buildVortex(r0, c0, scale)
            10 -> "Infinity" to buildInfinity(r0, c0, scale)
            11 -> "Anchor" to buildAnchor(r0, c0, scale)
            12 -> "Planet" to buildPlanet(r0, c0, scale)
            else -> "Swords" to buildSwords(r0, c0, scale)
        }
        return "$baseName$scaleSuffix" to dots
    }

    // --- Master Level Specification Generator (200 Progressive Levels) ---
    // Every 5 levels (tier5), the difficulty and number of arrows strictly increase!
    fun getLevelSpec(lvlNum: Int): LevelSpec {
        val tier5 = (lvlNum - 1) / 5   // 0 for Lv 1-5, 1 for Lv 6-10, 2 for Lv 11-15, etc.
        val sub = (lvlNum - 1) % 5    // 0 to 4 within the 5-level tier

        when (tier5) {
            // Tier 0 (Levels 1 to 5): Intro Mini Shapes (9 to 19 dots -> 3 to 7 arrows, straight tutorial arrows)
            0 -> {
                val (shapeIdx, shapeScale) = when (sub) {
                    0 -> 3 to ShapeScale.SMALL    // Cross Mini: 9 dots -> ~3-4 straight arrows
                    1 -> 6 to ShapeScale.SMALL    // Star Mini: 13 dots -> ~4-5 arrows
                    2 -> 2 to ShapeScale.SMALL    // Kite Mini: 14 dots -> ~4-5 arrows
                    3 -> 0 to ShapeScale.SMALL    // Heart Mini: 16 dots -> ~5-6 arrows
                    else -> 4 to ShapeScale.SMALL // Shield Mini: 19 dots -> ~6-7 arrows
                }
                val (sName, dots) = getShapeByIndex(shapeIdx, 2, 2, shapeScale)
                val maxR = dots.maxOf { it.first } + 2
                val maxC = dots.maxOf { it.second } + 2
                return LevelSpec("Totem $lvlNum ($sName)", maxR, maxC, listOf(dots))
            }

            // Tier 1 (Levels 6 to 10): Compact Shapes (22 to 34 dots -> 7 to 11 arrows)
            1 -> {
                val (shapeIdx, shapeScale) = when (sub) {
                    0 -> 11 to ShapeScale.SMALL     // Anchor Mini: 22 dots -> ~7-8 arrows
                    1 -> 13 to ShapeScale.MEDIUM    // Swords Medium: 26 dots -> ~8-9 arrows
                    2 -> 0 to ShapeScale.MEDIUM     // Heart Medium: 28 dots -> ~9 arrows
                    3 -> 2 to ShapeScale.MEDIUM     // Kite Medium: 30 dots -> ~9-10 arrows
                    else -> 12 to ShapeScale.MEDIUM // Planet Medium: 34 dots -> ~10-11 arrows
                }
                val (sName, dots) = getShapeByIndex(shapeIdx, 2, 2, shapeScale)
                val maxR = dots.maxOf { it.first } + 2
                val maxC = dots.maxOf { it.second } + 2
                return LevelSpec("Totem $lvlNum ($sName)", maxR, maxC, listOf(dots))
            }

            // Tier 2 (Levels 11 to 15): Medium Monoliths (38 to 48 dots -> 12 to 15 arrows)
            2 -> {
                val (shapeIdx, shapeScale) = when (sub) {
                    0 -> 1 to ShapeScale.MEDIUM     // Diamond Medium: 40 dots -> ~11-12 arrows
                    1 -> 6 to ShapeScale.MEDIUM     // Star Medium: 40 dots -> ~12-13 arrows
                    2 -> 7 to ShapeScale.MEDIUM     // Crown Medium: 40 dots -> ~13 arrows
                    3 -> 3 to ShapeScale.MEDIUM     // Cross Medium: 48 dots -> ~14-15 arrows
                    else -> 4 to ShapeScale.MEDIUM   // Shield Medium: 48 dots -> ~15 arrows
                }
                val (sName, dots) = getShapeByIndex(shapeIdx, 2, 2, shapeScale)
                val maxR = dots.maxOf { it.first } + 2
                val maxC = dots.maxOf { it.second } + 2
                return LevelSpec("Totem $lvlNum ($sName)", maxR, maxC, listOf(dots))
            }

            // Tier 3 (Levels 16 to 20): Grand Monoliths (46 to 76 dots -> 16 to 24 arrows)
            3 -> {
                val (shapeIdx, shapeScale) = when (sub) {
                    0 -> 2 to ShapeScale.LARGE      // Kite Grand: 46 dots -> ~16 arrows
                    1 -> 0 to ShapeScale.LARGE      // Heart Grand: 49 dots -> ~17-18 arrows
                    2 -> 1 to ShapeScale.LARGE      // Diamond Grand: 60 dots -> ~19-20 arrows
                    3 -> 7 to ShapeScale.LARGE      // Crown Grand: 66 dots -> ~20-21 arrows
                    else -> 4 to ShapeScale.LARGE   // Shield Grand: 76 dots -> ~22-24 arrows
                }
                val (sName, dots) = getShapeByIndex(shapeIdx, 2, 2, shapeScale)
                val maxR = dots.maxOf { it.first } + 2
                val maxC = dots.maxOf { it.second } + 2
                return LevelSpec("Totem $lvlNum ($sName)", maxR, maxC, listOf(dots))
            }

            // Tier 4 (Levels 21 to 25): Twin Constellations (~65 to 85 dots -> 22 to 27 arrows)
            4 -> {
                return buildTwinLevel(lvlNum, sub, ShapeScale.MEDIUM, ShapeScale.MEDIUM)
            }

            // Tier 5 (Levels 26 to 30): Twin Medium & Large (~85 to 105 dots -> 27 to 33 arrows)
            5 -> {
                return buildTwinLevel(lvlNum, sub, ShapeScale.MEDIUM, ShapeScale.LARGE)
            }

            // Tier 6 (Levels 31 to 35): Twin Titans (~110 to 135 dots -> 34 to 40 arrows)
            6 -> {
                return buildTwinLevel(lvlNum, sub, ShapeScale.LARGE, ShapeScale.LARGE)
            }

            // Tier 7 (Levels 36 to 40): Triad Constellations (3 Shapes -> 40 to 46 arrows)
            7 -> {
                return buildTriadLevel(lvlNum, sub, ShapeScale.MEDIUM, ShapeScale.MEDIUM)
            }

            // Tier 8 (Levels 41 to 45): Triad Grands (3 Large Shapes -> 46 to 52 arrows)
            8 -> {
                return buildTriadLevel(lvlNum, sub, ShapeScale.MEDIUM, ShapeScale.LARGE)
            }

            // Tier 9 (Levels 46 to 50): Quad Constellation (4 Shapes in 2x2 grid -> 53 to 62 arrows)
            9 -> {
                return buildGridLevel(lvlNum, 2, 2, "Quad Constellation")
            }

            // Tier 10 to 14 (Levels 51 to 75): Growing Archipelagos (5, 6, 7, 8, 9 shapes)
            in 10..14 -> {
                val clusterCount = 5 + (tier5 - 10)
                val gCols = if (clusterCount <= 6) 2 else 3
                val gRows = (clusterCount + gCols - 1) / gCols
                return buildFlexibleGridLevel(lvlNum, clusterCount, gRows, gCols, "Archipelago")
            }

            // Tier 15 to 19 (Levels 76 to 100): 10 to 14 Clusters
            in 15..19 -> {
                val clusterCount = 10 + (tier5 - 15)
                val gCols = 3
                val gRows = (clusterCount + gCols - 1) / gCols
                return buildFlexibleGridLevel(lvlNum, clusterCount, gRows, gCols, "Galaxy")
            }

            // Tier 20 to 27 (Levels 101 to 140): 15 to 22 Clusters
            in 20..27 -> {
                val clusterCount = 15 + (tier5 - 20)
                val gCols = 4
                val gRows = (clusterCount + gCols - 1) / gCols
                return buildFlexibleGridLevel(lvlNum, clusterCount, gRows, gCols, "Cosmic Sector")
            }

            // Tier 28 to 39 (Levels 141 to 200): 23 to 34 Clusters
            else -> {
                val clusterCount = (23 + (tier5 - 28)).coerceAtMost(36)
                val gCols = 5
                val gRows = (clusterCount + gCols - 1) / gCols
                return buildFlexibleGridLevel(lvlNum, clusterCount, gRows, gCols, "Multiverse Sovereign")
            }
        }
    }

    private fun buildTwinLevel(lvlNum: Int, sub: Int, sc1: ShapeScale, sc2: ShapeScale): LevelSpec {
        val pool = listOf(0, 1, 2, 4, 6, 7, 9, 12) // Solid dot density: Heart, Diamond, Kite, Shield, Star, Crown, Vortex, Planet
        val sIdx1 = pool[(lvlNum * 2) % pool.size]
        val sIdx2 = pool[(lvlNum * 2 + 1) % pool.size]
        val (sName1, dots1) = getShapeByIndex(sIdx1, 2, 2, sc1)
        val (sName2, dots2) = if (sub % 2 == 1) {
            val cOffset = dots1.maxOf { it.second } + 4
            getShapeByIndex(sIdx2, 2, cOffset, sc2)
        } else {
            val rOffset = dots1.maxOf { it.first } + 4
            getShapeByIndex(sIdx2, rOffset, 2, sc2)
        }
        val allDots = dots1 + dots2
        val rows = allDots.maxOf { it.first } + 3
        val cols = allDots.maxOf { it.second } + 3
        return LevelSpec("Twin $sName1 & $sName2 ($lvlNum)", rows, cols, listOf(dots1, dots2))
    }

    private fun buildTriadLevel(lvlNum: Int, sub: Int, sc1: ShapeScale, sc2: ShapeScale): LevelSpec {
        val pool = listOf(0, 1, 2, 4, 6, 7, 9, 12)
        val sIdx1 = pool[(lvlNum * 3) % pool.size]
        val sIdx2 = pool[(lvlNum * 3 + 1) % pool.size]
        val sIdx3 = pool[(lvlNum * 3 + 2) % pool.size]
        val scale = if (sub >= 3) sc2 else sc1
        val (sName1, d1) = getShapeByIndex(sIdx1, 2, 10, scale)
        val maxR1 = d1.maxOf { it.first }
        val (sName2, d2) = getShapeByIndex(sIdx2, maxR1 + 4, 2, scale)
        val maxC2 = d2.maxOf { it.second }
        val (sName3, d3) = getShapeByIndex(sIdx3, maxR1 + 4, maxC2 + 4, scale)
        val allDots = d1 + d2 + d3
        val rows = allDots.maxOf { it.first } + 3
        val cols = allDots.maxOf { it.second } + 3
        return LevelSpec("Triad $sName1 Constellation ($lvlNum)", rows, cols, listOf(d1, d2, d3))
    }

    private fun buildGridLevel(lvlNum: Int, gridRows: Int, gridCols: Int, typeName: String): LevelSpec {
        val gap = 3
        val clusters = mutableListOf<List<Pair<Int, Int>>>()
        val tier5 = (lvlNum - 1) / 5
        val scale = if (tier5 >= 20) ShapeScale.SMALL else ShapeScale.MEDIUM
        val pool = listOf(0, 1, 2, 4, 6, 7, 9, 12)

        for (gr in 0 until gridRows) {
            for (gc in 0 until gridCols) {
                val shapeIdx = pool[(lvlNum + gr * gridCols + gc) % pool.size]
                val r0 = 2 + gr * (12 + gap)
                val c0 = 2 + gc * (12 + gap)
                val (_, d) = getShapeByIndex(shapeIdx, r0, c0, scale)
                clusters.add(d)
            }
        }
        val allDots = clusters.flatten()
        val rows = (allDots.maxOf { it.first } + 3).coerceAtMost(100)
        val cols = (allDots.maxOf { it.second } + 3).coerceAtMost(100)
        return LevelSpec("$typeName $lvlNum", rows, cols, clusters)
    }

    private fun buildFlexibleGridLevel(lvlNum: Int, clusterCount: Int, gridRows: Int, gridCols: Int, typeName: String): LevelSpec {
        val gap = 3
        val clusters = mutableListOf<List<Pair<Int, Int>>>()
        val pool = listOf(0, 1, 2, 4, 6, 7, 9, 12)
        val tier5 = (lvlNum - 1) / 5
        val scale = if (tier5 >= 25) ShapeScale.SMALL else ShapeScale.MEDIUM
        val dim = if (scale == ShapeScale.SMALL) 9 else 12

        var added = 0
        for (gr in 0 until gridRows) {
            for (gc in 0 until gridCols) {
                if (added >= clusterCount) break
                val shapeIdx = pool[(lvlNum + added) % pool.size]
                val r0 = 2 + gr * (dim + gap)
                val c0 = 2 + gc * (dim + gap)
                val (_, d) = getShapeByIndex(shapeIdx, r0, c0, scale)
                clusters.add(d)
                added++
            }
            if (added >= clusterCount) break
        }
        val allDots = clusters.flatten()
        val rows = (allDots.maxOf { it.first } + 3).coerceAtMost(100)
        val cols = (allDots.maxOf { it.second } + 3).coerceAtMost(100)
        return LevelSpec("$typeName $lvlNum ($clusterCount Sectors)", rows, cols, clusters)
    }

    fun generateLevel(id: Int): DotGridLevel {
        val spec = getLevelSpec(id)
        val rng = Random(id * 9973L + 42L)

        // Sort clusters innermost first (closest to board center) so inner arrows escape via empty outer space
        val crMid = spec.rows / 2.0
        val ccMid = spec.cols / 2.0
        val sortedClusters = spec.clusters.sortedBy { c ->
            val avgR = c.sumOf { it.first } / c.size.toDouble()
            val avgC = c.sumOf { it.second } / c.size.toDouble()
            (avgR - crMid) * (avgR - crMid) + (avgC - ccMid) * (avgC - ccMid)
        }

        for (attempt in 0 until 30) {
            val globalOccupied = HashSet<Pair<Int, Int>>()
            val allArrows = mutableListOf<List<Pair<Int, Int>>>()
            var clustersOk = true

            val allClustersDots = spec.clusters.flatten().toSet()
            for (cluster in sortedClusters) {
                var clusterOk = false
                val otherClustersDots = allClustersDots - cluster.toSet()
                for (cAtt in 0 until 35) {
                    val occupiedForCluster = (globalOccupied + otherClustersDots)
                    val (success, cArrows) = packCluster(cluster, spec.rows, spec.cols, occupiedForCluster, rng, id, allClustersDots.size)
                    if (success) {
                        clusterOk = true
                        for (a in cArrows) {
                            allArrows.add(a)
                            for (d in a) globalOccupied.add(d)
                        }
                        break
                    }
                }
                if (!clusterOk) {
                    clustersOk = false
                    break
                }
            }

            if (clustersOk && allArrows.isNotEmpty()) {
                val rawArrows = allArrows.mapIndexed { idx, arr ->
                    RawDotArrow(arr, colors[idx % colors.size])
                }
                val candidate = DotGridLevel(id, spec.name, spec.rows, spec.cols, rawArrows)
                if (verifySolvable(candidate)) {
                    return candidate
                }
            }
        }

        // Safe fallback if random attempts exhausted
        return fallbackDenseLevel(id, spec)
    }

    fun getTargetArrowCount(lvlNum: Int): Int {
        val tier5 = (lvlNum - 1) / 5
        val sub = (lvlNum - 1) % 5
        return when (tier5) {
            0 -> 3 + sub // Lv 1-5: 3, 4, 5, 6, 7 arrows
            1 -> 8 + sub // Lv 6-10: 8, 9, 10, 11, 12 arrows
            2 -> 13 + sub // Lv 11-15: 13, 14, 15, 16, 17 arrows
            3 -> 18 + sub // Lv 16-20: 18, 19, 20, 21, 22 arrows
            4 -> 23 + sub // Lv 21-25: 23, 24, 25, 26, 27 arrows
            5 -> 28 + sub // Lv 26-30: 28, 29, 30, 31, 32 arrows
            6 -> 33 + sub // Lv 31-35: 33, 34, 35, 36, 37 arrows
            7 -> 38 + sub // Lv 36-40: 38, 39, 40, 41, 42 arrows
            8 -> 43 + sub // Lv 41-45: 43, 44, 45, 46, 47 arrows
            9 -> 48 + sub * 2 // Lv 46-50: 48, 50, 52, 54, 56 arrows
            else -> 56 + (tier5 - 9) * 6 + sub * 2
        }
    }

    internal fun packCluster(
        cDots: List<Pair<Int, Int>>,
        boardRows: Int,
        boardCols: Int,
        globalOccupied: Set<Pair<Int, Int>>,
        rng: Random,
        lvlNum: Int,
        boardTotalDots: Int = cDots.size
    ): Pair<Boolean, List<List<Pair<Int, Int>>>> {
        val unfilled = cDots.toMutableSet()
        val occupied = globalOccupied.toMutableSet()
        val clusterArrows = mutableListOf<MutableList<Pair<Int, Int>>>()

        fun isExitClear(headR: Int, headC: Int, dr: Int, dc: Int): Boolean {
            var cr = headR + dr
            var cc = headC + dc
            while (cr in 0 until boardRows && cc in 0 until boardCols) {
                if (occupied.contains(cr to cc)) return false
                cr += dr
                cc += dc
            }
            return true
        }

        // Phase 1: Place reverse arrows inside cluster (scaling length & turns with 5-level tier)
        val tier5 = (lvlNum - 1) / 5
        val targetTotal = getTargetArrowCount(lvlNum)
        val targetClusterArrows = (targetTotal.toDouble() * (cDots.size.toDouble() / boardTotalDots.coerceAtLeast(1))).coerceAtLeast(1.5)
        val idealLen = (cDots.size / targetClusterArrows).coerceIn(2.0, 5.0)
        val (minLen, maxLen) = when {
            idealLen <= 2.6 -> 2 to 3
            idealLen <= 3.3 -> 2 to 4
            idealLen <= 3.9 -> 3 to 4
            idealLen <= 4.6 -> 3 to 5
            else -> 4 to 6
        }
        val turnProbability = when (tier5) {
            0 -> 0.05 // Level 1-5: straight tutorial arrows, immediate clarity
            1 -> 0.12 // Level 6-10: gentle single corners
            2 -> 0.20 // Level 11-15: corners and L-bends
            3 -> 0.28 // Level 16-20
            4 -> 0.35 // Level 21-25
            5 -> 0.42 // Level 26-30
            else -> (0.45 + (tier5 - 6) * 0.012).coerceAtMost(0.75)
        }

        for (step in 0 until 800) {
            if (unfilled.isEmpty()) break
            val candidates = unfilled.shuffled(rng)
            for (head in candidates) {
                val (headR, headC) = head
                val validDirs = cardinals.filter { (dr, dc) -> isExitClear(headR, headC, dr, dc) }.shuffled(rng)
                if (validDirs.isEmpty()) continue

                var placed = false
                for ((dr, dc) in validDirs) {
                    val firstBack = (headR - dr) to (headC - dc)
                    if (!unfilled.contains(firstBack)) continue

                    val dots = mutableListOf(head, firstBack)
                    val tLen = rng.nextInt(if (unfilled.size <= 5) 2 else minLen, maxLen + 1).coerceAtMost(unfilled.size)
                    var curr = firstBack
                    var lastDir = (firstBack.first - headR) to (firstBack.second - headC)
                    val dSet = dots.toMutableSet()

                    for (s in 0 until (tLen - 2)) {
                        val nbrs = cardinals.map { (ndr, ndc) ->
                            ((curr.first + ndr) to (curr.second + ndc)) to (ndr to ndc)
                        }.filter { unfilled.contains(it.first) && !dSet.contains(it.first) }
                        if (nbrs.isEmpty()) break

                        // Warnsdorff's heuristic: prefer neighbors with fewer unfilled neighbors to avoid stranding dots
                        val minDegree = nbrs.minOf { n ->
                            cardinals.count { (ndr, ndc) ->
                                val p = (n.first.first + ndr) to (n.first.second + ndc)
                                unfilled.contains(p) && !dSet.contains(p)
                            }
                        }
                        val bestNbrs = nbrs.filter { n ->
                            val deg = cardinals.count { (ndr, ndc) ->
                                val p = (n.first.first + ndr) to (n.first.second + ndc)
                                unfilled.contains(p) && !dSet.contains(p)
                            }
                            deg == minDegree
                        }

                        val turns = bestNbrs.filter { it.second != lastDir }
                        val chosen = if (turns.isNotEmpty() && rng.nextDouble() < turnProbability) {
                            turns.random(rng)
                        } else {
                            bestNbrs.random(rng)
                        }
                        dots.add(chosen.first)
                        dSet.add(chosen.first)
                        lastDir = chosen.second
                        curr = chosen.first
                    }

                    // Check forward exit clearance does not self-intersect
                    var selfBlocks = false
                    var cr = headR + dr
                    var cc = headC + dc
                    while (cr in 0 until boardRows && cc in 0 until boardCols) {
                        if (dSet.contains(cr to cc)) {
                            selfBlocks = true
                            break
                        }
                        cr += dr
                        cc += dc
                    }
                    if (selfBlocks) continue

                    val tailToHead = dots.reversed().toMutableList()
                    for (d in tailToHead) {
                        occupied.add(d)
                        unfilled.remove(d)
                    }
                    clusterArrows.add(tailToHead)
                    placed = true
                    break
                }
                if (placed) break
            }
        }

        // Phase 2: Absorb remaining dots into adjacent heads and tails
        var changed = true
        while (unfilled.isNotEmpty() && changed) {
            changed = false

            // Try 1: Pair 2 adjacent unfilled dots into a new 2-dot arrow
            if (unfilled.size >= 2) {
                for (u1 in unfilled.toList()) {
                    if (!unfilled.contains(u1)) continue
                    for ((dr, dc) in cardinals.shuffled(rng)) {
                        val u2 = (u1.first + dr) to (u1.second + dc)
                        if (unfilled.contains(u2) && isExitClear(u2.first, u2.second, dr, dc)) {
                            val newArr = mutableListOf(u1, u2)
                            clusterArrows.add(newArr)
                            occupied.add(u1)
                            occupied.add(u2)
                            unfilled.remove(u1)
                            unfilled.remove(u2)
                            changed = true
                            break
                        }
                    }
                }
            }

            for (u in unfilled.toList()) {
                var absorbed = false

                // Try 2: Head extension
                for (arr in clusterArrows) {
                    val head = arr.last()
                    for ((dr, dc) in cardinals.shuffled(rng)) {
                        if (head.first + dr == u.first && head.second + dc == u.second) {
                            if (isExitClear(u.first, u.second, dr, dc)) {
                                arr.add(u)
                                occupied.add(u)
                                unfilled.remove(u)
                                changed = true
                                absorbed = true
                                break
                            }
                        }
                    }
                    if (absorbed) break
                }
                if (absorbed) continue

                // Try 3: Tail extension
                for (k in clusterArrows.indices) {
                    val arr = clusterArrows[k]
                    val tail = arr.first()
                    if (kotlin.math.abs(u.first - tail.first) + kotlin.math.abs(u.second - tail.second) == 1) {
                        var blocks = false
                        for (j in (k + 1) until clusterArrows.size) {
                            val other = clusterArrows[j]
                            val oh = other.last()
                            val op = other[other.size - 2]
                            val odr = (oh.first - op.first).coerceIn(-1, 1)
                            val odc = (oh.second - op.second).coerceIn(-1, 1)
                            var cr = oh.first + odr
                            var cc = oh.second + odc
                            while (cr in 0 until boardRows && cc in 0 until boardCols) {
                                if (cr == u.first && cc == u.second) {
                                    blocks = true
                                    break
                                }
                                cr += odr
                                cc += odc
                            }
                            if (blocks) break
                        }
                        if (!blocks) {
                            arr.add(0, u)
                            occupied.add(u)
                            unfilled.remove(u)
                            changed = true
                            absorbed = true
                            break
                        }
                    }
                }
            }
        }

        return (unfilled.isEmpty()) to clusterArrows
    }

    private fun verifySolvable(level: DotGridLevel): Boolean {
        val occupied = HashSet<Pair<Int, Int>>()
        for (a in level.arrows) {
            for (d in a.dots) {
                if (!occupied.add(d)) return false
            }
        }

        val remaining = level.arrows.toMutableList()
        while (remaining.isNotEmpty()) {
            val freed = remaining.firstOrNull { arrow ->
                val head = arrow.dots.last()
                val prev = arrow.dots[arrow.dots.size - 2]
                val dr = head.first - prev.first
                val dc = head.second - prev.second
                var cr = head.first + dr
                var cc = head.second + dc
                var blocked = false
                while (cr in 0 until level.rows && cc in 0 until level.cols) {
                    if (occupied.contains(cr to cc)) {
                        blocked = true
                        break
                    }
                    cr += dr
                    cc += dc
                }
                !blocked
            } ?: return false

            for (d in freed.dots) {
                occupied.remove(d)
            }
            remaining.remove(freed)
        }
        return true
    }

    private fun fallbackDenseLevel(id: Int, spec: LevelSpec): DotGridLevel {
        val rawArrows = mutableListOf<RawDotArrow>()
        val rng = Random(id * 9973L + 42L)
        val allDots = spec.clusters.flatten().toSet()

        for (c in spec.clusters) {
            val otherDots = allDots - c.toSet()
            val minR = c.minOf { it.first }
            val maxR = c.maxOf { it.first }
            val minC = c.minOf { it.second }
            val maxC = c.maxOf { it.second }
            val midR = (minR + maxR) / 2.0
            val midC = (minC + maxC) / 2.0

            val remaining = c.toMutableSet()
            val clusterArrows = mutableListOf<List<Pair<Int, Int>>>()

            fun isExternalClear(r: Int, c: Int, dr: Int, dc: Int): Boolean {
                var cr = r + dr
                var cc = c + dc
                while (cr in 0 until spec.rows && cc in 0 until spec.cols) {
                    if (otherDots.contains(cr to cc)) return false
                    cr += dr
                    cc += dc
                }
                return true
            }

            // 1. Vertical arrows pointing UP (head at min row)
            val topDots = remaining.filter { it.first < midR }.toSet()
            val topCols = topDots.map { it.second }.distinct().shuffled(rng)
            for (col in topCols) {
                val colDots = topDots.filter { it.second == col && remaining.contains(it) }.sortedBy { it.first }
                if (colDots.size >= 2 && isExternalClear(colDots.first().first, col, -1, 0)) {
                    val len = if (colDots.size == 3 || colDots.size == 5) 3 else 2
                    val chunk = colDots.subList(0, len)
                    clusterArrows.add(chunk.reversed()) // Head at minimum row -> dr = -1, dc = 0 (UP)
                    remaining.removeAll(chunk.toSet())
                }
            }

            // 2. Vertical arrows pointing DOWN (head at max row)
            val botDots = remaining.filter { it.first > midR }.toSet()
            val botCols = botDots.map { it.second }.distinct().shuffled(rng)
            for (col in botCols) {
                val colDots = botDots.filter { it.second == col && remaining.contains(it) }.sortedBy { it.first }
                if (colDots.size >= 2 && isExternalClear(colDots.last().first, col, 1, 0)) {
                    val len = if (colDots.size == 3 || colDots.size == 5) 3 else 2
                    val chunk = colDots.subList(colDots.size - len, colDots.size)
                    clusterArrows.add(chunk) // Head at maximum row -> dr = +1, dc = 0 (DOWN)
                    remaining.removeAll(chunk.toSet())
                }
            }

            // 3. Horizontal arrows pointing LEFT (head at min col)
            val leftDots = remaining.filter { it.second <= midC }.toSet()
            val leftRows = leftDots.map { it.first }.distinct().shuffled(rng)
            for (row in leftRows) {
                val rowDots = leftDots.filter { it.first == row && remaining.contains(it) }.sortedBy { it.second }
                if (rowDots.size >= 2 && isExternalClear(row, rowDots.first().second, 0, -1)) {
                    val len = if (rowDots.size == 3 || rowDots.size == 5) 3 else 2
                    val chunk = rowDots.subList(0, len)
                    clusterArrows.add(chunk.reversed()) // Head at minimum col -> dr = 0, dc = -1 (LEFT)
                    remaining.removeAll(chunk.toSet())
                }
            }

            // 4. Horizontal arrows pointing RIGHT (head at max col)
            val rightDots = remaining.filter { it.second > midC }.toSet()
            val rightRows = rightDots.map { it.first }.distinct().shuffled(rng)
            for (row in rightRows) {
                val rowDots = rightDots.filter { it.first == row && remaining.contains(it) }.sortedBy { it.second }
                if (rowDots.size >= 2 && isExternalClear(row, rowDots.last().second, 0, 1)) {
                    val len = if (rowDots.size == 3 || rowDots.size == 5) 3 else 2
                    val chunk = rowDots.subList(rowDots.size - len, rowDots.size)
                    clusterArrows.add(chunk) // Head at maximum col -> dr = 0, dc = +1 (RIGHT)
                    remaining.removeAll(chunk.toSet())
                }
            }

            // 5. Remaining dots: slice horizontally alternating directions
            val remainingRows = remaining.map { it.first }.distinct().sorted()
            for (row in remainingRows) {
                val rowDots = remaining.filter { it.first == row }.sortedBy { it.second }
                var i = 0
                while (i < rowDots.size) {
                    val rem = rowDots.size - i
                    val len = if (rem == 3 || rem == 5) 3 else 2
                    val chunk = rowDots.subList(i, (i + len).coerceAtMost(rowDots.size))
                    if (chunk.size >= 2) {
                        val canLeft = isExternalClear(row, chunk.first().second, 0, -1)
                        val canRight = isExternalClear(row, chunk.last().second, 0, 1)
                        val arrowDots = when {
                            canLeft && !canRight -> chunk.reversed() // LEFT
                            canRight && !canLeft -> chunk            // RIGHT
                            row % 2 == 0 -> chunk                    // RIGHT
                            else -> chunk.reversed()                 // LEFT
                        }
                        clusterArrows.add(arrowDots)
                        remaining.removeAll(chunk.toSet())
                        i += len
                    } else {
                        i++
                    }
                }
            }

            // 6. Absorb any last single dots into adjacent arrows (prepended to tail orthogonally)
            for (dot in remaining.toList()) {
                val adj = clusterArrows.find { arr ->
                    val tail = arr.first()
                    kotlin.math.abs(tail.first - dot.first) + kotlin.math.abs(tail.second - dot.second) == 1
                }
                if (adj != null) {
                    val newArr = mutableListOf(dot)
                    newArr.addAll(adj)
                    val idx = clusterArrows.indexOf(adj)
                    clusterArrows[idx] = newArr
                    remaining.remove(dot)
                }
            }

            // 7. Pair any 2 remaining adjacent dots
            if (remaining.size >= 2) {
                val remList = remaining.toList()
                for (u1 in remList) {
                    if (!remaining.contains(u1)) continue
                    for ((dr, dc) in cardinals) {
                        val u2 = (u1.first + dr) to (u1.second + dc)
                        if (remaining.contains(u2)) {
                            val arrowDots = if (isExternalClear(u2.first, u2.second, dr, dc)) listOf(u1, u2) else listOf(u2, u1)
                            clusterArrows.add(arrowDots)
                            remaining.remove(u1)
                            remaining.remove(u2)
                            break
                        }
                    }
                }
            }

            for (arr in clusterArrows) {
                val col = colors[rawArrows.size % colors.size]
                rawArrows.add(RawDotArrow(arr, col))
            }
        }

        return DotGridLevel(id, spec.name, spec.rows, spec.cols, rawArrows)
    }
}
