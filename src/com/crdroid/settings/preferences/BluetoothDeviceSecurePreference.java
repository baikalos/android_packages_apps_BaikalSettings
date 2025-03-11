package com.crdroid.settings.preferences;

import android.bluetooth.*;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import android.os.UserHandle;
import android.util.Log;


import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;

import android.os.Parcel;
import android.os.Parcelable;


public class BluetoothDeviceSecurePreference extends BluetoothDevicePreferenceBase {

    private static final String TAG = "Baikal.BluetoothDeviceSecurePreference";

    public BluetoothDeviceSecurePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BluetoothDeviceSecurePreference(Context context) {
        this(context, null);
    }

    @Override
    public String readPropertiesFromStore(String key) {
        return Settings.Secure.getString(getContext().getContentResolver(),key);
    }

    @Override
    public void writePropertiesToStore(String key, String val) {
        Settings.Secure.putStringForUser(getContext().getContentResolver(), key,
            val, UserHandle.USER_CURRENT);
    }
}
