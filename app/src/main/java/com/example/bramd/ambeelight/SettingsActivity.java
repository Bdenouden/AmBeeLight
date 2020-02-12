package com.example.bramd.ambeelight;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceManager;
import android.util.Log;


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
public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final String IP_ADDRESS = "ipAddress";
    public static final String IP_ADDRESS_DEFAULT = "192.168.4.1";

    EditTextPreference setIpTextPref;
    SharedPreferences prefs;

    SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.activity_settings);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myPrefListner = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (findPreference(key) instanceof EditTextPreference) {
                    EditTextPreference setIpTextPref = (EditTextPreference) findPreference(key);
                    setIpTextPref.setSummary(prefs.getString(key, "no value set"));
                }
                Log.i("Settings myPrefListener","triggered!");
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(myPrefListner);

        // set initial summary for ipAddress textpref
        setIpTextPref = (EditTextPreference) findPreference(IP_ADDRESS);
        setIpTextPref.setSummary(prefs.getString(IP_ADDRESS, IP_ADDRESS_DEFAULT));
    }


    @Override
    protected void onResume() {
        super.onResume();
        // re attach listener to editable fields
        Log.i("Settings","Resumed!");
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(myPrefListner);
        setIpTextPref.setSummary(prefs.getString(IP_ADDRESS, IP_ADDRESS_DEFAULT));
    }


    @Override
    protected void onPause() {
        super.onPause();
        // re attach listener to editable fields
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(myPrefListner);
        setIpTextPref.setSummary(prefs.getString(IP_ADDRESS, IP_ADDRESS_DEFAULT));

    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
