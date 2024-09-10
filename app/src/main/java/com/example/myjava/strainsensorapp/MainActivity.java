package com.example.myjava.strainsensorapp;

import static com.example.myjava.bluetoothSolve.Constants.pointCount;
import static com.example.myjava.bluetoothSolve.Constants.resistanceCount;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.myjava.R;
import com.example.myjava.bluetoothSolve.BLEClient;
import com.example.myjava.bluetoothSolve.Constants;
import com.example.myjava.dataManage.CsvOperate;
import com.example.myjava.dataManage.ExcelUtils;
import com.example.myjava.math.PiecewiseLinearFunction;
import com.example.myjava.permissionManage.PermissionManage;



import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button searchBtn;
    private TextView connectTx;
    private MatrixGridView matrixGridView;

    //Bluetooth
    public BLEClient mBLEClient;
    boolean hasConnected = false;
    //File
    private CsvOperate resistance;
    private CsvOperate logFile;
    DecimalFormat df = new DecimalFormat(".00");
    PiecewiseLinearFunction[] functions = new PiecewiseLinearFunction[resistanceCount];

    //Handler for main thread
    private class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();

            if (activity != null) {
                switch (msg.what) {
                    case Constants.CONNECT_SUCCESS:
                        hasConnected = true;
                        searchBtn.setEnabled(false);
                        break;
                    case Constants.GATT_SERVICES_DISCOVERED:
                        Toast.makeText(activity, "连接成功！！", Toast.LENGTH_LONG).show();
                        connectTx.setText("Connected");
                        searchBtn.setEnabled(false);
                        byte[] data = {0x31, (byte) 0x92};
                        mBLEClient.writeBLECharacteristicValue(data);
                        break;
                    case Constants.DISPLAY_SAVE:
                        int idx = 0;
                        Double[] rowData = new Double[resistanceCount * 2];
                        int [] checklength =(int[]) msg.obj;
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < 8; j++) {
                                double evaluate = functions[idx].evaluate((double) checklength[idx]);
                                matrixGridView.setValue(i, j, evaluate);
                                rowData[idx] = (double) checklength[idx];
                                rowData[idx + resistanceCount] = evaluate;
                                idx++;
                            }
                        }
                        //存储数据
                        saveDataToFile(rowData);
                        break;
                    case Constants.READ_FAIL:
                        Toast.makeText(activity, "接收数据失败！！", Toast.LENGTH_LONG).show();
                        break;
                    case Constants.GATT_DISCONNECTED:
                        connectTx.setText("UnConnect");
                        Toast.makeText(activity, "和设备断开连接！！", Toast.LENGTH_LONG).show();
                        break;
                    case Constants.Cmd_Stop:
                        break;
                }
            }
        }
    }



    private void saveDataToFile(Double[] rowData) {
        for (int i = 0; i < rowData.length; i++) {
            WriteDoubleFramesToFile(rowData[i], i, rowData.length, resistance);
        }
    }

    private final MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate);

        searchBtn = findViewById(R.id.search_bt);
        searchBtn.setOnClickListener(this);
        connectTx = findViewById(R.id.tv_Connect);
        matrixGridView = findViewById(R.id.matrixGridView);

        //File
        if (PermissionManage.verifyStoragePermissions(this)) {
            resistance = new CsvOperate(Constants.Resistance, false, this,true);
            logFile = new CsvOperate(Constants.LogFile, false, this,false);
            String[][] rawData = ExcelUtils.readFromExcel("3x8阵列输出.xlsx");
            functions = getFunctions(rawData);
        }
        mBLEClient = new BLEClient(MainActivity.this, handler, logFile);

    }

    private PiecewiseLinearFunction[] getFunctions(String[][] rawData) {
        if (rawData == null) {
            return new PiecewiseLinearFunction[resistanceCount];
        }
        double[] yPoints = new double[pointCount];
        double[] xPoints = new double[pointCount];

        PiecewiseLinearFunction[] result = new PiecewiseLinearFunction[resistanceCount];

        for (int i = 0; i < pointCount; i++) {
            if (rawData[i][0] == null) {
                continue;
            }
            yPoints[i] = Double.parseDouble(rawData[i][0]);
        }
        int idx = 0;

        for (int j = 1; j <= resistanceCount; j++) {
            for (int i = 0; i < pointCount; i++) {
                if (rawData[i][j] == null) {
                    continue;
                }
                xPoints[i] = Double.parseDouble(rawData[i][j]);
            }
            result[idx++] = new PiecewiseLinearFunction(xPoints, yPoints);
        }
        return result;

    }

    @Override

    public void onClick(View v) {
        if (v == searchBtn) {
            if (PermissionManage.verifyBluetoothPermissions(this))
                mBLEClient.searchDevice();

        }
    }

    private static final int REQUEST_CODE_LOCATION_SETTINGS = 1;

    //用户处理权限反馈：如果允许授权，在这里进行接下来的操作；如果拒绝授权，在这里提示用户去“设置”更改权限管�?
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PermissionManage.REQUEST_CODE_ACCESS_COARSE_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    //检查定位是否打开
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (networkProvider || gpsProvider) {
                        //TODO
                        mBLEClient.searchDevice();
                    } else {
                        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
                    }
                } else {
                    Toast.makeText(this, "未获得定位权限，无法使用APP", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            //检查定位是否打开
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (networkProvider || gpsProvider) {
                //TODO
                mBLEClient.searchDevice();
            } else {
                Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                this.startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
            }
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        resistance.closeFile();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //不符合流程的非正常退出，需要提前关闭
            BLEClient.runalways = false;
            mBLEClient.close();

            System.exit(0);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private void WriteDoubleFramesToFile(double src, int frameIndex, int framesInOneLine, CsvOperate fileToWrite) {

        if (frameIndex % framesInOneLine == 0) { //行首
            String date = formatter.format(new java.util.Date());
            fileToWrite.writeStringWithoutEOL(date);
        }

        if ((frameIndex + 1) % framesInOneLine == 0) { //行尾
            fileToWrite.writeDoubleArray(src, true);
        } else {
            fileToWrite.writeDoubleArray(src, false);
        }
        return;
    }



}
