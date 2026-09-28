package com.example.arrowescape.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ParticleManager {
    private val particles = mutableListOf<Particle>()
    private val confetti = mutableListOf<ConfettiParticle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        val color: Int,
        var alpha: Float = 1f,
        val decay: Float = 0.04f
    )

    data class ConfettiParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var rotation: Float,
        var vRot: Float,
        var width: Float,
        var height: Float,
        val color: Int,
        var alpha: Float = 1f,
        val gravity: Float = 0.4f,
        val decay: Float = 0.008f
    )

    fun emitLaunchTrail(x: Float, y: Float, dx: Float, dy: Float, color: Int) {
        val count = 8
        for (i in 0 until count) {
            val angle = Math.atan2(-dy.toDouble(), -dx.toDouble()) + (Random.nextFloat() - 0.5f) * 0.8f
            val speed = Random.nextFloat() * 8f + 3f
            particles.add(
                Particle(
                    x = x + (Random.nextFloat() - 0.5f) * 20f,
                    y = y + (Random.nextFloat() - 0.5f) * 20f,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    size = Random.nextFloat() * 6f + 3f,
                    color = color,
                    alpha = 0.9f,
                    decay = Random.nextFloat() * 0.04f + 0.03f
                )
            )
        }
    }

    fun emitBumpSparks(x: Float, y: Float, dx: Float, dy: Float) {
        val count = 12
        for (i in 0 until count) {
            val angle = Math.atan2(-dy.toDouble(), -dx.toDouble()) + (Random.nextFloat() - 0.5f) * 1.5f
            val speed = Random.nextFloat() * 6f + 2f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    size = Random.nextFloat() * 5f + 2f,
                    color = Color.parseColor("#F43F5E"), // Red spark
                    alpha = 1f,
                    decay = Random.nextFloat() * 0.06f + 0.04f
                )
            )
        }
    }

    fun emitVictoryConfetti(viewWidth: Float, viewHeight: Float) {
        val colors = intArrayOf(
            Color.parseColor("#6366F1"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#10B981"),
            Color.parseColor("#3B82F6"),
            Color.parseColor("#A855F7")
        )
        val count = 100
        for (i in 0 until count) {
            val originX = viewWidth * (0.2f + Random.nextFloat() * 0.6f)
            val originY = viewHeight * 0.5f
            val angle = -Math.PI / 2 + (Random.nextFloat() - 0.5f) * 2.0
            val speed = Random.nextFloat() * 18f + 8f

            confetti.add(
                ConfettiParticle(
                    x = originX,
                    y = originY,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 20f,
                    width = Random.nextFloat() * 16f + 10f,
                    height = Random.nextFloat() * 10f + 6f,
                    color = colors[Random.nextInt(colors.size)],
                    alpha = 1f,
                    gravity = 0.45f,
                    decay = Random.nextFloat() * 0.008f + 0.005f
                )
            )
        }
    }

    fun updateAndDraw(canvas: Canvas) {
        // Particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx
            p.y += p.vy
            p.alpha -= p.decay

            if (p.alpha <= 0f) {
                pIter.remove()
                continue
            }

            paint.color = p.color
            paint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x, p.y, p.size, paint)
        }

        // Confetti
        val cIter = confetti.iterator()
        val rect = RectF()
        while (cIter.hasNext()) {
            val c = cIter.next()
            c.x += c.vx
            c.y += c.vy
            c.vy += c.gravity
            c.vx *= 0.98f
            c.rotation += c.vRot
            c.alpha -= c.decay

            if (c.alpha <= 0f || c.y > canvas.height + 50) {
                cIter.remove()
                continue
            }

            canvas.save()
            canvas.translate(c.x, c.y)
            canvas.rotate(c.rotation)
            paint.color = c.color
            paint.alpha = (c.alpha * 255).toInt().coerceIn(0, 255)
            rect.set(-c.width / 2f, -c.height / 2f, c.width / 2f, c.height / 2f)
            canvas.drawRect(rect, paint)
            canvas.restore()
        }
    }

    val hasActiveEffects: Boolean
        get() = particles.isNotEmpty() || confetti.isNotEmpty()

    fun clear() {
        particles.clear()
        confetti.clear()
    }
}
