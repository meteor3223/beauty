package com.zhy.adapter.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;

import com.xym.beautygallery.ad.AdManager;
import com.xym.beautygallery.ad.AdUtils;
import com.xym.beautygallery.ad.NativeAd;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by zhy on 16/4/9.
 */
public abstract class CommonAdapter<T> extends MultiItemTypeAdapter<T> {
    protected Context mContext;
    protected int mLayoutId;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;

    public CommonAdapter(final Context context, final int layoutId, List<T> datas) {
        super(context, datas);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutId = layoutId;
        mDatas = datas;

        addItemViewDelegate(new ItemViewDelegate<T>() {
            @Override
            public int getItemViewLayoutId() {
                return layoutId;
            }

            @Override
            public boolean isForViewType(T item, int position) {
                return true;
            }

            @Override
            public void convert(ViewHolder holder, T t, int position) {
                if (mEnableAd && AdUtils.isAdItem(position, mAdGap)) {
                    NativeAd ad = mAds.get(position);
                    if (ad == null) {
                        ad = AdManager.getInstance(mContext).randomAdInfo(mNativeAdId);
                        mAds.put(position, ad);
                    }
                    CommonAdapter.this.convertAd(holder, ad, position);
                } else {
                    CommonAdapter.this.convert(holder, t, AdUtils.convertPos(position, mAdGap));
                }
            }

        });

    }

    protected abstract void convert(ViewHolder holder, T t, int position);

    protected void convertAd(ViewHolder holder, NativeAd t, int position) {
    }

}
