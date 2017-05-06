package com.letion.letionrefreshlayout.header;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.letion.letionrefreshlayout.IHeaderView;
import com.letion.letionrefreshlayout.R;

/**
 * Created by w7109 on 2017/1/6.
 */
public class DefaultHeaderView implements IHeaderView {
    private LayoutInflater mInflater;
    private TextView headerText;
    private ProgressBar mProgressBar;

    public DefaultHeaderView(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getHeaderView() {
        View mHeaderView = mInflater.inflate(R.layout.layout_default_header, null, false);
        headerText = (TextView) mHeaderView.findViewById(R.id.tv_header);
        mProgressBar = (ProgressBar) mHeaderView.findViewById(R.id.progressBar);
        return mHeaderView;
    }

    @Override
    public void pullViewToRefresh(int deltaY) {
        headerText.setText("下拉刷新");
    }

    @Override
    public void releaseViewToRefresh(int deltaY) {
        headerText.setText("释放刷新");
    }

    @Override
    public void headerRefreshing() {
        headerText.setText("正在加载");
    }

    @Override
    public void headerRefreshComplete() {
        headerText.setText("下拉刷新");
    }
}
