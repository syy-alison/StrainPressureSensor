package com.example.myjava.math;

import java.util.Arrays;

public class PiecewiseLinearFunction {
    private double[] xPoints; // 所有x坐标点
    private double[] yPoints; // 所有y坐标点


    public PiecewiseLinearFunction(double[] xPoints, double[] yPoints) {
        if (xPoints.length != yPoints.length || xPoints.length < 2) {
            throw new IllegalArgumentException("Must have at least two points to define a segment.");
        }
        this.xPoints = xPoints.clone(); // 存储x坐标点的副本
        this.yPoints = yPoints.clone(); // 存储y坐标点的副本
        Arrays.sort(xPoints);
        Arrays.sort(yPoints);

    }

    public double evaluate(double x) {
        // 检查x值是否在定义的范围内
        if (x < xPoints[0]) {
           return 0;
        }

        // 找到x所属的线段
        for (int i = 0; i < xPoints.length - 1; i++) {
            if (xPoints[i] <= x && x <= xPoints[i + 1]) {
                // 计算当前线段的斜率和截距
                double m = (yPoints[i + 1] - yPoints[i]) / (xPoints[i + 1] - xPoints[i]);
                double b = yPoints[i] - m * xPoints[i];
                // 使用线性方程计算y值
                return m * x + b;
            }
        }

        // 如果x值等于最大的xPoint，返回最后一个点的y值
        return yPoints[yPoints.length - 1];
    }
}
