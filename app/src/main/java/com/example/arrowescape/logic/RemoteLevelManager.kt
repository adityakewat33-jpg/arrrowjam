package com.example.arrowescape.logic

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.arrowescape.model.DotGridArrow
import com.example.arrowescape.model.DotGridLevel
import com.example.arrowescape.model.RawDotArrow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * RemoteLevelManager handles Over-The-Air (OTA) remote level synchronization.
 * Whenever internet connection is available, it queries the remote levels endpoint,
 * downloads newly added levels, caches them locally for offline play,
 * and notifies the UI in real-time.
 */
object RemoteLevelManager {

    private const val TAG = "RemoteLevelManager"
    private const val CACHE_FILE_NAME = "remote_levels_cache.json"
    private const val PREFS_NAME = "remote_levels_prefs"
    private const val KEY_ENDPOINT_URL = "custom_endpoint_url"
    private const val KEY_LAST_SYNC_VERSION = "last_sync_version"

    // Default URL endpoint pointing to developer's GitHub repository
    const val DEFAULT_REMOTE_URL = "https://raw.githubusercontent.com/adityakewat33-jpg/arrrowjam/main/levels.json"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isSyncing = false

    private var isNetworkCallbackRegistered = false

    /**
     * Listener callback invoked on Main Thread when new levels are detected and synced.
     * parameters: (totalLevelsNow: Int, newLevelsAdded: Int)
     */
    var onLevelsUpdated: ((totalLevels: Int, newAdded: Int) -> Unit)? = null

    /**
     * Initialize manager:
     * 1. Loads any previously cached remote levels immediately into repository (instant, offline-ready).
     * 2. Checks internet and triggers background sync.
     * 3. Registers real-time NetworkCallback to auto-sync the moment user turns on internet.
     */
    fun init(context: Context) {
        val appContext = context.applicationContext
        loadCachedLevels(appContext)
        syncIfConnected(appContext)
        registerNetworkCallback(appContext)
    }

    private fun registerNetworkCallback(context: Context) {
        if (isNetworkCallbackRegistered) return
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = android.net.NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    Log.d(TAG, "Network available: triggering auto level sync...")
                    syncIfConnected(context)
                }
            })
            isNetworkCallbackRegistered = true
        } catch (e: Throwable) {
            Log.w(TAG, "Unable to register network callback: ${e.message}")
        }
    }

    /**
     * Returns true if device is connected to active internet (Wi-Fi, Cellular, or Ethernet).
     */
    fun isInternetAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } catch (e: Throwable) {
            Log.w(TAG, "Error checking network capability: ${e.message}")
            false
        }
    }

    /**
     * Trigger background sync if device has internet connectivity.
     */
    fun syncIfConnected(context: Context, force: Boolean = false) {
        if (isSyncing) return
        if (!isInternetAvailable(context) && !force) {
            Log.d(TAG, "No active internet connection. Skipping remote level sync.")
            return
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val endpointUrl = prefs.getString(KEY_ENDPOINT_URL, DEFAULT_REMOTE_URL) ?: DEFAULT_REMOTE_URL

        isSyncing = true
        Thread {
            try {
                fetchAndApplyRemoteLevels(context, endpointUrl)
            } catch (e: Throwable) {
                Log.w(TAG, "Sync failed: ${e.message}")
            } finally {
                isSyncing = false
            }
        }.start()
    }

    /**
     * Load cached levels from local storage so all remote levels stay 100% playable offline.
     */
    private fun loadCachedLevels(context: Context) {
        try {
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            if (!cacheFile.exists()) return

            val jsonString = cacheFile.readText()
            val parsedLevels = parseLevelsJson(jsonString)
            if (parsedLevels.isNotEmpty()) {
                DotGridLevelRepository.registerRemoteLevels(parsedLevels)
                Log.d(TAG, "Loaded ${parsedLevels.size} remote levels from local cache. Total: ${DotGridLevelRepository.totalLevels}")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error loading cached remote levels", e)
        }
    }

    /**
     * Performs HTTP GET request to fetch remote levels JSON, validates, caches, and registers.
     */
    fun fetchAndApplyRemoteLevels(context: Context, endpointUrl: String): Boolean {
        var connection: HttpURLConnection? = null
        try {
            val fullUrl = if (endpointUrl.contains("?")) "$endpointUrl&_cb=${System.currentTimeMillis()}" else "$endpointUrl?_cb=${System.currentTimeMillis()}"
            Log.d(TAG, "Fetching remote levels from: $fullUrl")
            val url = URL(fullUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "ArrowJam-Android")
                setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                setRequestProperty("Pragma", "no-cache")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "HTTP error $responseCode from $endpointUrl")
                return false
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()

            val rawJson = sb.toString()
            return processAndStoreRemoteJson(context, rawJson)
        } catch (e: Throwable) {
            Log.w(TAG, "Network sync error: ${e.message}")
            return false
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Ingests JSON string, registers levels, and notifies UI on main thread.
     */
    fun processAndStoreRemoteJson(context: Context, rawJson: String): Boolean {
        val parsedLevels = parseLevelsJson(rawJson)
        if (parsedLevels.isEmpty()) {
            Log.d(TAG, "No valid levels found in JSON response.")
            return false
        }

        // Count how many are truly new
        val previousTotal = DotGridLevelRepository.totalLevels
        val newLevelsCount = parsedLevels.count { it.id > previousTotal }

        // Cache file to disk for offline access
        try {
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            cacheFile.writeText(rawJson)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed writing levels cache to disk", e)
        }

        // Register in Level Repository
        DotGridLevelRepository.registerRemoteLevels(parsedLevels)
        val newTotal = DotGridLevelRepository.totalLevels

        Log.i(TAG, "Successfully synced ${parsedLevels.size} levels. New total: $newTotal (New added: $newLevelsCount)")

        mainHandler.post {
            onLevelsUpdated?.invoke(newTotal, newLevelsCount)
        }
        return true
    }

    /**
     * Parses the JSON array/object structure into DotGridLevel models.
     * Supports both format:
     * {
     *   "version": 1,
     *   "levels": [
     *     {
     *       "id": 201,
     *       "name": "Level 201",
     *       "rows": 8,
     *       "cols": 8,
     *       "arrows": [
     *         { "dots": [[1,1], [1,2], [1,3]], "color": "#00E5FF" }
     *       ]
     *     }
     *   ]
     * }
     * Or directly a JSON Array of level objects.
     */
    fun parseLevelsJson(jsonString: String): List<DotGridLevel> {
        val result = mutableListOf<DotGridLevel>()
        try {
            val trimmed = jsonString.trim()
            val jsonArray: JSONArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val rootObj = JSONObject(trimmed)
                rootObj.optJSONArray("levels") ?: JSONArray()
            }

            for (i in 0 until jsonArray.length()) {
                val levelObj = jsonArray.optJSONObject(i) ?: continue
                val id = levelObj.optInt("id", -1)
                if (id <= 0) continue

                val name = levelObj.optString("name", "Level $id")
                val rows = levelObj.optInt("rows", 7).coerceAtLeast(3)
                val cols = levelObj.optInt("cols", 7).coerceAtLeast(3)

                val rawArrows = mutableListOf<RawDotArrow>()
                val arrowsArr = levelObj.optJSONArray("arrows")
                if (arrowsArr != null && arrowsArr.length() > 0) {
                    for (a in 0 until arrowsArr.length()) {
                        val arrowObj = arrowsArr.optJSONObject(a) ?: continue
                        val dotsArr = arrowObj.optJSONArray("dots") ?: continue
                        val dots = mutableListOf<Pair<Int, Int>>()
                        for (d in 0 until dotsArr.length()) {
                            val coordArr = dotsArr.optJSONArray(d)
                            if (coordArr != null && coordArr.length() >= 2) {
                                dots.add(coordArr.getInt(0) to coordArr.getInt(1))
                            }
                        }
                        if (dots.size >= 2) {
                            val colorHex = arrowObj.optString("color", "")
                            val color = try {
                                if (colorHex.isNotEmpty()) Color.parseColor(colorHex) else DotGridArrow.COLOR_CYAN
                            } catch (_: Throwable) {
                                DotGridArrow.COLOR_CYAN
                            }
                            rawArrows.add(RawDotArrow(dots, color))
                        }
                    }
                }

                result.add(DotGridLevel(id, name, rows, cols, rawArrows))
            }
        } catch (e: Throwable) {
            Log.e(TAG, "JSON parsing error: ${e.message}", e)
        }
        return result
    }

    /**
     * Allows setting a custom endpoint URL (useful for testing or pointing to developer's server).
     */
    fun setEndpointUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ENDPOINT_URL, url).apply()
        syncIfConnected(context, force = true)
    }

    fun getEndpointUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ENDPOINT_URL, DEFAULT_REMOTE_URL) ?: DEFAULT_REMOTE_URL
    }
}
