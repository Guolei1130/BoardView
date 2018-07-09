package com.guolei.ui;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guolei.boardview.BaseBoardViewAdapter;
import com.guolei.boardview.BoardViewHolder;
import com.guolei.boardview.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ColumnAdapter extends BaseBoardViewAdapter<ColumnAdapter.SimpleViewHolder,String> {

    private List<String> mData = new ArrayList<>();

    private BoardViewHolder mBoardViewHolder;

    ColumnAdapter(BoardViewHolder boardViewHolder,List<String> data) {
        mBoardViewHolder = boardViewHolder;
        mData.addAll(data);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        holder.mTextView.setText(mData.get(position));
        if (mData.get(position).equals(mBoardViewHolder.getSelectedId())) {
            holder.itemView.setVisibility(View.INVISIBLE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getPositionFromId(String id) {
        return mData.indexOf(id);
    }

    @Override
    public String getIdFromPosition(int position) {
        return mData.get(position);
    }

    @Override
    public void add(int position, String data) {
        mData.add(position,data);
    }

    @Override
    public String getData(int position) {
        return mData.get(position);
    }

    @Override
    public String remove(int position) {
        return mData.remove(position);
    }

    public void swap(int fromPos, int toPos) {
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
