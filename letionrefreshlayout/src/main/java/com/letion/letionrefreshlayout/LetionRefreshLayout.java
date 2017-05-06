package com.letion.letionrefreshlayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.letion.letionrefreshlayout.footer.DefaultFooterView;
import com.letion.letionrefreshlayout.header.DefaultHeaderView;
import com.letion.letionrefreshlayout.interf.OnFooterRefreshListener;
import com.letion.letionrefreshlayout.interf.OnHeaderRefreshListener;
import com.letion.letionrefreshlayout.util.MeasureTool;

/**
 * Created by w7109 on 2017/1/6.
 */
public class LetionRefreshLayout extends LinearLayout {
    private static final String TAG = LetionRefreshLayout.class.getSimpleName();
    private static final int PULL_UP_STATE = 0,PULL_DOWN_STATE = 1; // 上拉 or 下拉
    private static final int PULL_TO_REFRESH = 2,RELEASE_TO_REFRESH = 3,REFRESHING = 4; // 下拉，释放，刷新

    private int lastY ; // 下拉时触摸点的位置,计算滑动的距离
    private int mPullState;// 上拉 或者 下拉

    //list or grid
    private AdapterView<?> mAdapterView;
    //RecyclerView
    private RecyclerView mRecyclerView;
    //ScrollView
    private ScrollView mScrollView;
    //WebView
    private WebView mWebView;

    private IHeaderView IHeader;
    private IFooterView IFooter;
    private OnHeaderRefreshListener mOnHeaderRefreshListener;
    private OnFooterRefreshListener mOnFooterRefreshListener;

    // Header
    private int mHeaderState;
    private View mHeaderView;
    private int mHeadViewHeight;
    // Footer
    private int mFooterState;
    private View mFooterView;
    private int mFooterViewHeight;

    public LetionRefreshLayout(Context context) {
        this(context,null);
    }

    public LetionRefreshLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LetionRefreshLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL); // 设置该布局容器为垂直方向
    }

    public void setIHeaderView(IHeaderView baseHeaderAdapter) {
        IHeader = baseHeaderAdapter;
        initHeaderView();
        initSubViewType();
    }

    /**
     * 实现默认的下拉刷新布局
     */
    public void setIHeaderView() {
        IHeader = new DefaultHeaderView(getContext());
        initHeaderView();
        initSubViewType();
    }

    public void setIFooterView(IFooterView baseFooterAdapter) {
        IFooter = baseFooterAdapter;
        initFooterView();
    }

    public void setIFooterView() {
        IFooter = new DefaultFooterView(getContext());
        initFooterView();
    }

    /**
     * 初始化顶部View，添加到当前ViewGroup中，并将其隐藏
     */
    private void initHeaderView(){
        mHeaderView = IHeader.getHeaderView();
        MeasureTool.measureView(mHeaderView);
        mHeadViewHeight = mHeaderView.getMeasuredHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mHeadViewHeight);
        params.topMargin = -mHeadViewHeight;
        addView(mHeaderView, 0, params);
    }

    /**
     * 初始化底部View，添加到当前ViewGroup中，并将其隐藏
     */
    private void initFooterView(){
        mFooterView = IFooter.getFooterView();
        MeasureTool.measureView(mFooterView);
        mFooterViewHeight = mFooterView.getMeasuredHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                mFooterViewHeight);
        addView(mFooterView, params);
    }

    /**
     * 确定当前view group内部子视图类型
     */
    private void initSubViewType(){
        int count = getChildCount();
        if (count < 2) return;
        View childAt = getChildAt(1);

        if (childAt instanceof AdapterView<?>){
            mAdapterView = (AdapterView<?>) childAt;
        }else if(childAt instanceof RecyclerView){
            mRecyclerView = (RecyclerView) childAt;
        }else if( childAt instanceof ScrollView){
            mScrollView = (ScrollView) childAt;
        }else if(childAt instanceof WebView){
            mWebView = (WebView) childAt;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int y = (int) ev.getRawY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = y - lastY;
                if (isParentViewScroll(deltaY)) {
                    Log.e(TAG, "onInterceptTouchEvent: belong to ParentView");
                    return true; //此时,触发onTouchEvent事件
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int deltaY = y - lastY;
                if (mPullState == PULL_DOWN_STATE) {
                    Log.e(TAG, "onTouchEvent: pull down begin-->" + deltaY);
                    initHeaderViewToRefresh(deltaY);
                } else if (mPullState == PULL_UP_STATE) {
                    initFooterViewToRefresh(deltaY);
                }
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int topMargin = getHeaderTopMargin();
                Log.e(TAG, "onTouchEvent: topMargin==" + topMargin);
                if (mPullState == PULL_DOWN_STATE) {
                    if (topMargin >= 0) {
                        headerRefreshing();
                    } else {
                        reSetHeaderTopMargin(-mHeadViewHeight);
                    }
                } else if (mPullState == PULL_UP_STATE) {
                    if (Math.abs(topMargin) >= mHeadViewHeight
                            + mFooterViewHeight) {
                        // 开始执行footer 刷新
                        footerRefreshing();
                    } else {
                        // 还没有执行刷新，重新隐藏
                        reSetHeaderTopMargin(-mHeadViewHeight);
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private void initHeaderViewToRefresh(int deltaY) {
        if (IHeader == null) {
            return;
        }
        int topDistance = UpdateHeadViewMarginTop(deltaY);
        if (topDistance < 0 && topDistance > -mHeadViewHeight) {
            IHeader.pullViewToRefresh(deltaY);
            mHeaderState = PULL_TO_REFRESH;
        } else if (topDistance > 0 && mHeaderState != RELEASE_TO_REFRESH) {
            IHeader.releaseViewToRefresh(deltaY);
            mHeaderState = RELEASE_TO_REFRESH;
        }

    }

    private void initFooterViewToRefresh(int deltaY) {
        if (IFooter == null) {
            return;
        }

        int topDistance = UpdateHeadViewMarginTop(deltaY);

        Log.e("zzz", "the distance  is " + topDistance);

        // 如果header view topMargin 的绝对值大于或等于(header + footer) 四分之一 的高度
        // 说明footer view 完全显示出来了，修改footer view 的提示状态
        if (Math.abs(topDistance) >= (mHeadViewHeight + mFooterViewHeight)/4
                && mFooterState != RELEASE_TO_REFRESH) {
            IFooter.pullViewToRefresh(deltaY);
            mFooterState = RELEASE_TO_REFRESH;
        } else if (Math.abs(topDistance) < (mHeadViewHeight + mFooterViewHeight)/4) {
            IFooter.releaseViewToRefresh(deltaY);
            mFooterState = PULL_TO_REFRESH;
        }
    }


    private int UpdateHeadViewMarginTop(int deltaY) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        float topMargin = params.topMargin + deltaY * 0.3f;
        params.topMargin = (int) topMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
        return params.topMargin;
    }


    private void headerRefreshing() {
        if (IHeader == null) {
            return;
        }

        mHeaderState = REFRESHING;
        setHeaderTopMargin(0);
        IHeader.headerRefreshing();
        if (mOnHeaderRefreshListener != null) {
            mOnHeaderRefreshListener.onHeaderRefresh(this);
        }
    }

    private void footerRefreshing() {
        if (IFooter == null) {
            return;
        }

        mFooterState = REFRESHING;
        int top = mHeadViewHeight + mFooterViewHeight;
        setHeaderTopMargin(-top);
        IFooter.footerRefreshing();
        if (mOnFooterRefreshListener != null) {
            mOnFooterRefreshListener.onFooterRefresh(this);
        }
    }

    public void onHeaderRefreshComplete() {
        if (IHeader == null) {
            return;
        }
        setHeaderTopMargin(-mHeadViewHeight);
        IHeader.headerRefreshComplete();
        mHeaderState = PULL_TO_REFRESH;
    }

    public void onFooterRefreshComplete() {
        if (IFooter == null) {
            return;
        }
        setHeaderTopMargin(-mHeadViewHeight);
        IFooter.footerRefreshComplete();
        mFooterState = PULL_TO_REFRESH;
    }

    /**
     * 滑动由父View（当前View）处理
     *
     * @param deltaY
     * @return
     */
    private boolean isParentViewScroll(int deltaY) {
        boolean belongToParentView = false;
        if (mHeaderState == REFRESHING) {
            belongToParentView = false;
        }

        if (mAdapterView != null) {

            if (deltaY > 0) {
                View child = mAdapterView.getChildAt(0);
                if (child == null) {
                    belongToParentView = false;
                }

                if (mAdapterView.getFirstVisiblePosition() == 0 && child.getTop() == 0) {
                    mPullState = PULL_DOWN_STATE;
                    belongToParentView = true;
                }
            } else if (deltaY < 0) {
                View lastChild = mAdapterView.getChildAt(mAdapterView
                        .getChildCount() - 1);
                if (lastChild == null) {
                    // 如果mAdapterView中没有数据,不拦截
                    belongToParentView = false;
                }
                // 最后一个子view的Bottom小于父View的高度说明mAdapterView的数据没有填满父view,
                // 等于父View的高度说明mAdapterView已经滑动到最后
                if (lastChild.getBottom() <= getHeight()
                        && mAdapterView.getLastVisiblePosition() == mAdapterView
                        .getCount() - 1) {
                    mPullState = PULL_UP_STATE;
                    belongToParentView = true;
                }
            }
        }


        if (mRecyclerView != null) {
            if (deltaY > 0) {
                View child = mRecyclerView.getChildAt(0);
                if (child == null) {
                    belongToParentView = false;
                }
                LinearLayoutManager mLinearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int firstPosition = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition();

                if (firstPosition == 0) {
                    mPullState = PULL_DOWN_STATE;
                    belongToParentView = true;
                }
            } else if (deltaY < 0) {
                View child = mRecyclerView.getChildAt(0);
                if (child == null) {
                    belongToParentView = false;
                }
                if (mRecyclerView.computeVerticalScrollExtent() + mRecyclerView.computeVerticalScrollOffset()
                        >= mRecyclerView.computeVerticalScrollRange()){
                    belongToParentView = true;
                    mPullState = PULL_UP_STATE;
                }else {
                    belongToParentView = false;
                }
            }
        }

        if (mScrollView != null) {
            View child = mScrollView.getChildAt(0);
            if (deltaY > 0) {

                if (child == null) {
                    belongToParentView = false;
                }

                int distance = mScrollView.getScrollY();
                if (distance == 0) {
                    mPullState = PULL_DOWN_STATE;
                    belongToParentView = true;
                }
            } else if (deltaY < 0
                    && child.getMeasuredHeight() <= getHeight()
                    + mScrollView.getScrollY()) {
                mPullState = PULL_UP_STATE;
                belongToParentView = true;

            }
        }

        if (mWebView != null) {
            View child = mWebView.getChildAt(0);
            if (deltaY > 0) {

                if (child == null) {
                    belongToParentView = false;
                }

                int distance = mWebView.getScrollY();
                if (distance == 0) {
                    mPullState = PULL_DOWN_STATE;
                    belongToParentView = true;
                }
            }
        }


        return belongToParentView;
    }

    /**
     * 设置header view 的topMargin的值
     *
     * @param topMargin ，为0时，说明header view 刚好完全显示出来； 为-mHeaderViewHeight时，说明完全隐藏了
     * @description
     */
    private void setHeaderTopMargin(int topMargin) {

        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        params.topMargin = topMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
    }

    /**
     * //上拉或下拉至一半时，放弃下来，视为完成一次下拉统一处理，初始化所有内容
     *
     * @param topMargin
     */
    private void reSetHeaderTopMargin(int topMargin) {

        if (IHeader != null) {
            IHeader.headerRefreshComplete();
        }

        if (IFooter != null) {
            IFooter.footerRefreshComplete();
        }

        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        params.topMargin = topMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
    }

    /**
     * 获取当前header view 的topMargin
     *
     * @return
     * @description
     */

    private int getHeaderTopMargin() {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        return params.topMargin;
    }

    public void setOnHeaderRefreshListener(OnHeaderRefreshListener onHeaderRefreshListener) {
        mOnHeaderRefreshListener = onHeaderRefreshListener;
    }

    public void setOnFooterRefreshListener(OnFooterRefreshListener onFooterRefreshListener) {
        mOnFooterRefreshListener = onFooterRefreshListener;
    }
}
