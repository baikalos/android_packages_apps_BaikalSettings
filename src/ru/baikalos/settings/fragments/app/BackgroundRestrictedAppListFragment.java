/*
 * Copyright (C) 2026 BaikalOS
 */
package ru.baikalos.settings.fragments.app;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.baikalos.BaikalAppProfile;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import androidx.annotation.Keep;

@Keep
public class BackgroundRestrictedAppListFragment extends AppListPreferenceFragmentBase {

    @Override
    protected boolean isShowSystem() {
        return true;
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
        return R.string.app_background_restricted_title;
    }

    @Override
    protected int getCategoryTitleStringId() {
        return R.string.app_background_restricted_category_title;
    }

    @Override
    protected boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile) {
        boolean restricted = false;
        if( profile == null ) return false;
        if( (profile.mBackgroundMode & BaikalAppProfile.BAIKAL_BACKGROUND_BOOT_DISABLED) != 0 ) restricted = true;
        if( !restricted && (profile.mBackgroundMode & BaikalAppProfile.BAIKAL_BACKGROUND_DONOTWAKE) != 0 ) restricted = true;
        if( !restricted && profile.mBackgroundLevel > 0 ) restricted = true;
        return restricted;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
