package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.SpeechAnalysisResult
import com.example.data.db.SpeechSession
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SpeechCoral
import com.example.ui.theme.StreakGold
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeechTrainingScreen(
    liveTranscript: String,
    isListening: Boolean,
    isAnalyzing: Boolean,
    latestAnalysisResult: SpeechAnalysisResult?,
    speechHistory: List<SpeechSession>,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onUpdateTranscript: (String) -> Unit,
    onAnalyzeTranscript: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val randomTopicBank = remember {
        listOf(
            "1-Minute Pitch on Your Favorite Idea",
            "Atomic Habits & Daily Wins",
            "Ideal Morning Routine for High Performance",
            "Staying Calm Under High Pressure",
            "Core Personal Values That Guide You",
            "A Skill You'd Master Instantly",
            "Explain Artificial Intelligence to a 10-Year-Old",
            "A Book or Movie That Completely Changed Your Mind",
            "How to Overcome Creative Blocks",
            "Why Consistency Beats Intensity Every Time",
            "Your Favorite Way to Unwind and Disconnect",
            "A Major Challenge You Overcame Recently",
            "What Makes a Truly Inspiring Leader?",
            "If You Could Travel Anywhere Tomorrow...",
            "The Best Piece of Career Advice You Received",
            "Why Public Speaking is an Essential Superpower",
            "Turning Hard Failures into Valuable Growth",
            "Is Remote Work Better Than Office Work?",
            "How Technology Influences Modern Relationships",
            "The Future of Renewable Energy & Climate Action",
            "Should Financial Literacy Be Taught in Schools?",
            "What Does Happiness Mean to You?",
            "Describe Your Dream Passion Project",
            "How to Build Meaningful Long-term Friendships",
            "The Impact of Social Media on Daily Focus",
            "A Hobby You Love and Why Everyone Should Try It",
            "Space Exploration: Is It Worth the Cost?",
            "How to Give Constructive Feedback Effectively",
            "The Importance of Mindful Breathing & Meditation",
            "Lessons Learned from Your Favorite Mentor",
            "Why Curiosity Is More Important Than Talent"
        )
    }

    var selectedTopic by remember { mutableStateOf("1-Minute Pitch") }
    var customTopicInput by remember { mutableStateOf("") }

    // Session Phase: IDLE, PREP (10 Min), SPEECH (1-2 Min)
    var sessionPhase by remember { mutableStateOf("IDLE") } // "IDLE", "PREP", "SPEECH"
    var selectedSpeechDurationSeconds by remember { mutableIntStateOf(60) } // 60s or 120s (1 min or 2 min)

    // Timers
    var prepSecondsRemaining by remember { mutableIntStateOf(600) } // 10 minutes = 600s
    var isPrepTimerActive by remember { mutableStateOf(false) }
    var prepNotes by remember { mutableStateOf("") }

    var speechSecondsRemaining by remember { mutableIntStateOf(60) }
    var isSpeechTimerActive by remember { mutableStateOf(false) }

    val topics = listOf("1-Minute Pitch", "Atomic Habits", "Core Values", "Technology & AI", "Custom")

    // 10-Minute Prep Countdown Timer Logic
    LaunchedEffect(isPrepTimerActive, prepSecondsRemaining) {
        if (isPrepTimerActive && prepSecondsRemaining > 0) {
            delay(1000L)
            prepSecondsRemaining--
        } else if (isPrepTimerActive && prepSecondsRemaining == 0) {
            isPrepTimerActive = false
            // Auto transition to speech phase
            sessionPhase = "SPEECH"
            speechSecondsRemaining = selectedSpeechDurationSeconds
            isSpeechTimerActive = true
            onStartListening()
        }
    }

    // Speech Countdown Timer Logic (1 to 2 Min)
    LaunchedEffect(isSpeechTimerActive, speechSecondsRemaining) {
        if (isSpeechTimerActive && speechSecondsRemaining > 0) {
            delay(1000L)
            speechSecondsRemaining--
        } else if (isSpeechTimerActive && speechSecondsRemaining == 0) {
            isSpeechTimerActive = false
            onStopListening()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Clean Title Header
        item {
            Column {
                Text(
                    text = "Speech Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Random Topic Generator & Selector Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("speech_topic_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Topic Selection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hit for Random Topic Button
                    Button(
                        onClick = {
                            val filteredBank = randomTopicBank.filter { it != selectedTopic }
                            val newTopic = filteredBank.random()
                            selectedTopic = newTopic
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_random_topic"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Random Topic",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hit for Random Practice Topic",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Topic Chips Row
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        topics.forEach { topic ->
                            FilterChip(
                                selected = selectedTopic == topic,
                                onClick = { selectedTopic = topic },
                                label = { Text(topic) },
                                modifier = Modifier.testTag("topic_chip_$topic")
                            )
                        }
                    }

                    if (selectedTopic == "Custom") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customTopicInput,
                            onValueChange = { customTopicInput = it },
                            label = { Text("Enter Custom Topic") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_topic"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Interactive 2-Phase Practice Studio Card (10-Min Prep + 1-2 Min Speech)
        item {
            val activeTopicName = if (selectedTopic == "Custom" && customTopicInput.isNotBlank()) customTopicInput else selectedTopic

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("speech_recording_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Active Topic Display Badge
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SpeechCoral.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "TOPIC: ${activeTopicName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpeechCoral,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phase Toggle / Status Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { sessionPhase = "PREP" },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sessionPhase == "PREP" || sessionPhase == "IDLE") MaterialTheme.colorScheme.surface else Color.Transparent,
                                contentColor = if (sessionPhase == "PREP" || sessionPhase == "IDLE") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("10-Min Prep", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                sessionPhase = "SPEECH"
                                isPrepTimerActive = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sessionPhase == "SPEECH") MaterialTheme.colorScheme.surface else Color.Transparent,
                                contentColor = if (sessionPhase == "SPEECH") SpeechCoral else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Speech (1-2 Min)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Phase Transition Container with Subtle Slide & Fade Animation
                    AnimatedContent(
                        targetState = sessionPhase,
                        transitionSpec = {
                            if (targetState == "SPEECH") {
                                (slideInHorizontally { width -> width / 3 } + fadeIn(tween(250))) togetherWith
                                        (slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(250)))
                            } else {
                                (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(250))) togetherWith
                                        (slideOutHorizontally { width -> width / 3 } + fadeOut(tween(250)))
                            }
                        },
                        label = "SessionPhaseTransition"
                    ) { currentPhase ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (currentPhase == "PREP" || currentPhase == "IDLE") {
                                // SECTION 1: 10-MINUTE PREPARATION PHASE
                                val prepMinutes = prepSecondsRemaining / 60
                                val prepSecs = prepSecondsRemaining % 60
                                val prepProgress = prepSecondsRemaining / 600f

                                Box(
                                    modifier = Modifier.size(130.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.fillMaxSize(),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeWidth = 8.dp
                                    )
                                    CircularProgressIndicator(
                                        progress = { prepProgress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = StreakGold,
                                        strokeWidth = 8.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = String.format(Locale.getDefault(), "%02d:%02d", prepMinutes, prepSecs),
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isPrepTimerActive) "PREPARING" else "PREP TIMER",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StreakGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Prep Action Controls
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { isPrepTimerActive = !isPrepTimerActive },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("btn_toggle_prep_timer"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPrepTimerActive) MaterialTheme.colorScheme.secondary else StreakGold
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (isPrepTimerActive) "Pause Prep" else "Start 10-Min Prep",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            isPrepTimerActive = false
                                            sessionPhase = "SPEECH"
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("btn_skip_to_speech"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SpeechCoral),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Go to Speech >", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Prep Notes Editor
                                Text(
                                    text = "Preparation Notes & Speech Outline:",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.Start),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = prepNotes,
                                    onValueChange = { prepNotes = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .testTag("input_prep_notes"),
                                    placeholder = {
                                        Text("Jot down main points, key arguments, or outline...")
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                )
                            } else {
                                // SECTION 2: SPEECH RECOGNITION PHASE (1 to 2 Min)

                                // Speech Duration Selector (1 Min vs 2 Min)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Text("Speech Target:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    FilterChip(
                                        selected = selectedSpeechDurationSeconds == 60,
                                        onClick = {
                                            selectedSpeechDurationSeconds = 60
                                            if (!isSpeechTimerActive) speechSecondsRemaining = 60
                                        },
                                        label = { Text("1 Minute") }
                                    )
                                    FilterChip(
                                        selected = selectedSpeechDurationSeconds == 120,
                                        onClick = {
                                            selectedSpeechDurationSeconds = 120
                                            if (!isSpeechTimerActive) speechSecondsRemaining = 120
                                        },
                                        label = { Text("2 Minutes") }
                                    )
                                }

                                val speechMinutes = speechSecondsRemaining / 60
                                val speechSecs = speechSecondsRemaining % 60
                                val totalDurationFloat = selectedSpeechDurationSeconds.toFloat()
                                val progressFloat by animateFloatAsState(
                                    targetValue = speechSecondsRemaining / totalDurationFloat,
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                    label = "TimerProgress"
                                )

                                val dynamicRingColor by animateColorAsState(
                                    targetValue = when {
                                        speechSecondsRemaining > (selectedSpeechDurationSeconds / 2) -> EmeraldPrimary
                                        speechSecondsRemaining > (selectedSpeechDurationSeconds / 4) -> StreakGold
                                        else -> SpeechCoral
                                    },
                                    label = "RingColor"
                                )

                                val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
                                val pulseScale by infiniteTransition.animateFloat(
                                    initialValue = 1.0f,
                                    targetValue = if (isSpeechTimerActive && speechSecondsRemaining <= 10) 1.06f else 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(500),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "PulseScale"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .scale(pulseScale),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.fillMaxSize(),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeWidth = 8.dp
                                    )

                                    CircularProgressIndicator(
                                        progress = { progressFloat },
                                        modifier = Modifier.fillMaxSize(),
                                        color = dynamicRingColor,
                                        strokeWidth = 8.dp
                                    )

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = String.format(Locale.getDefault(), "%d:%02d", speechMinutes, speechSecs),
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSpeechTimerActive) dynamicRingColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isSpeechTimerActive) "RECORDING" else "SPEECH TIMER",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Controls Row
                                if (!isSpeechTimerActive) {
                                    Button(
                                        onClick = {
                                            isSpeechTimerActive = true
                                            speechSecondsRemaining = selectedSpeechDurationSeconds
                                            onStartListening()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("btn_start_speech_practice"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SpeechCoral),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = "Mic")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Start Speech Recording (${selectedSpeechDurationSeconds / 60} Min)", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            isSpeechTimerActive = false
                                            onStopListening()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("btn_stop_speech_practice"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Finish Speech Session", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Transcript Text Field
                                Text(
                                    text = "Transcript (Live Speech or Text):",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.Start),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = liveTranscript,
                                    onValueChange = onUpdateTranscript,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("input_speech_transcript"),
                                    placeholder = {
                                        Text("Speak into mic or type your speech here...")
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Gemini AI Analysis Trigger Button
                                Button(
                                    onClick = {
                                        if (liveTranscript.isNotBlank()) {
                                            onAnalyzeTranscript(activeTopicName, liveTranscript)
                                        }
                                    },
                                    enabled = liveTranscript.isNotBlank() && !isAnalyzing,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("btn_analyze_with_gemini"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    if (isAnalyzing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Analyzing with Gemini AI...")
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Analyze Speech with Gemini", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini Analysis Result Card
        latestAnalysisResult?.let { result ->
            item {
                AnalysisFeedbackResultCard(
                    result = result,
                    topic = if (selectedTopic == "Custom") customTopicInput else selectedTopic
                )
            }
        }

        // Speech Practice History Log
        item {
            Text(
                text = "Past Speech Sessions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (speechHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved speech sessions yet. Complete your first 1-minute practice above!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(speechHistory) { session ->
                SpeechHistoryItemCard(session = session)
            }
        }
    }
}

@Composable
fun AnalysisFeedbackResultCard(
    result: SpeechAnalysisResult,
    topic: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speech_analysis_result_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Encouraging Header
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Great Practice Session!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = result.encouragingNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Metric Pills: Clarity Score & Filler Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Clarity Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${result.clarityScore}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Filler Words", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${result.fillerWordCount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = SpeechCoral)
                    }
                }
            }

            // Filler Word List
            if (result.fillerWordsList.isNotEmpty()) {
                Column {
                    Text(
                        text = "Detected Filler Words:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        result.fillerWordsList.take(6).forEach { word ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(word, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Vocabulary Suggestions
            Column {
                Text(
                    text = "Vocabulary & Variety:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${result.vocabularyVariety} ${result.vocabularySuggestions}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Tone & Confidence
            Column {
                Text(
                    text = "Tone & Confidence:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.toneConfidence,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Actionable Tip Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StreakGold.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Tip",
                        tint = StreakGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tip for Next Session:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StreakGold
                        )
                        Text(
                            text = result.actionableTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeechHistoryItemCard(session: SpeechSession) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speech_session_${session.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.topic,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${session.date} • Clarity: ${session.clarityScore}% • Fillers: ${session.fillerWordCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Details"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Transcript:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "\"${session.transcript}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tip: ${session.actionableTip}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }
        }
    }
}

