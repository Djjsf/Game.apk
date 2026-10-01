package com.example.data.repository

import android.util.Log
import com.example.data.local.LeaderboardDao
import com.example.data.local.LeaderboardEntry
import com.example.data.remote.LeaderboardApiService
import com.example.data.remote.ScoreData
import com.example.data.remote.ScorePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LeaderboardRepository(
    private val dao: LeaderboardDao,
    private val api: LeaderboardApiService = LeaderboardApiService.create()
) {
    val topScores: Flow<List<LeaderboardEntry>> = dao.getTopScores(limit = 100)

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count == 0) {
            val initialChampions = listOf(
                LeaderboardEntry(playerName = "SlitherKing", score = 380, burgersEaten = 32, wallBounces = 18),
                LeaderboardEntry(playerName = "NeonViper", score = 290, burgersEaten = 25, wallBounces = 24),
                LeaderboardEntry(playerName = "BurgerBeast", score = 230, burgersEaten = 21, wallBounces = 14),
                LeaderboardEntry(playerName = "PythonPro", score = 180, burgersEaten = 16, wallBounces = 19),
                LeaderboardEntry(playerName = "PixelCobra", score = 140, burgersEaten = 12, wallBounces = 9),
                LeaderboardEntry(playerName = "BounceMaster", score = 110, burgersEaten = 9, wallBounces = 28),
                LeaderboardEntry(playerName = "Mamba99", score = 80, burgersEaten = 7, wallBounces = 11)
            )
            dao.insertAll(initialChampions)
        }
    }

    suspend fun savePlayerScore(
        playerName: String,
        score: Int,
        burgersEaten: Int,
        wallBounces: Int
    ): LeaderboardEntry = withContext(Dispatchers.IO) {
        val entry = LeaderboardEntry(
            playerName = playerName.trim().ifEmpty { "Anonymous Snake" },
            score = score,
            burgersEaten = burgersEaten,
            wallBounces = wallBounces,
            isLocalPlayer = true
        )
        val id = dao.insertEntry(entry)
        val saved = entry.copy(id = id)

        // Asynchronously sync to global remote API
        try {
            val payload = ScorePayload(
                playerName = saved.playerName,
                scoreData = ScoreData(
                    score = saved.score,
                    burgers = saved.burgersEaten,
                    bounces = saved.wallBounces,
                    timestamp = saved.timestamp
                )
            )
            api.submitGlobalScore(payload)
        } catch (e: Exception) {
            Log.d("LeaderboardRepo", "Network sync deferred: ${e.message}")
        }

        saved
    }

    suspend fun fetchGlobalLeaderboard() = withContext(Dispatchers.IO) {
        try {
            val remoteItems = api.getGlobalScores()
            val validEntries = remoteItems.mapNotNull { item ->
                val data = item.data ?: return@mapNotNull null
                val name = item.name ?: "Player"
                if (data.score > 0) {
                    LeaderboardEntry(
                        playerName = name,
                        score = data.score,
                        burgersEaten = data.burgers,
                        wallBounces = data.bounces,
                        timestamp = data.timestamp,
                        isLocalPlayer = false
                    )
                } else null
            }
            if (validEntries.isNotEmpty()) {
                dao.insertAll(validEntries)
            }
        } catch (e: Exception) {
            Log.d("LeaderboardRepo", "Failed to fetch remote scores: ${e.message}")
        }
    }
}
