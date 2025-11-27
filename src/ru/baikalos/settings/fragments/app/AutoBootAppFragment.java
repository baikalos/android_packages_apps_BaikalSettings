package ru.baikalos.settings.fragments.app;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.baikalos.BaikalAppProfile;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

import androidx.annotation.Keep;

@Keep
public class AutoBootAppFragment extends AppSwitchPreferenceFragmentBase {

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
        return R.string.app_autoboot_title;
    }

    @Override
    protected int getCategoryTitleStringId() {
        return R.string.app_autoboot_category_title;
    }

    @Override
    protected boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile) {
        return getContext().getPackageManager().checkPermission(
                Manifest.permission.RECEIVE_BOOT_COMPLETED, 
                info.packageName) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected boolean isChecked(ApplicationInfo info, BaikalAppProfile profile) {
        return profile != null && (profile.mBackgroundMode & BaikalAppProfile.BAIKAL_BACKGROUND_BOOT_DISABLED) == 0;
    }

    @Override
    protected boolean onCheckChanged(ApplicationInfo info, BaikalAppProfile profile, boolean newValue) {
        int oldMode = profile.mBackgroundMode;
        
        if (newValue) {
            profile.mBackgroundMode &= ~BaikalAppProfile.BAIKAL_BACKGROUND_BOOT_DISABLED;
        } else {
            profile.mBackgroundMode |= BaikalAppProfile.BAIKAL_BACKGROUND_BOOT_DISABLED;
        }

        // English comment: Return true only if the mode actually changed
        return oldMode != profile.mBackgroundMode;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
