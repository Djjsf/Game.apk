package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LeaderboardEntry
import com.example.data.repository.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class ScoreSummary(
    val score: Int,
    val burgers: Int,
    val bounces: Int
)

data class GameUiState(
    val score: Int = 0,
    val highScore: Int = 0,
    val burgersEaten: Int = 0,
    val bounces: Int = 0,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val tailCollisionEnabled: Boolean = true, // Game Over triggers ONLY on self-collision
    val boostActive: Boolean = false,
    val lastDirection: Direction = Direction.RIGHT,
    val playerName: String = "SlitherNinja",
    val showNameInputDialog: Boolean = false,
    val showLeaderboardDialog: Boolean = false,
    val pendingScoreToSave: ScoreSummary? = null,
    val isSubmittingScore: Boolean = false
)

class SnakeGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("slither_snake_prefs", Context.MODE_PRIVATE)
    private val db = AppDatabase.getInstance(application)
    private val repository = LeaderboardRepository(db.leaderboardDao())

    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = repository.topScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(
        GameUiState(
            highScore = prefs.getInt("high_score", 0),
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            tailCollisionEnabled = true, // Always true per user specification
            playerName = prefs.getString("player_name", "SlitherNinja") ?: "SlitherNinja"
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var jsExecutor: ((String) -> Unit)? = null

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
            repository.fetchGlobalLeaderboard()
        }
    }

    fun attachJsExecutor(executor: (String) -> Unit) {
        this.jsExecutor = executor
    }

    fun detachJsExecutor() {
        this.jsExecutor = null
    }

    fun sendDirection(direction: Direction) {
        _uiState.update { it.copy(lastDirection = direction) }
        jsExecutor?.invoke("window.game && window.game.setDirection('${direction.name}');")
    }

    fun toggleBoost(enable: Boolean? = null) {
        val newState = enable ?: !_uiState.value.boostActive
        _uiState.update { it.copy(boostActive = newState) }
        jsExecutor?.invoke("window.game && window.game.setBoost($newState);")
    }

    fun togglePause() {
        val newPauseState = !_uiState.value.isPaused
        _uiState.update { it.copy(isPaused = newPauseState) }
        jsExecutor?.invoke("window.game && window.game.togglePause();")
    }

    fun restartGame() {
        _uiState.update {
            it.copy(
                score = 0,
                burgersEaten = 0,
                bounces = 0,
                isPaused = false,
                isGameOver = false,
                boostActive = false,
                lastDirection = Direction.RIGHT,
                showNameInputDialog = false,
                pendingScoreToSave = null
            )
        }
        jsExecutor?.invoke("window.game && window.game.restartGame();")
    }

    fun toggleSound() {
        val newSound = !_uiState.value.soundEnabled
        _uiState.update { it.copy(soundEnabled = newSound) }
        prefs.edit().putBoolean("sound_enabled", newSound).apply()
        jsExecutor?.invoke("window.game && window.game.toggleSound($newSound);")
    }

    fun toggleTailCollision() {
        // Kept for backward compatibility if needed, but defaults to true
        val newCollision = !_uiState.value.tailCollisionEnabled
        _uiState.update { it.copy(tailCollisionEnabled = newCollision) }
        prefs.edit().putBoolean("tail_collision", newCollision).apply()
        jsExecutor?.invoke("window.game && window.game.toggleTailCollision($newCollision);")
    }

    fun updatePlayerName(newName: String) {
        val trimmed = newName.trim().take(20)
        _uiState.update { it.copy(playerName = trimmed) }
        prefs.edit().putString("player_name", trimmed).apply()
    }

    fun openNameInputDialog(score: Int? = null, burgers: Int? = null, bounces: Int? = null) {
        val currentScore = score ?: _uiState.value.score
        val currentBurgers = burgers ?: _uiState.value.burgersEaten
        val currentBounces = bounces ?: _uiState.value.bounces
        _uiState.update {
            it.copy(
                showNameInputDialog = true,
                pendingScoreToSave = ScoreSummary(currentScore, currentBurgers, currentBounces)
            )
        }
    }

    fun dismissNameInputDialog() {
        _uiState.update { it.copy(showNameInputDialog = false) }
    }

    fun submitPlayerScore(name: String) {
        val trimmedName = name.trim().ifEmpty { "SlitherNinja" }
        updatePlayerName(trimmedName)

        val pending = _uiState.value.pendingScoreToSave
            ?: ScoreSummary(_uiState.value.score, _uiState.value.burgersEaten, _uiState.value.bounces)

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingScore = true) }
            repository.savePlayerScore(
                playerName = trimmedName,
                score = pending.score,
                burgersEaten = pending.burgers,
                wallBounces = pending.bounces
            )
            _uiState.update {
                it.copy(
                    isSubmittingScore = false,
                    showNameInputDialog = false,
                    showLeaderboardDialog = true
                )
            }
        }
    }

    fun openLeaderboard() {
        _uiState.update { it.copy(showLeaderboardDialog = true) }
        viewModelScope.launch {
            repository.fetchGlobalLeaderboard()
        }
    }

    fun dismissLeaderboard() {
        _uiState.update { it.copy(showLeaderboardDialog = false) }
    }

    fun onGameOverFromBridge(score: Int, highScore: Int, burgers: Int, bounces: Int) {
        _uiState.update {
            it.copy(
                isGameOver = true,
                score = score,
                burgersEaten = burgers,
                bounces = bounces
            )
        }
        // Prompt for player name input to save score if score > 0
        if (score > 0) {
            openNameInputDialog(score, burgers, bounces)
        }
    }

    fun onScoreUpdatedFromBridge(
        score: Int,
        highScore: Int,
        burgersEaten: Int,
        bounces: Int,
        isPaused: Boolean,
        isGameOver: Boolean
    ) {
        val savedHighScore = maxOf(highScore, _uiState.value.highScore, score)
        if (savedHighScore > prefs.getInt("high_score", 0)) {
            prefs.edit().putInt("high_score", savedHighScore).apply()
        }

        _uiState.update {
            it.copy(
                score = score,
                highScore = savedHighScore,
                burgersEaten = burgersEaten,
                bounces = bounces,
                isPaused = isPaused,
                isGameOver = isGameOver
            )
        }
    }
}
