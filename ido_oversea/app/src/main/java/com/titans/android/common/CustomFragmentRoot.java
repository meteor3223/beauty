package com.titans.android.common;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xym.beautygallery.R;
import com.xym.beautygallery.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CustomFragmentRoot extends BaseFragment implements OnPageChangeListener {
    @BindView(R.id.title_indicator)
    TitleIndicator mIndicator;
    @BindView(R.id.main_viewpager)
    CustomViewPager mViewPager;

    protected Context mContext;
    private Unbinder unbinder;
    private TabSelectAdapter mAdapter;
    private List<TabInfo> mTabs = new ArrayList<TabInfo>();
    private int mCurrentTab = 0;
    private long mLastClickBackTime = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_fragment_layout, container, false);
        mContext = getActivity();
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void addTab(TabInfo tab) {
        mTabs.add(tab);
        mAdapter.notifyDataSetChanged();
    }

    private void addTabs(List<TabInfo> tabs) {
        mTabs.addAll(tabs);
        mAdapter.notifyDataSetChanged();
    }

    private void initView() {
        preparePage(mTabs);
        initPage();
    }

    protected void initPage() {
        mAdapter = new TabSelectAdapter(this, mViewPager, mTabs);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCanScroll(true);
        mViewPager.setPageMargin(10);
        mViewPager.setOffscreenPageLimit(mTabs.size());
        mIndicator.init(mViewPager, mTabs, 0, R.drawable.tab_item_bg_selector);
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(getDefaultPage());
    }

    protected int getDefaultPage() {
        return 0;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mIndicator.onScrolled((mViewPager.getWidth() + mViewPager.getPageMargin()) * position
                + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int pos) {
        mCurrentTab = pos;
        mIndicator.setCurrentTab(pos);
    }

    public void navigateByTag(String tag) {
        for (int index = 0, count = mTabs.size(); index < count; index++) {
            if (mTabs.get(index).tag.equals(tag)) {
                mViewPager.setCurrentItem(index);
            }
        }
    }

    public Fragment getFragmentByIndex(int pos) {
        return mTabs.get(pos).fragment;
    }

    public Fragment getFragmentByTag(String tag) {
        for (int index = 0, count = mTabs.size(); index < count; index++) {
            if (mTabs.get(index).tag.equals(tag)) {
                return mTabs.get(index).fragment;
            }
        }
        return null;
    }

    public void preparePage(List<TabInfo> tabs) {

    }

    public void reInit() {
        mTabs.clear();
        preparePage(mTabs);
        mAdapter.refresh(mTabs);
        mIndicator.reinit(mViewPager, mTabs, 0, R.drawable.tabhost_select);
        //       mTitleText.setText(mTabs.get(0).title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
