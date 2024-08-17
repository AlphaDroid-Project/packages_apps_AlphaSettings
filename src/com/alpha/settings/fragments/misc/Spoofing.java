/*
 * Copyright (C) 2024 Project-Pixelstar
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
package com.alpha.settings.fragments.misc;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.alpha.SystemRestartUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;


public class Spoofing extends SettingsPreferenceFragment {

    private static final String TAG = "Spoofing";

    private static final String SYS_PIF_SPOOF = "persist.sys.pif";
    private static final String SYS_PIXELPROPS_SPOOF = "persist.sys.pixelprops";
    private static final String SYS_PHOTOS_SPOOF = "persist.sys.pixelprops.gphotos";
    private static final String SYS_GAMES_SPOOF = "persist.sys.pixelprops.games";
    private static final String SYS_SNAPCHAT_SPOOF = "persist.sys.pixelprops.snapchat";

    private static final String KEY_PIF_JSON_FILE_PREFERENCE = "pif_json_file_preference";

    private Preference mPifJsonFilePreference;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.spoofing_settings);
        mHandler = new Handler();
        mPifJsonFilePreference = findPreference(KEY_PIF_JSON_FILE_PREFERENCE);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mPifJsonFilePreference) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/json");
            startActivityForResult(intent, 10001);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Log.d(TAG, "URI received: " + uri.toString());
            try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    Log.d(TAG, "JSON data: " + json);
                    JSONObject jsonObject = new JSONObject(json);
                    for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                        String key = it.next();
                        String value = jsonObject.getString(key);
                        Log.d(TAG, "Setting property: persist.sys.pihooks_" + key + " = " + value);
                        SystemProperties.set("persist.sys.pihooks_" + key, value);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading JSON or setting properties", e);
            }
            mHandler.postDelayed(() -> {
                SystemRestartUtils.showSystemRestartDialog(getContext());
            }, 1250);
        }
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver, Settings.Secure.HIDE_DEVELOPER_STATUS,
                0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver, Settings.Secure.SPOOF_STORAGE_ENCRYPTION_STATUS,
                0, UserHandle.USER_CURRENT);
        SystemProperties.set(SYS_PIF_SPOOF, "true");
        SystemProperties.set(SYS_PIXELPROPS_SPOOF, "true");
        SystemProperties.set(SYS_GAMES_SPOOF, "false");
        SystemProperties.set(SYS_PHOTOS_SPOOF, "true");
        SystemProperties.set(SYS_SNAPCHAT_SPOOF, "false");
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }
}
