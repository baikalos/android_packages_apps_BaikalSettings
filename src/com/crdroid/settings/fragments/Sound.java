/*
 * Copyright (C) 2016-2022 crDroid Android Project
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
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;


import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.crdroid.settings.fragments.sound.AdaptivePlayback;
import com.crdroid.settings.fragments.sound.PulseSettings;
import com.crdroid.settings.utils.TelephonyUtils;

import java.util.List;
import java.util.ArrayList;

import lineageos.providers.LineageSettings;

@SearchIndexable
public class Sound extends SettingsPreferenceFragment {

    public static final String TAG = "Sound";

    private static final String KEY_VIBRATE_CATEGORY = "incall_vib_options";
    private static final String KEY_VIBRATE_CONNECT = "vibrate_on_connect";
    private static final String KEY_VIBRATE_CALLWAITING = "vibrate_on_callwaiting";
    private static final String KEY_VIBRATE_DISCONNECT = "vibrate_on_disconnect";
    private static final String KEY_VOLUME_PANEL_LEFT = "volume_panel_on_left";

    private static final String SONIF_A2DP_PREF = "sonif_a2dp_pref";

    private static final String AUDIO_TWEAKS_A2DP_LAST_CODEC = "audio_tweaks_a2dp_last_codec";
    private static final String AUDIO_TWEAKS_A2DP_LAST_BITRATE = "audio_tweaks_a2dp_last_bitrate";

    private static final String SYSTEM_PROPERTY_A2DP_LAST_CODEC = "baikal.last.a2dp_codec";
    private static final String SYSTEM_PROPERTY_A2DP_LAST_BITRATE = "baikal.last.a2dp_bitrate";

    private SwitchPreference mVolumePanelLeft;
    private ListPreference mSonifA2dp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_sound);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

        boolean isAudioPanelOnLeft = LineageSettings.Secure.getIntForUser(resolver,
                LineageSettings.Secure.VOLUME_PANEL_ON_LEFT, isAudioPanelOnLeftSide(getActivity()) ? 1 : 0,
                UserHandle.USER_CURRENT) != 0;

        mVolumePanelLeft = (SwitchPreference) prefScreen.findPreference(KEY_VOLUME_PANEL_LEFT);
        mVolumePanelLeft.setChecked(isAudioPanelOnLeft);

        final PreferenceCategory vibCategory = prefScreen.findPreference(KEY_VIBRATE_CATEGORY);

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(vibCategory);
        }

        try {
            int sonif_a2dp = 0;
            try {
                sonif_a2dp = Integer.parseInt(SystemProperties.get("persist.baikal.sonif_a2dp", "0"));
            } catch(Exception e1) {
            }

	        mSonifA2dp = (ListPreference) findPreference(SONIF_A2DP_PREF);
      	    if( mSonifA2dp != null ) { 
                Log.i(TAG, "mSonifA2dp: val=" + sonif_a2dp);
                mSonifA2dp.setValue(Integer.toString(sonif_a2dp));
                mSonifA2dp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                  public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        SystemProperties.set("persist.baikal.sonif_a2dp", newValue.toString());
                        Log.e(TAG, "mSonifA2dp: fps=" + newValue.toString());
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mSonifA2dp Fatal! exception", re );
                    }
                    return true;
                  }
                });
            }
        } catch(Exception e2) {
        }


        Preference  codec = (Preference) findPreference(AUDIO_TWEAKS_A2DP_LAST_CODEC);
        Preference  bitrate = (Preference) findPreference(AUDIO_TWEAKS_A2DP_LAST_BITRATE);

        String sCodec = SystemProperties.get(SYSTEM_PROPERTY_A2DP_LAST_CODEC,"");
        String sBitrate = SystemProperties.get(SYSTEM_PROPERTY_A2DP_LAST_BITRATE,"");


        if( sCodec != null && sCodec.startsWith("SBC HD") ) {
            codec.setVisible(true);
            codec.setSummary(sCodec);
            bitrate.setVisible(true);
            bitrate.setSummary(sBitrate + " kBit/s");
        } 
        else if( sCodec == null || sCodec.equals("") || sCodec.equals("NONE") ) {
            codec.setVisible(false);
            codec.setSummary("");
            bitrate.setVisible(false);
            bitrate.setSummary("");
        } else {
            codec.setVisible(true);
            codec.setSummary(sCodec);
            bitrate.setVisible(false);
            bitrate.setSummary("");
        }

    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        LineageSettings.Secure.putIntForUser(resolver,
                LineageSettings.Secure.VOLUME_PANEL_ON_LEFT, isAudioPanelOnLeftSide(mContext) ? 1 : 0,
                UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VIBRATE_ON_CONNECT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VIBRATE_ON_CALLWAITING, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VIBRATE_ON_DISCONNECT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.VOLUME_DIALOG_TIMEOUT, 3, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.SCREENSHOT_SHUTTER_SOUND, 1, UserHandle.USER_CURRENT);
        PulseSettings.reset(mContext);
        AdaptivePlayback.reset(mContext);
    }

    private static boolean isAudioPanelOnLeftSide(Context context) {
        try {
            Context con = context.createPackageContext("org.lineageos.lineagesettings", 0);
            int id = con.getResources().getIdentifier("def_volume_panel_on_left",
                    "bool", "org.lineageos.lineagesettings");
            return con.getResources().getBoolean(id);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.crdroid_settings_sound) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!TelephonyUtils.isVoiceCapable(context)) {
                        keys.add(KEY_VIBRATE_CATEGORY);
                        keys.add(KEY_VIBRATE_CONNECT);
                        keys.add(KEY_VIBRATE_CALLWAITING);
                        keys.add(KEY_VIBRATE_DISCONNECT);
                    }

                    return keys;
                }
            };
}
