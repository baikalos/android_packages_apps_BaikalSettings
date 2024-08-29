/*
 * Copyright (C) 2016-2020 crDroid Android Project
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;


import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.settings.R;

import com.crdroid.settings.preferences.SystemPropertiesListPreference;

import com.android.internal.baikalos.BaikalConstants;


import java.util.List;
import java.util.ArrayList;

@SearchIndexable
public class PerformanceProfileEditor extends SettingsPreferenceFragment {

    public static final String TAG = "PerformanceProfileEditor";

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( !BaikalConstants.isKernelCompatible() ) {
            return;
        }

        addPreferencesFromResource(R.xml.baikal_profile_edit);

        mResolver = getActivity().getContentResolver();

        createProfileEditorCategory("boost","Interaction Boost");
        createProfileEditorCategory("render","Rendering Boost");
        createProfileEditorCategory("inter","Balanced");
        createProfileEditorCategory("sustain","Sustained Performance");
        createProfileEditorCategory("fixed","Fixed Performance");
        createProfileEditorCategory("low","Low Power");
        createProfileEditorCategory("idle","Device Idle");

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    private PreferenceCategory createProfileEditorCategory(String profile, String name) {

        PreferenceCategory category = new PreferenceCategory(getActivity());
        category.setTitle(name);

        final PreferenceScreen screen = getPreferenceScreen();
        screen.addPreference(category);

        addProfileEditorListPreference(category,"CPULittleClusterMaxFreq",profile,
                R.array.baikalos_little_cluster_entries,R.array.baikalos_little_cluster_values,R.string.baikalos_little_freq_max_title);

        addProfileEditorListPreference(category,"CPUBigClusterMaxFreq",profile,
                R.array.baikalos_big_cluster_entries,R.array.baikalos_big_cluster_values,R.string.baikalos_big_freq_max_title);

        addProfileEditorListPreference(category,"CPUBigPlusClusterMaxFreq",profile,
                R.array.baikalos_bigplus_cluster_entries,R.array.baikalos_bigplus_cluster_values,R.string.baikalos_bigplus_freq_max_title);

        addProfileEditorListPreference(category,"CPULittleClusterMinFreq",profile,
                R.array.baikalos_little_cluster_entries,R.array.baikalos_little_cluster_values,R.string.baikalos_little_freq_min_title);

        addProfileEditorListPreference(category,"CPUBigClusterMinFreq",profile,
                R.array.baikalos_big_cluster_entries,R.array.baikalos_big_cluster_values,R.string.baikalos_big_freq_min_title);

        addProfileEditorListPreference(category,"CPUBigPlusClusterMinFreq",profile,
                R.array.baikalos_bigplus_cluster_entries,R.array.baikalos_bigplus_cluster_values,R.string.baikalos_bigplus_freq_min_title);

        addProfileEditorListPreference(category,"GPUMaxFreq",profile,
                R.array.baikalos_gpu_entries,R.array.baikalos_gpu_values,R.string.baikalos_gpu_max_title);

        addProfileEditorListPreference(category,"GPUMinFreq",profile,
                R.array.baikalos_gpu_entries,R.array.baikalos_gpu_values,R.string.baikalos_gpu_min_title);

        addProfileEditorListPreference(category,"TASchedtuneBoost",profile,
                R.array.baikalos_boost_entries,R.array.baikalos_boost_values,R.string.baikalos_task_boost_title);

        addProfileEditorListPreference(category,"TASilverBoost",profile,
                R.array.baikalos_boost_entries,R.array.baikalos_boost_values,R.string.baikalos_silver_boost_title);

        addProfileEditorListPreference(category,"TAGoldBoost",profile,
                R.array.baikalos_boost_entries,R.array.baikalos_boost_values,R.string.baikalos_gold_boost_title);

        addProfileEditorListPreference(category,"TAPlusBoost",profile,
                R.array.baikalos_boost_entries,R.array.baikalos_boost_values,R.string.baikalos_plus_boost_title);

        addProfileEditorListPreference(category,"CPULittleClusterCoreCtl",profile,
                R.array.baikalos_core_ctl_entries,R.array.baikalos_core_ctl_values,R.string.baikalos_little_core_ctl_title);

        addProfileEditorListPreference(category,"CPUBigClusterCoreCtl",profile,
                R.array.baikalos_core_ctl_entries,R.array.baikalos_core_ctl_values,R.string.baikalos_big_core_ctl_title);

        addProfileEditorListPreference(category,"CPUBigPlusClusterCoreCtl",profile,
                R.array.baikalos_core_ctl_entries,R.array.baikalos_core_ctl_values,R.string.baikalos_plus_core_ctl_title);

        return category;
    }

    private void addProfileEditorListPreference(PreferenceCategory category, String key, String profile, int entries, int values, int title) {
        String property_key = "persist.baikal." + profile + "." + key;
        if( !isPropertySupported(property_key) ) return;

        SystemPropertiesListPreference pref = new SystemPropertiesListPreference(getActivity());
        Resources res = getResources();
        pref.setKey(property_key);
        pref.setTitle(res.getString(title));
        pref.setEntries(res.getStringArray(entries));
        pref.setEntryValues(res.getStringArray(values));
        pref.setDefaultValue("-99999");
        category.addPreference(pref);
                    
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    Settings.Global.putInt(mResolver,Settings.Global.BAIKALOS_PROFILE_MANAGER_REFRESH, 1);
                    Log.e(TAG, "getPerfProfile: setForceReload");
                } catch(Exception re) {
                    Log.e(TAG, "onCreate: addProfileEditorListPreference " + key + ". Fatal! exception", re );
                }
                return true;
            }
        });
    }

    private boolean isPropertySupported(String key) {
        if( "DEADBEEF".equals(SystemProperties.get(key,"DEADBEEF")) ) return false;
        return true;        
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.baikal_profile_edit);
}
