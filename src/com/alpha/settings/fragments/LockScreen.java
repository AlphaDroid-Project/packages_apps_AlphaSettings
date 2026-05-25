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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.alpha.Utils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.alpha.settings.fragments.lockscreen.DozeSettings;
import com.alpha.settings.fragments.lockscreen.PulseSettings;
import com.alpha.settings.fragments.lockscreen.MediaArtSettings;
import com.alpha.settings.fragments.lockscreen.udfps.UdfpsAnimation;
import com.alpha.settings.fragments.lockscreen.udfps.UdfpsIconPicker;
import com.alpha.settings.utils.DeviceUtils;
import com.alpha.settings.utils.SystemUtils;
import com.alpha.settings.utils.TelephonyUtils;

import java.util.List;

@SearchIndexable
public class LockScreen extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener  {

    public static final String TAG = "LockScreen";

    private static final String LOCKSCREEN_GESTURES_CATEGORY = "lockscreen_gestures_category";
    private static final String LOCKSCREEN_INTERFACE_CATEGORY = "lockscreen_interface_category";
    private static final String KEY_RIPPLE_EFFECT = "enable_ripple_effect";
    private static final String KEY_UDFPS_ANIMATIONS = "udfps_recognizing_animation_preview";
    private static final String KEY_UDFPS_ICONS = "udfps_icon_picker";

    private static final String KEY_FP_SUCCESS = "fp_success_vibrate";
    private static final String KEY_FP_ERROR = "fp_error_vibrate";

    private static final String KEY_CARRIER_NAME = "lockscreen_show_carrier";

    private Preference mUdfpsAnimations;
    private Preference mUdfpsIcons;
    private Preference mRippleEffect;
    private SwitchPreferenceCompat mFpSuccessVib;
    private SwitchPreferenceCompat mFpErrorVib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alpha_settings_lockscreen);
        final Context context = getContext();

        PreferenceCategory gestCategory = (PreferenceCategory) findPreference(LOCKSCREEN_GESTURES_CATEGORY);

        mUdfpsAnimations = (Preference) findPreference(KEY_UDFPS_ANIMATIONS);
        mUdfpsIcons = (Preference) findPreference(KEY_UDFPS_ICONS);
        mFpSuccessVib = findPreference(KEY_FP_SUCCESS);
        mFpErrorVib = findPreference(KEY_FP_ERROR);
        mRippleEffect = (Preference) findPreference(KEY_RIPPLE_EFFECT);

        boolean hasFingerprint = DeviceUtils.hasFingerprint(context);
        if (!hasFingerprint) {
            gestCategory.removePreference(mUdfpsAnimations);
            gestCategory.removePreference(mUdfpsIcons);
            gestCategory.removePreference(mRippleEffect);
        } else {
            if (!Utils.isPackageInstalled(context, "com.alpha.udfps.animations")) {
                gestCategory.removePreference(mUdfpsAnimations);
            }
            if (!Utils.isPackageInstalled(context, "com.alpha.udfps.icons")) {
                gestCategory.removePreference(mUdfpsIcons);
            }
        }

        boolean hapticAvailable = DeviceUtils.hasVibrator(context);
        if (!hasFingerprint || !hapticAvailable) {
            gestCategory.removePreference(mFpSuccessVib);
            gestCategory.removePreference(mFpErrorVib);
        }

        if (!TelephonyUtils.isVoiceCapable(context)) {
            PreferenceCategory intCategory = (PreferenceCategory) findPreference(LOCKSCREEN_INTERFACE_CATEGORY);
            SwitchPreferenceCompat carrierName = findPreference(KEY_CARRIER_NAME);
            intCategory.removePreference(carrierName);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_BATTERY_INFO, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.DOUBLE_TAP_SLEEP_LOCKSCREEN, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ENABLE_RIPPLE_EFFECT, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FP_ERROR_VIBRATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FP_SUCCESS_VIBRATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_ENABLE_POWER_MENU, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_ENABLED, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_LOCATION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_TEXT, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_WIND_INFO, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_HUMIDITY_INFO, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                "quick_look_calendar", 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                "quick_look_alarm", 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                "quick_look_media", 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                "quick_look_now_playing", 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                "quick_look_google_smartspace", 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                "quick_look_smartspacer", 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_SHOW_CARRIER, 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.PULSE_ON_NEW_TRACKS, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_ALWAYS_ON_WALLPAPER_ENABLED, mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_dozeSupportsAodWallpaper) ? 1 : 0,
                UserHandle.USER_CURRENT);

        DozeSettings.reset(mContext);
        PulseSettings.reset(mContext);
        MediaArtSettings.reset(mContext);
        UdfpsAnimation.Companion.reset(mContext);
        UdfpsIconPicker.Companion.reset(mContext);
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
            new BaseSearchIndexProvider(R.xml.alpha_settings_lockscreen) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    boolean hasFingerprint = DeviceUtils.hasFingerprint(context);
                    if (!hasFingerprint) {
                        keys.add(KEY_UDFPS_ANIMATIONS);
                        keys.add(KEY_UDFPS_ICONS);
                        keys.add(KEY_RIPPLE_EFFECT);
                    } else {
                        if (!Utils.isPackageInstalled(context, "com.alpha.udfps.animations")) {
                            keys.add(KEY_UDFPS_ANIMATIONS);
                        }
                        if (!Utils.isPackageInstalled(context, "com.alpha.udfps.icons")) {
                            keys.add(KEY_UDFPS_ICONS);
                        }
                    }
                    boolean hapticAvailable = DeviceUtils.hasVibrator(context);
                    if (!hasFingerprint || !hapticAvailable) {
                        keys.add(KEY_FP_SUCCESS);
                        keys.add(KEY_FP_ERROR);
                    }
                    if (!TelephonyUtils.isVoiceCapable(context)) {
                        keys.add(KEY_CARRIER_NAME);
                    }
                    return keys;
                }
            };
}
