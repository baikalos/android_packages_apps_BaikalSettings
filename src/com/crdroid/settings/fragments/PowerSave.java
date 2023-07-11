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

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.os.RemoteException;
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

    private static final String POWERSAVER_SCREENON = "powesaver_screenon";
    private static final String POWERSAVER_STANDBY = "powesaver_standby";
    private static final String POWERSAVER_IDLE = "powesaver_idle";

    private static final String APP_FREEZER = "cached_apps_freezer";

    private ListPreference mAppPerfProfile;
    private ListPreference mAppThermProfile;
    private SwitchPreference mAppFreezer;

    private ListPreference mPowersaverScreenon;
    private ListPreference mPowersaverStandby;
    private ListPreference mPowersaverIdle;

    private Context mContext;

    private boolean isKernelIncompatible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getContentResolver();

        String[] perfProfiles = getResources().getStringArray(R.array.performance_listvalues);
        String[] thermProfiles = getResources().getStringArray(R.array.thermal_listvalues);

        boolean perfProf  = (perfProfiles !=null && perfProfiles.length > 1);
        boolean thermProf  = (thermProfiles !=null && thermProfiles.length > 1);
        boolean appFreezer = false;

        try {
             appFreezer = ActivityManager.getService().isAppFreezerSupported();
            
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to obtain freezer support status from ActivityManager");
        }

        if( !BaikalConstants.isKernelCompatible() ) {
            Log.e(TAG, "profiles : incompatible kernel");
            thermProf = false;
            perfProf = false;
            isKernelIncompatible = true;
        }

	    addPreferencesFromResource(R.xml.crdroid_settings_powersave);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        final PreferenceScreen screen = getPreferenceScreen();

        try {
            mAppFreezer = (SwitchPreference) findPreference(APP_FREEZER);
            if( mAppFreezer != null && (!appFreezer || isKernelIncompatible) ) mAppFreezer.setVisible(false);

            PreferenceScreen prefEditor = (PreferenceScreen) findPreference("baikalos_profile_editor");
            if( prefEditor != null && isKernelIncompatible ) prefEditor.setVisible(false);

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

            mPowersaverScreenon = (ListPreference) findPreference(POWERSAVER_SCREENON);
            if( mPowersaverScreenon != null ) { 
                int profile = Settings.Global.getInt(resolver,Settings.Global.BAIKALOS_POWER_LEVEL_ON, 0);
                if( profile == -1 ) profile = 7;
                Log.e(TAG, "mPowersaverScreenon: getProfile=" + profile);
                try { mPowersaverScreenon.setValue(Integer.toString(profile)); } catch (Exception rre) { }
                mPowersaverScreenon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Settings.Global.putInt(resolver,Settings.Global.BAIKALOS_POWER_LEVEL_ON, Integer.parseInt(newValue.toString()));
                            Log.e(TAG, "mPowersaverScreenon: setProfile=" + newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mPowersaverScreenon Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mPowersaverStandby = (ListPreference) findPreference(POWERSAVER_STANDBY);
            if( mPowersaverStandby != null ) { 
                int profile = Settings.Global.getInt(resolver,Settings.Global.BAIKALOS_POWER_LEVEL_STANDBY, 0);
                if( profile == -1 ) profile = 7;
                Log.e(TAG, "mPowersaverStandby: getProfile=" + profile);
                try { mPowersaverStandby.setValue(Integer.toString(profile)); } catch (Exception rre) { }
                mPowersaverStandby.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Settings.Global.putInt(resolver,Settings.Global.BAIKALOS_POWER_LEVEL_STANDBY, Integer.parseInt(newValue.toString()));
                            Log.e(TAG, "mPowersaverStandby: setProfile=" + newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mPowersaverStandby Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mPowersaverIdle = (ListPreference) findPreference(POWERSAVER_IDLE);
            if( mPowersaverIdle != null ) { 
                int profile = Settings.Global.getInt(resolver,Settings.Global.BAIKALOS_POWER_LEVEL_IDLE, 0);
                if( profile == -1 ) profile = 7;
                Log.e(TAG, "mPowersaverIdle: getProfile=" + profile);
                try { mPowersaverIdle.setValue(Integer.toString(profile)); } catch (Exception rre) { }
                mPowersaverIdle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            Settings.Global.putInt(resolver,Settings.Global.BAIKALOS_POWER_LEVEL_IDLE, Integer.parseInt(newValue.toString()));
                            Log.e(TAG, "mPowersaverIdle: setProfile=" + newValue.toString());
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mPowersaverIdle Fatal! exception", re );
                        }
                        return true;
                    }
                });
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
