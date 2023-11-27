/*
 * Copyright (C) 2017-2022 crDroid Android Project
 * Copyright (C) 2023 AlphaDroid
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

import com.alpha.settings.fragments.Buttons;
import com.alpha.settings.fragments.LockScreen;
import com.alpha.settings.fragments.QuickSettings;
import com.alpha.settings.fragments.Miscellaneous;
import com.alpha.settings.fragments.Navigation;
import com.alpha.settings.fragments.Notifications;
import com.alpha.settings.fragments.Sound;
import com.alpha.settings.fragments.StatusBar;
import com.alpha.settings.fragments.UserInterface;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class AlphaSettings extends DashboardFragment {

    private static final String TAG = "AlphaSettings";

    private static final int MENU_RESET = Menu.FIRST;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset_settings_title)
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
            Buttons.reset(rContext);
            LockScreen.reset(rContext);
            QuickSettings.reset(rContext);
            Miscellaneous.reset(rContext);
            Navigation.reset(rContext);
            Notifications.reset(rContext);
            Sound.reset(rContext);
            StatusBar.reset(rContext);
            UserInterface.reset(rContext);
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
        return R.layout.alpha_settings;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.layout.alpha_settings);
}
