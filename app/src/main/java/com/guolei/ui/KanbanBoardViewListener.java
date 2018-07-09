package com.guolei.ui;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import com.guolei.boardview.BaseBoardViewListener;
import com.guolei.boardview.R;

/**
 * Copyright © 2013-2018 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/7/9
 * Time: 下午9:37
 * Desc:
 */
public class KanbanBoardViewListener extends BaseBoardViewListener<String>{

    @Override
    public int getColumnRecyclerViewId() {
        return R.id.recycler_view;
    }

    @Override
    public void onReleaseRow(int fromColumnIndex, int fromRowIndex, int toColumnIndex, int toRowIndex) {
        // do you self logic
    }

    @Override
    public void onReleaseColumn(int from, int to) {
        // do you self logic
    }

    @Override
    public int getResponseViewId() {
        return R.id.title;
    }

    @Override
    public boolean isEnableSelectColumn(int column) {
        return true;
    }

    @Override
    public boolean isEnableSwapInColumn(int column) {
        return true;
    }

    @Override
    public boolean isEnableInsertRowInColumn(int column, String s) {
        return true;
    }

    @Override
    public boolean isEnableSelectRow(int column, int row) {
        return true;
    }
}
