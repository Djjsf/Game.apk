package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {

    @Query("SELECT * FROM leaderboard ORDER BY score DESC, timestamp DESC LIMIT :limit")
    fun getTopScores(limit: Int = 50): Flow<List<LeaderboardEntry>>

    @Query("SELECT * FROM leaderboard ORDER BY score DESC LIMIT :limit")
    suspend fun getTopScoresSync(limit: Int = 50): List<LeaderboardEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LeaderboardEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LeaderboardEntry>)

    @Query("SELECT COUNT(*) FROM leaderboard")
    suspend fun getCount(): Int

    @Query("DELETE FROM leaderboard WHERE id = :id")
    suspend fun deleteById(id: Long)
}
