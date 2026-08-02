package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.StepSession
import com.example.ui.components.MotionPermissionCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SpeechCoral
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StepsTrackerScreen(
    currentLiveSteps: Int,
    isSessionActive: Boolean,
    isSessionPaused: Boolean,
    isSensorAvailable: Boolean,
    todayTotalSteps: Int,
    dailyGoal: Int,
    strideLengthMeters: Double,
    useMiles: Boolean,
    recentSessions: List<StepSession>,
    onStartSession: (String) -> Unit,
    onPauseSession: () -> Unit,
    onResumeSession: () -> Unit,
    onStopAndSaveSession: (Int, Int, Double, String) -> Unit,
    onSimulateStep: () -> Unit,
    onAddManualSession: (Int, String) -> Unit,
    onEditSession: (StepSession) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedActivityType by remember { mutableStateOf("Walking") }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var showManualModal by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<StepSession?>(null) }
    var showPermissionCard by remember { mutableStateOf(true) }

    // Live session timer
    LaunchedEffect(isSessionActive, isSessionPaused) {
        if (isSessionActive && !isSessionPaused) {
            while (true) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    if (!isSessionActive) {
        elapsedSeconds = 0L
    }

    // Calculated session metrics
    val distanceKm = (currentLiveSteps * strideLengthMeters) / 1000.0
    val distanceDisplay = if (useMiles) {
        String.format(Locale.getDefault(), "%.2f mi", distanceKm * 0.621371)
    } else {
        String.format(Locale.getDefault(), "%.2f km", distanceKm)
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val durationText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner / Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Walk & Run Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Button(
                    onClick = { showManualModal = true },
                    modifier = Modifier.testTag("btn_quick_add_steps")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Steps")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual Log")
                }
            }
        }

        // Optional Sensor Permission Card
        if (showPermissionCard && !isSessionActive) {
            item {
                MotionPermissionCard(
                    isSensorAvailable = isSensorAvailable,
                    onEnableMotionAccess = { showPermissionCard = false },
                    onUseManualFallback = { showPermissionCard = false }
                )
            }
        }

        // Today's Daily Step Goal Summary Ring / Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_step_summary_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = "Steps",
                                    tint = EmeraldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Today's Total Steps",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$todayTotalSteps / $dailyGoal steps",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val progressRatio = (todayTotalSteps.toFloat() / dailyGoal).coerceIn(0f, 1f)
                        Text(
                            text = "${(progressRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (todayTotalSteps.toFloat() / dailyGoal).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = EmeraldPrimary,
                        trackColor = EmeraldPrimary.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Active Session Card (Live Step Counter)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_session_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSessionActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Activity Type Chips
                    if (!isSessionActive) {
                        Text(
                            text = "Select Activity Type",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Walking", "Jogging", "Running").forEach { type ->
                                FilterChip(
                                    selected = selectedActivityType == type,
                                    onClick = { selectedActivityType = type },
                                    label = { Text(type) },
                                    leadingIcon = {
                                        Icon(
                                            if (type == "Running") Icons.Default.DirectionsRun else Icons.Default.DirectionsWalk,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("chip_$type")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Live Counter Display
                    Text(
                        text = if (isSessionActive) "ACTIVE $selectedActivityType SESSION" else "READY TO TRACK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$currentLiveSteps",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 54.sp
                    )

                    Text(
                        text = "STEPS DETECTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Distance & Timer Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Distance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = distanceDisplay,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Duration",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = durationText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Est. Calories",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(currentLiveSteps * 0.04).toInt()} kcal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Controls Row (Start / Pause / Resume / Stop)
                    if (!isSessionActive) {
                        Button(
                            onClick = { onStartSession(selectedActivityType) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_start_tracking"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Track Walk / Run", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!isSessionPaused) {
                                OutlinedButton(
                                    onClick = onPauseSession,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_pause_session")
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pause")
                                }
                            } else {
                                Button(
                                    onClick = onResumeSession,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_resume_session")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Resume")
                                }
                            }

                            Button(
                                onClick = {
                                    onStopAndSaveSession(
                                        currentLiveSteps,
                                        elapsedSeconds.toInt(),
                                        distanceKm,
                                        selectedActivityType
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_stop_and_save_session"),
                                colors = ButtonDefaults.buttonColors(containerColor = SpeechCoral)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop & Save")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Test step simulation button for browser preview environment
                        OutlinedButton(
                            onClick = onSimulateStep,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_simulate_step")
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = "Footstep")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Footstep (+5 steps)")
                        }
                    }
                }
            }
        }

        // Recent Walk / Run Sessions Log
        item {
            Text(
                text = "Recent Walk & Run Sessions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (recentSessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved sessions yet. Tap 'Track Walk / Run' to begin!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentSessions) { session ->
                SessionItemCard(
                    session = session,
                    useMiles = useMiles,
                    onEditClick = { sessionToEdit = session }
                )
            }
        }
    }

    // Modal Dialog for Manual Add Session
    if (showManualModal) {
        ManualAddStepsDialog(
            onDismiss = { showManualModal = false },
            onConfirm = { steps, activity ->
                onAddManualSession(steps, activity)
                showManualModal = false
            }
        )
    }

    // Modal Dialog for Editing Session
    sessionToEdit?.let { session ->
        EditSessionDialog(
            session = session,
            onDismiss = { sessionToEdit = null },
            onSave = { updatedSession ->
                onEditSession(updatedSession)
                sessionToEdit = null
            }
        )
    }
}

@Composable
fun SessionItemCard(
    session: StepSession,
    useMiles: Boolean,
    onEditClick: () -> Unit
) {
    val distDisplay = if (useMiles) {
        String.format(Locale.getDefault(), "%.2f mi", session.distanceKm * 0.621371)
    } else {
        String.format(Locale.getDefault(), "%.2f km", session.distanceKm)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_item_${session.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (session.activityType == "Running") Icons.Default.DirectionsRun else Icons.Default.DirectionsWalk,
                        contentDescription = session.activityType,
                        tint = EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${session.steps} steps • ${session.activityType}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${session.date} • $distDisplay • ${session.durationSeconds / 60}m ${session.durationSeconds % 60}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Session", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ManualAddStepsDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var stepText by remember { mutableStateOf("1500") }
    var selectedActivity by remember { mutableStateOf("Walking") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Manual Steps") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = stepText,
                    onValueChange = { stepText = it.filter { char -> char.isDigit() } },
                    label = { Text("Number of Steps") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_steps")
                )

                Text("Activity Type:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Walking", "Jogging", "Running").forEach { act ->
                        FilterChip(
                            selected = selectedActivity == act,
                            onClick = { selectedActivity = act },
                            label = { Text(act) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val steps = stepText.toIntOrNull() ?: 0
                    if (steps > 0) {
                        onConfirm(steps, selectedActivity)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_manual_steps")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditSessionDialog(
    session: StepSession,
    onDismiss: () -> Unit,
    onSave: (StepSession) -> Unit
) {
    var stepText by remember { mutableStateOf(session.steps.toString()) }
    var notesText by remember { mutableStateOf(session.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Correct Session Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = stepText,
                    onValueChange = { stepText = it.filter { char -> char.isDigit() } },
                    label = { Text("Step Count") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Session Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newSteps = stepText.toIntOrNull() ?: session.steps
                    val updated = session.copy(
                        steps = newSteps,
                        notes = notesText
                    )
                    onSave(updated)
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
