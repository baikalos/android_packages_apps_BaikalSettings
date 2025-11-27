/*
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
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;

import android.baikalos.IBaikalService;
import android.baikalos.BaikalAppProfile;
import android.baikalos.IBaikalAppProfileService;

import ru.baikalos.settings.fragments.BaikalAppProfileFragment;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Keep
public abstract class AppSwitchPreferenceFragmentBase extends SettingsPreferenceFragment 
        implements SearchView.OnQueryTextListener {

    protected final String TAG = getClass().getSimpleName();
    
    private static final String STATE_SYSTEM = "state_system";
    private static final int MENU_SYSTEM = Menu.FIRST;

    protected IBaikalService mBaikalService;
    protected IBaikalAppProfileService mAppProfileService;
    protected PackageManager mPm;

    protected boolean mShowSystem = false;
    protected String mSearchQuery = "";

    // --- Abstract methods for subclasses ---
    protected abstract int getPreferenceXmlResId();
    protected abstract String getCategoryKey();
    protected abstract int getTitleStringId();
    protected abstract int getCategoryTitleStringId();
    protected abstract boolean shouldIncludeApp(ApplicationInfo info, BaikalAppProfile profile);
    protected abstract boolean isChecked(ApplicationInfo info, BaikalAppProfile profile);
    protected abstract boolean onCheckChanged(ApplicationInfo info, BaikalAppProfile profile, boolean newValue);

    /**
     * Internal preference class to handle long clicks on the item view
     */
    private class BaikalAppSwitchPreference extends SwitchPreferenceCompat {
        private final AppEntry mEntry;

        public BaikalAppSwitchPreference(Context context, AppEntry entry) {
            super(context);
            mEntry = entry;
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);
            // English: Attach long click listener directly to the itemView
            holder.itemView.setOnLongClickListener(v -> {
                onAppLongClicked(mEntry.info, mEntry.profile);
                return true;
            });
        }
    }

    /**
     * Base implementation for long click. 
     * Launches the BaikalOS App Profile fragment.
     */
    protected void onAppLongClicked(ApplicationInfo info, BaikalAppProfile profile) {
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

    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppEntry>> {
        private final PreferenceCategory category;
        LoadAppsTask(PreferenceCategory cat) { this.category = cat; }

        @Override
        protected List<AppEntry> doInBackground(Void... voids) {
            List<AppEntry> result = new ArrayList<>();
            List<ApplicationInfo> all = mPm.getInstalledApplications(PackageManager.MATCH_ANY_USER);
            
            for (ApplicationInfo info : all) {
                try {
                    boolean isSystem = (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    if (!mShowSystem && isSystem) continue;
                    if ((info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) continue;

                    BaikalAppProfile p = mBaikalService.getBaikalAppProfileNotNull(info.uid);
                    
                    if (shouldIncludeApp(info, p)) {
                        String label = info.loadLabel(mPm).toString();
                        if (TextUtils.isEmpty(mSearchQuery) || 
                            label.toLowerCase().contains(mSearchQuery) || 
                            info.packageName.toLowerCase().contains(mSearchQuery)) {
                            result.add(new AppEntry(info, p, label));
                        }
                    }
                } catch (Exception e) {
                    // English: IPC failure or app uninstalled during scan
                }
            }
            Collections.sort(result, (a, b) -> a.label.compareToIgnoreCase(b.label));
            return result;
        }

        @Override
        protected void onPostExecute(List<AppEntry> entries) {
            final Context context = getPrefContext();
            for (AppEntry entry : entries) {
                // English: Using custom class to enable long click support
                BaikalAppSwitchPreference pref = new BaikalAppSwitchPreference(context, entry);
                pref.setTitle(entry.label);
                pref.setSummary(entry.info.packageName);
                pref.setIcon(entry.info.loadIcon(mPm));
                pref.setChecked(isChecked(entry.info, entry.profile));

                pref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean val = (Boolean) newValue;
                    if (onCheckChanged(entry.info, entry.profile, val)) {
                        try {
                            mAppProfileService.saveProfile(entry.profile);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to save profile via BaikalService", e);
                        }
                    }
                    return true;
                });

                category.addPreference(pref);
            }
        }
    }

    private static class AppEntry {
        ApplicationInfo info;
        BaikalAppProfile profile;
        String label;
        AppEntry(ApplicationInfo i, BaikalAppProfile p, String l) {
            info = i; profile = p; label = l;
        }
    }
}
