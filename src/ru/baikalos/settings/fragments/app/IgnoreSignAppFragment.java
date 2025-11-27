package ru.baikalos.settings.fragments.app;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.baikalos.BaikalAppProfile;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;

import androidx.annotation.Keep;

@Keep
public class IgnoreSignAppFragment extends AppSwitchPreferenceFragmentBase {

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
        return R.string.app_ignore_sign_title;
    }

    @Override
    protected int getCategoryTitleStringId() {
        return R.string.app_ignore_sign_category_title;
    }
    

    @Override
    protected boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile) {
        return profile != null && (profile.mOverride & BaikalAppProfile.BAIKAL_OVERRIDE_SIGNATURE) != 0;
    }

    @Override
    protected boolean isChecked(ApplicationInfo info, BaikalAppProfile profile) {
        return profile != null && (profile.mOverride & BaikalAppProfile.BAIKAL_OVERRIDE_SIGNATURE) != 0;
    }

    @Override
    protected boolean onCheckChanged(ApplicationInfo info, BaikalAppProfile profile, boolean newValue) {
        int oldMode = profile.mOverride;
        
        if (!newValue) {
            profile.mOverride &= ~BaikalAppProfile.BAIKAL_OVERRIDE_SIGNATURE;
        } else {
            profile.mOverride |= BaikalAppProfile.BAIKAL_OVERRIDE_SIGNATURE;
        }

        // English comment: Return true only if the mode actually changed
        return oldMode != profile.mOverride;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
