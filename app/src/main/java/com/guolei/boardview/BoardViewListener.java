package com.guolei.boardview;
//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.support.v7.widget.RecyclerView;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/4/25
 * Time: 下午3:22
 * Desc:
 */
public interface BoardViewListener<T> extends ItemTouchHelper.ResponseEventListener {

    default int getPxInColumn() {
        return 3 * 8;
    }

    int getColumnRecyclerViewId();

    void onSwap(RecyclerView recyclerView, int from, int to);

    T getData(RecyclerView recyclerView, int position);

    T onRemove(RecyclerView recyclerView, int position);

    void onInsert(RecyclerView recyclerView, int position, T data);

    void onReleaseRow(int fromColumnIndex, int fromRowIndex, int toColumnIndex, int toRowIndex);

    void onReleaseColumn(int from, int to);

    /**
     * 是否允许被选中
     */
    boolean isEnableSelectColumn(int column);

    /**
     * 是否支持列内交换
     */
    boolean isEnableSwapInColumn(int column);

    /**
     * 是否支持插入数据
     */
    boolean isEnableInsertRowInColumn(int column, T t);

    /**
     * 是否支持row被选中
     */
    boolean isEnableSelectRow(int column, int row);

}
