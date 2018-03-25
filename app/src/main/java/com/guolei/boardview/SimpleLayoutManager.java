package com.guolei.boardview;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;


public class SimpleLayoutManager extends LinearLayoutManager {

    SimpleLayoutManager(Context context) {
        super(context);
    }

    SimpleLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @SuppressWarnings("unused")
    public SimpleLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            Log.e(Provider.TAG, "onLayoutChildren: " + e.getMessage());
        }
    }
}
