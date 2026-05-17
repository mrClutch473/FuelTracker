package com.example.fueltracker_android.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import android.view.animation.DecelerateInterpolator
import kotlin.math.roundToInt

class AnimatedCounter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var animator: ValueAnimator? = null
    private var suffix: String = ""

    fun setSuffix(suffix: String) {
        this.suffix = suffix
    }

    fun animateTo(target: Float, duration: Long = 800L) {
        animator?.cancel()
        val current = text.toString().replace(suffix, "").trim().toFloatOrNull() ?: 0f
        animator = ValueAnimator.ofFloat(current, target).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val value = (it.animatedValue as Float)
                text = "${value.roundToInt()} $suffix".trim()
            }
            start()
        }
    }

    fun animateToDouble(target: Double, decimals: Int = 1, duration: Long = 800L) {
        animator?.cancel()
        val current = text.toString().replace(suffix, "").trim().toDoubleOrNull() ?: 0.0
        animator = ValueAnimator.ofFloat(current.toFloat(), target.toFloat()).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val value = (it.animatedValue as Float).toDouble()
                text = "${String.format("%.${decimals}f", value)} $suffix".trim()
            }
            start()
        }
    }
}
