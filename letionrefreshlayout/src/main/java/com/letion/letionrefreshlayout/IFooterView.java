package com.letion.letionrefreshlayout;

import android.view.View;

/**
 * Created by w7109 on 2017/1/6.
 */
public interface IFooterView {

    /**
     * 自定义实现footerView
     * @return
     */
    View getFooterView();

    /**
     * footerView 被上拉时此事件发生
     * @param deltaY 上拉的距离
     */
    void pullViewToRefresh(int deltaY);

    /**
     * footer view上拉后，footer view 完全显示 此事件发生
     * @param deltaY 上拉的距离
     */
    void releaseViewToRefresh(int deltaY);

    /**
     * footer view正在刷新
     */
    void footerRefreshing();

    /**
     * footer view完成刷新
     */
    void footerRefreshComplete();
}
