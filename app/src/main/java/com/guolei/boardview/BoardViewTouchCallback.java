package com.guolei.boardview;


import android.support.v7.widget.RecyclerView;


public class BoardViewTouchCallback extends ItemTouchHelper.Callback {

    private RecyclerView.Adapter mAdapter;

    public BoardViewTouchCallback(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP
                | ItemTouchHelper.DOWN;
        final int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        mAdapter.notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder.itemView.setAlpha(0.8f);
            viewHolder.itemView.setRotation(-5f);
        }
        super.onSelectedChanged(viewHolder, actionState);

    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
//        viewHolder.itemView.setBackgroundColor(Color.parseColor("#ff00ddff"));
        viewHolder.itemView.setAlpha(1f);
        viewHolder.itemView.setRotation(0f);
    }
}
