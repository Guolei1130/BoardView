package com.guolei.boardview;



import android.support.v7.widget.RecyclerView;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/2/28
 * Time: 上午10:17
 * Desc:
 */
public class BoardViewCallback implements BoardView.Callback {

    private static final String TAG = "BoardView";

    @Override
    public void onMoved(RecyclerView recyclerView, int from, int to) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof ColumnAdapter) {
            ((ColumnAdapter) adapter).swap(from, to);
        }
        adapter.notifyItemMoved(from, to);
    }

    @Override
    public String onRemoved(RecyclerView recyclerView, int position) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        String data = "error data";
        if (adapter instanceof ColumnAdapter) {
            data = ((ColumnAdapter) adapter).remove(position);
        }
        adapter.notifyItemRemoved(position);
        return data;
    }

    @Override
    public void onInserted(RecyclerView recyclerView, int position, String data) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof ColumnAdapter) {
            ((ColumnAdapter) adapter).add(position, data);
        }
        adapter.notifyItemInserted(position);
    }
}
