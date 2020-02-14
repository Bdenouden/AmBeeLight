package com.example.bramd.ambeelight;

import android.content.SharedPreferences;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class infoActivity extends AppCompatActivity {

    private AppCompatImageView loader;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;

    TextView cp;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_info);
        TextView errorField = findViewById(R.id.info_last_error_field);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        errorField.setText(prefs.getString("info_last_exception", "No exceptions."));

        loader = findViewById(R.id.ambeeLoading);
        showLoader();

        cp = findViewById(R.id.copyright);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showLoader() {
        Drawable drawable = loader.getDrawable();
        Log.i("Loader","here I am!");
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                avd2.registerAnimationCallback(new Animatable2.AnimationCallback() {
                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        super.onAnimationEnd(drawable);
                        avd2.start();
                    }
                });
            }
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setTitle("Information");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ambeeLoadingClicked(View v) {
        loader.setImageResource(R.drawable.ambee_done);
        showLoader();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            avd2.clearAnimationCallbacks();
        }

        cp.setVisibility(View.VISIBLE);
    }
}
