package com.guolei.ui;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guolei.boardview.BoardViewHolder;
import com.guolei.boardview.CustomItemDecoration;
import com.guolei.boardview.R;
import com.guolei.boardview.SimpleLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class BoardViewAdapter extends RecyclerView.Adapter<BoardViewAdapter.SimpleViewHolder> {

    private List<List<String>> mData = new ArrayList<>();

    private BoardViewHolder mBoardViewHolder;

    public BoardViewAdapter(BoardViewHolder boardViewHolder) {
        mBoardViewHolder = boardViewHolder;
        initData();
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            mData.add(new ArrayList<>());
            int max = new Random().nextInt(20);
            for (int j = 0; j < max; j++) {
                mData.get(i).add("column: " + i + ";row:" + j);
            }
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview,
                parent,
                false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, int position) {
        holder.recyclerView.setLayoutManager(new SimpleLayoutManager(holder.itemView.getContext()));
        RecyclerView.Adapter adapter = new ColumnAdapter(mBoardViewHolder, mData.get(position));
        holder.recyclerView.setAdapter(adapter);
        if (holder.recyclerView.getItemDecorationAt(0) == null) {
            holder.recyclerView.addItemDecoration(new CustomItemDecoration());
        }
        holder.recyclerView.getItemAnimator().setAddDuration(0);
        holder.recyclerView.getItemAnimator().setRemoveDuration(0);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void swap(int from, int to) {
        Collections.swap(mData, from, to);
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        TextView title;

        SimpleViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recycler_view);
            title = itemView.findViewById(R.id.title);
        }
    }

}
