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

package ru.baikalos.settings.fragments;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Process;
import android.os.SystemProperties;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;
import android.provider.Settings;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;

import android.os.ServiceManager;
import android.os.RemoteException;

import android.content.res.Resources;

import android.baikalos.BaikalAppProfile;
import android.baikalos.IBaikalService;
import android.baikalos.IBaikalAppProfileService;
//import com.android.internal.baikalos.BaikalAppProfileBackend;

//import com.android.internal.baikalos.BaikalPowerWhitelistBackend;
//import com.android.internal.baikalos.BaikalSpoofer;
import com.android.internal.baikalos.BaikalConstants;
import ru.baikalos.settings.*;
import ru.baikalos.settings.BaikalFlags;
import ru.baikalos.settings.BaikalFlags.BaikalFlag;
import ru.baikalos.settings.utils.Util;


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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import java.lang.FunctionalInterface;

import androidx.annotation.Keep;
@Keep
public class BaikalAppProfileFragment extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "BaikalPreferences";

    public static final String ARG_PACKAGE_NAME = "package";
    public static final String ARG_PACKAGE_UID = "uid";

    private IBaikalAppProfileService mAppProfileService;
    private IBaikalService mService;

    private String mPackageName;
    private int mUid;
    private Context mContext;

    private SwitchPreferenceCompat mAppDisable;


    private BaikalAppProfile mProfile;
    boolean mChanged = false;

    private boolean mEmptyBoolean;
    private String mEmptyString;
    private int mEmptyInt;


    private boolean isKernelIncompatible = false;

    public BaikalAppProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaikalFlags.Instance().init();

        Bundle args = getArguments();

        mPackageName = args.getString(ARG_PACKAGE_NAME);
        mUid = args.getInt(ARG_PACKAGE_UID);

        addPreferencesFromResource(R.xml.baikalos_app_profile);

        mContext = (Context) getActivity();
        final Resources res = getActivity().getResources();

        Log.w(TAG, "BaikalAppProfileFragment: " + mPackageName + "/" + mUid);

        mService = mContext.getBaikalContext().getBaikalService();
        mAppProfileService = mContext.getBaikalContext().getBaikalAppProfileService();
        
        String[] perfProfiles = Util.getPerfModeEntries(mContext); //getResources().getStringArray(R.array.performance_listvalues);
        String[] thermProfiles = getResources().getStringArray(R.array.thermal_listvalues);

        boolean perfProf  = (perfProfiles !=null && perfProfiles.length > 1);
        boolean thermProf  = (thermProfiles !=null && thermProfiles.length > 1);

        /*
        if( !BaikalConstants.isKernelCompatible() ) {
            Log.e(TAG, "profiles : incompatible kernel");
            isKernelIncompatible = true;
        }

        Log.e(TAG, "perf profiles : perfProfiles=" + perfProfiles);
        Log.e(TAG, "perf profiles : perfProfiles.length=" + perfProfiles.length);
        */

        try {
            mProfile = mAppProfileService.getProfile(mUid);
        } catch(Exception e) {
            Log.e(TAG, "mProfile: wtf", e);
        }

        if( mProfile == null ) {
            mProfile = new BaikalAppProfile(mUid);
            Log.e(TAG, "Loaded default: mUid=" + mUid + " pf=" + mProfile.serialize());
        } else {
            Log.e(TAG, "Loaded: mUid=" + mUid + " pf=" + mProfile.serialize());
        }

        try {

            PreferenceCategory mainCategory = (PreferenceCategory) findPreference("app_profile_main");

            PreferenceScreen screen = getPreferenceScreen();

            screen.setTitle(getApplicationName(mPackageName));
            getActivity().setTitle(getApplicationName(mPackageName));

            mainCategory.setTitle(mPackageName);
            mainCategory.setSummary(mPackageName);

            mAppDisable = (SwitchPreferenceCompat) findPreference("app_profile_disable");

            if( mAppDisable != null ) {
                int state = getApplicationState(mPackageName);
                boolean enabled = state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
                mAppDisable.setChecked(!enabled);
                mainCategory.setEnabled(enabled);
                Log.e(TAG, "mAppDisable: mPackageName=" + mPackageName + ", state=" + state);

                if( UserHandle.getAppId(mUid) < 10000 || isSystemWl() || isAppManagerWl() ) {
                    mAppDisable.setEnabled(false);
                } else {
                    mAppDisable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                boolean disabled = ((Boolean)newValue);
                                if( disabled ) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setCancelable(false);  
                                    builder.setTitle(R.string.app_dsable_confirm_title);
                                    builder.setMessage(R.string.app_dsable_confirm_summary);
                                    builder.setPositiveButton(R.string.app_disable_confirm_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            int state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                                            Log.e(TAG, "mAppDisable: mPackageName=" + mPackageName + ",state=" + state);
                                            setApplicationState(mPackageName, state);
                                            mainCategory.setEnabled(!disabled);
                                        }
                                    });
                                    builder.setNegativeButton(R.string.app_disable_confirm_cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            mAppDisable.setChecked(false);
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                } else {                              
                                    int state = disabled ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                                    Log.e(TAG, "mAppDisable: mPackageName=" + mPackageName + ",state=" + state);
                                    setApplicationState(mPackageName, state);
                                    mainCategory.setEnabled(!disabled);
                                }
                            } catch(Exception re) {
                                Log.e(TAG, "onCreate: mAppDisable Fatal! exception", re );
                            }
                            return true;
                        }
                    });
                }
            }

            if( UserHandle.getAppId(mUid) < 10000 ) {
//                mAppDisable.setChecked(!enabled);
                mainCategory.setEnabled(false);
            }

            initSwitchPreference(false, true, "app_profile_old_links", false, v->this.mEmptyBoolean = v);

            initSwitchPreference(true, true, "app_profile_debug", BaikalAppProfile.BAIKAL_APP_DEBUG, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_fake_su", BaikalAppProfile.BAIKAL_APP_FAKE_SU, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_hiddenapi_su", BaikalAppProfile.BAIKAL_APP_ALLOW_HIDDEN_API, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_allpermissions_su", BaikalAppProfile.BAIKAL_APP_ALLOW_ALL_PERMISSIONS, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);


            ListPreference perfModeList = (ListPreference) findPreference("app_profile_performance");
            if( perfModeList != null ) {
                perfModeList.setEntries(Util.getPerfModeEntries(mContext));
                perfModeList.setEntryValues(Util.getPerfModeValues(mContext));
            }

            initListPreference(perfProf, true, "app_profile_performance", () -> Integer.toString(mProfile.mPerfProfile), v -> mProfile.mPerfProfile = Integer.parseInt(v));

            initListPreference(thermProf, true, "app_profile_thermal", () -> Integer.toString(mProfile.mThermalProfile), v -> mProfile.mThermalProfile = Integer.parseInt(v));
            initListPreference(false, true, "app_profile_cpu_performance_limit", () -> Integer.toString(mProfile.mPerformanceLevel), v -> mProfile.mPerformanceLevel = Integer.parseInt(v));
            initListPreference(false, true, "app_profile_boost_control", () -> Integer.toString(mProfile.mBoostControl), v -> mProfile.mBoostControl = Integer.parseInt(v));

            initSwitchPreference(true, true, "app_profile_bypass_charging", BaikalAppProfile.BAIKAL_APP_BYPASS_CHARGING, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_spoof_default_dialer", BaikalAppProfile.BAIKAL_APP_DEFAULT_DIALER, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_spoof_default_sms", BaikalAppProfile.BAIKAL_APP_DEFAULT_SMS, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);

            initSwitchPreference(true, true, "app_profile_pinned", BaikalAppProfile.BAIKAL_BACKGROUND_PINNED, () -> mProfile.mBackgroundMode, v -> mProfile.mBackgroundMode = v);
            initSwitchPreference(true, true, "app_profile_donotclose", BaikalAppProfile.BAIKAL_BACKGROUND_DONOTCLOSE, () -> mProfile.mBackgroundMode, v -> mProfile.mBackgroundMode = v);
            initSwitchPreference(true, true, "app_profile_disable_boot", BaikalAppProfile.BAIKAL_BACKGROUND_BOOT_DISABLED, () -> mProfile.mBackgroundMode, v -> mProfile.mBackgroundMode = v);
            initSwitchPreference(true, true, "app_profile_disable_wakeup", BaikalAppProfile.BAIKAL_BACKGROUND_DONOTWAKE, () -> mProfile.mBackgroundMode, v -> mProfile.mBackgroundMode = v);
            initSwitchPreference(true, true, "app_profile_heavy_memory", BaikalAppProfile.BAIKAL_BACKGROUND_HEAVY_MEM, () -> mProfile.mBackgroundMode, v -> mProfile.mBackgroundMode = v);
            initSwitchPreference(false, false, "app_profile_heavy_cpu", BaikalAppProfile.BAIKAL_BACKGROUND_HEAVY_CPU, () -> mProfile.mBackgroundMode, v -> mProfile.mBackgroundMode = v);

            initListPreference(true, true, "app_profile_background", () -> Integer.toString(mProfile.mBackgroundLevel), v -> mProfile.mBackgroundLevel = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_spoof", () -> Integer.toString(mProfile.mSpoofDevice), v -> mProfile.mSpoofDevice = Integer.parseInt(v));

            initSwitchPreference(true, true, "app_profile_phka", BaikalAppProfile.BAIKAL_SPOOF_INTEGRITY_SW_ATTEST, () -> mProfile.mSpoof, v -> mProfile.mSpoof = v);
            initSwitchPreference(true, true, "app_profile_devmode", BaikalAppProfile.BAIKAL_SPOOF_INTEGRITY_HIDE_DEBUG, () -> mProfile.mSpoof, v -> mProfile.mSpoof = v);
            initSwitchPreference(true, true, "app_profile_filterfs", BaikalAppProfile.BAIKAL_SPOOF_INTEGRITY_FILTER_FS, () -> mProfile.mSpoof, v -> mProfile.mSpoof = v);
            initSwitchPreference(true, true, "app_profile_filterfs_add", BaikalAppProfile.BAIKAL_SPOOF_INTEGRITY_FILTER_FS_ADD, () -> mProfile.mSpoof, v -> mProfile.mSpoof = v);

            initSwitchPreference(true, true, "app_profile_allowwhileidle", BaikalAppProfile.BAIKAL_BACKGROUND_ALLOW_WHILE_IDLE, () -> mProfile.mBackgroundMode, v -> mProfile.mBackgroundMode = v);
            initSwitchPreference(true, true, "app_profile_idle_network", BaikalAppProfile.BAIKAL_BACKGROUND_NET_WHILE_IDLE, () -> mProfile.mBackgroundMode, v -> mProfile.mBackgroundMode = v);

            initSwitchPreference(true, true, "app_profile_hide_idle", BaikalAppProfile.BAIKAL_APP_HIDE_IDLE, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_hide_location", BaikalAppProfile.BAIKAL_APP_HIDE_LOCATION, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_hide_battery_opt", BaikalAppProfile.BAIKAL_APP_HIDE_BATTERY_OPT, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_hide_phone_opt", BaikalAppProfile.BAIKAL_APP_HIDE_PHONE, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);
            initSwitchPreference(true, true, "app_profile_hide_sms_opt", BaikalAppProfile.BAIKAL_APP_HIDE_SMS, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);

            initSwitchPreference(true, true, "app_profile_block_overlays", BaikalAppProfile.BAIKAL_APP_BLOCK_OVERLAYS, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);

            initListPreference(true, true, "app_profile_brightness", () -> Integer.toString(mProfile.mBrightness), v -> mProfile.mBrightness = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_rotation", () -> Integer.toString(mProfile.mRotation), v -> mProfile.mRotation = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_darkmode", () -> Integer.toString(mProfile.mDarkMode), v -> mProfile.mDarkMode = Integer.parseInt(v));

            boolean hasMaxFps = mContext.getResources().getBoolean(R.bool.config_show_peak_refresh_rate_switch);
            boolean hasMinFps = mContext.getResources().getBoolean(R.bool.config_show_min_refresh_rate_switch);

            ListPreference appMaxFps = (ListPreference) initListPreference(hasMaxFps, true, "app_profile_max_fps", () -> Integer.toString(mProfile.mMaxFrameRate), v -> mProfile.mMaxFrameRate = Integer.parseInt(v));
            ListPreference appMinFps = (ListPreference) initListPreference(hasMinFps, true, "app_profile_min_fps", () -> Integer.toString(mProfile.mMinFrameRate), v -> mProfile.mMinFrameRate = Integer.parseInt(v));

            if( hasMinFps || hasMaxFps ) { 
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


                if( hasMinFps && appMinFps != null ) {
                    appMinFps.setEntries(entries.toArray(new String[entries.size()]));
                    appMinFps.setEntryValues(values.toArray(new String[values.size()]));
                }
                if( hasMaxFps && appMaxFps != null ) {
                    appMaxFps.setEntries(entries.toArray(new String[entries.size()]));
                    appMaxFps.setEntryValues(values.toArray(new String[values.size()]));
                }
            } 

            if( !hasMinFps && appMinFps != null ) appMinFps.setVisible(false);
            if( !hasMaxFps && appMaxFps != null ) appMaxFps.setVisible(false);

            initListPreference(true, true, "app_profile_keep_on", () -> Integer.toString(mProfile.mKeepOn), v -> mProfile.mKeepOn = Integer.parseInt(v));

            initSwitchPreference(true, true, "app_profile_override_fonts", BaikalAppProfile.BAIKAL_OVERRIDE_FONTS, () -> mProfile.mOverride, v -> mProfile.mOverride = v);
            initSwitchPreference(true, true, "app_profile_forced_screenshot", BaikalAppProfile.BAIKAL_OVERRIDE_FORCED_SCREENSHOT, () -> mProfile.mOverride, v -> mProfile.mOverride = v);
            initListPreference(true, true, "app_profile_installer", () -> Integer.toString(mProfile.mInstaller), v -> mProfile.mInstaller = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_location", () -> Integer.toString(mProfile.mLocationLevel), v -> mProfile.mLocationLevel = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_file_access", () -> Integer.toString(mProfile.mFileAccess), v -> mProfile.mFileAccess = Integer.parseInt(v));
            initSwitchPreference(true, true, "app_profile_allow_sig_override", BaikalAppProfile.BAIKAL_OVERRIDE_SIGNATURE, () -> mProfile.mOverride, v -> mProfile.mOverride = v);

            initSwitchPreference(true, true, "app_profile_block_focus_recv", BaikalAppProfile.BAIKAL_AUDIO_BAFR, () -> mProfile.mAudio, v -> mProfile.mAudio = v);
            initSwitchPreference(true, true, "app_profile_block_focus_send", BaikalAppProfile.BAIKAL_AUDIO_BAFS, () -> mProfile.mAudio, v -> mProfile.mAudio = v);
            initListPreference(false, false, "app_profile_force_sonification", () -> Integer.toString(mEmptyInt), v -> mEmptyInt = Integer.parseInt(v));

            initListPreference(true, true, "app_profile_camera", () -> Integer.toString(mProfile.mCameraMode), v -> mProfile.mCameraMode = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_microphone", () -> Integer.toString(mProfile.mMicrophoneMode), v -> mProfile.mMicrophoneMode = Integer.parseInt(v));

            boolean hasHms = Utils.isPackageInstalled(mContext, "com.huawei.hwid");
            boolean hasGms = Utils.isPackageInstalled(mContext, "com.google.android.gms");

            initListPreference(hasHms, true, "app_profile_hide_hms", () -> Integer.toString(mProfile.mBlockHMS), v -> mProfile.mBlockHMS = Integer.parseInt(v));
            initListPreference(hasGms, true, "app_profile_hide_gms", () -> Integer.toString(mProfile.mBlockGMS), v -> mProfile.mBlockGMS = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_hide_3p", () -> Integer.toString(mProfile.mBlock3P), v -> mProfile.mBlock3P = Integer.parseInt(v));

            initListPreference(true, true, "app_profile_block_notification", () -> Integer.toString(mProfile.mBlockNotification), v -> mProfile.mBlockNotification = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_block_sms", () -> Integer.toString(mProfile.mBlockSMS), v -> mProfile.mBlockSMS = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_block_contacts", () -> Integer.toString(mProfile.mBlockContacts), v -> mProfile.mBlockContacts = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_block_calllog", () -> Integer.toString(mProfile.mBlockCallLog), v -> mProfile.mBlockCallLog = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_block_calendar", () -> Integer.toString(mProfile.mBlockCalendar), v -> mProfile.mBlockCalendar = Integer.parseInt(v));
            initListPreference(true, true, "app_profile_block_media", () -> Integer.toString(mProfile.mBlockMedia), v -> mProfile.mBlockMedia = Integer.parseInt(v));

            initSwitchPreference(true, true, "app_profile_priv_phone_state", BaikalAppProfile.BAIKAL_APP_PRIVELEGED_PHONE, () -> mProfile.mAppOpts, v -> mProfile.mAppOpts = v);

            initEditTextPreference(true, true, "app_profile_spoof_sim_country", () -> mProfile.mSpoofSimCountry, v -> mProfile.mSpoofSimCountry = String.valueOf(v));
            initEditTextPreference(true, true, "app_profile_spoof_sim_mnc", () -> mProfile.mSpoofSimMnc, v -> mProfile.mSpoofSimMnc = String.valueOf(v));
            initEditTextPreference(true, true, "app_profile_spoof_sim_opname", () -> mProfile.mSpoofSimOpName, v -> mProfile.mSpoofSimOpName = String.valueOf(v));
            initEditTextPreference(true, true, "app_profile_spoof_sim_ln", () -> mProfile.mSpoofSimLN, v -> mProfile.mSpoofSimLN = String.valueOf(v));

       } catch(Exception re) {
           Log.e(TAG, "onCreate: Fatal! exception", re );
       }
       
    }

    private SwitchPreferenceCompat initSwitchPreference(boolean visible, boolean enabled, String name, int opt_flag, IntGetter getter, IntSetter setter) {
        BaikalFlags.BaikalFlag flag = BaikalFlags.Instance().get(name);
        return initSwitchPreference(flag.getVisibility(visible), flag.getEnabled(enabled), name, (getter.get() & opt_flag) != 0,
            v -> {
                int val = getter.get();
                if (v) val |= opt_flag;
                else   val &= ~opt_flag;
                setter.set(val);
            });    
    }

    private SwitchPreferenceCompat initSwitchPreference(boolean visible, boolean enabled, String name, boolean value, Consumer<Boolean> setter) {
        SwitchPreferenceCompat mPref = (SwitchPreferenceCompat) findPreference(name);
        if( mPref != null ) {
            Log.d(TAG, name + ": mPackageName=" + mPackageName + ", val=" + value);
            if( !visible ) { mPref.setVisible(false); return mPref; }
            if( !enabled ) mPref.setEnabled(false);
            mPref.setChecked(value);
            mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        setter.accept((Boolean)newValue);
                        mChanged = true;
                        Log.e(TAG, name + ": mPackageName=" + mPackageName + ", newval=" + (Boolean)newValue);
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: " + name + " Fatal! exception", re );
                    }
                    return true;
                }
            });
        } else {
            Log.e(TAG, name + ": undefined key for mPackageName=" + mPackageName + ", val=" + value);
        }
        return mPref;
    }

    private EditTextPreference initEditTextPreference(boolean visible, boolean enabled, String name, StringGetter getter, StringSetter setter) {
        BaikalFlags.BaikalFlag flag = BaikalFlags.Instance().get(name);
        return initEditTextPreference(flag.getVisibility(visible), flag.getEnabled(enabled), name, getter.get(), v -> { setter.set(v); });
    }

    private EditTextPreference initEditTextPreference(boolean visible, boolean enabled, String name, String value, Consumer<String> setter) {
        EditTextPreference mPref = (EditTextPreference) findPreference(name);
        if( mPref != null ) {
            Log.e(TAG, name + ": mPackageName=" + mPackageName + ", val=" + value);
            if( !visible ) { mPref.setVisible(false); return mPref; }
            if( !enabled ) mPref.setEnabled(false);
            mPref.setText(value);
            mPref.setSummary(value);
            mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        setter.accept(newValue.toString());
                        mPref.setSummary(newValue.toString());
                        mChanged = true;
                        Log.e(TAG, name + ": mPackageName=" + mPackageName + ", newval=" + newValue.toString());
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: " + name + " Fatal! exception", re );
                    }
                    return true;
                }
            });
        } else {
            Log.e(TAG, name + ": undefined key for mPackageName=" + mPackageName + ", val=" + value);
        }
        return mPref;
    }

    private ListPreference initListPreference(boolean visible, boolean enabled, String name, StringGetter getter, StringSetter setter) {
        BaikalFlags.BaikalFlag flag = BaikalFlags.Instance().get(name);
        return initListPreference(flag.getVisibility(visible), flag.getEnabled(enabled), name, getter.get(), v -> { setter.set(v); });
    }

    private ListPreference initListPreference(boolean visible, boolean enabled, String name, String value, Consumer<String> setter) {
        ListPreference mPref = (ListPreference) findPreference(name);
        if( mPref != null ) {
            Log.e(TAG, name + ": mPackageName=" + mPackageName + ", val=" + value);
            if( !visible ) { mPref.setVisible(false); return mPref; }
            if( !enabled ) mPref.setEnabled(false);
            mPref.setValue(value);
            mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        setter.accept(newValue.toString());
                        mChanged = true;
                        Log.e(TAG, name + ": mPackageName=" + mPackageName + ", newval=" + newValue.toString());
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: " + name + " Fatal! exception", re );
                    }
                    return true;
                }
            });
        } else {
            Log.e(TAG, name + ": undefined key for mPackageName=" + mPackageName + ", val=" + value);
        }
        return mPref;
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
        //Log.d(TAG, "onSaveInstanceState() - commit");
        //Log.d(TAG, "mProfile=" + mProfile.serialize());
        /*
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
        }*/
        //Log.d(TAG, "onSaveInstanceState() - exit");
    }


    @Override
    protected void onUnbindPreferences() {
        super.onUnbindPreferences();
        Log.d(TAG, "onUnbindPreferences() - commit");
        if( mProfile != null ) {
            Log.d(TAG, "mProfile=" + mProfile.serialize());
            if( mChanged ) {
                Log.d(TAG, "Saving profile " + mPackageName + "/" + mUid);
                try {
                    mAppProfileService.saveProfile(mProfile);
                } catch(Exception e) {
                    Log.e(TAG, "mProfile: wtf", e);
                }
            }
        }
        /*
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
        }*/
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


    private boolean isSystemWl() {
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
        return false;
    }

    public static final String[] sAppManagerPackages =
    {
        "com.android.vending",
        "com.google.android.gms",
        "com.google.android.gms.policy_sidecar_aps",
        "com.google.android.gsf",
        "com.google.android.markup",
        "com.google.android.projection.gearhead",
        "com.google.android.syncadapters.calendar",
        "com.google.android.syncadapters.contacts",
        "com.google.android.syncadapters.contacts",
        "com.google.android.gm.exchange",
        "com.google.android.apps.customization.pixel",
        "com.google.android.apps.restore",
        "com.google.android.apps.wellbeing",
        "com.google.android.soundpicker",
        "com.google.android.settings.intelligence",
        "com.google.android.setupwizard",
        "com.google.android.partnersetup",
        "com.google.android.feedback",
        "com.google.android.tts",
        "com.google.android.marvin.talkback",
        "com.google.android.googlequicksearchbox",
        "com.huawei.hwid",
        "com.huawei.appmarket",
        "com.dolby.daxservice",
        "com.dolby.daxappui",
        "james.dsp",
        "org.lineageos.audiofx",
        "com.crdroid.faceunlock"
    };



    private boolean isAppManagerWl() {
        if( mUid < Process.FIRST_APPLICATION_UID ) return true;
        if( mPackageName == null ) return false;
        if( Arrays.stream(sAppManagerPackages).anyMatch(mPackageName::equals) ) {
            return true;
        }
        return false;
    }


    /*
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
    }*/

    private int getApplicationState(String packageName) {
        try {
            return getPackageManager().getApplicationEnabledSetting(packageName);
        } catch (Exception e) {
            Log.d(TAG, "setApplicationState:", e);
        }
        return PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
    }

    private void setApplicationState(String packageName, int state) {
        try {
            getPackageManager().setApplicationEnabledSetting(packageName,state,0);
        } catch (Exception e) {
            Log.d(TAG, "setApplicationState:", e);
        }
    }

    private String getApplicationName(String packageName) {
        final PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName,
                            PackageManager.GET_META_DATA);
            CharSequence title = info.applicationInfo.loadLabel(pm);
            return title != null ? title.toString() : packageName;
        } catch (Exception e) {
            Log.d(TAG, "getApplicationName:", e);
        }
        return packageName;
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.baikalos_app_profile);

    @FunctionalInterface
    public interface IntGetter {
        int get();
    }

    @FunctionalInterface
    public interface IntSetter {
        void set(int value);
    }

    @FunctionalInterface
    public interface StringGetter {
        String get();
    }

    @FunctionalInterface
    public interface StringSetter {
        void set(String value);
    }

}
