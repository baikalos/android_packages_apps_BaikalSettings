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

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

//import com.crdroid.settings.fragments.misc.GmsSwitch;
//import com.crdroid.settings.fragments.misc.SensorBlock;
//import com.crdroid.settings.fragments.misc.SmartCharging;

import java.util.List;

import lineageos.providers.LineageSettings;
import org.lineageos.internal.util.PackageManagerUtils;


@SearchIndexable
public class Services extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "Services";

    private static final String GMS_PREF = "gms_enabled_settings";
    private static final String HMS_PREF = "hms_enabled_settings";
    private static final String DOLBY_PREF = "dolby_enabled_settings";
    private static final String JDSP_PREF = "jdsp_enabled_settings";
    private static final String AFX_PREF = "afx_enabled_settings";
    private static final String FU_PREF = "fi_enabled_settings";


    private static final String GMS_PACKAGE = "com.google.android.gms";
    private static final String MGMS_PACKAGE = "com.mgoogle.android.gms";
    private static final String HMS_PACKAGE = "com.huawei.hwid";
    private static final String FU_PACKAGE = "com.crdroid.faceunlock";

    private static final String DOLBY_PACKAGE = "com.dolby.daxservice";
    private static final String JDSP_PACKAGE = "james.dsp";
    private static final String AFX_PACKAGE = "org.lineageos.audiofx";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_services);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        Preference gmsPreference = (Preference) findPreference(GMS_PREF);

        if (gmsPreference != null &&
            !PackageManagerUtils.isAppInstalled(getContext(), GMS_PACKAGE, 
                PackageManager.MATCH_DISABLED_COMPONENTS | 
                PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS) )  {
           gmsPreference.setVisible(false); 
        }

        Preference hmsPreference = (Preference) findPreference(HMS_PREF);
        if (hmsPreference != null &&
            !PackageManagerUtils.isAppInstalled(getContext(), HMS_PACKAGE, 
                PackageManager.MATCH_DISABLED_COMPONENTS | 
                PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS) )  {
           hmsPreference.setVisible(false); 
        }

        Preference fuPreference = (Preference) findPreference(FU_PREF);
        if (fuPreference != null &&
            !PackageManagerUtils.isAppInstalled(getContext(), FU_PACKAGE, 
                PackageManager.MATCH_DISABLED_COMPONENTS | 
                PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS) )  {
           fuPreference.setVisible(false); 
        }

        Preference dolbyPreference = (Preference) findPreference(DOLBY_PREF);
        if (dolbyPreference != null &&
            !PackageManagerUtils.isAppInstalled(getContext(), DOLBY_PACKAGE, 
                PackageManager.MATCH_DISABLED_COMPONENTS | 
                PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS) )  {
           dolbyPreference.setVisible(false); 
        }

        Preference jdspPreference = (Preference) findPreference(JDSP_PREF);
        if (jdspPreference != null &&
            !PackageManagerUtils.isAppInstalled(getContext(), JDSP_PACKAGE, 
                PackageManager.MATCH_DISABLED_COMPONENTS | 
                PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS) )  {
           jdspPreference.setVisible(false); 
        }

        Preference afxPreference = (Preference) findPreference(AFX_PREF);
        if (afxPreference != null &&
            !PackageManagerUtils.isAppInstalled(getContext(), AFX_PACKAGE, 
                PackageManager.MATCH_DISABLED_COMPONENTS | 
                PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS) )  {
           afxPreference.setVisible(false); 
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
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
            new BaseSearchIndexProvider(R.xml.crdroid_settings_services) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
