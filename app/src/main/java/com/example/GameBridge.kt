package com.example

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.webkit.JavascriptInterface

/**
 * JavaScript interface to receive callbacks from the HTML5 canvas game running in WebView.
 */
class GameBridge(
    private val context: Context,
    private val onScoreUpdated: (score: Int, highScore: Int, burgersEaten: Int, bounces: Int, isPaused: Boolean, isGameOver: Boolean) -> Unit,
    private val onBounce: () -> Unit = {},
    private val onEat: (isGolden: Boolean) -> Unit = {},
    private val onGameOverCallback: (score: Int, highScore: Int, burgers: Int, bounces: Int) -> Unit = { _, _, _, _ -> }
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    @JavascriptInterface
    fun onScoreUpdate(
        score: Int,
        highScore: Int,
        burgersEaten: Int,
        bounces: Int,
        isPaused: Boolean,
        isGameOver: Boolean
    ) {
        onScoreUpdated(score, highScore, burgersEaten, bounces, isPaused, isGameOver)
    }

    @JavascriptInterface
    fun onWallBounce() {
        onBounce()
        triggerHaptic(durationMs = 45, amplitude = 180)
    }

    @JavascriptInterface
    fun onEatBurger(isGolden: Boolean) {
        onEat(isGolden)
        triggerHaptic(durationMs = if (isGolden) 70 else 30, amplitude = if (isGolden) 220 else 120)
    }

    @JavascriptInterface
    fun onGameOver(score: Int, highScore: Int, burgers: Int, bounces: Int) {
        triggerHaptic(durationMs = 150, amplitude = 255)
        onGameOverCallback(score, highScore, burgers, bounces)
    }

    private fun triggerHaptic(durationMs: Long, amplitude: Int) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val effect = VibrationEffect.createOneShot(
                            durationMs,
                            amplitude.coerceIn(1, 255)
                        )
                        v.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore if vibration permission or device lacks haptic motor
        }
    }
}
