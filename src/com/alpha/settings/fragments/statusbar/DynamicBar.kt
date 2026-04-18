/*
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.alpha.settings.fragments.statusbar

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import org.json.JSONArray

class DynamicBar : SettingsPreferenceFragment() {

    private val resolver: ContentResolver
        get() = requireContext().contentResolver

    private val handler = Handler(Looper.getMainLooper())
    private var settingsObserver: ContentObserver? = null

    private val eventTypeIds = listOf(
        "screen_recording",
        "privacy",
        "audio_recording",
        "media",
        "call",
        "notification",
        "timer",
        "stopwatch",
        "alarm",
        "charging",
        "bluetooth",
        "hotspot",
        "ringer",
        "vpn",
        "clipboard",
        "torch",
        "casting",
        "promoted_ongoing",
        "sports",
        "app_switch",
        "biometric_unlock",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.dynamic_bar)

        setupKeyguardSubPrefs()
        setupEventToggles()
        updateCompactNotificationVisibility()
        registerObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        settingsObserver?.let { resolver.unregisterContentObserver(it) }
    }

    private fun setupKeyguardSubPrefs() {
        val keyguardPref = findPreference<SwitchPreferenceCompat>(SETTINGS_KEY_KEYGUARD_ENABLED)
        keyguardPref?.setOnPreferenceChangeListener { _, newValue ->
            updateKeyguardSubPrefsVisibility(newValue as Boolean)
            true
        }
        // Set initial visibility from current setting value
        val keyguardEnabled = Settings.Secure.getIntForUser(
            resolver, SETTINGS_KEY_KEYGUARD_ENABLED, 1, UserHandle.USER_CURRENT
        ) == 1
        updateKeyguardSubPrefsVisibility(keyguardEnabled)
    }

    private fun updateKeyguardSubPrefsVisibility(keyguardEnabled: Boolean) {
        findPreference<Preference>(SETTINGS_KEY_BATTERY_CHIP_MODE)?.isVisible = keyguardEnabled
    }

    private fun setupEventToggles() {
        val disabledEvents = getDisabledEvents()

        for (typeId in eventTypeIds) {
            val pref = findPreference<SwitchPreferenceCompat>("event_$typeId") ?: continue
            pref.isChecked = typeId !in disabledEvents
            pref.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                toggleEvent(typeId, enabled)
                if (typeId == "notification") {
                    updateCompactNotificationVisibility()
                }
                true
            }
        }
    }

    private fun getDisabledEvents(): Set<String> {
        val json = Settings.Secure.getStringForUser(
            resolver, SETTINGS_KEY_EVENTS, UserHandle.USER_CURRENT
        ) ?: return emptySet()

        if (json.isBlank()) return emptySet()

        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapTo(mutableSetOf()) { arr.getString(it) }
        } catch (_: Exception) {
            emptySet()
        }
    }

    private fun toggleEvent(typeId: String, enabled: Boolean) {
        val current = getDisabledEvents()
        val updated = if (enabled) current - typeId else current + typeId
        val json = if (updated.isEmpty()) "" else JSONArray(updated.toList()).toString()
        Settings.Secure.putStringForUser(
            resolver, SETTINGS_KEY_EVENTS, json, UserHandle.USER_CURRENT
        )
    }

    private fun updateCompactNotificationVisibility() {
        val compactPref = findPreference<Preference>(SETTINGS_KEY_COMPACT_NOTIFICATIONS)
        val notifPref = findPreference<SwitchPreferenceCompat>("event_notification")
        compactPref?.isVisible = notifPref?.isChecked == true
    }

    private fun registerObserver() {
        settingsObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                when (uri?.lastPathSegment) {
                    SETTINGS_KEY_EVENTS -> {
                        val disabledEvents = getDisabledEvents()
                        for (typeId in eventTypeIds) {
                            val pref = findPreference<SwitchPreferenceCompat>("event_$typeId")
                            pref?.isChecked = typeId !in disabledEvents
                        }
                        updateCompactNotificationVisibility()
                    }
                    SETTINGS_KEY_KEYGUARD_ENABLED -> {
                        val enabled = Settings.Secure.getIntForUser(
                            resolver, SETTINGS_KEY_KEYGUARD_ENABLED, 1, UserHandle.USER_CURRENT
                        ) == 1
                        updateKeyguardSubPrefsVisibility(enabled)
                    }
                }
            }
        }

        resolver.registerContentObserver(
            Settings.Secure.getUriFor(SETTINGS_KEY_EVENTS),
            false, settingsObserver!!
        )
        resolver.registerContentObserver(
            Settings.Secure.getUriFor(SETTINGS_KEY_KEYGUARD_ENABLED),
            false, settingsObserver!!
        )
    }

    override fun getMetricsCategory(): Int {
        return MetricsProto.MetricsEvent.ALPHA
    }

    companion object {
        private const val SETTINGS_KEY_ENABLED = "ax_dynamic_bar_enabled"
        private const val SETTINGS_KEY_KEYGUARD_ENABLED = "ax_dynamic_bar_keyguard_enabled"
        private const val SETTINGS_KEY_EVENTS = "ax_dynamic_bar_events"
        private const val SETTINGS_KEY_COMPACT_NOTIFICATIONS = "ax_dynamic_bar_compact_notifications"
        private const val SETTINGS_KEY_BATTERY_CHIP_MODE = "ax_dynamic_bar_keyguard_battery_chip_mode"

        // Cutout ring settings (Show on cutout)
        private const val SETTINGS_KEY_CUTOUT_RING_ENABLED = "cutout_ring_enabled"
        private const val SETTINGS_KEY_CUTOUT_COLLAPSE_TO_RING = "cutout_collapse_to_ring"
        private const val SETTINGS_KEY_RING_GAP = "cutout_ring_gap_x1000"
        private const val SETTINGS_KEY_RING_SCALE_X = "cutout_ring_scale_x_x1000"
        private const val SETTINGS_KEY_RING_SCALE_Y = "cutout_ring_scale_y_x1000"
        private const val SETTINGS_KEY_RING_OFFSET_X = "cutout_ring_offset_x_dp10"
        private const val SETTINGS_KEY_RING_OFFSET_Y = "cutout_ring_offset_y_dp10"
        private const val SETTINGS_KEY_RING_OPACITY = "cutout_ring_opacity"
        private const val SETTINGS_KEY_RING_STROKE = "cutout_ring_stroke_dp10"

        @JvmStatic
        fun reset(context: Context) {
            val resolver = context.contentResolver
            Settings.Secure.putIntForUser(
                resolver, SETTINGS_KEY_ENABLED, 0,
                UserHandle.USER_CURRENT
            )
            Settings.Secure.putIntForUser(
                resolver, SETTINGS_KEY_KEYGUARD_ENABLED, 1,
                UserHandle.USER_CURRENT
            )
            Settings.Secure.putIntForUser(
                resolver, SETTINGS_KEY_COMPACT_NOTIFICATIONS, 1,
                UserHandle.USER_CURRENT
            )
            Settings.Secure.putIntForUser(
                resolver, SETTINGS_KEY_BATTERY_CHIP_MODE, 1,
                UserHandle.USER_CURRENT
            )
            // Reset cutout ring settings
            Settings.System.putIntForUser(
                resolver, SETTINGS_KEY_CUTOUT_RING_ENABLED, 0,
                UserHandle.USER_CURRENT
            )
            Settings.System.putIntForUser(
                resolver, SETTINGS_KEY_CUTOUT_COLLAPSE_TO_RING, 0,
                UserHandle.USER_CURRENT
            )
            // Reset ring geometry
            for (key in arrayOf(
                SETTINGS_KEY_RING_GAP, SETTINGS_KEY_RING_SCALE_X, SETTINGS_KEY_RING_SCALE_Y,
                SETTINGS_KEY_RING_OFFSET_X, SETTINGS_KEY_RING_OFFSET_Y, SETTINGS_KEY_RING_OPACITY,
                SETTINGS_KEY_RING_STROKE,
            )) {
                Settings.System.putStringForUser(resolver, key, null, UserHandle.USER_CURRENT)
            }
        }
    }
}
