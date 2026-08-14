package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.UsageEntity
import com.example.ui.theme.GeometricActivePurple
import com.example.ui.theme.GeometricBorder
import com.example.ui.theme.GeometricBorderSubtle
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
fun AnalyticsScreen(
    viewModel: TimeTrackViewModel
) {
    val stats by viewModel.aggregatedStats.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Today", "This Week", "This Month")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Screen Activity Analytics",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Privacy-first metrics & interaction signals",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeometricTextSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshAnalytics() },
                    modifier = Modifier.testTag("refresh_analytics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Analytics",
                        tint = GeometricPurple
                    )
                }
            }
        }

        // Time Filter Tabs (Geometric Pill Row)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = GeometricSurface,
                contentColor = GeometricPurple,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                tabTitles.forEachIndexed { idx, title ->
                    val isSelected = selectedTab == idx
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) GeometricPurple else Color.Transparent,
                        modifier = Modifier
                            .padding(2.dp)
                            .clickable { selectedTab = idx }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else GeometricTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Category Breakdown Card with Canvas Donut Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Category Distribution",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Canvas Donut Chart
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                val strokeWidth = 22f
                                val total = maxOf(1, stats.totalScreenTimeMinutes).toFloat()
                                val studyAngle = (stats.studyMinutes / total) * 360f
                                val socialAngle = (stats.socialMinutes / total) * 360f
                                val entertainmentAngle = (stats.entertainmentMinutes / total) * 360f
                                val otherAngle = 360f - (studyAngle + socialAngle + entertainmentAngle)

                                var startAngle = -90f
                                drawArc(
                                    color = GeometricPurple,
                                    startAngle = startAngle,
                                    sweepAngle = studyAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += studyAngle

                                drawArc(
                                    color = GeometricActivePurple,
                                    startAngle = startAngle,
                                    sweepAngle = socialAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += socialAngle

                                drawArc(
                                    color = GeometricLightPurple,
                                    startAngle = startAngle,
                                    sweepAngle = entertainmentAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += entertainmentAngle

                                drawArc(
                                    color = GeometricBorder,
                                    startAngle = startAngle,
                                    sweepAngle = maxOf(0f, otherAngle),
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${stats.totalScreenTimeMinutes / 60}h ${stats.totalScreenTimeMinutes % 60}m",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = GeometricTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Category Metrics
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryRow(color = GeometricPurple, name = "Study", mins = stats.studyMinutes)
                            CategoryRow(color = GeometricActivePurple, name = "Social", mins = stats.socialMinutes)
                            CategoryRow(color = GeometricLightPurple, name = "Gaming / Entertainment", mins = stats.entertainmentMinutes)
                            CategoryRow(color = GeometricBorder, name = "Productivity & Other", mins = stats.productivityMinutes + stats.otherMinutes)
                        }
                    }
                }
            }
        }

        // Dedicated Short-Video Activity Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("short_video_activity_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GeometricLightPurple,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = GeometricDarkPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Short-Video Activity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Confidence Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GeometricLightPurple
                        ) {
                            Text(
                                text = "Estimated Signal",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = GeometricDarkPurple,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Estimated Shorts/Reels-style interactions based on scroll events and foreground duration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeometricTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Grid metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GeometricSurface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Estimated Time", style = MaterialTheme.typography.labelSmall, color = GeometricTextSecondary)
                                Text("${stats.shortVideoMetrics.totalTimeMinutes} mins", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GeometricRedAlert)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GeometricSurface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Sessions", style = MaterialTheme.typography.labelSmall, color = GeometricTextSecondary)
                                Text("${stats.shortVideoMetrics.estimatedSessions} sessions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GeometricSurface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Scroll Events", style = MaterialTheme.typography.labelSmall, color = GeometricTextSecondary)
                                Text("${stats.shortVideoMetrics.estimatedScrollInteractions}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GeometricPurple)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GeometricSurface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Peak Time", style = MaterialTheme.typography.labelSmall, color = GeometricTextSecondary)
                                Text(stats.shortVideoMetrics.peakHour, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Hourly Usage Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Hourly Distribution (24h)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Canvas Bar Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        val barWidth = size.width / 24f - 3f
                        val maxBarHeight = size.height - 10f

                        for (i in 0 until 24) {
                            val activeMins = stats.hourlyActivity.getOrElse(i) { 0 }
                            val heightRatio = (activeMins.coerceAtMost(60)) / 60f
                            val barH = maxOf(4f, maxBarHeight * heightRatio)
                            val x = i * (size.width / 24f) + 1.5f
                            val y = size.height - barH

                            drawRoundRect(
                                color = if (i in 18..22) GeometricActivePurple else GeometricPurple,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("12 AM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = GeometricTextSecondary)
                        Text("06 AM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = GeometricTextSecondary)
                        Text("12 PM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = GeometricTextSecondary)
                        Text("06 PM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = GeometricTextSecondary)
                        Text("11 PM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = GeometricTextSecondary)
                    }
                }
            }
        }

        // App Usage List
        item {
            Text(
                text = "Most Used Applications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(stats.topApps) { app ->
            AppUsageRow(app = app)
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun CategoryRow(color: Color, name: String, mins: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "${mins / 60}h ${mins % 60}m",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AppUsageRow(app: UsageEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = GeometricLightPurple,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GeometricDarkPurple
                        )
                    }
                }

                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${app.category} • ${app.scrollInteractions} scrolls",
                        style = MaterialTheme.typography.labelSmall,
                        color = GeometricTextSecondary
                    )
                }
            }

            Text(
                text = "${app.durationMinutes}m",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = GeometricPurple
            )
        }
    }
}

