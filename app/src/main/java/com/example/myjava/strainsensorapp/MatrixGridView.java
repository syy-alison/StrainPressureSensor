package com.example.myjava.strainsensorapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MatrixGridView extends View {

    private int rows = 8; // 行数
    private int columns = 3; // 列数
    private int cellWidth; // 每个矩形的宽度
    private int cellHeight; // 每个矩形的高度
    private int horizontalSpacing; // 水平间距
    private int verticalSpacing; // 垂直间距
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
        paint.setTextSize(30); // 文本大小
        paint.setTextAlign(Paint.Align.CENTER); // 文本居中

        horizontalSpacing = 20; // 设置水平间距
        verticalSpacing = 20; // 设置垂直间距

        values = new double[rows][columns];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellWidth = (w - (columns - 1) * horizontalSpacing) / columns;
        cellHeight = (h - (rows - 1) * verticalSpacing) / rows;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                // 计算矩形左上角坐标
                int rectLeft = j * (cellWidth + horizontalSpacing);
                int rectTop = i * (cellHeight + verticalSpacing);
                int rectRight = rectLeft + cellWidth;
                int rectBottom = rectTop + cellHeight;

                // 绘制矩形
                int colorForValue = getColorForValue(values[i][j]);
                paint.setColor(colorForValue); // 浅蓝色背景
                canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);

                // 绘制文本
                paint.setColor(Color.WHITE); // 浅蓝色背景
                String text = String.valueOf(values[i][j]);
                float textX = rectLeft + cellWidth / 2f;
                float textY = rectTop + cellHeight / 2f - paint.ascent() / 2f - paint.descent() / 2f;
                canvas.drawText(text, textX, textY, paint);
            }
        }


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
        if (row >= 0 && row < rows && col >= 0 && col < columns) {
            values[row][col] = value;
            invalidate(); // 重绘视图
        }
    }
}