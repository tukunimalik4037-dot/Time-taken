package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ScreenAccessibility"
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _scrollCount = MutableStateFlow(0)
        val scrollCount: StateFlow<Int> = _scrollCount.asStateFlow()

        private val _lastForegroundPackage = MutableStateFlow("")
        val lastForegroundPackage: StateFlow<String> = _lastForegroundPackage.asStateFlow()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceRunning.value = true
        Log.d(TAG, "Accessibility Service Connected with privacy protections enabled.")

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                         AccessibilityEvent.TYPE_VIEW_SCROLLED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            // Explicitly do NOT inspect window node contents or private text
            flags = AccessibilityServiceInfo.DEFAULT
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val pkg = event.packageName?.toString() ?: ""
                if (pkg.isNotEmpty() && pkg != packageName) {
                    _lastForegroundPackage.value = pkg
                }
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                _scrollCount.value = _scrollCount.value + 1
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        _isServiceRunning.value = false
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
    }
}
