package com.example.myjava.strainsensorapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GradientIndicatorView extends View {

    private Paint paint;
    private Paint textPaint;
    private double minValue; // 渐变的最小值
    private double maxValue; // 渐变的最大值
    private int rectWidth;   // 矩形的宽度
    private int rectHeight;  // 矩形的高度

    // 标注的文本值
    private double[] tickValues = {0, 1, 2, 3, 3.5};

    public GradientIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        // 初始化文本绘制用的Paint
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK); // 文本颜色
        textPaint.setTextSize(16); // 文本大小
        // 文本居中对齐
        textPaint.setTextAlign(Paint.Align.CENTER);

        minValue = 0.0;
        maxValue = 3.5;
        rectWidth = 500; // 您可以根据需要设置矩形的宽度
        rectHeight = 50; // 增加高度以容纳文本
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制渐变色块
        paint.setColor(getColorForValue(maxValue)); // 用最大值的颜色绘制背景
        canvas.drawRect(0, 0, rectWidth, rectHeight, paint);

        // 绘制每个值对应的颜色条
        for (double value : tickValues) {
            float fraction = (float) ((float) (value - minValue) / (maxValue - minValue));
            int color = getColorForValue(value);
            paint.setColor(color);
            // 计算每个颜色条的宽度，这里设置为5px，您可以根据需要调整
            float colorBarWidth = 5;
            canvas.drawRect(fraction * rectWidth - colorBarWidth / 2, 0,
                    fraction * rectWidth + colorBarWidth / 2, rectHeight, paint);
        }

        // 绘制文本标注
        for (double tickValue : tickValues) {
            float tickFraction = (float) ((float) (tickValue - minValue) / (maxValue - minValue));
            String tickText = String.valueOf(tickValue);
            // 计算文本的X坐标，文本中心对齐
            float textX = tickFraction * rectWidth;
            // 计算文本的Y坐标，文本底部对齐到渐变色块顶部
            float textY = rectHeight - textPaint.descent(); // 文本底部对齐
            // 绘制文本
            canvas.drawText(tickText, textX, textY, textPaint);
        }
    }

    private int getColorForValue(double value) {
        float normalizedValue = (float) ((float) (value - minValue) / (maxValue - minValue));
        float hue = 240f + (normalizedValue * 120f);
        float saturation = 1f;
        float brightness = 1f;

        return Color.HSVToColor(new float[]{hue, saturation, brightness});
    }
}