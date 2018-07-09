package com.guolei.boardview;
//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.support.v7.widget.RecyclerView;

import com.guolei.ui.BoardViewAdapter;
import com.guolei.ui.ColumnAdapter;


/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/2/27
 * Time: 上午11:36
 * Desc:
 */
public class BoardViewTouchCallback extends ItemTouchHelper.Callback {

    private int mFromPos = 0;

    private BoardViewHolder mBoardViewHolder;

    public BoardViewTouchCallback(BoardViewHolder boardViewHolder) {
        mBoardViewHolder = boardViewHolder;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP
                | ItemTouchHelper.DOWN;
        final int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter instanceof BoardViewAdapter && mBoardViewHolder.getBoardViewListener() != null
                && mBoardViewHolder.getBoardViewListener().isEnableSelectColumn(toPosition)) {
            ((BoardViewAdapter) adapter).swap(fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
        }
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
            mFromPos = viewHolder.getAdapterPosition();
        }
        super.onSelectedChanged(viewHolder, actionState);

    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
//        viewHolder.itemView.setBackgroundColor(Color.parseColor("#ff00ddff"));
        viewHolder.itemView.setAlpha(1f);
        viewHolder.itemView.setRotation(0f);
        if (mBoardViewHolder.getBoardViewListener() != null) {
            mBoardViewHolder.getBoardViewListener().onReleaseColumn(mFromPos,
                    viewHolder.getAdapterPosition());
        }
    }
}
