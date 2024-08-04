package com.example.myjava.dataManage;



import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;



import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * Created by Administrator on 2018-8-22.
 */

public class ExcelUtils {
    private static File parentDir;
    private static File mFile;


    public static String[][] readFromExcel(String filename) {

        parentDir = Environment.getExternalStorageDirectory();
        mFile = new File(parentDir, filename);

        try (InputStream inputStream = new FileInputStream(mFile)) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = 10;
            int totalColumns = 25;
            String[][] result = new String[totalRows][totalColumns];
            for (int i = 0; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if(row == null){
                    continue;
                }
                for (int j = 0; j < totalColumns; j++) {
                    Cell cell = row.getCell(j);
                    switch (cell.getCellTypeEnum()) {
                        case STRING:
                            // 获取字符串类型的单元格值
                            result[i][j] = cell.getStringCellValue();
                            break;
                        case NUMERIC:
                            // 获取数值类型的单元格值
                            double numericValue = cell.getNumericCellValue();
                            // 如果你需要字符串形式的数值
                            result[i][j] = String.valueOf(numericValue);
                            break;
                        // 处理其他单元格类型...
                    }
                }
            }
            return result;
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }


    // 获取Excel文件夹
    public String getExcelDir() {
        // SD卡指定文件夹
        String sdcardPath = Environment.getExternalStorageDirectory().toString();
        File dir = new File(sdcardPath + File.separator + "Excel" + File.separator + "Person");

        if (dir.exists()) {
            return dir.toString();

        } else {
            dir.mkdirs();
            Log.d("BAG", "保存路径不存在,");
            return dir.toString();
        }
    }


}
