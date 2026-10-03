package com.example.arrowescape.logic

import android.content.Context
import android.graphics.Color
import com.example.arrowescape.model.DotGridArrow
import com.example.arrowescape.model.DotGridLevel
import com.example.arrowescape.model.RawDotArrow
import org.json.JSONArray

object DotGridLevelRepository {

    private val levelCache = java.util.concurrent.ConcurrentHashMap<Int, DotGridLevel>()
    var totalLevels: Int = 700
        private set

    fun init(context: Context) {
        // 1. Immediately populate metadata for all 700 levels (instant ~0.2ms)
        for (i in 1..totalLevels) {
            val spec = DotGridLevelGenerator.getLevelSpec(i)
            levelCache[i] = DotGridLevel(i, spec.name, spec.rows, spec.cols, emptyList())
        }
        // 2. Pre-generate only the first 5 levels in background for instant start
        Thread {
            try {
                for (i in 1..5) {
                    levelCache[i] = DotGridLevelGenerator.generateLevel(i)
                }
            } catch (e: Throwable) {
                android.util.Log.e("ArrowLevel", "Error warming up levels", e)
            }
        }.start()
    }

    fun getLevel(id: Int): DotGridLevel {
        val existing = levelCache[id]
        if (existing != null && existing.arrows.isNotEmpty()) {
            return existing
        }
        return try {
            val generated = DotGridLevelGenerator.generateLevel(id)
            levelCache[id] = generated
            // Also asynchronously warm up the next level
            if (id < totalLevels && levelCache[id + 1]?.arrows?.isEmpty() != false) {
                Thread {
                    try {
                        levelCache[id + 1] = DotGridLevelGenerator.generateLevel(id + 1)
                    } catch (_: Throwable) {}
                }.start()
            }
            generated
        } catch (e: Throwable) {
            android.util.Log.e("ArrowLevel", "Error generating level $id, using safe fallback", e)
            val spec = DotGridLevelGenerator.getLevelSpec(id)
            val fallback = DotGridLevel(
                id = id,
                name = spec.name,
                rows = spec.rows,
                cols = spec.cols,
                arrows = listOf(
                    RawDotArrow(listOf(1 to 1, 1 to 2, 1 to 3), DotGridArrow.COLOR_CYAN),
                    RawDotArrow(listOf(2 to 1, 2 to 2, 2 to 3), DotGridArrow.COLOR_YELLOW)
                )
            )
            levelCache[id] = fallback
            fallback
        }
    }

    fun registerRemoteLevels(levels: List<DotGridLevel>) {
        if (levels.isEmpty()) return
        val maxId = levels.maxOf { it.id }
        if (maxId > totalLevels) {
            totalLevels = maxId
        }
        for (level in levels) {
            levelCache[level.id] = level
        }
    }

    fun getAllLevels(): List<DotGridLevel> {
        val list = mutableListOf<DotGridLevel>()
        for (i in 1..totalLevels) {
            val existing = levelCache[i]
            if (existing != null) {
                list.add(existing)
            } else {
                val spec = DotGridLevelGenerator.getLevelSpec(i)
                list.add(DotGridLevel(i, spec.name, spec.rows, spec.cols, emptyList()))
            }
        }
        return list
    }
}

object GameManager {
    private const val PREFS_NAME = "arrow_escape_game_data"
    private const val KEY_COINS = "user_coins"
    private const val KEY_LIFETIME_COINS = "lifetime_coins"
    private const val KEY_COMPLETED_LEVELS = "completed_level_ids"
    private const val KEY_UNLOCKED_THEMES = "unlocked_themes"
    private const val KEY_CURRENT_THEME = "current_theme_id"
    private const val KEY_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements"
    private const val KEY_HINTS = "user_hints"

    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Give new users starting coins (50 coins) and starting hints (3 hints)
        if (!prefs.contains(KEY_COINS)) {
            prefs.edit().putInt(KEY_COINS, 50).putInt(KEY_LIFETIME_COINS, 50).apply()
        }
        if (!prefs.contains(KEY_HINTS)) {
            prefs.edit().putInt(KEY_HINTS, 3).apply()
        }
    }

    var hints: Int
        get() = prefs.getInt(KEY_HINTS, 3)
        set(value) = prefs.edit().putInt(KEY_HINTS, value.coerceAtLeast(0)).apply()

    fun addHints(amount: Int) {
        hints += amount
    }

    fun useHint(): Boolean {
        if (hints > 0) {
            hints--
            return true
        }
        return false
    }

    fun buyHints(packSize: Int, cost: Int): Boolean {
        if (spendCoins(cost)) {
            addHints(packSize)
            return true
        }
        return false
    }

    var coins: Int
        get() = prefs.getInt(KEY_COINS, 50)
        set(value) = prefs.edit().putInt(KEY_COINS, value).apply()

    var lifetimeCoins: Int
        get() = prefs.getInt(KEY_LIFETIME_COINS, 50)
        private set(value) = prefs.edit().putInt(KEY_LIFETIME_COINS, value).apply()

    fun addCoins(amount: Int) {
        val newCoins = coins + amount
        coins = newCoins
        lifetimeCoins = lifetimeCoins + amount
    }

    fun spendCoins(amount: Int): Boolean {
        if (coins >= amount) {
            coins -= amount
            return true
        }
        return false
    }

    fun isLevelCompleted(levelId: Int): Boolean {
        val set = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()
        return set.contains(levelId.toString())
    }

    fun isLevelUnlocked(levelId: Int): Boolean {
        if (levelId <= 1) return true
        return isLevelCompleted(levelId - 1)
    }

    fun markLevelCompleted(levelId: Int): Boolean {
        val set = (prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()).toMutableSet()
        val isFirstTime = !set.contains(levelId.toString())
        if (isFirstTime) {
            set.add(levelId.toString())
            prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, set).apply()
            // Fixed 50 coins per level completion
            addCoins(50)
        }
        return isFirstTime
    }

    fun getCompletedLevelCount(): Int {
        val set = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()
        return set.size
    }

    fun isThemeUnlocked(themeId: String): Boolean {
        if (themeId == "cyber_neon") return true
        val set = prefs.getStringSet(KEY_UNLOCKED_THEMES, emptySet()) ?: emptySet()
        return set.contains(themeId)
    }

    fun unlockTheme(themeId: String) {
        val set = (prefs.getStringSet(KEY_UNLOCKED_THEMES, emptySet()) ?: emptySet()).toMutableSet()
        set.add(themeId)
        prefs.edit().putStringSet(KEY_UNLOCKED_THEMES, set).apply()
    }

    var currentThemeId: String
        get() = prefs.getString(KEY_CURRENT_THEME, "cyber_neon") ?: "cyber_neon"
        set(value) = prefs.edit().putString(KEY_CURRENT_THEME, value).apply()

    fun getCurrentTheme(): com.example.arrowescape.model.GameTheme {
        return com.example.arrowescape.model.GameTheme.THEMES.find { it.id == currentThemeId }
            ?: com.example.arrowescape.model.GameTheme.THEMES.first()
    }

    fun isAchievementUnlocked(achId: String): Boolean {
        val set = prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet()) ?: emptySet()
        return set.contains(achId)
    }

    fun unlockAchievement(achId: String) {
        val set = (prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet()) ?: emptySet()).toMutableSet()
        set.add(achId)
        prefs.edit().putStringSet(KEY_UNLOCKED_ACHIEVEMENTS, set).apply()
    }

    fun checkAndUnlockAchievements(): List<com.example.arrowescape.model.Achievement> {
        val newlyUnlocked = mutableListOf<com.example.arrowescape.model.Achievement>()
        val completedCount = getCompletedLevelCount()
        val customThemesCount = com.example.arrowescape.model.GameTheme.THEMES.count { it.id != "cyber_neon" && isThemeUnlocked(it.id) }

        for (ach in com.example.arrowescape.model.Achievement.ALL) {
            if (!isAchievementUnlocked(ach.id)) {
                val shouldUnlock = when (ach.id) {
                    "theme_collector" -> customThemesCount >= 1
                    "theme_duo" -> customThemesCount >= 2
                    "theme_master" -> customThemesCount >= 4
                    "hint_stockpile" -> hints >= 5
                    "hint_arsenal" -> hints >= 10
                    "vault_saver" -> coins >= 500
                    else -> ach.isUnlocked(completedCount, lifetimeCoins, 0)
                }

                if (shouldUnlock) {
                    unlockAchievement(ach.id)
                    addCoins(ach.rewardCoins)
                    newlyUnlocked.add(ach)
                }
            }
        }
        return newlyUnlocked
    }
}
