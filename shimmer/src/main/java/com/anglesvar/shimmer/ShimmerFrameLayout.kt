package com.anglesvar.shimmer

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.use

/**
 * A FrameLayout that displays a shimmer effect over its children.
 *
 * Usage in XML:
 * ```xml
 * <com.anglesvar.shimmer.ShimmerFrameLayout
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:shimmer_auto_start="true"
 *     app:shimmer_base_alpha="0.3"
 *     app:shimmer_duration="1000"
 *     app:shimmer_direction="left_to_right">
 *
 *     <!-- Your content here -->
 *
 * </com.anglesvar.shimmer.ShimmerFrameLayout>
 * ```
 */
class ShimmerFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val shimmerDrawable = ShimmerDrawable()
    private var showShimmer = true
    private var stoppedShimmerBecauseVisibility = false

    init {
        setWillNotDraw(false)
        shimmerDrawable.callback = this

        val shimmer = if (attrs == null) {
            Shimmer.AlphaHighlightBuilder().build()
        } else {
            context.obtainStyledAttributes(attrs, R.styleable.ShimmerFrameLayout, defStyleAttr, 0)
                .use { a ->
                    val builder = if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_colored) &&
                        a.getBoolean(R.styleable.ShimmerFrameLayout_shimmer_colored, false)
                    ) {
                        Shimmer.ColorHighlightBuilder()
                    } else {
                        Shimmer.AlphaHighlightBuilder()
                    }

                    builder.consumeAttributes(context, a)
                    builder.build()
                }
        }

        setShimmer(shimmer)
    }

    /**
     * Sets the shimmer configuration.
     */
    fun setShimmer(shimmer: Shimmer?) {
        shimmerDrawable.setShimmer(shimmer)
        shimmer?.let {
            if (it.clipToChildren) {
                setLayerType(LAYER_TYPE_HARDWARE, shimmerDrawable.paint)
            } else {
                setLayerType(LAYER_TYPE_NONE, null)
            }
        }
    }

    /**
     * Gets the current shimmer configuration.
     */
    fun getShimmer(): Shimmer? = shimmerDrawable.shimmer

    /**
     * Starts the shimmer animation.
     */
    fun startShimmer() {
        if (isAttachedToWindow) {
            shimmerDrawable.startShimmer()
        }
    }

    /**
     * Stops the shimmer animation.
     */
    fun stopShimmer() {
        stoppedShimmerBecauseVisibility = false
        shimmerDrawable.stopShimmer()
    }

    /**
     * Returns true if the shimmer animation is running.
     */
    fun isShimmerRunning(): Boolean = shimmerDrawable.isShimmerRunning()

    /**
     * Shows the shimmer effect.
     *
     * @param startShimmer if true, starts the animation immediately
     */
    @JvmOverloads
    fun showShimmer(startShimmer: Boolean = true) {
        showShimmer = true
        if (startShimmer) {
            startShimmer()
        }
        invalidate()
    }

    /**
     * Hides the shimmer effect and stops the animation.
     */
    fun hideShimmer() {
        stopShimmer()
        showShimmer = false
        invalidate()
    }

    /**
     * Returns true if the shimmer effect is visible.
     */
    fun isShimmerVisible(): Boolean = showShimmer

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = right - left
        val height = bottom - top
        shimmerDrawable.setBounds(0, 0, width, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        shimmerDrawable.maybeStartShimmer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopShimmer()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (showShimmer) {
            shimmerDrawable.draw(canvas)
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === shimmerDrawable
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (visibility != View.VISIBLE) {
            if (isShimmerRunning()) {
                stopShimmer()
                stoppedShimmerBecauseVisibility = true
            }
        } else if (stoppedShimmerBecauseVisibility) {
            shimmerDrawable.maybeStartShimmer()
            stoppedShimmerBecauseVisibility = false
        }
    }
}
