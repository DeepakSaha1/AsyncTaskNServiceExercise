package com.example.asynctasknserviceexercise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetStateReceiver extends BroadcastReceiver {
    static INetStateChange iNetStateChange;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            //checks connectivity whether true or false
            Toast.makeText(context, "Connected to the network..", Toast.LENGTH_SHORT).show();
            iNetStateChange.mOnNetStateChangeListener(true);
        } else {
            //when network connectivity fails, it calls our network state listener and tells it that network connection is failed
            Toast.makeText(context, "Internet is disconnected.", Toast.LENGTH_SHORT).show();
            iNetStateChange.mOnNetStateChangeListener(false);
        }
    }


    public static void setConnectivityListener(NetStateReceiver.INetStateChange iNetStateChangeOb) {
        //this method is used to instantiate connectivity listener
        iNetStateChange = iNetStateChangeOb;
    }

    //using interface to communicate
    public interface INetStateChange {
        //customized method to listen network connectivity events
        void mOnNetStateChangeListener(boolean isConnected);
    }


}
