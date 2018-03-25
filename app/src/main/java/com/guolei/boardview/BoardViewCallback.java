package com.guolei.boardview;

import android.support.v7.widget.RecyclerView;


public class BoardViewCallback implements BoardView.Callback {

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
