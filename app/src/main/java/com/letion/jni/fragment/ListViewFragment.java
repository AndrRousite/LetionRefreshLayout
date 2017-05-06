package com.letion.jni.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.letion.jni.R;
import com.letion.letionrefreshlayout.LetionRefreshLayout;
import com.letion.letionrefreshlayout.interf.OnFooterRefreshListener;
import com.letion.letionrefreshlayout.interf.OnHeaderRefreshListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by w7109 on 2017/5/6.
 */
public class ListViewFragment extends Fragment implements OnHeaderRefreshListener, OnFooterRefreshListener {
    LetionRefreshLayout refreshLayout;
    List<String> datas;
    ArrayAdapter<String> adapter;
    int currentPage = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_listview, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        ListView listView = (ListView) view.findViewById(R.id.listView);
        refreshLayout = (LetionRefreshLayout) view.findViewById(R.id.refreshView);
        datas = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            datas.add("Hello , This is " + i + "个任务");
        }
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, datas);
        listView.setAdapter(adapter);
        refreshLayout.setIHeaderView();
        refreshLayout.setIFooterView();
        refreshLayout.setOnHeaderRefreshListener(this);
        refreshLayout.setOnFooterRefreshListener(this);
    }

    @Override
    public void onHeaderRefresh(LetionRefreshLayout layout) {
        currentPage = 1;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                datas.clear();
                for (int i = 0; i < 10; i++) {
                    datas.add("Hello , This is " + i + "个任务");
                }
                adapter.notifyDataSetChanged();
                refreshLayout.onHeaderRefreshComplete();
            }
        }, 5000);
    }

    @Override
    public void onFooterRefresh(LetionRefreshLayout layout) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = currentPage * 10; i < (currentPage + 1) * 10; i++) {
                    datas.add("Hello , This is " + i + "个任务");
                }
                adapter.notifyDataSetChanged();
                refreshLayout.onFooterRefreshComplete();
                currentPage++;
            }
        }, 5000);
    }
}
