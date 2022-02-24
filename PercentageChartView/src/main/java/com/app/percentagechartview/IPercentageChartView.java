package com.app.percentagechartview;

import android.content.Context;

public interface IPercentageChartView {
    Context getViewContext();

    void postInvalidate();

    void postInvalidateOnAnimation();

    boolean isInEditMode();

    int getWidth();

    int getHeight();

    void onProgressUpdated(float progress);
}
