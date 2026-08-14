package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GeometricActivePurple
import com.example.ui.theme.GeometricBorder
import com.example.ui.theme.GeometricBorderSubtle
import com.example.ui.theme.GeometricDarkPurple
import com.example.ui.theme.GeometricLightPurple
import com.example.ui.theme.GeometricPurple
import com.example.ui.theme.GeometricRedAlert
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import com.example.ui.viewmodel.TimeTrackViewModel

@Composable
fun SettingsScreen(
    viewModel: TimeTrackViewModel,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()

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
                    text = "Application Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Preferences, Cloud Synchronization & System Rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = GeometricTextSecondary
                )
            }
        }

        // Section: System & Reminders
        item {
            Text(
                text = "Reminders & Reliability",
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
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        tint = GeometricPurple,
                        title = "Notification Preferences",
                        subtitle = "Exact alarm sounds, default 10m reminder, vibration",
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                })
                            } catch (e: Exception) { }
                        }
                    )

                    SettingsItem(
                        icon = Icons.Default.BatteryChargingFull,
                        tint = GeometricPurple,
                        title = "Battery Optimization Exemption",
                        subtitle = "Allow exact alarms while device is in Doze/Idle mode",
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                            } catch (e: Exception) { }
                        }
                    )
                }
            }
        }

        // Section: Privacy & Cloud
        item {
            Text(
                text = "Privacy & Cloud Sync",
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
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        tint = GeometricPurple,
                        title = "Privacy Dashboard",
                        subtitle = "Pause monitoring, wipe analytics, export JSON logs",
                        onClick = onNavigateToPrivacy,
                        testTag = "settings_privacy_dashboard_item"
                    )

                    SettingsItem(
                        icon = Icons.Default.CloudSync,
                        tint = GeometricPurple,
                        title = "Cloud Sync & Firestore State",
                        subtitle = "Status: ${cloudSyncStatus.syncState} • Tap to sync now",
                        onClick = { viewModel.triggerCloudSync() }
                    )
                }
            }
        }

        // Section: Administration
        item {
            Text(
                text = "Developer & Administration",
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
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsItem(
                        icon = Icons.Default.AdminPanelSettings,
                        tint = GeometricDarkPurple,
                        title = "Admin Panel & Telemetry Console",
                        subtitle = "User counts, push broadcasts, feature flags",
                        onClick = onNavigateToAdmin,
                        testTag = "settings_admin_panel_item"
                    )
                }
            }
        }

        // Section: About
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GeometricLightPurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeometricBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TimeTrack AI v1.0",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GeometricDarkPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time timetable scheduling engine & privacy-guaranteed screen activity analytics with Gemini 3.5 Flash intelligence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeometricDarkPurple.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = GeometricLightPurple,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GeometricTextSecondary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = GeometricTextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

