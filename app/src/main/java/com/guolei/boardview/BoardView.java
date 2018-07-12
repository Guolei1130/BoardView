package com.guolei.boardview;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/2/27
 * Time: 下午4:21
 * Desc:
 */
@SuppressWarnings("unused")
public class BoardView extends FrameLayout {
    private static final String TAG = BoardView.class.getSimpleName();

    private static final int ACTION_BIND = 0;
    private static final int ACTION_UPDATE = 1;

    private View mMirrorView;
    private RecyclerView mContentView;
    private RecyclerView mCurrentSelectedRecyclerView;
    private View mCurrentSelectedLayout;
    private GestureDetectorCompat mGestureDetector;

    private RecyclerView.ViewHolder mSelected;
    private float mInitialTouchX, mInitialTouchY;
    private MotionEvent mCurrentTouchEvent, mLastMoveEvent, mLongPressEvent;

    private boolean mCanMove = true;

    private int mColumnHeaderHeight;
    private int mInsertPosition = RecyclerView.NO_POSITION;

    private int mTouchSlop, mScaledTouchSlop;
    private int mParentHeight, mParentWidth;

    private int mOriginColumnIndex, mOriginRowIndex, mTargetColumnIndex, mTargetRowIndex;

    private BoardViewListener mBoardViewListener;
    private BoardViewStateHolder mBoardViewHolder;

    public BoardView(@NonNull Context context) {
        this(context, null);
    }

    public BoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        //noinspection deprecation
        mTouchSlop = ViewConfiguration.getTouchSlop();
        mScaledTouchSlop = viewConfiguration.getScaledTouchSlop();
        initGestureDetector();
        mBoardViewHolder = new BoardViewStateHolder();
    }

    private void initGestureDetector() {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetectorCompat(getContext(), new BoardViewGestureDetector());
        }
    }

    public void setListener(BoardViewListener listener) {
        mBoardViewListener = listener;
        mBoardViewHolder.setBoardViewListener(listener);
        mContentView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(listener.getPxInColumn(), 0, listener.getPxInColumn(), 0);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = new RecyclerView(getContext());
        mContentView.setLayoutParams(generateDefaultLayoutParams());
        mContentView.setLayoutManager(new SimpleLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false));
        mContentView.setHasFixedSize(true);
        addView(mContentView);
        mContentView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mGestureDetector == null) {
                    return;
                }
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mGestureDetector.setIsLongpressEnabled(false);
                    removeLongPressMessage();
                } else {
                    mGestureDetector.setIsLongpressEnabled(true);
                }

            }
        });

        PagerSnapHelper linearSnapHelper = new PagerSnapHelper();
        linearSnapHelper.attachToRecyclerView(mContentView);
        mMirrorView = new View(getContext());
        mMirrorView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mMirrorView.setVisibility(GONE);
        mMirrorView.setAlpha(.8f);
        addView(mMirrorView);


    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mContentView.setAdapter(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(new BoardViewTouchCallback(mBoardViewHolder));
        helper.attachToRecyclerView(mContentView);
        helper.setResponseEventListener(mBoardViewListener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (landupInnerRecyclerView(ev)) {
            mGestureDetector.setIsLongpressEnabled(true);
        } else {
            mGestureDetector.setIsLongpressEnabled(false);
        }
        mGestureDetector.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                recoverSelected();
                break;
        }
        return mSelected != null || super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (landupInnerRecyclerView(event)) {
            mGestureDetector.setIsLongpressEnabled(true);
        } else {
            mGestureDetector.setIsLongpressEnabled(false);
        }
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mSelected != null) {
                    mCurrentTouchEvent = MotionEvent.obtain(event);
                    if (mBoardViewHolder.isInSmallMode()) {
                        mMirrorView.setTranslationX(event.getX() - mLongPressEvent.getX()
                                + mInitialTouchX);
                        mMirrorView.setTranslationY(event.getY() - mLongPressEvent.getY()
                                + mInitialTouchY);
                    } else {
                        mMirrorView.setTranslationX(event.getX() - mInitialTouchX);
                        mMirrorView.setTranslationY(event.getY() - mInitialTouchY);
                    }
                    if (mLastMoveEvent != null && (Math.abs(mLastMoveEvent.getY() - event.getY()) > mTouchSlop
                            || Math.abs(mLastMoveEvent.getX() - event.getX()) > mTouchSlop)) {
                        mLastMoveEvent = MotionEvent.obtain(event);
                        updateAdapterIfNecessary(event);
                        //判断是需要进行横向欢动还是纵向滑动
                    }
                    selectScroll();
                    //mContentView.invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                recoverSelected();
                break;
        }
        return mSelected != null || super.onTouchEvent(event);
    }

    /**
     * 选择一个滑动方向
     */
    private void selectScroll() {
        /*
         *  1.找到落点所在的RecyclerView
         */
        RecyclerView mCurrentRecyclerView = findRecyclerView(mCurrentTouchEvent);
        // 如果落点在RecyclerView的下面，这种情况下是找不到RecyclerView的，
        if (mCurrentRecyclerView == null) {
            mCurrentRecyclerView = findRecyclerViewWithoutY(mCurrentTouchEvent);
            if (mCurrentRecyclerView == null) {
                mContentView.removeCallbacks(mContentViewRunnable);
                return;
            }
        }
//        mCurrentRecyclerView = mContentView.getChildAt(1).findViewById(mBoardViewListener.getColumnRecyclerViewId());
        int mCurrentRecyclerViewTop = mCurrentRecyclerView.getTop();
        int mCurrentRecyclerViewBottom = mCurrentRecyclerView.getBottom();

        float distanceTop = Math.abs((int) (mColumnHeaderHeight + mCurrentRecyclerViewTop
                - mCurrentTouchEvent.getY()));
        float distanceBottom = Math.abs((int) (mColumnHeaderHeight + mCurrentRecyclerViewBottom
                - mCurrentTouchEvent.getY()));
        float distanceLeft = mCurrentTouchEvent.getX();
        float distanceRight = (getWindowWidth() / mBoardViewHolder.getScaleFactor()
                - mCurrentTouchEvent.getX());

        if ((distanceTop < distanceLeft && distanceTop < distanceRight)
                || (distanceBottom < distanceLeft && distanceBottom < distanceRight)) {
            mCurrentSelectedRecyclerView.removeCallbacks(mInnerRecyclerViewRunnable);
            mInnerRecyclerViewRunnable.run();
        } else {
            mContentView.removeCallbacks(mContentViewRunnable);
            mContentViewRunnable.run();
        }
    }

    private void updateAdapterIfNecessary(MotionEvent event) {
        if (mSelected == null) return;
        //分为两种情况，1 在mSelected所在的RecyclerView与当前点所能查找到的的RecyclerView是一个
        RecyclerView targetRecycler = findRecyclerView(event);
        if (targetRecycler == mCurrentSelectedRecyclerView) {
            // 同一个RecyclerView
            View child = findRecyclerViewChild(event);
            if (child == null) {
                return;
            }
            LinearLayoutManager layoutManager = (LinearLayoutManager) mCurrentSelectedRecyclerView.getLayoutManager();
            RecyclerView.ViewHolder target = mCurrentSelectedRecyclerView.getChildViewHolder(child);
            int currentColumn = mContentView.getChildViewHolder(mCurrentSelectedLayout).getAdapterPosition();
            if (!mBoardViewHolder.getBoardViewListener().isEnableSwapInColumn(currentColumn)) {
                return;
            }
            if (target != mSelected && !mCurrentSelectedRecyclerView.isAnimating() && mCanMove) {
                if (mBoardViewListener != null) {
                    int toPos = target.getAdapterPosition();
                    int position = mSelected.getAdapterPosition();
                    if (position == -1) {
                        AbsBoardViewAdapter adapter = (AbsBoardViewAdapter) mCurrentSelectedRecyclerView.getAdapter();
                        position = adapter.getPositionFromId(mBoardViewHolder.getSelectedId());
                    }
                    if (position == -1) return;
                    mBoardViewListener.onSwap(mCurrentSelectedRecyclerView, position,
                            toPos);
                    /*
                     * 保持RecyclerView不发生移动
                     * {@link android.support.v7.widget.helper.ItemTouchHelper#onMoved}
                     */
                    //noinspection RedundantCast
                    ((android.support.v7.widget.helper.ItemTouchHelper.ViewDropHandler) layoutManager)
                            .prepareForDrop(mSelected.itemView, target.itemView,
                                    (int) event.getX(), (int) event.getY());
                    return;
                }
            }
        }

        // 跨RecyclerView
        if (targetRecycler != null && targetRecycler != mCurrentSelectedRecyclerView) {
            Object data = null;
            boolean enableInsert;
            if (mBoardViewListener != null) {
                int pos = ((AbsBoardViewAdapter) mCurrentSelectedRecyclerView.getAdapter())
                        .getPositionFromId(mBoardViewHolder.getSelectedId());
                data = mBoardViewListener.getData(mCurrentSelectedRecyclerView, pos);
                //noinspection unchecked
                enableInsert = mBoardViewListener.isEnableInsertRowInColumn(BoardViewHelper.findParentIndex(targetRecycler), data);
                if (!enableInsert) {
                    return;
                }
                data = mBoardViewListener.onRemove(mCurrentSelectedRecyclerView, pos);
            }

            mCurrentSelectedRecyclerView = targetRecycler;
            //找到落点所在的位置
            View view = findRecyclerViewChild(event);
            int position = 0;
            RecyclerView.ViewHolder tmpHolder = null;
            if (view != null) {
                tmpHolder = mCurrentSelectedRecyclerView.getChildViewHolder(view);
                position = tmpHolder.getAdapterPosition();
            }
            if (mBoardViewListener != null) {
                //noinspection unchecked
                mBoardViewListener.onInsert(mCurrentSelectedRecyclerView, position, data);
                if (position == 0) {
                    Log.d(TAG, "updateAdapterIfNecessary: " + "Event没有落在View上，这种情况是错误的");
//                    throw new IllegalStateException(event.toString());
                }
            }
            mInsertPosition = position;
            //有时候会出现，删除动画。加上这句，能稍微改善下
            mSelected.itemView.setAlpha(0);
            final RecyclerView.ViewHolder tmpViewHolder1 = mCurrentSelectedRecyclerView
                    .findViewHolderForAdapterPosition(position);
            if (tmpViewHolder1 == null || tmpViewHolder1.getAdapterPosition() != -1) {
                postDelayed(this::updateSelectedByInsert, 16);
                mCanMove = false;
            } else {
                mSelected = tmpHolder;
                select(mSelected, ACTION_UPDATE);
            }
        }
    }

    final Runnable mContentViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSelected != null && scrollIfNecessary()) {
                if (mSelected != null) {
                    updateAdapterIfNecessary(mCurrentTouchEvent);
                }
                mContentView.removeCallbacks(mContentViewRunnable);
                ViewCompat.postOnAnimation(mContentView, this);
            }
        }
    };

    /**
     * 滑动外层的RecyclerView
     */
    private boolean scrollIfNecessary() {
        int direction = getWindowWidth() - mCurrentTouchEvent.getX() > getWindowWidth() / 2 ? -1 : 1;
        if (!mContentView.canScrollHorizontally(direction)) return false;
        //边缘检测
        if (mContentView.getLeft() + 150 > mCurrentTouchEvent.getX()) {
            //在左
            mContentView.smoothScrollBy(-mScaledTouchSlop * 8, 0);
            return true;
        } else if (mContentView.getRight() - 150 < mCurrentTouchEvent.getX()) {
            //在右
            mContentView.smoothScrollBy(mScaledTouchSlop * 8, 0);
            return true;
        }
        return false;
    }

    final Runnable mInnerRecyclerViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSelected != null && scrollInnerRecyclerViewIfNecessary()) {
                if (mSelected != null) {
                    updateAdapterIfNecessary(mCurrentTouchEvent);
                }
                mCurrentSelectedRecyclerView.removeCallbacks(mInnerRecyclerViewRunnable);
                ViewCompat.postOnAnimation(mCurrentSelectedRecyclerView, this);
            }
        }
    };

    /**
     * 滑动内嵌的RecyclerView
     */
    private boolean scrollInnerRecyclerViewIfNecessary() {
        int direction = mCurrentSelectedRecyclerView.getBottom() - (mCurrentTouchEvent.getY() - mColumnHeaderHeight)
                > mCurrentTouchEvent.getY() - mColumnHeaderHeight - mCurrentSelectedRecyclerView.getTop() ? -1 : 1;
        if (!mCurrentSelectedRecyclerView.canScrollVertically(direction)) return false;
        //边缘检测
        if (mCurrentSelectedRecyclerView.getTop() + mColumnHeaderHeight > mCurrentTouchEvent.getY()) {
            mCurrentSelectedRecyclerView.smoothScrollBy(0, -mScaledTouchSlop * 5);
            return true;
        } else if (mCurrentSelectedRecyclerView.getBottom() - 50 < mCurrentTouchEvent.getY()) {
            mCurrentSelectedRecyclerView.smoothScrollBy(0, mScaledTouchSlop * 5);
            return true;
        }
        return false;
    }


    private void recoverSelected() {
        mLastMoveEvent = null;
        mCanMove = true;
        if (mMirrorView != null && mSelected != null) {
            //x方向的偏移量
            float diffX = mCurrentSelectedLayout.getLeft() + mSelected.itemView.getLeft() + getScrollX()
                    - (mMirrorView.getLeft() + mMirrorView.getTranslationX());
            //y方向的偏移量
            float diffY = mSelected.itemView.getTop() + mCurrentSelectedRecyclerView.getTop()
                    - mMirrorView.getTranslationY();
            mMirrorView.animate().cancel();
            mMirrorView.animate()
                    .setDuration(200)
                    .rotation(0)
                    .translationXBy(diffX)
                    .translationYBy(diffY)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            // no op
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (mSelected == null) {
                                return;
                            }
                            mTargetColumnIndex = mContentView.getChildViewHolder((View) mCurrentSelectedRecyclerView.getParent())
                                    .getAdapterPosition();
                            mTargetRowIndex = mSelected.getAdapterPosition();
                            if (mBoardViewListener != null) {
                                mBoardViewListener.onReleaseRow(mOriginColumnIndex, mOriginRowIndex,
                                        mTargetColumnIndex, mTargetRowIndex);
                                RecyclerView.ViewHolder holder = mCurrentSelectedRecyclerView.findViewHolderForAdapterPosition(mTargetColumnIndex);
                                if (holder != null && holder.itemView != null) {
                                    holder.itemView.setAlpha(1);
                                    holder.itemView.setVisibility(VISIBLE);
                                }
                            }
                            mMirrorView.setVisibility(GONE);
                            mSelected.itemView.setAlpha(1);
                            mSelected.itemView.setVisibility(VISIBLE);
//                            final int posi = mSelected.getAdapterPosition();
//                            if (posi == -1) {
//                                // 这里是ViewHolder丢失位置信息的处理办法
//                                ColumnAdapter adapter = (ColumnAdapter) mCurrentSelectedRecyclerView.getAdapter();
//                                final int position = adapter.getPositionFromId();
//                                RecyclerView.ViewHolder tmpViewHolder = mCurrentSelectedRecyclerView
//                                        .findViewHolderForAdapterPosition(position);
//                                if (tmpViewHolder != null) {
//                                    tmpViewHolder.itemView.setVisibility(VISIBLE);
//                                } else {
//                                    postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            mCurrentSelectedRecyclerView.getAdapter().notifyItemChanged(position);
//                                        }
//                                    }, 300);
//                                    Log.e(TAG, "onAnimationEnd: ");
//                                }
//                            } else {
//                                postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        mCurrentSelectedRecyclerView.getAdapter().notifyItemChanged(posi);
//                                    }
//                                }, 300);
//                                Log.e(TAG, "onAnimationEnd: ");
//                            }
                            mBoardViewHolder.setSelectedId("");
                            mSelected = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            // no op
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                            // no op
                        }
                    })
                    .start();
        }
    }

    private boolean landupInnerRecyclerView(MotionEvent event) {
        View view = mContentView.findChildViewUnder(event.getX(), event.getY());
        if (view == null) {
            view = findChildViewUnderWithInsets(mContentView, event.getX(), event.getY());
        }
        if (view == null) return false;
        int y = (int) event.getY();
        View recyclerView = view.findViewById(mBoardViewListener.getColumnRecyclerViewId());
        return recyclerView != null && y > recyclerView.getTop() && y < recyclerView.getBottom();
    }

    private void select(RecyclerView.ViewHolder selected, int actionState) {
        if (selected != null && selected != mSelected) {
            if (mSelected != null) {
                mSelected.itemView.setVisibility(VISIBLE);
            }
            mSelected = selected;
            int position = mSelected.getAdapterPosition();
            AbsBoardViewAdapter adapter = (AbsBoardViewAdapter) mCurrentSelectedRecyclerView.getAdapter();
            String id = adapter.getIdFromPosition(position);
            mBoardViewHolder.setSelectedId(id);
            if (actionState == ACTION_BIND) {
                onBindSelected(mSelected.itemView);
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void onBindSelected(View selectedView) {
        //先设置偏移量
        int x = mCurrentSelectedLayout.getLeft() + selectedView.getLeft();
        int y = mCurrentSelectedLayout.getTop() + selectedView.getTop() + mColumnHeaderHeight;
        ViewGroup.LayoutParams params = mMirrorView.getLayoutParams();
        params.width = selectedView.getWidth();
        params.height = selectedView.getHeight();
        mMirrorView.setLayoutParams(params);
        mMirrorView.setTranslationX(x);
        mMirrorView.setTranslationY(y);
        Bitmap bitmap = Bitmap.createBitmap(selectedView.getWidth(), selectedView.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        selectedView.draw(canvas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mMirrorView.setBackground(new BitmapDrawable(selectedView.getResources(), bitmap));
        } else {
            //noinspection deprecation
            mMirrorView.setBackgroundDrawable(new BitmapDrawable(selectedView.getResources(), bitmap));
        }
        mMirrorView.setVisibility(VISIBLE);
        mMirrorView.setRotation(-5f);
        mSelected.itemView.setAlpha(0);
    }

    /**
     * 找到当前触摸点所在的RecyclerView
     */
    private RecyclerView findRecyclerView(MotionEvent event) {
        View child = mContentView.findChildViewUnder(event.getX(), event.getY());
        if (child == null) {
            return null;
        }
        return child.findViewById(mBoardViewListener.getColumnRecyclerViewId());
    }

    /**
     * 找到当前触点所在的RecyclerView,不一定落在RecyclerView上.
     */
    private RecyclerView findRecyclerViewWithoutY(MotionEvent event) {
        float x = event.getX();
        for (int i = 0; i < mContentView.getChildCount(); i++) {
            View child = mContentView.getChildAt(i);
            if (child.getLeft() <= x && child.getRight() >= x) {
                return child.findViewById(mBoardViewListener.getColumnRecyclerViewId());
            }
        }
        return null;
    }

    private int getCurrentColumnTitleHeight(MotionEvent event) {
        if (mColumnHeaderHeight != 0)
            return mColumnHeaderHeight;
        View child = mContentView.findChildViewUnder(event.getX(), event.getY());
        return mColumnHeaderHeight = child.findViewById(mBoardViewListener.getResponseViewId()).getHeight();
    }

    private View findRecyclerViewChild(MotionEvent event) {
        View child = mContentView.findChildViewUnder(event.getX(), event.getY());
        mCurrentSelectedLayout = child;
        mCurrentSelectedRecyclerView = child.findViewById(mBoardViewListener.getColumnRecyclerViewId());
        int titleHeight = getCurrentColumnTitleHeight(event);
        View view = mCurrentSelectedRecyclerView.findChildViewUnder(event.getX() - child.getLeft(),
                event.getY() - titleHeight);
        if (view == null) {
            view = findChildViewUnderWithInsets(mCurrentSelectedRecyclerView,
                    event.getX() - mCurrentSelectedLayout.getLeft(),
                    event.getY() - mCurrentSelectedLayout.getTop());
        }
        return view;
    }

    private void updateSelectedByInsert() {
        RecyclerView.ViewHolder viewHolder = mCurrentSelectedRecyclerView
                .findViewHolderForAdapterPosition(mInsertPosition);
        if (viewHolder != null) {
            if (viewHolder.getAdapterPosition() != -1) {
                select(viewHolder, ACTION_UPDATE);
                mCanMove = true;
            } else {
                Log.d(TAG, "updateSelectedByInsert: error " + viewHolder.getAdapterPosition());
            }
        }
    }

    @SuppressWarnings("unused")
    private int getWindowHeight() {
        return getContext().getResources().getDisplayMetrics().heightPixels;
    }

    private int getWindowWidth() {
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }

    public void scale() {
        boolean lessen = !mBoardViewHolder.isInSmallMode();
        if (lessen) {
            //缩小
            mBoardViewHolder.setInSmallMode(true);
        } else {
            //还原
            mBoardViewHolder.setInSmallMode(false);
        }
        float scale = mBoardViewHolder.getScaleFactor();
        View rootView = (View) getParent();
        if (lessen && mParentHeight == 0) {
            mParentHeight = rootView.getHeight();
            mParentWidth = rootView.getWidth();
        }
        if (lessen) {
            rootView.getLayoutParams().width = (int) (mParentWidth * (1 / scale));
            rootView.getLayoutParams().height = (int) (mParentHeight * (1 / scale));
        } else {
            rootView.getLayoutParams().width = -1;
            rootView.getLayoutParams().height = -1;
        }
        setPivotX(0f);
        setPivotY(0f);
        setScaleX(scale);
        setScaleY(scale);
        rootView.requestLayout();
        for (int i = 0; i < mContentView.getChildCount(); i++) {
            View child = mContentView.getChildAt(i);
            child.requestLayout();
        }
    }

    private class BoardViewGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (findRecyclerView(e) == null) {
                scale();
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.e(TAG, "onLongPress: ");
            if (mContentView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                return;
            }
            if (mSelected != null || e.getAction() != MotionEvent.ACTION_DOWN) {
                return;
            }
            mLastMoveEvent = e;
            RecyclerView childRecyclerView = findRecyclerView(e);
            if (childRecyclerView == null || childRecyclerView.getScrollState()
                    != RecyclerView.SCROLL_STATE_IDLE) {
                return;
            }
            RecyclerView.ViewHolder columnViewHolder = mContentView
                    .getChildViewHolder((View) childRecyclerView.getParent());
            mOriginColumnIndex = columnViewHolder.getAdapterPosition();
            View recyclerViewChild = findRecyclerViewChild(e);
            if (recyclerViewChild == null) {
                recyclerViewChild = findChildViewUnderWithInsets(childRecyclerView,
                        e.getX(), e.getY());
            }
            if (recyclerViewChild == null) {
                return;
            }
            RecyclerView.ViewHolder itemViewHolder = childRecyclerView.getChildViewHolder(recyclerViewChild);
            mOriginRowIndex = itemViewHolder.getAdapterPosition();
            if (!mBoardViewHolder.getBoardViewListener().isEnableSelectRow(mOriginColumnIndex, mOriginRowIndex)) {
                return;
            }
            select(itemViewHolder, 0);
            if (mSelected != null) {
                //左右边界情况
                Rect rect = new Rect();
                mSelected.itemView.getGlobalVisibleRect(rect);
                int width = (int) (mSelected.itemView.getWidth() * mBoardViewHolder.getScaleFactor());
                if (Math.abs(rect.left - rect.right) < width) {
                    if (rect.right + width > getWindowWidth()) {
                        //右侧
                        mInitialTouchX = e.getRawX() - rect.left;
                    } else {
                        mInitialTouchX = width - (rect.right - e.getRawX());
                    }
                    mInitialTouchY = e.getRawY() - rect.top;
                } else {
                    mInitialTouchX = e.getRawX() - rect.left;
                    mInitialTouchY = e.getRawY() - rect.top;
                }
            } else {
                mInitialTouchX = e.getX();
                mInitialTouchY = e.getY();
            }
            //缩小状态下，上面的方案不行，采用下面的方案
            if (mBoardViewHolder.isInSmallMode()) {
                mLongPressEvent = MotionEvent.obtain(e);
                mInitialTouchX = mMirrorView.getTranslationX();
                mInitialTouchY = mMirrorView.getTranslationY();
            }
        }

    }

    private Method getItemDecorInsetsForChildMethod;

    {
        try {
            getItemDecorInsetsForChildMethod = RecyclerView.class
                    .getDeclaredMethod("getItemDecorInsetsForChild", View.class);
            getItemDecorInsetsForChildMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加上ItemDecoration的位置
     */
    private View findChildViewUnderWithInsets(RecyclerView recyclerView, float x, float y) {
        final int count = recyclerView.getChildCount();
        Rect rect = new Rect(0, 0, 0, 0);
        for (int i = count - 1; i >= 0; i--) {
            final View child = recyclerView.getChildAt(i);
            final float translationX = child.getTranslationX();
            final float translationY = child.getTranslationY();
            try {
                rect = (Rect) getItemDecorInsetsForChildMethod.invoke(recyclerView, child);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (x >= child.getLeft() + translationX - rect.left - 30
                    && x <= child.getRight() + translationX + rect.right + 30
                    && y >= child.getTop() + translationY - rect.top
                    && y <= child.getBottom() + translationY + rect.bottom) {
                return child;
            }
        }
        return null;
    }

    public void removeLongPressMessage() {
        try {
            Field impl = mGestureDetector.getClass().getDeclaredField("mImpl");
            impl.setAccessible(true);
            Object gestureDetectorCompatImpl = impl.get(mGestureDetector);
            if (gestureDetectorCompatImpl.getClass().getSimpleName()
                    .equals("GestureDetectorCompatImplJellybeanMr2")) {
                Class gestureDetectorCompatImplJellybeanMr2Class = Class
                        .forName("android.support.v4.view.GestureDetectorCompat" +
                                "$GestureDetectorCompatImplJellybeanMr2");
                Field detectorField = gestureDetectorCompatImplJellybeanMr2Class
                        .getDeclaredField("mDetector");
                detectorField.setAccessible(true);
                Object gestureDetector = detectorField.get(gestureDetectorCompatImpl);
                Field handlerField = gestureDetector.getClass().getDeclaredField("mHandler");
                handlerField.setAccessible(true);
                Handler handler = (Handler) handlerField.get(gestureDetector);
                handler.removeMessages(2);
            } else {
                Class gestureDetectorCompatImplBaseClass = Class
                        .forName("android.support.v4.view.GestureDetectorCompat" +
                                "$GestureDetectorCompatImplBase");
                Field handlerField = gestureDetectorCompatImplBaseClass
                        .getDeclaredField("mHandler");
                handlerField.setAccessible(true);
                Handler handler = (Handler) handlerField.get(gestureDetectorCompatImpl);
                handler.removeMessages(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "removeLongPressMessage: ");
        }
    }

    public BoardViewStateHolder getBoardViewHolder() {
        return mBoardViewHolder;
    }

    public RecyclerView getContentView() {
        return mContentView;
    }

}
