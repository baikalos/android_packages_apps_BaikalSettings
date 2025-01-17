/*
 * Copyright (C) 2017 AICP
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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.crdroid.settings.utils.Util;
import com.crdroid.settings.utils.SuShell;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.crdroid.Utils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.baikalos.BaikalConstants;

public class LogIt extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "BaikalExtras";

    private static final String PREF_LOGCAT = "logcat";
    private static final String PREF_LOGCAT_RADIO = "logcat_radio";
    private static final String PREF_KMSG = "kmseg";
    private static final String PREF_DMESG = "dmesg";
    private static final String PREF_DUMPSYS = "dumpsys";
    private static final String PREF_BAIKALOS_LOG_IT = "baikalos_log_it_now";
    private static final String PREF_SHARE_TYPE = "baikalos_log_share_type";

    private static final String BAIKALOS_ROOT = Environment.getExternalStorageDirectory() + "/BaikalOS";

    private static final String LOGCAT_FILE = new File(BAIKALOS_ROOT, "baikalos_logcat.txt").getAbsolutePath();
    private static final String LOGCAT_LAST_FILE = new File(BAIKALOS_ROOT, "baikalos_logcat_last.txt").getAbsolutePath();
    private static final String LOGCAT_RADIO_FILE = new File(BAIKALOS_ROOT, "baikalos_radiolog.txt").getAbsolutePath();
    private static final String KMSG_FILE = new File(BAIKALOS_ROOT, "baikalos_kmsg.txt").getAbsolutePath();
    private static final String DMESG_FILE = new File(BAIKALOS_ROOT, "baikalos_dmesg.txt").getAbsolutePath();
    private static final String DUMPSYS_FILE = new File(BAIKALOS_ROOT, "baikalos_dumpsys.txt").getAbsolutePath();

    private static final String HASTE_LOGCAT_KEY = new File(BAIKALOS_ROOT, "baikalos_haste_logcat_key").getAbsolutePath();
    private static final String HASTE_LOGCAT_LAST_KEY = new File(BAIKALOS_ROOT, "baikalos_haste_logcat_last_key").getAbsolutePath();
    private static final String HASTE_LOGCAT_RADIO_KEY = new File(BAIKALOS_ROOT, "baikalos_haste_logcat_radio_key").getAbsolutePath();
    private static final String HASTE_KMSG_KEY = new File(BAIKALOS_ROOT, "baikalos_haste_kmsg_key").getAbsolutePath();
    private static final String HASTE_DMESG_KEY = new File(BAIKALOS_ROOT, "baikalos_haste_dmesg_key").getAbsolutePath();
    private static final String HASTE_DUMPSYS_KEY = new File(BAIKALOS_ROOT, "baikalos_haste_dumpsys_key").getAbsolutePath();

    private static final String BAIKALOS_HASTE = "https://hastebin.com/documents";
    //private static final File sdCardDirectory = Environment.getExternalStorageDirectory();
    private static final File logcatFile = new File(BAIKALOS_ROOT, "baikalos_logcat.txt");
    private static final File logcatHasteKey = new File(BAIKALOS_ROOT, "baikalos_haste_logcat_key");
    private static final File logcatLastFile = new File(BAIKALOS_ROOT, "baikalos_logcat_last.txt");
    private static final File logcatLastHasteKey = new File(BAIKALOS_ROOT, "baikalos_haste_logcat_last_key");
    private static final File logcatRadioFile = new File(BAIKALOS_ROOT, "baikalos_radiolog.txt");
    private static final File logcatRadioHasteKey = new File(BAIKALOS_ROOT, "baikalos_haste_logcat_radio_key");
    private static final File dmesgFile = new File(BAIKALOS_ROOT, "baikalos_dmesg.txt");
    private static final File dumpsysFile = new File(BAIKALOS_ROOT, "baikalos_dumpsys.txt");
    private static final File dmesgHasteKey = new File(BAIKALOS_ROOT, "baikalos_haste_dmesg_key");
    private static final File dumpsysHasteKey = new File(BAIKALOS_ROOT, "baikalos_haste_dumpsys_key");
    private static final File kmsgFile = new File(BAIKALOS_ROOT, "baikalos_kmsg.txt");
    private static final File kmsgHasteKey = new File(BAIKALOS_ROOT, "baikalos_haste_kmsg_key");
    private static final File shareZipFile = new File(BAIKALOS_ROOT, "baikalos_logs.zip");

    private static final int HASTE_MAX_LOG_SIZE = 40000000;

    private CheckBoxPreference mLogcat;
    private CheckBoxPreference mLogcatRadio;
    private CheckBoxPreference mKmsg;
    private CheckBoxPreference mDmesg;
    private CheckBoxPreference mDumpsys;
    private Preference mBaikalOSLogIt;
    private ListPreference mShareType;

    SwitchPreference mDEBUG_MASK_SENSORS;
    SwitchPreference mDEBUG_MASK_TORCH;
    SwitchPreference mDEBUG_MASK_TELEPHONY;
    SwitchPreference mDEBUG_MASK_TELEPHONY_RAW;
    SwitchPreference mDEBUG_MASK_BLUETOOTH;
    SwitchPreference mDEBUG_MASK_ACTIONS;
    SwitchPreference mDEBUG_MASK_APP_PROFILE;
    SwitchPreference mDEBUG_MASK_DEV_PROFILE;
    SwitchPreference mDEBUG_MASK_SERVICES;
    SwitchPreference mDEBUG_MASK_ACTIVITY;
    SwitchPreference mDEBUG_MASK_ALARM;
    SwitchPreference mDEBUG_MASK_BROADCAST;
    SwitchPreference mDEBUG_MASK_OOM;
    SwitchPreference mDEBUG_MASK_LOCATION;

    SwitchPreference mDEBUG_MASK_RAW;
    SwitchPreference mDEBUG_MASK_OOM_RAW;

    SwitchPreference mDEBUG_MASK_FREEZER;
    SwitchPreference mDEBUG_MASK_POWERHAL;

    SwitchPreference mDEBUG_MASK_POWER;
    SwitchPreference mDEBUG_MASK_JOBS;
    SwitchPreference mDEBUG_MASK_WAKELOCKS;
    SwitchPreference mDEBUG_MASK_IDLE;
    SwitchPreference mDEBUG_MASK_NETWORK;

    private String sharingIntentString;

    private boolean shareHaste = false;
    private boolean shareZip = true;
    private int mDebugMask = 0;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();

	    addPreferencesFromResource(R.xml.log_it);

        File baikalRoot = new File(Environment.getExternalStorageDirectory(),"BaikalOS");

        try {
            if( !baikalRoot.exists() ) {
                baikalRoot.mkdirs();
            }
            
        } catch(Exception fe) {
            Log.e(TAG, "onCreate: Can;t create baikalos root folder", fe );
        }

        mLogcat = (CheckBoxPreference) findPreference(PREF_LOGCAT);
        mLogcat.setOnPreferenceChangeListener(this);
        mLogcatRadio = (CheckBoxPreference) findPreference(PREF_LOGCAT_RADIO);
        mLogcatRadio.setOnPreferenceChangeListener(this);
        mKmsg = (CheckBoxPreference) findPreference(PREF_KMSG);
        mKmsg.setOnPreferenceChangeListener(this);
        mDmesg = (CheckBoxPreference) findPreference(PREF_DMESG);
        mDmesg.setOnPreferenceChangeListener(this);
        mDumpsys = (CheckBoxPreference) findPreference(PREF_DUMPSYS);
        mDumpsys.setOnPreferenceChangeListener(this);
        mBaikalOSLogIt = findPreference(PREF_BAIKALOS_LOG_IT);
        mShareType = (ListPreference) findPreference(PREF_SHARE_TYPE);
        mShareType.setOnPreferenceChangeListener(this);

        try {
            String debugMaskString = Settings.Global.getString(mContext.getContentResolver(), Settings.Global.BAIKALOS_DEBUG_MASK);
            if( debugMaskString == null || "".equals(debugMaskString) ) {
                debugMaskString = "0";
            }
            mDebugMask = Integer.parseInt(debugMaskString,16);
 
            mDEBUG_MASK_SENSORS=initDebugPref("BAIKAL_DEBUG_SENSORS", BaikalConstants.DEBUG_MASK_SENSORS);
            mDEBUG_MASK_TORCH=initDebugPref("BAIKAL_DEBUG_TORCH", BaikalConstants.DEBUG_MASK_TORCH);
            mDEBUG_MASK_TELEPHONY=initDebugPref("BAIKAL_DEBUG_TELEPHONY", BaikalConstants.DEBUG_MASK_TELEPHONY);
            mDEBUG_MASK_TELEPHONY_RAW=initDebugPref("BAIKAL_DEBUG_TELEPHONY_RAW", BaikalConstants.DEBUG_MASK_TELEPHONY_RAW);
            mDEBUG_MASK_BLUETOOTH=initDebugPref("BAIKAL_DEBUG_BLUETOOTH", BaikalConstants.DEBUG_MASK_BLUETOOTH);
            mDEBUG_MASK_ACTIONS=initDebugPref("BAIKAL_DEBUG_ACTIONS", BaikalConstants.DEBUG_MASK_ACTIONS);
            mDEBUG_MASK_APP_PROFILE=initDebugPref("BAIKAL_DEBUG_APP_PROFILE", BaikalConstants.DEBUG_MASK_APP_PROFILE);
            mDEBUG_MASK_DEV_PROFILE=initDebugPref("BAIKAL_DEBUG_DEV_PROFILE", BaikalConstants.DEBUG_MASK_DEV_PROFILE);
            mDEBUG_MASK_SERVICES=initDebugPref("BAIKAL_DEBUG_SERVICES", BaikalConstants.DEBUG_MASK_SERVICES);
            mDEBUG_MASK_ACTIVITY=initDebugPref("BAIKAL_DEBUG_ACTIVITY", BaikalConstants.DEBUG_MASK_ACTIVITY);
            mDEBUG_MASK_ALARM=initDebugPref("BAIKAL_DEBUG_ALARM", BaikalConstants.DEBUG_MASK_ALARM);
            mDEBUG_MASK_BROADCAST=initDebugPref("BAIKAL_DEBUG_BROADCAST", BaikalConstants.DEBUG_MASK_BROADCAST);
            mDEBUG_MASK_OOM=initDebugPref("BAIKAL_DEBUG_OOM", BaikalConstants.DEBUG_MASK_OOM);
            mDEBUG_MASK_LOCATION=initDebugPref("BAIKAL_DEBUG_LOCATION", BaikalConstants.DEBUG_MASK_LOCATION);
            mDEBUG_MASK_RAW=initDebugPref("BAIKAL_DEBUG_RAW", BaikalConstants.DEBUG_MASK_RAW);
            mDEBUG_MASK_OOM_RAW=initDebugPref("BAIKAL_DEBUG_OOM_RAW", BaikalConstants.DEBUG_MASK_OOM_RAW);
            mDEBUG_MASK_FREEZER=initDebugPref("BAIKAL_DEBUG_FREEZER", BaikalConstants.DEBUG_MASK_FREEZER);
            mDEBUG_MASK_POWERHAL=initDebugPref("BAIKAL_DEBUG_POWERHAL", BaikalConstants.DEBUG_MASK_POWERHAL);
            mDEBUG_MASK_POWER=initDebugPref("BAIKAL_DEBUG_POWER", BaikalConstants.DEBUG_MASK_POWER);
            mDEBUG_MASK_JOBS=initDebugPref("BAIKAL_DEBUG_JOBS", BaikalConstants.DEBUG_MASK_JOBS);
            mDEBUG_MASK_WAKELOCKS=initDebugPref("BAIKAL_DEBUG_WAKELOCKS", BaikalConstants.DEBUG_MASK_WAKELOCKS);
            mDEBUG_MASK_IDLE=initDebugPref("BAIKAL_DEBUG_IDLE", BaikalConstants.DEBUG_MASK_IDLE);
            mDEBUG_MASK_NETWORK=initDebugPref("BAIKAL_DEBUG_NETWORK", BaikalConstants.DEBUG_MASK_NETWORK);
        } catch(Exception re) {
            Log.e(TAG, "onCreate: Fatal! exception", re );
        }

        resetValues();
    }


    private SwitchPreference initDebugPref( String name, int mask) {
        SwitchPreference mLogPref = (SwitchPreference) findPreference( name.toLowerCase() );
        if( mLogPref != null ) { 
            mLogPref.setChecked( (mDebugMask & mask) != 0 );
            mLogPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        ((SwitchPreference)preference).setChecked((Boolean) newValue);
                        if( (Boolean) newValue ) mDebugMask |= mask;
                        else mDebugMask &= ~mask;
                        Log.e(TAG, "set debug_mask="+ String.format("%X",mDebugMask));
                        Settings.Global.putString(mContext.getContentResolver(), Settings.Global.BAIKALOS_DEBUG_MASK, String.format("%X",mDebugMask));
                    } catch(Exception re) {
                        Log.e(TAG, "onCreate: mLogPref Fatal! exception", re );
                    }
                    return true;
                }
            });
        } else {
            Log.e(TAG, "onCreate: mLogPref null for " + name + "!");
        }
        return mLogPref;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLogcat
            || preference == mLogcatRadio
            || preference == mKmsg
            || preference == mDmesg 
            || preference == mDumpsys) {
            updateEnabledState((CheckBoxPreference) preference, (Boolean) newValue);
            return true;
        } else if (preference == mShareType) {
            if ("0".equals(newValue)) {
                mShareType.setSummary(getString(R.string.log_it_share_type_haste));
                shareHaste = true;
                shareZip = false;
            } else if ("1".equals(newValue)) {
                mShareType.setSummary(getString(R.string.log_it_share_type_zip));
                shareHaste = false;
                shareZip = true;
            } else {
                mShareType.setSummary("");
                shareHaste = false;
                shareZip = false;
            }
            return true;
        } else {
            return false;
        }
    }

    protected void updateEnabledState(CheckBoxPreference changedPref, boolean newValue) {
        final CheckBoxPreference logSelectors[] = {mLogcat, mLogcatRadio, mKmsg, mDmesg, mDumpsys,};
        boolean enabled = newValue;
        // Enabled if any checkbox is checked
        if (!enabled) {
            for (CheckBoxPreference pref: logSelectors) {
                if (pref == changedPref) {
                    // Checked status not up-to-date, we use newValue for that
                    continue;
                }
                if (pref.isChecked()) {
                    enabled = true;
                    break;
                }
            }
        }
        mBaikalOSLogIt.setEnabled(enabled);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mBaikalOSLogIt) {
            new CreateLogTask().execute(mLogcat.isChecked(), mLogcatRadio.isChecked(),
                    mKmsg.isChecked(), mDmesg.isChecked(), mDumpsys.isChecked());
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    public void logItDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.log_it_dialog_title);
        builder.setMessage(R.string.logcat_warning);
        builder.setPositiveButton(R.string.share_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.log_it_share_subject);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingIntentString);
                startActivity(Intent.createChooser(sharingIntent,
                        getString(R.string.log_it_share_via)));
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void logZipDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.log_it_dialog_title);
        builder.setMessage(R.string.logcat_warning);
        builder.setPositiveButton(R.string.share_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("application/zip");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.log_it_share_subject);
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareZipFile));
                try {
                    StrictMode.disableDeathOnFileUriExposure();
                    startActivity(Intent.createChooser(sharingIntent,
                            getString(R.string.log_it_share_via)));
                } finally {
                    StrictMode.enableDeathOnFileUriExposure();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void makeLogcat() throws SuShell.SuDeniedException, IOException {

        String rmCommand = "rm " + LOGCAT_FILE;
        SuShell.runWithShellCheck(rmCommand);

        String command = "logcat -d -b all";
        if (shareHaste) {
            command += " | tail -c " + HASTE_MAX_LOG_SIZE + " > " + LOGCAT_FILE
                    + "&& curl -s -X POST -T " + LOGCAT_FILE + " " + BAIKALOS_HASTE
                    + " | cut -d'\"' -f4 | echo \"https://hastebin.com/$(cat -)\" > "
                            + HASTE_LOGCAT_KEY;
        } else {
            command += " > " + LOGCAT_FILE;
        }
        SuShell.runWithShellCheck(command);
    }

    public void makeLogcatLast() throws SuShell.SuDeniedException, IOException {

        String rmCommand = "rm " + LOGCAT_LAST_FILE;
        SuShell.runWithShellCheck(rmCommand);

        String command = "logcat -d -b all -L";
        if (shareHaste) {
            command += " | tail -c " + HASTE_MAX_LOG_SIZE + " > " + LOGCAT_LAST_FILE
                    + "&& curl -s -X POST -T " + LOGCAT_LAST_FILE + " " + BAIKALOS_HASTE
                    + " | cut -d'\"' -f4 | echo \"https://hastebin.com/$(cat -)\" > "
                            + HASTE_LOGCAT_LAST_KEY;
        } else {
            command += " > " + LOGCAT_LAST_FILE;
        }
        SuShell.runWithShellCheck(command);
    }

    public void makeLogcatRadio() throws SuShell.SuDeniedException, IOException {

        String rmCommand = "rm " + LOGCAT_RADIO_FILE;
        SuShell.runWithShellCheck(rmCommand);

        String command = "logcat -d -b radio";
        if (shareHaste) {
            command += " | tail -c " + HASTE_MAX_LOG_SIZE + " > " + LOGCAT_RADIO_FILE
                    + "&& curl -s -X POST -T " + LOGCAT_RADIO_FILE + " " + BAIKALOS_HASTE
                    + " | cut -d'\"' -f4 | echo \"https://hastebin.com/$(cat -)\" > "
                            + HASTE_LOGCAT_RADIO_KEY;
        } else {
            command += " > " + LOGCAT_RADIO_FILE;
        }
        SuShell.runWithShellCheck(command);
    }

    public void makeKmsg() throws SuShell.SuDeniedException, IOException {

        String rmCommand = "rm " + KMSG_FILE;
        SuShell.runWithShellCheck(rmCommand);

        //String command = "test -e /proc/last_kmsg && cat /proc/last_kmsg  || cat /sys/fs/pstore/console-ramoops*";

        String command = "cat /sys/fs/pstore/console-ramoops*";
        if (shareHaste) {
            command += " | tail -c " + HASTE_MAX_LOG_SIZE + " > " + KMSG_FILE
                    + " && curl -s -X POST -T " + KMSG_FILE + " " + BAIKALOS_HASTE
                    + " | cut -d'\"' -f4 | echo \"https://hastebin.com/$(cat -)\" > "
                            + HASTE_KMSG_KEY;
        } else {
            command += " > " + KMSG_FILE;
        }
        SuShell.runWithShellCheck(command);
    }

    public void makeDmesg() throws SuShell.SuDeniedException, IOException {

        String rmCommand = "rm " + DMESG_FILE;
        SuShell.runWithShellCheck(rmCommand);

        String command = "dmesg -T";
        if (shareHaste) {
            command += " | tail -c " + HASTE_MAX_LOG_SIZE + " > " + DMESG_FILE
                    + "&& curl -s -X POST -T " + DMESG_FILE + " " + BAIKALOS_HASTE
                    + " | cut -d'\"' -f4 | echo \"https://hastebin.com/$(cat -)\" > "
                            + HASTE_DMESG_KEY;
        } else {
            command += " > " + DMESG_FILE;
        }
        SuShell.runWithShellCheck(command);
    }

    public void makeDumpsys() throws SuShell.SuDeniedException, IOException {

        if( shareHaste ) return; // we do not support dumpsys on haste

        String rmCommand = "rm " + DUMPSYS_FILE;
        SuShell.runWithShellCheck(rmCommand);

        String command = "dumpsys power";
        command += " > " + DUMPSYS_FILE;
        SuShell.runWithShellCheck(command);

        command = "dumpsys suspend_control_internal -a";
        command += " >> " + DUMPSYS_FILE;
        SuShell.runWithShellCheck(command);

        command = "dumpsys alarm";
        command += " >> " + DUMPSYS_FILE;
        SuShell.runWithShellCheck(command);

        command = "dumpsys battery";
        command += " >> " + DUMPSYS_FILE;
        SuShell.runWithShellCheck(command);

        command = "dumpsys batterystats";
        command += " >> " + DUMPSYS_FILE;
        SuShell.runWithShellCheck(command);

    }

    private void createShareZip(boolean logcat, boolean logcatRadio, boolean kmsg, boolean dmesg, boolean dumpsys)
                                throws IOException {

        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(shareZipFile.getAbsolutePath())));
            if (logcat) {
                writeToZip(logcatFile, out);
            }
            if (logcatRadio) {
                writeToZip(logcatRadioFile, out);
            }
            if (dmesg) {
                writeToZip(dmesgFile, out);
            }
            if (logcat) {
                writeToZip(logcatLastFile, out);
            }
            if (kmsg) {
                writeToZip(kmsgFile, out);
            }
            if (dumpsys) {
                writeToZip(dumpsysFile, out);
            }
        } finally {
            if (out != null) out.close();
        }
    }
    private void writeToZip(File file, ZipOutputStream out) throws IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            if (in != null) in.close();
        }
    }

    private class CreateLogTask extends AsyncTask<Boolean, Void, String> {

        private Exception mException = null;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.log_it_logs_in_progress));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(Boolean... params) {
            String sharingIntentString = "";
            if (params.length != 5) {
                Log.e(TAG, "CreateLogTask: invalid argument count");
                return sharingIntentString;
            }
            try {
                if (params[0]) {
                    makeLogcat();
                    if (shareHaste) {
                        sharingIntentString += "\nLogcat: " +
                                Util.readStringFromFile(logcatHasteKey);
                    }
                    makeLogcatLast();
                    if (shareHaste) {
                        sharingIntentString += "\nLogcatLast: " +
                                Util.readStringFromFile(logcatLastHasteKey);
                    }
                }
                if (params[1]) {
                    makeLogcatRadio();
                    if (shareHaste) {
                        sharingIntentString += "\nRadio log: " +
                                Util.readStringFromFile(logcatRadioHasteKey);
                    }
                }
                if (params[2]) {
                    makeKmsg();
                    if (shareHaste) {
                        sharingIntentString += "\nKmsg: " +
                                Util.readStringFromFile(kmsgHasteKey);
                    }
                }
                if (params[3]) {
                    makeDmesg();
                    if (shareHaste) {
                        sharingIntentString += "\nDmesg: " +
                                Util.readStringFromFile(dmesgHasteKey);
                    }
                }
                if (params[4]) {
                    makeDumpsys();
                }
                if (shareZip) {
                    createShareZip(params[0], params[1], params[2], params[3], params[4]);
                }
            } catch (SuShell.SuDeniedException e) {
                mException = e;
            } catch (IOException e) {
                e.printStackTrace();
                mException = e;
            }
            return sharingIntentString;
        }

        @Override
        protected void onPostExecute(String param) {
            super.onPostExecute(param);
            progressDialog.dismiss();
            if (shareHaste && param != null && param.length() > 1) {
                sharingIntentString = param.substring(1);
                logItDialog();
            }
            if (shareZip) {
                logZipDialog();
            }
        }
    }

    public void resetValues() {
        mLogcat.setChecked(false);
        mLogcatRadio.setChecked(false);
        mKmsg.setChecked(false);
        mDmesg.setChecked(false);
        mDumpsys.setChecked(false);
        mBaikalOSLogIt.setEnabled(false);
        mShareType.setValue("1");
        mShareType.setSummary(mShareType.getEntry());
        shareHaste = false;
        shareZip = true;

    }


    public static void reset(Context mContext) {
        
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.log_it) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };

}
