package com.example.arrowescape.logic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val isForceUpdate: Boolean = false
)

/**
 * AppUpdateManager handles automatic Over-The-Air (OTA) APK updates.
 * Whenever internet connection is turned on or the app opens:
 * 1. Checks remote app_version.json on GitHub / AppSphere
 * 2. Compares versionCode with installed app
 * 3. Notifies UI when an update is available
 * 4. Downloads and triggers Android Package Installer via FileProvider
 */
object AppUpdateManager {

    private const val TAG = "AppUpdateManager"
    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_LAST_CHECK_TIME = "last_check_timestamp"

    // Remote version metadata URL on GitHub
    const val DEFAULT_VERSION_URL =
        "https://raw.githubusercontent.com/adityakewat33-jpg/arrrowjam/main/app_version.json"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isChecking = false
    private var isDownloading = false
    private var isNetworkCallbackRegistered = false

    var onUpdateAvailable: ((updateInfo: AppUpdateInfo) -> Unit)? = null

    var latestUpdateInfo: AppUpdateInfo? = null
        private set

    fun init(context: Context) {
        val appContext = context.applicationContext
        checkForUpdateIfConnected(appContext)
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
                    Log.d(TAG, "Network connected: auto-checking for app update...")
                    checkForUpdateIfConnected(context)
                }
            })
            isNetworkCallbackRegistered = true
        } catch (e: Throwable) {
            Log.w(TAG, "Unable to register network callback: ${e.message}")
        }
    }

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
            false
        }
    }

    fun getCurrentVersionCode(context: Context): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            }
        } catch (e: Throwable) {
            1
        }
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Throwable) {
            "1.0"
        }
    }

    fun checkForUpdateIfConnected(context: Context, force: Boolean = false) {
        if (!isInternetAvailable(context)) return
        if (isChecking) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
        val now = System.currentTimeMillis()

        // Rate limit automatic background checks to once every 15 minutes unless forced
        if (!force && (now - lastCheck < 15 * 60 * 1000L)) {
            return
        }

        isChecking = true
        Thread {
            try {
                val url = URL(DEFAULT_VERSION_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.requestMethod = "GET"
                conn.useCaches = false

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val jsonStr = reader.use { it.readText() }
                    conn.disconnect()

                    val json = JSONObject(jsonStr)
                    val remoteVersionCode = json.getInt("versionCode")
                    val remoteVersionName = json.optString("versionName", "1.0")
                    val apkUrl = json.getString("apkUrl")
                    val notes = json.optString("releaseNotes", "New update with improvements and bug fixes.")
                    val forceUpdate = json.optBoolean("forceUpdate", false)

                    val currentCode = getCurrentVersionCode(context)
                    Log.d(TAG, "Update check: installed=$currentCode, remote=$remoteVersionCode")

                    prefs.edit().putLong(KEY_LAST_CHECK_TIME, now).apply()

                    if (remoteVersionCode > currentCode) {
                        val info = AppUpdateInfo(remoteVersionCode, remoteVersionName, apkUrl, notes, forceUpdate)
                        latestUpdateInfo = info
                        mainHandler.post {
                            onUpdateAvailable?.invoke(info)
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to check for app update: ${e.message}")
            } finally {
                isChecking = false
            }
        }.start()
    }

    fun startDownloadAndInstall(
        activity: Activity,
        updateInfo: AppUpdateInfo,
        onProgress: (percent: Int) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (isDownloading) return
        isDownloading = true

        Thread {
            var input: java.io.InputStream? = null
            var output: FileOutputStream? = null
            var conn: HttpURLConnection? = null
            try {
                val url = URL(updateInfo.apkUrl)
                conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = true
                conn.connect()

                if (conn.responseCode !in 200..299) {
                    throw IllegalStateException("Server returned HTTP ${conn.responseCode}")
                }

                val fileLength = conn.contentLength
                input = conn.inputStream

                val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: activity.cacheDir
                val apkFile = File(storageDir, "arrowjam_latest.apk")
                if (apkFile.exists()) apkFile.delete()

                output = FileOutputStream(apkFile)

                val data = ByteArray(8192)
                var total: Long = 0
                var count: Int
                var lastReportedPercent = -1

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)

                    if (fileLength > 0) {
                        val percent = ((total * 100) / fileLength).toInt().coerceIn(0, 100)
                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent
                            mainHandler.post { onProgress(percent) }
                        }
                    }
                }

                output.flush()

                if (!apkFile.exists() || apkFile.length() < 100_000) {
                    throw IllegalStateException("Downloaded APK file is incomplete or corrupt")
                }

                Log.d(TAG, "APK download complete: ${apkFile.absolutePath} (${apkFile.length()} bytes)")
                mainHandler.post {
                    onProgress(100)
                    installApk(activity, apkFile)
                }

            } catch (e: Throwable) {
                Log.e(TAG, "Error downloading APK update", e)
                mainHandler.post {
                    onError("Download failed: ${e.message}")
                }
            } finally {
                isDownloading = false
                try { output?.close() } catch (_: Throwable) {}
                try { input?.close() } catch (_: Throwable) {}
                conn?.disconnect()
            }
        }.start()
    }

    fun installApk(activity: Activity, apkFile: File) {
        try {
            // Android 8.0+ Unknown App Sources Permission Check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${activity.packageName}")
                    }
                    activity.startActivity(settingsIntent)
                    android.widget.Toast.makeText(
                        activity,
                        "Please allow 'Install unknown apps' for Arrow Jam to update",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(installIntent)

        } catch (e: Throwable) {
            Log.e(TAG, "Error launching package installer", e)
            android.widget.Toast.makeText(
                activity,
                "Unable to open installer: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}
