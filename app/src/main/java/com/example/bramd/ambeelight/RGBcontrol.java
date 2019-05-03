package com.example.bramd.ambeelight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import static android.support.v4.content.ContextCompat.getSystemService;

public class RGBcontrol extends AppCompatActivity {
    private static View anchor;
    private SeekBar SBred;
    private SeekBar SBgreen;
    private SeekBar SBblue;
    private TextView RText;
    private TextView GText;
    private TextView BText;
    private ImageView colorShow;


    SharedPreferences prefs; //todo rave en stobo seekbar toevoegen


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgbcontrol);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //todo implement this---
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        Log.i("preferences", String.valueOf(prefs.getBoolean("raveToggle",false )));
        //----

        FloatingActionButton fab = findViewById(R.id.fab);

        RText = findViewById(R.id.RText);
        GText = findViewById(R.id.GText);
        BText = findViewById(R.id.BText);
        SBred = findViewById(R.id.SBred);
        SBgreen = findViewById(R.id.SBgreen);
        SBblue  = findViewById(R.id.SBblue);
        colorShow = findViewById(R.id.colorShow);
        Button offBtn = findViewById(R.id.offBtn);

        anchor = this.findViewById(R.id.anchor); //must be declared before getIP() is called!!!
        getIp();

        SBred.setMax(255);
        SBgreen.setMax(255);
        SBblue.setMax(255);

//        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        final NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        Log.i("NETWORKINFO","wifi.isConnected = " + mWifi.isConnected());
        wifiCheck();    //TODO toggle voor wificheck invoegen

        //off button
        offBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SBred.setProgress(0);
                SBgreen.setProgress(0);
                SBblue.setProgress(0);
                MessageSender messageSender = new MessageSender();
                byte [] message = {(byte)0x43, (byte)SBred.getProgress(), (byte)SBgreen.getProgress(), (byte)SBblue.getProgress()};
                getIp(); //problem?
                senderParams.colordata = message;

                //wifi check and sending data
                if(wifiCheck()){
                    messageSender.execute();
                    Snackbar.make(v,"Data send to AmBeeLight!" , Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {//and me
                    Snackbar.make(v,"Please connect to wifi!" , Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }//and me
            }
        });


        //seekbar red color
        SBred.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RText.setText("Red: " + progress);
                colorShow.setBackgroundColor(Color.rgb( progress, SBgreen.getProgress() ,SBblue.getProgress() ));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //seekbar green color
        SBgreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                GText.setText("Green: "+ progress);
                colorShow.setBackgroundColor(Color.rgb( SBred.getProgress(), progress ,SBblue.getProgress() ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //seekbar blue color
        SBblue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                BText.setText("Blue: " + progress);
                colorShow.setBackgroundColor(Color.rgb( SBred.getProgress(), SBgreen.getProgress() ,progress ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageSender messageSender = new MessageSender();
                byte [] message = {(byte)0x43, (byte)SBred.getProgress(), (byte)SBgreen.getProgress(), (byte)SBblue.getProgress()};
                getIp();
                senderParams.colordata = message;
                if(wifiCheck()){
                    messageSender.execute();
                    Snackbar.make(view,"Data send to AmBeeLight!" , Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    Snackbar.make(view,"Please connect to wifi!" , Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    Boolean wifiCheck(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.i("NETWORKINFO","wifi.isConnected = " + mWifi.isConnected());
        return mWifi.isConnected();
    }

    public static void showSnackbar(String message, int length) {
        Snackbar.make(anchor,message, length)
                .setAction("action", null)
                .show();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rgbcontrol, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(RGBcontrol.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id==R.id.information){
            Log.i("Menu","Information selected");
            Intent intent = new Intent(RGBcontrol.this, infoActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    void getIp() {
        Log.i("PING!","getIP PING");
        try {
            FileInputStream fileInputStream = openFileInput("targetIp_file");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuffer = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            Log.i("FILE","targetIp = "+stringBuffer.toString());
            senderParams.targetIp = stringBuffer.toString();
            Log.i("FILE","targetIp = "+senderParams.targetIp);
        }
        catch (FileNotFoundException e){
            Log.e("FILE", "FILE NOT FOUND");
            senderParams.targetIp = "192.168.1.0";   //fallback IP to prevent error
            showSnackbar("Please go to settings first!",Snackbar.LENGTH_INDEFINITE);
        }
        catch (Exception e) {
            Log.e("FILE", "no valid ip loaded from memory");
            senderParams.targetIp = "192.168.1.0";   //fallback IP to prevent error
            showSnackbar("Something went wrong, please tell Bram!",Snackbar.LENGTH_INDEFINITE);
        }
    }
}

class senderParams {
    static byte [] colordata;
    static String targetIp = "192.168.1.0"; //fallback IP address

    senderParams(String target, byte [] colorDataIn){
        colordata=colorDataIn;
        targetIp=target;
    }
}
