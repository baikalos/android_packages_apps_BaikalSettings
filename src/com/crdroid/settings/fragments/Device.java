/*
 * Copyright (C) 2016-2023 crDroid Android Project
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
package com.crdroid.settings.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import com.crdroid.settings.preferences.SystemPropertiesEditTextPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.baikalos.BaikalSpoofer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


@SearchIndexable
public class Device extends SettingsPreferenceFragment {

    public static final String TAG = "Device";

    private Preference mReset;
    private Preference mUpdate;
    private Preference mRestart;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_device);

        mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        boolean isCertificateSpooferAvailable = mContext.getResources().
                getBoolean(com.android.internal.R.bool.config_certificateSpfrAvailable);

        String bootid = SystemProperties.get("ro.boot.vbmeta.digest");
        if( bootid == null || "".equals(bootid) ) isCertificateSpooferAvailable = false;

        if (!isCertificateSpooferAvailable ) {
            Preference pref = (Preference) findPreference("baikalos_enable_certificate_spoof");
            if( pref != null ) {
                pref.setVisible(false);
            }
            pref = (Preference) findPreference("baikalos_enable_certificate_spoof_vending");
            if( pref != null ) {
                pref.setVisible(false);
            }
            pref = (Preference) findPreference("baikalos_enable_certificate_spoof_apps");
            if( pref != null ) {
                pref.setVisible(false);
            }
        }

        boolean isSignatureSpooferAvailable = mContext.getResources().
                getBoolean(com.android.internal.R.bool.config_signatureSpfrAvailable);

        //if (!isSignatureSpooferAvailable) {
        //    Preference pref = (Preference) findPreference("baikalos_disable_signature_spoof");
        //    if( pref != null ) pref.setVisible(false);
        //}

        mReset = (Preference) findPreference("spoof_setings_reset");
        mUpdate = (Preference) findPreference("spoof_setings_update");
        mRestart = (Preference) findPreference("spoof_restart_gms");
        fill();
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Log.e(TAG, "onPreferenceTreeClick: preference=" + preference);
        if (preference == mReset) {
            settingsReset();
            return true;
        } else if( preference == mUpdate )  {
            settingsUpdate();
            return true;
        } else if( preference == mRestart ) {
            restartGoogleServices();
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void restartGoogleServices() {
        ActivityManager mAm = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        mAm.killBackgroundProcesses("com.google.android.gms");
    }

    private void settingsUpdate() {
        Log.e(TAG, "settingsUpdate");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        PiItem item = updateFromGoogle();
        if( item == null ) {
            Log.e(TAG, "settingsUpdate: can't update");
            return;
        }
        updateFrom(item);
    }

    private void settingsReset() {
        Log.e(TAG, "settingsReset");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.spoof_setings_reset_title);
        builder.setMessage(R.string.spoof_setings_reset_summary);
        builder.setPositiveButton(R.string.app_setings_reset_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                resetSettings();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void resetSettings() {
        SystemProperties.set("persist.spf.manufacturer","");
        SystemProperties.set("persist.spf.model","");
        SystemProperties.set("persist.spf.fingerprint", "");
        SystemProperties.set("persist.spf.brand", "");
        SystemProperties.set("persist.spf.product", "");
        SystemProperties.set("persist.spf.device", "");
        SystemProperties.set("persist.spf.id", "");
        SystemProperties.set("persist.spf.release", "");
        SystemProperties.set("persist.spf.incremental", "");
        SystemProperties.set("persist.spf.security_patch", "");
        SystemProperties.set("persist.spf.firs_api_level", "");
        SystemProperties.set("persist.spf.sdk_int", "");
        fill();
    }

    private void fill() {
            fill("persist.spf.manufacturer", BaikalSpoofer.MANUFACTURER);
            fill("persist.spf.model", BaikalSpoofer.MODEL);
            fill("persist.spf.fingerprint", BaikalSpoofer.FINGERPRINT);
            fill("persist.spf.brand", BaikalSpoofer.BRAND);
            fill("persist.spf.product", BaikalSpoofer.PRODUCT);
            fill("persist.spf.device", BaikalSpoofer.DEVICE);
            fill("persist.spf.id", BaikalSpoofer.ID);
            fill("persist.spf.release", BaikalSpoofer.RELEASE);
            fill("persist.spf.incremental", BaikalSpoofer.INCREMENTAL);
            fill("persist.spf.security_patch", BaikalSpoofer.SECURITY_PATCH);
            fill("persist.spf.firs_api_level", BaikalSpoofer.FIRST_API_LEVEL);
            fill("persist.spf.sdk_int", BaikalSpoofer.SDK_INT);
    }

    private void fill(String key, String def) {
        SystemPropertiesEditTextPreference preference = (SystemPropertiesEditTextPreference) findPreference(key);
        if( preference != null ) preference.setText(BaikalSpoofer.SystemPropertiesGetNotNullOrEmpty(key,def));
    }

    private void updateFrom(PiItem item) {

        SystemProperties.set("persist.spf.manufacturer",item.MANUFACTURER);
        SystemProperties.set("persist.spf.model",item.MODEL);
        SystemProperties.set("persist.spf.fingerprint", item.FINGERPRINT);
        SystemProperties.set("persist.spf.brand", item.BRAND);
        SystemProperties.set("persist.spf.product", item.PRODUCT);
        SystemProperties.set("persist.spf.device", item.DEVICE);
        SystemProperties.set("persist.spf.id", item.ID);
        SystemProperties.set("persist.spf.release", item.RELEASE);
        SystemProperties.set("persist.spf.incremental", item.INCREMENTAL);
        SystemProperties.set("persist.spf.security_patch", item.SECURITY_PATCH);
        SystemProperties.set("persist.spf.firs_api_level", item.DEVICE_INITIAL_SDK_INT);
        SystemProperties.set("persist.spf.sdk_int", item.SDK_INT);
        Settings.Global.putString(mContext.getContentResolver(), "baikal_kb_data_override", item.ATT_CERT);

            fill("persist.spf.manufacturer", item.MANUFACTURER);
            fill("persist.spf.model", item.MODEL);
            fill("persist.spf.fingerprint", item.FINGERPRINT);
            fill("persist.spf.brand", item.BRAND);
            fill("persist.spf.product", item.PRODUCT);
            fill("persist.spf.device", item.DEVICE);
            fill("persist.spf.id", item.ID);
            fill("persist.spf.release", item.RELEASE);
            fill("persist.spf.incremental", item.INCREMENTAL);
            fill("persist.spf.security_patch", item.SECURITY_PATCH);
            fill("persist.spf.firs_api_level", item.DEVICE_INITIAL_SDK_INT);
            fill("persist.spf.sdk_int", item.SDK_INT);

    }

    public PiItem updateFromGoogle() {
        PiItem item = null;
        try {

            URL url = new URI(BaikalSpoofer.getDevString(BaikalSpoofer.DEV_CONST.SPOOFER_JSON_URL)).toURL();
            //URL url = new URL("https://raw.githubusercontent.com/baikalos/android_vendor_certification/refs/heads/13.0/gms_certified_props.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            item = new PiItem();
            if( !item.update(content.toString()) ) return null;
            return item;
        } catch (Exception e) {
            Log.e(TAG, "updateFromGoogle:",e);
            return null;
        }
    }

    class PiItem {
        public String MANUFACTURER;
        public String MODEL;
        public String FINGERPRINT;
        public String PRODUCT;
        public String DEVICE;
        public String BRAND;
        public String ID;
        public String INCREMENTAL;
        public String RELEASE;
        public String SECURITY_PATCH;
        public String DEVICE_INITIAL_SDK_INT;
        public String SDK_INT;
        public String ATT_CERT;

        public PiItem() {
            SDK_INT = "33";
        }

        public PiItem(String json) {
            update(json);
        }

        public boolean update(String json) {
            try {
                JSONObject parsedProps = new JSONObject(json);
                Iterator<String> keys = parsedProps.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = parsedProps.getString(key);
                    Log.e(TAG, "update:" + key + ":" + value);
                    switch(key) {
                        case "MANUFACTURER":
                            MANUFACTURER = value;
                            break;
                        case "MODEL":
                            MODEL = value;
                            break;
                        case "FINGERPRINT":
                            FINGERPRINT = value;
                            break;
                        case "PRODUCT":
                            PRODUCT = value;
                            break;
                        case "DEVICE":
                            DEVICE = value;
                            break;
                        case "BRAND":
                            BRAND = value;
                            break;
                        case "ID":
                            ID = value;
                            break;
                        case "VERSION.INCREMENTAL":
                        case "INCREMENTAL":
                            INCREMENTAL = value;
                            break;
                        case "VERSION.RELEASE":
                        case "RELEASE":
                            RELEASE = value;
                            break;
                        case "VERSION.SECURITY_PATCH":
                        case "SECURITY_PATCH":
                            SECURITY_PATCH = value;
                            break;
                        case "VERSION.DEVICE_INITIAL_SDK_INT":
                        case "DEVICE_INITIAL_SDK_INT":
                            DEVICE_INITIAL_SDK_INT = value;
                            break;
                        case "SDK_INT":
                            SDK_INT = value;
                            break;
                        case "ATT_CERT":
                            ATT_CERT = value;
                            break;
                    }
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, "update:",e);
                return false;
            }
        }
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.crdroid_settings_device) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
