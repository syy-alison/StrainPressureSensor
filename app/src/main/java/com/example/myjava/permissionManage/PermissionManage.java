package com.example.myjava.permissionManage;



import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class PermissionManage {

    // Storage Permissions
    public static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //Bluetooth Permissions
    public static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 2;
    private static String[] PERMISSIONS_BLUETOOTH = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static boolean verifyStoragePermissions(Context mContext) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Environment.isExternalStorageManager()) { //Android 6.0以前无需动态申请
            return true;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                Environment.isExternalStorageManager()) {
            Toast.makeText(mContext, "已获得访问所有文件权限", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(mContext)
                    .setMessage("本程序需要您同意允许访问所有文件权限")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            mContext.startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", null) // 你可以根据需要添加取消按钮的逻辑
                    .show();
        }
        //判断是否具有权限
        int permissionCheck1 = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);

        //判断是否需要向用户解释为什么需要申请该权限
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(mContext, "该应用需要读写文件权限以记录实验数据", Toast.LENGTH_LONG).show();
        }

        //请求权限
        if ((permissionCheck1 != PackageManager.PERMISSION_GRANTED) || (permissionCheck2 != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions((Activity)mContext, PERMISSIONS_STORAGE, REQUEST_CODE_EXTERNAL_STORAGE);
            return false;
        } else {
            return true;
        }

    }


    public static boolean verifyBluetoothPermissions(Context mContext) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //Android 6.0以前无需动态申请
            return true;
        }

        //判断是否具有权限
        int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);

        //判断是否需要向用户解释为什么需要申请该权限
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(mContext,"自Android 6.0开始需要打开位置权限才可以搜索到蓝牙设备", Toast.LENGTH_LONG).show();
        }

        //请求权限
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity)mContext,PERMISSIONS_BLUETOOTH,REQUEST_CODE_ACCESS_COARSE_LOCATION);
            return false;
        }else{
            return true;
        }

    }

}
