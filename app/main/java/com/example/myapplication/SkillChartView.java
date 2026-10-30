package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SkillChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Integer> values = new ArrayList<>();
    private final String[] names = {"SHOT", "PASS", "DRIB", "CTRL", "SPD", "STA", "FIN"};

    public SkillChartView(Context context) {
        super(context);
        init();
    }

    public SkillChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkillChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setColor(0xFF2196F3);
        textPaint.setColor(0xFF9FB6D1);
        textPaint.setTextSize(22f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<Integer> data) {
        this.values = new ArrayList<>(data);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values == null || values.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        int max = 20;
        for (int value : values) {
            max = Math.max(max, value);
        }

        float cellWidth = width / (float) values.size();
        float barWidth = cellWidth * 0.55f;

        for (int i = 0; i < values.size(); i++) {
            float barHeight = (values.get(i) / (float) max) * (height - 60);
            float centerX = i * cellWidth + cellWidth / 2f;
            float left = centerX - barWidth / 2f;
            float top = height - 40 - barHeight;

            canvas.drawRect(left, top, left + barWidth, height - 40, barPaint);
            canvas.drawText(names[i], centerX, height - 10, textPaint);
        }
    }
}
