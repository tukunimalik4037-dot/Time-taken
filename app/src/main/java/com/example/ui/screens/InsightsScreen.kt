package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repository.SmartProductivityInsight
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
fun InsightsScreen(
    viewModel: TimeTrackViewModel
) {
    val aiSummary by viewModel.aiSummary.collectAsState()
    val isGenerating by viewModel.isGeneratingSummary.collectAsState()
    val insights by viewModel.smartInsights.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header
        item {
            Column {
                Text(
                    text = "AI Productivity Insights",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Natural-language summaries powered by Gemini 3.5 Flash",
                    style = MaterialTheme.typography.bodySmall,
                    color = GeometricTextSecondary
                )
            }
        }

        // Hero AI Summary Card (Geometric Card Layout)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_summary_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = GeometricLightPurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GeometricPurple,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Daily AI Synthesis",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GeometricDarkPurple
                                )
                                Text(
                                    text = "Gemini 3.5 Flash Model",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = GeometricPurple
                                )
                            }
                        }

                        // Privacy badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Sanitized",
                                    tint = GeometricPurple,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Sanitized Metrics",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = GeometricPurple
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isGenerating) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = GeometricPurple,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Analyzing daily patterns...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GeometricDarkPurple
                            )
                        }
                    } else {
                        Text(
                            text = aiSummary?.aiSummaryText ?: "Consistent progress! You completed scheduled timetable sessions with strong study focus.",
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                            color = GeometricDarkPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.generateAiSummary(forceRefresh = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("regenerate_summary_button"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeometricPurple,
                            contentColor = Color.White
                        ),
                        enabled = !isGenerating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Regenerate with Gemini AI",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Smart Non-Medical Insights Header
        item {
            Text(
                text = "Productivity Patterns",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Smart Insights List
        items(insights) { insight ->
            SmartInsightCard(insight = insight)
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun SmartInsightCard(insight: SmartProductivityInsight) {
    val icon = when (insight.iconName) {
        "Bolt" -> Icons.Default.Bolt
        "Smartphone" -> Icons.Default.Smartphone
        "CheckCircle" -> Icons.Default.CheckCircle
        "Balance" -> Icons.Default.Scale
        "TrendingUp" -> Icons.Default.TrendingUp
        "Bedtime" -> Icons.Default.Bedtime
        else -> Icons.Default.AutoAwesome
    }

    val iconColor = when (insight.trendType) {
        "ATTENTION" -> GeometricRedAlert
        else -> GeometricPurple
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = GeometricLightPurple,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = GeometricTextSecondary
                )
                Text(
                    text = insight.value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = insight.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GeometricTextSecondary
                )
            }
        }
    }
}

