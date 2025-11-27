/*
 * Copyright (C) 2016-2022 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.baikalos.settings.fragments;

import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import android.os.Bundle;
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.android.settings.R;

public class PowerSaverProfileEditorActivity extends CollapsingToolbarBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_saver_profile_editor);

        int type = getIntent().getIntExtra("type",0);
        String name = getIntent().getStringExtra("name");
        String ps_name = getIntent().getStringExtra("ps_name");
        if( ps_name != null ) setTitle(ps_name);

        Log.d("Baikal", "Replacing fragment");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, PowerSaverProfileEditor.newInstance(type,name))
                //.addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Log.d("Baikal", "onBackPressed");
        if (getFragmentManager().getBackStackEntryCount() >= 1) {
            Log.d("Baikal", "popBackStack");
            getFragmentManager().popBackStack(); // return to previous fragment
        }
        else {
            Log.d("Baikal", "super.onBackPressed");
            super.onBackPressed(); // Exit application when no fragment is on the backstack
        }
    }
}
