package com.guolei.boardview;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ColumnAdapter extends RecyclerView.Adapter<ColumnAdapter.SimpleViewHolder> {

    private List<String> mData = new ArrayList<>();

    ColumnAdapter(int index, int count) {
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

    int getPositionFromId() {
        for (int i = 0; i < mData.size(); i++) {
            if (Long.parseLong(mData.get(i)) == Provider.getInstance().getSelectedId()) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    String remove(int position) {
        return mData.remove(position);
    }

    void add(int position, String data) {
        mData.add(position, data);
    }

    void swap(int fromPos, int toPos) {
        Collections.swap(mData, fromPos, toPos);
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        SimpleViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.content);
        }
    }

    long getIdByPosition(int position) {
        if (position > mData.size() - 1) {
            return RecyclerView.NO_ID;
        }
        return Long.parseLong(mData.get(position));
    }

}
