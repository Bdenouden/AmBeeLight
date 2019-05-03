package com.example.bramd.ambeelight;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;



public class MessageSender extends AsyncTask<senderParams ,Void ,Void>
{

    protected Void doInBackground(senderParams... params){

        byte[] message = senderParams.colordata;
        String targetIp = senderParams.targetIp;
        Log.i("Message sender","Target ip = " + targetIp);
        //RGBcontrol.showSnackbar("Target ip = "+ targetIp,Snackbar.LENGTH_LONG);

        try
        {
            Socket s = new Socket(targetIp, 55056);
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.write(message);
            dos.close();
            dos.flush();
            Log.i("Message sender", "Succesfully send to AmBeeLight!");
        }catch (IOException e)
        {
            e.printStackTrace();
            Log.i("Message sender", "Could not send to AmBeeLight!");
            RGBcontrol.showSnackbar("Could not send to AmBeeLight!", Snackbar.LENGTH_LONG);
        }
        return null;
    }
}


