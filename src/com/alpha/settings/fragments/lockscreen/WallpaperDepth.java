/*
 * Copyright (C) 2023 BlackIron Project
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

package com.alpha.settings.fragments.lockscreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

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

import java.util.List;

import com.alpha.settings.utils.ImageUtils;

@SearchIndexable
public class WallpaperDepth extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener  {

    public static final String TAG = "WallpaperDepth";

    private static final String DEPTH_WALLPAPER_KEY = "depth_wallpaper_subject_image_uri";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wallpaper_depth);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }


    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (DEPTH_WALLPAPER_KEY.equals(preference.getKey())) {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, 10001);
            } catch(Exception e) {
                Toast.makeText(getContext(), R.string.qs_header_needs_gallery, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == 10001) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }

            Context context = getContext();
            final Uri imgUri = result.getData();
            if (imgUri != null) {
                String savedImagePath = ImageUtils.saveImageToInternalStorage(context, imgUri,
                        "depthwallpaper", "DEPTH_WALLPAPER_SUBJECT");
                if (savedImagePath != null) {
                    Settings.System.putStringForUser(context.getContentResolver(),
                            Settings.System.DEPTH_WALLPAPER_IMAGE_URI, savedImagePath, UserHandle.USER_CURRENT);
                }
            }
        }
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.wallpaper_depth) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
