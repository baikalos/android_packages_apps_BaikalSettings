/*
 * Copyright (C) 2023 crDroid Android Project
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

package com.crdroid.settings.fragments.services;

import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.concurrent.Semaphore;

public class HmsSwitch extends SettingsPreferenceFragment {

    private static final String TAG = "HmsSwitch";
    private static final String HMS_ENABLED = "hms_enabled";
    private static final String HMS_ENABLED_FOOTER = "hms_enabled_footer";

    private boolean mConfirmed = false;
    private SwitchPreference mSwitchEnable;
    private boolean mEnabled;
    private boolean mChanged;
    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.hms_enabled_settings);

        mResolver = getContext().getContentResolver();

        mEnabled = Settings.Secure.getIntForUser(mResolver,
            Settings.Secure.HMS_ENABLED, 0, UserHandle.USER_CURRENT) != 0;

        mSwitchEnable = (SwitchPreference) findPreference(HMS_ENABLED);
        if( mSwitchEnable != null ) {
            mSwitchEnable.setChecked(mEnabled);
            Log.e(TAG, "mSwitchEnable: hms_enabled=" + mEnabled);
            mSwitchEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        if( mEnabled == (Boolean)newValue ) return false;
                        Settings.Secure.putIntForUser(mResolver,
                               Settings.Secure.HMS_ENABLED, (Boolean)newValue ? 1:0, UserHandle.USER_CURRENT);
                        mEnabled = (Boolean)newValue;
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mSwitchEnable Fatal! exception", re );
                    }
                    return true;
                }
            });
        }
    }


    public static void reset(Context mContext) {
        /*ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.GMS_ENABLED, 0, UserHandle.USER_CURRENT);*/
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
