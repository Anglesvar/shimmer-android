package com.anglesvar.shimmer

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.res.use
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.tan

/**
 * Shimmer configuration class that defines the appearance and behavior of the shimmer effect.
 * Use [AlphaHighlightBuilder] or [ColorHighlightBuilder] to create instances.
 */
class Shimmer private constructor(builder: Builder<*>) {

    enum class Shape {
        LINEAR,
        RADIAL
    }

    enum class Direction {
        LEFT_TO_RIGHT,
        TOP_TO_BOTTOM,
        RIGHT_TO_LEFT,
        BOTTOM_TO_TOP
    }

    @ColorInt
    val baseColor: Int = builder.baseColor
    @ColorInt
    val highlightColor: Int = builder.highlightColor
    val shape: Shape = builder.shape
    val direction: Direction = builder.direction
    val alphaShimmer: Boolean = builder.alphaShimmer
    val clipToChildren: Boolean = builder.clipToChildren
    val autoStart: Boolean = builder.autoStart

    val animationDuration: Long = builder.animationDuration
    val repeatCount: Int = builder.repeatCount
    val repeatMode: Int = builder.repeatMode
    val repeatDelay: Long = builder.repeatDelay
    val startDelay: Long = builder.startDelay

    @FloatRange(from = 0.0)
    val widthRatio: Float = builder.widthRatio
    @FloatRange(from = 0.0)
    val heightRatio: Float = builder.heightRatio
    val fixedWidth: Int = builder.fixedWidth
    val fixedHeight: Int = builder.fixedHeight

    @FloatRange(from = 0.0)
    val intensity: Float = builder.intensity
    @FloatRange(from = 0.0)
    val dropoff: Float = builder.dropoff
    val tilt: Float = builder.tilt

    val colors: IntArray = IntArray(4)
    val positions: FloatArray = FloatArray(4)
    val bounds: RectF = RectF()

    init {
        updateColors()
        updatePositions()
    }

    fun width(width: Int): Int = if (fixedWidth > 0) fixedWidth else (widthRatio * width).toInt()

    fun height(height: Int): Int =
        if (fixedHeight > 0) fixedHeight else (heightRatio * height).toInt()

    fun updateColors() {
        when (shape) {
            Shape.LINEAR -> {
                colors[0] = baseColor
                colors[1] = highlightColor
                colors[2] = highlightColor
                colors[3] = baseColor
            }

            Shape.RADIAL -> {
                colors[0] = highlightColor
                colors[1] = highlightColor
                colors[2] = baseColor
                colors[3] = baseColor
            }
        }
    }

    fun updatePositions() {
        if (intensity < 0f || dropoff < 0f) {
            positions[0] = 0f
            positions[1] = 0f
            positions[2] = 1f
            positions[3] = 1f
            return
        }

        val intensityNorm = intensity.coerceAtMost(1f)
        val dropoffNorm = dropoff.coerceAtMost(1f)
        val edgeWidth = (1f - intensityNorm) / 2f
        val centerWidth = 1f - edgeWidth * 2f

        positions[0] = 0f
        positions[1] = edgeWidth
        positions[2] = edgeWidth + centerWidth - centerWidth * dropoffNorm
        positions[3] = 1f
    }

    fun updateBounds(viewWidth: Int, viewHeight: Int) {
        val width = width(viewWidth)
        val height = height(viewHeight)

        val magnitude = max(width, height)
        val tiltRadians = Math.toRadians(tilt.toDouble())
        val offsetX = magnitude * sin(tiltRadians).toFloat()
        val offsetY = magnitude * cos(tiltRadians).toFloat()

        bounds.set(
            -offsetX,
            -offsetY,
            width + offsetX,
            height + offsetY
        )
    }

    abstract class Builder<T : Builder<T>> {
        @ColorInt
        var baseColor: Int = 0x4CFFFFFF
        @ColorInt
        var highlightColor: Int = Color.WHITE
        var shape: Shape = Shape.LINEAR
        var direction: Direction = Direction.LEFT_TO_RIGHT
        var alphaShimmer: Boolean = true
        var clipToChildren: Boolean = true
        var autoStart: Boolean = true

        var animationDuration: Long = 1000L
        var repeatCount: Int = -1 // Infinite
        var repeatMode: Int = 1 // RESTART
        var repeatDelay: Long = 0L
        var startDelay: Long = 0L

        @FloatRange(from = 0.0)
        var widthRatio: Float = 1f
        @FloatRange(from = 0.0)
        var heightRatio: Float = 1f
        var fixedWidth: Int = 0
        var fixedHeight: Int = 0

        @FloatRange(from = 0.0)
        var intensity: Float = 0f
        @FloatRange(from = 0.0)
        var dropoff: Float = 0.5f
        var tilt: Float = 20f

        protected abstract fun getThis(): T

        fun setDirection(direction: Direction): T = apply { this.direction = direction }.getThis()

        fun setShape(shape: Shape): T = apply { this.shape = shape }.getThis()

        fun setClipToChildren(clip: Boolean): T = apply { this.clipToChildren = clip }.getThis()

        fun setAutoStart(autoStart: Boolean): T = apply { this.autoStart = autoStart }.getThis()

        fun setDuration(millis: Long): T = apply {
            require(millis >= 0) { "Duration must be non-negative" }
            this.animationDuration = millis
        }.getThis()

        fun setRepeatCount(count: Int): T = apply { this.repeatCount = count }.getThis()

        fun setRepeatMode(mode: Int): T = apply { this.repeatMode = mode }.getThis()

        fun setRepeatDelay(millis: Long): T = apply {
            require(millis >= 0) { "Repeat delay must be non-negative" }
            this.repeatDelay = millis
        }.getThis()

        fun setStartDelay(millis: Long): T = apply {
            require(millis >= 0) { "Start delay must be non-negative" }
            this.startDelay = millis
        }.getThis()

        fun setWidthRatio(ratio: Float): T = apply {
            require(ratio >= 0f) { "Width ratio must be non-negative" }
            this.widthRatio = ratio
        }.getThis()

        fun setHeightRatio(ratio: Float): T = apply {
            require(ratio >= 0f) { "Height ratio must be non-negative" }
            this.heightRatio = ratio
        }.getThis()

        fun setFixedWidth(width: Int): T = apply {
            require(width >= 0) { "Fixed width must be non-negative" }
            this.fixedWidth = width
        }.getThis()

        fun setFixedHeight(height: Int): T = apply {
            require(height >= 0) { "Fixed height must be non-negative" }
            this.fixedHeight = height
        }.getThis()

        fun setIntensity(intensity: Float): T = apply {
            require(intensity >= 0f) { "Intensity must be non-negative" }
            this.intensity = intensity
        }.getThis()

        fun setDropoff(dropoff: Float): T = apply {
            require(dropoff >= 0f) { "Dropoff must be non-negative" }
            this.dropoff = dropoff
        }.getThis()

        fun setTilt(tilt: Float): T = apply { this.tilt = tilt }.getThis()

        fun copyFrom(other: Shimmer): T = apply {
            baseColor = other.baseColor
            highlightColor = other.highlightColor
            shape = other.shape
            direction = other.direction
            clipToChildren = other.clipToChildren
            autoStart = other.autoStart
            animationDuration = other.animationDuration
            repeatCount = other.repeatCount
            repeatMode = other.repeatMode
            repeatDelay = other.repeatDelay
            startDelay = other.startDelay
            widthRatio = other.widthRatio
            heightRatio = other.heightRatio
            fixedWidth = other.fixedWidth
            fixedHeight = other.fixedHeight
            intensity = other.intensity
            dropoff = other.dropoff
            tilt = other.tilt
        }.getThis()

        fun consumeAttributes(context: Context, attrs: TypedArray): T = apply {
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_clip_to_children)) {
                clipToChildren = attrs.getBoolean(
                    R.styleable.ShimmerFrameLayout_shimmer_clip_to_children,
                    clipToChildren
                )
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_auto_start)) {
                autoStart =
                    attrs.getBoolean(R.styleable.ShimmerFrameLayout_shimmer_auto_start, autoStart)
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_base_alpha)) {
                setBaseAlpha(
                    attrs.getFloat(
                        R.styleable.ShimmerFrameLayout_shimmer_base_alpha,
                        0.3f
                    )
                )
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_highlight_alpha)) {
                setHighlightAlpha(
                    attrs.getFloat(
                        R.styleable.ShimmerFrameLayout_shimmer_highlight_alpha,
                        1f
                    )
                )
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_duration)) {
                animationDuration = attrs.getInt(
                    R.styleable.ShimmerFrameLayout_shimmer_duration,
                    animationDuration.toInt()
                ).toLong()
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_count)) {
                repeatCount =
                    attrs.getInt(R.styleable.ShimmerFrameLayout_shimmer_repeat_count, repeatCount)
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_delay)) {
                repeatDelay = attrs.getInt(
                    R.styleable.ShimmerFrameLayout_shimmer_repeat_delay,
                    repeatDelay.toInt()
                ).toLong()
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_mode)) {
                repeatMode =
                    attrs.getInt(R.styleable.ShimmerFrameLayout_shimmer_repeat_mode, repeatMode)
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_start_delay)) {
                startDelay = attrs.getInt(
                    R.styleable.ShimmerFrameLayout_shimmer_start_delay,
                    startDelay.toInt()
                ).toLong()
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_direction)) {
                val directionValue = attrs.getInt(
                    R.styleable.ShimmerFrameLayout_shimmer_direction,
                    direction.ordinal
                )
                direction = Direction.entries[directionValue]
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_shape)) {
                val shapeValue =
                    attrs.getInt(R.styleable.ShimmerFrameLayout_shimmer_shape, shape.ordinal)
                shape = Shape.entries[shapeValue]
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_dropoff)) {
                dropoff = attrs.getFloat(R.styleable.ShimmerFrameLayout_shimmer_dropoff, dropoff)
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_intensity)) {
                intensity =
                    attrs.getFloat(R.styleable.ShimmerFrameLayout_shimmer_intensity, intensity)
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_width_ratio)) {
                widthRatio =
                    attrs.getFloat(R.styleable.ShimmerFrameLayout_shimmer_width_ratio, widthRatio)
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_height_ratio)) {
                heightRatio =
                    attrs.getFloat(R.styleable.ShimmerFrameLayout_shimmer_height_ratio, heightRatio)
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_fixed_width)) {
                fixedWidth = attrs.getDimensionPixelSize(
                    R.styleable.ShimmerFrameLayout_shimmer_fixed_width,
                    fixedWidth
                )
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_fixed_height)) {
                fixedHeight = attrs.getDimensionPixelSize(
                    R.styleable.ShimmerFrameLayout_shimmer_fixed_height,
                    fixedHeight
                )
            }
            if (attrs.hasValue(R.styleable.ShimmerFrameLayout_shimmer_tilt)) {
                tilt = attrs.getFloat(R.styleable.ShimmerFrameLayout_shimmer_tilt, tilt)
            }
        }.getThis()

        protected open fun setBaseAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T = apply {
            val intAlpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
            baseColor = (baseColor and 0x00FFFFFF) or (intAlpha shl 24)
        }.getThis()

        protected open fun setHighlightAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T =
            apply {
                val intAlpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
                highlightColor = (highlightColor and 0x00FFFFFF) or (intAlpha shl 24)
            }.getThis()

        fun build(): Shimmer = Shimmer(this)
    }

    class AlphaHighlightBuilder : Builder<AlphaHighlightBuilder>() {
        init {
            alphaShimmer = true
        }

        override fun getThis(): AlphaHighlightBuilder = this
    }

    class ColorHighlightBuilder : Builder<ColorHighlightBuilder>() {
        init {
            alphaShimmer = false
        }

        override fun getThis(): ColorHighlightBuilder = this

        fun setBaseColor(@ColorInt color: Int): ColorHighlightBuilder = apply {
            baseColor = (baseColor and -0x1000000) or (color and 0x00FFFFFF)
        }

        fun setHighlightColor(@ColorInt color: Int): ColorHighlightBuilder = apply {
            highlightColor = color
        }
    }
}
