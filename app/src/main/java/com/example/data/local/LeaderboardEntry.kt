package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerName: String,
    val score: Int,
    val burgersEaten: Int,
    val wallBounces: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isLocalPlayer: Boolean = false
)
