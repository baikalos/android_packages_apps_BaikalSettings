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
public class UserProfileAppListFragment extends AppListPreferenceFragmentBase {

    @Override
    protected boolean isShowSystem() {
        return true;
    }

    @Override
    protected int getPreferenceXmlResId() {
        // XML should contain a PreferenceCategory with key "all_apps_category"
        return R.xml.baikalos_app_all;
    }

    @Override
    protected String getCategoryKey() {
        return "app_all_category";
    }

    @Override
    protected int getTitleStringId() {
        return R.string.app_user_profile_title;
    }

    @Override
    protected int getCategoryTitleStringId() {
        return R.string.app_user_profile_category_title;
    }


    @Override
    protected boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile) {
        // English: Include everything for this specific list
        return profile != null && !profile.isDefault();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
