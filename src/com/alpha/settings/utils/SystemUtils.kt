/*
 * Copyright (C) 2025 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alpha.settings.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

import androidx.appcompat.app.AlertDialog

import com.android.internal.R as FrameworkR
import com.android.internal.util.alpha.Utils

object SystemUtils {

    @JvmStatic
    fun showSystemUiRestartDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(FrameworkR.string.systemui_restart_title)
            .setMessage(FrameworkR.string.systemui_restart_message)
            .setPositiveButton(FrameworkR.string.systemui_restart_yes) { _, _ ->
                restartSystemUI(context)
            }
            .setNegativeButton(FrameworkR.string.systemui_restart_not_now, null)
            .show()
    }

    @JvmStatic
    fun restartSystemUI(context: Context) {
        Toast.makeText(
            context,
            FrameworkR.string.systemui_restart_process,
            Toast.LENGTH_LONG
        ).show()

        Handler(Looper.getMainLooper()).postDelayed({
            Utils.restartSystemUI()
        }, 2000) // 2-second delay
    }
} 
