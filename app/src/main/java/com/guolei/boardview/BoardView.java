package com.guolei.boardview;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
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

import java.lang.reflect.Method;

public class BoardView extends FrameLayout {
    private static final String TAG = Provider.TAG;

    private static final int ACTION_BIND = 0;
    private static final int ACTION_UPDATE = 1;

    private View mMirrorView;
    private View mCurrentSelectedLayout;
    private RecyclerView mContentView;
    private RecyclerView mCurrentSelectedRecyclerView;

    private GestureDetectorCompat mGestureDetector;

    private RecyclerView.ViewHolder mSelected;

    private float mInitialTouchX, mInitialTouchY;
    private MotionEvent mCurrentTouchEvent, mLastMoveEvent, mLongPressEvent;

    private boolean mCanMove = true;

    private int mTitleHeight;
    private int mInsertPosition = -1;

    private int mTouchSlop;
    private int mScaledTouchSlop;

    private Callback mCallback;

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
    }

    private void initGestureDetector() {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetectorCompat(getContext(), new BoardViewGestureDetector());
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = new RecyclerView(getContext());
        mContentView.setLayoutParams(generateDefaultLayoutParams());
        mContentView.setLayoutManager(new SimpleLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mContentView.setHasFixedSize(true);
        addView(mContentView);
        RecyclerView.Adapter adapter = new BoardViewAdapter();
        mContentView.setAdapter(adapter);
        mContentView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mGestureDetector == null) {
                    return;
                }
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mGestureDetector.setIsLongpressEnabled(false);
                } else {
                    mGestureDetector.setIsLongpressEnabled(true);
                }

            }
        });
        ItemTouchHelper helper = new ItemTouchHelper(new BoardViewTouchCallback(adapter));
        helper.attachToRecyclerView(mContentView);
        LinearSnapHelper linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(mContentView);
        mMirrorView = new View(getContext());
        mMirrorView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mMirrorView.setVisibility(GONE);
        mMirrorView.setAlpha(.8f);
        addView(mMirrorView);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (landUpInlineRecyclerView(ev)) {
            mGestureDetector.onTouchEvent(ev);
        }
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
        if (landUpInlineRecyclerView(event)) {
            mGestureDetector.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mSelected != null) {
                    mCurrentTouchEvent = MotionEvent.obtain(event);
                    if (Provider.getInstance().isSmall()) {
                        mMirrorView.setTranslationX(event.getX() - mLongPressEvent.getX() + mInitialTouchX);
                        mMirrorView.setTranslationY(event.getY() - mLongPressEvent.getY() + mInitialTouchY);
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
                    mContentView.invalidate();
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
        if (mCurrentRecyclerView == null) {
            mContentView.removeCallbacks(mScrollRunnable);
            return;
        }
        mCurrentRecyclerView = mContentView.getChildAt(1).findViewById(R.id.recycler_view);
        int mCurrentRecyclerViewTop = mCurrentRecyclerView.getTop();
        int mCurrentRecyclerViewBottom = mCurrentRecyclerView.getBottom();

        int distanceTop = Math.abs((int) (mTitleHeight + mCurrentRecyclerViewTop
                - mCurrentTouchEvent.getY()));
        int distanceBottom = Math.abs((int) (mTitleHeight + mCurrentRecyclerViewBottom
                - mCurrentTouchEvent.getY()));
        int distanceLeft = (int) mCurrentTouchEvent.getX();
        int distanceRight = (int) (getWindowWidth() / Provider.getInstance().getFac()
                - mCurrentTouchEvent.getX());

        if ((distanceTop < distanceLeft && distanceTop < distanceRight)
                || (distanceBottom < distanceLeft && distanceBottom < distanceRight)) {
            mCurrentSelectedRecyclerView.removeCallbacks(mInlineScrollRunnable);
            mInlineScrollRunnable.run();
        } else {
            mContentView.removeCallbacks(mScrollRunnable);
            mScrollRunnable.run();
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
            LinearLayoutManager layoutManager = (LinearLayoutManager) mCurrentSelectedRecyclerView
                    .getLayoutManager();
            RecyclerView.ViewHolder target = mCurrentSelectedRecyclerView.getChildViewHolder(child);
            if (target != mSelected && !mCurrentSelectedRecyclerView.isAnimating() && mCanMove) {
                if (mCallback != null) {
                    int toPos = target.getAdapterPosition();
                    int position = mSelected.getAdapterPosition();
                    if (position == -1) {
                        ColumnAdapter adapter = (ColumnAdapter) mCurrentSelectedRecyclerView.getAdapter();
                        position = adapter.getPositionFromId();
                    }
                    if (position == -1) return;
                    mCallback.onMoved(mCurrentSelectedRecyclerView, position,
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
            String data = "error data";
            if (mCallback != null) {
                int pos = ((ColumnAdapter) mCurrentSelectedRecyclerView.getAdapter()).getPositionFromId();
                data = mCallback.onRemoved(mCurrentSelectedRecyclerView, pos);
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
            if (mCallback != null) {
                mCallback.onInserted(mCurrentSelectedRecyclerView, position, data);
                if (position == 0) {
                    Log.e(TAG, "updateAdapterIfNecessary: " + "Event没有落在View上，这种情况是错误的");
                }
            }
            mInsertPosition = position;
            //有时候会出现，删除动画。加上这句，能稍微改善下
            mSelected.itemView.setAlpha(0);
            final RecyclerView.ViewHolder tmpViewHolder = mCurrentSelectedRecyclerView
                    .findViewHolderForAdapterPosition(position);
            if (tmpViewHolder == null || tmpViewHolder.getAdapterPosition() != -1) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSelectedByInsert();
                    }
                }, 16);
                mCanMove = false;
            } else {
                mSelected = tmpHolder;
                select(mSelected, ACTION_UPDATE);
            }
        }
    }

    final Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSelected != null && scrollIfNecessary()) {
                if (mSelected != null) {
                    updateAdapterIfNecessary(mCurrentTouchEvent);
                }
                mContentView.removeCallbacks(mScrollRunnable);
                ViewCompat.postOnAnimation(mContentView, this);
            }
        }
    };

    /**
     * 滑动外层的RecyclerView
     */
    private boolean scrollIfNecessary() {
        int direction = getWindowWidth() - mCurrentTouchEvent.getX() > getWindowWidth() / 2 ? -1 : 1;
        if (!mContentView.canScrollHorizontally(direction)) {
            return false;
        }
        //边缘检测
        if (mContentView.getLeft() + 50 > mCurrentTouchEvent.getX()) {
            //在左
            mContentView.smoothScrollBy(-mScaledTouchSlop * 5, 0);
            return true;
        } else if (mContentView.getRight() - 50 < mCurrentTouchEvent.getX()) {
            //在右
            mContentView.smoothScrollBy(mScaledTouchSlop * 5, 0);
            return true;
        }
        return false;
    }

    final Runnable mInlineScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSelected != null && scrollInlineRVIfNecessary()) {
                if (mSelected != null) {
                    updateAdapterIfNecessary(mCurrentTouchEvent);
                }
                mCurrentSelectedRecyclerView.removeCallbacks(mInlineScrollRunnable);
                ViewCompat.postOnAnimation(mCurrentSelectedRecyclerView, this);
            }
        }
    };

    /**
     * 滑动内嵌的RecyclerView
     */
    private boolean scrollInlineRVIfNecessary() {
        int direction = mCurrentSelectedRecyclerView.getBottom() - (mCurrentTouchEvent.getY() - mTitleHeight)
                > mCurrentTouchEvent.getY() - mTitleHeight - mCurrentSelectedRecyclerView.getTop() ? -1 : 1;
        if (!mCurrentSelectedRecyclerView.canScrollVertically(direction)) return false;
        //边缘检测
        if (mCurrentSelectedRecyclerView.getTop() + mTitleHeight > mCurrentTouchEvent.getY()) {
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
                            mMirrorView.setVisibility(GONE);
                            mSelected.itemView.setAlpha(1);
                            mSelected.itemView.setVisibility(VISIBLE);
                            int posi = mSelected.getAdapterPosition();
                            if (posi == -1) {
                                // 这里是ViewHolder丢失位置信息的处理办法
                                ColumnAdapter adapter = (ColumnAdapter) mCurrentSelectedRecyclerView
                                        .getAdapter();
                                int position = adapter.getPositionFromId();
                                RecyclerView.ViewHolder tmpViewHolder = mCurrentSelectedRecyclerView
                                        .findViewHolderForAdapterPosition(position);
                                if (tmpViewHolder != null) {
                                    tmpViewHolder.itemView.setVisibility(VISIBLE);
                                } else {
                                    mCurrentSelectedRecyclerView.getAdapter().notifyItemChanged(position);
                                }
                            } else {
                                mCurrentSelectedRecyclerView.getAdapter().notifyItemChanged(posi);
                            }
                            Provider.getInstance().setSelectedId(RecyclerView.NO_ID);
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

    private void select(RecyclerView.ViewHolder selected, int actionState) {
        if (selected != null && selected != mSelected) {
            if (mSelected != null) {
                mSelected.itemView.setVisibility(VISIBLE);
            }
            mSelected = selected;
            int position = mSelected.getAdapterPosition();
            ColumnAdapter adapter = (ColumnAdapter) mCurrentSelectedRecyclerView.getAdapter();
            long id = adapter.getIdByPosition(position);
            Provider.getInstance().setSelectedId(id);
            if (actionState == ACTION_BIND) {
                onBindSelected(mSelected.itemView);
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void onBindSelected(View selectedView) {
        //先设置偏移量
        int x = mCurrentSelectedLayout.getLeft() + selectedView.getLeft();
        int y = mCurrentSelectedLayout.getTop() + selectedView.getTop() + mTitleHeight;
        ViewGroup.LayoutParams params = mMirrorView.getLayoutParams();
        params.width = selectedView.getWidth();
        params.height = selectedView.getHeight();
        mMirrorView.setLayoutParams(params);
        mMirrorView.setTranslationX(x);
        mMirrorView.setTranslationY(y);
        Bitmap bitmap = Bitmap.createBitmap(selectedView.getWidth(), selectedView.getHeight(), Bitmap.Config.RGB_565);
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

    private boolean landUpInlineRecyclerView(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        View view = mContentView.findChildViewUnder(x, y);
        if (view == null) {
            view = findChildViewUnderWithInsets(mContentView, x, y);
        }
        if (view == null) {
            return false;
        }
        View recyclerView = view.findViewById(R.id.recycler_view);
        return y > recyclerView.getTop() && y < recyclerView.getBottom();
    }

    /**
     * 找到当前触摸点所在的RecyclerView
     */
    private RecyclerView findRecyclerView(MotionEvent event) {
        View child = mContentView.findChildViewUnder(event.getX(), event.getY());
        if (child == null) {
            return null;
        }
        return child.findViewById(R.id.recycler_view);
    }

    private View findRecyclerViewChild(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        View child = mContentView.findChildViewUnder(x, y);
        mCurrentSelectedLayout = child;
        mCurrentSelectedRecyclerView = child.findViewById(R.id.recycler_view);
        int titleHeight = getCurrentColumnTitleHeight(event);
        View view = mCurrentSelectedRecyclerView.findChildViewUnder(x - child.getLeft(),
                y - titleHeight);
        if (view == null) {
            view = findChildViewUnderWithInsets(mCurrentSelectedRecyclerView, x - mCurrentSelectedLayout.getLeft(),
                    y - mCurrentSelectedLayout.getTop());
        }
        return view;
    }

    private int getCurrentColumnTitleHeight(MotionEvent event) {
        if (mTitleHeight == 0) {
            View child = mContentView.findChildViewUnder(event.getX(), event.getY());
            mTitleHeight = child.findViewById(R.id.title).getHeight();
        }
        return mTitleHeight;
    }

    private void updateSelectedByInsert() {
        RecyclerView.ViewHolder viewHolder = mCurrentSelectedRecyclerView.findViewHolderForAdapterPosition(mInsertPosition);
        if (viewHolder != null) {
            if (viewHolder.getAdapterPosition() != -1) {
                select(viewHolder, ACTION_UPDATE);
                mCanMove = true;
            } else {
                Log.e(TAG, "updateSelectedByInsert: error " + viewHolder.getAdapterPosition());
            }
        }
    }

    private int mWindowHeight;

    @SuppressWarnings("unused")
    private int getWindowHeight() {
        if (mWindowHeight == 0) {
            mWindowHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        }
        return mWindowHeight;
    }

    private int mWindowWidth;

    private int getWindowWidth() {
        if (mWindowWidth == 0) {
            mWindowWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        }
        return mWindowWidth;
    }

    public void scale(boolean lessen) {
        if (lessen) {
            //缩小
            Provider.getInstance().setSmall(true);
        } else {
            //还原
            Provider.getInstance().setSmall(false);
        }
        // TODO: 18/3/7 缩小后 还原，高度问题，这里计算的不对
        float scale = Provider.getInstance().getFac();
        View rootView = (View) getParent();
        rootView.getLayoutParams().width = (int) (getWindowWidth() * (1 / scale));
        rootView.getLayoutParams().height = (int) (rootView.getHeight() * (1 / scale));
        setScaleX(scale);
        setScaleY(scale);
        setPivotX(0f);
        setPivotY(0f);
        requestLayout();
        for (int i = 0; i < mContentView.getChildCount(); i++) {
            View child = mContentView.getChildAt(i);
            child.requestLayout();
        }
    }

    private class BoardViewGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mContentView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE
                    || mSelected != null) {
                return;
            }
            mLastMoveEvent = e;
            RecyclerView childRecyclerView = findRecyclerView(e);
            if (childRecyclerView == null) return;
            View recyclerViewChild = findRecyclerViewChild(e);
            if (recyclerViewChild == null) {
                recyclerViewChild = findChildViewUnderWithInsets(childRecyclerView, e.getX(), e.getY());
            }
            if (recyclerViewChild == null) return;
            select(childRecyclerView.getChildViewHolder(recyclerViewChild), 0);
            if (mSelected != null) {
                //左右边界情况
                Rect rect = new Rect();
                mSelected.itemView.getGlobalVisibleRect(rect);
                int width = (int) (mSelected.itemView.getWidth() * Provider.getInstance().getFac());
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
            if (Provider.getInstance().isSmall()) {
                mLongPressEvent = MotionEvent.obtain(e);
                mInitialTouchX = mMirrorView.getTranslationX();
                mInitialTouchY = mMirrorView.getTranslationY();
            }
        }

    }


    private Method getItemDecorInsetsForChildMethod;

    {
        try {
            getItemDecorInsetsForChildMethod = RecyclerView.class.getDeclaredMethod("getItemDecorInsetsForChild", View.class);
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
                Log.e(TAG, "findChildViewUnderWithInsets: " + rect.toString());
                return child;
            }
        }
        return null;
    }

    public interface Callback {
        void onMoved(RecyclerView recyclerView, int from, int to);

        String onRemoved(RecyclerView recyclerView, int position);

        void onInserted(RecyclerView recyclerView, int position, String data);
    }
}
