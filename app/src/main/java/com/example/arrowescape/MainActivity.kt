package com.example.arrowescape

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.arrowescape.ads.AdMobManager
import com.example.arrowescape.audio.SoundManager
import com.example.arrowescape.databinding.ActivityMainBinding
import com.example.arrowescape.logic.DotGridLevelRepository
import com.example.arrowescape.logic.GameManager
import com.example.arrowescape.logic.RemoteLevelManager
import com.example.arrowescape.logic.AppUpdateManager
import com.example.arrowescape.logic.AppUpdateInfo
import androidx.appcompat.app.AlertDialog
import com.example.arrowescape.model.Achievement
import com.example.arrowescape.model.DotGridLevel
import com.example.arrowescape.model.GameTheme
import com.example.arrowescape.ui.LevelsAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var soundManager: SoundManager
    private lateinit var prefs: SharedPreferences

    private var currentLevelIndex: Int = 0
    private var currentLives: Int = MAX_LIVES
    private var lifeAdsWatchedThisLevel: Int = 0

    companion object {
        const val MAX_LIVES = 3
        const val MAX_LIFE_ADS_PER_LEVEL = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("ArrowCrash", "CRASH in thread ${thread.name}: ${throwable.message}", throwable)
            try {
                val sw = java.io.StringWriter()
                throwable.printStackTrace(java.io.PrintWriter(sw))
                getSharedPreferences("arrow_escape_crash", MODE_PRIVATE).edit().putString("last_crash", sw.toString()).commit()
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Google Mobile Ads SDK (Banner & Rewarded)
        AdMobManager.init(this)
        AdMobManager.loadBannerAd(binding.flHomeBannerContainer, null, this)
        AdMobManager.loadBannerAd(binding.flGameBannerContainer, null, this)

        // Initialize Level Repository and Game Manager (Coin/Theme/Achievement/Hint data)
        DotGridLevelRepository.init(this)
        GameManager.init(this)

        // Initialize Over-The-Air (OTA) Remote Level Synchronization
        RemoteLevelManager.init(this)
        RemoteLevelManager.onLevelsUpdated = { total, newAdded ->
            updateHomeScreenUI()
            if (binding.viewLevels.visibility == View.VISIBLE) {
                updateLevelsScreenUI()
            }
            if (newAdded > 0) {
                Toast.makeText(this, "🎉 $newAdded new levels added!", Toast.LENGTH_LONG).show()
            }
        }

        // Initialize Over-The-Air (OTA) App In-App Auto-Updater
        AppUpdateManager.init(this)
        AppUpdateManager.onUpdateAvailable = { updateInfo ->
            showAppUpdateDialog(updateInfo)
        }

        prefs = getSharedPreferences("arrow_escape_prefs", MODE_PRIVATE)
        soundManager = SoundManager(this)
        binding.gameBoardView.soundManager = soundManager

        currentLevelIndex = prefs.getInt("saved_level_index", 0).coerceIn(0, DotGridLevelRepository.totalLevels - 1)

        applyCurrentTheme()
        setupListeners()
        updateHomeScreenUI()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.viewLevels.visibility == View.VISIBLE ||
                    binding.viewGame.visibility == View.VISIBLE ||
                    binding.viewShop.visibility == View.VISIBLE ||
                    binding.viewThemes.visibility == View.VISIBLE ||
                    binding.viewAchievements.visibility == View.VISIBLE ||
                    binding.viewMoreApps.visibility == View.VISIBLE) {
                    showHomeScreen()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        RemoteLevelManager.syncIfConnected(this)
    }

    private fun applyCurrentTheme() {
        val theme = GameManager.getCurrentTheme()
        binding.root.setBackgroundColor(theme.bgColor)
        binding.viewGame.setBackgroundColor(theme.bgColor)
        binding.gameBoardView.applyTheme(theme)
    }

    private fun setupListeners() {
        // --- Navigation from Home Screen ---
        binding.cardPlayHero.setOnClickListener {
            val nextUncompletedLevel = getNextUncompletedLevel()
            currentLevelIndex = nextUncompletedLevel.id - 1
            showGameScreen()
            loadLevel(nextUncompletedLevel)
        }

        binding.btnHomeLevels.setOnClickListener {
            showLevelsScreen()
        }

        binding.btnHomeShop.setOnClickListener {
            showThemesScreen()
        }

        binding.btnHomeStore.setOnClickListener {
            showShopScreen()
        }

        binding.btnHomeAchievements.setOnClickListener {
            showAchievementsScreen()
        }

        binding.btnHomeMoreApps.setOnClickListener {
            showMoreAppsScreen()
        }

        binding.btnBackFromMoreApps.setOnClickListener {
            showHomeScreen()
        }

        binding.btnVisitAppSphere.setOnClickListener {
            openUrl("https://adityakewat33-jpg.github.io/AppSphere/")
        }

        binding.btnOpenAppSphereStore.setOnClickListener {
            openUrl("https://adityakewat33-jpg.github.io/AppSphere/")
        }

        binding.btnInstallFlapMaster.setOnClickListener {
            Toast.makeText(this, "Opening FlapMaster Arcade APK download...", Toast.LENGTH_SHORT).show()
            openUrl("https://github.com/adityakewat33-jpg/AppSphere/raw/main/uploads/apks/flapmaster.apk")
        }

        binding.btnInstallStatusSaver.setOnClickListener {
            Toast.makeText(this, "Opening Status Saver APK download...", Toast.LENGTH_SHORT).show()
            openUrl("https://github.com/adityakewat33-jpg/AppSphere/raw/main/uploads/apks/statussaver.apk")
        }

        binding.btnInstallBlastGrid.setOnClickListener {
            Toast.makeText(this, "Opening BlastGrid APK download...", Toast.LENGTH_SHORT).show()
            openUrl("https://github.com/adityakewat33-jpg/AppSphere/raw/main/uploads/apks/blastgrid.apk")
        }

        binding.btnHeaderCoins.setOnClickListener {
            showShopScreen()
        }

        binding.btnHomeSound.setOnClickListener {
            soundManager.isMuted = !soundManager.isMuted
            updateSoundUI()
        }

        // --- Back Buttons from Full Pages ---
        binding.btnBackFromLevels.setOnClickListener {
            showHomeScreen()
        }

        binding.btnBackFromShop.setOnClickListener {
            showHomeScreen()
        }

        binding.btnBackFromThemes.setOnClickListener {
            showHomeScreen()
        }

        binding.btnBackFromAchievements.setOnClickListener {
            showHomeScreen()
        }

        binding.btnBackToHome.setOnClickListener {
            showHomeScreen()
        }

        binding.btnLevelsCoinPill.setOnClickListener {
            showShopScreen()
        }

        // --- Game Board Callbacks ---
        binding.gameBoardView.onMoveMade = { moves ->
            binding.tvMoves.text = moves.toString()
        }

        binding.gameBoardView.onWrongArrowTapped = {
            handleWrongArrowTouch()
        }

        binding.gameBoardView.onLevelCompleted = {
            val levelId = currentLevelIndex + 1
            val isFirstTime = GameManager.markLevelCompleted(levelId)
            showVictoryDialog(levelId, isFirstTime)
            updateHomeScreenUI()
        }

        // --- Strictly 3 In-Game Bottom Action Buttons ---
        // 1. Hint Button
        binding.btnHint.setOnClickListener {
            if (binding.gameBoardView.isGameOver) return@setOnClickListener
            if (GameManager.hints > 0) {
                if (GameManager.useHint()) {
                    binding.gameBoardView.showHint()
                    updateHintUI()
                }
            } else {
                showOutOfHintsDialog()
            }
        }

        // 2. Extra Life (+1 Life via Rewarded Video)
        binding.btnExtraLife.setOnClickListener {
            if (currentLives >= MAX_LIVES) {
                Toast.makeText(this, "Lives are already full! (3/3)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (lifeAdsWatchedThisLevel >= MAX_LIFE_ADS_PER_LEVEL) {
                Toast.makeText(this, "Ad limit reached for this level (2/2 watched)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            playRewardedAd("+1 Extra Life") {
                lifeAdsWatchedThisLevel++
                currentLives = (currentLives + 1).coerceAtMost(MAX_LIVES)
                if (binding.gameBoardView.isGameOver) {
                    binding.gameBoardView.isGameOver = false
                }
                updateLivesUI()
                val left = MAX_LIFE_ADS_PER_LEVEL - lifeAdsWatchedThisLevel
                Toast.makeText(this@MainActivity, "❤️ +1 Life added! ($left ad${if (left == 1) "" else "s"} left)", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Replay Button
        binding.btnRestart.setOnClickListener {
            soundManager.playBump()
            currentLives = MAX_LIVES
            lifeAdsWatchedThisLevel = 0
            binding.gameBoardView.isGameOver = false
            updateLivesUI()
            binding.gameBoardView.restart()
        }

        binding.btnSound.setOnClickListener {
            soundManager.isMuted = !soundManager.isMuted
            updateSoundUI()
        }

        // --- Shop Rewarded Video & Hint Buying Listeners ---
        binding.btnWatchRewardedCoins.setOnClickListener {
            playRewardedAd("Free 50 Coins") {
                GameManager.addCoins(50)
                updateCoinDisplays()
                Toast.makeText(this@MainActivity, "🎉 +50 Free Coins added!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBuyHintPack1.setOnClickListener {
            if (GameManager.buyHints(3, 100)) {
                updateCoinDisplays()
                updateHintUI()
                Toast.makeText(this, "💡 Purchased 3 Hints!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not enough coins! Watch a video or complete levels to earn coins.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBuyHintPack2.setOnClickListener {
            if (GameManager.buyHints(10, 300)) {
                updateCoinDisplays()
                updateHintUI()
                Toast.makeText(this, "✨ Purchased 10 Hints Bundle!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not enough coins! Watch a video or complete levels to earn coins.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBuyHintPack3.setOnClickListener {
            if (GameManager.buyHints(25, 700)) {
                updateCoinDisplays()
                updateHintUI()
                Toast.makeText(this, "👑 Purchased 25 Hints Master Bundle!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not enough coins! Watch a video or complete levels to earn coins.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBuyHintPack4.setOnClickListener {
            if (GameManager.buyHints(50, 1300)) {
                updateCoinDisplays()
                updateHintUI()
                Toast.makeText(this, "⚡ Purchased 50 Hints Mega Bundle!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not enough coins! Watch a video or complete levels to earn coins.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShopGoTrophies.setOnClickListener {
            showAchievementsScreen()
        }

        binding.btnShopGoThemes.setOnClickListener {
            showThemesScreen()
        }
    }

    // --- Life System Logic ---
    private fun handleWrongArrowTouch() {
        if (binding.gameBoardView.isGameOver) return

        currentLives = (currentLives - 1).coerceAtLeast(0)
        updateLivesUI()

        if (currentLives <= 0) {
            binding.gameBoardView.isGameOver = true
            showGameOverDialog()
        }
    }

    private fun updateLivesUI() {
        val hearts = when {
            currentLives >= 3 -> "❤️❤️❤️"
            currentLives == 2 -> "❤️❤️🖤"
            currentLives == 1 -> "❤️🖤🖤"
            else -> "🖤🖤🖤"
        }
        binding.tvGameLives.text = hearts

        // Extra Life button visual state according to 3-life limit and 2-ad maximum
        when {
            currentLives >= MAX_LIVES -> {
                binding.btnExtraLife.alpha = 0.5f
                binding.tvExtraLifeSubtitle.text = "Full (3/3)"
                binding.tvExtraLifeSubtitle.setTextColor(getColor(R.color.text_secondary))
            }
            lifeAdsWatchedThisLevel >= MAX_LIFE_ADS_PER_LEVEL -> {
                binding.btnExtraLife.alpha = 0.4f
                binding.tvExtraLifeSubtitle.text = "Limit (2/2)"
                binding.tvExtraLifeSubtitle.setTextColor(getColor(R.color.text_secondary))
            }
            else -> {
                val remaining = MAX_LIFE_ADS_PER_LEVEL - lifeAdsWatchedThisLevel
                binding.btnExtraLife.alpha = 1.0f
                binding.tvExtraLifeSubtitle.text = "Ad ($remaining left)"
                binding.tvExtraLifeSubtitle.setTextColor(getColor(R.color.accent_emerald))
            }
        }
    }

    private fun updateHintUI() {
        val h = GameManager.hints
        binding.tvHintButtonBadge.text = if (h > 0) "$h Left" else "Free Ad"
    }

    // --- Screen Navigation ---
    private fun showHomeScreen() {
        binding.viewGame.visibility = View.GONE
        binding.viewLevels.visibility = View.GONE
        binding.viewShop.visibility = View.GONE
        binding.viewThemes.visibility = View.GONE
        binding.viewAchievements.visibility = View.GONE
        binding.viewMoreApps.visibility = View.GONE
        binding.viewHome.visibility = View.VISIBLE
        updateHomeScreenUI()
    }

    private fun showLevelsScreen() {
        binding.viewHome.visibility = View.GONE
        binding.viewGame.visibility = View.GONE
        binding.viewShop.visibility = View.GONE
        binding.viewThemes.visibility = View.GONE
        binding.viewAchievements.visibility = View.GONE
        binding.viewMoreApps.visibility = View.GONE
        binding.viewLevels.visibility = View.VISIBLE
        updateLevelsScreenUI()
    }

    private fun showGameScreen() {
        binding.viewHome.visibility = View.GONE
        binding.viewLevels.visibility = View.GONE
        binding.viewShop.visibility = View.GONE
        binding.viewThemes.visibility = View.GONE
        binding.viewAchievements.visibility = View.GONE
        binding.viewMoreApps.visibility = View.GONE
        binding.viewGame.visibility = View.VISIBLE
        updateCoinDisplays()
        updateLivesUI()
        updateHintUI()
    }

    private fun showShopScreen() {
        binding.viewHome.visibility = View.GONE
        binding.viewLevels.visibility = View.GONE
        binding.viewGame.visibility = View.GONE
        binding.viewThemes.visibility = View.GONE
        binding.viewAchievements.visibility = View.GONE
        binding.viewMoreApps.visibility = View.GONE
        binding.viewShop.visibility = View.VISIBLE
        updateCoinDisplays()
    }

    private fun showThemesScreen() {
        binding.viewHome.visibility = View.GONE
        binding.viewLevels.visibility = View.GONE
        binding.viewGame.visibility = View.GONE
        binding.viewShop.visibility = View.GONE
        binding.viewAchievements.visibility = View.GONE
        binding.viewMoreApps.visibility = View.GONE
        binding.viewThemes.visibility = View.VISIBLE
        updateCoinDisplays()
        populateThemesScreen()
    }

    private fun showAchievementsScreen() {
        binding.viewHome.visibility = View.GONE
        binding.viewLevels.visibility = View.GONE
        binding.viewGame.visibility = View.GONE
        binding.viewShop.visibility = View.GONE
        binding.viewThemes.visibility = View.GONE
        binding.viewMoreApps.visibility = View.GONE
        binding.viewAchievements.visibility = View.VISIBLE
        updateCoinDisplays()
        populateAchievementsScreen()
    }

    private fun showMoreAppsScreen() {
        binding.viewHome.visibility = View.GONE
        binding.viewLevels.visibility = View.GONE
        binding.viewGame.visibility = View.GONE
        binding.viewShop.visibility = View.GONE
        binding.viewThemes.visibility = View.GONE
        binding.viewAchievements.visibility = View.GONE
        binding.viewMoreApps.visibility = View.VISIBLE
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Throwable) {
            Toast.makeText(this, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- UI Update Helpers ---
    private fun updateHomeScreenUI() {
        updateCoinDisplays()
        updateSoundUI()

        val nextLevel = getNextUncompletedLevel()
        binding.tvHeroPlayLabel.text = "PLAY • Level ${nextLevel.id}"

        val completedCount = GameManager.getCompletedLevelCount()
        val totalCount = DotGridLevelRepository.totalLevels
        binding.tvHomeProgressPill.text = "⭐ $completedCount / $totalCount Cleared"
        binding.tvHomeAllLevelsLabel.text = "All Levels (1 - $totalCount)"
    }

    private fun updateSoundUI() {
        val isMuted = soundManager.isMuted
        binding.btnSound.alpha = if (isMuted) 0.4f else 1.0f
        binding.ivHomeSoundIcon.alpha = if (isMuted) 0.4f else 1.0f
        binding.tvHomeSoundText.text = if (isMuted) "Muted" else "Sound"
    }

    private fun updateLevelsScreenUI() {
        updateCoinDisplays()

        val allLevels = DotGridLevelRepository.getAllLevels()
        val completedCount = GameManager.getCompletedLevelCount()
        binding.tvLevelsPageProgress.text = "$completedCount / ${allLevels.size} Cleared"

        binding.rvLevelsPage.layoutManager = GridLayoutManager(this, 5)
        binding.rvLevelsPage.adapter = LevelsAdapter(
            levels = allLevels,
            currentLevelId = currentLevelIndex + 1
        ) { selected ->
            currentLevelIndex = selected.id - 1
            showGameScreen()
            loadCurrentLevel()
        }
    }

    private fun updateCoinDisplays() {
        val countStr = GameManager.coins.toString()
        binding.tvHomeCoinCount.text = countStr
        binding.tvGameCoinCount.text = countStr
        binding.tvLevelsCoinCount.text = countStr
        binding.tvShopCoins.text = countStr
        binding.tvThemesCoins.text = countStr
        binding.tvAchievementsCoins.text = countStr

        // Shop Vault Stats
        binding.tvShopVaultCoins.text = "$countStr 🪙"
        binding.tvShopVaultHints.text = "${GameManager.hints} 💡"
        binding.tvShopVaultCleared.text = "${GameManager.getCompletedLevelCount()} / ${DotGridLevelRepository.getAllLevels().size}"
    }

    private fun getNextUncompletedLevel(): DotGridLevel {
        val all = DotGridLevelRepository.getAllLevels()
        val uncompleted = all.firstOrNull { !GameManager.isLevelCompleted(it.id) }
        val targetId = uncompleted?.id ?: 1
        return DotGridLevelRepository.getLevel(targetId)
    }

    private fun loadCurrentLevel() {
        val level = DotGridLevelRepository.getLevel(currentLevelIndex + 1)
        loadLevel(level)
    }

    private fun loadLevel(level: DotGridLevel) {
        val fullLevel = if (level.arrows.isEmpty()) {
            DotGridLevelRepository.getLevel(level.id)
        } else {
            level
        }
        currentLevelIndex = fullLevel.id - 1
        prefs.edit().putInt("saved_level_index", currentLevelIndex).apply()

        currentLives = MAX_LIVES
        lifeAdsWatchedThisLevel = 0
        binding.gameBoardView.isGameOver = false
        updateLivesUI()
        updateHintUI()

        binding.tvLevelTitle.text = "Level ${fullLevel.id}"
        binding.tvLevelSubtitle.text = fullLevel.name
        binding.tvMoves.text = "0"
        binding.gameBoardView.loadLevel(fullLevel)
    }

    // --- Game Over Dialog ---
    private fun showGameOverDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_glass_card)
            setPadding(48, 48, 48, 48)
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }

        val icon = TextView(this).apply {
            text = "💔"
            textSize = 44f
            gravity = android.view.Gravity.CENTER
        }
        val title = TextView(this).apply {
            text = "Out of Lives!"
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 4)
        }
        val remainingAds = (MAX_LIFE_ADS_PER_LEVEL - lifeAdsWatchedThisLevel).coerceAtLeast(0)
        val message = TextView(this).apply {
            text = if (remainingAds > 0) {
                "You lost all 3 lives.\nWatch an ad to revive with +1 life ($remainingAds left), or replay the level."
            } else {
                "You lost all 3 lives.\nAd limit reached for this level (2/2). Replay to try again!"
            }
            textSize = 13f
            setTextColor(getColor(R.color.text_secondary))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val btnRevive = Button(this).apply {
            if (remainingAds > 0) {
                text = "🎬 Revive with +1 Life ($remainingAds left)"
                setTextColor(Color.WHITE)
                setBackgroundResource(R.drawable.bg_btn_primary)
                isEnabled = true
                alpha = 1.0f
                setOnClickListener {
                    dialog.dismiss()
                    playRewardedAd("Revive Life") {
                        lifeAdsWatchedThisLevel++
                        currentLives = 1
                        binding.gameBoardView.isGameOver = false
                        updateLivesUI()
                        Toast.makeText(this@MainActivity, "❤️ Revived with +1 Life!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                text = "🚫 Ad Limit Reached (2/2)"
                setTextColor(getColor(R.color.text_secondary))
                setBackgroundResource(R.drawable.bg_button)
                isEnabled = false
                alpha = 0.5f
            }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120).apply {
                bottomMargin = 14
            }
        }

        val btnReplay = Button(this).apply {
            text = "🔄 Replay Level"
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.bg_button)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120).apply {
                bottomMargin = 14
            }
            setOnClickListener {
                dialog.dismiss()
                currentLives = MAX_LIVES
                lifeAdsWatchedThisLevel = 0
                binding.gameBoardView.isGameOver = false
                updateLivesUI()
                binding.gameBoardView.restart()
            }
        }

        val btnHome = Button(this).apply {
            text = "🏠 Home"
            setTextColor(getColor(R.color.text_secondary))
            setBackgroundResource(R.drawable.bg_button)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
            setOnClickListener {
                dialog.dismiss()
                showHomeScreen()
            }
        }

        layout.addView(icon)
        layout.addView(title)
        layout.addView(message)
        layout.addView(btnRevive)
        layout.addView(btnReplay)
        layout.addView(btnHome)

        dialog.setContentView(layout)
        dialog.show()
    }

    // --- Out of Hints Dialog ---
    private fun showOutOfHintsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_glass_card)
            setPadding(48, 48, 48, 48)
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }

        val tvTitle = TextView(this).apply {
            text = "💡 Need a Hint?"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
        }
        val tvDesc = TextView(this).apply {
            text = "You're out of hints! Watch a short video to get 1 Free Hint immediately, or visit the Shop."
            textSize = 13f
            setTextColor(getColor(R.color.text_secondary))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 12, 0, 24)
        }

        val btnWatchAd = Button(this).apply {
            text = "🎬 Watch Video (+1 Free Hint)"
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.bg_btn_primary)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120).apply {
                bottomMargin = 14
            }
            setOnClickListener {
                dialog.dismiss()
                playRewardedAd("Free Hint") {
                    GameManager.addHints(1)
                    updateHintUI()
                    binding.gameBoardView.showHint()
                    Toast.makeText(this@MainActivity, "💡 Free Hint Granted!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val btnShop = Button(this).apply {
            text = "🛒 Go to Shop"
            setTextColor(getColor(R.color.coin_gold))
            setBackgroundResource(R.drawable.bg_button)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120).apply {
                bottomMargin = 14
            }
            setOnClickListener {
                dialog.dismiss()
                showShopScreen()
            }
        }

        val btnCancel = Button(this).apply {
            text = "Cancel"
            setTextColor(getColor(R.color.text_secondary))
            setBackgroundResource(R.drawable.bg_button)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
            setOnClickListener { dialog.dismiss() }
        }

        layout.addView(tvTitle)
        layout.addView(tvDesc)
        layout.addView(btnWatchAd)
        layout.addView(btnShop)
        layout.addView(btnCancel)

        dialog.setContentView(layout)
        dialog.show()
    }

    // --- Rewarded Video Ad Handler (Google AdMob with graceful fallback) ---
    private fun playRewardedAd(rewardName: String, onRewardEarned: () -> Unit) {
        AdMobManager.showRewardedAd(
            activity = this,
            rewardName = rewardName,
            onRewardEarned = onRewardEarned,
            onFallbackSimulation = {
                simulateRewardedAd(rewardName, onRewardEarned)
            }
        )
    }

    // --- Simulated Rewarded Video Ad ---
    private fun simulateRewardedAd(rewardName: String, onRewardEarned: () -> Unit) {
        val adDialog = Dialog(this)
        adDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        adDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        adDialog.setCancelable(false)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_glass_card)
            setPadding(48, 48, 48, 48)
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }

        val tvTitle = TextView(this).apply {
            text = "🎬 Rewarded Video"
            textSize = 18f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val tvStatus = TextView(this).apply {
            text = "Playing sponsor video for: $rewardName\nPlease wait 2 seconds..."
            textSize = 13f
            setTextColor(getColor(R.color.text_secondary))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 14, 0, 20)
        }
        val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 24)
        }

        layout.addView(tvTitle)
        layout.addView(tvStatus)
        layout.addView(pb)
        adDialog.setContentView(layout)
        adDialog.show()

        layout.postDelayed({
            if (!isFinishing && !isDestroyed && adDialog.isShowing) {
                adDialog.dismiss()
                soundManager.playWin()
                onRewardEarned()
            }
        }, 2000)
    }

    // --- Victory Dialog ---
    private fun showVictoryDialog(levelId: Int, isFirstTime: Boolean) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_victory)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val tvStats = dialog.findViewById<TextView>(R.id.tvVictoryStats)
        val tvCoinsEarned = dialog.findViewById<TextView>(R.id.tvVictoryCoinsEarned)
        val btnNext = dialog.findViewById<Button>(R.id.btnNextLevel)
        val btnReplay = dialog.findViewById<Button>(R.id.btnReplay)

        val moves = binding.gameBoardView.moveCount
        tvStats.text = "Cleared in $moves moves!"

        val rewardAmount = 50
        tvCoinsEarned.text = "+$rewardAmount Coins!"
        if (!isFirstTime) {
            GameManager.addCoins(50)
        }

        updateCoinDisplays()

        btnNext.setOnClickListener {
            dialog.dismiss()
            val nextLevel = getNextUncompletedLevel()
            currentLevelIndex = nextLevel.id - 1
            loadCurrentLevel()
        }

        btnReplay.setOnClickListener {
            dialog.dismiss()
            currentLives = MAX_LIVES
            lifeAdsWatchedThisLevel = 0
            binding.gameBoardView.isGameOver = false
            updateLivesUI()
            binding.gameBoardView.restart()
        }

        dialog.show()
    }

    // --- Populate Full-Page Themes Screen ---
    private fun populateThemesScreen() {
        binding.layoutThemesList.removeAllViews()

        val currentTheme = GameManager.getCurrentTheme()
        binding.tvThemesActiveBadge.text = "${currentTheme.icon} ${currentTheme.name}"
        binding.tvThemesActiveIcon.text = currentTheme.icon
        binding.tvThemesActiveDesc.text = "Active Style: ${currentTheme.description}"

        for (theme in GameTheme.THEMES) {
            val isUnlocked = GameManager.isThemeUnlocked(theme.id)
            val isCurrent = theme.id == GameManager.currentThemeId

            val item = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setBackgroundResource(if (isCurrent) R.drawable.bg_card_unlocked_ach else R.drawable.bg_glass_card)
                setPadding(dp(14), dp(12), dp(14), dp(12))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = dp(10)
                }
            }

            // Left mini art swatch preview
            val swatch = FrameLayout(this).apply {
                val size = dp(56)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = dp(12)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(14).toFloat()
                    setColor(theme.bgColor)
                    setStroke(dp(2), theme.dotColor)
                }
            }

            // Inside swatch: 4 radiant preview dots in a 2x2 grid
            val swatchGrid = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            val row1 = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
            }
            val row2 = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(4)
                }
            }

            val pColors = if (theme.arrowColors.size >= 4) theme.arrowColors else listOf(theme.dotColor, theme.dotColor, theme.dotColor, theme.dotColor)
            for (i in 0 until 4) {
                val dot = View(this).apply {
                    val dotSize = dp(10)
                    layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                        marginStart = if (i % 2 == 1) dp(4) else 0
                    }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(pColors[i])
                    }
                }
                if (i < 2) row1.addView(dot) else row2.addView(dot)
            }
            swatchGrid.addView(row1)
            swatchGrid.addView(row2)
            swatch.addView(swatchGrid)

            // Middle info column
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val title = TextView(this).apply {
                text = "${theme.icon} ${theme.name}"
                textSize = 15f
                setTextColor(if (isCurrent) Color.WHITE else Color.parseColor("#E2E8F0"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            val desc = TextView(this).apply {
                text = theme.description
                textSize = 11f
                setTextColor(Color.parseColor("#94A3B8"))
                setPadding(0, dp(2), 0, dp(4))
            }

            // Palette preview chips row (showing up to 6 colors from theme.arrowColors)
            val paletteRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val chipsCount = theme.arrowColors.size.coerceAtMost(6)
            for (ci in 0 until chipsCount) {
                val chip = View(this).apply {
                    val cSize = dp(11)
                    layoutParams = LinearLayout.LayoutParams(cSize, cSize).apply {
                        marginEnd = dp(4)
                    }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(theme.arrowColors[ci])
                        setStroke(dp(1), Color.parseColor("#33FFFFFF"))
                    }
                }
                paletteRow.addView(chip)
            }

            info.addView(title)
            info.addView(desc)
            info.addView(paletteRow)

            // Right action button / badge
            val btnAction = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    marginStart = dp(8)
                }
                setPadding(dp(14), dp(8), dp(14), dp(8))
                textSize = 11f
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)

                when {
                    isCurrent -> {
                        text = "✓ EQUIPPED"
                        setTextColor(Color.parseColor("#34D399"))
                        setBackgroundResource(R.drawable.bg_badge_emerald)
                    }
                    isUnlocked -> {
                        text = "EQUIP"
                        setTextColor(Color.WHITE)
                        setBackgroundResource(R.drawable.bg_btn_primary)
                        setOnClickListener {
                            GameManager.currentThemeId = theme.id
                            applyCurrentTheme()
                            populateThemesScreen()
                            Toast.makeText(this@MainActivity, "Equipped ${theme.name} theme!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        text = "${theme.cost} 🪙"
                        setTextColor(Color.parseColor("#F59E0B"))
                        setBackgroundResource(R.drawable.bg_badge_gold)
                        setOnClickListener {
                            if (GameManager.spendCoins(theme.cost)) {
                                GameManager.unlockTheme(theme.id)
                                GameManager.currentThemeId = theme.id
                                applyCurrentTheme()
                                GameManager.checkAndUnlockAchievements()
                                updateCoinDisplays()
                                populateThemesScreen()
                                Toast.makeText(this@MainActivity, "Unlocked & Equipped ${theme.name}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "Not enough coins! Watch a video or complete levels.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            item.addView(swatch)
            item.addView(info)
            item.addView(btnAction)
            binding.layoutThemesList.addView(item)
        }
    }

    // --- Helper function for DP sizing ---
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // --- Populate Full-Page Achievements Screen ---
    private fun populateAchievementsScreen() {
        val newlyUnlocked = GameManager.checkAndUnlockAchievements()
        if (newlyUnlocked.isNotEmpty()) {
            updateCoinDisplays()
        }
        binding.layoutAchievementsList.removeAllViews()

        val completedCount = GameManager.getCompletedLevelCount()
        val lifetimeCoins = GameManager.lifetimeCoins
        val customThemesCount = GameTheme.THEMES.count { it.id != "cyber_neon" && GameManager.isThemeUnlocked(it.id) }
        val currentCoins = GameManager.coins
        val currentHints = GameManager.hints
        val totalCount = Achievement.ALL.size
        var unlockedCount = 0

        for (ach in Achievement.ALL) {
            if (GameManager.isAchievementUnlocked(ach.id)) {
                unlockedCount++
            }
        }

        // Mastery header statistics
        val percent = if (totalCount > 0) (unlockedCount * 100) / totalCount else 0
        binding.pbAchievementsMastery.progress = percent
        binding.tvAchievementsMasteryCount.text = "$unlockedCount / $totalCount Unlocked"
        binding.tvAchievementsProgressSummary.text = "$unlockedCount / $totalCount Unlocked"
        val totalPossibleBonus = Achievement.ALL.sumOf { it.rewardCoins }
        binding.tvAchievementsBonusInfo.text = "Earn up to ${totalPossibleBonus} Bonus Coins across all achievements!"

        for (ach in Achievement.ALL) {
            val isUnlocked = GameManager.isAchievementUnlocked(ach.id)
            val progressPair = ach.getProgress(completedCount, lifetimeCoins, customThemesCount, currentCoins, currentHints)

            val item = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setBackgroundResource(if (isUnlocked) R.drawable.bg_card_unlocked_ach else R.drawable.bg_glass_card)
                setPadding(dp(14), dp(12), dp(14), dp(12))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = dp(10)
                }
            }

            // Left icon circular badge
            val iconBadge = FrameLayout(this).apply {
                val size = dp(44)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = dp(12)
                }
                val bg = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    if (isUnlocked) {
                        setColor(Color.parseColor("#064E3B"))
                        setStroke(dp(1), Color.parseColor("#10B981"))
                    } else {
                        setColor(Color.parseColor("#1E293B"))
                        setStroke(dp(1), Color.parseColor("#334155"))
                    }
                }
                background = bg
            }

            val iconText = TextView(this).apply {
                text = ach.icon
                textSize = 20f
                gravity = android.view.Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            iconBadge.addView(iconText)

            // Middle info column
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val title = TextView(this).apply {
                text = ach.title
                textSize = 15f
                setTextColor(if (isUnlocked) Color.WHITE else Color.parseColor("#E2E8F0"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val desc = TextView(this).apply {
                text = ach.description
                textSize = 11f
                setTextColor(Color.parseColor("#94A3B8"))
                setPadding(0, dp(2), 0, 0)
            }

            info.addView(title)
            info.addView(desc)

            if (!isUnlocked) {
                // Live Progress bar and label
                val progressLabel = TextView(this).apply {
                    text = "Progress: ${progressPair.first} / ${progressPair.second}"
                    textSize = 10f
                    setTextColor(Color.parseColor("#38BDF8"))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, dp(4), 0, dp(1))
                }

                val progressBar = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(6)).apply {
                        topMargin = dp(2)
                    }
                    progressDrawable = getDrawable(R.drawable.progress_bar_custom)
                    max = progressPair.second.coerceAtLeast(1)
                    progress = progressPair.first
                }

                info.addView(progressLabel)
                info.addView(progressBar)
            }

            // Right status badge pill
            val statusPill = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    marginStart = dp(10)
                }
                setPadding(dp(10), dp(6), dp(10), dp(6))
                textSize = 10f
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                if (isUnlocked) {
                    setBackgroundResource(R.drawable.bg_badge_emerald)
                    setTextColor(Color.parseColor("#34D399"))
                    text = "✓ CLAIMED\n+${ach.rewardCoins} 🪙"
                } else {
                    setBackgroundResource(R.drawable.bg_badge_gold)
                    setTextColor(Color.parseColor("#F59E0B"))
                    text = "REWARD\n+${ach.rewardCoins} 🪙"
                }
            }

            item.addView(iconBadge)
            item.addView(info)
            item.addView(statusPill)
            binding.layoutAchievementsList.addView(item)
        }
    }

    private fun showAppUpdateDialog(updateInfo: AppUpdateInfo) {
        if (isFinishing || isDestroyed) return
        val dialogView = layoutInflater.inflate(R.layout.dialog_app_update, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(!updateInfo.isForceUpdate)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvUpdateDialogTitle)
        val tvVersion = dialogView.findViewById<TextView>(R.id.tvUpdateDialogVersion)
        val tvNotes = dialogView.findViewById<TextView>(R.id.tvUpdateReleaseNotes)
        val layoutProgress = dialogView.findViewById<View>(R.id.layoutUpdateProgress)
        val pbDownload = dialogView.findViewById<ProgressBar>(R.id.pbUpdateDownload)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvUpdateProgressStatus)
        val btnLater = dialogView.findViewById<Button>(R.id.btnUpdateLater)
        val btnUpdateNow = dialogView.findViewById<Button>(R.id.btnUpdateNow)

        tvVersion.text = "Arrow Jam v${updateInfo.versionName} (Build ${updateInfo.versionCode})"
        tvNotes.text = updateInfo.releaseNotes

        if (updateInfo.isForceUpdate) {
            btnLater.visibility = View.GONE
        } else {
            btnLater.setOnClickListener {
                dialog.dismiss()
            }
        }

        btnUpdateNow.setOnClickListener {
            btnUpdateNow.isEnabled = false
            btnUpdateNow.text = "DOWNLOADING..."
            btnLater.isEnabled = false
            layoutProgress.visibility = View.VISIBLE
            pbDownload.progress = 0
            tvStatus.text = "Connecting to server..."

            AppUpdateManager.startDownloadAndInstall(
                activity = this,
                updateInfo = updateInfo,
                onProgress = { percent ->
                    pbDownload.progress = percent
                    tvStatus.text = if (percent >= 100) {
                        "Download complete! Opening installer..."
                    } else {
                        "Downloading update: $percent%..."
                    }
                },
                onError = { errorMsg ->
                    btnUpdateNow.isEnabled = true
                    btnUpdateNow.text = "RETRY"
                    btnLater.isEnabled = true
                    tvStatus.text = "⚠️ $errorMsg"
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            )
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
