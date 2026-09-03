/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.custom.preference;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;

import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceViewHolder;

import com.android.settingslib.PrimarySwitchPreference;

public class SystemSettingPrimarySwitchPreference extends PrimarySwitchPreference {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ContentObserver mSettingObserver;
    private boolean mDefaultValue = false;

    public SystemSettingPrimarySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SystemSettingPrimarySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SystemSettingPrimarySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SystemSettingPrimarySwitchPreference(Context context) {
        super(context, null);
        init();
    }

    private void init() {
        setPreferenceDataStore(new DataStore());
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue instanceof Boolean) {
            mDefaultValue = (Boolean) defaultValue;
        }
        updateState();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        updateState();
        super.onBindViewHolder(holder);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        if (getKey() != null) {
            if (mSettingObserver == null) {
                mSettingObserver = new ContentObserver(mHandler) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        updateState();
                    }
                };
            }
            getContext().getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(getKey()), false, mSettingObserver);
            updateState();
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if (mSettingObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mSettingObserver);
            mSettingObserver = null;
        }
    }

    private void updateState() {
        if (getKey() == null) return;
        boolean isChecked = Settings.System.getInt(
                getContext().getContentResolver(), getKey(), mDefaultValue ? 1 : 0) != 0;
        setChecked(isChecked);
    }

    private class DataStore extends PreferenceDataStore {
        @Override
        public void putBoolean(String key, boolean value) {
            Settings.System.putInt(getContext().getContentResolver(), key, value ? 1 : 0);
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return Settings.System.getInt(getContext().getContentResolver(), key,
                    defaultValue ? 1 : 0) != 0;
        }
    }
}
