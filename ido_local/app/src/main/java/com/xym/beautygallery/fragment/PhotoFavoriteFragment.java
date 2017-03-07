package com.xym.beautygallery.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lid.lib.LabelImageView;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.squareup.picasso.Picasso;
import com.titans.android.common.CustomFragment;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.BeautyApplication;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.module.AlbumInfo;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by root on 10/9/16.
 */
public class PhotoFavoriteFragment extends CustomFragment {
    @BindView(R.id.main_photo_rv)
    RecyclerView mainPhotoRv;
    @BindView(R.id.main_ad_root)
    RelativeLayout mainAdRoot;
    private Context mContext;
    private Unbinder unbinder;
    private BannerView adView;

    private List<AlbumInfo> mDatas;
    private CommonAdapter<AlbumInfo> mAdapter;
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;
    private EmptyWrapper mEmptyWrapper;
    private LoadMoreWrapper mLoadMoreWrapper;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        StatsWrapper.onPageStart(mContext, StatsReportConstants.ENTRY_PAGE_FAVORITE_FRAGMENT);
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        mContext = getActivity();
        unbinder = ButterKnife.bind(this, view);

        initDatas();
        mainPhotoRv.setLayoutManager(new MyGridLayoutManager(mContext, 3));
        mainPhotoRv.addItemDecoration(new RBaseItemDecoration(Constants.DEFAULT_ITEM_DECORATION));
        mAdapter = new CommonAdapter<AlbumInfo>(mContext, R.layout.main_photo_item, mDatas) {
            @Override
            protected void convert(ViewHolder holder, AlbumInfo s, int position) {
                TextView photoPics = holder.getView(R.id.main_photo_item_pics_tv);
                LabelImageView photoIv = holder.getView(R.id.main_photo_item_iv);

                WindowManager wm = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                int lcdWidth = wm.getDefaultDisplay().getWidth();

                int widthIv = lcdWidth / 3;

                int heightIv = (int) (((double) ((widthIv - 10) * mDatas.get(position).album_height)) / mDatas.get(position).album_width);

                ViewGroup.LayoutParams para;
                para = photoIv.getLayoutParams();
                para.height = heightIv;
                para.width = widthIv;
                photoIv.setLayoutParams(para);

                if (mDatas.get(position).is_love > 0) {
                    photoIv.setLabelVisual(true);
                } else {
                    photoIv.setLabelVisual(false);
                }
                Picasso.with(mContext).load(mDatas.get(position).album_thumb).config(Bitmap.Config.RGB_565).into(photoIv);
                photoPics.setText(mDatas.get(position).album_pics);
            }
        };
        initHeaderAndFooter();

        mLoadMoreWrapper = new LoadMoreWrapper(mHeaderAndFooterWrapper);
        mLoadMoreWrapper.setLoadMoreView(R.layout.default_loading);
        mLoadMoreWrapper.setOnLoadMoreListener(new LoadMoreWrapper.OnLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                loadMore();
            }
        });
        mainPhotoRv.setAdapter(mLoadMoreWrapper);
        mainPhotoRv.setFocusable(false);
        mAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                AlbumInfo currentAlbum = mDatas.get(position);
                if (currentAlbum != null) {
                    DataManager.getInstance(mContext).setmCurrentAlbum(currentAlbum);
                    DataManager.getInstance(mContext).setIsFromFav(true);
                    ((BeautyApplication) mContext.getApplicationContext()).setRefreshCallBack(new BeautyApplication.RefreshInterface() {
                        @Override
                        public void doRefresh() {
                            mLoadMoreWrapper.notifyDataSetChanged();
                        }
                    });
                    Intent intent = new Intent(mContext, MzituAlbumBrowseActivity.class);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                mDatas.get(position).is_love = 0;
                mDatas.get(position).love_time = System.currentTimeMillis();
                AlbumInfo newInfo = new AlbumInfo(mDatas.get(position));
                DataManager.getInstance(mContext).setFavoriteStatus(newInfo);
                mLoadMoreWrapper.notifyDataSetChanged();
                int picMzituPos = DataManager.getInstance(mContext).removeLovePicMzituList(newInfo.album_address);
                if (picMzituPos >= 0) {
                    ((BeautyApplication) mContext.getApplicationContext()).handleMzituLoveRefreshCallBack(picMzituPos, 0);
                }
                return true;
            }
        });
        ((BeautyApplication) mContext.getApplicationContext()).setLoveRefreshCallBack(new BeautyApplication.LoveRefreshInterface() {
            @Override
            public void doLoveRefresh(int pos, int isLove) {
                mLoadMoreWrapper.notifyDataSetChanged();
            }
        });
        loadNewAd();
        return view;
    }

    private void loadMore() {
        ProgressBar mLoadingPb = mLoadMoreWrapper.getmLoadMoreHolder().getView(R.id.loading_progress);
        TextView mLoadingTx = mLoadMoreWrapper.getmLoadMoreHolder().getView(R.id.loading_text);
        if (Utils.isNetworkAvaialble(mContext)) {
            mLoadingPb.setVisibility(View.INVISIBLE);
            mLoadingTx.setText(R.string.rv_list_end);
        } else {
            mLoadingPb.setVisibility(View.INVISIBLE);
            mLoadingTx.setText(R.string.network_not_available);
        }
    }

    private void loadNewAd() {
        final Button closeButton = new Button(mContext);

        adView = new BannerView((Activity) mContext, ADSize.BANNER, Constants.APPID, Constants.BannerPosID);
        //设置广告轮播时间，为0或30~120之间的数字，单位为s,0标识不自动轮播
        adView.setRefresh(30);
        adView.setADListener(new AbstractBannerADListener() {
            @Override
            public void onADClicked() {
                adView.destroy();
                closeButton.setVisibility(View.GONE);
                super.onADClicked();
            }

            @Override
            public void onNoAD(int arg0) {
                closeButton.setVisibility(View.GONE);
                Log.i("AD_DEMO", "BannerNoAD，eCode=" + arg0);
            }

            @Override
            public void onADReceiv() {
                closeButton.setVisibility(View.VISIBLE);
                Log.i("AD_DEMO", "ONBannerReceive");
            }
        });
        /* 发起广告请求，收到广告数据后会展示数据   */
        adView.loadAD();
        // 将adView添加到父控件中(注：该父控件不一定为您的根控件，只要该控件能通过addView能添加广告视图即可)
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

    private BroadcastReceiver mFavReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_FAV_REFRESH.equals(action)) {
                mLoadMoreWrapper.notifyDataSetChanged();
            }
        }
    };

    private void initDatas() {
        mDatas = DataManager.getInstance(mContext).getmPicFavListData();

        IntentFilter filter = new IntentFilter(Constants.ACTION_FAV_REFRESH);
        mContext.registerReceiver(mFavReceiver, filter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        adView.destroy();
        super.onDestroyView();
        StatsWrapper.onPageEnd(mContext, StatsReportConstants.ENTRY_PAGE_FAVORITE_FRAGMENT);
        unbinder.unbind();
        mContext.unregisterReceiver(mFavReceiver);
    }

    @Override
    public void onResume() {
        if (!Utils.isNetworkAvaialble(mContext)) {
            Toast.makeText(mContext, R.string.network_not_available, Toast.LENGTH_SHORT).show();
        } else if (Utils.isNetworkMobile(mContext)) {
            Toast.makeText(mContext, R.string.access_mobile_network, Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
