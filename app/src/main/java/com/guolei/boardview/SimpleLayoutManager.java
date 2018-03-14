package com.guolei.boardview;



import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/1
 * Time: 下午5:16
 * Desc:
 */
public class SimpleLayoutManager extends LinearLayoutManager {
    public SimpleLayoutManager(Context context) {
        super(context);
    }

    public SimpleLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SimpleLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
//            Log.e("BoardView", "onLayoutChildren: " + e.getMessage());
        }
    }
}
