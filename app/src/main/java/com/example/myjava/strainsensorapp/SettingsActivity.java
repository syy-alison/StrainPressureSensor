package com.example.myjava.strainsensorapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myjava.R;
import com.example.myjava.bluetoothSolve.MatrixConfig;

public class SettingsActivity extends AppCompatActivity {

    private EditText etRows;
    private EditText etColumns;
    private EditText etPointCount;
    private TextView tvResistanceCount;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化视图
        initViews();
        // 设置监听器
        setupListeners();
    }

    private void initViews() {
        etRows = findViewById(R.id.et_rows);
        etColumns = findViewById(R.id.et_columns);
        etPointCount = findViewById(R.id.et_point_count);
        tvResistanceCount = findViewById(R.id.tv_resistance_count);
        btnNext = findViewById(R.id.btn_next);

        // 设置默认值
        etRows.setText(String.valueOf(MatrixConfig.getDefaultRows()));
        etColumns.setText(String.valueOf(MatrixConfig.getDefaultColumns()));
        etPointCount.setText(String.valueOf(MatrixConfig.getDefaultPointCount()));
        updateResistanceCount();
    }

    private void setupListeners() {
        // 为输入框添加文本变化监听器
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateResistanceCount();
            }
        };

        etRows.addTextChangedListener(textWatcher);
        etColumns.addTextChangedListener(textWatcher);
        // 校准点数不需要实时更新电阻数量，所以不添加监听器

        // 为下一步按钮添加点击监听器
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    startMainActivity();
                }
            }
        });
    }

    private void updateResistanceCount() {
        try {
            String rowsStr = etRows.getText().toString();
            String columnsStr = etColumns.getText().toString();
            
            if (!rowsStr.isEmpty() && !columnsStr.isEmpty()) {
                int rows = Integer.parseInt(rowsStr);
                int columns = Integer.parseInt(columnsStr);
                int resistanceCount = rows * columns;
                tvResistanceCount.setText("电阻数量：" + resistanceCount);
            } else {
                tvResistanceCount.setText("电阻数量：0");
            }
        } catch (NumberFormatException e) {
            tvResistanceCount.setText("电阻数量：0");
        }
    }

    private boolean validateInput() {
        String rowsStr = etRows.getText().toString().trim();
        String columnsStr = etColumns.getText().toString().trim();
        String pointCountStr = etPointCount.getText().toString().trim();

        if (rowsStr.isEmpty()) {
            Toast.makeText(this, "请输入矩阵行数", Toast.LENGTH_SHORT).show();
            etRows.requestFocus();
            return false;
        }

        if (columnsStr.isEmpty()) {
            Toast.makeText(this, "请输入矩阵列数", Toast.LENGTH_SHORT).show();
            etColumns.requestFocus();
            return false;
        }

        if (pointCountStr.isEmpty()) {
            Toast.makeText(this, "请输入校准点数", Toast.LENGTH_SHORT).show();
            etPointCount.requestFocus();
            return false;
        }

        try {
            int rows = Integer.parseInt(rowsStr);
            int columns = Integer.parseInt(columnsStr);
            int pointCount = Integer.parseInt(pointCountStr);

            if (rows <= 0) {
                Toast.makeText(this, "矩阵行数必须大于0", Toast.LENGTH_SHORT).show();
                etRows.requestFocus();
                return false;
            }

            if (columns <= 0) {
                Toast.makeText(this, "矩阵列数必须大于0", Toast.LENGTH_SHORT).show();
                etColumns.requestFocus();
                return false;
            }

            if (pointCount <= 0) {
                Toast.makeText(this, "校准点数必须大于0", Toast.LENGTH_SHORT).show();
                etPointCount.requestFocus();
                return false;
            }

            // 检查矩阵大小是否合理（可以根据需要调整）
            if (rows > 16 || columns > 16) {
                Toast.makeText(this, "矩阵大小不能超过16x16", Toast.LENGTH_SHORT).show();
                return false;
            }

            // 检查校准点数是否合理
            if (pointCount > 100) {
                Toast.makeText(this, "校准点数不能超过100", Toast.LENGTH_SHORT).show();
                return false;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void startMainActivity() {
        int rows = Integer.parseInt(etRows.getText().toString().trim());
        int columns = Integer.parseInt(etColumns.getText().toString().trim());
        int pointCount = Integer.parseInt(etPointCount.getText().toString().trim());

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("matrix_rows", rows);
        intent.putExtra("matrix_columns", columns);
        intent.putExtra("point_count", pointCount);
        startActivity(intent);
        finish(); // 结束当前Activity，防止返回
    }
} 