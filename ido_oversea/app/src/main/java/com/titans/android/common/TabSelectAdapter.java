package com.titans.android.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TabSelectAdapter extends FragmentPagerAdapter {

    private CustomFragmentRoot mContext;
    private ViewPager mViewPager;

    private List<TabInfo> mTabInfos = new ArrayList<TabInfo>();

    public TabSelectAdapter(CustomFragmentRoot context, ViewPager mViewPager, List<TabInfo> tabInfos) {
        super(context.getChildFragmentManager());
        this.mContext = context;
        this.mViewPager = mViewPager;
        this.mTabInfos = tabInfos;
        this.mViewPager.setAdapter(this);
    }

    public void refresh(List<TabInfo> tabInfos) {
        if (tabInfos != null) {
            mTabInfos = tabInfos;
            notifyDataSetChanged();
        }
    }

    @Override
    public Fragment getItem(int pos) {
        TabInfo tabInfo = mTabInfos.get(pos);
        if (tabInfo != null && tabInfo.fragment == null) {
            tabInfo.fragment = Fragment.instantiate(mContext.getActivity(), tabInfo.clss.getName());
            tabInfo.fragment.setArguments(tabInfo.data);
        }
        return tabInfo.fragment;
    }

    @Override
    public int getCount() {
        return mTabInfos.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TabInfo tab = mTabInfos.get(position);
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        tab.fragment = fragment;
        return fragment;
    }
}
