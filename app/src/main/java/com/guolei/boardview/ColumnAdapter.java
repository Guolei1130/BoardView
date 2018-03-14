package com.guolei.boardview;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/2/27
 * Time: 上午11:24
 * Desc:
 */
public class ColumnAdapter extends RecyclerView.Adapter<ColumnAdapter.SimpleViewHolder> {

    private List<String> mData = new ArrayList<>();

    public ColumnAdapter(int index, int count) {
        for (int i = 0; i < count; i++) {
            mData.add(String.valueOf(index) + String.valueOf(i));
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        holder.mTextView.setText(mData.get(position));
        if (Long.parseLong(mData.get(position)) == Provider.getInstance().getSelectedId()) {
            holder.itemView.setVisibility(View.INVISIBLE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public int getPositionFromId() {
        for (int i = 0; i < mData.size(); i++) {
            if (Long.parseLong(mData.get(i)) == Provider.getInstance().getSelectedId()) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public String remove(int position) {
        return mData.remove(position);
    }

    public void add(int position, String data) {
        mData.add(position, data);
    }

    public void swap(int fromPos, int toPos) {
        Collections.swap(mData, fromPos, toPos);
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.content);
        }
    }

    public long getIdByPosition(int position) {
        if (position > mData.size() - 1) {
            return RecyclerView.NO_ID;
        }
        return Long.parseLong(mData.get(position));
    }

}
