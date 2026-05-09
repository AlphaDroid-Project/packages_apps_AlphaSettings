package com.alpha.settings.trampoline

import com.android.settings.SettingsActivity
import com.alpha.settings.fragments.statusbar.DynamicBar

class DynamicBarSettingsActivity : SettingsActivity() {
    override fun isValidFragment(fragmentName: String): Boolean {
        return DynamicBar::class.java.name == fragmentName
    }
}
