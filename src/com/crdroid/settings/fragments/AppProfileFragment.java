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
import android.os.Handler;
import android.os.LocaleList;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Process;
import android.os.SystemProperties;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;

import android.os.ServiceManager;
import android.os.RemoteException;

import android.content.res.Resources;

import android.baikalos.AppProfile;
import com.android.internal.baikalos.AppProfileBackend;

import com.android.internal.baikalos.PowerWhitelistBackend;
import com.android.internal.baikalos.BaikalSpoofer;
import com.android.internal.baikalos.BaikalConstants;

import com.android.internal.util.crdroid.Utils;

//import com.crdroid.settings.R;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;


import java.io.File;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class AppProfileFragment extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "BaikalPreferences";

    public static final String ARG_PACKAGE_NAME = "package";
    public static final String ARG_PACKAGE_UID = "uid";

    private static final String APP_PROFILE_OLD_LINKS = "app_profile_old_links";
    private static final String APP_PROFILE_DEBUG = "app_profile_debug";
    private static final String APP_PROFILE_DISABLE_BOOT = "app_profile_disable_boot";
    private static final String APP_PROFILE_DISABLE_WAKEUP = "app_profile_disable_wakeup";
    private static final String APP_PROFILE_DISABLE_JOBS = "app_profile_disable_jobs";
    private static final String APP_PROFILE_ALLOW_IDLE_NETWORK = "app_profile_idle_network";
    private static final String APP_PROFILE_HIDE_IDLE = "app_profile_hide_idle";
    private static final String APP_PROFILE_PERF = "app_profile_performance";
    private static final String APP_PROFILE_THERM = "app_profile_thermal";
    private static final String APP_PROFILE_BRIGHTNESS = "app_profile_brightness";
    private static final String APP_PROFILE_ROTATION = "app_profile_rotation";
    private static final String APP_PROFILE_MAX_FPS = "app_profile_max_fps";
    private static final String APP_PROFILE_MIN_FPS = "app_profile_min_fps";
    private static final String APP_PROFILE_READER = "app_profile_reader";
    private static final String APP_PROFILE_KEEP_ON = "app_profile_keep_on";
    private static final String APP_PROFILE_FULL_SCREEN = "app_profile_full_screen";
    private static final String APP_PROFILE_OVERRIDE_FONTS = "app_profile_override_fonts";
//    private static final String APP_PROFILE_CAMERA_HAL1 = "app_profile_camera_hal1";
    private static final String APP_PROFILE_PINNED = "app_profile_pinned";
    private static final String APP_PROFILE_DONOTCLOSE = "app_profile_donotclose";
    private static final String APP_PROFILE_STAMINA = "app_profile_stamina";
    private static final String APP_PROFILE_ALLOWWHILEIDLE = "app_profile_allowwhileidle";
    private static final String APP_PROFILE_REQUIRE_GMS = "app_profile_require_gms";
//    private static final String APP_PROFILE_RESTRICTED = "app_profile_restricted";
    private static final String APP_PROFILE_BACKGROUND = "app_profile_background";
    private static final String APP_PROFILE_FREEZER = "app_profile_freezer";
    private static final String APP_PROFILE_HEAVY_MEMORY = "app_profile_heavy_memory";
    private static final String APP_PROFILE_HEAVY_CPU = "app_profile_heavy_cpu";
    private static final String APP_PROFILE_BLOCK_OVERLAYS = "app_profile_block_overlays";
    private static final String APP_PROFILE_HIDE_HMS = "app_profile_hide_hms";
    private static final String APP_PROFILE_HIDE_GMS = "app_profile_hide_gms";
    private static final String APP_PROFILE_HIDE_3P = "app_profile_hide_3p";
    private static final String APP_PROFILE_PRIV_PHONE_STATE = "app_profile_priv_phone_state";

//    private static final String APP_PROFILE_DISABLE_TWL = "app_profile_disable_twl";
    private static final String VOLUME_SCALE = "app_profile_volume_scale";

    private static final String APP_PROFILE_PERFORMANCE_SCALE = "app_profile_cpu_performance_limit";

    private static final String APP_PROFILE_BLOCK_FOCUS_RECV = "app_profile_block_focus_recv";
    private static final String APP_PROFILE_BLOCK_FOCUS_SEND = "app_profile_block_focus_send";
    private static final String APP_PROFILE_FORCE_SONIFICATION = "app_profile_force_sonification";
    private static final String APP_PROFILE_INSTALLER = "app_profile_installer";
    private static final String APP_PROFILE_SCALEFACTOR = "app_profile_scalefactor";

    private static final String APP_PROFILE_BYPASS_CHARGING = "app_profile_bypass_charging";

    private static final String APP_PROFILE_SPOOF = "app_profile_spoof";
    private static final String APP_PROFILE_FILE_ACCESS = "file_access";
    private static final String APP_PROFILE_PHKA = "app_profile_phka";
    private static final String APP_PROFILE_DEVMODE = "app_profile_devmode";

    private static final String APP_PROFILE_LOCATION = "app_profile_location";
    private static final String APP_PROFILE_CAMERA = "app_profile_camera";
    private static final String APP_PROFILE_MICROPHONE = "app_profile_microphone";

    private static final String APP_PROFILE_LOWRES = "app_profile_lowres";

    private static final String APP_PROFILE_LANGUAGE = "app_profile_language";

    private static final String APP_PROFILE_FORCED_SCREENSHOT = "app_profile_forced_screenshot";

    private static final String APP_PROFILE_BLOCK_CONTACTS = "app_profile_block_contacts";
    private static final String APP_PROFILE_BLOCK_CALLLOG = "app_profile_block_calllog";
    private static final String APP_PROFILE_BLOCK_CALENDAR = "app_profile_block_calendar";
    private static final String APP_PROFILE_BLOCK_MEDIA = "app_profile_block_media";


    private String mPackageName;
    private int mUid;
    private Context mContext;

    private SwitchPreference mAppOldLinks;
    private SwitchPreference mAppDebug;
    private SwitchPreference mAppPinned;
    private SwitchPreference mAppDoNotClose;
    private SwitchPreference mAppFreezer;
    private SwitchPreference mAppHeavyMemory;
    private SwitchPreference mAppHeavyCPU;
    private SwitchPreference mAppStamina;
    private SwitchPreference mAppAllowWhileIdle;
    private SwitchPreference mAppRequireGms;
    private SwitchPreference mAppDisableBoot;
    private SwitchPreference mAppDisableWakeup;
    private SwitchPreference mAppDisableJobs;
    private SwitchPreference mAppDisableFreezer;
    private SwitchPreference mBlockFocusRecv;
    private SwitchPreference mBlockFocusSend;
    private SwitchPreference mBypassCharging;
    private SwitchPreference mAppKeepOn;
    private SwitchPreference mAppFullScreen;
    private SwitchPreference mAppOverrideFonts;
    private SwitchPreference mLowRes;
    private SwitchPreference mAppPhkaProfile;
    private SwitchPreference mAppDevModeProfile;
    private SwitchPreference mAppAllowIdleNetwork;
    private SwitchPreference mAppHideIdle;
    private SwitchPreference mAppForcedScreenshot;
    private SwitchPreference mAppBlockOverlaysProfile;
    private SwitchPreference mAppHideHMS;
    private SwitchPreference mAppHideGMS;
    private SwitchPreference mAppHide3P;

    private SwitchPreference mAppBlockContacts;
    private SwitchPreference mAppBlockCalllog;
    private SwitchPreference mAppBlockCalendar;
    private SwitchPreference mAppBlockMedia;

    private SwitchPreference mAppPrivPhoneState;

    private ListPreference mAppReader;
    private ListPreference mForceSonification;
    private ListPreference mInstaller;
    private ListPreference mScaleFactor;
    private ListPreference mAppPerfProfile;
    private ListPreference mAppThermProfile;
    private ListPreference mAppBrightnessProfile;
    private ListPreference mAppRotationProfile;
    private ListPreference mAppMaxFpsProfile;
    private ListPreference mAppMinFpsProfile;
    private ListPreference mAppBackgroundProfile;
    private ListPreference mAppSpoofProfile;
    private ListPreference mAppFileAccess;
    private ListPreference mAppLocation;
    private ListPreference mAppCamera;
    private ListPreference mAppMicrophone;
    private ListPreference mPerformanceScale;
    private ListPreference mAppLanguageProfile;

    private AppProfileBackend mAppSettings;
    private AppProfile mProfile;


    private boolean isKernelIncompatible = false;

    public AppProfileFragment() {
        //mPackageName = packageName; 
        //mUid = uid;
    }

    /*public AppProfileFragment(String packageName, int uid) {
        mPackageName = packageName; 
        mUid = uid;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        mPackageName = args.getString(ARG_PACKAGE_NAME);
        mUid = args.getInt(ARG_PACKAGE_UID);

        addPreferencesFromResource(R.xml.app_profile);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        String[] perfProfiles = getResources().getStringArray(R.array.performance_listvalues);
        String[] thermProfiles = getResources().getStringArray(R.array.thermal_listvalues);
        String[] refreshRates = getResources().getStringArray(R.array.max_fps_listvalues);
        String[] cpuLimits = getResources().getStringArray(R.array.cpu_resources_listentries);

        boolean isSuperSaverAvailable = getResources().
                getBoolean(com.android.internal.R.bool.config_superSaverAvailable);


        boolean perfProf  = (perfProfiles !=null && perfProfiles.length > 1);
        boolean thermProf  = (thermProfiles !=null && thermProfiles.length > 1);
        boolean variableFps = (refreshRates !=null && refreshRates.length > 1);

        if( !BaikalConstants.isKernelCompatible() ) {
            Log.e(TAG, "profiles : incompatible kernel");
            //thermProf = false;
            //perfProf = false;
            isKernelIncompatible = true;
        }

        Log.e(TAG, "perf profiles : perfProfiles=" + perfProfiles);
        Log.e(TAG, "perf profiles : perfProfiles.length=" + perfProfiles.length);

        boolean appFreezer = false;

        try {
             appFreezer = ActivityManager.getService().isAppFreezerSupported();
            
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to obtain freezer support status from ActivityManager");
        }

        mAppSettings = AppProfileBackend.getInstance(new Handler(),mContext);
        //mAppSettings.registerObserver(false);

        mAppSettings.loadProfiles();
        mProfile = mAppSettings.getProfile(mPackageName);
        if( mProfile == null ) { 
            mProfile = new AppProfile(mPackageName,-1);
        }

        //mProfile = mAppSettings.updateProfileFromSystemSettings(mProfile);


    
        try {

            PowerWhitelistBackend mBackend = PowerWhitelistBackend.getInstance(getContext());
            mBackend.refreshList();

            mAppOldLinks = (SwitchPreference) findPreference(APP_PROFILE_OLD_LINKS);
            if( mAppOldLinks != null ) {
                mAppOldLinks.setChecked(mProfile.mOldLinks);
                Log.e(TAG, "mAppOldLinks: mPackageName=" + mPackageName + ", mOldLinks=" + mProfile.mOldLinks);
                mAppOldLinks.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mOldLinks = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppOldLinks: mPackageName=" + mPackageName + ", mOldLinks=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mOldLinks Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }
        
            mAppDebug = (SwitchPreference) findPreference(APP_PROFILE_DEBUG);
            if( mAppDebug != null ) {
                mAppDebug.setChecked(mProfile.mDebug);
                Log.e(TAG, "mAppDebug: mPackageName=" + mPackageName + ", mDebug=" + mProfile.mDebug);
                mAppDebug.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mDebug = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppDebug: mPackageName=" + mPackageName + ",mDebug=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppDebug Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppDisableBoot = (SwitchPreference) findPreference(APP_PROFILE_DISABLE_BOOT);
            if( mAppDisableBoot != null ) {
                mAppDisableBoot.setChecked(mProfile.mBootDisabled);
                Log.e(TAG, "mAppDisableBoot: mPackageName=" + mPackageName + ",mBootDisabled=" + mProfile.mBootDisabled);
                mAppDisableBoot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mBootDisabled = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppDisableBoot: mPackageName=" + mPackageName + ",disableBoot=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppDisableBoot Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppDisableWakeup = (SwitchPreference) findPreference(APP_PROFILE_DISABLE_WAKEUP);
            if( mAppDisableWakeup != null ) {
                mAppDisableWakeup.setChecked(mProfile.mDisableWakeup);
                Log.e(TAG, "mAppDisableWakeup: mPackageName=" + mPackageName + ",mDisableWakeup=" + mProfile.mDisableWakeup);
                mAppDisableWakeup.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mDisableWakeup = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppDisableWakeup: mPackageName=" + mPackageName + ", mDisableWakeup=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppDisableWakeup Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppDisableJobs = (SwitchPreference) findPreference(APP_PROFILE_DISABLE_JOBS);
            if( mAppDisableJobs != null ) {
                mAppDisableJobs.setChecked(mProfile.mDisableJobs);
                Log.e(TAG, "mAppDisableJobs: mPackageName=" + mPackageName + ",mDisableJobs=" + mProfile.mDisableJobs);
                mAppDisableJobs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mDisableJobs = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppDisableJobs: mPackageName=" + mPackageName + ", mDisableJobs=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppDisableJobs Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppDisableFreezer = (SwitchPreference) findPreference(APP_PROFILE_FREEZER);

            if( mAppDisableFreezer != null ) {
                if( !appFreezer ) {
                    mAppDisableFreezer.setVisible(false);
                } else if( isKernelIncompatible ) {
                    mAppDisableFreezer.setEnabled(false);
                } else {
                    mAppDisableFreezer.setChecked(mProfile.mDisableFreezer);
                    Log.e(TAG, "mAppDisableFreezer: mPackageName=" + mPackageName + ",mDisableFreezer=" + mProfile.mDisableFreezer);
                    mAppDisableFreezer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mDisableFreezer = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppDisableFreezer: mPackageName=" + mPackageName + ", mDisableFreezer=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppDisableFreezer Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            mAppHeavyMemory = (SwitchPreference) findPreference(APP_PROFILE_HEAVY_MEMORY);

            if( isKernelIncompatible ) {
                mAppHeavyMemory.setVisible(false);
                mAppHeavyMemory = null;
            }

            if( mAppHeavyMemory != null ) {
                if( isKernelIncompatible ) {
                    mAppHeavyMemory.setEnabled(false);
                } else {
                    mAppHeavyMemory.setChecked(mProfile.mHeavyMemory);
                    Log.e(TAG, "mAppHeavyMemory: mPackageName=" + mPackageName + ",mAppHeavyMemory=" + mProfile.mHeavyMemory);
                    mAppHeavyMemory.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mHeavyMemory = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppHeavyMemory: mPackageName=" + mPackageName + ", mAppHeavyMemory=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppHeavyMemory Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            mAppHeavyCPU = (SwitchPreference) findPreference(APP_PROFILE_HEAVY_CPU);

            if( isKernelIncompatible ) {
                mAppHeavyCPU.setVisible(false);
                mAppHeavyCPU = null;
            }

            if( mAppHeavyCPU != null ) {
                if( isKernelIncompatible ) {
                    mAppHeavyCPU.setEnabled(false);
                } else {
                    mAppHeavyCPU.setChecked(mProfile.mHeavyCPU);
                    Log.e(TAG, "mAppHeavyMemory: mPackageName=" + mPackageName + ",mAppHeavyMemory=" + mProfile.mHeavyCPU);
                    mAppHeavyCPU.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mHeavyCPU = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppHeavyCPU: mPackageName=" + mPackageName + ", mAppHeavyCPU=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppHeavyCPU Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            mAppAllowIdleNetwork = (SwitchPreference) findPreference(APP_PROFILE_ALLOW_IDLE_NETWORK);
            if( mAppAllowIdleNetwork != null ) {
                mAppAllowIdleNetwork.setChecked(mProfile.mAllowIdleNetwork);
                Log.e(TAG, "mAppAllowIdleNetwork: mPackageName=" + mPackageName + ",mAppAllowIdleNetwork=" + mProfile.mAllowIdleNetwork);
                mAppAllowIdleNetwork.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mAllowIdleNetwork = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppAllowIdleNetwork: mPackageName=" + mPackageName + ", mAppAllowIdleNetwork=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppAllowIdleNetwork Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppHideIdle = (SwitchPreference) findPreference(APP_PROFILE_HIDE_IDLE);
            if( mAppHideIdle != null ) {
                mAppHideIdle.setChecked(mProfile.mHideIdle);
                Log.e(TAG, "mAppHideIdle: mPackageName=" + mPackageName + ", mAppHideIdle=" + mProfile.mHideIdle);
                mAppHideIdle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mHideIdle = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppHideIdle: mPackageName=" + mPackageName + ", mAppHideIdle=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppHideIdle Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppPerfProfile = (ListPreference) findPreference(APP_PROFILE_PERF);
            if( mAppPerfProfile != null ) { 
                if(!perfProf) {
                    mAppPerfProfile.setVisible(false);
                } else if( isKernelIncompatible ) {
                    mAppPerfProfile.setEnabled(false);
                } else {
                    int profile = mProfile.mPerfProfile;
                    Log.e(TAG, "getAppPerfProfile: mPackageName=" + mPackageName + ", getProfile=" + profile);
                    mAppPerfProfile.setValue(Integer.toString(profile));
                    mAppPerfProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mPerfProfile = Integer.parseInt(newValue.toString());
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppPerfProfile: mPackageName=" + mPackageName + ",setProfile=" + newValue.toString());
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
                } else if( isKernelIncompatible ) {
                    mAppThermProfile.setEnabled(false);
                } else {
                    int profile = mProfile.mThermalProfile;
                    Log.e(TAG, "getAppThermProfile: mPackageName=" + mPackageName + ", getProfile=" + profile);
                    mAppThermProfile.setValue(Integer.toString(profile));
                    mAppThermProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mThermalProfile = Integer.parseInt(newValue.toString());
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppThermProfile: mPackageName=" + mPackageName + ",setProfile=" + newValue.toString());
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppThermProfile Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

        
            mAppBrightnessProfile = (ListPreference) findPreference(APP_PROFILE_BRIGHTNESS);
            if( mAppBrightnessProfile != null ) {
                int brightness = mProfile.mBrightness;
                Log.e(TAG, "getAppBrightness: mPackageName=" + mPackageName + ", brightness=" + brightness);
                mAppBrightnessProfile.setValue(Integer.toString(brightness));
                mAppBrightnessProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mBrightness = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "setAppBrightness: mPackageName=" + mPackageName + ", brightness=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppBrightness Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppRotationProfile = (ListPreference) findPreference(APP_PROFILE_ROTATION);
            if( mAppRotationProfile != null ) {
                int rotation = mProfile.mRotation;
                Log.e(TAG, "getAppRotation: mPackageName=" + mPackageName + ", rotation=" + rotation);
                mAppRotationProfile.setValue(Integer.toString(rotation));
                mAppRotationProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mRotation = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "setAppRotation: mPackageName=" + mPackageName + ", rotation=" + val);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: setAppRotation Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppMaxFpsProfile = (ListPreference) findPreference(APP_PROFILE_MAX_FPS);
            if( mAppMaxFpsProfile != null ) {
                if(!mContext.getResources().getBoolean(R.bool.config_show_peak_refresh_rate_switch)) {
                    mAppMaxFpsProfile.setVisible(false);
                } else {

                    List<String> entries = new ArrayList<>();
                    List<String> values = new ArrayList<>();

                    entries.add("Default");
                    values.add("0");

                    Display.Mode mode = mContext.getDisplay().getMode();
                    Display.Mode[] modes = mContext.getDisplay().getSupportedModes();

                    for (Display.Mode m : modes) {
                        if (m.getPhysicalWidth() == mode.getPhysicalWidth() &&
                            m.getPhysicalHeight() == mode.getPhysicalHeight()) {
                            entries.add(String.format("%.02fHz", m.getRefreshRate())
                                    .replaceAll("[\\.,]00", ""));
                            values.add(String.format(Locale.US, "%d", (int)m.getRefreshRate()));
                        }
                    }

                    mAppMaxFpsProfile.setEntries(entries.toArray(new String[entries.size()]));
                    mAppMaxFpsProfile.setEntryValues(values.toArray(new String[values.size()]));

                    int fps = mProfile.mMaxFrameRate;
                    Log.e(TAG, "mAppMaxFpsProfile: mPackageName=" + mPackageName + ", max_fps=" + fps);
                    mAppMaxFpsProfile.setValue(Integer.toString(fps));
                    mAppMaxFpsProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mMaxFrameRate = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppMaxFpsProfile: set mPackageName=" + mPackageName + ", max_fps=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: setAppFps Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }

            mAppMinFpsProfile = (ListPreference) findPreference(APP_PROFILE_MIN_FPS);
            if( mAppMinFpsProfile != null ) {

                if (!mContext.getResources().getBoolean(R.bool.config_show_min_refresh_rate_switch)) {
                    mAppMinFpsProfile.setVisible(false);
                } else {
                
                    List<String> entries = new ArrayList<>();
                    List<String> values = new ArrayList<>();

                    entries.add("Default");
                    values.add("0");

                    Display.Mode mode = mContext.getDisplay().getMode();
                    Display.Mode[] modes = mContext.getDisplay().getSupportedModes();
                    for (Display.Mode m : modes) {
                        if (m.getPhysicalWidth() == mode.getPhysicalWidth() &&
                            m.getPhysicalHeight() == mode.getPhysicalHeight()) {
                            entries.add(String.format("%.02fHz", m.getRefreshRate())
                                    .replaceAll("[\\.,]00", ""));
                            values.add(String.format(Locale.US, "%d", (int)m.getRefreshRate()));
                        }
                    }

                    mAppMinFpsProfile.setEntries(entries.toArray(new String[entries.size()]));
                    mAppMinFpsProfile.setEntryValues(values.toArray(new String[values.size()]));

                    int fps = mProfile.mMinFrameRate;
                    Log.e(TAG, "mAppMinFpsProfile: mPackageName=" + mPackageName + ", min_fps=" + fps);
                    mAppMinFpsProfile.setValue(Integer.toString(fps));
                    mAppMinFpsProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mMinFrameRate = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppMinFpsProfile: set mPackageName=" + mPackageName + ", min_fps=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppMinFpsProfile Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }


            mAppKeepOn = (SwitchPreference) findPreference(APP_PROFILE_KEEP_ON);
            if( mAppKeepOn != null ) {
                mAppKeepOn.setChecked(mProfile.mKeepOn);
                Log.e(TAG, "mAppKeepOn: mPackageName=" + mPackageName + ",mKeepOn=" + mProfile.mKeepOn);
                mAppKeepOn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mKeepOn = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppKeepOn: mPackageName=" + mPackageName + ",mKeepOn=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppKeepOn Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppReader = (ListPreference) findPreference(APP_PROFILE_READER);

            if( mAppReader != null ) {

                if( !isSuperSaverAvailable ) {
                    mAppReader.setVisible(false);
                } else {
                    mAppReader.setValue(Integer.toString(mProfile.mReader));
                    Log.e(TAG, "mAppReader: mPackageName=" + mPackageName + ",mReader=" + mProfile.mReader);
                    mAppReader.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                int val = Integer.parseInt(newValue.toString());
                                mProfile.mReader = val;
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppReader: mPackageName=" + mPackageName + ",mReader=" + val);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppReader Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }


            mAppFullScreen = (SwitchPreference) findPreference(APP_PROFILE_FULL_SCREEN);
            if( mAppFullScreen != null ) {
                mAppFullScreen.setChecked(mProfile.mFullScreen);
                Log.e(TAG, "mAppFullScreen: mPackageName=" + mPackageName + ",mAppFullScreen=" + mProfile.mFullScreen);
                mAppFullScreen.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mFullScreen = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppFullScreen: mPackageName=" + mPackageName + ",mAppFullScreen=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppFullScreen Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }

            mAppOverrideFonts = (SwitchPreference) findPreference(APP_PROFILE_OVERRIDE_FONTS);
            if( mAppOverrideFonts != null ) {
                mAppOverrideFonts.setChecked(mProfile.mOverrideFonts);
                Log.e(TAG, "mAppOverrideFonts: mPackageName=" + mPackageName + ",mOverrideFonts=" + mProfile.mOverrideFonts);
                mAppOverrideFonts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mOverrideFonts = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppOverrideFonts: mPackageName=" + mPackageName + ",mOverrideFonts=" + (Boolean)newValue);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppOverrideFonts Fatal! exception", re );
                        }
                        return true;
                    }
                });
            }


            mAppPinned = (SwitchPreference) findPreference(APP_PROFILE_PINNED);

            if( mAppPinned != null ) {
                if( isKernelIncompatible ) {
                    mAppPinned.setEnabled(false);
                } else {
                    mAppPinned.setChecked(mProfile.mPinned);
                    Log.e(TAG, "mAppPinned: mPackageName=" + mPackageName + ",mPinned=" + mProfile.mPinned);
                    mAppPinned.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mPinned = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppPinned: mPackageName=" + mPackageName + ",mPinned=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppPinned Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            mAppDoNotClose = (SwitchPreference) findPreference(APP_PROFILE_DONOTCLOSE);

            if( mAppDoNotClose != null ) {
                if( isKernelIncompatible ) {
                    mAppDoNotClose.setEnabled(false);
                } else {
                    mAppDoNotClose.setChecked(mProfile.mDoNotClose);
                    Log.e(TAG, "mAppDoNotClose: mPackageName=" + mPackageName + ",mAppDoNotClose=" + mProfile.mDoNotClose);
                    mAppDoNotClose.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mDoNotClose = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppDoNotClose: mPackageName=" + mPackageName + ",mAppDoNotClose=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppDoNotClose Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }


            mAppStamina = (SwitchPreference) findPreference(APP_PROFILE_STAMINA);
            if( mAppStamina != null ) {
                if( isStaminaWl() && !mProfile.mStamina ) {
                        mAppStamina.setChecked(true);
                        mAppStamina.setEnabled(false);
                } else if( !mProfile.mStamina && (mBackend.isSysWhitelisted(mPackageName) || isStaminaImportant() || mProfile.mStaminaEnforced) ) {
                    mAppStamina.setChecked(true);
                    mAppStamina.setEnabled(false);
                } else {
                    mAppStamina.setChecked(mProfile.mStamina);
                    Log.e(TAG, "mAppStamina: mPackageName=" + mPackageName + ",mStamina=" + mProfile.mStamina);
                    mAppStamina.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mStamina = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppStamina: mPackageName=" + mPackageName + ", mStamina=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppStamina Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            mAppAllowWhileIdle = (SwitchPreference) findPreference(APP_PROFILE_ALLOWWHILEIDLE);
            if( mAppAllowWhileIdle != null ) {
                {
                    mAppAllowWhileIdle.setChecked(mProfile.mAllowWhileIdle);
                    Log.e(TAG, "mAppAllowWhileIdle: mPackageName=" + mPackageName + ",mAllowWhileIdle=" + mProfile.mAllowWhileIdle);
                    mAppAllowWhileIdle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                mProfile.mAllowWhileIdle = ((Boolean)newValue);
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mAppAllowWhileIdle: mPackageName=" + mPackageName + ", mAllowWhileIdle=" + (Boolean)newValue);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppAllowWhileIdle Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }
       
            mAppBackgroundProfile = (ListPreference) findPreference(APP_PROFILE_BACKGROUND);
            if( mAppBackgroundProfile != null ) {

                boolean isSystemWhitelisted = mBackend.isSysWhitelisted(mPackageName);
                if( isSystemWhitelisted ) {
                    Log.e(TAG, "getAppBackground: mPackageName=" + mPackageName + ", isSysWhitelisted=true");
                    mAppBackgroundProfile.setEnabled(false);
                    if( mProfile.mBackgroundModeConfig >= 0 ) {
                        mAppBackgroundProfile.setValue("-1");
                        mProfile.mBackgroundModeConfig = -1;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                    }
                } else {
                    int background = mProfile.mBackgroundModeConfig;
                    Log.e(TAG, "getAppBackground: mPackageName=" + mPackageName + ", background=" + background);
                    mAppBackgroundProfile.setValue(Integer.toString(background));
                    mAppBackgroundProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            if( isSystemWhitelisted && val >= 0 ) {
                                Log.e(TAG, "setAppBackground: ignore limited background mode for system whitelisted app mPackageName=" + mPackageName + ",background=" + val);
                                return false;
                            }
                            mProfile.mBackgroundModeConfig = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "setAppBackground: mPackageName=" + mPackageName + ",background=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: setAppBackground Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }    
            }

            mAppRequireGms = (SwitchPreference) findPreference(APP_PROFILE_REQUIRE_GMS);
            if( mAppRequireGms != null ) {
                boolean requireGms = mProfile.mRequireGms;
                Log.e(TAG, "mAppRequireGms: mPackageName=" + mPackageName + ",requireGms=" + requireGms);
                mAppRequireGms.setChecked(mProfile.mRequireGms);
                mAppRequireGms.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mRequireGms = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppRequireGms: mPackageName=" + mPackageName + ",requireGms=" + mProfile.mRequireGms);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppRequireGms Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }


            mAppForcedScreenshot = (SwitchPreference) findPreference(APP_PROFILE_FORCED_SCREENSHOT);
            if( mAppForcedScreenshot != null ) {
                boolean forcedScreenshot = mProfile.mForcedScreenshot;
                Log.e(TAG, "mAppForcedScreenshot: mPackageName=" + mPackageName + ", forcedScreenshot=" + forcedScreenshot);
                mAppForcedScreenshot.setChecked(mProfile.mForcedScreenshot);
                mAppForcedScreenshot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mForcedScreenshot = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppForcedScreenshot: mPackageName=" + mPackageName + ", forcedScreenshot=" + mProfile.mForcedScreenshot);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppForcedScreenshot Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mPerformanceScale = (ListPreference) findPreference(APP_PROFILE_PERFORMANCE_SCALE);
            if( mPerformanceScale != null ) {
                int num = cpuLimits != null ? cpuLimits.length : 0;
                if( num < 1 ) {
                    mPerformanceScale.setVisible(false);
                    if( mProfile.mPerformanceLevel != 0 ) {
                        mProfile.mPerformanceLevel = 0;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                    }
                } else {

                    int level = mProfile.mPerformanceLevel;
                    if( level < 0 ) level = 0;
                    if( level >= num ) level = num-1;
                    mPerformanceScale.setValue(Integer.toString(level));
                    Log.e(TAG, "mPerformanceScale: mPackageName=" + mPackageName + ", mPerformanceScale=" + level);
                    mPerformanceScale.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                int val = Integer.parseInt(newValue.toString());
                                mProfile.mPerformanceLevel = val;
                                mAppSettings.updateProfile(mProfile);
                                mAppSettings.save();
                                Log.e(TAG, "mPerformanceScale: mPackageName=" + mPackageName + ", mPerformanceScale=" + val);
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mPerformanceScale Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }


            mBlockFocusRecv = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_FOCUS_RECV);
            if( mBlockFocusRecv != null ) {
                boolean ignoreAudioFocus = mProfile.mBAFRecv;
                Log.e(TAG, "mBlockFocusRecv: mPackageName=" + mPackageName + ", mBAFRecv=" + ignoreAudioFocus);
                mBlockFocusRecv.setChecked(ignoreAudioFocus);
                mBlockFocusRecv.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mBAFRecv = (Boolean)newValue;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mBlockFocusRecv: mPackageName=" + mPackageName + ", mBAFRecv=" + mProfile.mBAFRecv);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mBlockFocusRecv Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mBlockFocusSend = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_FOCUS_SEND);
            if( mBlockFocusSend != null ) {
                boolean ignoreAudioFocus = mProfile.mBAFSend;
                Log.e(TAG, "mBlockFocusSend: mPackageName=" + mPackageName + ", mBAFSend=" + ignoreAudioFocus);
                mBlockFocusSend.setChecked(ignoreAudioFocus);
                mBlockFocusSend.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mBAFSend = (Boolean)newValue;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mBlockFocusSend: mPackageName=" + mPackageName + ", mBAFSend=" + mProfile.mBAFSend);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mBlockFocusSend Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mForceSonification = (ListPreference) findPreference(APP_PROFILE_FORCE_SONIFICATION);
            if( mForceSonification != null ) {
                int forceSonification = mProfile.mSonification;
                mForceSonification.setValue(Integer.toString(forceSonification));
                Log.e(TAG, "mForceSonification: mPackageName=" + mPackageName + ", mSonification=" + forceSonification);
                mForceSonification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mSonification = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mForceSonification: mPackageName=" + mPackageName + ", mSonification=" + mProfile.mSonification);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mForceSonification Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mInstaller = (ListPreference) findPreference(APP_PROFILE_INSTALLER);
            if( mInstaller != null ) {
                int installer = mProfile.mInstaller;
                mInstaller.setValue(Integer.toString(installer));
                Log.e(TAG, "mInstaller: mPackageName=" + mPackageName + ", mInstaller=" + installer);
                mInstaller.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mInstaller = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mInstaller: mPackageName=" + mPackageName + ", mInstaller=" + mProfile.mInstaller);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mInstaller Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mScaleFactor = (ListPreference) findPreference(APP_PROFILE_SCALEFACTOR);
            if( mScaleFactor != null ) {
                int scaleFactor = mProfile.mScaleFactor;
                mScaleFactor.setValue(Integer.toString(scaleFactor));
                Log.e(TAG, "mScaleFactor: mPackageName=" + mPackageName + ", mScaleFactor=" + scaleFactor);
                mScaleFactor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        mProfile.mScaleFactor = val;
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mScaleFactor: mPackageName=" + mPackageName + ", mScaleFactor=" + mProfile.mScaleFactor);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mScaleFactor Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mBypassCharging = (SwitchPreference) findPreference(APP_PROFILE_BYPASS_CHARGING);

            if( mBypassCharging != null ) {

                if( isKernelIncompatible ) {
                    mBypassCharging.setEnabled(false);
                } else {
                    boolean bypassCharging = mProfile.mBypassCharging;
                    Log.e(TAG, "mBypassCharging: mPackageName=" + mPackageName + ", mBypassCharging=" + bypassCharging);
                    mBypassCharging.setChecked(bypassCharging);
                    mBypassCharging.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mBypassCharging = (Boolean)newValue;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mBypassCharging: mPackageName=" + mPackageName + ", mBypassCharging=" + mProfile.mBypassCharging);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mBypassCharging Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }


            mAppLocation = (ListPreference) findPreference(APP_PROFILE_LOCATION);
            if( mAppLocation != null ) {
                int level = mProfile.mLocationLevel;
                Log.e(TAG, "mAppLocation: mPackageName=" + mPackageName + ", level=" + level);
                mAppLocation.setValue(Integer.toString(level));
                mAppLocation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mLocationLevel = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppLocation: mPackageName=" + mPackageName + ", level=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppLocation Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }

            mAppCamera = (ListPreference) findPreference(APP_PROFILE_CAMERA);
            if( mAppCamera != null ) {
                    int level = mProfile.mCamera;
                    Log.e(TAG, "mAppCamera: mPackageName=" + mPackageName + ",level=" + level);
                    mAppCamera.setValue(Integer.toString(level));
                    mAppCamera.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mCamera = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppCamera: mPackageName=" + mPackageName + ",level=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppCamera Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }

            mAppMicrophone = (ListPreference) findPreference(APP_PROFILE_MICROPHONE);
            if( mAppMicrophone != null ) {
                    int level = mProfile.mMicrophone;
                    Log.e(TAG, "mAppMicrophone: mPackageName=" + mPackageName + ",level=" + level);
                    mAppMicrophone.setValue(Integer.toString(level));
                    mAppMicrophone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mMicrophone = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppMicrophone: mPackageName=" + mPackageName + ",level=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppMicrophone Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }

            mAppFileAccess = (ListPreference) findPreference(APP_PROFILE_FILE_ACCESS);
            if( mAppFileAccess != null ) {
                    int access = mProfile.mFileAccess;
                    Log.e(TAG, "mAppFileAccess: mPackageName=" + mPackageName + ",mAppFileAccess=" + access);
                    mAppFileAccess.setValue(Integer.toString(access));
                    mAppFileAccess.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mFileAccess = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppFileAccess: mPackageName=" + mPackageName + ",mAppFileAccess=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppFileAccess Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }

            mAppSpoofProfile = (ListPreference) findPreference(APP_PROFILE_SPOOF);
            if( mAppSpoofProfile != null ) {
                    int spoof = mProfile.mSpoofDevice;
                    Log.e(TAG, "setAppSpoof: mPackageName=" + mPackageName + ",spoof=" + spoof);
                    mAppSpoofProfile.setValue(Integer.toString(spoof));
                    mAppSpoofProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            int val = Integer.parseInt(newValue.toString());
                            mProfile.mSpoofDevice = val;
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "setAppSpoof: mPackageName=" + mPackageName + ",spoof=" + val);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: setAppSpoof Fatal! exception", re );
                        }
                        return true;
                      }
                    });
            }

            mAppPhkaProfile = (SwitchPreference) findPreference(APP_PROFILE_PHKA);
            if( mAppPhkaProfile != null ) {
                boolean appPhkaProfile = mProfile.mPreventHwKeyAttestation;
                Log.e(TAG, "mAppPhkaProfile: mPackageName=" + mPackageName + ", appPhkaProfile=" + appPhkaProfile);
                mAppPhkaProfile.setChecked(mProfile.mPreventHwKeyAttestation);
                mAppPhkaProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mPreventHwKeyAttestation = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppPhkaProfile: mPackageName=" + mPackageName + ", appPhkaProfile=" + mProfile.mPreventHwKeyAttestation);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppPhkaProfile Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppDevModeProfile = (SwitchPreference) findPreference(APP_PROFILE_DEVMODE);
            if( mAppDevModeProfile != null ) {
                boolean appDevModeProfile = mProfile.mHideDevMode;
                Log.e(TAG, "mAppDevModeProfile: mPackageName=" + mPackageName + ", appDevModeProfile=" + appDevModeProfile);
                mAppDevModeProfile.setChecked(mProfile.mHideDevMode);
                mAppDevModeProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mHideDevMode = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppDevModeProfile: mPackageName=" + mPackageName + ", appDevModeProfile=" + mProfile.mHideDevMode);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppDevModeProfile Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppBlockOverlaysProfile = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_OVERLAYS);
            if( mAppBlockOverlaysProfile != null ) {
                boolean blockOverlays = mProfile.mBlockOverlays;
                Log.e(TAG, "mAppBlockOverlaysProfile: mPackageName=" + mPackageName + ", mBlockOverlays=" + blockOverlays);
                mAppBlockOverlaysProfile.setChecked(blockOverlays);
                mAppBlockOverlaysProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mBlockOverlays = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppBlockOverlaysProfile: mPackageName=" + mPackageName + ", mBlockOverlays=" + mProfile.mBlockOverlays);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppBlockOverlaysProfile Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }


            

            mAppHideHMS = (SwitchPreference) findPreference(APP_PROFILE_HIDE_HMS);
            if( mAppHideHMS != null ) {
                if( !Utils.isPackageInstalled(mContext, "com.huawei.hwid")) {
                    mAppHideHMS.setVisible(false);                    
                } else {
                    boolean hide = mProfile.mHideHMS;
                    Log.e(TAG, "mAppHideHMS: mPackageName=" + mPackageName + ", mAppHideHMS=" + hide);
                    mAppHideHMS.setChecked(hide);
                    mAppHideHMS.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mHideHMS = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppHideHMS: mPackageName=" + mPackageName + ", mAppHideHMS=" + mProfile.mHideHMS);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppHideHMS Fatal! exception", re );
                        }
                        return true;
                      }
                    });
                }
            }


            mAppHideGMS = (SwitchPreference) findPreference(APP_PROFILE_HIDE_GMS);
            if( mAppHideGMS != null ) {
                if( !Utils.isPackageInstalled(mContext, "com.google.android.gms")) {
                    mAppHideGMS.setVisible(false);                    
                } else {
                    boolean hide = mProfile.mHideGMS;
                    Log.e(TAG, "mAppHideGMS: mPackageName=" + mPackageName + ", mAppHideGMS=" + hide);
                    mAppHideGMS.setChecked(hide);
                    mAppHideGMS.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                      public boolean onPreferenceChange(Preference preference, Object newValue) {
                        try {
                            mProfile.mHideGMS = ((Boolean)newValue);
                            mAppSettings.updateProfile(mProfile);
                            mAppSettings.save();
                            Log.e(TAG, "mAppHideGMS: mPackageName=" + mPackageName + ", mAppHideGMS=" + mProfile.mHideGMS);
                        } catch(Exception re) {
                            Log.e(TAG, "onCreate: mAppHideGMS Fatal! exception", re );
                            }
                        return true;
                      }
                    });
                }
            }

            mAppHide3P = (SwitchPreference) findPreference(APP_PROFILE_HIDE_3P);
            if( mAppHide3P != null ) {
                boolean hide = mProfile.mHide3P;
                Log.e(TAG, "mAppHide3P: mPackageName=" + mPackageName + ", mAppHide3P=" + hide);
                mAppHide3P.setChecked(hide);
                mAppHide3P.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mHide3P = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppHide3P: mPackageName=" + mPackageName + ", mAppHide3P=" + mProfile.mHide3P);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppHide3P Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppPrivPhoneState = (SwitchPreference) findPreference(APP_PROFILE_PRIV_PHONE_STATE);
            if( mAppPrivPhoneState != null ) {
                boolean hide = mProfile.mPriviledgedPhoneState;
                Log.e(TAG, "mAppPrivPhoneState: mPackageName=" + mPackageName + ", mAppPrivPhoneState=" + hide);
                mAppPrivPhoneState.setChecked(hide);
                mAppPrivPhoneState.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mPriviledgedPhoneState = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppPrivPhoneState: mPackageName=" + mPackageName + ", mAppPrivPhoneState=" + mProfile.mPriviledgedPhoneState);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppPrivPhoneState Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppBlockContacts = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_CONTACTS);
            if( mAppBlockContacts != null ) {
                boolean block = mProfile.mBlockContacts;
                Log.e(TAG, "mAppBlockContacts: mPackageName=" + mPackageName + ", mAppBlockContacts=" + block);
                mAppBlockContacts.setChecked(block);
                mAppBlockContacts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mBlockContacts = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppBlockContacts: mPackageName=" + mPackageName + ", mAppBlockContacts=" + mProfile.mBlockContacts);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppBlockContacts Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppBlockCalllog = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_CALLLOG);
            if( mAppBlockCalllog != null ) {
                boolean block = mProfile.mBlockCalllog;
                Log.e(TAG, "mAppBlockCalllog: mPackageName=" + mPackageName + ", mAppBlockCalllog=" + block);
                mAppBlockCalllog.setChecked(block);
                mAppBlockCalllog.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mBlockCalllog = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppBlockCalllog: mPackageName=" + mPackageName + ", mAppBlockCalllog=" + mProfile.mBlockCalllog);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppBlockCalllog Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }


            mAppBlockCalendar = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_CALENDAR);
            if( mAppBlockCalendar != null ) {
                boolean block = mProfile.mBlockCalendar;
                Log.e(TAG, "mAppBlockCalendar: mPackageName=" + mPackageName + ", mAppBlockCalendar=" + block);
                mAppBlockCalendar.setChecked(block);
                mAppBlockCalendar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mBlockCalendar = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppBlockCalendar: mPackageName=" + mPackageName + ", mAppBlockCalendar=" + mProfile.mBlockCalendar);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppBlockCalendar Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }

            mAppBlockMedia = (SwitchPreference) findPreference(APP_PROFILE_BLOCK_MEDIA);
            if( mAppBlockMedia != null ) {
                boolean block = mProfile.mBlockMedia;
                Log.e(TAG, "mAppBlockMedia: mPackageName=" + mPackageName + ", mAppBlockMedia=" + block);
                mAppBlockMedia.setChecked(block);
                mAppBlockMedia.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        mProfile.mBlockMedia = ((Boolean)newValue);
                        mAppSettings.updateProfile(mProfile);
                        mAppSettings.save();
                        Log.e(TAG, "mAppBlockMedia: mPackageName=" + mPackageName + ", mAppBlockMedia=" + mProfile.mBlockMedia);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mAppBlockMedia Fatal! exception", re );
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //ContentResolver resolver = getActivity().getContentResolver();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() - commit");
        if( mAppSettings != null ) {
            //if( mProfile != null ) mAppSettings.updateSystemSettings(mProfile);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    if( mProfile != null ) {
                        mAppSettings.updateSystemSettings(mProfile);
                    }
                    mAppSettings.commit();
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
        if( mAppSettings != null ) {
            //if( mProfile != null ) mAppSettings.updateSystemSettings(mProfile);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    if( mProfile != null ) {
                        mAppSettings.updateSystemSettings(mProfile);
                    }
                    mAppSettings.commit();
                }
            };
            thread.start();
        }
        Log.d(TAG, "onUnbindPreferences() - exit");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        Log.d(TAG, "onOptionsItemSelected(" + menuItem + ")");
        return super.onOptionsItemSelected(menuItem);
    }



    private boolean isStaminaWl() {
        if( mUid < Process.FIRST_APPLICATION_UID ) return true;
        if( mPackageName == null ) return false;
        if( mPackageName.startsWith("com.android.service.ims") ) return true;
        if( mPackageName.startsWith("com.android.launcher3") ) return true;
        if( mPackageName.startsWith("com.android.systemui") ) return true;
        if( mPackageName.startsWith("com.android.nfc") ) return true;
        if( mPackageName.startsWith("com.android.providers") ) return true;
        if( mPackageName.startsWith("com.android.inputmethod") ) return true;
        if( mPackageName.startsWith("com.qualcomm.qti.telephonyservice") ) return true;
        if( mPackageName.startsWith("com.android.phone") ) return true;
        if( mPackageName.startsWith("com.android.server.telecom") ) return true;
        if( mPackageName.startsWith("com.android.dialer") ) return true;
        if( mPackageName.startsWith("com.google.android.dialer") ) return true;
        if( mPackageName.startsWith("com.google.android.gsf") ) return true;
        if( mPackageName.startsWith("com.google.android.gms") ) return true;
        if( mPackageName.startsWith("com.google.android.contacts") ) return true;
        if( mPackageName.startsWith("com.google.android.calendar") ) return true;
        if( mPackageName.startsWith("com.google.android.ims") ) return true;
        if( mPackageName.startsWith("com.google.android.ext.shared") ) return true;
        return false;
    }

    private boolean isStaminaImportant() {
        try {
            if( SystemProperties.get("baikal.dialer","").equals(mPackageName) ) return true; 
            if( SystemProperties.get("baikal.sms","").equals(mPackageName) ) return true; 
            if( SystemProperties.get("baikal.call_screening","").equals(mPackageName) ) return true; 
        } catch( Exception e ) {
        }
        return false;
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.app_profile);

}
