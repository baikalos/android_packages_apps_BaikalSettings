/*
 * Copyright (C) 2026 BaikalOS
 */
package ru.baikalos.settings.fragments.app;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.baikalos.BaikalAppProfile;
import android.baikalos.IBaikalService;


import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import androidx.annotation.Keep;

@Keep
public class BackgroundAppListFragment extends AppListPreferenceFragmentBase {

    private IBaikalService mService;

    @Override
    protected boolean isShowSystem() {
        return false;
    }

    @Override
    protected int getPreferenceXmlResId() {
        return R.xml.baikalos_app_all;
    }

    @Override
    protected String getCategoryKey() {
        return "app_all_category";
    }

    @Override
    protected int getTitleStringId() {
        return R.string.app_background_title;
    }

    @Override
    protected int getCategoryTitleStringId() {
        return R.string.app_background_category_title;
    }

    @Override
    protected boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile) {
        if( mService == null ) {
            mService = getContext().getBaikalContext().getBaikalService();
        }
        try {
            if( mService.getBackgroundStartCount(info.uid) > 0 ) return true;
        } catch(Exception e) {}
        return false;
    }

    @Override
    protected String getSummary(ApplicationInfo info) {
        try {
            return info.packageName + ", " + mService.getBackgroundStartCount(info.uid);
        } catch(Exception e) {}
        return info.packageName;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
