package com.example.myjava.bluetoothSolve;

/**
 * 矩阵配置管理类
 * 用于管理压力传感器矩阵的行数、列数等配置参数
 */
public class MatrixConfig {
    
    // 默认配置
    private static final int DEFAULT_ROWS = 16;
    private static final int DEFAULT_COLUMNS = 16;
    private static final int DEFAULT_POINT_COUNT = 10;
    
    // 当前配置
    private static int currentRows = DEFAULT_ROWS;
    private static int currentColumns = DEFAULT_COLUMNS;
    private static int currentPointCount = DEFAULT_POINT_COUNT;
    
    /**
     * 设置矩阵大小
     * @param rows 矩阵行数
     * @param columns 矩阵列数
     */
    public static void setMatrixSize(int rows, int columns) {
        currentRows = rows;
        currentColumns = columns;
    }
    
    /**
     * 设置校准点数
     * @param pointCount 校准点数
     */
    public static void setPointCount(int pointCount) {
        currentPointCount = pointCount;
    }
    
    /**
     * 获取矩阵行数
     * @return 矩阵行数
     */
    public static int getRows() {
        return currentRows;
    }
    
    /**
     * 获取矩阵列数
     * @return 矩阵列数
     */
    public static int getColumns() {
        return currentColumns;
    }
    
    /**
     * 获取校准点数
     * @return 校准点数
     */
    public static int getPointCount() {
        return currentPointCount;
    }
    
    /**
     * 获取电阻数量（行数 × 列数）
     * @return 电阻数量
     */
    public static int getResistanceCount() {
        return currentRows * currentColumns;
    }
    
    /**
     * 重置为默认配置
     */
    public static void resetToDefault() {
        currentRows = DEFAULT_ROWS;
        currentColumns = DEFAULT_COLUMNS;
        currentPointCount = DEFAULT_POINT_COUNT;
    }
    
    /**
     * 获取默认行数
     * @return 默认行数
     */
    public static int getDefaultRows() {
        return DEFAULT_ROWS;
    }
    
    /**
     * 获取默认列数
     * @return 默认列数
     */
    public static int getDefaultColumns() {
        return DEFAULT_COLUMNS;
    }
    
    /**
     * 获取默认校准点数
     * @return 默认校准点数
     */
    public static int getDefaultPointCount() {
        return DEFAULT_POINT_COUNT;
    }

    public static int getOneFrameByte(){
        return currentRows * currentColumns * 3 + 2;
    }
} 