package com.guolei.boardview;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


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
        if (adapter instanceof BaseBoardViewAdapter) {
            ((BaseBoardViewAdapter) adapter).swap(from, to);
        }
        adapter.notifyItemMoved(from, to);
        Log.e(TAG, "onSwap: " + from + ";" + to);
    }

    @Override
    public T getData(RecyclerView recyclerView, int position) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        T data = null;
        if (adapter instanceof BaseBoardViewAdapter) {
            data = (T) ((BaseBoardViewAdapter) adapter).getData(position);
        }
        return data;
    }

    @Override
    public T onRemove(RecyclerView recyclerView, int position) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        T data = null;
        if (adapter instanceof BaseBoardViewAdapter) {
            data = (T) ((BaseBoardViewAdapter) adapter).remove(position);
        }
        adapter.notifyItemRemoved(position);
        return data;
    }

    @Override
    public void onInsert(RecyclerView recyclerView, int position, T data) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof BaseBoardViewAdapter) {
            ((BaseBoardViewAdapter) adapter).add(position, data);
        }
        adapter.notifyItemInserted(position);
    }
}
