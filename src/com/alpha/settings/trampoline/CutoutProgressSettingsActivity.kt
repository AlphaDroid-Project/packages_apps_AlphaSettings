package com.alpha.settings.trampoline

import com.android.settings.SettingsActivity
import com.alpha.settings.fragments.statusbar.CutoutRingSettingsFragment

class CutoutProgressSettingsActivity : SettingsActivity() {
    override fun isValidFragment(fragmentName: String): Boolean {
        return CutoutRingSettingsFragment::class.java.name == fragmentName
    }
}