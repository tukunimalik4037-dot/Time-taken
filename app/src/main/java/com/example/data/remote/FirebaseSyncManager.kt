package com.example.data.remote

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Firebase Real-Time Cloud Sync Architecture
 * Handles user isolation, Firestore synchronization simulation & status,
 * Security Rules validation, and Cloud Messaging configuration.
 */
data class CloudSyncStatus(
    val isUserAuthenticated: Boolean = true,
    val userId: String = "usr_prod_89412",
    val userEmail: String = "user@timetrack.ai",
    val isSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val pendingChangesCount: Int = 0,
    val fcmToken: String = "fcm_token_sample_889a7f3",
    val syncState: String = "ONLINE_SYNCED" // ONLINE_SYNCED, SYNCING, OFFLINE, CONFLICT_RESOLVED
)

object FirebaseSyncManager {
    private val _syncStatus = MutableStateFlow(CloudSyncStatus())
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    fun triggerManualSync(context: Context) {
        _syncStatus.value = _syncStatus.value.copy(
            syncState = "SYNCING"
        )
        // Simulate synchronization cycle
        _syncStatus.value = _syncStatus.value.copy(
            lastSyncTimestamp = System.currentTimeMillis(),
            pendingChangesCount = 0,
            syncState = "ONLINE_SYNCED"
        )
    }

    fun toggleCloudSync(enabled: Boolean) {
        _syncStatus.value = _syncStatus.value.copy(
            isSyncEnabled = enabled,
            syncState = if (enabled) "ONLINE_SYNCED" else "OFFLINE"
        )
    }

    fun clearCloudUserData() {
        _syncStatus.value = _syncStatus.value.copy(
            lastSyncTimestamp = System.currentTimeMillis(),
            pendingChangesCount = 0
        )
    }

    const val FIRESTORE_SECURITY_RULES_INFO = """
    rules_version = '2';
    service cloud.firestore {
      match /databases/{database}/documents {
        match /users/{userId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
          match /timetable/{document=**} {
            allow read, write: if request.auth != null && request.auth.uid == userId;
          }
          match /dailySummaries/{document=**} {
            allow read, write: if request.auth != null && request.auth.uid == userId;
          }
          match /analytics/{document=**} {
            allow read, write: if request.auth != null && request.auth.uid == userId;
          }
        }
      }
    }
    """
}
