package com.example.bramd.ambeelight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.SocketHandler;

public class RGBcontrol extends AppCompatActivity {
//    private static View anchor;
    private SeekBar SBred;
    private SeekBar SBgreen;
    private SeekBar SBblue;
    private TextView RText;
    private TextView GText;
    private TextView BText;
    private ImageView colorShow;


    SharedPreferences prefs; //todo shared prefs from menu


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgbcontrol);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RText = findViewById(R.id.RText);
        GText = findViewById(R.id.GText);
        BText = findViewById(R.id.BText);
        SBred = findViewById(R.id.SBred);
        SBgreen = findViewById(R.id.SBgreen);
        SBblue = findViewById(R.id.SBblue);
        colorShow = findViewById(R.id.colorShow);

//        anchor = this.findViewById(R.id.anchor); //must be declared before getIP() is called!!!
        SBred.setMax(255);
        SBgreen.setMax(255);
        SBblue.setMax(255);

        //seekbar red color
        SBred.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RText.setText("Red: " + progress);
                colorShow.setBackgroundColor(Color.rgb(progress, SBgreen.getProgress(), SBblue.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //seekbar green color
        SBgreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                GText.setText("Green: " + progress);
                colorShow.setBackgroundColor(Color.rgb(SBred.getProgress(), progress, SBblue.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //seekbar blue color
        SBblue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                BText.setText("Blue: " + progress);
                colorShow.setBackgroundColor(Color.rgb(SBred.getProgress(), SBgreen.getProgress(), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void offBtnClicked(View v) {
        SBred.setProgress(0);
        SBblue.setProgress(0);
        SBgreen.setProgress(0);
        sendCurrentSBValues(v);
    }

    public void sendCurrentSBValues(View v) {
        Toast.makeText(this, "sending....", Toast.LENGTH_SHORT);
        byte[] message = {(byte) 0x43, (byte) SBred.getProgress(), (byte) SBgreen.getProgress(), (byte) SBblue.getProgress()};

        if (wifiCheck()) {
            BasicSender sender = new BasicSender(this);
            sender.execute(message);
            Log.i("testBasicSender", "wifi check successful");
        } else {
            Snackbar.make(v, "Please connect to wifi!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }


    private boolean wifiCheck() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

//    public static void showSnackbar(String message, int length) {
//        Snackbar.make(anchor, message, length)
//                .setAction("action", null)
//                .show();
//    }


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
        } else if (id == R.id.information) {
            Log.i("Menu", "Information selected");
            Intent intent = new Intent(RGBcontrol.this, infoActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    void getIp() {
        Log.i("PING!", "getIP PING");
        try {
            FileInputStream fileInputStream = openFileInput("targetIp_file");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuffer = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            Log.i("FILE", "targetIp = " + stringBuffer.toString());
            senderParams.targetIp = stringBuffer.toString();
            Log.i("FILE", "targetIp = " + senderParams.targetIp);
        } catch (FileNotFoundException e) {
            Log.e("FILE", "FILE NOT FOUND");
            senderParams.targetIp = "192.168.1.0";   //fallback IP to prevent error
//            showSnackbar("Please go to settings first!", Snackbar.LENGTH_INDEFINITE);
        } catch (Exception e) {
            Log.e("FILE", "no valid ip loaded from memory");
            senderParams.targetIp = "192.168.1.0";   //fallback IP to prevent error
//            showSnackbar("Something went wrong, please tell Bram!", Snackbar.LENGTH_INDEFINITE);
        }
    }

    private void setIpAddress(String ipAddress) {
        SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ipAddress", ipAddress);
        editor.commit();
    }

    private static class BasicSender extends AsyncTask<byte[], Integer, Boolean> {
        private WeakReference<RGBcontrol> activityWeakReference;
        private String ipAddress = "";
        private Socket s = new Socket();

        BasicSender(RGBcontrol activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() { // get ip from shared pref
            super.onPreExecute();

            RGBcontrol activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            SharedPreferences prefs = activity.getSharedPreferences("data", MODE_PRIVATE);
            ipAddress = prefs.getString("ipAddress", "192.168.4.1");
        }

        @Override
        protected Boolean doInBackground(byte[]... bytes) {
            byte[] message = bytes[0];
            Log.i("Basic Sender", "Target ip = " + ipAddress);
            Log.i("Basic sender", "Message = " + Arrays.toString(message));
            try {
                s.connect(new InetSocketAddress(ipAddress, 55056), 30);
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                dos.write(message);
                Log.i("Basic sender", "Successfully send to AmBeeLight!");
            } catch (IOException e) {
//                e.printStackTrace();
                Log.i("Basic sender", "Could not send to AmBeeLight!");
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            RGBcontrol activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            RGBcontrol activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            if (bool) {
                Toast.makeText(activity, "Send to ambeelight", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Could not send :( ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

