/*
 * Copyright (C) 2016-2024 crDroid Android Project
 * Copyright (C) 2025 AlphaDroid
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

import static org.lineageos.internal.util.DeviceKeysConstants.*;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.util.alpha.SystemRestartUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.alpha.settings.fragments.misc.SensorBlock;
import com.alpha.settings.fragments.misc.SmartPixels;


import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.*;
 
import org.json.JSONObject;
import org.w3c.dom.*;


@SearchIndexable
public class Miscellaneous extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "Miscellaneous";

    private static final String POCKET_JUDGE = "pocket_judge";
    private static final String SYS_GAMES_SPOOF = "persist.sys.pp.gam";
    private static final String SYS_PHOTOS_SPOOF = "persist.sys.pp.gph";
    private static final String SYS_NETFLIX_SPOOF = "persist.sys.pp.nf";
    private static final String SMART_PIXELS = "smart_pixels";
    private static final String KEY_THREE_FINGERS_SWIPE = "three_fingers_swipe";
    private static final String KEY_IMPORT_KEYBOX = "import_keybox";
    private static final String KEY_CLEAR_KEYBOX = "clear_keybox";
    private static final String KEYBOX_PATH = "/data/misc/keybox/keybox.xml";

    private Preference mPocketJudge;
    private ListPreference mThreeFingersSwipeAction;
    private Preference mSmartPixels;
    private Preference mImportKeybox;
    private Preference mClearKeybox;

    private final ActivityResultLauncher<String> mImportKeyboxLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleKeyboxImport(uri);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alpha_settings_misc);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        mPocketJudge = (Preference) prefScreen.findPreference(POCKET_JUDGE);
        boolean mPocketJudgeSupported = res.getBoolean(
                com.android.internal.R.bool.config_pocketModeSupported);
        if (!mPocketJudgeSupported && mPocketJudge != null) {
            prefScreen.removePreference(mPocketJudge);
        }

        mSmartPixels = (Preference) prefScreen.findPreference(SMART_PIXELS);
        boolean mSmartPixelsSupported = getResources().getBoolean(
                com.android.internal.R.bool.config_supportSmartPixels);
        if (!mSmartPixelsSupported && mSmartPixels != null) {
            prefScreen.removePreference(mSmartPixels);
        }

        Action threeFingersSwipeAction = Action.fromSettings(getContentResolver(),
                Settings.System.KEY_THREE_FINGERS_SWIPE_ACTION,
                Action.NOTHING);
        mThreeFingersSwipeAction = initList(KEY_THREE_FINGERS_SWIPE, threeFingersSwipeAction);

        mClearKeybox = findPreference(KEY_CLEAR_KEYBOX);
        mClearKeybox.setOnPreferenceClickListener(preference -> {
            clearKeybox();
            return true;
         });

         mImportKeybox = findPreference(KEY_IMPORT_KEYBOX);
         mImportKeybox.setOnPreferenceClickListener(preference -> {
            mImportKeyboxLauncher.launch("text/xml");
            return true;
        });
    }

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putIntForUser(getContentResolver(), setting, Integer.valueOf(value), UserHandle.USER_CURRENT);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mThreeFingersSwipeAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_THREE_FINGERS_SWIPE_ACTION);
            return true;
        }
        return false;
    }

    private void handleKeyboxImport(Uri uri) {
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            if (root == null || !"AndroidAttestation".equals(root.getNodeName())) {
                Log.e(TAG, "Invalid root element. Expected <AndroidAttestation>");
                showToast(R.string.import_failed);
                return;
            }

            NodeList keyboxes = doc.getElementsByTagName("Keybox");
            if (keyboxes.getLength() == 0) {
                Log.e(TAG, "No <Keybox> element found in XML.");
                showToast(R.string.import_failed);
                return;
            }

            JSONObject keyboxJson = new JSONObject();
            for (int i = 0; i < keyboxes.getLength(); i++) {
                Element keyboxElement = (Element) keyboxes.item(i);
                NodeList keys = keyboxElement.getElementsByTagName("Key");
                if (keys.getLength() == 0) {
                    Log.w(TAG, "No <Key> entries in <Keybox>. Skipping.");
                    continue;
                }

                for (int j = 0; j < keys.getLength(); j++) {
                    Element keyElement = (Element) keys.item(j);
                    String algorithm = keyElement.getAttribute("algorithm").toUpperCase();
                    if (TextUtils.isEmpty(algorithm)) {
                        Log.w(TAG, "Missing 'algorithm' attribute in <Key>. Skipping.");
                        continue;
                    }

                    if (algorithm.equals("ECDSA")) algorithm = "EC";
                    Element privKeyElem = (Element) keyElement.getElementsByTagName("PrivateKey").item(0);
                    if (privKeyElem == null) {
                        Log.w(TAG, "No <PrivateKey> found for algorithm " + algorithm + ". Skipping.");
                        continue;
                    }

                    String privKeyRaw = getRawText(privKeyElem);
                    String privKey = extractBase64FromPEM(privKeyRaw);
                    if (TextUtils.isEmpty(privKey)) {
                        Log.w(TAG, "Empty private key for " + algorithm + ". Skipping.");
                        continue;
                    }

                    keyboxJson.put(algorithm + ".PRIV", privKey);
                    NodeList certList = keyElement.getElementsByTagName("Certificate");
                    for (int k = 0; k < certList.getLength(); k++) {
                        Element certElem = (Element) certList.item(k);
                        String certRaw = getRawText(certElem);
                        String cert = extractBase64FromPEM(certRaw);
                        if (!TextUtils.isEmpty(cert)) {
                            keyboxJson.put(algorithm + ".CERT_" + (k + 1), cert);
                        } else {
                           Log.w(TAG, "Empty certificate #" + (k + 1) + " for " + algorithm);

                        }
                    }
                }
            }

            if (keyboxJson.length() == 0) {
                Log.e(TAG, "Parsed keybox is empty. Import failed.");
                showToast(R.string.import_failed);
                return;
            }

            Settings.System.putString(requireContext().getContentResolver(),
                    "custom_keybox_data", keyboxJson.toString());
            showToast(R.string.import_success);
            SystemRestartUtils.showSystemRestartDialog(getContext());
        } catch (Exception e) {
            Log.e(TAG, "Keybox import failed", e);
            showToast(R.string.import_failed);
        }
    }

    private String getRawText(Element element) {
        StringBuilder builder = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                builder.append(node.getNodeValue());
            }
        }
        return builder.toString().trim();
    }

    private String extractBase64FromPEM(String pem) {
        return pem.replaceAll("-----BEGIN [^-]+-----", "")
                  .replaceAll("-----END [^-]+-----", "")
                  .replaceAll("[\\r\\n\\s]+", "");
    }

    private void showToast(int resId) {
        getActivity().runOnUiThread(() -> 
            Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show()
        );
    }

    private void clearKeybox() {
        try {
            Settings.System.putString(requireContext().getContentResolver(), "custom_keybox_data", null);
            showToast(R.string.clear_success);
            SystemRestartUtils.showSystemRestartDialog(getContext());
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear keybox", e);
            showToast(R.string.clear_failed);
        }
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.POCKET_JUDGE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.AUTO_BRIGHTNESS_ONE_SHOT, 0, UserHandle.USER_CURRENT);
        SystemProperties.set(SYS_GAMES_SPOOF, "false");
        SystemProperties.set(SYS_PHOTOS_SPOOF, "true");
        SystemProperties.set(SYS_NETFLIX_SPOOF, "false");
        SensorBlock.reset(mContext);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALPHA;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.alpha_settings_misc) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();

                    boolean mPocketJudgeSupported = res.getBoolean(
                            com.android.internal.R.bool.config_pocketModeSupported);
                    if (!mPocketJudgeSupported)
                        keys.add(POCKET_JUDGE);


                    boolean mSmartPixelsSupported = context.getResources().getBoolean(
                            com.android.internal.R.bool.config_supportSmartPixels);
                    if (!mSmartPixelsSupported)
                        keys.add(SMART_PIXELS);

                    return keys;
                }
            };
}
