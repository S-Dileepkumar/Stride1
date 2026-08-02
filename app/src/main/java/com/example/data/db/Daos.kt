package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StepSessionDao {
    @Query("SELECT * FROM step_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StepSession>>

    @Query("SELECT * FROM step_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getSessionsForDate(date: String): Flow<List<StepSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StepSession): Long

    @Update
    suspend fun updateSession(session: StepSession)

    @Query("DELETE FROM step_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT SUM(steps) FROM step_sessions WHERE date = :date")
    fun getTotalStepsForDate(date: String): Flow<Int?>

    @Query("SELECT * FROM step_sessions WHERE timestamp >= :sinceTimestamp")
    fun getSessionsSince(sinceTimestamp: Long): Flow<List<StepSession>>
}

@Dao
interface SpeechSessionDao {
    @Query("SELECT * FROM speech_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SpeechSession>>

    @Query("SELECT * FROM speech_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getSessionsForDate(date: String): Flow<List<SpeechSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SpeechSession): Long

    @Query("DELETE FROM speech_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettingsEntity)
}
