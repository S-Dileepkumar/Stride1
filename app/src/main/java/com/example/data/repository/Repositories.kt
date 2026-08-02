package com.example.data.repository

import com.example.data.db.SpeechSession
import com.example.data.db.SpeechSessionDao
import com.example.data.db.StepSession
import com.example.data.db.StepSessionDao
import com.example.data.db.UserSettingsDao
import com.example.data.db.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StepRepository(private val stepSessionDao: StepSessionDao) {
    val allSessions: Flow<List<StepSession>> = stepSessionDao.getAllSessions()

    fun getSessionsForDate(date: String): Flow<List<StepSession>> =
        stepSessionDao.getSessionsForDate(date)

    fun getTotalStepsForDate(date: String): Flow<Int> =
        stepSessionDao.getTotalStepsForDate(date).map { it ?: 0 }

    suspend fun insertSession(session: StepSession): Long =
        stepSessionDao.insertSession(session)

    suspend fun updateSession(session: StepSession) =
        stepSessionDao.updateSession(session)

    suspend fun deleteSession(id: Long) =
        stepSessionDao.deleteSessionById(id)
}

class SpeechRepository(private val speechSessionDao: SpeechSessionDao) {
    val allSessions: Flow<List<SpeechSession>> = speechSessionDao.getAllSessions()

    fun getSessionsForDate(date: String): Flow<List<SpeechSession>> =
        speechSessionDao.getSessionsForDate(date)

    suspend fun insertSession(session: SpeechSession): Long =
        speechSessionDao.insertSession(session)

    suspend fun deleteSession(id: Long) =
        speechSessionDao.deleteSessionById(id)
}

class SettingsRepository(private val userSettingsDao: UserSettingsDao) {
    val settings: Flow<UserSettingsEntity> = userSettingsDao.getSettings().map {
        it ?: UserSettingsEntity()
    }

    suspend fun saveSettings(settings: UserSettingsEntity) =
        userSettingsDao.saveSettings(settings)
}
