/*
 * Copyright (C) 2017-2024 crDroid Android Project
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

package com.alpha.settings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;

@SearchIndexable
public class AlphaSettings extends DashboardFragment {

    private static final String TAG = "AlphaSettings";
    private static final int MENU_RESET = Menu.FIRST;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // User Interface (Blue)
        setupSlashPreference("user_interface", R.string.ui_title, R.string.ui_summary,
                R.drawable.ic_settings_ui, "#4285F4", "com.alpha.settings.trampoline.UserInterfaceActivity");

        // Status Bar (Cyan)
        setupSlashPreference("statusbar", R.string.statusbar_title, R.string.statusbar_summary,
                R.drawable.ic_settings_statusbar, "#00ACC1", "com.alpha.settings.trampoline.StatusBarActivity");

        // Quick Settings (Purple)
        setupSlashPreference("quick_settings", R.string.quicksettings_title, R.string.quicksettings_summary,
                R.drawable.ic_settings_quicksettings, "#AB47BC", "com.alpha.settings.trampoline.QuickSettingsActivity");

        // Buttons (Deep Orange)
        setupSlashPreference("buttons", R.string.button_title, R.string.button_summary,
                R.drawable.ic_settings_buttons, "#FF5722", "com.alpha.settings.trampoline.ButtonsActivity");

        // Lock Screen (Red)
        setupSlashPreference("lockscreen", R.string.lockscreen_title, R.string.lockscreen_summary,
                R.drawable.ic_settings_lockscreen, "#EF5350", "com.alpha.settings.trampoline.LockScreenActivity");

        // Notifications (Amber)
        setupSlashPreference("notifications", R.string.notifications_title, R.string.notifications_summary,
                R.drawable.ic_settings_notifications, "#FFA000", "com.alpha.settings.trampoline.NotificationsActivity");

        // Sound (Green)
        setupSlashPreference("sound", R.string.sound_title, R.string.sound_summary,
                R.drawable.ic_settings_sound, "#66BB6A", "com.alpha.settings.trampoline.SoundActivity");

        // Miscellaneous (Indigo)
        setupSlashPreference("misc", R.string.misc_title, R.string.misc_summary,
                R.drawable.ic_settings_misc, "#5C6BC0", "com.alpha.settings.trampoline.MiscellaneousActivity");

        // About (Grey/Black)
        setupSlashPreference("about", R.string.about_title, R.string.about_summary,
                R.drawable.ic_settings_about, "#546E7A", "com.alpha.settings.trampoline.AboutActivity");
    }

    private void setupSlashPreference(String key, int titleRes, int summaryRes, int iconRes, String colorHex, String activityClass) {
        LayoutPreference pref = findPreference(key);
        if (pref != null) {
            View root = pref.findViewById(R.id.slash_row_container);
            View contentArea = root.findViewById(R.id.slash_content_area);

            // --- DYNAMIC TOP MARGIN FOR FIRST ITEM ---
            if ("user_interface".equals(key)) {
                int topMarginPx = (int) (24 * getResources().getDisplayMetrics().density);
                if (root.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                    android.view.ViewGroup.MarginLayoutParams params =
                        (android.view.ViewGroup.MarginLayoutParams) root.getLayoutParams();
                    params.topMargin = topMarginPx;
                    root.setLayoutParams(params);
                }
            }

            TextView titleView = root.findViewById(R.id.slash_title);
            TextView summaryView = root.findViewById(R.id.slash_summary);
            ImageView iconView = root.findViewById(R.id.slash_icon);

            if (titleView != null) titleView.setText(titleRes);
            if (summaryView != null) summaryView.setText(summaryRes);

            // Color Logic
            int baseColor = Color.parseColor(colorHex);
            int iconTint = getDarkerColor(baseColor);

            if (contentArea != null) {
                android.graphics.drawable.Drawable bg = contentArea.getBackground();
                if (bg instanceof android.graphics.drawable.RippleDrawable) {
                    bg = ((android.graphics.drawable.RippleDrawable) bg).getDrawable(0);
                }
                if (bg instanceof android.graphics.drawable.LayerDrawable) {
                    android.graphics.drawable.LayerDrawable layerDrawable = (android.graphics.drawable.LayerDrawable) bg;
                    android.graphics.drawable.Drawable rightLayer = layerDrawable.findDrawableByLayerId(R.id.layer_slash_right);
                    if (rightLayer != null) {
                        rightLayer.mutate().setTint(baseColor);
                    }
                }
            }

            if (iconView != null) {
                iconView.setImageResource(iconRes);
                iconView.setImageTintList(ColorStateList.valueOf(iconTint));
            }

            // Click Listener
            View.OnClickListener listener = v -> {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", activityClass));
                startActivity(intent);
            };

            if (contentArea != null) contentArea.setOnClickListener(listener);
        }
    }

    /**
     * Calculates a darker version of the given color for the icon tint.
     * Reduces brightness (value) to create a high-contrast icon on top of the vivid background.
     */
    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.3f; // Reduce brightness by 70% (was 50%)
        return Color.HSVToColor(hsv);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, "")
                .setIcon(R.drawable.ic_reset)
                .setAlphabeticShortcut('r')
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                        MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    public void resetAll(Context context) {
        new ResetAllTask(context).execute();
    }

    public void showResetAllDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.reset_settings_title)
                .setMessage(R.string.reset_settings_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetAll(context);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private class ResetAllTask extends AsyncTask<Void, Void, Void> {
        private Context rContext;

        public ResetAllTask(Context context) {
            super();
            rContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            finish();
            startActivity(getIntent());
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                 showResetAllDialog(getActivity());
                return true;
            default:
                return false;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.alpha_settings;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.alpha_settings);
}
