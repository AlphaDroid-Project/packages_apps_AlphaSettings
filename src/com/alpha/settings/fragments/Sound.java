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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.alpha.settings.utils.DeviceUtils;
import com.alpha.settings.utils.TelephonyUtils;

import java.util.List;
import java.util.ArrayList;

@SearchIndexable
public class Sound extends SettingsPreferenceFragment {

    public static final String TAG = "Sound";

    private static final String KEY_VOLUME_CATEGORY = "volume_panel";
    private static final String KEY_VIBRATE_CATEGORY = "incall_vib_options";
    private static final String KEY_VIBRATE_CONNECT = "vibrate_on_connect";
    private static final String KEY_VIBRATE_CALLWAITING = "vibrate_on_callwaiting";
    private static final String KEY_VIBRATE_DISCONNECT = "vibrate_on_disconnect";
    private static final String KEY_VOLUME_PANEL_LEFT = "volume_panel_on_left";
    private static final String KEY_VOLUME_HAPTIC = "volume_dialog_haptic_feedback";
    private static final String KEY_BACKGROUND_MUSIC_RECOGNITION = "background_music_recognition";
    /** ASI's on-demand master. Read to gate our switch; never written here. */
    private static final String KEY_ASI_NOW_PLAYING = "now_playing_enabled";
    /** Where the framework parks the switch while the master is off. */
    private static final String KEY_BMR_SAVED = "background_music_recognition_saved";

    private SwitchPreferenceCompat mBackgroundMusicRecognition;
    private SwitchPreferenceCompat mVolumePanelLeft;
    private SwitchPreferenceCompat mVolumeHaptic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alpha_settings_sound);

        final Context context = getContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        boolean isAudioPanelOnLeft = Settings.Secure.getIntForUser(context.getContentResolver(),
                Settings.Secure.VOLUME_PANEL_ON_LEFT, 0,
                UserHandle.USER_CURRENT) != 0;

        mBackgroundMusicRecognition = prefScreen.findPreference(KEY_BACKGROUND_MUSIC_RECOGNITION);

        mVolumePanelLeft = prefScreen.findPreference(KEY_VOLUME_PANEL_LEFT);
        mVolumePanelLeft.setChecked(isAudioPanelOnLeft);

        boolean voiceCapable = TelephonyUtils.isVoiceCapable(context);
        boolean hapticAvailable = DeviceUtils.hasVibrator(context);

        if (!voiceCapable || !hapticAvailable) {
            final PreferenceCategory vibCategory = prefScreen.findPreference(KEY_VIBRATE_CATEGORY);
            prefScreen.removePreference(vibCategory);
        }
        if (!hapticAvailable) {
            final PreferenceCategory volCategory = prefScreen.findPreference(KEY_VOLUME_CATEGORY);
            mVolumeHaptic = volCategory.findPreference(KEY_VOLUME_HAPTIC);
            volCategory.removePreference(mVolumeHaptic);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBackgroundMusicRecognition != null) {
            // Background recognition has no recognizer without ASI's master, so it is only
            // offerable while that is on. Its own value is left alone either way.
            mBackgroundMusicRecognition.setEnabled(Settings.Secure.getIntForUser(
                    getContext().getContentResolver(), KEY_ASI_NOW_PLAYING, 0,
                    UserHandle.USER_CURRENT) != 0);
        }
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.VOLUME_PANEL_ON_LEFT, 0,
                UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.VOLUME_DIALOG_DISMISS_TIMEOUT, 3000, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.VOLUME_DIALOG_HAPTIC_FEEDBACK, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VIBRATE_ON_CONNECT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VIBRATE_ON_CALLWAITING, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VIBRATE_ON_DISCONNECT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.SCREENSHOT_SHUTTER_SOUND, 1, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                KEY_BACKGROUND_MUSIC_RECOGNITION, 0, UserHandle.USER_CURRENT);
        // Otherwise a parked choice would come back the next time the master is re-enabled.
        Settings.Secure.putIntForUser(resolver, KEY_BMR_SAVED, 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.alpha_settings_sound) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    boolean voiceCapable = TelephonyUtils.isVoiceCapable(context);
                    boolean hapticAvailable = DeviceUtils.hasVibrator(context);

                    if (!voiceCapable || !hapticAvailable) {
                        keys.add(KEY_VIBRATE_CATEGORY);
                        keys.add(KEY_VIBRATE_CONNECT);
                        keys.add(KEY_VIBRATE_CALLWAITING);
                        keys.add(KEY_VIBRATE_DISCONNECT);
                    }
                    if (!hapticAvailable) {
                        keys.add(KEY_VOLUME_HAPTIC);
                    }
                    if (!context.getResources().getBoolean(com.android.internal.R.bool
                            .config_supportsBackgroundMusicRecognition)) {
                        keys.add(KEY_BACKGROUND_MUSIC_RECOGNITION);
                    }
                    return keys;
                }
            };
}
