package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_sessions")
data class StepSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // format: YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val steps: Int,
    val durationSeconds: Int,
    val distanceKm: Double,
    val activityType: String = "Walking", // Walking, Running, Jogging
    val isSensorBased: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "speech_sessions")
data class SpeechSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // format: YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val topic: String,
    val transcript: String,
    val durationSeconds: Int = 60,
    val encouragingNote: String,
    val clarityScore: Int, // 0 to 100
    val clarityFeedback: String,
    val fillerWordCount: Int,
    val fillerWordsList: String, // JSON array string or comma separated
    val vocabularyVariety: String,
    val vocabularySuggestions: String,
    val toneConfidence: String,
    val actionableTip: String
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val strideLengthMeters: Double = 0.75,
    val dailyStepGoal: Int = 8000,
    val useMiles: Boolean = false,
    val isDarkMode: Boolean = false,
    val hasSeenOnboarding: Boolean = false
)
