package com.guolei.boardview;


/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/6
 * Time: 上午10:47
 * Desc:
 */
@SuppressWarnings("unused")
public final class BoardViewStateHolder {

    private static final String EMPTY_ID = "";

    private String mSelectedId = EMPTY_ID;
    private boolean mInSmallMode = false;
    private float mScaleFactor = 1f;
    private BoardViewListener mBoardViewListener;

    BoardViewStateHolder() {

    }

    public void setSelectedId(String selectedId) {
        mSelectedId = selectedId;
    }

    public String getSelectedId() {
        return mSelectedId;
    }

    public void setInSmallMode(boolean isSmall) {
        this.mInSmallMode = isSmall;
        mScaleFactor = isSmall ? .6f : 1f;
    }

    public boolean isInSmallMode() {
        return mInSmallMode;
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        mScaleFactor = scaleFactor;
    }

    public BoardViewListener getBoardViewListener() {
        return mBoardViewListener;
    }

    public void setBoardViewListener(BoardViewListener boardViewListener) {
        mBoardViewListener = boardViewListener;
    }
}
