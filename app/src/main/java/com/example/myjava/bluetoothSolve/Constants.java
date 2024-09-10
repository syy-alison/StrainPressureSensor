package com.example.myjava.bluetoothSolve;

public class Constants {
	public static final int SEARCHING_DEVICE=1;
	public static final int BONDING_DEVICE=2;
	public static final int CONNECT_FAIL=3;
	public static final int CONNECT_SUCCESS=4;
	public static final int DISCOVERY_FINISHED=5;
	public static final int SEND_CMD=6;
	public static final int READ_FAIL=7;
	public static final int SHOWCHART=10;
	public static final int SHOWLENGTH = 20;
	public static final int Cmd_Start=18;
	public static final int Cmd_Stop=19;
	public static final int Cmd_Test=21;

	public final static int GATT_DISCONNECTED = 22;
	public final static int GATT_SERVICES_DISCOVERED = 23;
	public final static  int DISPLAY_SAVE = 24;
	public final static String ACTION_GATT_DISCONNECTED =
			"com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

	public final static String ACTION_GATT_CONNECTED =
			"com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public static final String Resistance = "PressureSensor原始数据与转化数据.csv";

	public static final String LogFile = "PressureSensor日志.csv";
	/**
	 * 用于校准的点数量
	 */
	public static final int pointCount = 10;

	/**
	 * 用于校准的电阻数量
	 */
	public static final int  resistanceCount = 24;


}
