package com.guolei.boardview;


import android.support.v7.widget.RecyclerView;

class Provider {

    static final String TAG = "BoardView";

    private long mSelectedId = RecyclerView.NO_ID;
    private boolean isSmall = false;
    private float mFac = 1;
    private static Provider INSTANCE;

    static Provider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Provider();
        }
        return INSTANCE;
    }

    private Provider() {
    }

    void setSelectedId(long selectedId) {
        mSelectedId = selectedId;
    }

    long getSelectedId() {
        return mSelectedId;
    }

    void setSmall(boolean isSmall) {
        this.isSmall = isSmall;
        if (isSmall) {
            mFac = 0.6f;
        } else {
            mFac = 1f;
        }
    }

    boolean isSmall() {
        return isSmall;
    }

    float getFac() {
        return mFac;
    }
}
