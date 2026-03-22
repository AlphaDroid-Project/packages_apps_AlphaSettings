/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * Copyright (C) 2026 AlphaDroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alpha.settings.fragments.statusbar

import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.alpha.settings.utils.toArgb

class CutoutProgressSettingsFragment : SettingsPreferenceFragment(),
    Preference.OnPreferenceChangeListener {

    companion object {
        private const val KEY_RING_COLOR_MODE = "cutout_progress_ring_color_mode"
        private const val COLOR_MODE_CUSTOM = 2

        private const val KEY_RING_COLOR = "cutout_progress_ring_color"
        private const val DEFAULT_RING_COLOR = 0xFF2196F3.toInt()

        private const val KEY_ISLAND_POSITION = "cutout_progress_island_position"
    }

    private lateinit var ringColorModePref: ListPreference
    private lateinit var ringColorPref: Preference
    private lateinit var islandPosPref: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.cutout_progress_settings)

        ringColorModePref = findPreference(KEY_RING_COLOR_MODE)!!
        ringColorPref = findPreference(KEY_RING_COLOR)!!
        islandPosPref = findPreference(KEY_ISLAND_POSITION)!!

        setupIslandPositionPreference()

        val storedMode = readSystemInt(KEY_RING_COLOR_MODE, 0)
        ringColorModePref.value = storedMode.toString()
        ringColorPref.isVisible = (storedMode == COLOR_MODE_CUSTOM)
        ringColorModePref.onPreferenceChangeListener = this

        ringColorPref.summary = "#${argbToHex(readSystemInt(KEY_RING_COLOR, DEFAULT_RING_COLOR))}"
        ringColorPref.setOnPreferenceClickListener {
            val currentHex = argbToHex(readSystemInt(KEY_RING_COLOR, DEFAULT_RING_COLOR))
            val dialog = CutoutProgressColorPickerDialogFragment.newInstance(
                title = getString(R.string.cutout_progress_ring_color_title),
                colorHex = currentHex
            )
            dialog.setOnColorSelectedListener { color: Color ->
                writeSystemInt(KEY_RING_COLOR, color.toArgb())
                ringColorPref.summary = "#${argbToHex(color.toArgb())}"
            }
            dialog.show(parentFragmentManager, CutoutProgressColorPickerDialogFragment.TAG)
            true
        }
    }

    private fun setupIslandPositionPreference() {
        val windowManager = requireContext().getSystemService(WindowManager::class.java)
        val windowMetrics = windowManager.currentWindowMetrics
        val displayWidth = windowMetrics.bounds.width()
        val cutout = windowMetrics.windowInsets.displayCutout

        var isLeftCutout = false
        var isRightCutout = false

        cutout?.boundingRects?.forEach { rect ->
            if (rect.centerX() < displayWidth / 3) isLeftCutout = true
            else if (rect.centerX() > displayWidth * 2 / 3) isRightCutout = true
        }

        val entries = mutableListOf<CharSequence>()
        val values = mutableListOf<CharSequence>()

        entries.add(getString(R.string.cutout_island_pos_center))
        values.add("0")

        if (!isLeftCutout) {
            entries.add(getString(R.string.cutout_island_pos_left))
            values.add("1")
        }
        if (!isRightCutout) {
            entries.add(getString(R.string.cutout_island_pos_right))
            values.add("2")
        }

        islandPosPref.entries = entries.toTypedArray()
        islandPosPref.entryValues = values.toTypedArray()

        var currentPos = readSystemInt(KEY_ISLAND_POSITION, 0)
        if ((currentPos == 1 && isLeftCutout) || (currentPos == 2 && isRightCutout)) {
            currentPos = 0
            writeSystemInt(KEY_ISLAND_POSITION, currentPos)
        }

        islandPosPref.value = currentPos.toString()
        islandPosPref.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val intValue = (newValue as? String)?.toIntOrNull() ?: return false

        if (preference.key == KEY_RING_COLOR_MODE) {
            ringColorPref.isVisible = (intValue == COLOR_MODE_CUSTOM)
        }

        writeSystemInt(preference.key, intValue)
        return true
    }

    private fun readSystemInt(key: String, default: Int): Int =
        Settings.System.getInt(requireContext().contentResolver, key, default)

    private fun writeSystemInt(key: String, value: Int) {
        Settings.System.putInt(requireContext().contentResolver, key, value)
    }

    private fun argbToHex(argb: Int): String =
        String.format("%06X", 0xFFFFFF and argb)

    override fun getMetricsCategory(): Int = MetricsEvent.ALPHA
}
