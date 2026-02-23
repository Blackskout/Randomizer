package ru.hopes.randomizer

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd

class WinnerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val animatorUpdateListener: AnimatorUpdateListener =
        AnimatorUpdateListener { animation ->
            if (isAttachedToWindow) {
                val value: Float = animation.getAnimatedValue() as Float
                alpha = value
            } else {
                animation.cancel()
            }
        }

    fun cleanWithAnimation() {
        ValueAnimator.ofFloat(alpha, FULL_INVISIBLE_ALPHA).apply {
            setDuration(DEFAULT_DURATION)
            interpolator = LinearInterpolator()
            addUpdateListener(animatorUpdateListener)
            doOnEnd {
                text = null
            }
        }.start()
    }

    fun setupWinnerWithAnimation(winner: String) {
        text = winner
        ValueAnimator.ofFloat(alpha, FULL_VISIBLE_ALPHA).apply {
            setDuration(DEFAULT_DURATION)
            interpolator = LinearInterpolator()
            addUpdateListener(animatorUpdateListener)
        }.start()
    }

    companion object {

        private const val FULL_VISIBLE_ALPHA = 1f
        private const val FULL_INVISIBLE_ALPHA = 0f

        private const val DEFAULT_DURATION = 300L
    }
}