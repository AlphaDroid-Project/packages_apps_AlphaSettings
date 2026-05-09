/*
 * SPDX-FileCopyrightText: 2026 AlphaDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package com.alpha.settings.fragments.statusbar

import android.os.Bundle
import com.android.internal.logging.nano.MetricsProto
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment

class CutoutRingSettingsFragment : SettingsPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.cutout_ring_settings)
    }

    override fun getMetricsCategory(): Int {
        return MetricsProto.MetricsEvent.ALPHA
    }
}
