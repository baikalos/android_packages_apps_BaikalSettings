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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.crdroid.OmniJawsClient;
import com.android.internal.util.crdroid.Utils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.crdroid.settings.fragments.lockscreen.UdfpsSettings;

import java.util.List;

import com.crdroid.settings.utils.ImageUtils;

import lineageos.providers.LineageSettings;

@SearchIndexable
public class LockScreen extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener  {

    public static final String TAG = "LockScreen";

    private static final String LOCKSCREEN_INTERFACE_CATEGORY = "lockscreen_interface_category";
    private static final String LOCKSCREEN_GESTURES_CATEGORY = "lockscreen_gestures_category";
    private static final String KEY_UDFPS_SETTINGS = "udfps_settings";
    private static final String KEY_FP_SUCCESS_VIBRATE = "fp_success_vibrate";
    private static final String KEY_FP_ERROR_VIBRATE = "fp_error_vibrate";
    private static final String KEY_RIPPLE_EFFECT = "enable_ripple_effect";
    private static final String KEY_WEATHER = "lockscreen_weather_enabled";

    private static final String CUSTOM_IMAGE_REQUEST_CODE_KEY = "lockscreen_custom_image";
    private static final int CUSTOM_IMAGE_REQUEST_CODE = 1001;

    private Preference mCustomImagePreference;
    private Preference mUdfpsSettings;
    private Preference mFingerprintVib;
    private Preference mFingerprintVibErr;
    private Preference mRippleEffect;
    private Preference mWeather;

    private OmniJawsClient mWeatherClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.crdroid_settings_lockscreen);

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources resources = context.getResources();

        mCustomImagePreference = findPreference(CUSTOM_IMAGE_REQUEST_CODE_KEY);
        //int clockStyle = Settings.Secure.getIntForUser(getContext().getContentResolver(), "clock_style", 0, UserHandle.USER_CURRENT);
        String imagePath = Settings.System.getString(getContext().getContentResolver(), "custom_aod_image_uri");
        if (imagePath != null /*&& clockStyle > 0*/) {
            mCustomImagePreference.setSummary(imagePath);
            mCustomImagePreference.setEnabled(true);
        } /*else if (clockStyle == 0) {
            mCustomImagePreference.setSummary(getContext().getString(R.string.custom_aod_image_not_supported));
            mCustomImagePreference.setEnabled(false);
        }*/


        PreferenceCategory interfaceCategory = (PreferenceCategory) findPreference(LOCKSCREEN_INTERFACE_CATEGORY);
        PreferenceCategory gestCategory = (PreferenceCategory) findPreference(LOCKSCREEN_GESTURES_CATEGORY);

        FingerprintManager mFingerprintManager = (FingerprintManager)
                getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        mUdfpsSettings = (Preference) findPreference(KEY_UDFPS_SETTINGS);
        mFingerprintVib = (Preference) findPreference(KEY_FP_SUCCESS_VIBRATE);
        mFingerprintVibErr = (Preference) findPreference(KEY_FP_ERROR_VIBRATE);
        mRippleEffect = (Preference) findPreference(KEY_RIPPLE_EFFECT);

        try {
            boolean hasEnrolledFingerprints = mFingerprintManager.hasEnrolledFingerprints(UserHandle.myUserId());
            Log.d(TAG, "hasEnrolledFingerprints:" + hasEnrolledFingerprints);

            if( hasEnrolledFingerprints ) {
                SwitchPreference pref = (SwitchPreference) findPreference("baikalos_fp_wake_enabled");
                pref.setChecked(false);
                pref.setEnabled(false);
            }
        } catch(Exception e) {
            Log.d(TAG, "hasEnrolledFingerprints:", e);
        }

        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
            interfaceCategory.removePreference(mUdfpsSettings);
            gestCategory.removePreference(mFingerprintVib);
            gestCategory.removePreference(mFingerprintVibErr);
            gestCategory.removePreference(mRippleEffect);
        } else {
            if (!Utils.isPackageInstalled(getContext(), "com.crdroid.udfps.icons")) {
                interfaceCategory.removePreference(mUdfpsSettings);
            }
        }

        mWeather = (Preference) findPreference(KEY_WEATHER);
        mWeatherClient = new OmniJawsClient(getContext());
        updateWeatherSettings();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        LineageSettings.Secure.putIntForUser(resolver,
                LineageSettings.Secure.LOCKSCREEN_MEDIA_METADATA, 0, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.KG_CUSTOM_CLOCK_TOP_MARGIN, 280, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.KG_SMALL_CLOCK_TEXT_SIZE, 86, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.KG_LARGE_CLOCK_TEXT_SIZE, 180, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_BATTERY_INFO, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.DOUBLE_TAP_SLEEP_LOCKSCREEN, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_ALBUMART_FILTER, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ENABLE_RIPPLE_EFFECT, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FP_ERROR_VIBRATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FP_SUCCESS_VIBRATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_ENABLE_POWER_MENU, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_ENABLED, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_LOCATION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_STYLE, 0, UserHandle.USER_CURRENT);
        UdfpsSettings.reset(mContext);
    }

    private void updateWeatherSettings() {
        if (mWeatherClient == null || mWeather == null) return;

        boolean weatherEnabled = mWeatherClient.isOmniJawsEnabled();
        mWeather.setEnabled(weatherEnabled);
        mWeather.setSummary(weatherEnabled ? R.string.lockscreen_weather_summary :
            R.string.lockscreen_weather_enabled_info);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeatherSettings();
    }


    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mCustomImagePreference) {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, CUSTOM_IMAGE_REQUEST_CODE);
            } catch(Exception e) {
                Toast.makeText(getContext(), R.string.quick_settings_header_needs_gallery, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == CUSTOM_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && result != null) {
            Uri imgUri = result.getData();
            if (imgUri != null) {
                String savedImagePath = ImageUtils.saveImageToInternalStorage(getContext(), imgUri, "lockscreen_aod_image", "LOCKSCREEN_CUSTOM_AOD_IMAGE");
                if (savedImagePath != null) {
                    ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putStringForUser(resolver, "custom_aod_image_uri", savedImagePath, UserHandle.USER_CURRENT);
                    mCustomImagePreference.setSummary(savedImagePath);
                }
            }
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
            new BaseSearchIndexProvider(R.xml.crdroid_settings_lockscreen) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    FingerprintManager mFingerprintManager = (FingerprintManager)
                            context.getSystemService(Context.FINGERPRINT_SERVICE);
                    if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
                        keys.add(KEY_UDFPS_SETTINGS);
                        keys.add(KEY_FP_SUCCESS_VIBRATE);
                        keys.add(KEY_FP_ERROR_VIBRATE);
                        keys.add(KEY_RIPPLE_EFFECT);
                    } else {
                        if (!Utils.isPackageInstalled(context, "com.crdroid.udfps.icons")) {
                            keys.add(KEY_UDFPS_SETTINGS);
                        } else {
                            keys.add(KEY_FP_SUCCESS_VIBRATE);
                            keys.add(KEY_FP_ERROR_VIBRATE);
                        }
                    }

                    return keys;
                }
            };
}
