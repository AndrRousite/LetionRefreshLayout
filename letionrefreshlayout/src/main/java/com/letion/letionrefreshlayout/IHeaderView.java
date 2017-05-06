package com.letion.letionrefreshlayout;

import android.view.View;

/**
 * Created by w7109 on 2017/1/6.
 */
public interface IHeaderView {

    /**
     * 自定义实现headerView
     * @return
     */
    View getHeaderView();

    /**
     * 顶部HeadView 被下拉时此事件发生
     * @param deltaY 下拉的距离
     */
    void pullViewToRefresh(int deltaY);

    /**
     * 顶部head view下拉后，header view 完全显示 此事件发生
     * @param deltaY 下拉的距离
     */
    void releaseViewToRefresh(int deltaY);

    /**
     * head view正在刷新
     */
    void headerRefreshing();

    /**
     * head view完成刷新
     */
    void headerRefreshComplete();
}
