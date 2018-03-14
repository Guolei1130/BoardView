package com.guolei.boardview;


import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/2/27
 * Time: 下午3:01
 * Desc:
 */
public class BoardViewTouchHelper extends RecyclerView.ItemDecoration
        implements RecyclerView.OnChildAttachStateChangeListener {

    private RecyclerView mRecyclerView;

    float mSwipeEscapeVelocity;

    float mMaxSwipeVelocity;

    private int mSlop;

    GestureDetectorCompat mGestureDetector;

    private RecyclerView.ViewHolder mSelected;
    private float mInitialTouchX;
    private float mInitialTouchY;

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return;
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            final Resources resources = recyclerView.getResources();
            mSwipeEscapeVelocity = resources
                    .getDimension(android.support.v7.recyclerview.R.dimen.item_touch_helper_swipe_escape_velocity);
            mMaxSwipeVelocity = resources
                    .getDimension(android.support.v7.recyclerview.R.dimen.item_touch_helper_swipe_escape_max_velocity);
            setupCallbacks();
        }
    }

    private void destroyCallbacks() {

    }

    private void setupCallbacks() {
        ViewConfiguration vc = ViewConfiguration.get(mRecyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.addOnChildAttachStateChangeListener(this);
        initGestureDetector();
    }

    private void initGestureDetector() {
        if (mGestureDetector != null) {
            return;
        }
        mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(),
                new BoardViewTouchHelperGestureListener());
    }

    private boolean landUpRecyclerView(MotionEvent event) {
        View view = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
        if (view == null) return false;
        int y = (int) event.getY();
        View recyclerView = view.findViewById(R.id.recycler_view);
        return y > recyclerView.getTop() && y < recyclerView.getBottom();
    }

    private final RecyclerView.OnItemTouchListener mOnItemTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            if (landUpRecyclerView(e)) {
                mGestureDetector.onTouchEvent(e);
            }
            return mSelected != null;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            if (landUpRecyclerView(e)) {
                mGestureDetector.onTouchEvent(e);
            }

            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mSelected = null;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mSelected != null) {
                        int dx = (int) (e.getX() - mInitialTouchX);
                        int dy = (int) (e.getY() - mInitialTouchY);
                        mSelected.itemView.setTranslationX(dx);
                        mSelected.itemView.setTranslationY(dy);
                    }
                    break;
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    @Override
    public void onChildViewAttachedToWindow(View view) {

    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {

    }

    private class BoardViewTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            RecyclerView childRecyclerView = findRecyclerView(e);
            View recyclerViewChild = findRecyclerViewChild(e);
            mInitialTouchX = e.getX();
            mInitialTouchY = e.getY();
            mSelected = childRecyclerView.getChildViewHolder(recyclerViewChild);
        }
    }

    /**
     * 找到当前触摸点所在的RecyclerView
     */
    private RecyclerView findRecyclerView(MotionEvent event) {
        View child = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
        return child.findViewById(R.id.recycler_view);
    }

    private int getCurrentColumnTitleHeight(MotionEvent event) {
        View child = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
        return child.findViewById(R.id.title).getHeight();
    }

    private View findRecyclerViewChild(MotionEvent event) {
        View child = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
        RecyclerView childRecyclerView = child.findViewById(R.id.recycler_view);
        int titleHeight = getCurrentColumnTitleHeight(event);
        return childRecyclerView.findChildViewUnder(event.getX() - child.getLeft(),
                event.getY() - titleHeight);
    }
}
