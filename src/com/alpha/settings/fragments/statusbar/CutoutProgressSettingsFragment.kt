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
        // Feature Keys are handled natively by SystemSettingSwitchPreference
        // We only need to define keys here that require manual UI manipulation (like color dialogs)

        private const val KEY_RING_COLOR_MODE = "cutout_progress_ring_color_mode"
        private const val COLOR_MODE_ACCENT = 0
        private const val COLOR_MODE_RAINBOW = 1
        private const val COLOR_MODE_CUSTOM = 2

        private const val KEY_RING_COLOR = "cutout_progress_ring_color"
        private const val KEY_ERROR_COLOR = "cutout_progress_error_color"
        private const val KEY_FLASH_COLOR = "cutout_progress_finish_flash_color"
        private const val KEY_BG_COLOR = "cutout_progress_bg_ring_color"

        private const val KEY_FINISH_STYLE = "cutout_progress_finish_style"
        private const val KEY_EASING = "cutout_progress_easing"
        private const val KEY_PERCENT_POSITION = "cutout_progress_percent_position"
        private const val KEY_FILENAME_POSITION = "cutout_progress_filename_position"
        private const val KEY_FILENAME_TRUNCATE = "cutout_progress_filename_truncate"

        private const val KEY_ISLAND_POSITION = "cutout_progress_island_position"

        private const val DEFAULT_RING_COLOR = 0xFF2196F3.toInt()
        private const val DEFAULT_ERROR_COLOR = 0xFFF44336.toInt()
        private const val DEFAULT_FLASH_COLOR = 0xFFFFFFFF.toInt()
        private const val DEFAULT_BG_COLOR = 0xFF808080.toInt()
    }

    private lateinit var ringColorModePref: ListPreference
    private lateinit var islandPosPref: ListPreference

    private lateinit var ringColorPref: Preference
    private lateinit var errorColorPref: Preference
    private lateinit var flashColorPref: Preference
    private lateinit var bgColorPref: Preference

    private lateinit var finishStylePref: ListPreference
    private lateinit var easingPref: ListPreference
    private lateinit var pctPosPref: ListPreference
    private lateinit var fnamePosPref: ListPreference
    private lateinit var fnameTruncPref: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.cutout_progress_settings)

        ringColorModePref = findPreference(KEY_RING_COLOR_MODE)!!
        islandPosPref = findPreference(KEY_ISLAND_POSITION)!!

        ringColorPref = findPreference(KEY_RING_COLOR)!!
        errorColorPref = findPreference(KEY_ERROR_COLOR)!!
        flashColorPref = findPreference(KEY_FLASH_COLOR)!!
        bgColorPref = findPreference(KEY_BG_COLOR)!!

        finishStylePref = findPreference(KEY_FINISH_STYLE)!!
        easingPref = findPreference(KEY_EASING)!!
        pctPosPref = findPreference(KEY_PERCENT_POSITION)!!
        fnamePosPref = findPreference(KEY_FILENAME_POSITION)!!
        fnameTruncPref = findPreference(KEY_FILENAME_TRUNCATE)!!

        refreshColorSummaries()
        syncListPreferences()
        setupIslandPositionPreference()

        val storedMode = readSystemInt(KEY_RING_COLOR_MODE, COLOR_MODE_ACCENT)
        ringColorModePref.value = storedMode.toString()
        updateColorPickerVisibility(storedMode)

        ringColorModePref.onPreferenceChangeListener = this

        ringColorPref.setOnPreferenceClickListener {
            showColorPicker(
                title = getString(R.string.cutout_progress_ring_color_title),
                key = KEY_RING_COLOR,
                default = DEFAULT_RING_COLOR
            )
            true
        }
        errorColorPref.setOnPreferenceClickListener {
            showColorPicker(
                title = getString(R.string.cutout_progress_error_color_title),
                key = KEY_ERROR_COLOR,
                default = DEFAULT_ERROR_COLOR
            )
            true
        }
        flashColorPref.setOnPreferenceClickListener {
            showColorPicker(
                title = getString(R.string.cutout_progress_finish_flash_color_title),
                key = KEY_FLASH_COLOR,
                default = DEFAULT_FLASH_COLOR
            )
            true
        }
        bgColorPref.setOnPreferenceClickListener {
            showColorPicker(
                title = getString(R.string.cutout_progress_bg_ring_color_title),
                key = KEY_BG_COLOR,
                default = DEFAULT_BG_COLOR
            )
            true
        }

        finishStylePref.onPreferenceChangeListener = this
        easingPref.onPreferenceChangeListener = this
        pctPosPref.onPreferenceChangeListener = this
        fnamePosPref.onPreferenceChangeListener = this
        fnameTruncPref.onPreferenceChangeListener = this
    }

    private fun setupIslandPositionPreference() {
        val windowManager = requireContext().getSystemService(WindowManager::class.java)
        val windowMetrics = windowManager.currentWindowMetrics
        val displayWidth = windowMetrics.bounds.width()
        val cutout = windowMetrics.windowInsets.displayCutout

        var isLeftCutout = false
        var isRightCutout = false

        // Detect physical hardware camera position
        cutout?.boundingRects?.forEach { rect ->
            if (rect.centerX() < displayWidth / 3) isLeftCutout = true
            else if (rect.centerX() > displayWidth * 2 / 3) isRightCutout = true
        }

        val entries = mutableListOf<CharSequence>()
        val values = mutableListOf<CharSequence>()

        // 0 = Center Split, 1 = Left, 2 = Right
        entries.add(getString(R.string.cutout_island_pos_center)) // e.g. "Center Split"
        values.add("0")

        if (!isLeftCutout) {
            entries.add(getString(R.string.cutout_island_pos_left)) // e.g. "Left"
            values.add("1")
        }
        if (!isRightCutout) {
            entries.add(getString(R.string.cutout_island_pos_right)) // e.g. "Right"
            values.add("2")
        }

        islandPosPref.entries = entries.toTypedArray()
        islandPosPref.entryValues = values.toTypedArray()

        // Fallback to Center if user had an invalid selection previously saved
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
            writeSystemInt(KEY_RING_COLOR_MODE, intValue)
            updateColorPickerVisibility(intValue)
            return true
        }

        writeSystemInt(preference.key, intValue)
        return true
    }

    private fun updateColorPickerVisibility(mode: Int) {
        val isCustom = mode == COLOR_MODE_CUSTOM
        ringColorPref.isVisible = isCustom
    }

    private fun syncListPreferences() {
        listOf(
            finishStylePref to 0,
            easingPref to 0,
            pctPosPref to 0,
            fnamePosPref to 4,
            fnameTruncPref to 0
        ).forEach { (pref, default) ->
            pref.value = readSystemInt(pref.key, default).toString()
        }
    }

    private fun showColorPicker(title: String, key: String, default: Int) {
        val currentArgb = readSystemInt(key, default)
        val currentHex = argbToHex(currentArgb)

        val dialog = CutoutProgressColorPickerDialogFragment.newInstance(
            title = title,
            colorHex = currentHex
        )
        dialog.setOnColorSelectedListener { color: Color ->
            writeSystemInt(key, color.toArgb())
            refreshColorSummaries()
        }
        dialog.show(parentFragmentManager, CutoutProgressColorPickerDialogFragment.TAG)
    }

    private fun refreshColorSummaries() {
        ringColorPref.summary = "#${argbToHex(readSystemInt(KEY_RING_COLOR, DEFAULT_RING_COLOR))}"
        errorColorPref.summary = "#${argbToHex(readSystemInt(KEY_ERROR_COLOR, DEFAULT_ERROR_COLOR))}"
        flashColorPref.summary = "#${argbToHex(readSystemInt(KEY_FLASH_COLOR, DEFAULT_FLASH_COLOR))}"
        bgColorPref.summary = "#${argbToHex(readSystemInt(KEY_BG_COLOR, DEFAULT_BG_COLOR))}"
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