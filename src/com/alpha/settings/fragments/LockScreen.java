/*
 * Copyright (C) 2016-2025 crDroid Android Project
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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import com.alpha.settings.fragments.lockscreen.DozeSettings;
import com.alpha.settings.fragments.lockscreen.udfps.UdfpsAnimation;
import com.alpha.settings.fragments.lockscreen.udfps.UdfpsIconPicker;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.alpha.OmniJawsClient;
import com.android.internal.util.alpha.Utils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import lineageos.providers.LineageSettings;

@SearchIndexable
public class LockScreen extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener  {

    public static final String TAG = "LockScreen";

    private static final String LOCKSCREEN_INTERFACE_CATEGORY = "lockscreen_interface_category";
    private static final String LOCKSCREEN_GESTURES_CATEGORY = "lockscreen_gestures_category";
    private static final String KEY_SMARTSPACE = "lockscreen_smartspace_enabled";
    private static final String KEY_WEATHER = "lockscreen_weather_enabled";

    private Preference mUdfpsAnimations;
    private Preference mUdfpsIcons;
    private Preference mScreenOffUdfps;

    private SwitchPreferenceCompat mSmartspace;
    private SwitchPreferenceCompat mWeather;

    private OmniJawsClient mWeatherClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alpha_settings_lockscreen);

        mSmartspace = (SwitchPreferenceCompat) findPreference(KEY_SMARTSPACE);
        mSmartspace.setOnPreferenceChangeListener(this);

        mSmartspace = (SwitchPreferenceCompat) findPreference(KEY_SMARTSPACE);
        mSmartspace.setOnPreferenceChangeListener(this);

        mWeather = (SwitchPreferenceCompat) findPreference(KEY_WEATHER);
        mWeather.setOnPreferenceChangeListener(this);

        mWeatherClient = new OmniJawsClient(getContext());
        updateWeatherSettings();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSmartspace) {
            mSmartspace.setChecked((Boolean)newValue);
            updateWeatherSettings();
            showRestartToast();
            return true;
        } else if (preference == mWeather) {
            mWeather.setChecked((Boolean)newValue);
            showRestartToast();
            return true;
        }

        return false;
    }

    private void showRestartToast() {
        Toast.makeText(getContext(), R.string.restart_systemui, Toast.LENGTH_LONG).show();
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.SCREEN_OFF_UDFPS_ENABLED, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.LOCKSCREEN_SMARTSPACE_ENABLED, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_BATTERY_INFO, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_ENABLED, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_LOCATION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_TEXT, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_WIND_INFO, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_HUMIDITY_INFO, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_SHOW_CARRIER, 1, UserHandle.USER_CURRENT);
        DozeSettings.reset(mContext);
        UdfpsAnimation.reset(mContext);
        UdfpsIconPicker.reset(mContext);
    }

    private void updateWeatherSettings() {
        if (mWeatherClient == null || mWeather == null || mSmartspace == null) return;

        boolean weatherEnabled = mWeatherClient.isOmniJawsEnabled();
        mWeather.setEnabled(!mSmartspace.isChecked() && weatherEnabled);
        mWeather.setSummary(!mSmartspace.isChecked() && weatherEnabled ? R.string.lockscreen_weather_summary :
            R.string.lockscreen_weather_enabled_info);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeatherSettings();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.alpha_settings_lockscreen);
}
