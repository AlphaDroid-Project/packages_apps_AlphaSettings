package com.alpha.settings.trampoline

import com.android.settings.SettingsActivity
import com.alpha.settings.fragments.statusbar.CutoutProgressSettingsFragment

class CutoutProgressSettingsActivity : SettingsActivity() {
    override fun isValidFragment(fragmentName: String): Boolean {
        return CutoutProgressSettingsFragment::class.java.name == fragmentName
    }
}