package com.saggitt.omega.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.android.launcher3.util.Executors.THREAD_POOL_EXECUTOR
import com.saggitt.omega.preferences.OmegaPreferences
import kotlin.math.max

class BlurWallpaperFilter(private val context: Context) : WallpaperFilter {

    private var blurRadius = 25

    override fun applyPrefs(prefs: OmegaPreferences) {
        blurRadius = (prefs.themeBlurRadius.onGetValue() / BlurWallpaperProvider.DOWNSAMPLE_FACTOR)
            .toInt()
        blurRadius = blurRadius.coerceAtLeast(1).coerceAtMost(25)
    }

    override fun apply(wallpaper: Bitmap): WallpaperFilter.ApplyTask {
        return WallpaperFilter.ApplyTask.create { emitter ->
            THREAD_POOL_EXECUTOR.execute {
                try {
                    val factor = BlurWallpaperProvider.DOWNSAMPLE_FACTOR
                    val downW = max(1, wallpaper.width / factor)
                    val downH = max(1, wallpaper.height / factor)

                    val scaled = Bitmap.createScaledBitmap(wallpaper, downW, downH, true)
                    val blurred = StackBlur.blur(scaled, blurRadius, true)

                    val output = Bitmap.createBitmap(wallpaper.width, wallpaper.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(output)
                    val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                    val srcRect = Rect(0, 0, blurred.width, blurred.height)
                    val dstRect = Rect(0, 0, wallpaper.width, wallpaper.height)
                    canvas.drawBitmap(blurred, srcRect, dstRect, paint)

                    if (blurred != scaled) {
                        scaled.recycle()
                    }
                    blurred.recycle()

                    emitter.onSuccess(output)
                } catch (t: Throwable) {
                    emitter.onError(t)
                }
            }
        }
    }
}
