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

import android.bluetooth.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import androidx.preference.CheckBoxPreference;
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

import com.crdroid.settings.preferences.BluetoothDevicePreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.crdroid.Utils;

//import ru.baikalos.extras.BaseSettingsFragment;
//import ru.baikalos.extras.PerfProfileDetailsActivity;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import java.util.List;
import java.util.ArrayList;


public class SmartTrust extends SettingsPreferenceFragment {

    private static final String TAG = "BaikalExtras";

    private static final String BLUETOOTH_DEVICES = "trust_bluetooth_devices";

    private Context mContext;

    private PreferenceCategory mBluetoothDevices;
    private Set<String> mCheckedValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    addPreferencesFromResource(R.xml.crdroid_settings_smart_trust);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        final PreferenceScreen screen = getPreferenceScreen();

        mCheckedValues = new HashSet<String>();

        mBluetoothDevices = (PreferenceCategory) findPreference(BLUETOOTH_DEVICES);

        loadBluetoothTrustSettings();
        loadBluetoothDevices();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static void reset(Context context) {
        
    }

    private void loadBluetoothDevices() {

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();

        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> connectedLEDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);

        int i = 0;
        for (BluetoothDevice dev : pairedDevices) {
            String name = dev.getName() != null ? dev.getName() : "unknown";
            String address = dev.getAddress();
            addBluetoothDevicePreference(mBluetoothDevices,name,address);
        }

        for (BluetoothDevice dev : connectedLEDevices) {
            String name = dev.getName() != null ? dev.getName() : "unknown";
            String address = dev.getAddress();
            addBluetoothDevicePreference(mBluetoothDevices,name,address);
        }
    }

    private void addBluetoothDevicePreference(PreferenceCategory parent, String name, String address) {

        CheckBoxPreference mPreference = new CheckBoxPreference(mContext);
        mPreference.setTitle(name);
        mPreference.setSummary(address);
        mPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if( (boolean)newValue ) {
                    if( !mCheckedValues.contains(preference.getSummary()) ) {
                        mCheckedValues.add(String.valueOf(preference.getSummary()));
                    }
                } else {
                    if( mCheckedValues.contains(preference.getSummary()) ) {
                        mCheckedValues.remove(preference.getSummary());
                    }
                }
                saveBluetoothTrustSettings();
                return true;
            }
        });
        parent.addPreference(mPreference);
    }

    private void loadBluetoothTrustSettings() {
        String btDevices = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.BAIKALOS_TRUST_BT_DEV);

        if (btDevices != null && btDevices.length() != 0) {
            String[] parts = btDevices.split("\\|");
            for (int j = 0; j < parts.length; j++) {
                mCheckedValues.add(parts[j]);
            }
        }

    }

    private void saveBluetoothTrustSettings() {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> nextItem = mCheckedValues.iterator();

        while (nextItem.hasNext()) {
            String sTag = nextItem.next();
            buffer.append(sTag + "|");
        }

        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }

        Log.e(TAG, "checked string: " + buffer.toString());
        Settings.Secure.putStringForUser(mContext.getContentResolver(), Settings.Secure.BAIKALOS_TRUST_BT_DEV,
            buffer.toString(), UserHandle.USER_CURRENT);
    }
    
    

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.crdroid_settings_smart_trust) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };

}
