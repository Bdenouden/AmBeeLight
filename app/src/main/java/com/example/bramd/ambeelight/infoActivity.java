package com.example.bramd.ambeelight;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class infoActivity extends AppCompatActivity {

    private TextView errorField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_info);
        errorField = findViewById(R.id.info_last_error_field);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        errorField.setText(prefs.getString("info_last_exception", "No exceptions."));
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setTitle("Information");
        }
    }
}
