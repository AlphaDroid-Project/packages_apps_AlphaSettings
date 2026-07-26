/*
 * Copyright (C) 2016-2026 crDroid Android Project
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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.CrossWindowBlurListeners;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.alpha.settings.fragments.quicksettings.QsHeaderImageSettings;
import com.alpha.settings.preferences.CustomSeekBarPreference;
import com.alpha.settings.utils.DeviceUtils;

import java.util.List;
import java.util.ArrayList;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment {

    public static final String TAG = "QuickSettings";

    private static final String QS_BRIGHTNESS_CATEGORY = "qs_brightness_slider_category";
    private static final String QS_LAYOUT_CATEGORY = "qs_layout_category";
    private static final String KEY_BRIGHTNESS_SLIDER_HAPTIC = "qs_brightness_slider_haptic";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String KEY_QS_TILE_HAPTIC = "qs_tile_haptic";
    private static final String KEY_QS_BLUR_INTENSITY = "qs_panel_blur_intensity";

    private SwitchPreferenceCompat mBrightnessSliderHaptic;
    private SwitchPreferenceCompat mShowAutoBrightness;
    private SwitchPreferenceCompat mQsTileHaptic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alpha_settings_quicksettings);

        final Context context = getContext();

        PreferenceCategory brightnessCategory = (PreferenceCategory) findPreference(QS_BRIGHTNESS_CATEGORY);
        PreferenceCategory tileCategory = (PreferenceCategory) findPreference(QS_LAYOUT_CATEGORY);

        mBrightnessSliderHaptic = findPreference(KEY_BRIGHTNESS_SLIDER_HAPTIC);
        mQsTileHaptic = findPreference(KEY_QS_TILE_HAPTIC);
        boolean hapticAvailable = DeviceUtils.hasVibrator(context);

        if (!hapticAvailable) {
            brightnessCategory.removePreference(mBrightnessSliderHaptic);
            tileCategory.removePreference(mQsTileHaptic);
        }

        mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
        boolean automaticAvailable = context.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);

        if (!automaticAvailable) {
            brightnessCategory.removePreference(mShowAutoBrightness);
        }

        updateQsBlurPreferenceState(context);
    }

    /**
     * Device/build can render window blur ({@code ro.surface_flinger.supports_background_blur},
     * {@link CrossWindowBlurListeners#CROSS_WINDOW_BLUR_SUPPORTED}). If false, the blur intensity
     * row is not shown (same as “removed”).
     */
    static boolean isQsBlurSupported(Context context) {
        if (!CrossWindowBlurListeners.CROSS_WINDOW_BLUR_SUPPORTED) {
            return false;
        }
        return SystemProperties.getInt("ro.surface_flinger.supports_background_blur", 0) == 1;
    }

    /**
     * Window blurs are allowed ({@link Settings.Global#DISABLE_WINDOW_BLURS} == 0). Only relevant when
     * {@link #isQsBlurSupported} is true; when supported but this is false, the preference stays
     * visible and is disabled.
     */
    private static boolean isWindowBlurEnabled(Context context) {
        return Settings.Global.getInt(
                context.getContentResolver(), Settings.Global.DISABLE_WINDOW_BLURS, 0) == 0;
    }

    private void updateQsBlurPreferenceState(Context context) {
        Preference blurPref = findPreference(KEY_QS_BLUR_INTENSITY);
        if (blurPref == null) {
            return;
        }

        if (!isQsBlurSupported(context)) {
            blurPref.setVisible(false);
            return;
        }

        blurPref.setVisible(true);

        if (!isWindowBlurEnabled(context)) {
            blurPref.setEnabled(false);
            blurPref.setSummary(context.getString(R.string.qs_blur_intensity_summary) + " "
                    + context.getString(R.string.qs_blur_disabled_in_settings));
        } else {
            blurPref.setEnabled(true);
            blurPref.setSummary(context.getString(R.string.qs_blur_intensity_summary));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getContext();
        if (context != null) {
            updateQsBlurPreferenceState(context);
        }
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.ENABLE_LOCKSCREEN_QUICK_SETTINGS, 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.QS_PANEL_SCRIM_ALPHA, 100, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.QS_PANEL_BLUR_INTENSITY, 100, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_BRIGHTNESS_SLIDER_HAPTIC, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_BRIGHTNESS_SLIDER_SHAPE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_HAPTIC, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_SHOW_AUTO_BRIGHTNESS, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putStringForUser(resolver,
                Settings.System.QS_TILE_ICON_SHAPE, "circle", UserHandle.USER_CURRENT);
        QsHeaderImageSettings.reset(mContext);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.alpha_settings_quicksettings) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();

                    boolean automaticAvailable = res.getBoolean(
                            com.android.internal.R.bool.config_automatic_brightness_available);
                    if (!automaticAvailable) {
                        keys.add(KEY_SHOW_AUTO_BRIGHTNESS);
                    }

                    boolean hapticAvailable = DeviceUtils.hasVibrator(context);
                    if (!hapticAvailable) {
                        keys.add(KEY_BRIGHTNESS_SLIDER_HAPTIC);
                        keys.add(KEY_QS_TILE_HAPTIC);
                    }

                    if (!isQsBlurSupported(context)) {
                        keys.add(KEY_QS_BLUR_INTENSITY);
                    }

                    return keys;
                }
            };
}
