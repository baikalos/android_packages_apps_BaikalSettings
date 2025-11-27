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
package ru.baikalos.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.EditTextPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import ru.baikalos.settings.utils.Util;
import ru.baikalos.settings.utils.SuShell;

import java.util.List;

@SearchIndexable
public class Experimental extends SettingsPreferenceFragment /* implements Preference.OnPreferenceChangeListener */ {

    public static final String TAG = "Experimental";

    public static final String RUN_CMD_PREF = "run_cmd";
    public static final String RET_CMD_PREF = "ret_cmd";

    EditTextPreference mCmdEditTextPreference;

    Preference mCmdRetvalText;
    Preference mCmdRunIt;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.baikalos_settings_experimental);

        Context mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mCmdEditTextPreference = (EditTextPreference) findPreference(RUN_CMD_PREF);
        if( mCmdEditTextPreference != null ) {
            //mCmdEditTextPreference.setOnPreferenceChangeListener(this);
        }

        mCmdRetvalText = (Preference) findPreference(RET_CMD_PREF);
        mCmdRunIt = (Preference) findPreference("run_it");
    }


    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mCmdRunIt) {
            String cmd = mCmdEditTextPreference.getText();
            List<String> ret = SuShell.runWithShellCheck(cmd);
            if( ret == null ) mCmdRetvalText.setSummary("");
            else {
                String val = "";
                for(String line : ret) {
                    val += line + "\n";
                }
                mCmdRetvalText.setSummary(val);
            }
        }
        return true;
    }

    /*public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (RUN_CMD_PREF.equals(key)) {
            List<String> ret = SuShell.runWithShellCheck((String) newValue);
            if( ret == null ) mCmdRetvalText.setSummary("");
            else {
                String val = "";
                for(String line : ret) {
                    val += line + "\n";
                }
                mCmdRetvalText.setSummary(val);
            }
        }
        return true;
    }*/

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.baikalos_settings_experimental) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
