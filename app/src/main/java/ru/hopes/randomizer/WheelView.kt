package ru.hopes.randomizer

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withRotation
import ru.hopes.randomizer.ColorPalette.Default
import ru.hopes.randomizer.ColorPalette.Pastel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random


class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF: RectF = RectF()
    private var indicatorPath: Path = Path()


    private val animatorUpdateListener: AnimatorUpdateListener =
        AnimatorUpdateListener { animation ->
            if (isAttachedToWindow) {
                val value: Float = animation.getAnimatedValue() as Float
                setRotation(value)
            } else {
                animation.cancel()
                isSpinning = false
            }
        }

    private var _options: MutableList<String> = mutableListOf()
    val options: List<String>
        get() = _options


    private var defaultColors: IntArray = intArrayOf(
        "#FF4136".toColorInt(),  // Red
        "#FF851B".toColorInt(),  // Orange
        "#FFDC00".toColorInt(),  // Yellow
        "#2ECC40".toColorInt(),  // Green
        "#0074D9".toColorInt(),  // Blue
        "#B10DC9".toColorInt() // Purple
    )

    private var pastelColors: IntArray = intArrayOf(
        "#FFB3BA".toColorInt(),  // Pastel Red
        "#FFDFBA".toColorInt(),  // Pastel Orange
        "#FFFFBA".toColorInt(),  // Pastel Yellow
        "#BAFFC9".toColorInt(),  // Pastel Green
        "#BAE1FF".toColorInt(),  // Pastel Blue
        "#E6BAFF".toColorInt() // Pastel Purple
    )

    private val strokeWidth: Float = DEFAULT_STROKE_WIDTH

    private var isSpinning: Boolean = false

    private var duration: Long = DEFAULT_DURATION

    private var minSpin: Int =
        DEFAULT_MIN_SPIN // MAX_SPIN должен быть больше MIN_SPIN и разница должена быть кратна 360
    private var maxSpin: Int = DEFAULT_MAX_SPIN

    private var rotation = 0f
    private var colorPalette: ColorPalette = Default

    // Переменные для режима подкрутки
    private var weights: Map<String, Float> = emptyMap()
    private var riggedWinner: String? = null
    private var isRiggedMode: Boolean = false

    fun setOptions(options: MutableList<String>) {
        this._options = options
        invalidate()
    }

    fun removeOptions(optionsToRemove: List<String>) {
        _options.removeAll(optionsToRemove)
        invalidate()
    }

    fun setColorPalette(palette: ColorPalette) {
        this.colorPalette = palette
        invalidate()
    }

    // Методы API для режима подкрутки

    /**
     * Устанавливает веса (вероятности) для элементов колеса.
     * Элементы с большим весом имеют更高的 шанс выпадения.
     *
     * @param weights Мапа весов, где ключ — имя элемента, значение — его вес (> 0)
     */
    fun setWeights(weights: Map<String, Float>) {
        this.weights = weights
    }

    /**
     * Устанавливает жёстко заданного победителя.
     * Если установлено, этот элемент будет выпадать всегда (при включённом riggedMode).
     *
     * @param winner Имя победителя или null для отмены
     */
    fun setRiggedWinner(winner: String?) {
        this.riggedWinner = winner
    }

    /**
     * Включает или выключает режим подкрутки.
     *
     * @param enabled true для включения режима подкрутки
     */
    fun setRiggedMode(enabled: Boolean) {
        this.isRiggedMode = enabled
    }

    /**
     * Проверяет, включён ли режим подкрутки.
     *
     * @return true если режим подкрутки активен
     */
    fun isRiggedModeEnabled(): Boolean = isRiggedMode

    /**
     * Получает текущие веса элементов.
     *
     * @return Мапа весов или пустая мапа, если веса не установлены
     */
    fun getWeights(): Map<String, Float> = weights

    /**
     * Получает текущего жёстко заданного победителя.
     *
     * @return Имя победителя или null, если не установлено
     */
    fun getRiggedWinner(): String? = riggedWinner

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (_options.isEmpty()) return

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.9f

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        canvas.withRotation(rotation, centerX, centerY) {
            var startAngle = 0f
            val sweepAngle = RADIUS_ROUNDED_FULL_F / _options.size

            for (i in _options.indices) {
                val colorIndex =
                    i % (if (colorPalette === Pastel) pastelColors.size else defaultColors.size)
                val color =
                    if (colorPalette === Pastel) pastelColors[colorIndex] else defaultColors[colorIndex]

                paint.setColor(color)
                paint.style = Paint.Style.FILL
                drawArc(rectF, startAngle, sweepAngle, true, paint)

                paint.setColor(Color.BLACK)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                drawArc(rectF, startAngle, sweepAngle, true, paint)

                drawTextRadially(
                    this,
                    _options[i],
                    centerX,
                    centerY,
                    radius,
                    startAngle,
                    sweepAngle,
                    color
                )

                startAngle += sweepAngle
            }

        }

        drawIndicator(canvas, centerX, centerY, radius)
    }

    fun spinWheel(resultListener: (String) -> Unit) {
        if (isSpinning) return

        val targetRotation = if (isRiggedMode) {
            calculateRiggedRotation()
        } else {
            val random = Random(System.currentTimeMillis())
            rotation + random.nextInt(maxSpin - minSpin) + minSpin
        }

        val animator = ValueAnimator.ofFloat(rotation, targetRotation).apply {
            setDuration(this@WheelView.duration)
            interpolator = TimeInterpolator { input -> // интерполятор - Динамика прокрутки
                (1 - (1 - input).pow(3))
            }
            addUpdateListener(animatorUpdateListener)
            doOnEnd {
                resultListener.invoke(getSelectedOption())
                isSpinning = false
            }
        }

        animator.start()
        isSpinning = true
    }

    /**
     * Вычисляет целевой угол поворота для режима подкрутки.
     * Приоритет: 1) жёстко заданный победитель, 2) веса элементов, 3) случайный выбор
     */
    private fun calculateRiggedRotation(): Float {
        val winner = when {
            // Приоритет 1: жёстко заданный победитель
            riggedWinner != null && riggedWinner in _options -> riggedWinner!!
            // Приоритет 2: выбор на основе весов
            weights.isNotEmpty() -> selectWeightedWinner()
            // Приоритет 3: случайный выбор из доступных
            else -> _options.random()
        }

        return calculateRotationForWinner(winner)
    }

    /**
     * Выбирает победителя на основе весов (вероятностей).
     */
    private fun selectWeightedWinner(): String {
        val weightedOptions = _options.filter { it in weights && weights[it]!! > 0f }

        // Если нет валидных весов, выбираем случайно
        if (weightedOptions.isEmpty()) return _options.random()

        val totalWeight = weightedOptions.map { weights[it]!! }.reduce { acc, f -> acc + f }
        if (totalWeight <= 0f) return _options.random()

        val random = Random(System.currentTimeMillis()).nextFloat() * totalWeight
        var currentWeight = 0f

        for (option in weightedOptions) {
            currentWeight += weights[option]!!
            if (random <= currentWeight) return option
        }

        return weightedOptions.last()
    }

    /**
     * Вычисляет угол поворота, чтобы нужный элемент оказался под индикатором.
     */
    private fun calculateRotationForWinner(winner: String): Float {
        val winnerIndex = _options.indexOf(winner)
        if (winnerIndex == -1) {
            // Если победитель не найден, выбираем случайный угол
            val random = Random(System.currentTimeMillis())
            return rotation + random.nextInt(maxSpin - minSpin) + minSpin
        }

        val segmentAngle = RADIUS_ROUNDED_FULL_F / _options.size
        // Индикатор находится справа (0 градусов), нужно повернуть колесо так,
        // чтобы сегмент победителя оказался под индикатором
        val targetSegmentAngle = winnerIndex * segmentAngle + segmentAngle / 2
        
        // Добавляем случайное смещение внутри сегмента для реалистичности
        val randomOffset = (Random(System.currentTimeMillis()).nextFloat() - 0.5f) * segmentAngle * 0.8f
        
        // Целевой угол с учётом минимального количества оборотов
        val baseRotation = rotation + minSpin
        val normalizedCurrentRotation = baseRotation % RADIUS_ROUNDED_FULL_F
        
        // Вычисляем необходимый поворот
        val rotationToWinner = (RADIUS_ROUNDED_FULL_F - targetSegmentAngle + randomOffset - normalizedCurrentRotation + RADIUS_ROUNDED_FULL_F) % RADIUS_ROUNDED_FULL_F
        
        return baseRotation + rotationToWinner
    }

    private fun drawIndicator(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        paint.setColor(Color.WHITE)
        paint.style = Paint.Style.FILL

        val indicatorSize = radius * 0.1f
        indicatorPath.reset()
        indicatorPath.moveTo(centerX + radius, centerY)
        indicatorPath.lineTo(centerX + radius + indicatorSize, centerY - indicatorSize)
        indicatorPath.lineTo(centerX + radius + indicatorSize, centerY + indicatorSize)
        indicatorPath.close()

        canvas.drawPath(indicatorPath, paint)

        paint.setColor(Color.BLACK)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        canvas.drawPath(indicatorPath, paint)
    }

    private fun drawTextRadially(
        canvas: Canvas,
        text: String,
        centerX: Float,
        centerY: Float,
        radius: Float,
        startAngle: Float,
        sweepAngle: Float,
        backgroundColor: Int
    ) {
        paint.setColor(getContrastColor(backgroundColor))
        paint.textAlign = Paint.Align.LEFT
        paint.style = Paint.Style.FILL

        val midAngle = startAngle + sweepAngle / 2

        canvas.withRotation(midAngle, centerX, centerY) {
            val startX = centerX + radius * 0.3f
            val stopX = centerX + radius * 0.9f
            val availableWidth = stopX - startX

            val textSize = getOptimalTextSize(text, availableWidth, sweepAngle, radius)
            paint.textSize = textSize

            val textWidth = paint.measureText(text)
            val textX = startX + (availableWidth - textWidth) / 2

            val fontMetrics = paint.getFontMetrics()
            val textHeight = fontMetrics.bottom - fontMetrics.top
            val textY = centerY + (textHeight / 2) - fontMetrics.bottom

            drawText(text, textX, textY, paint)

        }
    }


    private fun getOptimalTextSize(
        text: String?,
        maxWidth: Float,
        sweepAngle: Float,
        radius: Float
    ): Float {
        var low = 1f
        var high = 100f
        var optimalSize = low
        val arcLength = (2 * Math.PI * radius * (sweepAngle / RADIUS_ROUNDED_FULL)).toFloat()

        while (low <= high) {
            val mid = (low + high) / 2
            paint.textSize = mid
            val textWidth = paint.measureText(text)
            val textHeight = paint.descent() - paint.ascent()

            if (textWidth <= maxWidth * 0.9f && textHeight <= arcLength * 0.9f) {
                optimalSize = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        // Apply more aggressive scaling factor based on the number of options
        val scaleFactor = max(0.5f, 1f - (_options.size / 70f)) // Adjust this formula as needed
        return optimalSize * scaleFactor
    }

    private fun getContrastColor(color: Int): Int {
        val luminance =
            (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    override fun setRotation(rotation: Float) {
        this.rotation = rotation
        invalidate()
    }
// 560
// 6 items
// 160 угол на который повернулся
// 1 item = 60
// 160 / 60 = 2 - индекс победителя

    fun getSelectedOption(): String {
        val normalizedRotation =
            (RADIUS_ROUNDED_FULL - (rotation % RADIUS_ROUNDED_FULL)) % RADIUS_ROUNDED_FULL
        val index = (normalizedRotation / (RADIUS_ROUNDED_FULL_F / _options.size)).toInt()
        return _options[index]
    }

    companion object {
        private const val RADIUS_ROUNDED_FULL = 360
        private const val RADIUS_ROUNDED_FULL_F = 360f

        private const val DEFAULT_MIN_SPIN = 720
        private const val DEFAULT_MAX_SPIN = 1080 + DEFAULT_MIN_SPIN

        private const val DEFAULT_DURATION = 5_000L

        private val DEFAULT_STROKE_WIDTH = 1f.toDp()

    }
}



enum class ColorPalette {
    Default, Pastel
}


