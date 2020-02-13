package com.example.bramd.ambeelight;

//scans the network for ip addresses where the port used to connect to the AmBeeLight
//is open and echos this


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class NetworkScan extends AppCompatActivity {
    public static final String IP_ADDRESS = "ipAddress";
    public static final String IP_ADDRESS_DEFAULT = "192.168.4.1";

    public String myIpAddress;
    public static ArrayList<String> addresses;
    private ProgressBar progressBar;
    private ProgressBar progressBar_Bottom;

    private ImageView checkMark;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;

    SharedPreferences prefs;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
    RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_network_scan);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        checkMark = findViewById(R.id.cm_scandone);
        mRecyclerView = findViewById(R.id.networkRecycler);
        addresses = new ArrayList<>();

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MainAdapter(addresses);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        progressBar = this.findViewById(R.id.progressBar);
        progressBar_Bottom = this.findViewById(R.id.progressBottom);
        progressBar_Bottom.setMax(254);
        //progressBar_Bottom.setBackgroundColor();

//        //button to scan the network
        View scanBtn = this.findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBtnClicked(v);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showCheckMark() {
        Drawable drawable = checkMark.getDrawable();

        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }
    }

    private void fadeOutAndHideImage(final ImageView img) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(1000);
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.INVISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeOut);
    }

    private Boolean wifiCheck() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.i("NETWORK INFO", "wifi.isConnected = " + mWifi.isConnected());
        return mWifi.isConnected();
    }

    //adds the actionbar back button and title
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Network scanner");
        }
    }

    //corrects the animation for the actionbar back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    //user clicked on one of the found ip addresses
    public void onIpClick(View v) {
        String selectedIp = ((TextView) v).getText().toString();
        Log.i("Scanner", "IP adress clicked!");
        Log.i("Scanner", selectedIp);

        byte[] message = new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x53, (byte) 0xff};
        new BasicSender(selectedIp).execute(message);
        showDialog(v);
    }

    //shows the confirm dialog to ensure the ambeelight is selected
    void showDialog(final View ip) {
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.alertdialog, null);
        final AlertDialog alertDialog;


        Button yesBtn = view.findViewById(R.id.adBtnYes);
        Button noBtn = view.findViewById(R.id.adBtnNo);

        alertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();
        alertDialog.show();

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View v) {
                Log.i("Dialog", "YesBtn clicked!");
                alertDialog.dismiss();
                SharedPreferences.Editor editor = prefs.edit();
                String newTargetIp = ((TextView) ip).getText().toString();
                editor.putString(IP_ADDRESS, newTargetIp);
                editor.commit();

                byte[] message = new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00, (byte) 0x00};
                new BasicSender(newTargetIp).execute(message);

                //finishing this activity will redirect to it's parent, in this case the settings page
                finish();
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Dialog", "noBtn clicked!");
                String newTargetIp = ((TextView) ip).getText().toString();
                byte[] message = new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00, (byte) 0x00};
                new BasicSender(newTargetIp).execute(message);
                alertDialog.dismiss();
            }
        });
    }



    public void scanBtnClicked(View v) {
        Log.i("ScanBtnClicked", "The 'Scan' button has been clicked!");
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wm.getConnectionInfo();
        myIpAddress = Formatter.formatIpAddress(wifiInfo.getIpAddress());
        String ipBase = myIpAddress.substring(0, myIpAddress.lastIndexOf(".") + 1);
        PortCheck portCheck = new PortCheck(this);
        portCheck.execute(ipBase);
        Log.i("Network Info", "My IP address is: " + myIpAddress);
    }

    //checks if wifi connection is available and if so, cycles through all possible supIp addresses
    //to find those where the ambeelight port is open
    private static class ProgressVariables {
        private Integer index;
        private String foundIp = null;
    }

    private static class PortCheck extends AsyncTask<String, ProgressVariables, Void> {
        private WeakReference<NetworkScan> activityWeakReference;

        PortCheck(NetworkScan activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        //checks for wifi connectivity and, if connected, clears the addresses list
        protected void onPreExecute() {      //clear current addresses in order to avoid duplicates or false info

            NetworkScan activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            if (!activity.wifiCheck()) {
                cancel(true);
                //progressBar.setVisibility(View.INVISIBLE);
                Log.w("PortCheck", "No WiFi connection, PortCheck canceled");
                Toast.makeText(activity, "Please connect to wifi!", Toast.LENGTH_SHORT).show();
            } else {
                activity.progressBar.setVisibility(View.VISIBLE);
                addresses.clear();
                Log.i("PortCheck", "Started scanning for AmBeeLights");
            }
        }

        // todo split across 2 threads, increase timeout
        protected Void doInBackground(String... ipBase) {     //check all available ip addresses for open port 55056
            if (isCancelled()) {
                return null;
            }
            for (int i = 0; i < 255; i++) {
                ProgressVariables progVar = new ProgressVariables();
                progVar.index = i;
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(ipBase[0] + i, 55056), 100);
                    socket.close();
                    String targetIp = ipBase[0] + i;
                    addresses.add(targetIp);
                    progVar.foundIp = targetIp;
                } catch (Exception ignored) {
                    try {
                        socket.close();
                    } catch (IOException e) {
//                        e.printStackTrace();
                    }
                }

                publishProgress(progVar);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ProgressVariables... progVars) {
            super.onProgressUpdate(progVars);
            ProgressVariables progVar = progVars[0];

            NetworkScan activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.progressBar_Bottom.setProgress(progVar.index);
            if (progVar.foundIp != null) {
                Log.i("NetworkScan", "Success! connected to " + progVar.foundIp);
//                activity.mAdapter.notifyDataSetChanged(); // todo port is blocked during sending, confirmation has to wait until the scan is finished
            }
        }

        //        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        protected void onPostExecute(Void result) {
            NetworkScan activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            Log.i("PortCheck", "PortCheck done!\nResults:\n" + addresses);
//            Log.i("PortCheck", String.valueOf(addresses));
            activity.mAdapter.notifyDataSetChanged();
            activity.progressBar.setVisibility(View.INVISIBLE);
            activity.checkMark.setVisibility(View.VISIBLE);
            activity.showCheckMark();
            activity.fadeOutAndHideImage(activity.checkMark);

            if (addresses.size() == 0) {             //no ip addresses entered into the list, no AmBeeLights were found on the network
                Toast.makeText(activity, "No AmBeeLights found!", Toast.LENGTH_LONG).show();
            }
        }
    }



    private static class BasicSender extends AsyncTask<byte[], Void, Void> {
        private String ipAddress ;
        private Socket s = new Socket();

        BasicSender(String targetIp) {
            ipAddress = targetIp.replaceAll(" ", "");
        }

        @Override
        protected Void doInBackground(byte[]... bytes) {
            byte[] message = bytes[0];
            Log.i("Basic Sender", "Target ip = " + ipAddress);
            Log.i("Basic sender", "Message = " + Arrays.toString(message));
            try {
                s.connect(new InetSocketAddress(ipAddress, 55056), 200);
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                dos.write(message);
                s.close();
                Log.i("Basic sender", "Successfully send to AmBeeLight!");
            } catch (IOException e) {
//                e.printStackTrace();
                Log.i("Basic sender", "Could not send to AmBeeLight!");
            }
            return null;
        }
    }
}


