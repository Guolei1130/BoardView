package com.guolei.boardview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Copyright © 2013-2018 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/7/9
 * Time: 下午9:33
 * Desc:
 */
public abstract class BaseBoardViewListener<T> implements BoardViewListener<T>{
    private static final String TAG = "BoardView";

    @Override
    public void onSwap(RecyclerView recyclerView, int from, int to) {
        // 调用太频繁导致失效？
        if (from == to) {
            return;
        }
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof AbsBoardViewAdapter) {
            ((AbsBoardViewAdapter) adapter).swap(from, to);
        }
        adapter.notifyItemMoved(from, to);
        Log.e(TAG, "onSwap: " + from + ";" + to);
    }

    @Override
    public T getData(RecyclerView recyclerView, int position) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        T data = null;
        if (adapter instanceof AbsBoardViewAdapter) {
            //noinspection unchecked
            data = (T) ((AbsBoardViewAdapter) adapter).getData(position);
        }
        return data;
    }

    @Override
    public T onRemove(RecyclerView recyclerView, int position) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        T data = null;
        if (adapter instanceof AbsBoardViewAdapter) {
            //noinspection unchecked
            data = (T) ((AbsBoardViewAdapter) adapter).remove(position);
        }
        adapter.notifyItemRemoved(position);
        return data;
    }

    @Override
    public void onInsert(RecyclerView recyclerView, int position, T data) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof AbsBoardViewAdapter) {
            //noinspection unchecked
            ((AbsBoardViewAdapter) adapter).add(position, data);
        }
        adapter.notifyItemInserted(position);
    }
}
