package com.example.bramd.ambeelight;

//scans the network for ip addresses where the port used to connect to the AmBeeLight
//is open and echos this


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class networkScan extends AppCompatActivity {
    private Button scanBtn;
    public String ipAddress;
    public ArrayList<String> addresses;
    private ProgressBar progressBar;
    private ProgressBar progressBar_Bottom;


    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
    RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_scan);
        setupActionBar();

        mRecyclerView = findViewById(R.id.networkRecycler);
        addresses = new ArrayList<>();

        mLayoutManager=new LinearLayoutManager(this);
        mAdapter = new MainAdapter(addresses);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        progressBar = this.findViewById(R.id.progressBar);
        progressBar_Bottom = this.findViewById(R.id.progressBottom);
        progressBar_Bottom.setMax(254);
        //progressBar_Bottom.setBackgroundColor();

        //button to scan the network
        scanBtn = this.findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                ipAddress =Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                new portCheck().execute(ipAddress);
            }
        });
    }

    Boolean wifiCheck(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.i("NETWORKINFO","wifi.isConnected = " + mWifi.isConnected());
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
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    //user clicked on one of the found ip addresses
    public void onIpClick(View v){
        String ip = ((TextView)v).getText().toString();
        Log.i("Scanner","IP adress clicked!");
        Log.i("Scanner",ip);
        MessageSender messageSender = new MessageSender();
        //writeFile(((TextView) v).getText().toString());  //moved to confirm dialog
        senderParams.targetIp=ip;                                                                   //write green blink to ambeelight
        senderParams.colordata= new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0xff, (byte) 0x00,(byte) 0x53, (byte) 0xff};
        messageSender.execute();
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
            @Override
            public void onClick(View v) {
                Log.i("Dialog","YesBtn clicked!");
                alertDialog.dismiss();
                writeFile(((TextView) ip).getText().toString()); //confirm and write address to memory
                MessageSender messageSender = new MessageSender();
                senderParams.colordata= new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00, (byte) 0x00};
                messageSender.execute();
                RGBcontrol.showSnackbar("AmBeeLight found!",Snackbar.LENGTH_SHORT);
                //finishing this activity will redirect to it's parent, in this case the settings page
                finish();

            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Dialog","noBtn clicked!");
                alertDialog.dismiss();
                MessageSender messageSender = new MessageSender();
                senderParams.colordata= new byte[]{(byte) 0x43, (byte) 0x00, (byte) 0x00, (byte) 0x00};
                messageSender.execute();
            }
        });
    }

    //checks if wifi connection is available and if so, cycles through all possible supIp addresses
    //to find those where the ambeelight port is open
    @SuppressLint("StaticFieldLeak")
    class portCheck extends AsyncTask<String, Integer, Void> {  //TODO: dit weer static maken, zie n++

        //checks for wifi connectivity and, if connected, clears the addresses list
        protected void onPreExecute(){      //clear current addresses in order to avoid duplicates or false info
            if(!wifiCheck()){
                cancel(true);
                //progressBar.setVisibility(View.INVISIBLE);
                Log.w("NetworkScan","No WiFi connection, portscan canceled");
                Snackbar.make(scanBtn,"Please connect to wifi!",Snackbar.LENGTH_LONG)
                        .setAction("Error",null)
                        .show();
            }
            else {
                progressBar.setVisibility(View.VISIBLE);
                addresses.clear();
                Log.i("PortPing","Started scanning for AmBeeLights");
            }
        }

        protected Void doInBackground(String...hostAddress){     //check all available ip addresses for open port 55056
            if(isCancelled()){return null;}
            String ipBase = hostAddress[0].substring(0,12);
            Log.i("NetworkScan","Hostaddress = "+hostAddress[0]);
            Log.i("NetworkScan","IpBase = "+ipBase);
            for(int i=0;i<255;i++) {
                progressBar_Bottom.setProgress(i);
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ipBase+i, 55056), 30);
                    socket.close();
                    Log.i("NetworkScan", "Success! connected to "+ipBase+i);
                    addresses.add(ipBase+i);

                } catch (Exception ignored) {}
            }
            return null;
        }

        protected void onPostExecute(Void result){
            Log.i("NetworkScan","Async done!");
            Log.i("NetworkScan", String.valueOf(addresses));
            mAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
            //Log.i("NetworkScan","progressbar = "+progressBar_Bottom.getProgress());

            if(addresses.size()==0){             //no ip addresses entered into the list, no AmBeeLights were found on the network
                Snackbar.make(scanBtn,"No AmBeeLights found!",Snackbar.LENGTH_LONG)
                    .setAction("Error",null)
                    .show();
            }

        }
    }

        //write data to the config file
        public void writeFile (String data){
            FileOutputStream outputStream;
            String filename = "targetIp_file";
            try {
                outputStream = openFileOutput(filename, MODE_PRIVATE);
                outputStream.write(data.getBytes());
                outputStream.close();
                Log.i("File","New IP written to file: "+ data);
            } catch (Exception e) {
                //e.printStackTrace();
                Log.i("File", "No new file could be written");
            }
        }
//
//
//        public String readFile() {
//            try{
//                FileInputStream fileInputStream = openFileInput(filename);
//                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
//
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                StringBuilder stringBuffer = new StringBuilder();
//
//                String line;
//                while ((line = bufferedReader.readLine())!=null){
//                    stringBuffer.append(line).append("\n");
//                }
//                return stringBuffer.toString();
//
//
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
}


