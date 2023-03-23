/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

package com.crdroid.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.view.View;
import android.util.Log;

import android.content.res.Resources;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.crdroid.Utils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.baikalos.BaikalConstants;

import java.util.List;
import java.util.ArrayList;


public class PowerSave extends SettingsPreferenceFragment {

    private static final String TAG = "BaikalExtras";

    private static final String APP_PROFILE_PERF = "default_profile_performance";
    private static final String APP_PROFILE_THERM = "default_profile_thermal";

    private ListPreference mAppPerfProfile;
    private ListPreference mAppThermProfile;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getContentResolver();

        String[] perfProfiles = getResources().getStringArray(R.array.performance_listvalues);
        String[] thermProfiles = getResources().getStringArray(R.array.thermal_listvalues);

        boolean perfProf  = (perfProfiles !=null && perfProfiles.length > 1);
        boolean thermProf  = (thermProfiles !=null && thermProfiles.length > 1);

        if( !BaikalConstants.isKernelCompatible() ) {
            Log.e(TAG, "profiles : incompatible kernel");
            thermProf = false;
            perfProf = false;
        }

	    addPreferencesFromResource(R.xml.crdroid_settings_powersave);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        final PreferenceScreen screen = getPreferenceScreen();

        try {
            mAppPerfProfile = (ListPreference) findPreference(APP_PROFILE_PERF);
            if( mAppPerfProfile != null ) { 
                if(!perfProf) {
                    mAppPerfProfile.setVisible(false);
                } else {
                    int profile = Settings.Global.getInt(resolver,Settings.Global.BAIKALOS_DEFAULT_PERFORMANCE, -1);
                    if( profile == -1 ) profile = 7;
                    Log.e(TAG, "getPerfProfile: getProfile=" + profile);
                    try { mAppPerfProfile.setValue(Integer.toString(profile)); } catch (Exception rre) { }
                    mAppPerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                Settings.Global.putInt(resolver,Settings.Global.BAIKALOS_DEFAULT_PERFORMANCE, Integer.parseInt(newValue.toString()));
                                Log.e(TAG, "getPerfProfile: setProfile=" + newValue.toString());
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppPerfProfile Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            mAppThermProfile = (ListPreference) findPreference(APP_PROFILE_THERM);
            if( mAppThermProfile != null ) {
                if(!thermProf) {
                    mAppThermProfile.setVisible(false);
                } else {
                    int profile = Settings.Global.getInt(resolver,Settings.Global.BAIKALOS_DEFAULT_THERMAL, -1);
                    if( profile == -1 ) profile = 1;
                    Log.e(TAG, "getThermProfile: getProfile=" + profile);
                    try { mAppThermProfile.setValue(Integer.toString(profile)); } catch (Exception rre) { }
                    mAppThermProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                Settings.Global.putInt(resolver,Settings.Global.BAIKALOS_DEFAULT_THERMAL, Integer.parseInt(newValue.toString()));
                                Log.e(TAG, "getThermProfile: setProfile=" + newValue.toString());
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppThermProfile Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }
        } catch(Exception e) {
            Log.e(TAG, "onCreate: PowerSave Fatal! exception", e );
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setSystemPropertyString(String key, String value) {
        Log.e(TAG, "setSystemPropertyBoolean: key=" + key + ", value=" + value);
        SystemProperties.set(key, value);
    }

    private String getSystemPropertyString(String key, String def) {
        return SystemProperties.get(key,def);
    }

    private boolean getSystemPropertyBoolean(String key) {
        if( SystemProperties.get(key,"0").equals("1") || SystemProperties.get(key,"0").equals("true") ) return true;
	    return false;
    }

    private void setSystemPropertyBoolean(String key, boolean value) {
        String text = value?"1":"0";
        Log.e(TAG, "setSystemPropertyBoolean: key=" + key + ", value=" + value);
        SystemProperties.set(key, text);
    }

    public static void reset(Context mContext) {
        
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.crdroid_settings_powersave) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };

}
