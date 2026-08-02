package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SpeechCoral
import com.example.ui.theme.StreakGold

data class TutorialSlide(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val featurePoints: List<String>
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingTutorialDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            TutorialSlide(
                title = "Welcome to Stride & Speak",
                subtitle = "Physical Movement meets Vocal Confidence",
                description = "Stride & Speak helps you build twin daily habits: physical activity through live step tracking and speech eloquence through 1-minute practice sessions with Gemini AI feedback.",
                icon = Icons.Default.DirectionsWalk,
                iconTint = EmeraldPrimary,
                featurePoints = listOf(
                    "Track steps & active sessions effortlessly",
                    "Practice 1-minute public speaking daily",
                    "Receive instant AI speech analysis"
                )
            ),
            TutorialSlide(
                title = "Step Tracker & Sensor Engine",
                subtitle = "Hardware Accelerometer & Peak Detection",
                description = "Track steps in real-time during daily walks or active workouts. Calculates exact distance from your custom stride length, track active duration, and log manual entries.",
                icon = Icons.Default.DirectionsWalk,
                iconTint = EmeraldPrimary,
                featurePoints = listOf(
                    "Real-time step counting with step goal rings",
                    "Distance calculation in kilometers or miles",
                    "Supports Walking, Jogging & Running activities"
                )
            ),
            TutorialSlide(
                title = "2-Phase Practice Studio",
                subtitle = "Random Topic Generator & Visual Countdown",
                description = "Sharpen your impromptu speaking with 30+ random topics. Take 10 minutes to outline key arguments, then deliver a 1 to 2 minute speech with a live visual ring timer.",
                icon = Icons.Default.Timer,
                iconTint = SpeechCoral,
                featurePoints = listOf(
                    "Hit button for 30+ random practice topics",
                    "10-Minute structured outline preparation timer",
                    "1 to 2 Minute speech recording with live visual ring"
                )
            ),
            TutorialSlide(
                title = "Gemini AI Speech Analysis",
                subtitle = "Clarity Scores & Filler Word Detection",
                description = "Get detailed AI evaluation on your transcript. Discover clarity percentage scores, exact filler word counts ('um', 'like', 'so'), vocabulary tips, and actionable coaching advice.",
                icon = Icons.Default.AutoAwesome,
                iconTint = StreakGold,
                featurePoints = listOf(
                    "Clarity score percentage & narrative feedback",
                    "Detects filler words ('um', 'like', 'basically')",
                    "Vocabulary suggestions & tone confidence analysis"
                )
            ),
            TutorialSlide(
                title = "Analytics & Evening Theme Mode",
                subtitle = "Streaks, Goals & High-Contrast Mode",
                description = "Monitor weekly step trends, maintain active step and speech streaks, and switch between a Clean Light Theme for daytime and a High-Contrast Dark Mode for evening sessions.",
                icon = Icons.Default.Analytics,
                iconTint = AccentIndigo,
                featurePoints = listOf(
                    "Weekly step progress bar charts",
                    "Step streak & speech practice streak tracking",
                    "Clean Light Mode & Evening Dark Mode in Settings"
                )
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    val currentSlide = slides[currentSlideIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("onboarding_tutorial_dialog"),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar with Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APP WALKTHROUGH (${currentSlideIndex + 1}/${slides.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_skip_onboarding")
                    ) {
                        Text(
                            text = "Skip",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slide Content with Fade Animation
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SlideAnimation"
                ) { slide ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hero Icon Badge
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(slide.iconTint.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = slide.iconTint,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = slide.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = slide.iconTint
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Feature Point Checklist
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                slide.featurePoints.forEach { point ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = slide.iconTint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = point,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (index == currentSlideIndex) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentSlideIndex) currentSlide.iconTint
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentSlideIndex > 0) {
                        OutlinedButton(
                            onClick = { currentSlideIndex-- },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_prev_slide")
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = "Previous")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    if (currentSlideIndex < slides.size - 1) {
                        Button(
                            onClick = { currentSlideIndex++ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = currentSlide.iconTint),
                            modifier = Modifier.testTag("btn_next_slide")
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.NavigateNext, contentDescription = "Next")
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.testTag("btn_finish_onboarding")
                        ) {
                            Text("Get Started!", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
