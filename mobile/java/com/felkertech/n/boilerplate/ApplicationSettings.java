package com.felkertech.n.weatherdelta.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;
import com.felkertech.n.Utils.SettingsManager;
import com.felkertech.n.weatherdelta.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ApplicationSettings extends AppCompatActivity {
    public static final int EDIT_TEXT = 2;
    public static final int EDIT_TEXT_PREF = 21;
    public static final int LIST_PREF = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_settings);
        findViewById(R.id.pref_fragment).setBackgroundColor(getResources().getColor(android.R.color.background_light));
        /*if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SettingsFragment())
                    .commit();
        }*/
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

//            getView().setBackgroundColor(getResources().getColor(android.R.color.background_light));
            bindSummary(R.string.TEMP_LOW_THRESHOLD, EDIT_TEXT_PREF);
            bindSummary(R.string.TEMP_HIGH_THRESHOLD, EDIT_TEXT_PREF);
            bindSummary(R.string.HUMIDITY_LOW, EDIT_TEXT_PREF);
            bindSummary(R.string.HUMIDITY_HIGH, EDIT_TEXT_PREF);
            bindSummary(R.string.OZONE_THRESHOLD, EDIT_TEXT_PREF);
            bindSummary(R.string.sm_sync_interval, LIST_PREF);

            enablePreference(R.string.sm_sync_interval, R.string.IS_PREMIUM);
            enablePreference(R.string.sm_sync_wifi, R.string.IS_PREMIUM);
	    try {
                bindAbout(R.string.action_about);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        public void bindSummary(final int preference_key, final int preference_type) {
            final SettingsManager sm = new SettingsManager(getActivity());
            final String TAG = "weather:AppSettings";
            switch(preference_type) {
                case EDIT_TEXT:
                    final MaterialEditTextPreference materialPreference = (MaterialEditTextPreference) findPreference(getString(preference_key));
                    materialPreference.setSummary(sm.getString(preference_key));
//                    Log.d(TAG, getString(preference_key));
                    materialPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            sm.setString(preference_key, (String) newValue);
                            materialPreference.setSummary(sm.getString(preference_key));
                            Log.d(TAG, "New value "+newValue);
                            return false;
                        }
                    });
                    break;
                case EDIT_TEXT_PREF:
                    final com.jenzz.materialpreference.Preference p = (com.jenzz.materialpreference.Preference) findPreference(getString(preference_key));
                    p.setSummary(sm.getString(preference_key));
                    p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            new MaterialDialog.Builder(getActivity())
                                    .title(p.getTitle())
                                    .content("")
                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                    .input("", sm.getString(preference_key), new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(MaterialDialog dialog, CharSequence input) {
                                            // Do something
                                            sm.setString(preference_key, input.toString());
//                                    materialPreference.setSummary(sm.getString(preference_key));
                                            p.setSummary(sm.getString(preference_key));
                                            Log.d(TAG, "New value " + input);
                                        }
                                    }).show();
                            return false;
                        }
                    });
                    break;
                case LIST_PREF:
                    final com.jenzz.materialpreference.Preference lp = (com.jenzz.materialpreference.Preference) findPreference(getString(preference_key));
                    lp.setSummary(getResources().getStringArray(R.array.pref_sync_intervals)[sm.getInt(preference_key)]);
                    lp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            new MaterialDialog.Builder(getActivity())
                                    .title(lp.getTitle())
                                    .items(R.array.pref_sync_intervals)
                                    .alwaysCallSingleChoiceCallback()
                                    .itemsCallbackSingleChoice(sm.getInt(preference_key), new MaterialDialog.ListCallbackSingleChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                            sm.setInt(R.string.sm_sync_interval, i);
                                            lp.setSummary(getResources().getStringArray(R.array.pref_sync_intervals)[sm.getInt(preference_key)]);
                                            return false;
                                        }
                                    })
                                    .show();

                            return false;
                        }
                    });
                    break;
            }
        }
        public void enablePreference(int resId, int boolId) {
            final SettingsManager sm = new SettingsManager(getActivity());
            final com.jenzz.materialpreference.Preference lp = (com.jenzz.materialpreference.Preference) findPreference(getString(resId));
            lp.setEnabled(sm.getBoolean(boolId));
        }
public void bindAbout(int resId) throws PackageManager.NameNotFoundException {
            final com.jenzz.materialpreference.Preference p =
                    (com.jenzz.materialpreference.Preference) findPreference(getString(resId));
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String v = "Version "+pInfo.versionName;
            String b = "Build "+pInfo.versionCode;
            final int[] taps = {0};
            final SettingsManager sm = new SettingsManager(getActivity());
            final boolean developerEnabled = sm.getBoolean(R.string.SM_DEVELOPER);
            p.setSummary(v+"\n"+b);
            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    taps[0]++;
                    if(taps[0] > 7 && !developerEnabled) {
                        Toast.makeText(getActivity(), "Debug mode enabled", Toast.LENGTH_SHORT).show();
                        sm.setBoolean(R.string.SM_DEVELOPER, true);
                        onCreate(null);
                    } else if(taps[0] > 3 && !developerEnabled) {
                        Toast.makeText(getActivity(), "You are "+(8-taps[0])+" steps away from debug mode", Toast.LENGTH_SHORT).show();
                    } else if(developerEnabled)
                        Toast.makeText(getActivity(), "Debug mode already enabled", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
    }
}
