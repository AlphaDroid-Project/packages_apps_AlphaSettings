package com.alpha.settings.fragments.misc;

import android.os.Bundle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.ListPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class WeatherSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_WEATHER_PROVIDER = "quick_look_weather_provider";
    private static final String KEY_WEATHER_SETTINGS_APP = "weather_settings_app";

    private ListPreference mWeatherProvider;
    private Preference mWeatherSettingsApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.weather_settings);

        mWeatherProvider = (ListPreference) findPreference(KEY_WEATHER_PROVIDER);
        mWeatherSettingsApp = findPreference(KEY_WEATHER_SETTINGS_APP);

        if (mWeatherProvider != null) {
            mWeatherProvider.setOnPreferenceChangeListener(this);
            int provider = Settings.Secure.getInt(getContentResolver(),
                    KEY_WEATHER_PROVIDER, 1);
            updateWeatherSettings(provider);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWeatherProvider) {
            int provider = Integer.parseInt((String) newValue);
            updateWeatherSettings(provider);
            return true;
        }
        return false;
    }

    private void updateWeatherSettings(int provider) {
        boolean isOmniJaws = provider == 1;
        if (mWeatherSettingsApp != null) {
            mWeatherSettingsApp.setEnabled(isOmniJaws);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }
}
