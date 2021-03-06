/*
 * Copyright (C) 2016 The Xperia Open Source Project
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

package com.android.settings.xosp;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.CheckBoxPreference;

import android.os.UserHandle;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import android.widget.Toast;
import com.android.internal.view.RotationPolicy;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import static android.provider.Settings.Secure.CAMERA_GESTURE_DISABLED;
import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;
import static android.provider.Settings.Secure.DOZE_ENABLED;
import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.WallpaperManager;
import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceCategory;
import android.provider.SearchIndexableResource;
import com.android.settings.SettingsPreferenceFragment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;
import com.android.settings.Utils;
import cyanogenmod.providers.CMSettings;

public class MiscPersonalizations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener{
    
    private static final String KEY_TAP_TO_WAKE = "tap_to_wake";
    private static final String KEY_DTA_LOCK = "double_tap_sleep_anywhere";
    
    private SwitchPreference mTapToWakePreference;
    private SwitchPreference mDT2SAnywherePreference;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        addPreferencesFromResource(R.xml.xosp_misc_cat);
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = activity.getContentResolver();
        
        mTapToWakePreference = (SwitchPreference) findPreference(KEY_TAP_TO_WAKE);
            if (mTapToWakePreference != null) {
                if (isTapToWakeAvailable(getResources()))
                    mTapToWakePreference.setOnPreferenceChangeListener(this);
            }
        mDT2SAnywherePreference = (SwitchPreference) findPreference(KEY_DTA_LOCK);
        mDT2SAnywherePreference.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.DOUBLE_TAP_SLEEP_ANYWHERE, 0) == 1));
    }
    
    private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_supportDoubleTapWake);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }
    
    private void updateState() {
    
        // Update tap to wake if it is available.
        if (mTapToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, 0);
            mTapToWakePreference.setChecked(value != 0);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        final String key = preference.getKey();
        
        if (preference == mTapToWakePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, value ? 1 : 0);
        }
        return true;
    }
    
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mDT2SAnywherePreference) {
            boolean enabled = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DOUBLE_TAP_SLEEP_ANYWHERE, enabled ? 1:0);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
    
    protected int getMetricsCategory(){
        return MetricsEvent.APPLICATION;
    }
}
