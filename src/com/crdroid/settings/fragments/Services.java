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

@SearchIndexable
public class Services extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "Services";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_services);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();
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
