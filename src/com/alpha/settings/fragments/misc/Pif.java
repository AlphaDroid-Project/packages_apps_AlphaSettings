/*
 * Copyright (C) 2023-2024 The Evolution X Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.alpha.settings.fragments.misc;

import android.os.Bundle;
import android.os.SystemProperties;

import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Pif extends SettingsPreferenceFragment {

    private static final String BRAND = "persist.sys.pihooks_BRAND";
    private static final String DEVICE = "persist.sys.pihooks_DEVICE";
    private static final String DEVICE_INITIAL_SDK = "persist.sys.pihooks_DEVICE_INITIAL_SDK_INT";
    private static final String FINGERPRINT= "persist.sys.pihooks_FINGERPRINT";
    private static final String ID = "persist.sys.pihooks_ID";
    private static final String INCREMENTAL = "persist.sys.pihooks_INCREMENTAL";
    private static final String MANUFACTURER = "persist.sys.pihooks_MANUFACTURER";
    private static final String MODEL = "persist.sys.pihooks_MODEL";
    private static final String PRODUCT = "persist.sys.pihooks_PRODUCT";
    private static final String RELEASE = "persist.sys.pihooks_RELEASE";
    private static final String SECURITY_PATCH = "persist.sys.pihooks_SECURITY_PATCH";
    private static final String TAGS = "persist.sys.pihooks_TAGS";
    private static final String TYPE = "persist.sys.pihooks_TYPE";

    private static final String TAG = "Pif";

    private Preference mManufacturerPreference;
    private Preference mModelPreference;
    private Preference mFingerprintPreference;
    private Preference mBrandPreference;
    private Preference mProductPreference;
    private Preference mDevicePreference;
    private Preference mReleasePreference;
    private Preference mIDPreference;
    private Preference mIncrementalPreference;
    private Preference mTypePreference;
    private Preference mTAGSPreference;
    private Preference mSecurityPatchPreference;
    private Preference mDeviceInitialSdkIntPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pif, rootKey);

        mManufacturerPreference = findPreference("manufacturer");
        mModelPreference = findPreference("model");
        mFingerprintPreference = findPreference("fingerprint");
        mBrandPreference = findPreference("brand");
        mProductPreference = findPreference("product");
        mDevicePreference = findPreference("device");
        mReleasePreference = findPreference("release");
        mIDPreference = findPreference("id");
        mIncrementalPreference = findPreference("incremental");
        mTypePreference = findPreference("type");
        mTAGSPreference = findPreference("tags");
        mSecurityPatchPreference = findPreference("security_patch");
        mDeviceInitialSdkIntPreference = findPreference("device_initial_sdk_int");

        mManufacturerPreference.setSummary(SystemProperties.get(MANUFACTURER, ""));
        mModelPreference.setSummary(SystemProperties.get(MODEL, ""));
        mFingerprintPreference.setSummary(SystemProperties.get(FINGERPRINT, ""));
        mBrandPreference.setSummary(SystemProperties.get(BRAND, ""));
        mProductPreference.setSummary(SystemProperties.get(PRODUCT, ""));
        mDevicePreference.setSummary(SystemProperties.get(DEVICE, ""));
        mReleasePreference.setSummary(SystemProperties.get(RELEASE, ""));
        mIDPreference.setSummary(SystemProperties.get(ID, ""));
        mIncrementalPreference.setSummary(SystemProperties.get(INCREMENTAL, ""));
        mTypePreference.setSummary(SystemProperties.get(TYPE, "user"));
        mTAGSPreference.setSummary(SystemProperties.get(TAGS, "release-keys"));
        mSecurityPatchPreference.setSummary(SystemProperties.get(SECURITY_PATCH, ""));
        mDeviceInitialSdkIntPreference.setSummary(SystemProperties.get(DEVICE_INITIAL_SDK, ""));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }
}
