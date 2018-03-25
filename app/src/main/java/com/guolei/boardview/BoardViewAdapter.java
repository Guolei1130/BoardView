package com.guolei.boardview;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class BoardViewAdapter extends RecyclerView.Adapter<BoardViewAdapter.SimpleViewHolder> {

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
        RecyclerView.Adapter adapter = new ColumnAdapter(position, 20);
        holder.recyclerView.setAdapter(adapter);
        if (holder.recyclerView.getItemDecorationAt(0) == null) {
            holder.recyclerView.addItemDecoration(new CustomItemDecoration());
        }
        holder.recyclerView.getItemAnimator().setAddDuration(0);
        holder.recyclerView.getItemAnimator().setRemoveDuration(0);
    }

    @Override
    public int getItemCount() {
        return 10;
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
