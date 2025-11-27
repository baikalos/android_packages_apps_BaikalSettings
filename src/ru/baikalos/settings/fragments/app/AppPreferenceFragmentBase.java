/*
 * Copyright (C) 2016-2023 crDroid Android Project
 * Copyright (C) 2026 BaikalOS
 */
package ru.baikalos.settings.fragments.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;


import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.SettingsPreferenceFragment;


import android.baikalos.IBaikalService;
import android.baikalos.BaikalAppProfile;
import android.baikalos.IBaikalAppProfileService;

import ru.baikalos.settings.fragments.BaikalAppProfileFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Keep;

@Keep
public abstract class AppPreferenceFragmentBase extends SettingsPreferenceFragment {

    public boolean isSystemHiddenApp(int uid, String packageName) {
        if( uid < 10000 ) return true;
        if( packageName == null ) return false;
        if( packageName.contains("auto_generated_rro_product__") ) return true;
        if( packageName.startsWith("android") ) return true;
        if( packageName.startsWith("com.android.carrierconfig") ) return true;
        if( packageName.startsWith("com.android.certinstaller") ) return true;
        if( packageName.startsWith("com.android.connectivity") ) return true;

        if( packageName.startsWith("com.android.credentialmanager") ) return true;
        if( packageName.startsWith("com.android.documentsui") ) return true;
        if( packageName.startsWith("com.android.dialer") ) return true;
        if( packageName.startsWith("com.android.dreams") ) return true;
        if( packageName.startsWith("com.android.emergency") ) return true;
        if( packageName.startsWith("com.android.externalstorage") ) return true;

        if( packageName.startsWith("com.android.frameworks") ) return true;
        if( packageName.startsWith("com.android.health") ) return true;
        if( packageName.startsWith("com.android.imsserviceentitlement") ) return true;
        if( packageName.startsWith("com.android.inputmethod") ) return true;
        if( packageName.startsWith("com.android.intentresolver") ) return true;
        if( packageName.startsWith("com.android.internal") ) return true;

        if( packageName.startsWith("com.android.keychain") ) return true;
        if( packageName.startsWith("com.android.launcher3") ) return true;
        if( packageName.startsWith("com.android.modulemetadata") ) return true;
        if( packageName.startsWith("com.android.mtp") ) return true;
        if( packageName.startsWith("com.android.networkstack") ) return true;
        if( packageName.startsWith("com.android.packageinstaller") ) return true;

        if( packageName.startsWith("com.android.permissioncontroller") ) return true;
        if( packageName.startsWith("com.android.phone") ) return true;
        if( packageName.startsWith("com.android.providers") ) return true;
        if( packageName.startsWith("com.android.proxyhandler") ) return true;
        if( packageName.startsWith("com.android.role") ) return true;
        if( packageName.startsWith("com.android.settings") ) return true;

        if( packageName.startsWith("com.android.shell") ) return true;
        if( packageName.startsWith("com.android.storagemanager") ) return true;
        if( packageName.startsWith("com.android.system") ) return true;
        if( packageName.startsWith("com.android.theme") ) return true;
        if( packageName.startsWith("com.android.webview") ) return true;

        if( packageName.startsWith("com.android.wifi") ) return true;
        if( packageName.startsWith("com.qualcomm") ) return true;
        if( packageName.startsWith("lineageos.platform") ) return true;
        if( packageName.startsWith("vendor.qti") ) return true;

        return false;
    }
}
