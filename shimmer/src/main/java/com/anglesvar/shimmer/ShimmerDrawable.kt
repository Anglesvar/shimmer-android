package com.anglesvar.shimmer

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * A Drawable that displays a shimmer effect animation.
 */
class ShimmerDrawable : Drawable() {

    val paint = Paint().apply {
        isAntiAlias = true
    }

    private val drawRect = RectF()
    private val shaderMatrix = Matrix()

    private var valueAnimator: ValueAnimator? = null
    var shimmer: Shimmer? = null
        private set

    private val updateListener = ValueAnimator.AnimatorUpdateListener {
        invalidateSelf()
    }

    init {
        setShimmer(Shimmer.AlphaHighlightBuilder().build())
    }

    /**
     * Sets the shimmer configuration.
     */
    fun setShimmer(shimmer: Shimmer?) {
        this.shimmer = shimmer
        shimmer?.let {
            paint.xfermode = PorterDuffXfermode(
                if (it.alphaShimmer) PorterDuff.Mode.DST_IN else PorterDuff.Mode.SRC_IN
            )
        }
        updateShader()
        updateValueAnimator()
        invalidateSelf()
    }

    /**
     * Starts the shimmer animation.
     */
    fun startShimmer() {
        if (valueAnimator?.isStarted == true) return

        valueAnimator?.let { animator ->
            animator.cancel()
            animator.removeAllUpdateListeners()
            animator.addUpdateListener(updateListener)

            val shimmer = this.shimmer ?: return
            if (shimmer.startDelay > 0) {
                animator.startDelay = shimmer.startDelay
            }
            animator.start()
        }
    }

    /**
     * Stops the shimmer animation.
     */
    fun stopShimmer() {
        valueAnimator?.let { animator ->
            animator.cancel()
            animator.removeAllUpdateListeners()
        }
    }

    /**
     * Returns true if the shimmer animation is running.
     */
    fun isShimmerRunning(): Boolean = valueAnimator?.isStarted == true

    /**
     * Conditionally starts the shimmer animation if autoStart is enabled.
     */
    fun maybeStartShimmer() {
        val shimmer = this.shimmer ?: return
        if (shimmer.autoStart && !isShimmerRunning()) {
            startShimmer()
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val width = bounds.width()
        val height = bounds.height()
        shimmer?.updateBounds(width, height)
        updateShader()
        maybeStartShimmer()
    }

    override fun draw(canvas: Canvas) {
        val shimmer = this.shimmer ?: return
        val shader = paint.shader ?: return

        val tiltTan = tan(Math.toRadians(shimmer.tilt.toDouble())).toFloat()
        val translateHeight = bounds.height() + tiltTan * bounds.width()
        val translateWidth = bounds.width() + tiltTan * bounds.height()

        val animator = valueAnimator ?: return
        val animatedValue = animator.animatedValue as? Float ?: 0f
        val shimmerTranslate =
            (shimmer.repeatDelay + shimmer.animationDuration) / shimmer.animationDuration.toFloat()

        val dx: Float
        val dy: Float

        when (shimmer.direction) {
            Shimmer.Direction.LEFT_TO_RIGHT -> {
                dx = offset(-translateWidth, translateWidth, animatedValue)
                dy = 0f
            }

            Shimmer.Direction.RIGHT_TO_LEFT -> {
                dx = offset(translateWidth, -translateWidth, animatedValue)
                dy = 0f
            }

            Shimmer.Direction.TOP_TO_BOTTOM -> {
                dx = 0f
                dy = offset(-translateHeight, translateHeight, animatedValue)
            }

            Shimmer.Direction.BOTTOM_TO_TOP -> {
                dx = 0f
                dy = offset(translateHeight, -translateHeight, animatedValue)
            }
        }

        shaderMatrix.reset()
        shaderMatrix.setRotate(shimmer.tilt, bounds.width() / 2f, bounds.height() / 2f)
        shaderMatrix.postTranslate(dx, dy)
        shader.setLocalMatrix(shaderMatrix)

        canvas.drawRect(bounds, paint)
    }

    override fun setAlpha(alpha: Int) {
        // No-op, modify the Shimmer object instead
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // No-op, modify the Shimmer object instead
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

    private fun updateValueAnimator() {
        val shimmer = this.shimmer ?: return

        val animatedValue = valueAnimator?.animatedValue as? Float
        valueAnimator?.cancel()
        valueAnimator?.removeAllUpdateListeners()

        val shimmerDuration = shimmer.repeatDelay + shimmer.animationDuration
        valueAnimator = ValueAnimator.ofFloat(
            0f,
            1f + shimmer.repeatDelay / shimmer.animationDuration.toFloat()
        ).apply {
            interpolator = LinearInterpolator()
            repeatCount = shimmer.repeatCount
            repeatMode = shimmer.repeatMode
            duration = shimmerDuration
            addUpdateListener(updateListener)

            animatedValue?.let {
                setCurrentPlayTime((it * shimmerDuration).toLong())
            }
        }
    }

    private fun updateShader() {
        val shimmer = this.shimmer ?: return
        val bounds = shimmer.bounds

        val width = bounds.width()
        val height = bounds.height()

        if (width <= 0 || height <= 0) {
            return
        }

        val shader = when (shimmer.shape) {
            Shimmer.Shape.LINEAR -> {
                val vertical = shimmer.direction == Shimmer.Direction.TOP_TO_BOTTOM ||
                        shimmer.direction == Shimmer.Direction.BOTTOM_TO_TOP

                LinearGradient(
                    0f,
                    0f,
                    if (vertical) 0f else width,
                    if (vertical) height else 0f,
                    shimmer.colors,
                    shimmer.positions,
                    Shader.TileMode.CLAMP
                )
            }

            Shimmer.Shape.RADIAL -> {
                RadialGradient(
                    width / 2f,
                    height / 2f,
                    (max(width, height) / sqrt(2.0)).toFloat(),
                    shimmer.colors,
                    shimmer.positions,
                    Shader.TileMode.CLAMP
                )
            }
        }

        paint.shader = shader
    }
}
