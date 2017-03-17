package com.xym.beautygallery.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.lid.lib.LabelImageView;
import com.squareup.picasso.Picasso;
import com.titans.android.common.CustomFragment;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.BeautyApplication;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.module.AlbumInfo;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.module.MapPicAd;
import com.xym.beautygallery.ui.MzituAlbumBrowseActivity;
import com.xym.beautygallery.ui.RBaseItemDecoration;
import com.xym.beautygallery.utils.Utils;
import com.xym.beautygallery.view.MyGridLayoutManager;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.EmptyWrapper;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by root on 7/7/16.
 */
public class MzituPhotoFragment extends CustomFragment {
    @BindView(R.id.main_photo_rv)
    RecyclerView mainPhotoRv;
    @BindView(R.id.main_ad_root)
    RelativeLayout mainAdRoot;
    private Context mContext;
    private Unbinder unbinder;
    private AdView adView;

    private List<AlbumInfo> mDatas;
    private CommonAdapter<AlbumInfo> mAdapter;
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;
    private EmptyWrapper mEmptyWrapper;
    private LoadMoreWrapper mLoadMoreWrapper;

    private NativeAdsManager mNativeAdsManager;
    private List<MapPicAd> mAdItems;
    private int adCount = 0;
    private int adDisplayFrequency = 6;
    private int ad_width = 0;
    private int ad_height = 0;

    private boolean checkAdType(int position) {
        if (position == mAdItems.size()) {
            MapPicAd tempPicAd = new MapPicAd();
            if (position % adDisplayFrequency == (adDisplayFrequency - 1)) {
                if (mNativeAdsManager.isLoaded()) {
                    tempPicAd.isAd = true;
                    tempPicAd.nativeAd = mNativeAdsManager.nextNativeAd();
                    adCount++;
                }
            }
            if (tempPicAd.isAd == false) {
                tempPicAd.picIndex = position - adCount;
            }
            mAdItems.add(position, tempPicAd);
        }
        if (position < mAdItems.size()) {
            return mAdItems.get(position).isAd;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        StatsWrapper.onPageStart(mContext, StatsReportConstants.ENTRY_PAGE_MZITU_FRAGMENT);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mContext = getActivity();
        unbinder = ButterKnife.bind(this, view);

       // String placement_id = native_ad;
        mNativeAdsManager = new NativeAdsManager(mContext, Constants.NATIVE_AD_ID, 8);
        mNativeAdsManager.loadAds();
        mAdItems = new ArrayList<>();

        initDatas();
        mainPhotoRv.setLayoutManager(new MyGridLayoutManager(mContext, 3));
        mainPhotoRv.addItemDecoration(new RBaseItemDecoration(Constants.DEFAULT_ITEM_DECORATION));
        mAdapter = new CommonAdapter<AlbumInfo>(mContext, R.layout.main_photo_item, mDatas) {
            @Override
            protected void convert(ViewHolder holder, AlbumInfo s, int position) {
                if (checkAdType(position)) {
                    LabelImageView photoIv = holder.getView(R.id.main_photo_item_iv);
                    TextView photoPics = holder.getView(R.id.main_photo_item_pics_tv);
                    LinearLayout nativeAdUnit = holder.getView(R.id.ad_unit);
                    NativeAd ad = null;

                    photoIv.setVisibility(View.GONE);
                    photoPics.setVisibility(View.GONE);
                    nativeAdUnit.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams para;
                    para = nativeAdUnit.getLayoutParams();
                    para.height = ad_height;
                    para.width = ad_width;
                    nativeAdUnit.setLayoutParams(para);

                    ad = mAdItems.get(position).nativeAd;
                    if (ad != null) {
                        ImageView adChoicesIm = holder.getView(R.id.ad_choices_view);
                        MediaView mvAdMedia = holder.getView(R.id.native_ad_media);
                        TextView tvAdTitle = holder.getView(R.id.native_ad_title);
                        TextView tvAdBody = holder.getView(R.id.native_ad_body);
                        Button btnAdCallToAction = holder.getView(R.id.native_ad_call_to_action);
                        // Downloading and setting the ad icon.
                        NativeAd.Image adIcon = ad.getAdChoicesIcon();
                        NativeAd.downloadAndDisplayImage(adIcon, adChoicesIm);

                        tvAdTitle.setText(ad.getAdTitle());
                        tvAdBody.setText(ad.getAdBody());
                        mvAdMedia.setNativeAd(ad);
                        btnAdCallToAction.setText(ad.getAdCallToAction());

                        ad.registerViewForInteraction(nativeAdUnit);
                    }
                } else {
                    int index = mAdItems.get(position).picIndex;
                    TextView photoPics = holder.getView(R.id.main_photo_item_pics_tv);
                    LabelImageView photoIv = holder.getView(R.id.main_photo_item_iv);
                    LinearLayout nativeAdUint = holder.getView(R.id.ad_unit);

                    photoIv.setVisibility(View.VISIBLE);
                    photoPics.setVisibility(View.VISIBLE);
                    nativeAdUint.setVisibility(View.GONE);
                    WindowManager wm = (WindowManager) mContext

                            .getSystemService(Context.WINDOW_SERVICE);
                    int lcdWidth = wm.getDefaultDisplay().getWidth();

                    int widthIv = lcdWidth / 3;
                    int heightIv = (int) (widthIv * 1.5f);
//                int heightIv = (int) (((double) ((widthIv - 10) * mDatas.get(index).album_height)) / mDatas.get(index).album_width);

                    ViewGroup.LayoutParams para;
                    para = photoIv.getLayoutParams();
                    para.height = heightIv;
                    para.width = widthIv;
                    ad_width = widthIv;
                    ad_height = heightIv;
                    photoIv.setLayoutParams(para);

                    if (mDatas.get(index).is_love > 0) {
                        photoIv.setLabelVisual(true);
                    } else {
                        photoIv.setLabelVisual(false);
                    }
                    Picasso.with(mContext).load(mDatas.get(index).album_thumb).config(Bitmap.Config.RGB_565).into(photoIv);
                    photoPics.setText(mDatas.get(index).album_pics);
                }
            }

        };

        initHeaderAndFooter();

        mLoadMoreWrapper = new LoadMoreWrapper(mHeaderAndFooterWrapper);
        mLoadMoreWrapper.setLoadMoreView(R.layout.default_loading);
        mLoadMoreWrapper.setOnLoadMoreListener(new LoadMoreWrapper.OnLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadMore();
                    }
                }, Constants.PIC_LOADING_WAIT_TIME);
            }
        });
        mainPhotoRv.setAdapter(mLoadMoreWrapper);
        mainPhotoRv.setFocusable(false);
        mAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, final int position) {
                if (checkAdType(position)) {

                } else {
                    int index = mAdItems.get(position).picIndex;
                    AlbumInfo currentAlbum = mDatas.get(index);
                    if (currentAlbum != null) {
                        DataManager.getInstance(mContext).setmCurrentAlbum(currentAlbum);
                        DataManager.getInstance(mContext).setIsFromFav(false);
                        ((BeautyApplication) mContext.getApplicationContext()).setRefreshCallBack(new BeautyApplication.RefreshInterface() {
                            @Override
                            public void doRefresh() {
                                mLoadMoreWrapper.notifyItemChanged(position);
                            }
                        });
                        Intent intent = new Intent(mContext, MzituAlbumBrowseActivity.class);
                        mContext.startActivity(intent);
                    }
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (checkAdType(position)) {

                } else {
                    int index = mAdItems.get(position).picIndex;
                    if (mDatas.get(index).is_love > 0) {
                        mDatas.get(index).is_love = 0;
                    } else {
                        mDatas.get(index).is_love = 1;
                    }
                    mDatas.get(index).love_time = System.currentTimeMillis();
                    AlbumInfo newInfo = new AlbumInfo(mDatas.get(index));
                    int lovePos = DataManager.getInstance(mContext).setFavoriteStatus(newInfo);
                    ((BeautyApplication) mContext.getApplicationContext()).handleLoveRefreshCallBack(lovePos, mDatas.get(index).is_love);
                    mLoadMoreWrapper.notifyItemChanged(position);
                }
                return true;
            }
        });
        ((BeautyApplication) mContext.getApplicationContext()).setMzituLoveRefreshCallBack(new BeautyApplication.MzituLoveRefreshInterface() {

            @Override
            public void doMzituLoveRefresh(int pos, int isLove) {
                int index = pos + (pos / (adDisplayFrequency - 1));
                mLoadMoreWrapper.notifyItemChanged(index);
            }
        });
//        loadNewAd();
        loadFBAd();
        return view;
    }

    private void loadMore() {
        ProgressBar mLoadingPb = mLoadMoreWrapper.getmLoadMoreHolder().getView(R.id.loading_progress);
        TextView mLoadingTx = mLoadMoreWrapper.getmLoadMoreHolder().getView(R.id.loading_text);
        if (Utils.isNetworkAvaialble(mContext)) {
            int countLoadBefore = mDatas.size();
            int countLoadCount = DataManager.getInstance(mContext).picMzituPushCache(Constants.PIC_PUSH_CACHE_NUMBER);
            if (countLoadBefore == 0) {
                mLoadMoreWrapper.notifyDataSetChanged();
            } else {
                mLoadMoreWrapper.notifyItemRangeInserted(countLoadBefore, countLoadCount);
            }
            if (countLoadCount == 0 && countLoadBefore > 0) {
                mLoadingPb.setVisibility(View.INVISIBLE);
                mLoadingTx.setText(R.string.rv_list_end);
            } else {
                mLoadingPb.setVisibility(View.VISIBLE);
                mLoadingTx.setText(R.string.rv_list_loading);
            }
//                        mLoadMoreWrapper.notifyDataSetChanged();
        } else {
            mLoadingPb.setVisibility(View.INVISIBLE);
            mLoadingTx.setText(R.string.network_not_available);
        }
    }

    private void loadFBAd() {
        final Button closeButton = new Button(mContext);
        adView = new AdView(mContext, Constants.BANNER_AD_ID, AdSize.BANNER_320_50);
        adView.loadAd();
        Log.e("MzituPhotoFragment", "loadFBAd ");

        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e("MzituPhotoFragment", "onError " + adError.getErrorCode());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.e("MzituPhotoFragment", "onAdLoaded ");
                closeButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.e("MzituPhotoFragment", "onAdClicked ");
                adView.destroy();
                closeButton.setVisibility(View.GONE);
            }
        });
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rllp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mainAdRoot.addView(adView, rllp);

        closeButton.setBackgroundResource(R.mipmap.close_icon);
        int side = (int) (18 * Utils.getScreenDensity(mContext));
        int margin = (int) (35 * Utils.getScreenDensity(mContext));
        RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(side, side);
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        buttonLayoutParams.bottomMargin = margin;
        closeButton.setLayoutParams(buttonLayoutParams);
        mainAdRoot.addView(closeButton);
        closeButton.setVisibility(View.INVISIBLE);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adView.destroy();
                closeButton.setVisibility(View.GONE);
            }
        });
    }

    private void initHeaderAndFooter() {
        mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(mAdapter);
    }

    private BroadcastReceiver mMzituPhotoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_MZITU_REFRESH.equals(action)) {
                if (mDatas.size() == 0) {
                    mLoadMoreWrapper.notifyDataSetChanged();
                }
            }
        }
    };

    private void initDatas() {
        mDatas = DataManager.getInstance(mContext).getmPicMzituListData();
        DataManager.getInstance(mContext).picMzituPushCache(Constants.PIC_PUSH_CACHE_NUMBER);

        IntentFilter filter = new IntentFilter(Constants.ACTION_MZITU_REFRESH);
        mContext.registerReceiver(mMzituPhotoReceiver, filter);
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
        adView.destroy();
        super.onDestroyView();
        StatsWrapper.onPageEnd(mContext, StatsReportConstants.ENTRY_PAGE_MZITU_FRAGMENT);
        unbinder.unbind();
        mContext.unregisterReceiver(mMzituPhotoReceiver);
    }
}
