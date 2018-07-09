package com.guolei.boardview;
//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.support.v7.widget.RecyclerView;

import com.guolei.boardview.BoardViewListener;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/6
 * Time: 上午10:47
 * Desc:
 */
@SuppressWarnings("WeakerAccess")
public final class BoardViewHolder {

    public static final String EMPTY_ID = "";

    private String mSelectedId = EMPTY_ID;
    private boolean isSmall = false;
    private float mFactor = 1f;
    private BoardViewListener mBoardViewListener;

    public BoardViewHolder() {

    }

    public void setSelectedId(String selectedId) {
        mSelectedId = selectedId;
    }

    public String getSelectedId() {
        return mSelectedId;
    }

    public void setSmall(boolean isSmall) {
        this.isSmall = isSmall;
        if (isSmall) {
            mFactor = 0.6f;
        } else {
            mFactor = 1f;
        }
    }

    public boolean isSmall() {
        return isSmall;
    }

    public float getFactor() {
        return mFactor;
    }

    public void setFactor(float factor) {
        mFactor = factor;
    }

    public BoardViewListener getBoardViewListener() {
        return mBoardViewListener;
    }

    public void setBoardViewListener(BoardViewListener boardViewListener) {
        mBoardViewListener = boardViewListener;
    }
}
