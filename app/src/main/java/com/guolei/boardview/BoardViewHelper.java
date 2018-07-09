package com.guolei.boardview;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Copyright © 2013-2018 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/7/5
 * Time: 下午10:27
 * Desc:
 */
public class BoardViewHelper {
    public static int findParentIndex(View childView) {
        while (childView != null) {
            if (childView.getParent() instanceof RecyclerView) {
                return ((RecyclerView) childView.getParent()).getChildViewHolder(childView).getAdapterPosition();
            } else {
                try {
                    childView = (View) childView.getParent();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }
}
