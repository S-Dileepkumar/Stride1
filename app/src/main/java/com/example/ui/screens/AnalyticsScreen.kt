package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.SpeechSession
import com.example.data.db.StepSession
import com.example.ui.components.CalendarHeatmap
import com.example.ui.components.DailyStepData
import com.example.ui.components.FillerTrendChart
import com.example.ui.components.StepBarChart
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SpeechCoral
import com.example.ui.theme.StreakGold
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    dailyStepsMap: Map<String, Int>,
    dailyGoal: Int,
    weeklyStepData: List<DailyStepData>,
    speechSessions: List<SpeechSession>,
    currentStepStreak: Int,
    bestStepStreak: Int,
    currentSpeechStreak: Int,
    modifier: Modifier = Modifier
) {
    var selectedHeatmapDate by remember { mutableStateOf<String?>(null) }

    val fillerTrendData = remember(speechSessions) {
        speechSessions.reversed().map { session ->
            Pair(session.date, session.fillerWordCount)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Analytics & Streaks",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Habit heatmaps, step trends & speech progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Streaks Header Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("step_streak_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StreakGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Step Streak", tint = StreakGold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Step Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currentStepStreak Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("speech_streak_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SpeechCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = "Speech Streak", tint = SpeechCoral)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Speech Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$currentSpeechStreak Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Calendar Heatmap View
        item {
            CalendarHeatmap(
                dailyStepsMap = dailyStepsMap,
                dailyGoal = dailyGoal,
                currentStreak = currentStepStreak,
                bestStreak = bestStepStreak,
                onDateSelected = { date -> selectedHeatmapDate = date }
            )
        }

        // Selected Date Summary Banner
        selectedHeatmapDate?.let { dateStr ->
            item {
                val stepsOnDate = dailyStepsMap[dateStr] ?: 0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Selected Date: $dateStr", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Total Steps: $stepsOnDate steps", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (stepsOnDate >= dailyGoal) {
                            Text("Goal Hit! 🎉", style = MaterialTheme.typography.titleSmall, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 7-Day Step Bar Chart
        item {
            StepBarChart(
                weeklyData = weeklyStepData,
                dailyGoal = dailyGoal
            )
        }

        // Speech Filler Word Trend Line Chart
        item {
            FillerTrendChart(
                fillerData = fillerTrendData
            )
        }
    }
}
