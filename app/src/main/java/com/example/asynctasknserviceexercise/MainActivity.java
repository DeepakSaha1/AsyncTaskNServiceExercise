package com.example.asynctasknserviceexercise;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //handling result of permission dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE_ASYNC) {     //when permission is taken from async task button
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //all permissions granted
                Intent intent = new Intent(this, ShowImageUsingAsyncTaskActivity.class);
                intent.putExtra("mDownloadUrl", Constant.DOWNLOAD_FILE_URL);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constant.PERMISSION_REQUEST_CODE_SERVICE) { //when permission is taken from service button
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //all permissions granted
                Intent intent = new Intent(this, ShowImageUsingServiceActivity.class);
                intent.putExtra("mDownloadUrl", Constant.DOWNLOAD_FILE_URL);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Click handler for download using async task button
    public void mUseAsyncTask(View view) {
        //check permissions at run time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {    //checking if device is android-23 or above
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //checking permissions manually and taking permissions if not granted already
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , Constant.PERMISSION_REQUEST_CODE_ASYNC);
            } else {
                Intent intent = new Intent(this, ShowImageUsingAsyncTaskActivity.class);
                intent.putExtra("mDownloadUrl", Constant.DOWNLOAD_FILE_URL);
                startActivity(intent);
            }
        } else {        //when device is below android-23
            Intent intent = new Intent(this, ShowImageUsingAsyncTaskActivity.class);
            intent.putExtra("mDownloadUrl", Constant.DOWNLOAD_FILE_URL);
            startActivity(intent);
        }

    }

    //Click handler for download using service button
    public void mUseService(View view) {
        //checking for runtime permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {    //checking if device is android-23 or above
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //checking permissions manually and taking permissions if not granted already
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constant.PERMISSION_REQUEST_CODE_SERVICE);
            } else {
                //If permissions already granted
                Intent intent = new Intent(this, ShowImageUsingServiceActivity.class);
                intent.putExtra("mDownloadUrl", Constant.DOWNLOAD_FILE_URL);
                startActivity(intent);
            }
        } else {      //when device is below android-23
            Intent intent = new Intent(this, ShowImageUsingServiceActivity.class);
            intent.putExtra("mDownloadUrl", Constant.DOWNLOAD_FILE_URL);
            startActivity(intent);
        }
    }
}
