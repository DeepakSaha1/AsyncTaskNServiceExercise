package com.example.asynctasknserviceexercise;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFileService extends IntentService {
    //using intent service to download file and display into imageview
    private static final String TAG = "DownloadFileService";

    //it is required to be false to run the service
    public DownloadFileService() {
        super("DownloadFileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //obtaining download url
        String kDownloadUrl = intent.getStringExtra("mDownloadUrl");

        //initializing streams
        InputStream kInputStream;
        OutputStream kOutputStream;
        HttpURLConnection kHttpURLConnection;
        Bitmap kResultBitmap;

        float kProgress;   //initializing progress as 0 for progress dialog
        long kDownloaded;    //it stores the downloaded file size
        try {
            URL url = new URL(kDownloadUrl);
            kHttpURLConnection = (HttpURLConnection) url.openConnection();

            //creating temporary file in user's device to store image
            File file = new File(Environment.getExternalStorageDirectory(),
                    Constant.FILE_NAME_SERVICE);


            long kFileSizeToDownload = url.openConnection().getContentLength();      //size of file to be downloaded
            Log.d(TAG, "fileSizeToDownload " + String.valueOf(kFileSizeToDownload));


            Log.d(TAG, "downloaded " + String.valueOf(file.length()));
            kDownloaded = file.length();     //size of the file existing locally

            //checking if file exists, if it exists then customizing connection to implement resume of file
            if (kDownloaded != 0 && kDownloaded < kFileSizeToDownload) {
                Log.d(TAG, "resuming file downloading from" + file.length());

                //setting download to resume ahead of already downloaded length
                kHttpURLConnection.setRequestProperty("Range", "bytes=" + kDownloaded + "-");
            }   //checking if file was completely downloaded?
            else if (file.length() == kFileSizeToDownload) {
                Log.d(TAG, "file size downloaded is " + file.length() + " fileSizeToDownload is " + kFileSizeToDownload);

                //If file is already downloaded completely, sending result back to the calling activity using broadcast
                kResultBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                //sending progress
                Intent intentSetProgress = new Intent("downloadProgress");
                intentSetProgress.putExtra("progress", 100);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentSetProgress);
                //sending result bitmap
                Intent intentSetResult = new Intent("downloadResult");
                intentSetResult.putExtra("imageBitmap", kResultBitmap);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentSetResult);
                //stopping service
                Constant.stoppedService = true;
                stopSelf();
                return;
            } else {    //if file doesnot exists, creating  new file
                boolean b = file.createNewFile();
                Log.d(TAG, "created new file " + b);
            }

            //establishing connection after setting properties
            kHttpURLConnection.connect();

            //obtaining size of file to be downloaded
            long FileSizeToDownload = kHttpURLConnection.getContentLength();
            kInputStream = kHttpURLConnection.getInputStream();

            //setting progress bar progress initial
            if (kDownloaded > 0) {
                //creating outputstream to restore file download from previous progress
                kOutputStream = new FileOutputStream(file, true);
                //restoring progress status on progressbar
                kProgress = (float) (kDownloaded * 100) / kFileSizeToDownload;
            } else {
                //create new output stream
                kOutputStream = new FileOutputStream(file, false);
                kProgress = 0f;

                Intent intentSetProgress = new Intent("downloadProgress");
                intentSetProgress.putExtra("progress", (int) kProgress);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentSetProgress);
            }

            //creating buffer of one kB
            byte[] data = new byte[1024];
            int receivedKB;
            long totalReceivedKB = 0;

            while ((receivedKB = kInputStream.read(data)) != -1) {
                if (Constant.stoppedService) {
                    //if service is explicitely flagged to stop its execution
                    kOutputStream.flush();
                    kOutputStream.close();
                    kInputStream.close();
                    kHttpURLConnection.disconnect();
                    Log.d(TAG, "download cancelled");

                    //sending 100 progress so that progress bar get dismissed immediately
                    Intent intentSetProgress = new Intent("downloadProgress");
                    intentSetProgress.putExtra("progress", 100);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentSetProgress);
                    stopSelf();//Stopping service execution
                    return;
                } else {
                    //if service is not flagged to stop
                    kOutputStream.write(data, 0, receivedKB);
                    totalReceivedKB = totalReceivedKB + receivedKB;
                    //sending progress of download back to the calling activity
                    Intent intentSetProgress = new Intent("downloadProgress");
                    intentSetProgress.putExtra("progress", (int) (kProgress + (float) (totalReceivedKB * 100) / kFileSizeToDownload));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentSetProgress);
                }
            }

            kOutputStream.flush();
            kOutputStream.close();
            kInputStream.close();
            kHttpURLConnection.disconnect();
            Log.d(TAG, "Downloaded file size:" + file.length());
            kResultBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            //sending result bitmap to activity using the broadcast
            Intent intentSetResult = new Intent("downloadResult");
            intentSetResult.putExtra("imageBitmap", kResultBitmap);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentSetResult);
            stopSelf();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
