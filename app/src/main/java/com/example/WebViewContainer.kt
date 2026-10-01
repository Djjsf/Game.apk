package com.example

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.Slate950

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    viewModel: SnakeGameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bridge = remember(viewModel) {
        GameBridge(
            context = context,
            onScoreUpdated = { score, highScore, burgersEaten, bounces, isPaused, isGameOver ->
                viewModel.onScoreUpdatedFromBridge(
                    score = score,
                    highScore = highScore,
                    burgersEaten = burgersEaten,
                    bounces = bounces,
                    isPaused = isPaused,
                    isGameOver = isGameOver
                )
            },
            onGameOverCallback = { score, highScore, burgers, bounces ->
                viewModel.onGameOverFromBridge(score, highScore, burgers, bounces)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("webview_game_container")
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    setBackgroundColor(android.graphics.Color.parseColor("#020617"))
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        @Suppress("DEPRECATION")
                        allowFileAccessFromFileURLs = true
                        @Suppress("DEPRECATION")
                        allowUniversalAccessFromFileURLs = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mediaPlaybackRequiresUserGesture = false
                    }

                    addJavascriptInterface(bridge, "AndroidBridge")

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            Log.d(
                                "SnakeWebView",
                                "${consoleMessage?.message()} -- line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}"
                            )
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: android.webkit.RenderProcessGoneDetail?
                        ): Boolean {
                            Log.w("SnakeWebView", "Render process gone. didCrash=${detail?.didCrash()}")
                            return true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Synchronize current initial states
                            val state = viewModel.uiState.value
                            view?.evaluateJavascript(
                                "window.game && window.game.toggleSound(${state.soundEnabled});",
                                null
                            )
                            view?.evaluateJavascript(
                                "window.game && window.game.toggleTailCollision(${state.tailCollisionEnabled});",
                                null
                            )
                        }
                    }

                    // Attach the JS dispatcher to the ViewModel
                    viewModel.attachJsExecutor { script ->
                        post {
                            evaluateJavascript(script, null)
                        }
                    }

                    val htmlContent = runCatching {
                        ctx.assets.open("snake_game.html").bufferedReader().use { it.readText() }
                    }.getOrNull()

                    if (htmlContent != null) {
                        loadDataWithBaseURL(
                            "file:///android_asset/",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    } else {
                        loadUrl("file:///android_asset/snake_game.html")
                    }
                }
            },
            update = { webView ->
                // Ensure the ViewModel's JS dispatcher is always bound to the active WebView
                viewModel.attachJsExecutor { script ->
                    webView.post {
                        webView.evaluateJavascript(script, null)
                    }
                }
            }
        )
    }
}

