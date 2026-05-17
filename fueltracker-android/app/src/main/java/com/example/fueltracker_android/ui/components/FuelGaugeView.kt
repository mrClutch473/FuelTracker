package com.example.fueltracker_android.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class FuelGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val START_ANGLE = 135f
        private const val SWEEP_TOTAL = 270f
        private const val MIN_CONSUMPTION = 5f   // лучший расход — полная дуга
        private const val MAX_CONSUMPTION = 20f  // худший расход — пустая дуга
    }

    // Текущее анимированное значение дуги (0f..1f)
    private var animatedFraction = 0f
    private var consumption = 0f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 36f
        color = Color.parseColor("#1A1F2E")
        strokeCap = Paint.Cap.ROUND
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 36f
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 56f
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
        color = Color.parseColor("#33F5A623")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 80f                // ↑ было 72f — крупнее, задаёт ритм
        typeface = Typeface.DEFAULT_BOLD
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8892A4")
        textAlign = Paint.Align.CENTER
        textSize = 32f                // ↑ было 30f
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5A623")
        textAlign = Paint.Align.CENTER
        textSize = 32f                // ↑ было 26f — единица теперь читаема
    }

    private val oval = RectF()
    private val glowOval = RectF()
    private var animator: ValueAnimator? = null

    // Вызывается из Fragment с реальным значением расхода
    fun setConsumption(value: Float, animate: Boolean = true) {
        consumption = value

        // Нормализуем: чем меньше расход — тем заполненнее дуга
        val fraction = 1f - ((value - MIN_CONSUMPTION) / (MAX_CONSUMPTION - MIN_CONSUMPTION))
            .coerceIn(0f, 1f)

        if (animate) {
            animator?.cancel()
            val anim = ValueAnimator.ofFloat(0f, fraction)
            anim.duration = 1200
            anim.interpolator = DecelerateInterpolator()
            anim.addUpdateListener { valueAnimator ->
                animatedFraction = valueAnimator.animatedValue as Float
                invalidate()
            }
            anim.start()
            animator = anim
        } else {
            animatedFraction = fraction
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f
        val radius = minOf(cx, cy) - 50f
        oval.set(cx - radius, cy - radius, cx + radius, cy + radius)
        glowOval.set(cx - radius, cy - radius, cx + radius, cy + radius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f

        // ── 1. Фоновая дуга (трек)
        canvas.drawArc(oval, START_ANGLE, SWEEP_TOTAL, false, trackPaint)

        val sweepAngle = SWEEP_TOTAL * animatedFraction

        if (sweepAngle > 0f) {
            // ── 2. Свечение (размытый широкий штрих под дугой)
            canvas.drawArc(glowOval, START_ANGLE, sweepAngle, false, glowPaint)

            // ── 3. Градиентная дуга
            // Матрица поворота чтобы градиент начинался от START_ANGLE
            val gradient = SweepGradient(
                cx, cy,
                intArrayOf(
                    Color.parseColor("#F5A623"),
                    Color.parseColor("#FF6B35"),
                    Color.parseColor("#F5A623")
                ),
                floatArrayOf(0f, 0.5f, 1f)
            )
            val matrix = Matrix()
            matrix.preRotate(START_ANGLE, cx, cy)
            gradient.setLocalMatrix(matrix)
            arcPaint.setShader(gradient)

            canvas.drawArc(oval, START_ANGLE, sweepAngle, false, arcPaint)
        }

        // ── 4. Текст по центру
        val consumptionText = if (consumption > 0f)
            String.format("%.1f", consumption)
        else "—"

        // Три строки: «расход» → число → «л / 100 км»
        // Число (80f) занимает примерно cy−58..cy+20 → «расход» идёт выше, единица ниже
        canvas.drawText("расход",        cx, cy - 62f, labelPaint)
        canvas.drawText(consumptionText, cx, cy + 20f, textPaint)
        canvas.drawText("л / 100 км",   cx, cy + 64f, unitPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}