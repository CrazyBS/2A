package com.cambiahealth.ahs.entity;

/**
 * Created by r627021 on 3/9/2016.
 */
public enum NdwFileSubmissionControl implements FixedWidth {
    PLAN(3),
    CONTEXT(30),
    CYCLE_ID(8),
    CUR_DATE(8),
    ROW_COUNT(10);

    private int width;

    NdwFileSubmissionControl(int fixedWidth) {
        width = fixedWidth;
    }

    public int getFixedWidth() {
        return width;
    }
}
