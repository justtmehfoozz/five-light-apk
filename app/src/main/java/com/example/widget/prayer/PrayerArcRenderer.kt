package com.example.widget.prayer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

/**
 * Generates an astronomical, calm monochrome sundial/prayer arc bitmap.
 * Avoids looking like a generic dashboard chart.
 */
object PrayerArcRenderer {

    fun generateArcBitmap(
        widthPx: Int = 360,
        heightPx: Int = 80,
        progressFraction: Float, // 0.0f..1.0f from Fajr to Isha
        isNight: Boolean = false,
        isDarkMode: Boolean = true
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            widthPx.coerceAtLeast(60),
            heightPx.coerceAtLeast(40),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        val paddingHorizontal = 18f
        val paddingBottom = 12f
        val paddingTop = 12f

        val arcWidth = widthPx - (paddingHorizontal * 2f)
        val arcHeight = (heightPx - paddingBottom - paddingTop) * 1.8f
        val baseLineY = heightPx - paddingBottom

        val arcRect = RectF(
            paddingHorizontal,
            baseLineY - arcHeight,
            paddingHorizontal + arcWidth,
            baseLineY + arcHeight
        )

        val arcStrokeColor = if (isDarkMode) Color.argb(60, 255, 255, 255) else Color.argb(40, 0, 0, 0)
        val horizonColor = if (isDarkMode) Color.argb(30, 255, 255, 255) else Color.argb(25, 0, 0, 0)
        val dotFillColor = if (isDarkMode) Color.WHITE else Color.parseColor("#222222")
        val dotHaloColor = if (isDarkMode) Color.argb(80, 255, 255, 255) else Color.argb(50, 0, 0, 0)
        val markerColor = if (isDarkMode) Color.argb(45, 255, 255, 255) else Color.argb(35, 0, 0, 0)

        // Horizon subtle baseline
        val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = horizonColor
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(paddingHorizontal - 6f, baseLineY, widthPx - paddingHorizontal + 6f, baseLineY, horizonPaint)

        // Astronomical Arc
        val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = arcStrokeColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawArc(arcRect, 180f, 180f, false, arcPaint)

        // Five subtle prayer markers on the arc (Fajr 0%, Dhuhr ~40%, Asr ~65%, Maghrib 100%, etc.)
        val markerFractions = floatArrayOf(0.0f, 0.20f, 0.45f, 0.70f, 1.0f)
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = markerColor
            style = Paint.Style.FILL
        }

        val rx = arcWidth / 2f
        val ry = arcHeight
        val cx = paddingHorizontal + rx
        val cy = baseLineY

        for (fraction in markerFractions) {
            val angleRad = Math.PI * (1.0 - fraction)
            val mx = (cx + rx * cos(angleRad)).toFloat()
            val my = (cy - ry * sin(angleRad)).toFloat()
            canvas.drawCircle(mx, my, 2f, markerPaint)
        }

        // Active Sun/Position Indicator
        val clampedProgress = progressFraction.coerceIn(0.0f, 1.0f)
        val angleRad = Math.PI * (1.0 - clampedProgress)
        val pipX = if (!isNight) (cx + rx * cos(angleRad)).toFloat() else (paddingHorizontal + arcWidth)
        val pipY = if (!isNight) (cy - ry * sin(angleRad)).toFloat() else baseLineY

        // Halo
        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dotHaloColor
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawCircle(pipX, pipY, 6.5f, haloPaint)

        // Dot
        val pipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isNight) Color.argb(120, 200, 200, 200) else dotFillColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(pipX, pipY, 3.5f, pipPaint)

        return bitmap
    }
}
