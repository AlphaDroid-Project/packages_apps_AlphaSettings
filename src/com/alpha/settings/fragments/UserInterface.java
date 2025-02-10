/*
 * Copyright (C) 2016-2024 crDroid Android Project
 *               2023-2025 AlphaDroid
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
package com.alpha.settings.fragments;

import static com.android.settings.alpha.AlphaConstants.DEFAULT_DASHBOARD_STYLE;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.alpha.SystemRestartUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import lineageos.providers.LineageSettings;

@SearchIndexable
public class UserInterface extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener{

    public static final String TAG = "UserInterface";

    private static final String KEY_DASHBOARD_STYLE = "settings_dashboard_style";

    private ListPreference mDashBoardStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alpha_settings_ui);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        ListPreference mDashBoardStyle =
                prefScreen.findPreference(KEY_DASHBOARD_STYLE);
        mDashBoardStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_DASHBOARD_STYLE.equals(preference.getKey())) {
            SystemRestartUtils.showSettingsRestartDialog(getContext());
            return true;
        }
        return false;
    }

    public static void reset(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.SETTINGS_DASHBOARD_STYLE,
                DEFAULT_DASHBOARD_STYLE, UserHandle.USER_CURRENT);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.alpha_settings_ui) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
