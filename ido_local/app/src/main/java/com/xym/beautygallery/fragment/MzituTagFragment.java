package com.xym.beautygallery.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.titans.android.common.CustomFragment;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.module.TagClassify;
import com.xym.beautygallery.module.TagInfo;
import com.xym.beautygallery.tagview.widget.Tag;
import com.xym.beautygallery.tagview.widget.TagListView;
import com.xym.beautygallery.view.MyLinearLayoutManager;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by root on 11/3/16.
 */
public class MzituTagFragment extends CustomFragment {
    @BindView(R.id.main_photo_rv)
    RecyclerView mainPhotoRv;
    @BindView(R.id.main_ad_root)
    RelativeLayout mainAdRoot;

    private Context mContext;
    private Unbinder unbinder;

    private List<TagClassify> mDatas;
    private CommonAdapter<TagClassify> mAdapter;
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;
    private LoadMoreWrapper mLoadMoreWrapper;

    private BroadcastReceiver mTagReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_TAG_LIST_REFRESH.equals(action)) {
                if (reloadDatas()) {
                    mLoadMoreWrapper.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        StatsWrapper.onPageStart(mContext, StatsReportConstants.ENTRY_PAGE_CLASSIFY_FRAGMENT);
        View view = inflater.inflate(R.layout.fragment_tag, container, false);

        mContext = getActivity();
        unbinder = ButterKnife.bind(this, view);
        initDatas();

        mainPhotoRv.setLayoutManager(new MyLinearLayoutManager(mContext));
        mAdapter = new CommonAdapter<TagClassify>(mContext, R.layout.tag_rv_item, mDatas) {
            @Override
            protected void convert(ViewHolder holder, TagClassify s, int position) {
                TextView textView = holder.getView(R.id.tag_title);
                TagListView taglistView = holder.getView(R.id.tagview);

                textView.setText(s.classifyName);
                taglistView.setTags(s.classifyTagList);
            }
        };
        initHeaderAndFooter();
        mLoadMoreWrapper = new LoadMoreWrapper(mHeaderAndFooterWrapper);
        mainPhotoRv.setAdapter(mLoadMoreWrapper);
        mainPhotoRv.setFocusable(false);

        IntentFilter filter = new IntentFilter(Constants.ACTION_TAG_LIST_REFRESH);
        mContext.registerReceiver(mTagReceiver, filter);
        return view;
    }

    private void initHeaderAndFooter() {
        mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(mAdapter);
    }

    private void initDatas() {
        mDatas = new ArrayList<>();
        reloadDatas();
    }

    private boolean reloadDatas() {
        if (mDatas.size() == 0) {
            HashMap<String, List<String>> tagNameList = DataManager.getInstance(mContext).getTagNameList();

            Set<Map.Entry<String, List<String>>> entry = tagNameList.entrySet();
            for (Map.Entry<String, List<String>> e : entry) {
                TagClassify newTag = new TagClassify();
                newTag.classifyName = e.getKey();
                newTag.classifyTagList = new ArrayList<>();
                int i = 0;
                for (String name : e.getValue()) {
                    Tag tempTag = new Tag();
                    TagInfo tagInfo = DataManager.getInstance(mContext).getTagInfoFromHashMap(name);
                    if (tagInfo != null) {
                        tempTag.setId(i);
                        tempTag.setChecked(false);
                        tempTag.setTitle(name);
                        tempTag.setTagInfo(new TagInfo(tagInfo));
                        i++;
                        newTag.classifyTagList.add(tempTag);
                    }
                }
                mDatas.add(newTag);
            }
            return true;
        }
        return false;
    }

    public void clearData() {
        mDatas.clear();
        mLoadMoreWrapper.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        StatsWrapper.onPageEnd(mContext, StatsReportConstants.ENTRY_PAGE_CLASSIFY_FRAGMENT);
        unbinder.unbind();
        mContext.unregisterReceiver(mTagReceiver);
    }
}
