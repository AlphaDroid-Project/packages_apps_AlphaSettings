/*
 * Copyright (C) 2022 BlackIron Project
 * Copyright (C) 2016-2025 crDroid Android Project
 * Copyright (C) 2023-2025 AlphaDroid
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

package com.alpha.settings.fragments.ui;

import static com.android.internal.util.alpha.AlphaConstants.WALLPAPER_BLUR_TARGET_PROP;
import static com.android.internal.util.alpha.AlphaConstants.WALLPAPER_BLUR_FILTER_PROP;
import static com.android.internal.util.alpha.AlphaConstants.WALLPAPER_DIM_TARGET_PROP;
import static com.android.internal.util.alpha.AlphaConstants.WALLPAPER_DIM_LEVEL_PROP;
import static com.android.internal.util.alpha.AlphaConstants.WALLPAPER_TARGET_DISABLED;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.alpha.settings.preferences.CustomSeekBarPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.alpha.SystemRestartUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.ButtonPreference;

import java.util.List;

@SearchIndexable
public class WallpaperStyle extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "WallpaperStyle";

    private static final String RESTART_BUTTON_KEY = "restart_systemui";

    private ListPreference mBlurWpPref;
    private ListPreference mBlurWpStylePref;
    private ListPreference mDimPref;
    private Preference mDimLvlPref;
    private ButtonPreference mRestartButton;
    private boolean mShowToast = true;
    private boolean mRestartEnabled = false;
    private boolean mBlurEnabled;
    private boolean mDimEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wallpaper_style);

        mBlurWpPref = findPreference(WALLPAPER_BLUR_TARGET_PROP);
        mBlurEnabled = SystemProperties.getInt(WALLPAPER_BLUR_TARGET_PROP,
                WALLPAPER_TARGET_DISABLED) != WALLPAPER_TARGET_DISABLED;
        mBlurWpPref.setOnPreferenceChangeListener(this);

        mBlurWpStylePref = findPreference(WALLPAPER_BLUR_FILTER_PROP);
        mBlurWpStylePref.setOnPreferenceChangeListener(this);

        mDimPref = findPreference(WALLPAPER_DIM_TARGET_PROP);
        mDimEnabled = SystemProperties.getInt(WALLPAPER_DIM_TARGET_PROP,
                WALLPAPER_TARGET_DISABLED) != WALLPAPER_TARGET_DISABLED;
        mDimPref.setOnPreferenceChangeListener(this);

        mDimLvlPref = findPreference(WALLPAPER_DIM_LEVEL_PROP);
        mDimLvlPref.setOnPreferenceChangeListener(this);

        mRestartButton = findPreference(RESTART_BUTTON_KEY);
        mRestartButton.setOnClickListener(view -> {
            SystemRestartUtils.restartSystemUi(getContext());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mBlurWpStylePref.setEnabled(mBlurEnabled);
        mDimLvlPref.setEnabled(mDimEnabled);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        boolean valueChanged = false;
        switch (key) {
            case WALLPAPER_BLUR_TARGET_PROP:
                mBlurEnabled = Integer.parseInt((String) newValue) != WALLPAPER_TARGET_DISABLED;
                mBlurWpStylePref.setEnabled(mBlurEnabled);
                valueChanged = true;
                break;
            case WALLPAPER_DIM_TARGET_PROP:
                mDimEnabled = Integer.parseInt((String) newValue) != WALLPAPER_TARGET_DISABLED;
                mDimLvlPref.setEnabled(mDimEnabled);
                valueChanged = true;
                break;
            case WALLPAPER_DIM_LEVEL_PROP:
            case WALLPAPER_BLUR_FILTER_PROP:
                valueChanged = true;
                break;
            default :
                break;
        }
        if (valueChanged) {
            if (mShowToast) {
                showRestartToast();
                mShowToast = false;
            }
            if (!mRestartEnabled) {
                mRestartButton.setEnabled(true);
                mRestartEnabled = true;
            }
        }
        return valueChanged;
    }

    private void updateDependents() {

    }

    private void showRestartToast() {
        Toast.makeText(getContext(), R.string.restart_systemui, Toast.LENGTH_LONG).show();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.wallpaper_style) {
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
