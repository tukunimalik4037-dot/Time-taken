package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeometricActivePurple
import com.example.ui.theme.GeometricBorder
import com.example.ui.theme.GeometricBorderSubtle
import com.example.ui.theme.GeometricCardBg
import com.example.ui.theme.GeometricDarkPurple
import com.example.ui.theme.GeometricLightPurple
import com.example.ui.theme.GeometricPurple
import com.example.ui.theme.GeometricPurpleTrack
import com.example.ui.theme.GeometricRedAlert
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import com.example.ui.viewmodel.TimeTrackViewModel

@Composable
fun DashboardScreen(
    viewModel: TimeTrackViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToInsights: () -> Unit
) {
    val timetables by viewModel.timetables.collectAsState()
    val activeState by viewModel.activeActivityState.collectAsState()
    val progress by viewModel.todayProgress.collectAsState()
    val stats by viewModel.aggregatedStats.collectAsState()

    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Top Header (Geometric Balance Header with Avatar Circle)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good Morning,",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GeometricTextSecondary,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = "User 👋",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    // Avatar Circle JD
                    Surface(
                        shape = CircleShape,
                        color = GeometricLightPurple,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { viewModel.triggerCloudSync() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "JD",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeometricDarkPurple
                                )
                            )
                        }
                    }
                }
            }

            // 2. Geometric Hero Progress Card (Lilac Container + Progress Ring)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_progress_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GeometricLightPurple
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TODAY'S PROGRESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = GeometricDarkPurple
                                )
                            )
                            Text(
                                text = "$progress%",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeometricDarkPurple,
                                    fontSize = 36.sp
                                )
                            )
                            val completedCount = timetables.count { it.status == "COMPLETED" }
                            Text(
                                text = "$completedCount of ${timetables.size} activities done",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = GeometricTextSecondary,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        // Circular Progress Ring (Track: GeometricPurpleTrack, Progress: GeometricPurple)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(80.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = GeometricPurpleTrack,
                                strokeWidth = 8.dp,
                            )
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = GeometricPurple,
                                strokeWidth = 8.dp,
                            )
                        }
                    }
                }
            }

            // 3. Grid Metrics Cards (Screen Time & Short Video)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Screen Time Box
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onNavigateToAnalytics() },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SCREEN TIME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp,
                                    color = GeometricTextSecondary
                                )
                            )
                            Text(
                                text = "${stats.totalScreenTimeMinutes / 60}h ${stats.totalScreenTimeMinutes % 60}m",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    // Short Video Box
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onNavigateToAnalytics() },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SHORT VIDEO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp,
                                    color = GeometricTextSecondary
                                )
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${stats.shortVideoMetrics.totalTimeMinutes}m",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GeometricRedAlert
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Est.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = GeometricTextSecondary.copy(alpha = 0.7f),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Next Activity Geometric Section
            item {
                val currentTask = activeState.currentActivity ?: activeState.nextActivity
                if (currentTask != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_activity_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GeometricSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            GeometricBorderSubtle
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (activeState.isCurrentlyActive) "ACTIVE ACTIVITY" else "NEXT ACTIVITY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = GeometricPurple
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentTask.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${currentTask.startTime} • ${currentTask.category}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = GeometricTextSecondary
                                        )
                                    )
                                }

                                // Book / Category Icon Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GeometricPurple,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Pill Action Buttons (Done full purple, Snooze outlined purple, Skip)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.markComplete(currentTask.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("action_done_button"),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GeometricPurple,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        "Done",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.snoozeTask(currentTask.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("action_snooze_button"),
                                    shape = RoundedCornerShape(50),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GeometricPurple),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = GeometricPurple
                                    )
                                ) {
                                    Text(
                                        "Snooze",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.skipTask(currentTask.id) },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(50),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = GeometricTextSecondary
                                    )
                                ) {
                                    Text(
                                        "Skip",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = GeometricSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GeometricPurple,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All Activities Completed!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Great job keeping up with today's schedule.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GeometricTextSecondary
                            )
                        }
                    }
                }
            }

            // 5. Hourly Activity Geometric Bar Chart Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hourly Activity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View Details",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GeometricPurple,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.clickable { onNavigateToAnalytics() }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Geometric Bar Chart
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        ) {
                            val barCount = 7
                            val spacing = 8.dp.toPx()
                            val barWidth = (size.width - (spacing * (barCount - 1))) / barCount
                            val sampleHeights = listOf(0.30f, 0.45f, 0.85f, 0.60f, 0.40f, 0.20f, 0.50f)

                            for (i in 0 until barCount) {
                                val ratio = sampleHeights[i]
                                val barH = size.height * ratio
                                val x = i * (barWidth + spacing)
                                val y = size.height - barH

                                drawRoundRect(
                                    color = if (i == 2) GeometricPurple else GeometricBorderSubtle,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // AI Insight Pill Banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.background,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToInsights() }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GeometricPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "AI Insight: Study period peaked at 6 PM. Focus levels were 15% higher than yesterday.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = GeometricTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Geometric Pill/Circle Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_timetable"),
            containerColor = GeometricPurple,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Timetable Activity")
        }
    }
}
