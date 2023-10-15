package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import java.lang.Integer.min
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private val animDuration = 3000L
    private var animProgress = 0F
    private var progressAnimator: ValueAnimator? = null
    private var animRotation = 0F
    private var rotationAnimator: ValueAnimator? = null

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5).toFloat()
    private var colors = emptyList<Int>()

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor())
            )
        }
    }

    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()
    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@StatsView.lineWidth
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        style = Paint.Style.FILL
        textSize = this@StatsView.textSize
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        val startDotAngle = -90F
        var startAngle = startDotAngle
        val total = data.sum()
        val involvedColors: MutableList<Int> = mutableListOf()
        data.forEachIndexed { index, datum ->
            val angle = percentage(datum, total) * 360F
            paint.color = colors.getOrElse(index) { generateRandomColor() }
                .also { involvedColors.add(it) }
            canvas.drawArc(oval, startAngle + animRotation, angle * animProgress, false, paint)
            startAngle += angle
        }

        if (involvedColors.isNotEmpty()) {
            paint.color = involvedColors[0]
            val angle = PI * 2 * (startDotAngle + animRotation) / 360
            canvas.drawPoint(
                center.x + radius * cos(angle).toFloat(),
                center.y + radius * sin(angle).toFloat(),
                paint)
        }

        canvas.drawText(
            "%.2f".format(total),
            center.x,
            center.y - textPaint.textSize * 3 / 4,
            textPaint
        )

        canvas.drawText(
            "100%",
            center.x,
            center.y + textPaint.textSize * 5 / 4,
            textPaint
        )
    }

    private fun update() {
        progressAnimator?.let {
            // Clear the previous animation which may be still in progress by the moment
            it.removeAllListeners()
            it.cancel()
        }
        rotationAnimator?.let {
            // Clear the previous animation which may be still in progress by the moment
            it.removeAllListeners()
            it.cancel()
        }

        animProgress = 0F
        animRotation = 0F

        progressAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                animProgress = anim.animatedValue as Float
                invalidate()
            }
            duration = animDuration
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }

        rotationAnimator = ValueAnimator.ofFloat(0F, 360F).apply {
            addUpdateListener { anim ->
                animRotation = anim.animatedValue as Float
                invalidate()
            }
            duration = animDuration
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun generateRandomColor() = Random.nextInt(0xff000000.toInt(), 0xffffffff.toInt())

    private fun percentage(part: Float, total: Float) = part / total
}
