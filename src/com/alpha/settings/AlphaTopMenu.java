package com.alpha.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.alpha.settings.trampoline.ButtonsActivity;
import com.alpha.settings.trampoline.LockScreenActivity;
import com.alpha.settings.trampoline.MiscellaneousActivity;
import com.alpha.settings.trampoline.NotificationsActivity;
import com.alpha.settings.trampoline.QuickSettingsActivity;
import com.alpha.settings.trampoline.SoundActivity;
import com.alpha.settings.trampoline.StatusBarActivity;
import com.alpha.settings.trampoline.UserInterfaceActivity;

import com.android.settings.R;

import com.android.settingslib.widget.LayoutPreference;

public class AlphaTopMenu extends Preference {

    public AlphaTopMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.alpha_top_menu);

    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final boolean selectable = false;
        final Context context = getContext();
        View v = holder.itemView;

        v.setFocusable(selectable);
        v.setClickable(selectable);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);

        // About Alpha
        FrameLayout aboutLayout = v.findViewById(R.id.about_layout);
        if (aboutLayout != null) {
            aboutLayout.setClickable(true);
            aboutLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.AboutActivity"));
                    context.startActivity(intent);
                }
            });
        }

        // Buttons
        FrameLayout buttonsLayout = v.findViewById(R.id.buttons_layout);
        if (buttonsLayout != null) {
            buttonsLayout.setClickable(true);
            buttonsLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.ButtonsActivity"));
                    context.startActivity(intent);
                }
            });
        }

        // Lock Screen
        FrameLayout lockscreenLayout = v.findViewById(R.id.lockscreen_layout);
        if (lockscreenLayout != null) {
            lockscreenLayout.setClickable(true);
            lockscreenLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.LockScreenActivity"));
                    context.startActivity(intent);
                }
            });
        }

        // Miscellaneous
        FrameLayout miscLayout = v.findViewById(R.id.misc_layout);

        if (miscLayout != null) {
            miscLayout.setClickable(true);
            miscLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.MiscellaneousActivity"));
                    context.startActivity(intent);
                }
            });
        }

        // Notifications
        FrameLayout notificationsLayout = v.findViewById(R.id.notifications_layout);

        if (notificationsLayout != null) {
            notificationsLayout.setClickable(true);
            notificationsLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.NotificationsActivity"));
                    context.startActivity(intent);
                }
            });
        }


        // Quick Settings
        FrameLayout qsLayout = v.findViewById(R.id.quick_settings_layout);

        if (qsLayout != null) {
            qsLayout.setClickable(true);
            qsLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.QuickSettingsActivity"));
                    context.startActivity(intent);
                }
            });
        }

        // Sound
        FrameLayout soundLayout = v.findViewById(R.id.sound_layout);

        if (soundLayout != null) {
            soundLayout.setClickable(true);
            soundLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.SoundActivity"));
                    context.startActivity(intent);
                }
            });
        }

        // Status Bar
        FrameLayout sbLayout = v.findViewById(R.id.statusbar_layout);

        if (sbLayout != null) {
            sbLayout.setClickable(true);
            sbLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.StatusBarActivity"));
                    context.startActivity(intent);
                }
            });
        }

        // User Interface
        FrameLayout uiSettingsLayout = v.findViewById(R.id.ui_layout);

        if (uiSettingsLayout != null) {
            uiSettingsLayout.setClickable(true);
            uiSettingsLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.alpha.settings.trampoline.UserInterfaceActivity"));
                    context.startActivity(intent);
                }
            });
        }
    }
}
