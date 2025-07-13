package com.example.myjava.strainsensorapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.myjava.bluetoothSolve.MatrixConfig;

public class MatrixGridView extends View {

    private int cellSize; // 每个正方形元素的尺寸
    private double[][] values; // 存储每个矩阵的数值
    private Paint paint; // 画笔对象

    public MatrixGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLUE); // 设置蓝色文本
        paint.setTextAlign(Paint.Align.CENTER); // 文本居中

        values = new double[MatrixConfig.getRows()][MatrixConfig.getColumns()];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算正方形元素的尺寸，为坐标轴留出空间
        int maxDimension = Math.max(MatrixConfig.getRows(), MatrixConfig.getColumns());
        
        // 为坐标轴预留空间：左侧50px，顶部50px
        int availableWidth = w - 50;
        int availableHeight = h - 50;
        
        // 计算基于宽度和高度的单元格大小，取较小值确保完全显示
        int cellSizeByWidth = availableWidth / MatrixConfig.getColumns();
        int cellSizeByHeight = availableHeight / MatrixConfig.getRows();
        
        // 取较小值确保矩阵完全适应屏幕
        cellSize = Math.min(cellSizeByWidth, cellSizeByHeight);
        
        // 确保单元格大小不会太小
        cellSize = Math.max(cellSize, 20);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 计算矩阵的实际尺寸
        int matrixWidth = MatrixConfig.getColumns() * cellSize;
        int matrixHeight = MatrixConfig.getRows() * cellSize;
        
        // 计算矩阵的起始位置，使其居中显示
        int startX = Math.max(50, (getWidth() - matrixWidth) / 2);
        int startY = Math.max(50, (getHeight() - matrixHeight) / 2);
        
        // 绘制顶部列号
        paint.setColor(Color.BLACK);
        paint.setTextSize(Math.min(20, cellSize / 3));
        paint.setTextAlign(Paint.Align.CENTER);
        for (int j = 0; j < MatrixConfig.getColumns(); j++) {
            float textX = startX + j * cellSize + cellSize / 2f;
            float textY = startY - 10; // 在矩阵上方显示
            canvas.drawText(String.valueOf(j + 1), textX, textY, paint);
        }
        
        // 绘制左侧行号
        paint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i < MatrixConfig.getRows(); i++) {
            float textX = startX - 10; // 在矩阵左侧显示
            float textY = startY + i * cellSize + cellSize / 2f - paint.ascent() / 2f - paint.descent() / 2f;
            canvas.drawText(String.valueOf(i + 1), textX, textY, paint);
        }
        
        // 绘制矩阵
        for (int i = 0; i < MatrixConfig.getRows(); i++) {
            for (int j = 0; j < MatrixConfig.getColumns(); j++) {
                // 计算矩形左上角坐标
                int rectLeft = startX + j * cellSize;
                int rectTop = startY + i * cellSize;
                int rectRight = rectLeft + cellSize;
                int rectBottom = rectTop + cellSize;

                // 绘制矩形
                int colorForValue = getColorForValue(values[i][j]);
                paint.setColor(colorForValue);
                canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);

                // 计算自适应文字大小（数值显示）
                String text = String.valueOf(values[i][j]);
                float textSize = calculateTextSize(text, cellSize);
                paint.setTextSize(textSize);
                
                // 绘制数值文本（居中显示）
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);
                float textX = rectLeft + cellSize / 2f;
                float textY = rectTop + cellSize / 2f - paint.ascent() / 2f - paint.descent() / 2f;
                canvas.drawText(text, textX, textY, paint);
            }
        }
    }
    
    /**
     * 计算适合单元格大小的文字大小
     * @param text 要显示的文本
     * @param cellSize 单元格大小
     * @return 合适的文字大小
     */
    private float calculateTextSize(String text, int cellSize) {
        // 初始文字大小
        float textSize = cellSize / 3f; // 从单元格大小的1/3开始
        Paint testPaint = new Paint();
        testPaint.setTextAlign(Paint.Align.CENTER);
        
        // 逐步调整文字大小，直到文字能够完全适应单元格
        while (textSize > 8) { // 最小文字大小限制
            testPaint.setTextSize(textSize);
            
            // 测量文字宽度和高度
            float textWidth = testPaint.measureText(text);
            float textHeight = testPaint.descent() - testPaint.ascent();
            
            // 检查文字是否能够适应单元格（留一些边距）
            if (textWidth <= cellSize * 0.9f && textHeight <= cellSize * 0.9f) {
                break;
            }
            
            textSize -= 1f; // 减小文字大小
        }
        
        return Math.max(textSize, 8f); // 确保最小文字大小为8
    }
    
    private int getColorForValue(double value) {
        // 将values的范围从0-3.5映射到0-360（色相的范围）
        float normalizedValue = (float) value / 3.5f;
        // 将归一化后的值映射到色相的范围，从240°蓝色到360°红色
        float hue = 240f + (normalizedValue * 120f);

        // 饱和度和亮度可以保持不变，这里我们设置为1（100%）
        float saturation = 1f; // 饱和度100%
        float brightness = 1f; // 亮度100%

        // 将HSV值转换为颜色
        return Color.HSVToColor(new float[]{hue, saturation, brightness});
    }

    // 外部可以通过这个方法来设置矩阵中特定位置的值
    public void setValue(int row, int col, double value) {
        if (row >= 0 && row < MatrixConfig.getRows() && col >= 0 && col < MatrixConfig.getColumns()) {
            values[row][col] = value;
            invalidate(); // 重绘视图
        }
    }
}