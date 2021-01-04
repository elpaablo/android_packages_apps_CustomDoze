/*
 * Copyright (C) 2015 The CyanogenMod Project
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

package com.custom.ambient.display;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceFragment;
import android.view.MenuItem;

public class DozeSettings extends PreferenceActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, getNewFragment())
                    .commit();
        }
    }

    private PreferenceFragment getNewFragment() {
        return new MainSettingsFragment();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment,
            Preference preference) {
        Fragment instantiate = Fragment.instantiate(this, preference.getFragment(),
            preference.getExtras());
        getFragmentManager().beginTransaction().replace(
                android.R.id.content, instantiate).addToBackStack(preference.getKey()).commit();

        return true;
    }

    public static class MainSettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        private Context mContext;
        private ActionBar actionBar;

        private SwitchPreference mAoDPreference;
        private SwitchPreference mAmbientDisplayPreference;
        private SwitchPreference mPickUpPreference;
        private SwitchPreference mHandwavePreference;
        private SwitchPreference mPocketPreference;
        private SwitchPreference mDozeOnChargePreference;
        private SwitchPreference mPulseOnNewTracks;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            setPreferencesFromResource(R.xml.doze_settings, rootKey);

            mContext = getActivity();

            actionBar = getActivity().getActionBar();
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);

            mAoDPreference =
                (SwitchPreference) findPreference(Utils.AOD_KEY);

            mDozeOnChargePreference =
                (SwitchPreference) findPreference(Utils.AOD_CHARGE_KEY);

            if (Utils.isAoDAvailable(mContext)) {
                mAoDPreference.setChecked(Utils.isAoDEnabled(mContext));
                mAoDPreference.setOnPreferenceChangeListener(this);
                mDozeOnChargePreference.setChecked(Utils.isDozeOnChargeEnabled(mContext));
                mDozeOnChargePreference.setOnPreferenceChangeListener(this);
            } else {
                mDozeOnChargePreference.setVisible(false);
                mAoDPreference.setVisible(false);
            }

            mPulseOnNewTracks =
                    (SwitchPreference) findPreference(Utils.PULSE_ON_NEW_TRACKS);
            mPulseOnNewTracks.setChecked(Utils.isPulseOnNewTracksEnabled(mContext));
            mPulseOnNewTracks.setOnPreferenceChangeListener(this);

            mAmbientDisplayPreference =
                (SwitchPreference) findPreference(Utils.AMBIENT_DISPLAY_KEY);
            mAmbientDisplayPreference.setChecked(Utils.isDozeEnabled(mContext));
            mAmbientDisplayPreference.setOnPreferenceChangeListener(this);

            mPickUpPreference =
                (SwitchPreference) findPreference(Utils.PICK_UP_KEY);
            mPickUpPreference.setChecked(Utils.tiltGestureEnabled(mContext));
            mPickUpPreference.setOnPreferenceChangeListener(this);

            mHandwavePreference =
                (SwitchPreference) findPreference(Utils.GESTURE_HAND_WAVE_KEY);
            mHandwavePreference.setChecked(Utils.handwaveGestureEnabled(mContext));
            mHandwavePreference.setOnPreferenceChangeListener(this);

            mPocketPreference =
                (SwitchPreference) findPreference(Utils.GESTURE_POCKET_KEY);
            mPocketPreference.setChecked(Utils.pocketGestureEnabled(mContext));
            mPocketPreference.setOnPreferenceChangeListener(this);

            if (!getResources().getBoolean(R.bool.has_tilt_sensor)) {
                mPickUpPreference.setVisible(false);
            }

            if (!getResources().getBoolean(R.bool.has_proximity_sensor)) {
                mHandwavePreference.setVisible(false);
                mPocketPreference.setVisible(false);
            }

            if (mAoDPreference == null) return;
            setPrefs();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();

            if (Utils.AOD_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mAoDPreference.setChecked(value);
                Utils.enableAoD(value, mContext);
                setPrefs();
                return true;
            } else if (Utils.AMBIENT_DISPLAY_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mAmbientDisplayPreference.setChecked(value);
                Utils.enableDoze(value, mContext);
                return true;
            } else if (Utils.PICK_UP_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mPickUpPreference.setChecked(value);
                Utils.enablePickUp(value, mContext);
                return true;
            } else if (Utils.GESTURE_HAND_WAVE_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mHandwavePreference.setChecked(value);
                Utils.enableHandWave(value, mContext);
                return true;
            } else if (Utils.GESTURE_POCKET_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mPocketPreference.setChecked(value);
                Utils.enablePocketMode(value, mContext);
                return true;
            } else if (Utils.AOD_CHARGE_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mDozeOnChargePreference.setChecked(value);
                Utils.enableDozeOnCharge(value, mContext);
                return true;
            } else if (Utils.PULSE_ON_NEW_TRACKS.equals(key)) {
                boolean value = (Boolean) newValue;
                Utils.enablePulseOnNewTracks(value, mContext);
                return true;
            }
            return false;
        }

        private void setPrefs() {
            final boolean aodEnabled = Utils.isAoDEnabled(mContext);
            mAmbientDisplayPreference.setEnabled(!aodEnabled);
            mPickUpPreference.setEnabled(!aodEnabled);
            mHandwavePreference.setEnabled(!aodEnabled);
            mPocketPreference.setEnabled(!aodEnabled);
            mDozeOnChargePreference.setEnabled(!aodEnabled);
            mPulseOnNewTracks.setEnabled(!aodEnabled);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
