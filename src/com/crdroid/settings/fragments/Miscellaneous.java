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
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;


import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

//import com.crdroid.settings.fragments.misc.GmsSwitch;
import com.crdroid.settings.fragments.misc.SensorBlock;
import com.crdroid.settings.fragments.misc.SmartCharging;

import java.util.ArrayList;
import java.util.List;

import lineageos.providers.LineageSettings;

import com.android.internal.baikalos.BaikalConstants;

@SearchIndexable
public class Miscellaneous extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "Miscellaneous";

    private static final String SMART_CHARGING = "smart_charging";
    private static final String POCKET_JUDGE = "pocket_judge";
    private static final String SYS_GAMES_SPOOF = "persist.sys.pixelprops.games";
    private static final String SYS_PHOTOS_SPOOF = "persist.sys.pixelprops.gphotos";
    private static final String SYS_NETFLIX_SPOOF = "persist.sys.pixelprops.netflix";

    private Preference mSmartCharging;
    private Preference mPocketJudge;

    private ListPreference mCaptivePortal;
    private ListPreference mCallScreeningService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_misc);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        mSmartCharging = (Preference) prefScreen.findPreference(SMART_CHARGING);
        boolean mSmartChargingSupported = res.getBoolean(
                com.android.internal.R.bool.config_bypassChargingAvailable);

        //if( !BaikalConstants.isKernelCompatible() ) mSmartChargingSupported = false;

        if (!mSmartChargingSupported && mSmartCharging != null)
        if (!mSmartChargingSupported)
            prefScreen.removePreference(mSmartCharging);

        if( !BaikalConstants.isKernelCompatible() ) {
            mSmartCharging.setEnabled(false);
        }

        boolean irCamAvailable = getResources().getBoolean(com.android.internal.R.bool.config_irCameraAvailable);
        if( !irCamAvailable ) {
            Preference mIgnoreIr = (Preference) prefScreen.findPreference("baikalos_camera_ignore_ir"); 
            if( mIgnoreIr != null ) {
                mIgnoreIr.setVisible(false);
            } 
        }

        mPocketJudge = (Preference) prefScreen.findPreference(POCKET_JUDGE);
        boolean mPocketJudgeSupported = res.getBoolean(
                com.android.internal.R.bool.config_pocketModeSupported);
        if (!mPocketJudgeSupported && mPocketJudge != null)
            prefScreen.removePreference(mPocketJudge);

        mCaptivePortal = (ListPreference) findPreference("system_captive_portal");
        if( mCaptivePortal != null ) {
            String portal = Settings.Global.getString(getActivity().getContentResolver(),
                        Settings.Global.CAPTIVE_PORTAL_HTTPS_URL);

            if( "https://captive.apple.com/generate_204".equals(portal) ) {
                portal = "apple";
            } else if( "https://api.browser.yandex.ru/generate_204".equals(portal) ) {
                portal = "yandex";
            } else {
                portal = "default";
            }
                    
            Log.i(TAG, "mCaptivePortal: portal=" + portal);
            mCaptivePortal.setValue(portal);
            mCaptivePortal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        String portal = (String)newValue;
                        if( "apple".equals(portal) ) {
                            Settings.Global.putString(getActivity().getContentResolver(),Settings.Global.CAPTIVE_PORTAL_HTTPS_URL, "https://captive.apple.com/generate_204");
                            Settings.Global.putString(getActivity().getContentResolver(),Settings.Global.CAPTIVE_PORTAL_HTTP_URL, "http://captive.apple.com/generate_204");
                        } else if( "yandex".equals(portal) ) {
                            Settings.Global.putString(getActivity().getContentResolver(),Settings.Global.CAPTIVE_PORTAL_HTTPS_URL, "https://api.browser.yandex.ru/generate_204");
                            Settings.Global.putString(getActivity().getContentResolver(),Settings.Global.CAPTIVE_PORTAL_HTTP_URL, "http://api.browser.yandex.ru/generate_204");
                        } else {
                            Settings.Global.putString(getActivity().getContentResolver(),Settings.Global.CAPTIVE_PORTAL_HTTPS_URL, "");
                            Settings.Global.putString(getActivity().getContentResolver(),Settings.Global.CAPTIVE_PORTAL_HTTP_URL, "");
                        }
                        Log.e(TAG, "mCaptivePortal: portal=" + portal);
                    } catch(Exception re) {
                        Log.e(TAG, "mCaptivePortal: mCaptivePortal Fatal! exception", re );
                    }
                    return true;
                }
            });
        }


        mCallScreeningService = (ListPreference) findPreference("callscreening_service");
        if( mCallScreeningService != null ) {

            PackageManager pm = getPackageManager();
            Intent intent = new Intent("android.telecom.CallScreeningService");
            List<ResolveInfo> services = pm.queryIntentServices(intent, PackageManager.MATCH_ALL);

            List<String> entries = new ArrayList<>();
            List<String> values = new ArrayList<>();

            entries.add(res.getString(R.string.default_app_none));
            values.add("");

            for (ResolveInfo info : services) {
                String pkgName = info.serviceInfo.packageName;
                CharSequence label = pkgName;
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
                    if( appInfo != null ) label = pm.getApplicationLabel(appInfo);
                } catch(Exception aie) {
                }

                Log.d(TAG, "mCallScreeningService: Supports call screening: " + pkgName + " -> " + label);

                entries.add(label.toString());
                values.add(pkgName);
            }

            mCallScreeningService.setEntries(entries.toArray(new String[entries.size()]));
            mCallScreeningService.setEntryValues(values.toArray(new String[values.size()]));
            
            String callScreeningPackageName = SystemProperties.get("persist.baikal.call_screening", "");
            mCallScreeningService.setValue(callScreeningPackageName);
            mCallScreeningService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        Log.e(TAG, "mCallScreeningService: set mCallScreeningPackageName=" + newValue.toString());
                        SystemProperties.set("persist.baikal.call_screening", newValue.toString());
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mCallScreeningService Fatal! exception", re );
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.POCKET_JUDGE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.THREE_FINGER_GESTURE, 0, UserHandle.USER_CURRENT);
        LineageSettings.System.putIntForUser(resolver,
                LineageSettings.System.AUTO_BRIGHTNESS_ONE_SHOT, 0, UserHandle.USER_CURRENT);
        //SystemProperties.set(SYS_GAMES_SPOOF, "false");
        //SystemProperties.set(SYS_PHOTOS_SPOOF, "true");
        //SystemProperties.set(SYS_NETFLIX_SPOOF, "false");
        //GmsSwitch.reset(mContext);
        SensorBlock.reset(mContext);
        SmartCharging.reset(mContext);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.crdroid_settings_misc) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();

                    boolean mSmartChargingSupported = res.getBoolean(
                            com.android.internal.R.bool.config_bypassChargingAvailable);
                    if (!mSmartChargingSupported)
                        keys.add(SMART_CHARGING);

                    boolean mPocketJudgeSupported = res.getBoolean(
                            com.android.internal.R.bool.config_pocketModeSupported);
                    if (!mPocketJudgeSupported)
                        keys.add(POCKET_JUDGE);

                    return keys;
                }
            };
}
