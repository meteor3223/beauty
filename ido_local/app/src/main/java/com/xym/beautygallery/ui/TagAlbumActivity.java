package com.xym.beautygallery.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.lid.lib.LabelImageView;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.squareup.picasso.Picasso;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.BaseSwipeBackActivity;
import com.xym.beautygallery.base.BeautyApplication;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.module.AlbumInfo;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.module.PicInfo;
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
 * Created by root on 11/4/16.
 */
public class TagAlbumActivity extends BaseSwipeBackActivity {
    @BindView(R.id.main_photo_rv)
    RecyclerView mainPhotoRv;
    @BindView(R.id.main_ad_root)
    RelativeLayout mainAdRoot;
    private BannerView adView;

    private List<AlbumInfo> mDatas;
    private CommonAdapter<AlbumInfo> mAdapter;
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;
    private EmptyWrapper mEmptyWrapper;
    private LoadMoreWrapper mLoadMoreWrapper;
    private int mHeaderCount;

    private LayoutInflater mInflater;
    private View mHeader;
    private RelativeLayout.LayoutParams reLayoutParams;

    private int calcItemPosition(int position) {
        int itemPosition = position - mHeaderCount;
        if (itemPosition < 0) {
            itemPosition = 0;
        } else if (itemPosition >= mDatas.size()) {
            itemPosition = mDatas.size() - 1;
        }
        return itemPosition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatsWrapper.onPageStart(mContext, StatsReportConstants.ENTRY_PAGE_CLASSIFY_DETAIL_ACTIVITY);
        mContext = this;
        setContentView(R.layout.activity_tag_album);
        ButterKnife.bind(this);

        mInflater = LayoutInflater.from(this);
        getActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(DataManager.getInstance(mContext).getmCurrentTag());

        initDatas();
        mainPhotoRv.setLayoutManager(new MyGridLayoutManager(mContext, 3));
        mainPhotoRv.addItemDecoration(new RBaseItemDecoration(Constants.DEFAULT_ITEM_DECORATION));
        mAdapter = new CommonAdapter<AlbumInfo>(mContext, R.layout.main_photo_item, mDatas) {
            @Override
            protected void convert(ViewHolder holder, AlbumInfo s, int position) {
                int itemPosition = calcItemPosition(position);
                TextView photoPics = holder.getView(R.id.main_photo_item_pics_tv);
                LabelImageView photoIv = holder.getView(R.id.main_photo_item_iv);

                WindowManager wm = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                int lcdWidth = wm.getDefaultDisplay().getWidth();

                int widthIv = lcdWidth / 3;

                int heightIv = (int) (((double) ((widthIv - 10) * mDatas.get(itemPosition).album_height)) / mDatas.get(itemPosition).album_width);

                ViewGroup.LayoutParams para;
                para = photoIv.getLayoutParams();
                para.height = heightIv;
                para.width = widthIv;
                photoIv.setLayoutParams(para);

                if (mDatas.get(itemPosition).is_love > 0) {
                    photoIv.setLabelVisual(true);
                } else {
                    photoIv.setLabelVisual(false);
                }
                Picasso.with(mContext).load(mDatas.get(itemPosition).album_thumb).config(Bitmap.Config.RGB_565).into(photoIv);
                photoPics.setText(mDatas.get(itemPosition).album_pics);
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
                final int itemPosition = calcItemPosition(position);
                AlbumInfo currentAlbum = mDatas.get(itemPosition);
                if (currentAlbum != null) {
                    DataManager.getInstance(mContext).setmCurrentAlbum(currentAlbum);
                    DataManager.getInstance(mContext).setIsFromFav(false);
                    ((BeautyApplication) mContext.getApplicationContext()).setRefreshCallBack(new BeautyApplication.RefreshInterface() {
                        @Override
                        public void doRefresh() {
                            mLoadMoreWrapper.notifyItemChanged(itemPosition);
                        }
                    });
                    Intent intent = new Intent(mContext, MzituAlbumBrowseActivity.class);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                int itemPosition = calcItemPosition(position);
                if (mDatas.get(itemPosition).is_love > 0) {
                    mDatas.get(itemPosition).is_love = 0;
                } else {
                    mDatas.get(itemPosition).is_love = 1;
                }
                mDatas.get(itemPosition).love_time = System.currentTimeMillis();
                AlbumInfo newInfo = new AlbumInfo(mDatas.get(itemPosition));
                int lovePos = DataManager.getInstance(mContext).setFavoriteStatus(newInfo);
                ((BeautyApplication) mContext.getApplicationContext()).handleLoveRefreshCallBack(lovePos, mDatas.get(itemPosition).is_love);
                mLoadMoreWrapper.notifyItemChanged(itemPosition);
                return true;
            }
        });
        loadNewAd();
    }

    private void loadMore() {
        ProgressBar mLoadingPb = mLoadMoreWrapper.getmLoadMoreHolder().getView(R.id.loading_progress);
        TextView mLoadingTx = mLoadMoreWrapper.getmLoadMoreHolder().getView(R.id.loading_text);
        if (Utils.isNetworkAvaialble(mContext)) {
            int countLoadBefore = mDatas.size();
            int countLoadCount = DataManager.getInstance(mContext).picTagPushCache(Constants.PIC_PUSH_CACHE_NUMBER);
            if (countLoadBefore == 0) {
                mLoadMoreWrapper.notifyDataSetChanged();
            } else {
                mLoadMoreWrapper.notifyItemRangeInserted(countLoadBefore + mHeaderCount, countLoadCount);
            }
            if (countLoadCount == 0 && countLoadBefore > mHeaderCount) {
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
        String tagDes = DataManager.getInstance(mContext).getmPicTagAlbumListData().tag_des;
        if (tagDes != null) {
            if (!tagDes.isEmpty()) {
                mHeader = mInflater.inflate(R.layout.mzitu_album_header, null);
                ((TextView) mHeader.findViewById(R.id.album_name)).setText(tagDes);
                mHeaderAndFooterWrapper.addHeaderView(mHeader);
                mHeaderCount = mHeaderAndFooterWrapper.getHeadersCount();

                mHeader.setFocusable(false);
            }
        }
    }

    private BroadcastReceiver mTagReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_TAG_REFRESH.equals(action)) {
                if (mDatas.size() == 0) {
                    DataManager.getInstance(mContext).requestTagPicAlbumDataFromServerStart();
                    mLoadMoreWrapper.notifyDataSetChanged();
                }
            }
        }
    };

    private void initDatas() {
        mDatas = DataManager.getInstance(mContext).getmPicTagListData();
        DataManager.getInstance(mContext).requestTagPicAlbumDataFromServerStart();

        IntentFilter filter = new IntentFilter(Constants.ACTION_TAG_REFRESH);
        mContext.registerReceiver(mTagReceiver, filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        StatsWrapper.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        StatsWrapper.onPause(this);
    }

    @Override
    protected void onDestroy() {
        adView.destroy();
        super.onDestroy();
        StatsWrapper.onPageEnd(mContext, StatsReportConstants.ENTRY_PAGE_CLASSIFY_DETAIL_ACTIVITY);
        DataManager.getInstance(mContext).requestTagPicAlbumDataFromServerStop();
        mContext.unregisterReceiver(mTagReceiver);
    }
}
