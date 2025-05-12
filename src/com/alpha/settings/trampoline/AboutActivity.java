/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.alpha.settings.trampoline;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.alpha.settings.fragments.About;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.core.SubSettingLauncher;

/** Trampoline activity for launching the {@link FirmwareVersionSettings} fragment. */
public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SubSettingLauncher(this)
                .setDestination(About.class.getName())
                .setSourceMetricsCategory(MetricsEvent.ALPHA)
                .launch();
        finish();
    }
}
