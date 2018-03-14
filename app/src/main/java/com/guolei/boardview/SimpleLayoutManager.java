package com.guolei.boardview;



import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;


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
