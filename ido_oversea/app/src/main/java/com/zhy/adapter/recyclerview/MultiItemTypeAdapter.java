package com.zhy.adapter.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.xym.beautygallery.ad.AdManager;
import com.xym.beautygallery.ad.AdUtils;
import com.xym.beautygallery.ad.NativeAd;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ItemViewDelegateManager;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhy on 16/4/9.
 */
public class MultiItemTypeAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    protected Context mContext;
    protected List<T> mDatas;
    protected Map<Integer,NativeAd> mAds;

    protected ItemViewDelegateManager mItemViewDelegateManager;
    protected OnItemClickListener mOnItemClickListener;

    protected String mNativeAdId;
    protected boolean mEnableAd = false;
    protected int mAdGap;

    public void setNativeId(String nativeId) {
        mNativeAdId = nativeId;
    }

    public void setNativeAdEnable(boolean enable) {
        mEnableAd = enable;
    }


    public MultiItemTypeAdapter(Context context, List<T> datas) {
        mContext = context;
        mDatas = datas;
        mItemViewDelegateManager = new ItemViewDelegateManager();
        mAds = new HashMap<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (!useItemViewDelegateManager()) return super.getItemViewType(position);
        return mItemViewDelegateManager.getItemViewType(getItem(position), position);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemViewDelegate itemViewDelegate = mItemViewDelegateManager.getItemViewDelegate(viewType);
        int layoutId = itemViewDelegate.getItemViewLayoutId();
        ViewHolder holder = ViewHolder.createViewHolder(mContext, parent, layoutId);
        onViewHolderCreated(holder, holder.getConvertView());
        return holder;
    }

    public void onViewHolderCreated(ViewHolder holder, View itemView) {

    }

    public T getItem(int pos){
        if(mEnableAd){
            return AdUtils.isAdItem(pos, mAdGap) ? null : mDatas.get(AdUtils.convertPos(pos, mAdGap));
        }else {
            return mDatas.get(pos);
        }
    }

    public void convert(ViewHolder holder, T t) {
        mItemViewDelegateManager.convert(holder, t, holder.getAdapterPosition());
    }

    protected boolean isEnabled(int viewType) {
        return true;
    }


    protected void setListener(final ViewHolder viewHolder, int viewType) {
        final int position = viewHolder.getAdapterPosition();
        if (!isEnabled(viewType)) return;
        if (mEnableAd && AdUtils.isAdItem(position, mAdGap)) return;
        viewHolder.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int realPos = AdUtils.convertPos(position, mAdGap);
                    mOnItemClickListener.onItemClick(v, viewHolder, realPos);
                }
            }
        });

        viewHolder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemClickListener != null) {
                    int realPos = AdUtils.convertPos(position, mAdGap);
                    return mOnItemClickListener.onItemLongClick(v, viewHolder, realPos);
                }
                return false;
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        setListener(holder, -1);
        convert(holder, getItem(position));
    }

    @Override
    public int getItemCount() {
        int itemCount = mDatas.size();
        if (mEnableAd) {
            mAdGap = AdManager.getInstance(mContext).getNativeAdGap(mNativeAdId);
            itemCount = itemCount + itemCount / mAdGap;
        }
        return itemCount;
    }


    public List<T> getDatas() {
        return mDatas;
    }

    public MultiItemTypeAdapter addItemViewDelegate(ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(itemViewDelegate);
        return this;
    }

    public MultiItemTypeAdapter addItemViewDelegate(int viewType, ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(viewType, itemViewDelegate);
        return this;
    }

    protected boolean useItemViewDelegateManager() {
        return mItemViewDelegateManager.getItemViewDelegateCount() > 0;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder holder, int position);

        boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}
