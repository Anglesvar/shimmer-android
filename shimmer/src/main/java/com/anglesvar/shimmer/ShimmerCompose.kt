package com.anglesvar.shimmer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.tan

/**
 * Configuration for Jetpack Compose shimmer effect.
 * Use [rememberShimmerConfig] to create instances with animations.
 */
data class ShimmerConfig(
    val direction: Shimmer.Direction = Shimmer.Direction.LEFT_TO_RIGHT,
    val shape: Shimmer.Shape = Shimmer.Shape.LINEAR,
    val baseColor: Color = Color.Gray.copy(alpha = 0.3f),
    val highlightColor: Color = Color.White,
    val intensity: Float = 0f,
    val dropoff: Float = 0.5f,
    val tilt: Float = 20f,
    val widthRatio: Float = 1f,
    val heightRatio: Float = 1f,
    val fixedWidth: Dp = 0.dp,
    val fixedHeight: Dp = 0.dp,
)

/**
 * State holder for shimmer animation.
 */
@Stable
class ShimmerState(
    val config: ShimmerConfig,
    val animatedProgress: State<Float>
)

/**
 * Remember shimmer state with animation.
 *
 * @param config The shimmer configuration
 * @param durationMillis Animation duration in milliseconds
 * @param delayMillis Delay before animation starts
 * @param repeatMode How the animation should repeat
 * @return A [ShimmerState] that can be used with [Modifier.shimmer]
 */
@Composable
fun rememberShimmerState(
    config: ShimmerConfig = ShimmerConfig(),
    durationMillis: Int = 1000,
    delayMillis: Int = 0,
    repeatMode: RepeatMode = RepeatMode.Restart
): ShimmerState {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = LinearEasing
            ),
            repeatMode = repeatMode
        ),
        label = "shimmer_progress"
    )

    return remember(config) {
        ShimmerState(config, progress)
    }
}

/**
 * Applies a shimmer effect to the content.
 *
 * Example usage:
 * ```
 * Box(
 *     modifier = Modifier
 *         .size(200.dp, 100.dp)
 *         .background(Color.Gray)
 *         .shimmer()
 * )
 * ```
 *
 * @param state The shimmer state controlling the animation
 */
fun Modifier.shimmer(state: ShimmerState): Modifier = drawWithCache {
    val width = size.width
    val height = size.height
    val config = state.config

    // Calculate shimmer dimensions
    val shimmerWidth = when {
        config.fixedWidth.value > 0 -> config.fixedWidth.toPx()
        else -> width * config.widthRatio
    }
    val shimmerHeight = when {
        config.fixedHeight.value > 0 -> config.fixedHeight.toPx()
        else -> height * config.heightRatio
    }

    // Calculate positions for gradient
    val positions = calculatePositions(config.intensity, config.dropoff)
    val colors = createColors(config)

    // Calculate bounds with tilt
    val magnitude = max(shimmerWidth, shimmerHeight)
    val tiltRadians = Math.toRadians(config.tilt.toDouble())
    val offsetX = (magnitude * sin(tiltRadians)).toFloat()
    val offsetY = (magnitude * cos(tiltRadians)).toFloat()

    onDrawWithContent {
        drawContent()

        val progress = state.animatedProgress.value

        // Calculate translation based on direction
        val translation = calculateTranslation(
            progress = progress,
            width = width,
            height = height,
            shimmerWidth = shimmerWidth,
            shimmerHeight = shimmerHeight,
            offsetX = offsetX,
            offsetY = offsetY,
            direction = config.direction
        )

        when (config.shape) {
            Shimmer.Shape.LINEAR -> {
                drawLinearShimmer(
                    colors = colors,
                    positions = positions,
                    translation = translation,
                    width = shimmerWidth,
                    height = shimmerHeight,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    tilt = config.tilt
                )
            }
            Shimmer.Shape.RADIAL -> {
                drawRadialShimmer(
                    colors = colors,
                    positions = positions,
                    translation = translation,
                    width = shimmerWidth,
                    height = shimmerHeight
                )
            }
        }
    }
}

/**
 * Convenience modifier that creates its own shimmer state with default configuration.
 *
 * @param direction Direction of the shimmer animation
 * @param shape Shape of the shimmer (LINEAR or RADIAL)
 * @param baseColor Base color of the shimmer
 * @param highlightColor Highlight color of the shimmer
 * @param intensity Intensity of the highlight
 * @param dropoff Gradient dropoff
 * @param tilt Tilt angle in degrees
 * @param durationMillis Animation duration in milliseconds
 */
fun Modifier.shimmer(
    direction: Shimmer.Direction = Shimmer.Direction.LEFT_TO_RIGHT,
    shape: Shimmer.Shape = Shimmer.Shape.LINEAR,
    baseColor: Color = Color.Gray.copy(alpha = 0.3f),
    highlightColor: Color = Color.White,
    intensity: Float = 0f,
    dropoff: Float = 0.5f,
    tilt: Float = 20f,
    durationMillis: Int = 1000,
): Modifier = composed {
    val config = remember(direction, shape, baseColor, highlightColor, intensity, dropoff, tilt) {
        ShimmerConfig(
            direction = direction,
            shape = shape,
            baseColor = baseColor,
            highlightColor = highlightColor,
            intensity = intensity,
            dropoff = dropoff,
            tilt = tilt
        )
    }
    val state = rememberShimmerState(config = config, durationMillis = durationMillis)
    shimmer(state)
}

/**
 * A convenience composable that wraps content with a shimmer background.
 * Useful for placeholder loading states.
 *
 * @param modifier Modifier for the shimmer container
 * @param state Optional shimmer state for controlling animation
 * @param backgroundColor Background color for the shimmer container
 * @param content Content to display with shimmer
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    state: ShimmerState? = null,
    backgroundColor: Color = Color.LightGray,
    content: @Composable () -> Unit = {}
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(backgroundColor)
            .then(
                if (state != null) Modifier.shimmer(state)
                else Modifier.shimmer()
            )
    ) {
        content()
    }
}

// Private helper functions

private fun calculatePositions(intensity: Float, dropoff: Float): FloatArray {
    if (intensity < 0f || dropoff < 0f) {
        return floatArrayOf(0f, 0f, 1f, 1f)
    }

    val intensityNorm = intensity.coerceAtMost(1f)
    val dropoffNorm = dropoff.coerceAtMost(1f)
    val edgeWidth = (1f - intensityNorm) / 2f
    val centerWidth = 1f - edgeWidth * 2f

    return floatArrayOf(
        0f,
        edgeWidth,
        edgeWidth + centerWidth - centerWidth * dropoffNorm,
        1f
    )
}

private fun createColors(config: ShimmerConfig): List<Color> {
    return when (config.shape) {
        Shimmer.Shape.LINEAR -> listOf(
            config.baseColor,
            config.highlightColor,
            config.highlightColor,
            config.baseColor
        )
        Shimmer.Shape.RADIAL -> listOf(
            config.highlightColor,
            config.highlightColor,
            config.baseColor,
            config.baseColor
        )
    }
}

private fun calculateTranslation(
    progress: Float,
    width: Float,
    height: Float,
    shimmerWidth: Float,
    shimmerHeight: Float,
    offsetX: Float,
    offsetY: Float,
    direction: Shimmer.Direction
): Offset {
    return when (direction) {
        Shimmer.Direction.LEFT_TO_RIGHT -> {
            val x = -shimmerWidth - 2 * offsetX + (width + shimmerWidth + 2 * offsetX) * progress
            Offset(x, 0f)
        }
        Shimmer.Direction.RIGHT_TO_LEFT -> {
            val x = width + shimmerWidth + 2 * offsetX - (width + shimmerWidth + 2 * offsetX) * progress
            Offset(x, 0f)
        }
        Shimmer.Direction.TOP_TO_BOTTOM -> {
            val y = -shimmerHeight - 2 * offsetY + (height + shimmerHeight + 2 * offsetY) * progress
            Offset(0f, y)
        }
        Shimmer.Direction.BOTTOM_TO_TOP -> {
            val y = height + shimmerHeight + 2 * offsetY - (height + shimmerHeight + 2 * offsetY) * progress
            Offset(0f, y)
        }
    }
}

private fun DrawScope.drawLinearShimmer(
    colors: List<Color>,
    positions: FloatArray,
    translation: Offset,
    width: Float,
    height: Float,
    offsetX: Float,
    offsetY: Float,
    tilt: Float
) {
    val tiltRadians = Math.toRadians(tilt.toDouble()).toFloat()
    val cos = cos(tiltRadians)
    val sin = sin(tiltRadians)

    val start = Offset(
        translation.x - offsetX,
        translation.y - offsetY
    )
    val end = Offset(
        translation.x + width + offsetX,
        translation.y + height + offsetY
    )

    val gradient = Brush.linearGradient(
        colorStops = colors.mapIndexed { index, color ->
            positions[index] to color
        }.toTypedArray(),
        start = start,
        end = end,
        tileMode = TileMode.Clamp
    )

    drawRect(
        brush = gradient,
        blendMode = BlendMode.SrcAtop
    )
}

private fun DrawScope.drawRadialShimmer(
    colors: List<Color>,
    positions: FloatArray,
    translation: Offset,
    width: Float,
    height: Float
) {
    val center = Offset(
        translation.x + width / 2f,
        translation.y + height / 2f
    )
    val radius = max(width, height) / 2f

    val gradient = Brush.radialGradient(
        colorStops = colors.mapIndexed { index, color ->
            positions[index] to color
        }.toTypedArray(),
        center = center,
        radius = radius,
        tileMode = TileMode.Clamp
    )

    drawRect(
        brush = gradient,
        blendMode = BlendMode.SrcAtop
    )
}
