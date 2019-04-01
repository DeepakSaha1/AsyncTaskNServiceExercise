package com.example.asynctasknserviceexercise;

public class Constant {
    //15mb image file
    public static String DOWNLOAD_FILE_URL = "http://www.effigis.com/wp-content/uploads/2015/02/DigitalGlobe_WorldView1_50cm_8bit_BW_DRA_Bangkok_Thailand_2009JAN06_8bits_sub_r_1.jpg";

    public static final int PERMISSION_REQUEST_CODE_ASYNC = 111;
    public static final int PERMISSION_REQUEST_CODE_SERVICE = 222;

    public static final String FILE_NAME_SERVICE = "/tempDownloadImageService.jpeg";
    public static final String FILE_NAME_ASYNC = "/tempImageDownloadAsync.jpeg";

    public static volatile boolean stoppedService = false;


}
