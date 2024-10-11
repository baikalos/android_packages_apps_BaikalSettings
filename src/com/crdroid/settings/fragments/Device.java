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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
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

import java.util.List;

@SearchIndexable
public class Device extends SettingsPreferenceFragment {

    public static final String TAG = "Device";

    private Preference mReset;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_device);

        mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        boolean isCertificateSpooferAvailable = mContext.getResources().
                getBoolean(com.android.internal.R.bool.config_certificateSpooferAvailable);

        if (!isCertificateSpooferAvailable) {
            ((Preference) findPreference("baikalos_disable_certificate_spoof")).setVisible(false);
        }

        mReset = (Preference) findPreference("spoof_setings_reset");
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
        } 
        return super.onPreferenceTreeClick(preference);
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
        SystemProperties.set("persist.spoof.manufacturer","");
        SystemProperties.set("persist.spoof.model","");
        SystemProperties.set("persist.spoof.fingerprint", "");
        SystemProperties.set("persist.spoof.brand", "");
        SystemProperties.set("persist.spoof.product", "");
        SystemProperties.set("persist.spoof.device", "");
        SystemProperties.set("persist.spoof.id", "");
        SystemProperties.set("persist.spoof.release", "");
        SystemProperties.set("persist.spoof.incremental", "");
        SystemProperties.set("persist.spoof.security_patch", "");
        SystemProperties.set("persist.spoof.firs_api_level", "");
        fill();
    }

    private void fill() {
            fill("persist.spoof.manufacturer", BaikalSpoofer.MANUFACTURER);
            fill("persist.spoof.model", BaikalSpoofer.MODEL);
            fill("persist.spoof.fingerprint", BaikalSpoofer.FINGERPRINT);
            fill("persist.spoof.brand", BaikalSpoofer.BRAND);
            fill("persist.spoof.product", BaikalSpoofer.PRODUCT);
            fill("persist.spoof.device", BaikalSpoofer.DEVICE);
            fill("persist.spoof.id", BaikalSpoofer.ID);
            fill("persist.spoof.release", BaikalSpoofer.RELEASE);
            fill("persist.spoof.incremental", BaikalSpoofer.INCREMENTAL);
            fill("persist.spoof.security_patch", BaikalSpoofer.SECURITY_PATCH);
            fill("persist.spoof.firs_api_level", String.valueOf(BaikalSpoofer.FIRST_API_LEVEL));
    }

    private void fill(String key, String def) {
        SystemPropertiesEditTextPreference preference = (SystemPropertiesEditTextPreference) findPreference(key);
        if( preference != null ) preference.setText(BaikalSpoofer.SystemPropertiesGetNotNullOrEmpty(key,def));
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
