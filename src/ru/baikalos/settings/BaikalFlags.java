/*
 * Copyright (C) 2019 BaikalOS
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

package ru.baikalos.settings;

import android.content.Context;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.system.Os;
import android.system.StructUtsname;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class BaikalFlags { 

    private static final String TAG = "BaikalFlags";

    private static final String DEV_ALL_OVERRIDE_PROPERTY = "persist.baikal.dev_all_override";
    private static final String DEV_VIS_OVERRIDE_PROPERTY = "persist.baikal.dev_vis_override";
    private static final String DEV_EN_OVERRIDE_PROPERTY = "persist.baikal.dev_en_override";
    private static final String DEV_KERN_OVERRIDE_PROPERTY = "persist.baikal.dev_kern_override";

    private Map<String, BaikalFlag> mFlags = new HashMap<>();
    private static boolean isKernelCompatible = true;
    private static boolean isAllOverride = SystemProperties.getBoolean(DEV_ALL_OVERRIDE_PROPERTY,false);
    private static boolean isVisOverride = SystemProperties.getBoolean(DEV_VIS_OVERRIDE_PROPERTY,false);
    private static boolean isEnOverride = SystemProperties.getBoolean(DEV_EN_OVERRIDE_PROPERTY,false);
    private static boolean isKernOverride = SystemProperties.getBoolean(DEV_KERN_OVERRIDE_PROPERTY,false);

    private static BaikalFlags mInstance;

    public class BaikalFlag {
        public String mKey;
        public String mName;
        public String mProperty;
        public boolean mVisible;
        public boolean mEnabled;
        public boolean mKernel;

        public BaikalFlag(String key, String name, String property, boolean visible, boolean enabled, boolean kernel) {
            mKey = key;
            mName = name;
            mProperty = property;
            mVisible = visible; 
            mEnabled = enabled;
            mKernel = kernel;
        }

        public boolean getVisibility(boolean visible) {
            if( isAllOverride || isVisOverride ) return true;
            if( !"".equals(mProperty) ) {
                int prop = SystemProperties.getInt(mProperty,0);
                if( prop == -1 ) return false;
                if( prop == 1 ) return true;
            }
            if( mKernel && !isKernelCompatible ) return false; 
            return mVisible & visible;
        }

        public boolean getEnabled(boolean enabled) {
            if( isAllOverride || isEnOverride ) return true;
            if( !"".equals(mProperty) ) {
                int prop = SystemProperties.getInt(mProperty,0);
                if( prop == -1 ) return false;
                if( prop == 1 ) return true;
            }
            return mEnabled & enabled;
        }
    }

    private BaikalFlags() {
        mInstance = this;
    }

    public static BaikalFlags Instance() {
        if( mInstance == null ) mInstance = new BaikalFlags();
        return mInstance;
    }

    public void add(BaikalFlag flag) {
        mFlags.put(flag.mKey,flag);
    }

    public BaikalFlag get(String key) {
        BaikalFlag flag = mFlags.get(key);
        if( flag != null ) return flag;
        flag = new BaikalFlag(key,key,"",false,false,true);
        Log.w(TAG, "Flag " + key + " not found!");
        return flag;
    }

    public void init() {
        add(new BaikalFlag("app_profile_old_links","app_profile_old_links","",false,true,false));
        add(new BaikalFlag("app_profile_cpu_performance_limit","app_profile_cpu_performance_limit","",false,false,false));
        add(new BaikalFlag("app_profile_heavy_cpu","app_profile_heavy_cpu","",false,true,false));

        add(new BaikalFlag("app_profile_debug","app_profile_debug","",true,true,false));
        add(new BaikalFlag("app_profile_performance","app_profile_performance","",true,true,false));
        add(new BaikalFlag("app_profile_thermal","app_profile_thermal","",true,true,false));
        add(new BaikalFlag("app_profile_boost_control","app_profile_boost_control","",true,true,false));
        add(new BaikalFlag("app_profile_bypass_charging","app_profile_bypass_charging","",true,true,false));
        add(new BaikalFlag("app_profile_spoof_default_dialer","app_profile_spoof_default_dialer","",true,true,false));
        add(new BaikalFlag("app_profile_spoof_default_sms","app_profile_spoof_default_sms","",true,true,false));
        add(new BaikalFlag("app_profile_pinned","app_profile_pinned","",true,true,false));
        add(new BaikalFlag("app_profile_donotclose","app_profile_donotclose","",true,true,false));
        add(new BaikalFlag("app_profile_disable_boot","app_profile_disable_boot","",true,true,false));

        add(new BaikalFlag("app_profile_disable_wakeup","app_profile_disable_wakeup","",false,true,false));

        add(new BaikalFlag("app_profile_heavy_memory","app_profile_heavy_memory","",true,true,false));
        add(new BaikalFlag("app_profile_background","app_profile_background","",true,true,false));
        add(new BaikalFlag("app_profile_spoof","app_profile_spoof","",true,true,false));

        add(new BaikalFlag("app_profile_phka","app_profile_phka","",false,true,false));

        add(new BaikalFlag("app_profile_devmode","app_profile_devmode","",true,true,false));
        add(new BaikalFlag("app_profile_filterfs","app_profile_filterfs","",true,true,false));
        add(new BaikalFlag("app_profile_filterfs_add","app_profile_filterfs_add","",true,true,false));
        add(new BaikalFlag("app_profile_allowwhileidle","app_profile_allowwhileidle","",true,true,false));
        add(new BaikalFlag("app_profile_idle_network","app_profile_idle_network","",true,true,false));
        add(new BaikalFlag("app_profile_block_overlays","app_profile_block_overlays","",true,true,false));
        add(new BaikalFlag("app_profile_brightness","app_profile_brightness","",true,true,false));
        add(new BaikalFlag("app_profile_rotation","app_profile_rotation","",true,true,false));
        add(new BaikalFlag("app_profile_darkmode","app_profile_darkmode","",true,true,false));
        add(new BaikalFlag("app_profile_max_fps","app_profile_max_fps","",true,true,false));
        add(new BaikalFlag("app_profile_min_fps","app_profile_min_fps","",true,true,false));
        add(new BaikalFlag("app_profile_keep_on","app_profile_keep_on","",true,true,false));

        add(new BaikalFlag("app_profile_override_fonts","app_profile_override_fonts","",false,true,false));

        add(new BaikalFlag("app_profile_forced_screenshot","app_profile_forced_screenshot","",true,true,false));
        add(new BaikalFlag("app_profile_installer","app_profile_installer","",true,true,false));
        add(new BaikalFlag("app_profile_location","app_profile_location","",true,true,false));

        add(new BaikalFlag("app_profile_file_access","app_profile_file_access","",false,true,false));

        add(new BaikalFlag("app_profile_allow_sig_override","app_profile_allow_sig_override","",true,true,false));

        add(new BaikalFlag("app_profile_block_focus_recv","app_profile_block_focus_recv","",false,true,false));
        add(new BaikalFlag("app_profile_block_focus_send","app_profile_block_focus_send","",false,true,false));
        add(new BaikalFlag("app_profile_force_sonification","app_profile_force_sonification","",false,true,false));

        add(new BaikalFlag("app_profile_camera","app_profile_camera","",true,true,false));

        add(new BaikalFlag("app_profile_microphone","app_profile_microphone","",false,true,false));

        add(new BaikalFlag("app_profile_hide_idle","app_profile_hide_idle","",true,true,false));
        add(new BaikalFlag("app_profile_hide_location","app_profile_hide_location","",true,true,false));
        add(new BaikalFlag("app_profile_hide_battery_opt","app_profile_hide_battery_opt","",true,true,false));
        add(new BaikalFlag("app_profile_hide_phone_opt","app_profile_hide_phone_opt","",false,true,false));
        add(new BaikalFlag("app_profile_hide_sms_opt","app_profile_hide_sms_opt","",false,true,false));

        add(new BaikalFlag("app_profile_hide_hms","app_profile_hide_hms","",true,true,false));
        add(new BaikalFlag("app_profile_hide_gms","app_profile_hide_gms","",true,true,false));
        add(new BaikalFlag("app_profile_hide_3p","app_profile_hide_3p","",true,true,false));
        add(new BaikalFlag("app_profile_block_notification","app_profile_block_notification","",true,true,false));
        add(new BaikalFlag("app_profile_block_sms","app_profile_block_sms","",true,true,false));
        add(new BaikalFlag("app_profile_block_contacts","app_profile_block_contacts","",true,true,false));
        add(new BaikalFlag("app_profile_block_calllog","app_profile_block_calllog","",true,true,false));
        add(new BaikalFlag("app_profile_block_calendar","app_profile_block_calendar","",true,true,false));
        add(new BaikalFlag("app_profile_block_media","app_profile_block_media","",true,true,false));

        add(new BaikalFlag("app_profile_priv_phone_state","app_profile_priv_phone_state","",false,true,false));

        add(new BaikalFlag("app_profile_spoof_sim_country","app_profile_spoof_sim_country","",true,true,false));
        add(new BaikalFlag("app_profile_spoof_sim_mnc","app_profile_spoof_sim_mnc","",true,true,false));
        add(new BaikalFlag("app_profile_spoof_sim_opname","app_profile_spoof_sim_opname","",true,true,false));
        add(new BaikalFlag("app_profile_spoof_sim_ln","app_profile_spoof_sim_ln","",true,true,false));
    }
}

