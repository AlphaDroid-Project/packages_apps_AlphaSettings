/*
 * Copyright (C) 2021-2026 crDroid Android Project
 * Copyright (C) 2026 AlphaDroid
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
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.alpha.settings.preferences.CustomSeekBarPreference;
import com.alpha.settings.preferences.colorpicker.ColorPickerPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Settings screen for Material You (Monet) tuning: theme style, color source, accent/background
 * colors, luminance/chroma sliders, and optional neutral tint — persisted in
 * {@link Settings.Secure#THEME_CUSTOMIZATION_OVERLAY_PACKAGES} with debounced writes to limit
 * overlay churn.
 */
@SearchIndexable
public class MonetSettings extends DashboardFragment implements OnPreferenceChangeListener {
    private static final String TAG = "MonetSettings";

    /** Internal list value when home + lock wallpapers are both used as color sources. */
    private static final String COLOR_SOURCE_LIST_BOTH = "both";

    private static final String OVERLAY_CATEGORY_ACCENT_COLOR =
            "android.theme.customization.accent_color";
    private static final String OVERLAY_CATEGORY_SYSTEM_PALETTE =
            "android.theme.customization.system_palette";
    private static final String OVERLAY_CATEGORY_THEME_STYLE =
            "android.theme.customization.theme_style";
    private static final String OVERLAY_CATEGORY_BG_COLOR =
            "android.theme.customization.bg_color";
    private static final String OVERLAY_COLOR_SOURCE =
            "android.theme.customization.color_source";
    private static final String OVERLAY_COLOR_BOTH =
            "android.theme.customization.color_both";
    private static final String OVERLAY_LUMINANCE_FACTOR =
            "android.theme.customization.luminance_factor";
    private static final String OVERLAY_CHROMA_FACTOR =
            "android.theme.customization.chroma_factor";
    private static final String OVERLAY_TINT_BACKGROUND =
            "android.theme.customization.tint_background";
    private static final String OVERLAY_FIDELITY =
            "android.theme.customization.fidelity";
    private static final String TIMESTAMP_FIELD = "_applied_timestamp";

    private static final String COLOR_SOURCE_PRESET = "preset";
    private static final String COLOR_SOURCE_HOME = "home_wallpaper";
    private static final String DEFAULT_STYLE = "TONAL_SPOT";
    private static final String STYLE_MONOCHROMATIC = "MONOCHROMATIC";
    private static final String STYLE_RAINBOW = "RAINBOW";

    private static final String PREF_THEME_STYLE = "theme_style";
    private static final String PREF_COLOR_SOURCE = "color_source";
    private static final String PREF_ACCENT_COLOR = "accent_color";
    private static final String PREF_BG_COLOR = "bg_color";
    private static final String PREF_LUMINANCE = "luminance_factor";
    private static final String PREF_CHROMA = "chroma_factor";
    private static final String PREF_TINT_BACKGROUND = "tint_background";
    private static final String PREF_FIDELITY = "fidelity";

    private static final int DEFAULT_COLOR = 0xFF1B6EF3;

    private ListPreference mThemeStyle;
    private ListPreference mColorSource;
    private ColorPickerPreference mAccentColor;
    private ColorPickerPreference mBgColor;
    private CustomSeekBarPreference mLuminance;
    private CustomSeekBarPreference mChroma;
    private SwitchPreferenceCompat mTintBackground;
    private SwitchPreferenceCompat mFidelity;
    private SharedPreferences mPrefs;

    private int mAccentValue = DEFAULT_COLOR;
    private int mBgValue = DEFAULT_COLOR;
    private String mCurrentSource = COLOR_SOURCE_HOME;

    private final Handler mSettingsHandler = new Handler(Looper.getMainLooper());
    private Runnable mDebouncedPutRunnable;
    private String mPendingSettingsJson;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.monet_engine;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThemeStyle = Objects.requireNonNull(findPreference(PREF_THEME_STYLE));
        mColorSource = Objects.requireNonNull(findPreference(PREF_COLOR_SOURCE));
        mAccentColor = Objects.requireNonNull(findPreference(PREF_ACCENT_COLOR));
        mBgColor = Objects.requireNonNull(findPreference(PREF_BG_COLOR));
        mLuminance = Objects.requireNonNull(findPreference(PREF_LUMINANCE));
        mChroma = Objects.requireNonNull(findPreference(PREF_CHROMA));
        mTintBackground = Objects.requireNonNull(findPreference(PREF_TINT_BACKGROUND));
        mFidelity = Objects.requireNonNull(findPreference(PREF_FIDELITY));
        mPrefs = requireActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);

        updatePreferences();
        mThemeStyle.setOnPreferenceChangeListener(this);
        mColorSource.setOnPreferenceChangeListener(this);
        mAccentColor.setOnPreferenceChangeListener(this);
        mBgColor.setOnPreferenceChangeListener(this);
        mLuminance.setOnPreferenceChangeListener(this);
        mChroma.setOnPreferenceChangeListener(this);
        mTintBackground.setOnPreferenceChangeListener(this);
        mFidelity.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Keep the Monet mock above the preference list (standard Settings pinned_header slot).
        final ViewGroup pinned = view.findViewById(R.id.pinned_header);
        if (pinned != null) {
            if (pinned.getChildCount() == 0) {
                setPinnedHeaderView(R.layout.monet_preview);
            }
            // Pinned FrameLayout has no style background; match SubSettings content color.
            pinned.setBackgroundColor(resolveActivityColorBackground(requireActivity()));
            final float d = getResources().getDisplayMetrics().density;
            ViewCompat.setElevation(pinned, 6f * d);
        }
    }

    private static int resolveActivityColorBackground(@NonNull Context activityContext) {
        final TypedArray ta = activityContext.obtainStyledAttributes(
                new int[]{android.R.attr.colorBackground});
        try {
            return ta.getColor(0, 0);
        } finally {
            ta.recycle();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    @Override
    public void onPause() {
        flushPendingSettingsWrite();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        flushPendingSettingsWrite();
        super.onDestroy();
    }

    private void updatePreferences() {
        try {
            JSONObject obj = getSettingsJson();
            final String style = obj.optString(OVERLAY_CATEGORY_THEME_STYLE, DEFAULT_STYLE);
            stripStyleIncompatibleKeys(obj, style);
            setListValue(mThemeStyle, style);

            final String source = obj.optString(OVERLAY_COLOR_SOURCE, COLOR_SOURCE_HOME);
            final boolean both = obj.optInt(OVERLAY_COLOR_BOTH, 0) == 1;
            mCurrentSource = (both && COLOR_SOURCE_HOME.equals(source))
                    ? COLOR_SOURCE_LIST_BOTH : source;
            setListValue(mColorSource, mCurrentSource);

            if (obj.has(OVERLAY_CATEGORY_SYSTEM_PALETTE)) {
                mAccentValue = ColorPickerPreference.convertToColorInt(
                        obj.optString(OVERLAY_CATEGORY_SYSTEM_PALETTE));
            } else {
                mAccentValue = mPrefs.getInt(PREF_ACCENT_COLOR, DEFAULT_COLOR);
            }
            mAccentColor.setNewPreviewColor(mAccentValue);

            if (obj.has(OVERLAY_CATEGORY_BG_COLOR)) {
                mBgValue = obj.optInt(OVERLAY_CATEGORY_BG_COLOR, DEFAULT_COLOR);
            } else {
                mBgValue = mPrefs.getInt(PREF_BG_COLOR, DEFAULT_COLOR);
            }
            mBgColor.setNewPreviewColor(mBgValue);

            mLuminance.setValue(toSlider(obj.optDouble(OVERLAY_LUMINANCE_FACTOR, 1.0)));
            mChroma.setValue(toSlider(obj.optDouble(OVERLAY_CHROMA_FACTOR, 1.0)));
            mTintBackground.setChecked(obj.optInt(OVERLAY_TINT_BACKGROUND, 0) == 1);
            mFidelity.setChecked(obj.optInt(OVERLAY_FIDELITY, 0) == 1);

            applyStyleUiPolicy(style);
        } catch (JSONException e) {
            Log.w(TAG, "Could not read theme overlay settings", e);
        }
    }

    /** Monochrome and Rainbow do not use custom neutral tint or custom background color. */
    private static boolean isBackgroundTintDisallowed(String style) {
        return STYLE_MONOCHROMATIC.equals(style) || STYLE_RAINBOW.equals(style);
    }

    /**
     * Monochromatic scheme ignores chroma / fidelity; drop keys so overlays stay clean.
     * Mono and Rainbow also cannot use custom bg tint or custom bg color.
     */
    private void stripStyleIncompatibleKeys(JSONObject obj, String style) throws JSONException {
        boolean changed = false;
        if (STYLE_MONOCHROMATIC.equals(style)) {
            if (obj.has(OVERLAY_CHROMA_FACTOR)) {
                obj.remove(OVERLAY_CHROMA_FACTOR);
                changed = true;
            }
            if (obj.has(OVERLAY_FIDELITY)) {
                obj.remove(OVERLAY_FIDELITY);
                changed = true;
            }
        }
        if (isBackgroundTintDisallowed(style)) {
            if (obj.has(OVERLAY_TINT_BACKGROUND)) {
                obj.remove(OVERLAY_TINT_BACKGROUND);
                changed = true;
            }
            if (obj.has(OVERLAY_CATEGORY_BG_COLOR)) {
                obj.remove(OVERLAY_CATEGORY_BG_COLOR);
                changed = true;
            }
        }
        if (changed) {
            putSettingsJson(obj);
        }
    }

    private boolean isMonochromeRestricted() {
        return STYLE_MONOCHROMATIC.equals(mThemeStyle.getValue());
    }

    private void applyBackgroundTintPolicy(String style) {
        final boolean disallow = isBackgroundTintDisallowed(style);
        mTintBackground.setEnabled(!disallow);
        mBgColor.setEnabled(!disallow && mTintBackground.isChecked());
        if (disallow) {
            mTintBackground.setChecked(false);
            mTintBackground.setSummary(STYLE_MONOCHROMATIC.equals(style)
                    ? getString(R.string.monet_engine_tint_disabled_monochrome)
                    : getString(R.string.monet_engine_tint_disabled_rainbow));
            mBgColor.setSummary(getString(R.string.monet_engine_bg_color_disabled_style));
        } else {
            mTintBackground.setSummary(getString(R.string.monet_engine_tint_background_summary));
            mBgColor.setSummary(null);
        }
    }

    /** Monochromatic: only theme style and luminance are user-adjustable; engine ignores the rest. */
    private void applyStyleUiPolicy(String style) {
        if (STYLE_MONOCHROMATIC.equals(style)) {
            final String disabled = getString(R.string.monet_engine_controls_disabled_monochrome);
            mColorSource.setEnabled(false);
            mColorSource.setSummary(disabled);
            mAccentColor.setEnabled(false);
            mAccentColor.setSummary(disabled);
            mChroma.setEnabled(false);
            mChroma.setSummary(disabled);
            mFidelity.setEnabled(false);
            mFidelity.setSummary(disabled);
            mLuminance.setEnabled(true);
            mLuminance.setSummary(getString(R.string.monet_engine_luminance_factor_summary));
            mThemeStyle.setEnabled(true);
            applyBackgroundTintPolicy(style);
            return;
        }
        mColorSource.setEnabled(true);
        setListValue(mColorSource, mCurrentSource);
        mChroma.setEnabled(true);
        mChroma.setSummary(getString(R.string.monet_engine_chroma_factor_summary));
        mFidelity.setEnabled(true);
        mFidelity.setSummary(getString(R.string.monet_engine_fidelity_summary));
        mLuminance.setEnabled(true);
        mLuminance.setSummary(getString(R.string.monet_engine_luminance_factor_summary));
        mThemeStyle.setEnabled(true);
        mAccentColor.setEnabled(COLOR_SOURCE_PRESET.equals(mCurrentSource));
        mAccentColor.setSummary(getString(R.string.monet_engine_custom_color_summary));
        applyBackgroundTintPolicy(style);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        try {
            if (preference == mThemeStyle) {
                String newStyle = (String) value;
                JSONObject obj = getSettingsJson();
                obj.put(OVERLAY_CATEGORY_THEME_STYLE, newStyle);
                stripStyleIncompatibleKeys(obj, newStyle);
                putSettingsJson(obj);
                flushPendingSettingsWrite();
                updatePreferences();
                return true;
            } else if (isMonochromeRestricted()
                    && preference != mThemeStyle
                    && preference != mLuminance) {
                return false;
            } else if (preference == mColorSource) {
                mCurrentSource = (String) value;
                JSONObject obj = getSettingsJson();
                if (COLOR_SOURCE_LIST_BOTH.equals(mCurrentSource)) {
                    obj.put(OVERLAY_COLOR_BOTH, 1);
                    obj.put(OVERLAY_COLOR_SOURCE, COLOR_SOURCE_HOME);
                } else {
                    obj.remove(OVERLAY_COLOR_BOTH);
                    obj.put(OVERLAY_COLOR_SOURCE, mCurrentSource);
                }
                obj.put(TIMESTAMP_FIELD, System.currentTimeMillis());
                if (!COLOR_SOURCE_PRESET.equals(mCurrentSource)) {
                    obj.remove(OVERLAY_CATEGORY_ACCENT_COLOR);
                    obj.remove(OVERLAY_CATEGORY_SYSTEM_PALETTE);
                } else {
                    putAccentKeys(obj);
                }
                putSettingsJson(obj);
                mAccentColor.setEnabled(COLOR_SOURCE_PRESET.equals(mCurrentSource));
                setListValue(mColorSource, mCurrentSource);
                return true;
            } else if (preference == mAccentColor) {
                mAccentValue = (Integer) value;
                mPrefs.edit().putInt(PREF_ACCENT_COLOR, mAccentValue).apply();
                if (COLOR_SOURCE_PRESET.equals(mCurrentSource)) {
                    JSONObject obj = getSettingsJson();
                    putAccentKeys(obj);
                    putSettingsJson(obj);
                }
                return true;
            } else if (preference == mBgColor) {
                mBgValue = (Integer) value;
                mPrefs.edit().putInt(PREF_BG_COLOR, mBgValue).apply();
                JSONObject obj = getSettingsJson();
                obj.put(OVERLAY_CATEGORY_BG_COLOR, mBgValue);
                putSettingsJson(obj);
                return true;
            } else if (preference == mLuminance) {
                writeFactor(OVERLAY_LUMINANCE_FACTOR, (Integer) value);
                return true;
            } else if (preference == mChroma) {
                writeFactor(OVERLAY_CHROMA_FACTOR, (Integer) value);
                return true;
            } else if (preference == mTintBackground) {
                boolean on = (Boolean) value;
                writeBool(OVERLAY_TINT_BACKGROUND, on);
                if (!isBackgroundTintDisallowed(mThemeStyle.getValue())) {
                    mBgColor.setEnabled(on);
                }
                return true;
            } else if (preference == mFidelity) {
                writeBool(OVERLAY_FIDELITY, (Boolean) value);
                return true;
            }
        } catch (JSONException e) {
            Log.w(TAG, "Could not update theme overlay settings", e);
        }
        return false;
    }

    private void writeFactor(String key, int slider) throws JSONException {
        JSONObject obj = getSettingsJson();
        if (slider == 0) {
            obj.remove(key);
        } else {
            obj.put(key, 1d + ((double) slider / 100d));
        }
        putSettingsJson(obj);
    }

    private void writeBool(String key, boolean enabled) throws JSONException {
        JSONObject obj = getSettingsJson();
        if (enabled) {
            obj.put(key, 1);
        } else {
            obj.remove(key);
        }
        putSettingsJson(obj);
    }

    private void putAccentKeys(JSONObject obj) throws JSONException {
        String rgb = ColorPickerPreference.convertToRGB(mAccentValue).replace("#", "");
        obj.put(OVERLAY_CATEGORY_ACCENT_COLOR, rgb);
        obj.put(OVERLAY_CATEGORY_SYSTEM_PALETTE, rgb);
    }

    private void setListValue(ListPreference pref, String value) {
        pref.setValue(value);
        int idx = pref.findIndexOfValue(value);
        if (idx >= 0) {
            pref.setSummary(pref.getEntries()[idx]);
        }
    }

    private static int toSlider(double factor) {
        return (int) Math.round((factor - 1.0) * 100.0);
    }

    private JSONObject getSettingsJson() throws JSONException {
        final Context ctx = getContext();
        if (ctx == null) {
            return new JSONObject();
        }
        final String json = Settings.Secure.getStringForUser(
                ctx.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                UserHandle.USER_CURRENT);
        return (json == null || json.isEmpty()) ? new JSONObject() : new JSONObject(json);
    }

    /**
     * Batched writes: sliders and rapid toggles otherwise trigger dozens of overlay applications
     * per second (see ThemeOverlayController / binder pressure in logcat).
     */
    private void putSettingsJson(JSONObject obj) {
        mPendingSettingsJson = obj.toString();
        if (mDebouncedPutRunnable != null) {
            mSettingsHandler.removeCallbacks(mDebouncedPutRunnable);
        }
        mDebouncedPutRunnable = () -> {
            mDebouncedPutRunnable = null;
            if (mPendingSettingsJson == null) {
                return;
            }
            final String json = mPendingSettingsJson;
            mPendingSettingsJson = null;
            if (getContext() == null) {
                return;
            }
            Settings.Secure.putStringForUser(
                    getContext().getContentResolver(),
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                    json, UserHandle.USER_CURRENT);
        };
        mSettingsHandler.postDelayed(mDebouncedPutRunnable, 200);
    }

    private void flushPendingSettingsWrite() {
        if (mDebouncedPutRunnable != null) {
            mSettingsHandler.removeCallbacks(mDebouncedPutRunnable);
            mDebouncedPutRunnable = null;
        }
        if (mPendingSettingsJson != null) {
            final String json = mPendingSettingsJson;
            mPendingSettingsJson = null;
            if (getContext() != null) {
                Settings.Secure.putStringForUser(
                        getContext().getContentResolver(),
                        Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                        json, UserHandle.USER_CURRENT);
            }
        }
    }

    public static void reset(@NonNull Context context) {
        Settings.Secure.putIntForUser(
                context.getContentResolver(),
                Settings.Secure.BERRY_BLACK_THEME,
                0,
                UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.monet_engine);
}
