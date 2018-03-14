package com.guolei.boardview;



import android.support.v7.widget.RecyclerView;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/6
 * Time: 上午10:47
 * Desc:
 */
public class Provider {

    private long mSelectedId = RecyclerView.NO_ID;
    private boolean isSmall = false;
    private float mFac = 1;

    private static Provider INSTANCE;


    public static Provider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Provider();
        }
        return INSTANCE;
    }

    private Provider() {

    }

    public void setSelectedId(long selectedId) {
        mSelectedId = selectedId;
    }

    public long getSelectedId() {
        return mSelectedId;
    }

    public void setSmall(boolean isSmall) {
        this.isSmall = isSmall;
        if (isSmall) {
            mFac = 0.6f;
        } else {
            mFac = 1f;
        }
    }

    public boolean isSmall() {
        return isSmall;
    }

    public float getFac() {
        return mFac;
    }
}
