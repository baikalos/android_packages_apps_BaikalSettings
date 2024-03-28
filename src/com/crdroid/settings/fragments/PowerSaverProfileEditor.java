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
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;



import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.settings.R;

import com.crdroid.settings.preferences.SystemPropertiesListPreference;

import com.android.internal.baikalos.BaikalConstants;
import com.android.internal.baikalos.PowerSaverSettings;
import com.android.internal.baikalos.PowerSaverPolicyConfig;


import java.util.List;
import java.util.ArrayList;

@SearchIndexable
public class PowerSaverProfileEditor extends SettingsPreferenceFragment {

    public static final String TAG = "PowerSaverProfileEditor";

    private int mType;
    private String mName;

    PowerSaverPolicyConfig mPolicy;
    PowerSaverSettings mPowerSaverSettings;

    Context mContext;
    Resources mResources;


    private PowerSaverProfileEditor() {
    }

    public static Fragment newInstance(int type, String name) {
        PowerSaverProfileEditor fragment = new PowerSaverProfileEditor();
        fragment.mType = type;
        fragment.mName = name;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( !BaikalConstants.isKernelCompatible() ) {
            return;
        }

        Log.e(TAG, "type=" + mType);
        Log.e(TAG, "name=" + mName);

        addPreferencesFromResource(R.xml.ps_profile);

        mContext = (Context) getActivity();
        mResources = getActivity().getResources();

        mPowerSaverSettings = new PowerSaverSettings(new Handler(),mContext);
        mPowerSaverSettings.registerObserver(false);
        mPowerSaverSettings.loadPolicies();

        mPolicy = mPowerSaverSettings.getPoliciesById().get(mType);
        if( mPolicy == null ) {
            mPolicy = new PowerSaverPolicyConfig(mName,mType); 
        }

        SwitchPreference switchPreference = null;
        ListPreference listPreference = null;

        try {

            switchPreference = (SwitchPreference) findPreference("ps_profile_AdvertiseIsEnabled");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.advertiseIsEnabled);
                Log.e(TAG, "ps_profile_AdvertiseIsEnabled: mName=" + mName + ", value=" + mPolicy.advertiseIsEnabled);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.advertiseIsEnabled = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_AdvertiseIsEnabled: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_AdvertiseIsEnabled Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            listPreference = (ListPreference) findPreference("ps_profile_AdjustBrightnessFactor");
            if( listPreference != null ) {
                int value = mPolicy.adjustBrightnessFactor;
                Log.e(TAG, "ps_profile_AdjustBrightnessFactor: mName=" + mName + ", value=" + value);
                listPreference.setValue(Integer.toString(value));
                listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mPolicy.adjustBrightnessFactor = val;
                        mPowerSaverSettings.updatePolicy(mPolicy);
                        mPowerSaverSettings.save();
                        Log.e(TAG, "ps_profile_AdjustBrightnessFactor: mName=" + mName + ", value=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: ps_profile_AdjustBrightnessFactor Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableFullBackup");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableFullBackup);
                Log.e(TAG, "ps_profile_EnableFullBackup: mName=" + mName + ", value=" + mPolicy.enableFullBackup);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableFullBackup = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableFullBackup: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableFullBackup Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableKeyValueBackup");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableKeyValueBackup);
                Log.e(TAG, "ps_profile_EnableKeyValueBackup: mName=" + mName + ", value=" + mPolicy.enableKeyValueBackup);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableKeyValueBackup = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableKeyValueBackup: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableKeyValueBackup Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableAnimation");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableAnimation);
                Log.e(TAG, "ps_profile_EnableAnimation: mName=" + mName + ", value=" + mPolicy.enableAnimation);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableAnimation = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableAnimation: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableAnimation Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableAod");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableAod);
                Log.e(TAG, "ps_profile_EnableAod: mName=" + mName + ", value=" + mPolicy.enableAod);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableAod = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableAod: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableAod Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableLaunchBoost");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableLaunchBoost);
                Log.e(TAG, "ps_profile_EnableLaunchBoost: mName=" + mName + ", value=" + mPolicy.enableLaunchBoost);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableLaunchBoost = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableLaunchBoost: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableLaunchBoost Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableOptionalSensors");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableOptionalSensors);
                Log.e(TAG, "ps_profile_EnableOptionalSensors: mName=" + mName + ", value=" + mPolicy.enableOptionalSensors);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableOptionalSensors = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableOptionalSensors: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableOptionalSensors Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableVibration");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableVibration);
                Log.e(TAG, "ps_profile_EnableVibration: mName=" + mName + ", value=" + mPolicy.enableVibration);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableVibration = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableVibration: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableVibration Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableAdjustBrightness");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableAdjustBrightness);
                Log.e(TAG, "ps_profile_EnableAdjustBrightness: mName=" + mName + ", value=" + mPolicy.enableAdjustBrightness);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableAdjustBrightness = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableAdjustBrightness: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableAdjustBrightness Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableDataSaver");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableDataSaver);
                Log.e(TAG, "ps_profile_EnableDataSaver: mName=" + mName + ", value=" + mPolicy.enableDataSaver);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableDataSaver = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableDataSaver: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableDataSaver Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableFirewall");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableFirewall);
                Log.e(TAG, "ps_profile_EnableFirewall: mName=" + mName + ", value=" + mPolicy.enableFirewall);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableFirewall = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableFirewall: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableFirewall Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableNightMode");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableNightMode);
                Log.e(TAG, "ps_profile_EnableNightMode: mName=" + mName + ", value=" + mPolicy.enableNightMode);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableNightMode = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableNightMode: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableNightMode Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_EnableQuickDoze");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.enableQuickDoze);
                Log.e(TAG, "ps_profile_EnableQuickDoze: mName=" + mName + ", value=" + mPolicy.enableQuickDoze);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.enableQuickDoze = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_EnableQuickDoze: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_EnableQuickDoze Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_ForceAllAppsStandby");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.forceAllAppsStandby);
                Log.e(TAG, "ps_profile_ForceAllAppsStandby: mName=" + mName + ", value=" + mPolicy.forceAllAppsStandby);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.forceAllAppsStandby = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_ForceAllAppsStandby: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_ForceAllAppsStandby Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_ForceBackgroundCheck");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.forceBackgroundCheck);
                Log.e(TAG, "ps_profile_ForceBackgroundCheck: mName=" + mName + ", value=" + mPolicy.forceBackgroundCheck);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.forceBackgroundCheck = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_ForceBackgroundCheck: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_ForceBackgroundCheck Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            switchPreference = (SwitchPreference) findPreference("ps_profile_KillInBackground");
            if( switchPreference != null ) {
                switchPreference.setChecked(mPolicy.killInBackground);
                Log.e(TAG, "ps_profile_KillInBackground: mName=" + mName + ", value=" + mPolicy.killInBackground);
                switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mPolicy.killInBackground = ((Boolean)newValue);
                            mPowerSaverSettings.updatePolicy(mPolicy);
                            mPowerSaverSettings.save();
                            Log.e(TAG, "ps_profile_KillInBackground: mName=" + mName + ", value=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: ps_profile_KillInBackground Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            listPreference = (ListPreference) findPreference("ps_profile_SoundTriggerMode");
            if( listPreference != null ) {
                int value = mPolicy.soundTriggerMode;
                Log.e(TAG, "ps_profile_SoundTriggerMode: mName=" + mName + ", value=" + value);
                listPreference.setValue(Integer.toString(value));
                listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mPolicy.soundTriggerMode = val;
                        mPowerSaverSettings.updatePolicy(mPolicy);
                        mPowerSaverSettings.save();
                        Log.e(TAG, "ps_profile_SoundTriggerMode: mName=" + mName + ", value=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: ps_profile_SoundTriggerMode Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            listPreference = (ListPreference) findPreference("ps_profile_LocationMode");
            if( listPreference != null ) {
                int value = mPolicy.locationMode;
                Log.e(TAG, "ps_profile_LocationMode: mName=" + mName + ", value=" + value);
                listPreference.setValue(Integer.toString(value));
                listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mPolicy.locationMode = val;
                        mPowerSaverSettings.updatePolicy(mPolicy);
                        mPowerSaverSettings.save();
                        Log.e(TAG, "ps_profile_LocationMode: mName=" + mName + ", value=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: ps_profile_LocationMode Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            listPreference = (ListPreference) findPreference("ps_profile_CacheAppTimeout");
            if( listPreference != null ) {
                int value = mPolicy.killBgRestrictedCachedIdleSettleTime;
                Log.e(TAG, "ps_profile_CacheAppTimeout: mName=" + mName + ", value=" + value);
                listPreference.setValue(Integer.toString(value));
                listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mPolicy.killBgRestrictedCachedIdleSettleTime = val;
                        mPowerSaverSettings.updatePolicy(mPolicy);
                        mPowerSaverSettings.save();
                        Log.e(TAG, "ps_profile_CacheAppTimeout: mName=" + mName + ", value=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: ps_profile_CacheAppTimeout Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }


       } catch(Exception re) {
           Log.e(TAG, "onCreate: Fatal! exception", re );
       }
       
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() - commit");
        if( mPowerSaverSettings != null ) {
            //if( mProfile != null ) mAppSettings.updateSystemSettings(mProfile);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    mPowerSaverSettings.commit();
                }
            };
            thread.start();
        }
        Log.d(TAG, "onSaveInstanceState() - exit");
    }


    @Override
    protected void onUnbindPreferences() {
        super.onUnbindPreferences();
        Log.d(TAG, "onUnbindPreferences() - commit");
        if( mPowerSaverSettings != null ) {
            //if( mProfile != null ) mAppSettings.updateSystemSettings(mProfile);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    mPowerSaverSettings.commit();
                }
            };
            thread.start();
        }
        Log.d(TAG, "onUnbindPreferences() - exit");
    }


    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.baikal_profile_edit);
}
