package com.guolei.boardview;

import android.support.v7.widget.RecyclerView;

/**
 * Copyright © 2013-2018 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/7/5
 * Time: 下午10:25
 * Desc:
 */
public abstract class AbsBoardViewAdapter<VH extends RecyclerView.ViewHolder,T> extends RecyclerView.Adapter<VH> {
    public abstract int getPositionFromId(String id);

    public abstract String getIdFromPosition(int position);

    public abstract void add(int position, T data);

    public abstract T getData(int position);

    public abstract T remove(int position);

    public abstract void swap(int fromPos, int toPos);
}
