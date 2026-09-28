package com.example.arrowescape.model

data class RawDotArrow(
    val dots: List<Pair<Int, Int>>, // (row, col) coordinates on dot grid
    val color: Int
)

data class DotGridLevel(
    val id: Int,
    val name: String,
    val rows: Int,
    val cols: Int,
    val arrows: List<RawDotArrow>
)

data class GameTheme(
    val id: String,
    val name: String,
    val icon: String,
    val bgColor: Int,
    val cardBgColor: Int,
    val dotColor: Int,
    val cost: Int,
    val description: String,
    val arrowColors: List<Int>
) {
    companion object {
        val THEMES = listOf(
            GameTheme(
                id = "cyber_neon",
                name = "Cyber Neon",
                icon = "⚡",
                bgColor = android.graphics.Color.parseColor("#0D111F"),
                cardBgColor = android.graphics.Color.parseColor("#182238"),
                dotColor = android.graphics.Color.parseColor("#2A3B5C"),
                cost = 0,
                description = "Classic dark navy grid with vivid electric neons",
                arrowColors = listOf(
                    android.graphics.Color.parseColor("#00E5FF"), // Neon Cyan
                    android.graphics.Color.parseColor("#FF2A85"), // Neon Pink
                    android.graphics.Color.parseColor("#FFE600"), // Electric Yellow
                    android.graphics.Color.parseColor("#39FF14"), // Neon Green
                    android.graphics.Color.parseColor("#FF6600"), // Neon Orange
                    android.graphics.Color.parseColor("#B026FF"), // Neon Purple
                    android.graphics.Color.parseColor("#3A7BFF"), // Electric Blue
                    android.graphics.Color.parseColor("#FF3344")  // Neon Red
                )
            ),
            GameTheme(
                id = "synthwave",
                name = "Synthwave 80s",
                icon = "🌆",
                bgColor = android.graphics.Color.parseColor("#160826"),
                cardBgColor = android.graphics.Color.parseColor("#2B104A"),
                dotColor = android.graphics.Color.parseColor("#522385"),
                cost = 100,
                description = "Retro 80s arcade dusk with hot pink and violet lasers",
                arrowColors = listOf(
                    android.graphics.Color.parseColor("#FF007F"),
                    android.graphics.Color.parseColor("#9D4EDD"),
                    android.graphics.Color.parseColor("#00F0FF"),
                    android.graphics.Color.parseColor("#FF5400"),
                    android.graphics.Color.parseColor("#E0AAFF"),
                    android.graphics.Color.parseColor("#FF0055"),
                    android.graphics.Color.parseColor("#7B2CBF"),
                    android.graphics.Color.parseColor("#FF9E00")
                )
            ),
            GameTheme(
                id = "emerald_matrix",
                name = "Emerald Matrix",
                icon = "🟢",
                bgColor = android.graphics.Color.parseColor("#04140E"),
                cardBgColor = android.graphics.Color.parseColor("#0C2B1F"),
                dotColor = android.graphics.Color.parseColor("#18533D"),
                cost = 250,
                description = "Hacker mainframe cyber grid with radiant mint & jade",
                arrowColors = listOf(
                    android.graphics.Color.parseColor("#00FF66"),
                    android.graphics.Color.parseColor("#00FFC6"),
                    android.graphics.Color.parseColor("#39FF14"),
                    android.graphics.Color.parseColor("#04E762"),
                    android.graphics.Color.parseColor("#2EC4B6"),
                    android.graphics.Color.parseColor("#80FFDB"),
                    android.graphics.Color.parseColor("#70E000"),
                    android.graphics.Color.parseColor("#52B788")
                )
            ),
            GameTheme(
                id = "solar_flare",
                name = "Solar Flare",
                icon = "🔥",
                bgColor = android.graphics.Color.parseColor("#1A0A03"),
                cardBgColor = android.graphics.Color.parseColor("#381404"),
                dotColor = android.graphics.Color.parseColor("#6E280B"),
                cost = 400,
                description = "Molten magma radiance with blazing gold and ember arrows",
                arrowColors = listOf(
                    android.graphics.Color.parseColor("#FFD700"),
                    android.graphics.Color.parseColor("#FF5400"),
                    android.graphics.Color.parseColor("#FF1E00"),
                    android.graphics.Color.parseColor("#FF9E00"),
                    android.graphics.Color.parseColor("#FFE500"),
                    android.graphics.Color.parseColor("#FF7700"),
                    android.graphics.Color.parseColor("#FF3366"),
                    android.graphics.Color.parseColor("#FFAA00")
                )
            ),
            GameTheme(
                id = "nordic_frost",
                name = "Nordic Frost",
                icon = "❄️",
                bgColor = android.graphics.Color.parseColor("#06121E"),
                cardBgColor = android.graphics.Color.parseColor("#0E2338"),
                dotColor = android.graphics.Color.parseColor("#1D4063"),
                cost = 500,
                description = "Glacial polar night with crystal cyan and iceberg blues",
                arrowColors = listOf(
                    android.graphics.Color.parseColor("#00E5FF"),
                    android.graphics.Color.parseColor("#38BDF8"),
                    android.graphics.Color.parseColor("#70E4FF"),
                    android.graphics.Color.parseColor("#A5F3FC"),
                    android.graphics.Color.parseColor("#3B82F6"),
                    android.graphics.Color.parseColor("#67E8F9"),
                    android.graphics.Color.parseColor("#0284C7"),
                    android.graphics.Color.parseColor("#E0F2FE")
                )
            ),
            GameTheme(
                id = "sakura_bloom",
                name = "Sakura Bloom",
                icon = "🌸",
                bgColor = android.graphics.Color.parseColor("#1A0914"),
                cardBgColor = android.graphics.Color.parseColor("#36132A"),
                dotColor = android.graphics.Color.parseColor("#5C2248"),
                cost = 650,
                description = "Tokyo night sakura with pastel rose, blossom pink & orchid",
                arrowColors = listOf(
                    android.graphics.Color.parseColor("#FF69B4"),
                    android.graphics.Color.parseColor("#FFB7C5"),
                    android.graphics.Color.parseColor("#FF1493"),
                    android.graphics.Color.parseColor("#E06377"),
                    android.graphics.Color.parseColor("#FF85A1"),
                    android.graphics.Color.parseColor("#DDA0DD"),
                    android.graphics.Color.parseColor("#F72585"),
                    android.graphics.Color.parseColor("#FDE2E4")
                )
            ),
            GameTheme(
                id = "obsidian_gold",
                name = "Obsidian Gold",
                icon = "👑",
                bgColor = android.graphics.Color.parseColor("#121008"),
                cardBgColor = android.graphics.Color.parseColor("#292312"),
                dotColor = android.graphics.Color.parseColor("#4A3F20"),
                cost = 800,
                description = "Ultra-luxurious royal obsidian with gleaming gold & champagne",
                arrowColors = listOf(
                    android.graphics.Color.parseColor("#FFD700"),
                    android.graphics.Color.parseColor("#FFE082"),
                    android.graphics.Color.parseColor("#FFB300"),
                    android.graphics.Color.parseColor("#F59E0B"),
                    android.graphics.Color.parseColor("#FFF176"),
                    android.graphics.Color.parseColor("#D4AF37"),
                    android.graphics.Color.parseColor("#FFA000"),
                    android.graphics.Color.parseColor("#FFF8E1")
                )
            ),
            GameTheme(
                id = "midnight_abyss",
                name = "Midnight Abyss",
                icon = "🌌",
                bgColor = android.graphics.Color.parseColor("#030308"),
                cardBgColor = android.graphics.Color.parseColor("#0D0E1C"),
                dotColor = android.graphics.Color.parseColor("#1C1E36"),
                cost = 1000,
                description = "Pure AMOLED deep cosmos with starlight cyans & nebula violets",
                arrowColors = listOf(
                    android.graphics.Color.parseColor("#818CF8"),
                    android.graphics.Color.parseColor("#C084FC"),
                    android.graphics.Color.parseColor("#38BDF8"),
                    android.graphics.Color.parseColor("#E2E8F0"),
                    android.graphics.Color.parseColor("#6366F1"),
                    android.graphics.Color.parseColor("#A78BFA"),
                    android.graphics.Color.parseColor("#60A5FA"),
                    android.graphics.Color.parseColor("#F472B6")
                )
            )
        )
    }
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val icon: String,
    val isUnlocked: (completedCount: Int, totalCoinsEarned: Int, movesTotal: Int) -> Boolean
) {
    fun getProgress(
        completedCount: Int,
        lifetimeCoins: Int,
        customThemesCount: Int,
        currentCoins: Int = 0,
        currentHints: Int = 0
    ): Pair<Int, Int> {
        return when (id) {
            // Level Progression (26)
            "first_blood" -> Pair(completedCount.coerceAtMost(1), 1)
            "level_2" -> Pair(completedCount.coerceAtMost(2), 2)
            "spark_master" -> Pair(completedCount.coerceAtMost(5), 5)
            "level_8" -> Pair(completedCount.coerceAtMost(8), 8)
            "level_10" -> Pair(completedCount.coerceAtMost(10), 10)
            "glow_master" -> Pair(completedCount.coerceAtMost(15), 15)
            "level_20" -> Pair(completedCount.coerceAtMost(20), 20)
            "level_25" -> Pair(completedCount.coerceAtMost(25), 25)
            "circuit_master" -> Pair(completedCount.coerceAtMost(30), 30)
            "level_40" -> Pair(completedCount.coerceAtMost(40), 40)
            "level_50" -> Pair(completedCount.coerceAtMost(50), 50)
            "colossus_slayer" -> Pair(completedCount.coerceAtMost(60), 60)
            "level_70" -> Pair(completedCount.coerceAtMost(70), 70)
            "level_80" -> Pair(completedCount.coerceAtMost(80), 80)
            "level_90" -> Pair(completedCount.coerceAtMost(90), 90)
            "century_club" -> Pair(completedCount.coerceAtMost(100), 100)
            "level_110" -> Pair(completedCount.coerceAtMost(110), 110)
            "level_120" -> Pair(completedCount.coerceAtMost(120), 120)
            "level_130" -> Pair(completedCount.coerceAtMost(130), 130)
            "level_140" -> Pair(completedCount.coerceAtMost(140), 140)
            "grandmaster_150" -> Pair(completedCount.coerceAtMost(150), 150)
            "level_160" -> Pair(completedCount.coerceAtMost(160), 160)
            "level_170" -> Pair(completedCount.coerceAtMost(170), 170)
            "level_180" -> Pair(completedCount.coerceAtMost(180), 180)
            "level_190" -> Pair(completedCount.coerceAtMost(190), 190)
            "cosmic_sovereign" -> Pair(completedCount.coerceAtMost(200), 200)

            // Lifetime Coins Milestones (8)
            "coin_100" -> Pair(lifetimeCoins.coerceAtMost(100), 100)
            "coin_250" -> Pair(lifetimeCoins.coerceAtMost(250), 250)
            "coin_hoarder" -> Pair(lifetimeCoins.coerceAtMost(500), 500)
            "coin_750" -> Pair(lifetimeCoins.coerceAtMost(750), 750)
            "coin_1000" -> Pair(lifetimeCoins.coerceAtMost(1000), 1000)
            "coin_2000" -> Pair(lifetimeCoins.coerceAtMost(2000), 2000)
            "coin_3500" -> Pair(lifetimeCoins.coerceAtMost(3500), 3500)
            "coin_5000" -> Pair(lifetimeCoins.coerceAtMost(5000), 5000)

            // Themes (3)
            "theme_collector" -> Pair(customThemesCount.coerceAtMost(1), 1)
            "theme_duo" -> Pair(customThemesCount.coerceAtMost(2), 2)
            "theme_master" -> Pair(customThemesCount.coerceAtMost(4), 4)

            // Inventory & Savings (3)
            "hint_stockpile" -> Pair(currentHints.coerceAtMost(5), 5)
            "hint_arsenal" -> Pair(currentHints.coerceAtMost(10), 10)
            "vault_saver" -> Pair(currentCoins.coerceAtMost(500), 500)

            else -> Pair(0, 1)
        }
    }

    companion object {
        val ALL = listOf(
            // --- Level Milestones (26) ---
            Achievement("first_blood", "First Escape", "Complete your first level", 50, "🎯") { c, _, _ -> c >= 1 },
            Achievement("level_2", "Double Down", "Complete 2 levels", 60, "👟") { c, _, _ -> c >= 2 },
            Achievement("spark_master", "Novice Navigator", "Complete 5 levels", 100, "⚡") { c, _, _ -> c >= 5 },
            Achievement("level_8", "Octo Pathfinder", "Complete 8 levels", 120, "🐙") { c, _, _ -> c >= 8 },
            Achievement("level_10", "Deca Victor", "Complete 10 levels", 150, "🔟") { c, _, _ -> c >= 10 },
            Achievement("glow_master", "Maze Runner", "Complete 15 levels", 200, "🌟") { c, _, _ -> c >= 15 },
            Achievement("level_20", "Twenty Steps", "Complete 20 levels", 250, "🧭") { c, _, _ -> c >= 20 },
            Achievement("level_25", "Silver Quarter", "Complete 25 levels", 300, "🥉") { c, _, _ -> c >= 25 },
            Achievement("circuit_master", "Cyber Specialist", "Complete 30 levels", 350, "🔮") { c, _, _ -> c >= 30 },
            Achievement("level_40", "Quadrant Voyager", "Complete 40 levels", 400, "🧩") { c, _, _ -> c >= 40 },
            Achievement("level_50", "Halfway Hero", "Complete 50 levels", 450, "🥈") { c, _, _ -> c >= 50 },
            Achievement("colossus_slayer", "Colossus Conqueror", "Complete 60 levels", 500, "👑") { c, _, _ -> c >= 60 },
            Achievement("level_70", "Neon Strategist", "Complete 70 levels", 600, "⚔️") { c, _, _ -> c >= 70 },
            Achievement("level_80", "Matrix Scholar", "Complete 80 levels", 700, "📜") { c, _, _ -> c >= 80 },
            Achievement("level_90", "Labyrinth Master", "Complete 90 levels", 800, "🌀") { c, _, _ -> c >= 90 },
            Achievement("century_club", "Century Legend", "Complete 100 levels", 1000, "🏆") { c, _, _ -> c >= 100 },
            Achievement("level_110", "Cosmic Jumper", "Complete 110 levels", 1100, "🚀") { c, _, _ -> c >= 110 },
            Achievement("level_120", "Starlight Walker", "Complete 120 levels", 1200, "💫") { c, _, _ -> c >= 120 },
            Achievement("level_130", "Nexus Maestro", "Complete 130 levels", 1300, "💠") { c, _, _ -> c >= 130 },
            Achievement("level_140", "Prism Overlord", "Complete 140 levels", 1400, "💎") { c, _, _ -> c >= 140 },
            Achievement("grandmaster_150", "Grandmaster Tactician", "Complete 150 levels", 1500, "🪐") { c, _, _ -> c >= 150 },
            Achievement("level_160", "Constellation Guru", "Complete 160 levels", 1600, "✨") { c, _, _ -> c >= 160 },
            Achievement("level_170", "Void Wanderer", "Complete 170 levels", 1700, "🛰️") { c, _, _ -> c >= 170 },
            Achievement("level_180", "Supernova Mind", "Complete 180 levels", 1800, "💥") { c, _, _ -> c >= 180 },
            Achievement("level_190", "Zenith Solver", "Complete 190 levels", 2000, "🦅") { c, _, _ -> c >= 190 },
            Achievement("cosmic_sovereign", "Cosmic Sovereign", "Complete all 200 levels", 2500, "🌌") { c, _, _ -> c >= 200 },

            // --- Lifetime Coins Milestones (8) ---
            Achievement("coin_100", "Pocket Change", "Earn 100 lifetime coins", 50, "🪙") { _, tc, _ -> tc >= 100 },
            Achievement("coin_250", "Piggy Banker", "Earn 250 lifetime coins", 80, "🐖") { _, tc, _ -> tc >= 250 },
            Achievement("coin_hoarder", "Coin Hoarder", "Earn 500 lifetime coins", 150, "💰") { _, tc, _ -> tc >= 500 },
            Achievement("coin_750", "Gold Miner", "Earn 750 lifetime coins", 200, "⛏️") { _, tc, _ -> tc >= 750 },
            Achievement("coin_1000", "Coin Tycoon", "Earn 1,000 lifetime coins", 300, "💎") { _, tc, _ -> tc >= 1000 },
            Achievement("coin_2000", "Treasury Titan", "Earn 2,000 lifetime coins", 500, "🏦") { _, tc, _ -> tc >= 2000 },
            Achievement("coin_3500", "Midas Dynasty", "Earn 3,500 lifetime coins", 800, "👑") { _, tc, _ -> tc >= 3500 },
            Achievement("coin_5000", "Gilded Sovereign", "Earn 5,000 lifetime coins", 1200, "🌟") { _, tc, _ -> tc >= 5000 },

            // --- Themes (3) ---
            Achievement("theme_collector", "Theme Connoisseur", "Unlock your first custom theme", 200, "🎨") { _, _, _ -> false },
            Achievement("theme_duo", "Palette Collector", "Unlock 2 custom themes", 300, "🎭") { _, _, _ -> false },
            Achievement("theme_master", "Wardrobe King", "Unlock all custom themes", 600, "👑") { _, _, _ -> false },

            // --- Inventory & Savings (3) ---
            Achievement("hint_stockpile", "Sharp Mind", "Hold 5 or more hints in inventory", 150, "💡") { _, _, _ -> false },
            Achievement("hint_arsenal", "Infinite Vision", "Hold 10 or more hints in inventory", 300, "🔮") { _, _, _ -> false },
            Achievement("vault_saver", "Savvy Saver", "Maintain a balance of 500+ coins", 250, "🛡️") { _, _, _ -> false }
        )
    }
}
