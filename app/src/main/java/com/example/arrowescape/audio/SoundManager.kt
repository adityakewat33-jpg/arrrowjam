package com.example.arrowescape.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val sampleRate = 44100
    var isMuted = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Play tactile launch swoop
    fun playLaunch() {
        vibrateLight()
        if (isMuted) return
        executor.execute {
            playFrequencySweep(startFreq = 400f, endFreq = 900f, durationMs = 150)
        }
    }

    // Play obstacle collision thud
    fun playBump() {
        vibrateMedium()
        if (isMuted) return
        executor.execute {
            playFrequencySweep(startFreq = 160f, endFreq = 60f, durationMs = 120, decay = true)
        }
    }

    // Play gentle hint sound
    fun playHint() {
        vibrateLight()
        if (isMuted) return
        executor.execute {
            playTone(freq = 587.33f, durationMs = 100) // D5
            Thread.sleep(80)
            playTone(freq = 880f, durationMs = 150) // A5
        }
    }

    // Play victory fanfare
    fun playWin() {
        vibrateSuccess()
        if (isMuted) return
        executor.execute {
            val notes = listOf(392f, 523.25f, 659.25f, 783.99f, 1046.5f)
            val durations = listOf(100L, 100L, 120L, 150L, 300L)
            for (i in notes.indices) {
                playTone(notes[i], durations[i].toInt())
                Thread.sleep(durations[i] / 2)
            }
        }
    }

    private fun playTone(freq: Float, durationMs: Int) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                val env = 1f - (i.toFloat() / numSamples)
                val sample = (sin(2.0 * PI * freq * t) * Short.MAX_VALUE * 0.4f * env).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAudio(buffer)
        } catch (_: Exception) {}
    }

    private fun playFrequencySweep(startFreq: Float, endFreq: Float, durationMs: Int, decay: Boolean = true) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)
            var currentPhase = 0.0

            for (i in 0 until numSamples) {
                val frac = i.toFloat() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * frac
                val env = if (decay) exp(-frac * 3.5).toFloat() else 1f - frac * 0.2f
                currentPhase += 2.0 * PI * currentFreq / sampleRate

                val sample = (sin(currentPhase) * Short.MAX_VALUE * 0.35f * env).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAudio(buffer)
        } catch (_: Exception) {}
    }

    private fun writeAudio(buffer: ShortArray) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.setNotificationMarkerPosition(buffer.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
            override fun onPeriodicNotification(track: AudioTrack?) {}
        })
    }

    private fun vibrateLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(25)
        }
    }

    private fun vibrateMedium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(45)
        }
    }

    private fun vibrateSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 70, 80)
            val amplitudes = intArrayOf(0, 150, 0, 255)
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(150)
        }
    }

    fun release() {
        executor.shutdown()
    }
}
