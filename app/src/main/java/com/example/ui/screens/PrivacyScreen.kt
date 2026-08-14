package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun PrivacyScreen(
    viewModel: TimeTrackViewModel
) {
    val context = LocalContext.current
    val privacyState by viewModel.privacyState.collectAsState()

    var showDeleteTodayDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

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
                    text = "Privacy & Data Controls",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Full transparency and control over your screen analytics",
                    style = MaterialTheme.typography.bodySmall,
                    color = GeometricTextSecondary
                )
            }
        }

        // Monitoring Master Toggle Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_monitoring_switch_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (privacyState.isScreenMonitoringEnabled) GeometricPurple else GeometricBorder
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (privacyState.isScreenMonitoringEnabled) GeometricLightPurple else GeometricSurface,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (privacyState.isScreenMonitoringEnabled) GeometricPurple else GeometricTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Screen Activity Tracking",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (privacyState.isScreenMonitoringEnabled) "Status: ACTIVE (User-Controlled)" else "Status: OFF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (privacyState.isScreenMonitoringEnabled) GeometricPurple else GeometricTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = privacyState.isScreenMonitoringEnabled,
                            onCheckedChange = { viewModel.toggleScreenMonitoring(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GeometricPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "When enabled, TimeTrack AI records app foreground duration and interaction frequencies. You can pause or wipe this data at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeometricTextSecondary
                    )
                }
            }
        }

        // Privacy Guarantee Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GeometricLightPurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = GeometricDarkPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Zero Private Screen Content Policy",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GeometricDarkPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We never inspect, record, or transmit passwords, messages, keystrokes, or screen contents. Only high-level app names and durations are stored locally.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GeometricDarkPurple.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // System Permissions Settings
        item {
            Text(
                text = "System Permissions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PermissionRow(
                        title = "Usage Access Permission",
                        subtitle = "Required to calculate daily app screen-time",
                        isGranted = privacyState.isUsageAccessPermissionGranted,
                        onOpenSettings = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            } catch (e: Exception) { }
                        }
                    )

                    PermissionRow(
                        title = "Exact Alarm & Notifications",
                        subtitle = "Required for reliable timetable reminders",
                        isGranted = privacyState.isNotificationPermissionGranted,
                        onOpenSettings = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                })
                            } catch (e: Exception) { }
                        }
                    )

                    PermissionRow(
                        title = "Optional Accessibility Service",
                        subtitle = "Measures scroll events without content inspection",
                        isGranted = privacyState.isAccessibilityPermissionGranted,
                        onOpenSettings = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            } catch (e: Exception) { }
                        }
                    )
                }
            }
        }

        // User Data Management Actions
        item {
            Text(
                text = "Data Management & Deletion",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Pause tracking
                    OutlinedButton(
                        onClick = { viewModel.togglePauseMonitoring() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
                    ) {
                        Icon(
                            imageVector = if (privacyState.isMonitoringPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = GeometricPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (privacyState.isMonitoringPaused) "Resume Tracking" else "Pause Tracking Temporarily",
                            color = GeometricPurple
                        )
                    }

                    // Delete Today's Data
                    Button(
                        onClick = { showDeleteTodayDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeometricLightPurple,
                            contentColor = GeometricDarkPurple
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Today's Analytics", fontWeight = FontWeight.Bold)
                    }

                    // Delete All Analytics
                    Button(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeometricRedAlert,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wipe All Historical Analytics", fontWeight = FontWeight.Bold)
                    }

                    // Export Data
                    OutlinedButton(
                        onClick = {
                            viewModel.exportData { json ->
                                exportedJsonText = json
                                showExportDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = GeometricTextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Collected Data (JSON)", color = GeometricTextPrimary)
                    }

                    // Clear Cloud Data
                    OutlinedButton(
                        onClick = { viewModel.clearCloudData() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorder)
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = GeometricTextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wipe Cloud Synced Records", color = GeometricTextPrimary)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // Confirmation Dialogs
    if (showDeleteTodayDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTodayDialog = false },
            title = { Text("Delete Today's Analytics?") },
            text = { Text("This will permanently remove all app usage and short-video activity records logged for today.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTodayAnalytics()
                        showDeleteTodayDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeometricPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Delete Today")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTodayDialog = false }) {
                    Text("Cancel", color = GeometricTextSecondary)
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Wipe All Analytics Data?") },
            text = { Text("This will permanently purge all historical screen-time records, short-video estimates, and AI summaries from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllAnalytics()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeometricRedAlert, contentColor = Color.White),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Wipe Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = GeometricTextSecondary)
                }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exported Data (JSON)") },
            text = {
                Text(
                    text = exportedJsonText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GeometricPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) GeometricPurple else GeometricRedAlert,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GeometricTextSecondary
            )
        }

        TextButton(onClick = onOpenSettings) {
            Text(
                text = if (isGranted) "Enabled" else "Grant",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isGranted) GeometricPurple else GeometricActivePurple
            )
        }
    }
}

