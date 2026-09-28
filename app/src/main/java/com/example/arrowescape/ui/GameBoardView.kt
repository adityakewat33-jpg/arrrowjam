package com.example.arrowescape.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.OvershootInterpolator
import com.example.arrowescape.audio.SoundManager
import com.example.arrowescape.logic.DotGridRaycastEngine
import com.example.arrowescape.model.DotGridArrow
import com.example.arrowescape.model.DotGridLevel
import com.example.arrowescape.model.GridDot
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class GameBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentLevel: DotGridLevel? = null
    val activeArrows = mutableListOf<DotGridArrow>()
    private val undoStack = mutableListOf<DotGridArrow>()

    var soundManager: SoundManager? = null
    var onMoveMade: ((moves: Int) -> Unit)? = null
    var onLevelCompleted: (() -> Unit)? = null
    var onWrongArrowTapped: (() -> Unit)? = null
    var isGameOver: Boolean = false

    var moveCount: Int = 0
        private set

    val particleManager = ParticleManager()

    // Base Grid Metrics
    private var dotSpacing: Float = 0f
    private var boardLeft: Float = 0f
    private var boardTop: Float = 0f
    private var boardWidth: Float = 0f
    private var boardHeight: Float = 0f
    private var tubeWidth: Float = 16f

    // Interactive Zoom & Pan
    var zoomScale: Float = 1.0f
        private set
    private var panX: Float = 0f
    private var panY: Float = 0f

    private val minZoom = 1.0f
    private var maxZoom = 18.0f

    // Touch gesture helpers
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private var touchDownX = 0f
    private var touchDownY = 0f

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val prevScale = zoomScale
            zoomScale = (zoomScale * detector.scaleFactor).coerceIn(minZoom, maxZoom)

            // Scale centered at pinch focal point
            val focusX = detector.focusX
            val focusY = detector.focusY

            panX = focusX - (focusX - panX) * (zoomScale / prevScale)
            panY = focusY - (focusY - panY) * (zoomScale / prevScale)

            clampPan()
            invalidate()
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Adaptive toggle zoom depending on grid size
            val targetZoom = if (zoomScale > 1.8f) 1.0f else (maxZoom * 0.45f).coerceIn(3.0f, 8.0f)
            animateZoomTo(targetZoom, e.x, e.y)
            return true
        }
    })

    // Paints
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#283452")
        style = Paint.Style.FILL
    }
    private val bloomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fiberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val headFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val hintGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FBBF24")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val arrowPath = Path()
    private val headPath = Path()
    private var lastFrameTime = System.currentTimeMillis()

    init {
        postInvalidateOnAnimation()
    }

    private var activeTheme: com.example.arrowescape.model.GameTheme? = null

    fun applyTheme(theme: com.example.arrowescape.model.GameTheme) {
        activeTheme = theme
        dotPaint.color = theme.dotColor
        val palette = theme.arrowColors
        if (palette.isNotEmpty()) {
            for (arrow in activeArrows) {
                arrow.color = palette[(arrow.id - 1).mod(palette.size)]
            }
            hintGlowPaint.color = palette[0]
        }
        invalidate()
    }

    fun loadLevel(level: DotGridLevel) {
        currentLevel = level
        isGameOver = false
        activeArrows.clear()
        undoStack.clear()
        moveCount = 0
        particleManager.clear()

        // Reset zoom and pan for new level
        zoomScale = 1.0f
        panX = 0f
        panY = 0f

        recalculateMetrics()

        val palette = activeTheme?.arrowColors ?: emptyList()
        var idCounter = 1
        for (raw in level.arrows) {
            val dots = raw.dots.map { (r, c) -> GridDot(r, c) }
            val arrowColor = if (palette.isNotEmpty()) {
                palette[(idCounter - 1).mod(palette.size)]
            } else {
                raw.color
            }
            activeArrows.add(
                DotGridArrow(
                    id = idCounter++,
                    dots = dots,
                    color = arrowColor
                )
            )
        }

        onMoveMade?.invoke(moveCount)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recalculateMetrics()
    }

    private fun recalculateMetrics() {
        val level = currentLevel ?: return
        val padding = 24f
        val availW = width - padding * 2
        val availH = height - padding * 2

        if (availW <= 0 || availH <= 0) return

        val spacingX = availW / (level.cols - 1).coerceAtLeast(1)
        val spacingY = availH / (level.rows - 1).coerceAtLeast(1)
        dotSpacing = min(spacingX, spacingY).coerceAtMost(65f)

        boardWidth = (level.cols - 1) * dotSpacing
        boardHeight = (level.rows - 1) * dotSpacing
        boardLeft = (width - boardWidth) / 2f
        boardTop = (height - boardHeight) / 2f

        val maxDimension = max(level.rows, level.cols)
        maxZoom = when {
            maxDimension >= 90 -> 24.0f
            maxDimension >= 60 -> 18.0f
            maxDimension >= 35 -> 12.0f
            else -> 6.5f
        }

        tubeWidth = (dotSpacing * 0.38f).coerceIn(2.5f, 22f)
        val cornerRadius = (dotSpacing * 0.42f).coerceAtLeast(1.0f)

        bloomPaint.pathEffect = CornerPathEffect(cornerRadius)
        corePaint.pathEffect = CornerPathEffect(cornerRadius)
        fiberPaint.pathEffect = CornerPathEffect(cornerRadius)
        hintGlowPaint.pathEffect = CornerPathEffect(cornerRadius)

        clampPan()
    }

    private fun clampPan() {
        if (width <= 0 || height <= 0) return

        // Maximum pan boundaries
        val scaledW = width * zoomScale
        val scaledH = height * zoomScale

        val minPanX = width - scaledW
        val minPanY = height - scaledH

        panX = if (zoomScale <= 1.0f) 0f else panX.coerceIn(minPanX, 0f)
        panY = if (zoomScale <= 1.0f) 0f else panY.coerceIn(minPanY, 0f)
    }

    private fun animateZoomTo(targetZoom: Float, focusX: Float, focusY: Float) {
        val startZoom = zoomScale
        val startPanX = panX
        val startPanY = panY

        val targetPanX = focusX - (focusX - panX) * (targetZoom / startZoom)
        val targetPanY = focusY - (focusY - panY) * (targetZoom / startZoom)

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 260
            addUpdateListener { anim ->
                val f = anim.animatedValue as Float
                zoomScale = startZoom + (targetZoom - startZoom) * f
                panX = startPanX + (targetPanX - startPanX) * f
                panY = startPanY + (targetPanY - startPanY) * f
                clampPan()
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val level = currentLevel ?: return

        val now = System.currentTimeMillis()
        val dt = ((now - lastFrameTime) / 1000f).coerceIn(0.001f, 0.05f)
        lastFrameTime = now

        var needsNextFrame = false

        // Apply Zoom & Pan Transformations
        canvas.save()
        canvas.translate(panX, panY)
        canvas.scale(zoomScale, zoomScale)

        // 1. Draw Background Dot Matrix with Viewport Culling
        // Calculate visible world boundaries
        val visibleLeft = -panX / zoomScale
        val visibleTop = -panY / zoomScale
        val visibleRight = visibleLeft + width / zoomScale
        val visibleBottom = visibleTop + height / zoomScale

        val minCol = (((visibleLeft - boardLeft) / dotSpacing).toInt() - 1).coerceIn(0, level.cols - 1)
        val maxCol = (((visibleRight - boardLeft) / dotSpacing).toInt() + 1).coerceIn(0, level.cols - 1)
        val minRow = (((visibleTop - boardTop) / dotSpacing).toInt() - 1).coerceIn(0, level.rows - 1)
        val maxRow = (((visibleBottom - boardTop) / dotSpacing).toInt() + 1).coerceIn(0, level.rows - 1)

        val dotRadius = (dotSpacing * 0.085f).coerceAtLeast(1.0f)
        for (r in minRow..maxRow) {
            val dy = boardTop + r * dotSpacing
            for (c in minCol..maxCol) {
                val dx = boardLeft + c * dotSpacing
                canvas.drawCircle(dx, dy, dotRadius, dotPaint)
            }
        }

        // 2. Draw Neon Arrows on the Dot Grid (with viewport culling)
        for (arrow in activeArrows) {
            if (arrow.isEscaped) continue

            if (arrow.isEscaping) {
                needsNextFrame = true
                arrow.escapeProgress += arrow.escapeVelocity * dt
                arrow.escapeVelocity += 34f * dt

                val maxEscapeDist = (arrow.dots.size + max(level.rows, level.cols) + 4).toFloat()
                if (arrow.escapeProgress > maxEscapeDist) {
                    arrow.isEscaping = false
                    arrow.isEscaped = true
                    checkWinCondition()
                    continue
                }
            }

            if (arrow.isBumping) {
                needsNextFrame = true
            }

            // Quick AABB culling for stationary arrows
            if (!arrow.isEscaping) {
                var arrowMinR = Int.MAX_VALUE
                var arrowMaxR = Int.MIN_VALUE
                var arrowMinC = Int.MAX_VALUE
                var arrowMaxC = Int.MIN_VALUE
                for (d in arrow.dots) {
                    if (d.r < arrowMinR) arrowMinR = d.r
                    if (d.r > arrowMaxR) arrowMaxR = d.r
                    if (d.c < arrowMinC) arrowMinC = d.c
                    if (d.c > arrowMaxC) arrowMaxC = d.c
                }
                if (arrowMaxR < minRow - 2 || arrowMinR > maxRow + 2 ||
                    arrowMaxC < minCol - 2 || arrowMinC > maxCol + 2) {
                    continue
                }
            }

            drawDotArrow(canvas, arrow, now)
        }

        // 3. Draw victory confetti
        particleManager.updateAndDraw(canvas)
        if (particleManager.hasActiveEffects) {
            needsNextFrame = true
        }

        canvas.restore()

        if (needsNextFrame) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawDotArrow(canvas: Canvas, arrow: DotGridArrow, now: Long) {
        buildArrowPath(arrow)

        val bumpX = if (arrow.isBumping) arrow.dc * arrow.bumpOffset else 0f
        val bumpY = if (arrow.isBumping) arrow.dr * arrow.bumpOffset else 0f

        canvas.save()
        canvas.translate(bumpX, bumpY)

        if (arrow.isHinted) {
            val pulse = (sin((now - arrow.hintTime) / 180.0) * 0.5 + 0.5).toFloat()
            hintGlowPaint.strokeWidth = tubeWidth * 2.4f
            hintGlowPaint.alpha = (pulse * 180 + 75).toInt()
            canvas.drawPath(arrowPath, hintGlowPaint)
        }

        bloomPaint.color = arrow.color
        bloomPaint.alpha = 55
        bloomPaint.strokeWidth = tubeWidth * 2.2f
        canvas.drawPath(arrowPath, bloomPaint)

        corePaint.color = arrow.color
        corePaint.alpha = 255
        corePaint.strokeWidth = tubeWidth
        canvas.drawPath(arrowPath, corePaint)

        fiberPaint.alpha = 190
        fiberPaint.strokeWidth = tubeWidth * 0.28f
        canvas.drawPath(arrowPath, fiberPaint)

        val headPixel = getHeadPixel(arrow)
        drawArrowHead(canvas, headPixel, arrow)

        canvas.restore()
    }

    private fun getHeadPixel(arrow: DotGridArrow): PointF {
        val h = arrow.head
        val hx = boardLeft + (h.c + arrow.dc * arrow.escapeProgress) * dotSpacing
        val hy = boardTop + (h.r + arrow.dr * arrow.escapeProgress) * dotSpacing
        return PointF(hx, hy)
    }

    private fun drawArrowHead(canvas: Canvas, headPos: PointF, arrow: DotGridArrow) {
        canvas.save()
        canvas.translate(headPos.x, headPos.y)
        canvas.rotate(arrow.angle)

        val headSize = tubeWidth * 1.55f
        headPath.reset()
        headPath.moveTo(headSize * 0.75f, 0f)
        headPath.lineTo(-headSize * 0.55f, -headSize * 0.65f)
        headPath.lineTo(-headSize * 0.25f, 0f)
        headPath.lineTo(-headSize * 0.55f, headSize * 0.65f)
        headPath.close()

        headFillPaint.color = arrow.color
        headFillPaint.alpha = 70
        canvas.scale(1.25f, 1.25f)
        canvas.drawPath(headPath, headFillPaint)

        canvas.scale(0.8f, 0.8f)
        headFillPaint.alpha = 255
        canvas.drawPath(headPath, headFillPaint)

        val dotP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(0f, 0f, tubeWidth * 0.18f, dotP)

        canvas.restore()
    }

    private fun buildArrowPath(arrow: DotGridArrow) {
        arrowPath.reset()
        if (arrow.dots.size < 2) return

        if (!arrow.isEscaping) {
            val first = arrow.dots[0]
            arrowPath.moveTo(boardLeft + first.c * dotSpacing, boardTop + first.r * dotSpacing)
            for (i in 1 until arrow.dots.size) {
                val d = arrow.dots[i]
                arrowPath.lineTo(boardLeft + d.c * dotSpacing, boardTop + d.r * dotSpacing)
            }
            return
        }

        val vertices = mutableListOf<PointF>()
        for (dot in arrow.dots) {
            vertices.add(PointF(boardLeft + dot.c * dotSpacing, boardTop + dot.r * dotSpacing))
        }

        val originalLengthDots = (arrow.dots.size - 1).toFloat()
        val originalLengthPx = originalLengthDots * dotSpacing
        val D = arrow.escapeProgress * dotSpacing

        val headDot = arrow.head
        val headX = boardLeft + (headDot.c + arrow.dc * arrow.escapeProgress) * dotSpacing
        val headY = boardTop + (headDot.r + arrow.dr * arrow.escapeProgress) * dotSpacing
        vertices.add(PointF(headX, headY))

        val cumDists = FloatArray(vertices.size)
        cumDists[0] = 0f
        for (i in 0 until vertices.size - 1) {
            val p1 = vertices[i]
            val p2 = vertices[i + 1]
            cumDists[i + 1] = cumDists[i] + hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
        }

        val totalPolyLen = cumDists.last()
        val sStart = D.coerceAtMost(totalPolyLen)
        val sEnd = (D + originalLengthPx).coerceAtMost(totalPolyLen)

        if (sEnd <= sStart) return

        val startPoint = getPointAtDistance(vertices, cumDists, sStart)
        arrowPath.moveTo(startPoint.x, startPoint.y)

        val eps = 0.5f
        for (i in 0 until vertices.size) {
            if (cumDists[i] > sStart + eps && cumDists[i] < sEnd - eps) {
                arrowPath.lineTo(vertices[i].x, vertices[i].y)
            }
        }

        val endPoint = getPointAtDistance(vertices, cumDists, sEnd)
        arrowPath.lineTo(endPoint.x, endPoint.y)
    }

    private fun getPointAtDistance(vertices: List<PointF>, cumDists: FloatArray, dist: Float): PointF {
        if (dist <= 0f) return vertices.first()
        if (dist >= cumDists.last()) return vertices.last()

        for (i in 0 until vertices.size - 1) {
            if (dist <= cumDists[i + 1]) {
                val segLen = cumDists[i + 1] - cumDists[i]
                val frac = if (segLen > 0f) ((dist - cumDists[i]) / segLen).coerceIn(0f, 1f) else 0f
                val p1 = vertices[i]
                val p2 = vertices[i + 1]
                return PointF(p1.x + (p2.x - p1.x) * frac, p1.y + (p2.y - p1.y) * frac)
            }
        }
        return vertices.last()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Let scale and double-tap gestures handle pinch and zoom
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY

                    val distFromDown = hypot((event.x - touchDownX).toDouble(), (event.y - touchDownY).toDouble())
                    if (distFromDown > 14f) {
                        isDragging = true
                        if (zoomScale > 1.0f) {
                            panX += dx
                            panY += dy
                            clampPan()
                            invalidate()
                        }
                    }
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_UP -> {
                // If not dragging and not scaling: Trigger TAP!
                if (!isDragging && !scaleDetector.isInProgress) {
                    handleTap(event.x, event.y)
                }
                isDragging = false
            }

            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }

        return true
    }

    private fun handleTap(screenX: Float, screenY: Float) {
        if (isGameOver) return
        val level = currentLevel ?: return

        // Inverse transform screen coordinates to world coordinates
        val worldX = (screenX - panX) / zoomScale
        val worldY = (screenY - panY) / zoomScale

        val arrow = DotGridRaycastEngine.findTappedArrow(
            worldX, worldY, boardLeft, boardTop, dotSpacing, activeArrows
        ) ?: return

        activeArrows.forEach { it.isHinted = false }

        val result = DotGridRaycastEngine.checkEscape(arrow, level.rows, level.cols, activeArrows)
        if (result.canEscape) {
            arrow.isEscaping = true
            arrow.escapeProgress = 0f
            arrow.escapeVelocity = 11f
            soundManager?.playLaunch()

            undoStack.add(arrow)
            moveCount++
            onMoveMade?.invoke(moveCount)
            postInvalidateOnAnimation()
        } else {
            triggerBumpAnimation(arrow)
            soundManager?.playBump()

            val headPixel = getHeadPixel(arrow)
            val impactX = headPixel.x + arrow.dc * (dotSpacing * 0.45f)
            val impactY = headPixel.y + arrow.dr * (dotSpacing * 0.45f)
            particleManager.emitBumpSparks(impactX, impactY, arrow.dc.toFloat(), arrow.dr.toFloat())
            onWrongArrowTapped?.invoke()
            postInvalidateOnAnimation()
        }
    }

    private fun triggerBumpAnimation(arrow: DotGridArrow) {
        if (arrow.isBumping) return
        arrow.isBumping = true

        val maxOffset = dotSpacing * 0.28f
        val animator = ValueAnimator.ofFloat(0f, 1f, -0.3f, 0f).apply {
            duration = 190
            interpolator = OvershootInterpolator(2.4f)
            addUpdateListener { anim ->
                val v = anim.animatedValue as Float
                arrow.bumpOffset = v * maxOffset
                invalidate()
            }
        }
        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                arrow.isBumping = false
                arrow.bumpOffset = 0f
                invalidate()
            }
        })
        animator.start()
    }

    fun showHint() {
        val level = currentLevel ?: return
        val freeArrow = DotGridRaycastEngine.findAvailableHint(level.rows, level.cols, activeArrows)
        if (freeArrow != null) {
            activeArrows.forEach { it.isHinted = false }
            freeArrow.isHinted = true
            freeArrow.hintTime = System.currentTimeMillis()
            soundManager?.playHint()
            invalidate()
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val lastArrow = undoStack.removeAt(undoStack.size - 1)
        lastArrow.isEscaped = false
        lastArrow.isEscaping = false
        lastArrow.escapeProgress = 0f
        moveCount = (moveCount - 1).coerceAtLeast(0)
        onMoveMade?.invoke(moveCount)
        invalidate()
    }

    fun restart() {
        currentLevel?.let { loadLevel(it) }
    }

    fun resetZoom() {
        zoomScale = 1.0f
        panX = 0f
        panY = 0f
        invalidate()
    }

    private fun checkWinCondition() {
        if (DotGridRaycastEngine.isPuzzleSolved(activeArrows)) {
            particleManager.emitVictoryConfetti(width.toFloat(), height.toFloat())
            soundManager?.playWin()
            postDelayed({
                onLevelCompleted?.invoke()
            }, 600)
            invalidate()
        }
    }
}
