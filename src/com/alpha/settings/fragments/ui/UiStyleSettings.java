/*
 * Copyright (C) 2025 AlphaDroid
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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.internal.alpha.style.UserStyleSettings;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.alpha.settings.preferences.CustomSeekBarPreference;
import com.alpha.settings.preferences.ResetCategoryPreference;

@SearchIndexable
public class UiStyleSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "UiStyleSettings";

    private static final String KEY_UI_STYLE = "ui_style";

    private static final String KEY_LIGHT_SATURATION = "light_saturation";
    private static final String KEY_LIGHT_LIGHTNESS = "light_lightness";
    private static final String KEY_LIGHT_OPACITY = "light_opacity";
    private static final String KEY_LIGHT_STRENGTH = "light_strength";
    private static final String KEY_LIGHT_ANGLE = "light_angle";

    private static final String KEY_DARK_SATURATION = "dark_saturation";
    private static final String KEY_DARK_LIGHTNESS = "dark_lightness";
    private static final String KEY_DARK_OPACITY = "dark_opacity";
    private static final String KEY_DARK_STRENGTH = "dark_strength";
    private static final String KEY_DARK_ANGLE = "dark_angle";

    private static final String SETTING_UI_STYLE = "ui_style";

    private static final int MENU_RESET_ALL = Menu.FIRST;

    private ListPreference mUiStylePref;

    private CustomSeekBarPreference mLightSaturation;
    private CustomSeekBarPreference mLightLightness;
    private CustomSeekBarPreference mLightOpacity;
    private CustomSeekBarPreference mLightStrength;
    private CustomSeekBarPreference mLightAngle;

    private CustomSeekBarPreference mDarkSaturation;
    private CustomSeekBarPreference mDarkLightness;
    private CustomSeekBarPreference mDarkOpacity;
    private CustomSeekBarPreference mDarkStrength;
    private CustomSeekBarPreference mDarkAngle;

    private ResetCategoryPreference mLightCategory;
    private ResetCategoryPreference mDarkCategory;

    private String mCurrentStyleId = "system_default";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ui_style_settings);

        setHasOptionsMenu(true);

        final ContentResolver resolver = getContentResolver();

        mUiStylePref = findPreference(KEY_UI_STYLE);
        mCurrentStyleId = Settings.System.getStringForUser(resolver,
                SETTING_UI_STYLE, UserHandle.USER_CURRENT);
        if (mCurrentStyleId == null || mCurrentStyleId.isEmpty()) {
            mCurrentStyleId = "system_default";
        }
        mUiStylePref.setValue(mCurrentStyleId);
        mUiStylePref.setSummary(mUiStylePref.getEntry());
        mUiStylePref.setOnPreferenceChangeListener(this);

        mLightCategory = findPreference("category_light_theme");
        if (mLightCategory != null) {
            mLightCategory.setOnResetClickListener(v -> resetLightSection());
        }

        mDarkCategory = findPreference("category_dark_theme");
        if (mDarkCategory != null) {
            mDarkCategory.setOnResetClickListener(v -> resetDarkSection());
        }

        mLightSaturation = findPreference(KEY_LIGHT_SATURATION);
        mLightLightness = findPreference(KEY_LIGHT_LIGHTNESS);
        mLightOpacity = findPreference(KEY_LIGHT_OPACITY);
        mLightStrength = findPreference(KEY_LIGHT_STRENGTH);
        mLightAngle = findPreference(KEY_LIGHT_ANGLE);

        mDarkSaturation = findPreference(KEY_DARK_SATURATION);
        mDarkLightness = findPreference(KEY_DARK_LIGHTNESS);
        mDarkOpacity = findPreference(KEY_DARK_OPACITY);
        mDarkStrength = findPreference(KEY_DARK_STRENGTH);
        mDarkAngle = findPreference(KEY_DARK_ANGLE);

        setSeekBarListeners();
        loadSettingsForStyle(mCurrentStyleId);
        updateCategoryVisibility();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET_ALL, 0, R.string.ui_style_reset_all)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_RESET_ALL) {
            resetAllStyles();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSeekBarListeners() {
        mLightSaturation.setOnPreferenceChangeListener(this);
        mLightLightness.setOnPreferenceChangeListener(this);
        mLightOpacity.setOnPreferenceChangeListener(this);
        mLightStrength.setOnPreferenceChangeListener(this);
        mLightAngle.setOnPreferenceChangeListener(this);

        mDarkSaturation.setOnPreferenceChangeListener(this);
        mDarkLightness.setOnPreferenceChangeListener(this);
        mDarkOpacity.setOnPreferenceChangeListener(this);
        mDarkStrength.setOnPreferenceChangeListener(this);
        mDarkAngle.setOnPreferenceChangeListener(this);
    }

    private void updateCategoryVisibility() {
        boolean isSystemDefault = "system_default".equals(mCurrentStyleId);
        mLightCategory.setVisible(!isSystemDefault);
        mDarkCategory.setVisible(!isSystemDefault);
    }

    private String getParamsKey(String styleId) {
        switch (styleId) {
            case "outline": return "ui_style_outline_params";
            case "neon": return "ui_style_neon_params";
            case "bevel": return "ui_style_bevel_params";
            case "gradient": return "ui_style_gradient_params";
            case "reflective": return "ui_style_reflective_params";
            case "slash": return "ui_style_slash_params";
            case "aerogel": return "ui_style_aerogel_params";
            case "metallic": return "ui_style_metallic_params";
            default: return null;
        }
    }

    private void loadSettingsForStyle(String styleId) {
        String paramsKey = getParamsKey(styleId);
        if (paramsKey == null) {
            setSeekBarDefaults();
            return;
        }

        ContentResolver resolver = getContentResolver();
        String combined = Settings.System.getStringForUser(resolver, paramsKey, UserHandle.USER_CURRENT);

        UserStyleSettings lightSettings = parseSettingsForMode(combined, false);
        mLightSaturation.setValue(Math.round(lightSettings.getSaturation() * 100));
        mLightLightness.setValue(Math.round(lightSettings.getLightness() * 100));
        mLightOpacity.setValue(Math.round(lightSettings.getOpacity() * 100));
        mLightStrength.setValue(Math.round(lightSettings.getStrength() * 100));
        mLightAngle.setValue(Math.round(lightSettings.getAngle()));

        UserStyleSettings darkSettings = parseSettingsForMode(combined, true);
        mDarkSaturation.setValue(Math.round(darkSettings.getSaturation() * 100));
        mDarkLightness.setValue(Math.round(darkSettings.getLightness() * 100));
        mDarkOpacity.setValue(Math.round(darkSettings.getOpacity() * 100));
        mDarkStrength.setValue(Math.round(darkSettings.getStrength() * 100));
        mDarkAngle.setValue(Math.round(darkSettings.getAngle()));
    }

    private UserStyleSettings parseSettingsForMode(String combined, boolean isDark) {
        if (combined == null || combined.isEmpty()) {
            return UserStyleSettings.DEFAULT;
        }

        String prefix = isDark ? "dark:" : "light:";
        String[] parts = combined.split("\\|");
        for (String part : parts) {
            if (part.startsWith(prefix)) {
                String params = part.substring(prefix.length());
                return UserStyleSettings.fromString(params);
            }
        }

        return UserStyleSettings.DEFAULT;
    }

    private void setSeekBarDefaults() {
        mLightSaturation.setValue(100);
        mLightLightness.setValue(100);
        mLightOpacity.setValue(100);
        mLightStrength.setValue(100);
        mLightAngle.setValue(0);

        mDarkSaturation.setValue(100);
        mDarkLightness.setValue(100);
        mDarkOpacity.setValue(100);
        mDarkStrength.setValue(100);
        mDarkAngle.setValue(0);
    }

    private void resetLightSection() {
        mLightSaturation.setValue(100);
        mLightLightness.setValue(100);
        mLightOpacity.setValue(100);
        mLightStrength.setValue(100);
        mLightAngle.setValue(0);
        saveSettings();
    }

    private void resetDarkSection() {
        mDarkSaturation.setValue(100);
        mDarkLightness.setValue(100);
        mDarkOpacity.setValue(100);
        mDarkStrength.setValue(100);
        mDarkAngle.setValue(0);
        saveSettings();
    }

    private void resetAllStyles() {
        ContentResolver resolver = getContentResolver();

        Settings.System.putStringForUser(resolver, SETTING_UI_STYLE,
                "system_default", UserHandle.USER_CURRENT);

        String[] keys = {
            "ui_style_outline_params",
            "ui_style_neon_params",
            "ui_style_bevel_params",
            "ui_style_gradient_params",
            "ui_style_reflective_params",
            "ui_style_slash_params",
            "ui_style_aerogel_params",
            "ui_style_metallic_params"
        };
        for (String key : keys) {
            Settings.System.putStringForUser(resolver, key, null, UserHandle.USER_CURRENT);
        }

        mCurrentStyleId = "system_default";
        mUiStylePref.setValue(mCurrentStyleId);
        mUiStylePref.setSummary(mUiStylePref.getEntry());
        setSeekBarDefaults();
        updateCategoryVisibility();
    }

    private UserStyleSettings buildLightSettings() {
        return new UserStyleSettings(
            mLightSaturation.getValue() / 100f,
            mLightLightness.getValue() / 100f,
            mLightOpacity.getValue() / 100f,
            mLightStrength.getValue() / 100f,
            mLightAngle.getValue()
        );
    }

    private UserStyleSettings buildDarkSettings() {
        return new UserStyleSettings(
            mDarkSaturation.getValue() / 100f,
            mDarkLightness.getValue() / 100f,
            mDarkOpacity.getValue() / 100f,
            mDarkStrength.getValue() / 100f,
            mDarkAngle.getValue()
        );
    }

    private void saveSettings() {
        String paramsKey = getParamsKey(mCurrentStyleId);
        if (paramsKey == null) {
            return;
        }

        UserStyleSettings lightSettings = buildLightSettings();
        UserStyleSettings darkSettings = buildDarkSettings();

        String combined = "light:" + lightSettings.toString() + "|dark:" + darkSettings.toString();

        ContentResolver resolver = getContentResolver();
        Settings.System.putStringForUser(resolver, paramsKey, combined, UserHandle.USER_CURRENT);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUiStylePref) {
            String value = (String) newValue;
            Settings.System.putStringForUser(getContentResolver(),
                    SETTING_UI_STYLE, value, UserHandle.USER_CURRENT);
            mCurrentStyleId = value;
            mUiStylePref.setValue(value);
            int index = mUiStylePref.findIndexOfValue(value);
            mUiStylePref.setSummary(mUiStylePref.getEntries()[index]);
            loadSettingsForStyle(value);
            updateCategoryVisibility();
            return true;
        }

        if (preference instanceof CustomSeekBarPreference) {
            ((CustomSeekBarPreference) preference).setValue((Integer) newValue);
            saveSettings();
            return true;
        }

        return false;
    }

    public static void reset(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Settings.System.putStringForUser(resolver, SETTING_UI_STYLE,
                "system_default", UserHandle.USER_CURRENT);

        String[] keys = {
            "ui_style_outline_params",
            "ui_style_neon_params",
            "ui_style_bevel_params",
            "ui_style_gradient_params",
            "ui_style_reflective_params",
            "ui_style_slash_params",
            "ui_style_aerogel_params",
            "ui_style_metallic_params"
        };
        for (String key : keys) {
            Settings.System.putStringForUser(resolver, key, null, UserHandle.USER_CURRENT);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.ui_style_settings);
}
