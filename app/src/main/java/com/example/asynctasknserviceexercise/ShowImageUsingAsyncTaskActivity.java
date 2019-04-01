package com.example.asynctasknserviceexercise;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ShowImageUsingAsyncTaskActivity extends AppCompatActivity implements NetStateReceiver.INetStateChange {


    private static final String TAG = "ShowImageUsingAsyncTask";

    ImageView mImage;
    ProgressDialog mProgressDialog;
    String mDownloadFileUrl;
    NetStateReceiver mNetStateReceiver;
    boolean mResumeDownload = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_using_async_task);
        mImage = findViewById(R.id.imageview_async);

        NetStateReceiver.setConnectivityListener(this);
        mNetStateReceiver = new NetStateReceiver();

        //initializing progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setMessage("Downloading image.. Press back to cancel download.");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);

        //obtaining download url string from received intent
        mDownloadFileUrl = getIntent().getStringExtra("mDownloadUrl");
    }

    //register receiver
    @Override
    protected void onStart() {
        super.onStart();
        this.registerReceiver(mNetStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    //unregister receiver
    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(mNetStateReceiver);
    }

    //Deleting file as soon as its user leaves activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        File kFile = new File(Environment.getExternalStorageDirectory(), Constant.FILE_NAME_ASYNC);
        boolean kDeleteFile = false;
        if (kFile.exists())
            kDeleteFile = kFile.delete();
        Log.d("MainActivity", "kDeleteFile=" + kDeleteFile);
    }


    @Override
    public void mOnNetStateChangeListener(boolean isConnected) {
        if (isConnected && mResumeDownload) {
            Toast.makeText(this, "Network available and connected", Toast.LENGTH_SHORT).show();
            //execute async task
            new DownloadFileTask().execute(mDownloadFileUrl);
        } else {
            Toast.makeText(this, "Network not available OR Download cancelled.", Toast.LENGTH_SHORT).show();
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
        //pause downloading if network is not available
    }

    //Creating async task to download file
    private class DownloadFileTask extends AsyncTask<String, Integer, Bitmap> {
        //Async task with mDownloadUrl as input and Bitmap as the output
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //handling back press when progress dialog is showing
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                    Log.d("ShowImageUsingAsyncTask", "cancel called");
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... downloadUrl) {
            InputStream kInputStream;
            OutputStream kOutputStream;
            HttpURLConnection kHttpURLConnection;
            Bitmap resultBitmap = null;
            int kProgress = 0;
            try {
                URL url = new URL(downloadUrl[0]);
                kHttpURLConnection = (HttpURLConnection) url.openConnection();

                File kFile = new File(Environment.getExternalStorageDirectory(), Constant.FILE_NAME_ASYNC);
                //downloaded is used to store size of local temporary file
                long kDownloaded = 0;

                //size of data Image
                long kDownloadFileSize = url.openConnection().getContentLength();

                //checking if file exists and is not downloaded completely
                if (kFile.exists() && kFile.length() < kDownloadFileSize) {
                    kDownloaded = kFile.length();
                    kHttpURLConnection.setRequestProperty("Range", "bytes=" + kDownloaded + "-");
                } else if (kFile.exists() && kFile.length() == kDownloadFileSize) {
                    resultBitmap = BitmapFactory.decodeFile(kFile.getAbsolutePath());
                    return resultBitmap;
                } else {
                    boolean kIfCreatedFile = kFile.createNewFile();
                    //if file doesn't exists in the system, create a new file
                    Log.d(TAG, "if created new file? " + kIfCreatedFile);
                }

                //establishing connection and obtianing input stream
                kHttpURLConnection.connect();
                kInputStream = kHttpURLConnection.getInputStream();

                if (kDownloaded > 0 && kDownloaded < kDownloadFileSize) {
                    kOutputStream = new FileOutputStream(kFile, true);
                    kProgress = (int) (kDownloaded * 100 / kDownloadFileSize);
                } else
                    kOutputStream = new FileOutputStream(kFile);


                //creating buffer of one kB
                byte[] data = new byte[1024];
                int receivedKB;
                long totalReceivedKB = 0;

                //reading 1024 bit of data in buffer 'data' until it reads till end of file
                while ((receivedKB = kInputStream.read(data)) != -1) {
                    if (isCancelled()) {
                        //checking if async task execution has not been cancelled
                        mResumeDownload = false;     //setting download to do not resume on connectivity status change
                        Log.d(TAG, "AsyncCancellationCheck " + "task cancelled");
                        break;
                    } else {
                        //writing buffer data into the output stream associated with file
                        kOutputStream.write(data, 0, receivedKB);
                        totalReceivedKB = totalReceivedKB + receivedKB;
                        publishProgress(kProgress + (int) (totalReceivedKB * 100 / kDownloadFileSize));
                    }

                }

                //releasing resources and closing streams
                kOutputStream.flush();
                kOutputStream.close();
                kInputStream.close();
                kHttpURLConnection.disconnect();

                //obtainig bitmap from file
                resultBitmap = BitmapFactory.decodeFile(kFile.getAbsolutePath());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultBitmap;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mImage.setImageBitmap(bitmap);
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(ShowImageUsingAsyncTaskActivity.this, "Download is cancelled.", Toast.LENGTH_SHORT).show();
        }

    }

}
