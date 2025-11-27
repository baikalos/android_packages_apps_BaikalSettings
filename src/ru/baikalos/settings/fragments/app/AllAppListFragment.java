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
public class AllAppListFragment extends AppListPreferenceFragmentBase {

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
        return R.string.app_all_title;
    }

    @Override
    protected int getCategoryTitleStringId() {
        return R.string.app_all_category_title;
    }

    @Override
    protected boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile) {
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
