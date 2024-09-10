package com.example.myjava.dataManage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

//mainfest
//<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
//<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"></uses-permission>
//<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"></uses-permission>

public class FileOperation {



	/*java.io.File类用于表示文件（目录）
     *File类只用于表示文件（目录）的信息（名称，大小等），
     *不能用于文件内容的访问
     *java.io.File: An abstract representation of file and directory pathnames.
     */
	
	// File android.content.Context.getFilesDir() : 获取你app的内部存储空间，相当于你的应用在内部存储上的根目录。
	// File android.content.Context.getExternalFilesDir(String type) : 获取本app在外部存储中的根目录。在APP被删除后，这些文件也会被删除。不需要动态申请权限，只需要声明android.permission.WRITE_EXTERNAL_STORAGE权限即可
	// File android.os.Environment.getExternalStorageDirectory() : 获取外部存储的根目录
	//把手机连接电脑，能被电脑识别的部分就一定是外部存储。
	
	private File mFile;
	private File parentDir;
	private Context mContext = null;
    private FileWriter mFileWriter = null;
	private FileReader mFileReader = null;
	private BufferedReader mBufferedReader = null;

    
    //system time
    private SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


	// construct 1
	public FileOperation(String filename){
		parentDir= Environment.getExternalStorageDirectory();
		mFile=new File(parentDir,filename);
		
		checkExist();
	}
	
	// construct 2
	public FileOperation(String filename, Context context){
		mContext=context;
		parentDir=mContext.getExternalFilesDir(null);
		mFile=new File(parentDir,filename);
		
		checkExist();
	}
	
	private void checkExist(){
		
		if(!mFile.exists()){
			Log.i("ZQQf", "第一次创建文件");
			try {
				mFile.createNewFile();
				mFile.setReadable(true);
				mFile.setWritable(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Log.i("ZQQf", "是否已存在文件："+mFile.exists());

	}
	
	public void writeFile(String str){

	    if( mFileWriter == null){
	        //Log.i("ZQQ","创建FileWriter（并清空原文件）");
            try {
                mFileWriter=new FileWriter(mFile,false); //TODO 覆盖模式
                //String date= formatter.format(new java.util.Date());
                //writeFile("文件创建于："+date);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

		try {
			mFileWriter.write(str+"\n");			
			mFileWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String readLineinFile(){

	    if( mFileReader == null ){
            //Log.i("ZQQ","创建FileReader");
            try {
                mFileReader=new FileReader(mFile);
                mBufferedReader=new BufferedReader(mFileReader);
            } catch (FileNotFoundException e) {
                //Toast.makeText(this, "打开文件失败", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }

		String Str = null;
		
		try {
			Str = mBufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("ZQQ","读内部文件失败!");
		}

		return Str;
	}


	public void closeFile(){
		try {
		    if(mFileWriter != null){
                mFileWriter.flush();
                mFileWriter.close();
            }

			if(mBufferedReader != null) mBufferedReader.close();
		    if(mFileReader != null) mFileReader.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}		
	}
	
	public String getFilePath(){
		if(mFile.exists()){
			return mFile.toString();			
		}
		
	    String str="No File!";
		return str;		
	}

}
