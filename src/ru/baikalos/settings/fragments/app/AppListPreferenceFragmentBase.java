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
public abstract class AppListPreferenceFragmentBase extends AppPreferenceFragmentBase
        implements SearchView.OnQueryTextListener {

    protected final String TAG = getClass().getSimpleName();
    
    private static final String STATE_SYSTEM = "state_system";
    private static final int MENU_SYSTEM = Menu.FIRST;

    protected IBaikalService mBaikalService;
    protected IBaikalAppProfileService mAppProfileService;
    protected PackageManager mPm;

    protected boolean mShowSystem = false;
    protected String mSearchQuery = "";

    // --- Abstract methods for customization ---
    protected abstract int getPreferenceXmlResId();
    protected abstract String getCategoryKey();
    protected abstract int getTitleStringId();
    protected abstract int getCategoryTitleStringId();
    
    protected abstract boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if (savedInstanceState != null) {
        //    mShowSystem = savedInstanceState.getBoolean(STATE_SYSTEM, false);
        //}

        mShowSystem = isShowSystem();

        addPreferencesFromResource(getPreferenceXmlResId());

        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            screen.setTitle(getTitleStringId());
        }
        
        mPm = getContext().getPackageManager();
        mBaikalService = getContext().getBaikalContext().getBaikalService();
        mAppProfileService = getContext().getBaikalContext().getBaikalAppProfileService();

        rebuildAppList();
        setHasOptionsMenu(true);
    }

    protected boolean isShowSystem() {
        return false;
    }

    protected void onAppClicked(ApplicationInfo info, BaikalAppProfile profile) {
        // English: Launch BaikalAppProfileFragment on click
        final Bundle args = new Bundle();
        args.putString("package", info.packageName);
        args.putInt("uid", info.uid);

        new SubSettingLauncher(getContext())
                .setDestination(BaikalAppProfileFragment.class.getName())
                .setSourceMetricsCategory(getMetricsCategory())
                .setTitleText(info.loadLabel(mPm))
                .setArguments(args)
                .setUserHandle(new UserHandle(UserHandle.getUserId(info.uid)))
                .launch();
    }

    public void rebuildAppList() {
        PreferenceCategory category = getPreferenceScreen().findPreference(getCategoryKey());
        if (category != null) {
            category.removeAll();
            category.setTitle(getCategoryTitleStringId());
            new LoadAppsTask(category).execute();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SYSTEM, mShowSystem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem searchItem = menu.add(Menu.NONE, Menu.NONE, 0, R.string.app_menu_search);
        searchItem.setIcon(android.R.drawable.ic_menu_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        
        SearchView searchView = new SearchView(getContext());
        searchView.setOnQueryTextListener(this);
        searchItem.setActionView(searchView);

        MenuItem systemItem = menu.add(Menu.NONE, MENU_SYSTEM, 1, R.string.app_menu_filter_system);
        systemItem.setCheckable(true);
        systemItem.setChecked(mShowSystem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_SYSTEM) {
            mShowSystem = !mShowSystem;
            item.setChecked(mShowSystem);
            rebuildAppList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchQuery = newText != null ? newText.toLowerCase().trim() : "";
        rebuildAppList();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) { return false; }

    protected String getSummary(ApplicationInfo info) {
        return info.packageName;
    }

    /**
     * Internal AsyncTask to prevent UI freeze during app loading
     */
    private class LoadAppsTask extends AsyncTask<Void, Void, List<ApplicationInfo>> {
        private final PreferenceCategory category;

        LoadAppsTask(PreferenceCategory cat) {
            this.category = cat;
        }

        @Override
        protected List<ApplicationInfo> doInBackground(Void... voids) {
            List<ApplicationInfo> result = new ArrayList<>();
            List<ApplicationInfo> all = mPm.getInstalledApplications(PackageManager.MATCH_ANY_USER);
            
            for (ApplicationInfo info : all) {
                try {

                    if( info.uid < 10000 ) continue;

                    // Check system filter
                    boolean isSystem = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    if (!mShowSystem && isSystem) continue;

                    if( isSystem && isSystemHiddenApp(info.uid, info.packageName) ) continue; 
                    
                    // Check installed filter
                    if ((info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) continue;

                    BaikalAppProfile p = mBaikalService.getBaikalAppProfileNotNull(info.uid);
                    
                    // Custom filter from subclass
                    if (shouldIncludeApp(info, p)) {
                        String label = info.loadLabel(mPm).toString();
                        if (TextUtils.isEmpty(mSearchQuery) || 
                            label.toLowerCase().contains(mSearchQuery) || 
                            info.packageName.toLowerCase().contains(mSearchQuery)) {
                            result.add(info);
                        }
                    }
                } catch (Exception e) {
                    // English: Service call failed
                }
            }
            
            Collections.sort(result, (a, b) -> 
                a.loadLabel(mPm).toString().compareToIgnoreCase(b.loadLabel(mPm).toString()));
            return result;
        }

        @Override
        protected void onPostExecute(List<ApplicationInfo> apps) {
            Context context = getPrefContext();
            if (context == null) context = getContext();

            for (ApplicationInfo info : apps) {
                Preference pref = new Preference(context);
                pref.setTitle(info.loadLabel(mPm));
                String summary = getSummary(info);
                pref.setSummary(summary);
                pref.setIcon(info.loadIcon(mPm));
                
                pref.setOnPreferenceClickListener(p -> {
                    try {
                        BaikalAppProfile prof = mBaikalService.getBaikalAppProfileNotNull(info.uid);
                        onAppClicked(info, prof);
                    } catch (Exception e) {
                        Log.e(TAG, "Error fetching profile on click", e);
                    }
                    return true;
                });
                category.addPreference(pref);
            }
        }
    }
}
