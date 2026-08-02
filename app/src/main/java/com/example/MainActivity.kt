package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.ai.GeminiSpeechAnalyzer
import com.example.ai.SpeechAnalysisResult
import com.example.data.db.AppDatabase
import com.example.data.db.SpeechSession
import com.example.data.db.StepSession
import com.example.data.db.UserSettingsEntity
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SpeechRepository
import com.example.data.repository.StepRepository
import com.example.sensor.StepDetectorSensorManager
import com.example.speech.SpeechToTextManager
import com.example.audio.OfflineMusicAudioEngine
import com.example.ui.components.DailyStepData
import com.example.ui.components.NavigationBottomBar
import com.example.ui.components.OfflineMusicMiniPlayerBar
import com.example.ui.components.OfflineMusicPlayerDialog
import com.example.ui.components.OnboardingTutorialDialog
import com.example.ui.components.ScreenTab
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.MusicStudioScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpeechTrainingScreen
import com.example.ui.screens.StepsTrackerScreen
import com.example.ui.theme.StrideAndSpeakTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var stepRepository: StepRepository
    private lateinit var speechRepository: SpeechRepository
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var sensorManager: StepDetectorSensorManager
    private lateinit var speechToTextManager: SpeechToTextManager
    private val geminiSpeechAnalyzer = GeminiSpeechAnalyzer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(this)
        stepRepository = StepRepository(database.stepSessionDao())
        speechRepository = SpeechRepository(database.speechSessionDao())
        settingsRepository = SettingsRepository(database.userSettingsDao())

        sensorManager = StepDetectorSensorManager(this)
        speechToTextManager = SpeechToTextManager(this)

        setContent {
            val userSettings by settingsRepository.settings.collectAsState(initial = UserSettingsEntity())
            StrideAndSpeakTheme(darkTheme = userSettings.isDarkMode) {
                StrideAndSpeakApp(
                    stepRepository = stepRepository,
                    speechRepository = speechRepository,
                    settingsRepository = settingsRepository,
                    sensorManager = sensorManager,
                    speechToTextManager = speechToTextManager,
                    geminiSpeechAnalyzer = geminiSpeechAnalyzer
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.stopTracking()
        speechToTextManager.destroy()
    }
}

@Composable
fun StrideAndSpeakApp(
    stepRepository: StepRepository,
    speechRepository: SpeechRepository,
    settingsRepository: SettingsRepository,
    sensorManager: StepDetectorSensorManager,
    speechToTextManager: SpeechToTextManager,
    geminiSpeechAnalyzer: GeminiSpeechAnalyzer
) {
    var currentTab by remember { mutableStateOf(ScreenTab.STEPS) }
    val scope = rememberCoroutineScope()

    // Database flows
    val allStepSessions by stepRepository.allSessions.collectAsState(initial = emptyList())
    val allSpeechSessions by speechRepository.allSessions.collectAsState(initial = emptyList())
    val userSettings by settingsRepository.settings.collectAsState(initial = UserSettingsEntity())

    val todayStr = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }
    val todaySessions = remember(allStepSessions, todayStr) {
        allStepSessions.filter { it.date == todayStr }
    }
    val todaySavedSteps = remember(todaySessions) { todaySessions.sumOf { it.steps } }

    // Live Sensor & Tracker States
    val liveSensorSteps by sensorManager.stepCount.collectAsState()
    val isSensorActive by sensorManager.isSensorActive.collectAsState()
    val isAccelerometerAvailable by sensorManager.isAccelerometerAvailable.collectAsState()

    var isSessionActive by remember { mutableStateOf(false) }
    var isSessionPaused by remember { mutableStateOf(false) }

    val currentTotalTodaySteps = todaySavedSteps + (if (isSessionActive) liveSensorSteps else 0)

    // Speech Studio States
    val liveTranscript by speechToTextManager.transcript.collectAsState()
    val isListening by speechToTextManager.isListening.collectAsState()
    var isAnalyzingSpeech by remember { mutableStateOf(false) }
    var latestSpeechResult by remember { mutableStateOf<SpeechAnalysisResult?>(null) }

    // Offline Music Audio Engine States
    val musicAudioEngine = remember { OfflineMusicAudioEngine() }
    val musicPlayerState by musicAudioEngine.playerState.collectAsState()
    var showExpandedMusicPlayer by remember { mutableStateOf(false) }

    // Walkthrough Tutorial Dialog State
    var showOnboardingTutorial by remember { mutableStateOf(false) }
    LaunchedEffect(userSettings.hasSeenOnboarding) {
        if (!userSettings.hasSeenOnboarding) {
            showOnboardingTutorial = true
        }
    }

    // Analytics Derived Data
    val dailyStepsMap = remember(allStepSessions) {
        allStepSessions.groupBy { it.date }
            .mapValues { entry -> entry.value.sumOf { it.steps } }
    }

    val weeklyStepData = remember(allStepSessions) {
        val formatter = DateTimeFormatter.ofPattern("E")
        (6 downTo 0).map { i ->
            val date = LocalDate.now().minusDays(i.toLong())
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayLabel = date.format(formatter)
            val steps = dailyStepsMap[dateStr] ?: 0
            DailyStepData(dateLabel = dayLabel, steps = steps)
        }
    }

    // Streaks Calculation
    val currentStepStreak = remember(dailyStepsMap, userSettings.dailyStepGoal) {
        var streak = 0
        var checkDate = LocalDate.now()
        while (true) {
            val dateStr = checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val steps = dailyStepsMap[dateStr] ?: 0
            if (steps >= userSettings.dailyStepGoal) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (checkDate == LocalDate.now()) {
                // If today hasn't reached goal yet, check yesterday
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        streak
    }

    val bestStepStreak = remember(currentStepStreak) {
        maxOf(currentStepStreak, 0)
    }

    val currentSpeechStreak = remember(allSpeechSessions) {
        val datesWithSpeech = allSpeechSessions.map { it.date }.toSet()
        var streak = 0
        var checkDate = LocalDate.now()
        while (true) {
            val dateStr = checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            if (datesWithSpeech.contains(dateStr)) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (checkDate == LocalDate.now()) {
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        streak
    }

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            androidx.compose.foundation.layout.Column {
                OfflineMusicMiniPlayerBar(
                    playerState = musicPlayerState,
                    onTogglePlayPause = { musicAudioEngine.togglePlayPause(context) },
                    onExpandPlayer = { showExpandedMusicPlayer = true }
                )
                NavigationBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (currentTab) {
            ScreenTab.STEPS -> {
                StepsTrackerScreen(
                    currentLiveSteps = liveSensorSteps,
                    isSessionActive = isSessionActive,
                    isSessionPaused = isSessionPaused,
                    isSensorAvailable = isAccelerometerAvailable,
                    todayTotalSteps = currentTotalTodaySteps,
                    dailyGoal = userSettings.dailyStepGoal,
                    strideLengthMeters = userSettings.strideLengthMeters,
                    useMiles = userSettings.useMiles,
                    recentSessions = allStepSessions,
                    onStartSession = { activity ->
                        isSessionActive = true
                        isSessionPaused = false
                        sensorManager.startTracking()
                    },
                    onPauseSession = {
                        isSessionPaused = true
                        sensorManager.pauseTracking()
                    },
                    onResumeSession = {
                        isSessionPaused = false
                        sensorManager.resumeTracking()
                    },
                    onStopAndSaveSession = { steps, duration, distance, activity ->
                        isSessionActive = false
                        isSessionPaused = false
                        sensorManager.stopTracking()

                        if (steps > 0) {
                            scope.launch(Dispatchers.IO) {
                                stepRepository.insertSession(
                                    StepSession(
                                        date = todayStr,
                                        steps = steps,
                                        durationSeconds = duration,
                                        distanceKm = distance,
                                        activityType = activity,
                                        isSensorBased = true
                                    )
                                )
                            }
                        }
                    },
                    onSimulateStep = {
                        sensorManager.incrementStepCount(5)
                    },
                    onAddManualSession = { steps, activity ->
                        scope.launch(Dispatchers.IO) {
                            val dist = (steps * userSettings.strideLengthMeters) / 1000.0
                            stepRepository.insertSession(
                                StepSession(
                                    date = todayStr,
                                    steps = steps,
                                    durationSeconds = (steps / 1.8).toInt(),
                                    distanceKm = dist,
                                    activityType = activity,
                                    isSensorBased = false,
                                    notes = "Manual step entry"
                                )
                            )
                        }
                    },
                    onEditSession = { session ->
                        scope.launch(Dispatchers.IO) {
                            stepRepository.updateSession(session)
                        }
                    },
                    modifier = screenModifier
                )
            }

            ScreenTab.SPEECH -> {
                SpeechTrainingScreen(
                    liveTranscript = liveTranscript,
                    isListening = isListening,
                    isAnalyzing = isAnalyzingSpeech,
                    latestAnalysisResult = latestSpeechResult,
                    speechHistory = allSpeechSessions,
                    onStartListening = {
                        speechToTextManager.clearTranscript()
                        speechToTextManager.startListening()
                    },
                    onStopListening = {
                        speechToTextManager.stopListening()
                    },
                    onUpdateTranscript = { newText ->
                        speechToTextManager.updateTranscriptManually(newText)
                    },
                    onAnalyzeTranscript = { topic, transcript ->
                        isAnalyzingSpeech = true
                        scope.launch {
                            val result = geminiSpeechAnalyzer.analyzeTranscript(topic, transcript)
                            latestSpeechResult = result
                            isAnalyzingSpeech = false

                            // Save to Room DB
                            withContext(Dispatchers.IO) {
                                speechRepository.insertSession(
                                    SpeechSession(
                                        date = todayStr,
                                        topic = topic,
                                        transcript = transcript,
                                        durationSeconds = 60,
                                        encouragingNote = result.encouragingNote,
                                        clarityScore = result.clarityScore,
                                        clarityFeedback = result.clarityFeedback,
                                        fillerWordCount = result.fillerWordCount,
                                        fillerWordsList = result.fillerWordsList.joinToString(", "),
                                        vocabularyVariety = result.vocabularyVariety,
                                        vocabularySuggestions = result.vocabularySuggestions,
                                        toneConfidence = result.toneConfidence,
                                        actionableTip = result.actionableTip
                                    )
                                )
                            }
                        }
                    },
                    modifier = screenModifier
                )
            }

            ScreenTab.MUSIC -> {
                MusicStudioScreen(
                    audioEngine = musicAudioEngine,
                    playerState = musicPlayerState,
                    onExpandPlayerDialog = { showExpandedMusicPlayer = true },
                    modifier = screenModifier
                )
            }

            ScreenTab.ANALYTICS -> {
                AnalyticsScreen(
                    dailyStepsMap = dailyStepsMap,
                    dailyGoal = userSettings.dailyStepGoal,
                    weeklyStepData = weeklyStepData,
                    speechSessions = allSpeechSessions,
                    currentStepStreak = currentStepStreak,
                    bestStepStreak = bestStepStreak,
                    currentSpeechStreak = currentSpeechStreak,
                    modifier = screenModifier
                )
            }

            ScreenTab.SETTINGS -> {
                SettingsScreen(
                    userSettings = userSettings,
                    onSaveSettings = { updated ->
                        scope.launch(Dispatchers.IO) {
                            settingsRepository.saveSettings(updated)
                        }
                    },
                    onReplayTutorial = {
                        showOnboardingTutorial = true
                    },
                    modifier = screenModifier
                )
            }
        }

        if (showExpandedMusicPlayer) {
            OfflineMusicPlayerDialog(
                playerState = musicPlayerState,
                audioEngine = musicAudioEngine,
                onDismiss = { showExpandedMusicPlayer = false }
            )
        }

        if (showOnboardingTutorial) {
            OnboardingTutorialDialog(
                onDismiss = {
                    showOnboardingTutorial = false
                    scope.launch(Dispatchers.IO) {
                        settingsRepository.saveSettings(userSettings.copy(hasSeenOnboarding = true))
                    }
                }
            )
        }
    }
}
