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

    @Test
    fun exportAllLevelsToJson() {
        val sb = StringBuilder()
        sb.append("{\n  \"version\": 4,\n  \"levels\": [\n")
        for (i in 1..700) {
            val level = com.example.arrowescape.logic.DotGridLevelGenerator.generateLevel(i)
            sb.append("    {\n")
            sb.append("      \"id\": ${level.id},\n")
            sb.append("      \"name\": \"${level.name.replace("\"", "\\\"")}\",\n")
            sb.append("      \"rows\": ${level.rows},\n")
            sb.append("      \"cols\": ${level.cols},\n")
            sb.append("      \"arrows\": [\n")
            for ((aIdx, arrow) in level.arrows.withIndex()) {
                val hex = String.format("#%06X", 0xFFFFFF and arrow.color)
                val dotsStr = arrow.dots.joinToString(", ") { "[${it.first}, ${it.second}]" }
                sb.append("        { \"dots\": [$dotsStr], \"color\": \"$hex\" }")
                if (aIdx < level.arrows.size - 1) sb.append(",")
                sb.append("\n")
            }
            sb.append("      ]\n")
            sb.append("    }")
            if (i < 700) sb.append(",")
            sb.append("\n")
            if (i % 50 == 0) {
                println("Generated $i / 700 levels...")
            }
        }
        sb.append("  ]\n}\n")
        val file = java.io.File("levels.json")
        file.writeText(sb.toString())
        println("Exported 700 levels to ${file.absolutePath} (${file.length()} bytes)")
        assertTrue(file.exists() && file.length() > 5000)
    }
}