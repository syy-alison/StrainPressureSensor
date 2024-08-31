package com.example.myjava.dataManage;

import static com.example.myjava.bluetoothSolve.Constants.resistanceCount;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Administrator on 2018-5-6.
 */

public class CsvOperate {
    private File mFile;
    private File parentDir;
    private Context mContext;
    private OutputStreamWriter mWriter;
    private FileReader mFileReader;
    private BufferedReader mBufferedReader;

    private boolean ReadOrWrite;//true:read;false:write

    //system time
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CsvOperate(String filename, boolean OnlyRead, Context context, boolean writeHead) {
        parentDir = Environment.getExternalStorageDirectory();
        mFile = new File(parentDir, filename);
        ReadOrWrite = OnlyRead;
        mContext = context;

        if (!mFile.exists()) {
            try {
                mFile.createNewFile();
                mFile.setReadable(true);
                mFile.setWritable(true);
                FileOutputStream fos = new FileOutputStream(mFile);
                fos.write(0xef);
                fos.write(0xbb);
                fos.write(0xbf);//BOM
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!OnlyRead) {
            try {
                FileOutputStream mFileOutputStream = new FileOutputStream(mFile, true); //追加模式;
                mWriter = new OutputStreamWriter(mFileOutputStream, "UTF-8");
                String date = formatter.format(new java.util.Date());
                writeStringWithEOL("文件创建于：" + date);
                //写表头
                if (writeHead) {
                    writeDataHead();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mFileReader = new FileReader(mFile.toString());
                mBufferedReader = new BufferedReader(mFileReader);
            } catch (FileNotFoundException e) {
                Toast.makeText(context, "打开文件失败", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

    }

    private void writeDataHead(){
        writeStringWithoutEOL("");
        for (int i = 0; i < resistanceCount; i++) {
            writeStringWithoutEOL("R" + i);
        }
        for (int i = 0; i < resistanceCount; i++) {
            if (i == resistanceCount - 1) {
                writeStringWithEOL("pressure" + i);
            } else {
                writeStringWithoutEOL("pressure" + i);
            }
        }
    }

    public void writeStringWithEOL(String str) { //自动换行
        try {
            mWriter.write("\"" + str + "\"" + "," + "\n");
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeStringWithoutEOL(String str) {
        try {
            mWriter.write("\"" + str + "\"" + ",");
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIntArray(int[] array, boolean EndOfLine) {


        StringBuilder mStringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            mStringBuilder.append(array[i]).append(",");
        }
        if (EndOfLine) {
            mStringBuilder.append("\n");
        }
        try {
            mWriter.write(mStringBuilder.toString());
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeDoubleArray(double array, boolean EndOfLine) {

        //TODO 尝试下Object或别的，看怎样能改成泛型

        StringBuilder mStringBuilder = new StringBuilder();
        for (int i = 0; i < 1; i++) {
            mStringBuilder.append(array).append(",");
        }
        if (EndOfLine) {
            mStringBuilder.append("\n");
        }
        try {
            mWriter.write(mStringBuilder.toString());
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String ReadLine() {
        try {
            return mBufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "读取文件失败", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public ArrayList<Integer> ParseLineToIntegers(String Line) {
        StringTokenizer st = new StringTokenizer(Line, ",");
        ArrayList<Integer> intArrayList = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String str = st.nextToken();
            intArrayList.add(Integer.parseInt(str));
        }

        return intArrayList;
    }

    public ArrayList<Double> ParseLineToDoubles(String Line) {
        StringTokenizer st = new StringTokenizer(Line, ",");
        ArrayList<Double> doubleArrayList = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String str = st.nextToken();
            doubleArrayList.add(Double.parseDouble(str));
        }

        return doubleArrayList;
    }


    public void closeFile() {
        if (!ReadOrWrite) {
            try {
                mWriter.flush();
                mWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mBufferedReader.close();
                mFileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFilePath() {
        if (mFile.exists()) {
            return mFile.toString();
        }

        String str = "No File!";
        return str;
    }
}
