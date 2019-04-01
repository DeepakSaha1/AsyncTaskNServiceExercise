package com.example.asynctasknserviceexercise;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

public class ShowImageUsingServiceActivity
        extends AppCompatActivity implements NetStateReceiver.INetStateChange {

    ImageView mImageView;
    String mDownloadUrl;
    ProgressDialog mProgressDialog;
    NetStateReceiver mNetStateReceiver;      //reference variable to instantiate broadcast receiver

    private BroadcastReceiver mGetLocalBroadcastResult;
    private BroadcastReceiver mGetGetLocalBroadcastProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_using_service);
        mImageView = findViewById(R.id.imageview_service);

        //setting network connectivity listener and
        //initializing our broadcast receiver to keep track on network status changes
        NetStateReceiver.setConnectivityListener(this);
        mNetStateReceiver = new NetStateReceiver();

        //initializing progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setMessage("Downloading image.. Press back to cancel download.");
        mProgressDialog.setCancelable(true);     //allows user to dismiss progressbar using back button
        mProgressDialog.setCanceledOnTouchOutside(false);    //prevents cancellation when user clicks outside progressbar window

        //obtaining download url from received intent
        mDownloadUrl = getIntent().getStringExtra("mDownloadUrl");

        mGetLocalBroadcastResult = new BroadcastReceiver() {
            //local receiver to set image when it is received
            @Override
            public void onReceive(Context context, Intent intent) {
                //when result obtained is of resultant bitmap type
                Bitmap kBitmap = intent.getParcelableExtra("imageBitmap");
                if (kBitmap == null)
                    Log.d("download-result", "Bitmap is null");
                mImageView.setImageBitmap(kBitmap);
            }
        };

        mGetGetLocalBroadcastProgress = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //when result obtained is of progressbar's progress type
                int progress = intent.getIntExtra("progress", 0);
                //  Log.d("download-progress", String.valueOf(progress));
                mProgressDialog.setProgress(progress);
                if (progress >= 100 && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        //binding our broadcast receiver dynamically
        this.registerReceiver(mNetStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mGetLocalBroadcastResult, new IntentFilter("downloadResult"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mGetGetLocalBroadcastProgress, new IntentFilter("downloadProgress"));

    }

    @Override
    protected void onStop() {
        super.onStop();
        //unbinding our broadcast receiver dynamically
        this.unregisterReceiver(mNetStateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGetGetLocalBroadcastProgress);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGetLocalBroadcastResult);
    }

    @Override
    protected void onDestroy() {
        //deleting file as soon as the user leaves the activity
        super.onDestroy();
        File kFile = new File(Environment.getExternalStorageDirectory(), Constant.FILE_NAME_SERVICE);

        boolean deletedFile2 = false;
        if (kFile.exists())
            deletedFile2 = kFile.delete();

        Log.d("MainActivity", "if deletedFile2 ?" + deletedFile2);
    }

    @Override
    public void mOnNetStateChangeListener(boolean isConnected) {
        if (isConnected) {
            Constant.stoppedService = false;
            //starting service
            Intent intent = new Intent(this, DownloadFileService.class);
            intent.putExtra("mDownloadUrl", mDownloadUrl);
            startService(intent);
            mProgressDialog.show();
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //It cancels the intent service using a thread safe variable in the service class
                    Constant.stoppedService = true;      //informing service to stop its execution
                }
            });
        } else {
            Constant.stoppedService = true;    //informing service to stop its execution
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }
}
