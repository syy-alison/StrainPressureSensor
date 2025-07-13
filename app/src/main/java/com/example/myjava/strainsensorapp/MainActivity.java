package com.example.myjava.strainsensorapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.myjava.R;
import com.example.myjava.bluetoothSolve.BLEClient;
import com.example.myjava.bluetoothSolve.Constants;
import com.example.myjava.bluetoothSolve.MatrixConfig;
import com.example.myjava.dataManage.CsvOperate;
import com.example.myjava.dataManage.ExcelUtils;
import com.example.myjava.math.PiecewiseLinearFunction;
import com.example.myjava.permissionManage.PermissionManage;


import org.apache.commons.collections4.CollectionUtils;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button searchBtn;
    private TextView connectTx;
    private MatrixGridView matrixGridView;
    private Switch switchCalibratedData;
    private boolean showCalibratedData;

    //Bluetooth
    public BLEClient mBLEClient;
    boolean hasConnected = false;
    //File
    private CsvOperate resistance;
    private CsvOperate logFile;
    DecimalFormat df = new DecimalFormat(".00");
    PiecewiseLinearFunction[] functions = new PiecewiseLinearFunction[MatrixConfig.getResistanceCount()];

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
                        String deviceName = (String) msg.obj;
                        Toast.makeText(activity, "连接成功：" + deviceName + "！！", Toast.LENGTH_LONG).show();
                        connectTx.setText("Connected");
                        searchBtn.setEnabled(false);
                        byte[] data = {0x31, (byte) 0x92};
                        mBLEClient.writeBLECharacteristicValue(data);
                        break;
                    case Constants.DISPLAY_SAVE:
                        int idx = 0;
                        Double[] rowData = new Double[MatrixConfig.getResistanceCount() * 2];
                        int[] checklength = (int[]) msg.obj;

                        for (int i = 0; i < MatrixConfig.getRows(); i++) {
                            for (int j = 0; j < MatrixConfig.getColumns(); j++) {
                                double showData = 0;
                                // 如果当前索引在可用数据范围内
                                rowData[idx] = (double) checklength[idx];
                                if (showCalibratedData) {
                                    showData = functions[idx].evaluate((double) checklength[idx]);
                                    rowData[idx + MatrixConfig.getResistanceCount()] = showData;
                                } else {
                                    showData = (double) checklength[idx];
                                }

                                matrixGridView.setValue(i, j, showData);
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

        // 接收从SettingsActivity传递的矩阵参数
        Intent intent = getIntent();
        if (intent != null) {
            int matrixRows = intent.getIntExtra("matrix_rows", 16);
            int matrixColumns = intent.getIntExtra("matrix_columns", 16);
            int pointCount = intent.getIntExtra("point_count", 10);
            // 设置矩阵大小到MatrixConfig
            MatrixConfig.setMatrixSize(matrixRows, matrixColumns);
            // 设置校准点数到MatrixConfig
            MatrixConfig.setPointCount(pointCount);
        }

        searchBtn = findViewById(R.id.search_bt);
        searchBtn.setOnClickListener(this);
        connectTx = findViewById(R.id.tv_Connect);
        matrixGridView = findViewById(R.id.matrixGridView);
        switchCalibratedData = findViewById(R.id.switch_calibrated_data);

        // 初始化boolean变量，与开关默认状态保持一致
        showCalibratedData = switchCalibratedData.isChecked();

        String excelFileName = MatrixConfig.getRows() + "x" + MatrixConfig.getColumns() + "阵列输出.xlsx";

        // 为开关添加监听器
        switchCalibratedData.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 当开关状态改变时，设置boolean变量的值
            showCalibratedData = isChecked;
            if(showCalibratedData && functions == null){
                // 校准文件不存在，提示用户并关闭开关
                Toast.makeText(this, excelFileName + "不存在或者缺失，无法显示校准后的数据" , Toast.LENGTH_LONG).show();
                switchCalibratedData.setChecked(false);
                showCalibratedData = false;
            }
        });

        //File
        if (PermissionManage.verifyStoragePermissions(this)) {
            resistance = new CsvOperate(Constants.Resistance, false, this,true);
            logFile = new CsvOperate(Constants.LogFile, false, this,false);
            
            // 根据矩阵配置动态生成Excel文件名
            String[][] rawData = ExcelUtils.readFromExcel(excelFileName);
            functions = getFunctions(rawData);
        }
        
        // 在MatrixConfig设置完成后创建BLEClient
        mBLEClient = new BLEClient(MainActivity.this, handler, logFile);

    }

    private PiecewiseLinearFunction[] getFunctions(String[][] rawData) {
        if (rawData == null) {
            return null;
        }
        double[] yPoints = new double[MatrixConfig.getPointCount()];
        double[] xPoints = new double[MatrixConfig.getPointCount()];

        PiecewiseLinearFunction[] result = new PiecewiseLinearFunction[MatrixConfig.getResistanceCount()];

        for (int i = 0; i < MatrixConfig.getPointCount(); i++) {
            if (rawData[i][0] == null) {
                return null;
            }
            yPoints[i] = Double.parseDouble(rawData[i][0]);
        }
        int idx = 0;

        for (int j = 1; j <= MatrixConfig.getResistanceCount(); j++) {
            for (int i = 0; i < MatrixConfig.getPointCount(); i++) {
                if (rawData[i][j] == null) {
                    return null;
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
