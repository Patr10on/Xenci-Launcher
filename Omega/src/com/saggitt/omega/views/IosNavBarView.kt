package com.saggitt.omega.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import com.android.launcher3.LauncherState
import com.android.launcher3.util.Themes
import com.saggitt.omega.OmegaLauncher
import com.saggitt.omega.gestures.handlers.NotificationsOpenGestureHandler
import com.saggitt.omega.gestures.handlers.OpenDashGestureHandler
import com.saggitt.omega.gestures.handlers.OpenHiddenAppsGestureHandler
import com.saggitt.omega.gestures.handlers.SleepGestureHandler
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.omegaPrefs
import kotlin.math.abs
import kotlin.math.roundToInt

class IosNavBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val prefs = context.omegaPrefs
    private val launcher: OmegaLauncher by lazy {
        context as? OmegaLauncher ?: OmegaLauncher.getLauncher(context)
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barRect = RectF()

    private var barWidthDp = 140f
    private var barHeightDp = 5f
    private var barBottomMarginDp = 10f
    private var barRadiusDp = 10f
    private var barOpacity = 0.85f
    private var colorMode = "auto"
    private var hapticEnabled = true

    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var dragScaleX = 1f

    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var lastTapTime = 0L
    private var hasTriggeredLongPress = false

    private val handlerLooper = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable {
        hasTriggeredLongPress = true
        executeAction(prefs.iosNavLongPressAction.onGetValue())
    }

    init {
        updateConfig()
    }

    fun updateConfig() {
        barWidthDp = prefs.iosNavWidth.onGetValue()
        barHeightDp = prefs.iosNavHeight.onGetValue()
        barBottomMarginDp = prefs.iosNavBottomMargin.onGetValue()
        barRadiusDp = prefs.iosNavRadius.onGetValue()
        barOpacity = prefs.iosNavOpacity.onGetValue()
        colorMode = prefs.iosNavColorMode.onGetValue()
        hapticEnabled = prefs.iosNavHaptic.onGetValue()
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val neededHeightPx = dpToPx(barBottomMarginDp + barHeightDp + 24f).roundToInt().coerceAtLeast(dpToPx(44f).roundToInt())
        setMeasuredDimension(width, neededHeightPx)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val pillW = dpToPx(barWidthDp) * dragScaleX
        val pillH = dpToPx(barHeightDp)
        val pillRadius = dpToPx(barRadiusDp)
        val bottomMarginPx = dpToPx(barBottomMarginDp)

        val left = (width - pillW) / 2f + dragOffsetX
        val right = left + pillW
        val bottom = height.toFloat() - bottomMarginPx + dragOffsetY
        val top = bottom - pillH

        barRect.set(left, top, right, bottom)

        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val baseColor = when (colorMode) {
            "white" -> Color.WHITE
            "black" -> Color.BLACK
            "accent" -> Themes.getColorAccent(context)
            else -> if (isNight) Color.WHITE else Color.BLACK
        }

        val alphaInt = (barOpacity * 255f).roundToInt().coerceIn(0, 255)
        barPaint.color = Color.argb(alphaInt, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))

        val shadowColor = if (baseColor == Color.WHITE) Color.argb((alphaInt * 0.45f).roundToInt(), 0, 0, 0)
        else Color.argb((alphaInt * 0.25f).roundToInt(), 255, 255, 255)
        shadowPaint.color = shadowColor
        val shadowOffset = dpToPx(1f)
        canvas.drawRoundRect(left, top + shadowOffset, right, bottom + shadowOffset, pillRadius, pillRadius, shadowPaint)

        canvas.drawRoundRect(barRect, pillRadius, pillRadius, barPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTime = System.currentTimeMillis()
                hasTriggeredLongPress = false
                handlerLooper.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - downX
                val dy = event.y - downY
                if (abs(dx) > 16f || abs(dy) > 16f) {
                    handlerLooper.removeCallbacks(longPressRunnable)
                }
                dragOffsetX = dx * 0.25f
                dragOffsetY = (dy * 0.35f).coerceAtMost(0f)
                dragScaleX = 1f + (abs(dy) / dpToPx(300f)).coerceAtMost(0.3f)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                handlerLooper.removeCallbacks(longPressRunnable)
                val upX = event.x
                val upY = event.y
                val elapsed = System.currentTimeMillis() - downTime
                val deltaY = downY - upY
                val deltaX = upX - downX

                animateReset()

                if (hasTriggeredLongPress) {
                    return true
                }

                val swipeThresholdY = dpToPx(40f)
                val swipeThresholdX = dpToPx(50f)

                if (deltaY > swipeThresholdY) {
                    if (elapsed >= 300L) {
                        executeAction(prefs.iosNavSwipeHoldAction.onGetValue())
                    } else {
                        executeAction(prefs.iosNavSwipeUpAction.onGetValue())
                    }
                } else if (abs(deltaX) > swipeThresholdX) {
                    executeAction(prefs.iosNavSwipeSideAction.onGetValue())
                } else if (abs(deltaX) < dpToPx(15f) && abs(deltaY) < dpToPx(15f)) {
                    val now = System.currentTimeMillis()
                    if (now - lastTapTime < 280L) {
                        executeAction(prefs.iosNavDoubleTapAction.onGetValue())
                        lastTapTime = 0L
                    } else {
                        lastTapTime = now
                        executeAction("home")
                    }
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                handlerLooper.removeCallbacks(longPressRunnable)
                animateReset()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animateReset() {
        val startY = dragOffsetY
        val startX = dragOffsetX
        val startScale = dragScaleX
        val anim = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                val f = va.animatedValue as Float
                dragOffsetY = startY * f
                dragOffsetX = startX * f
                dragScaleX = 1f + (startScale - 1f) * f
                invalidate()
            }
        }
        anim.start()
    }

    private fun executeAction(actionKey: String) {
        if (hapticEnabled) {
            try {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (_: Exception) {}
        }
        when (actionKey) {
            "home" -> {
                launcher.stateManager.goToState(LauncherState.NORMAL)
            }
            "recents" -> {
                launcher.stateManager.goToState(LauncherState.OVERVIEW)
            }
            "drawer" -> {
                launcher.stateManager.goToState(LauncherState.ALL_APPS)
            }
            "back" -> {
                launcher.onBackPressed()
            }
            "notifications" -> {
                try {
                    NotificationsOpenGestureHandler(launcher, null)
                        .onGestureTrigger(launcher.gestureController, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed notifications gesture", e)
                }
            }
            "assistant" -> {
                try {
                    OpenDashGestureHandler(launcher, null)
                        .onGestureTrigger(launcher.gestureController, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed assistant gesture", e)
                }
            }
            "sleep" -> {
                try {
                    SleepGestureHandler(launcher, null)
                        .onGestureTrigger(launcher.gestureController, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed sleep gesture", e)
                }
            }
            "hidden_apps" -> {
                try {
                    OpenHiddenAppsGestureHandler(launcher, null)
                        .onGestureTrigger(launcher.gestureController, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed hidden apps gesture", e)
                }
            }
            "none" -> {}
        }
    }

    companion object {
        private const val TAG = "IosNavBarView"

        fun openSystemNavigationSettings(context: Context) {
            val intents = listOf(
                Intent("com.android.settings.GESTURE_NAVIGATION_SETTINGS"),
                Intent("android.settings.SYSTEM_GESTURE_NAVIGATION_SETTINGS"),
                Intent("com.android.settings.action.GESTURE_NAVIGATION_SETTINGS"),
                Intent(Settings.ACTION_DISPLAY_SETTINGS),
                Intent(Settings.ACTION_SETTINGS)
            )
            for (intent in intents) {
                try {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        return
                    }
                } catch (_: Exception) {}
            }
            try {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {}
        }
    }
}
