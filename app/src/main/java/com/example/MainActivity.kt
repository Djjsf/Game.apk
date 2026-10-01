package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate950

class MainActivity : ComponentActivity() {

    private val viewModel: SnakeGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SnakeGameApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SnakeGameApp(
    viewModel: SnakeGameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val leaderboardEntries by viewModel.leaderboardEntries.collectAsStateWithLifecycle()

    BackHandler {
        if (uiState.showLeaderboardDialog) {
            viewModel.dismissLeaderboard()
        } else if (uiState.showNameInputDialog) {
            viewModel.dismissNameInputDialog()
        } else if (!uiState.isPaused) {
            viewModel.togglePause()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Slate950
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950)
        ) {
            // Top HUD ScoreBoard (Safe area for Status Bar)
            ScoreBoard(
                score = uiState.score,
                highScore = uiState.highScore,
                burgersEaten = uiState.burgersEaten,
                bounces = uiState.bounces,
                isPaused = uiState.isPaused,
                soundEnabled = uiState.soundEnabled,
                tailCollisionEnabled = uiState.tailCollisionEnabled,
                onTogglePause = { viewModel.togglePause() },
                onToggleSound = { viewModel.toggleSound() },
                onToggleCollision = { viewModel.toggleTailCollision() },
                onOpenLeaderboard = { viewModel.openLeaderboard() },
                modifier = Modifier.statusBarsPadding()
            )

            // Center Game Arena: Android WebView running HTML5 Canvas Slither Snake
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                WebViewContainer(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Arcade Controls: UP, DOWN, LEFT, RIGHT touch buttons & Turbo Boost
            GameControls(
                currentDirection = uiState.lastDirection,
                boostActive = uiState.boostActive,
                onDirectionChange = { dir -> viewModel.sendDirection(dir) },
                onBoostToggle = { active -> viewModel.toggleBoost(active) },
                onRestart = { viewModel.restartGame() },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }

    // Player Name Input Dialog (Prompts player to enter their name before saving score)
    if (uiState.showNameInputDialog) {
        com.example.ui.dialogs.NameInputDialog(
            initialName = uiState.playerName,
            scoreSummary = uiState.pendingScoreToSave,
            isSubmitting = uiState.isSubmittingScore,
            onSaveScore = { name -> viewModel.submitPlayerScore(name) },
            onDismiss = { viewModel.dismissNameInputDialog() }
        )
    }

    // Global Leaderboard Dialog (Displays top players' names alongside high scores)
    if (uiState.showLeaderboardDialog) {
        com.example.ui.dialogs.LeaderboardDialog(
            entries = leaderboardEntries,
            onDismiss = { viewModel.dismissLeaderboard() },
            onRestartGame = { viewModel.restartGame() }
        )
    }
}

