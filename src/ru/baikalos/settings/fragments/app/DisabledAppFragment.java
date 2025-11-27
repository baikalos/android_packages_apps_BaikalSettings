package ru.baikalos.settings.fragments.app;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.baikalos.BaikalAppProfile;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

import androidx.annotation.Keep;

@Keep
public class DisabledAppFragment extends AppSwitchPreferenceFragmentBase {

    private static final String TAG = "DisabledAppFragment";

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
        return R.string.app_disabled_title;
    }

    @Override
    protected int getCategoryTitleStringId() {
        return R.string.app_disabled_category_title;
    }


    @Override
    protected boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile) {
        if( profile == null ) return false;
        int state = getApplicationState(info.packageName);
        boolean enabled = state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        return !enabled;
    }

    @Override
    protected boolean isChecked(ApplicationInfo info, BaikalAppProfile profile) {
        if( profile == null ) return false;
        int state = getApplicationState(info.packageName);
        boolean enabled = state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        return !enabled;
    }

    @Override
    protected boolean onCheckChanged(ApplicationInfo info, BaikalAppProfile profile, boolean newValue) {
        if( profile == null ) return false;
        int current = getApplicationState(info.packageName);
        boolean enabled = current == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || current == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        if( (!enabled) != newValue ) {
            int state = newValue ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            Log.e(TAG, "mAppDisable: mPackageName=" + info.packageName + ",state=" + state);
            setApplicationState(info.packageName, state);
        }
        return false;
    }

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


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }


}
