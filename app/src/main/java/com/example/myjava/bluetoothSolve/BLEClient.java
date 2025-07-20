package com.example.myjava.bluetoothSolve;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


import com.example.myjava.dataManage.CsvOperate;
import com.example.myjava.dataManage.FileOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class BLEClient {
    //TODO 目前只考虑用于一个Activity，未来要用于多个Activity之前要检查过！！！

    // constants
    // constants
    private static final String ecCharacteristicNotifyUUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String ecCharacteristicWriteUUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    public static final int REQUEST_ENABLE_BT = 2;
    private static boolean connectFlag = false;
    private static BluetoothGattCharacteristic ecCharacteristicWrite;

    // member variables
    private Activity uiActivity;
    private Handler uiHandler;
    private static BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> BLDeviceList = new ArrayList<>();
    private static BluetoothGatt mBluetoothGatt;
    private AlertDialog BLDialog = null;
    private static BluetoothGattCharacteristic mCharacteristic;

    private static FileOperation lastConnectDeviceFile;
    private static String lastConnectDeviceAddress = null;
    private static boolean connectLastDevice = false;
    private boolean bStart = false;
    private static byte[] oneFrame; // 将在需要时初始化
    ProgressDialog mProgressDialog;
    private static boolean bDisplay = false;
    private static int j = 0;



    public static boolean runalways = false;

    volatile int framesHasReadTest = 0;


    private static CsvOperate logfile;//the number of frames we have read

    // construct function
    public BLEClient(Activity newAct, Handler newHandler, CsvOperate csvOperate) {
        uiActivity = newAct;
        uiHandler = newHandler;
        logfile = csvOperate;

        // File
        if (lastConnectDeviceAddress == null) {
            lastConnectDeviceFile = new FileOperation("LastConnectDeviceFile.txt", uiActivity);
            lastConnectDeviceAddress = lastConnectDeviceFile.readLineinFile();
            Log.i("ZQQ", "LastConnectDeviceAddress = " + lastConnectDeviceAddress);
            lastConnectDeviceFile.closeFile();
        }

        (new initBLEThread()).start();
    }

    private class initBLEThread extends Thread {

        @Override
        public void run() {

            final BluetoothManager bluetoothManager = (BluetoothManager) uiActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {  // Device does not support Bluetooth
                new AlertDialog.Builder(uiActivity).setTitle("No BluetoothAdapter").show();
            } else {                            // Device supports Bluetooth
                if (!mBluetoothAdapter.isEnabled()) {    // Bluetooth not opened

                    //Log.i("ZQQ","蓝牙还未开启");
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    uiActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    //Log.i("ZQQ","蓝牙已打开");
                    bStart = true;


                }
            }

        }

    }

    private static final long SCAN_PERIOD = 10000;// Stops scanning after 10 seconds.

    public void searchDevice() {
        // if(bStart) {
        mProgressDialog = ProgressDialog.show(uiActivity, "搜索与连接设备", "正在搜索设备……");
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

        BLDeviceList.clear();// clear device list

        boolean flag = mBluetoothAdapter.startLeScan(mLeScanCallback);
        Log.i("ZQQ", "是否开始搜索：" + String.valueOf(flag));

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                Looper.prepare();
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.i("ZQQ", "结束扫描");
                onScanFinished();
                Looper.loop();
            }
        };
        timer.schedule(task, SCAN_PERIOD);// Stops scanning after a pre-defined scan period.
        // }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) { //当一个BLE设备被找到

                    Log.i("ZQQ", device.getName() + "  " + device.getAddress());

                    if (device.getAddress().equals(lastConnectDeviceAddress)) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mBluetoothGatt = device.connectGatt(uiActivity, false, mGattCallback);
                        connectLastDevice = true;
                        mProgressDialog.dismiss();
                    } else {
                        if (!BLDeviceList.contains(device)) {  // add to device list
                            BLDeviceList.add(device);
                        }
                    }

                }
            };

    private void onScanFinished() {
        if (!connectLastDevice) { //没有在搜索设备过程中找到上一次连接成功的设备

            mProgressDialog.dismiss();

            String[] stringList = getStringListFromDevice();

            if (BLDialog != null) BLDialog.dismiss();

            BLDialog = new AlertDialog.Builder(uiActivity)
                    .setTitle("请选择设备：")
                    .setItems(stringList,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(uiActivity, BLDeviceList.get(which).getName(), Toast.LENGTH_SHORT).show();
                                    handleChosenDevice(which);
                                }

                                private void handleChosenDevice(int which) {
                                    BluetoothDevice mBLTdevice = BLDeviceList.get(which);
                                    mBluetoothGatt = mBLTdevice.connectGatt(uiActivity, false, mGattCallback);
                                    lastConnectDeviceAddress = mBLTdevice.getAddress();
                                    logfile.writeStringWithEOL("handleChosenDevice" + lastConnectDeviceAddress);
                                }
                            }).create();
            BLDialog.show();
        }

    }

    private static void setMtu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt.requestMtu(247);
        }
    }

    private String[] getStringListFromDevice() {

        int len = BLDeviceList.size();
        String str[] = new String[len];

        for (int idx = 0; idx < len; idx++) {
            BluetoothDevice device = BLDeviceList.get(idx);

            str[idx] = device.getName() + "\n"
                    + device.getAddress() + "\n";
        }

        return str;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            logfile.writeStringWithEOL("onConnectionStateChange" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                boolean discoverServices = gatt.discoverServices();
                logfile.writeStringWithEOL("discoverServices:" + discoverServices);
                intentAction = Constants.ACTION_GATT_CONNECTED;
                Message msg = Message.obtain();
                msg.what = Constants.GATT_SERVICES_DISCOVERED;
                // 获取设备名称并传递给MainActivity
                String deviceName = gatt.getDevice().getName();
                if (deviceName == null || deviceName.isEmpty()) {
                    deviceName = "未知设备";
                }
                msg.obj = deviceName;
                uiHandler.sendMessage(msg);
                lastConnectDeviceFile.writeFile(lastConnectDeviceAddress); //把本次连接成功的设备地址写入内部文件
                lastConnectDeviceFile.closeFile();
                connectFlag = true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.close();
                if (connectFlag) {
                    intentAction = Constants.ACTION_GATT_DISCONNECTED;
                    Log.i("ZQQ", "Disconnected from GATT server.");
                    Message msg = Message.obtain();
                    msg.what = Constants.GATT_DISCONNECTED;
                    uiHandler.sendMessage(msg);
                }
                connectFlag = false;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            logfile.writeStringWithEOL("onServicesDiscovered" + status);
            super.onServicesDiscovered(gatt, status);
            mBluetoothGatt = gatt;
            List<BluetoothGattService> bluetoothGattServices = mBluetoothGatt.getServices();
            new Thread(() -> {
                try {
                    for (BluetoothGattService service : bluetoothGattServices) {
                        Log.e("ble-service", "UUID=" + service.getUuid().toString());
                        List<BluetoothGattCharacteristic> listGattCharacteristic = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : listGattCharacteristic) {
                            logfile.writeStringWithEOL("ble-char" + "UUID=:" + characteristic.getUuid().toString());

                            if (characteristic.getUuid().toString().equals(ecCharacteristicNotifyUUID)) {
                                logfile.writeStringWithEOL("notifyBLECharacteristicValueChange:" + characteristic.getUuid().toString());
                                notifyBLECharacteristicValueChange(characteristic);
                            }
                            if (characteristic.getUuid().toString().equals(ecCharacteristicWriteUUID)) {
                                ecCharacteristicWrite = characteristic;
                            }

                        }
                    }
                } catch (Throwable ignored) {
                }
            }).start();
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    setMtu();
                } catch (Throwable ignored) {
                }
            }).start();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.e("BLEService", "onMtuChanged success MTU = " + mtu);
            } else {
                Log.e("BLEService", "onMtuChanged fail ");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            //如果连接设备发送数据到手机端，将通过这个函数获取数据。
            logfile.writeStringWithEOL("onCharacteristicChanged data length" + data.length);
            
            // 确保oneFrame数组已初始化
            if (oneFrame == null) {
                oneFrame = new byte[MatrixConfig.getOneFrameByte()];
            }
            
            if(data[0] == 0x31 && data[oneFrame.length - 1] == (byte) 0x92) {
                System.arraycopy(data, 0, oneFrame, 0, data.length);
                handle_test();
            }
        }
    };

    public static void notifyBLECharacteristicValueChange(BluetoothGattCharacteristic characteristic) {
        boolean res = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        logfile.writeStringWithEOL("notifyBLECharacteristicValueChange:" + res);
        if (!res) {
            return;
        }
        for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(dp);
        }
    }

    public void writeBLECharacteristicValue(byte[] byteArray) {
        logfile.writeStringWithEOL("writeBLECharacteristicValue" + byteArray[0] +"and" +  byteArray[1]);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                if (ecCharacteristicWrite != null) {
                    ecCharacteristicWrite.setValue(byteArray);
                    //设置回复形式
                    ecCharacteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    //开始写数据
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.writeCharacteristic(ecCharacteristicWrite);
                        logfile.writeStringWithEOL("writeBLECharacteristicValue start");
                    }
                }
            }
        };
        timer.schedule(task, 10000, 90000);// Stops scanning after a pre-defined

    }


    private void handle_test() {
        int[] resultTest = new int[MatrixConfig.getResistanceCount()];

        int num1, num2, num3, num;
        for (int idx = 0; idx < MatrixConfig.getResistanceCount(); idx++) {
            num1 = toInt(oneFrame[3 * idx + 3]);
            num2 = toInt(oneFrame[3 * idx + 2]);
            num3 = toInt(oneFrame[3 * idx + 1]);
            num = num3 * 256 * 256 + num2 * 256 + num1;
            resultTest[idx] = num;
        }
        framesHasReadTest++;
        Message msg = Message.obtain();
        msg.obj = resultTest; //直接把这一帧（7个数据）的数据传出去
        msg.arg1 = framesHasReadTest;
        msg.what = Constants.DISPLAY_SAVE;
        uiHandler.sendMessage(msg);
    }


    public void close() {

        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;

    }

    private int toInt(byte b) {
        return (0xff & b);
    }


}