package com.letion.letionrefreshlayout.footer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.letion.letionrefreshlayout.IFooterView;
import com.letion.letionrefreshlayout.R;

/**
 * Created by w7109 on 2017/1/6.
 */
public class DefaultFooterView implements IFooterView {
    private LayoutInflater mInflater;
    private TextView footerText;
    private ProgressBar mProgressBar;

    public DefaultFooterView(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getFooterView() {
        View mFooterView = mInflater.inflate(R.layout.layout_default_footer, null, false);
        footerText = (TextView) mFooterView.findViewById(R.id.tv_footer);
        mProgressBar = (ProgressBar) mFooterView.findViewById(R.id.progressBar);
        return mFooterView;
    }

    @Override
    public void pullViewToRefresh(int deltaY) {
        footerText.setText("上拉加载更多数据");
    }

    @Override
    public void releaseViewToRefresh(int deltaY) {
        footerText.setText("释放加载更多数据");
    }

    @Override
    public void footerRefreshing() {
        footerText.setText("正在加载更多数据");
    }

    @Override
    public void footerRefreshComplete() {
        footerText.setText("上拉加载更多数据");
    }
}
