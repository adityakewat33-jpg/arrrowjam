package com.example.arrowescape

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun test5LevelRamp() {
        var prevAvg = 0.0
        for (tier in 0..11) {
            val startLvl = tier * 5 + 1
            val endLvl = startLvl + 4
            val arrowsList = mutableListOf<Int>()
            for (lvl in startLvl..endLvl) {
                val l = com.example.arrowescape.logic.DotGridLevelGenerator.generateLevel(lvl)
                arrowsList.add(l.arrows.size)
            }
            val avg = arrowsList.average()
            println("TIER $tier (Lv $startLvl-$endLvl): min=${arrowsList.minOrNull()}, max=${arrowsList.maxOrNull()}, avg=$avg -> $arrowsList")
            assertTrue("Tier $tier average ($avg) should exceed previous tier ($prevAvg)", avg > prevAvg)
            prevAvg = avg
        }
    }
}