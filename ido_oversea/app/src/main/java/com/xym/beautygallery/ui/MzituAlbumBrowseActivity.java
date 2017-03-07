package com.xym.beautygallery.ui;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.squareup.picasso.Picasso;
import com.xym.beautygallery.R;
import com.xym.beautygallery.ad.AdConstants;
import com.xym.beautygallery.ad.AdManager;
import com.xym.beautygallery.ad.NativeAd;
import com.xym.beautygallery.appinfo.AppManager;
import com.xym.beautygallery.appinfo.AppRecommend;
import com.xym.beautygallery.base.AppConfigMgr;
import com.xym.beautygallery.base.BaseSwipeBackActivity;
import com.xym.beautygallery.base.BeautyApplication;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.download.DownloadService;
import com.xym.beautygallery.module.AlbumInfo;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.module.PicInfo;
import com.xym.beautygallery.utils.PackageUtils;
import com.xym.beautygallery.utils.Utils;
import com.xym.beautygallery.view.MyStaggeredGridLayoutManager;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.EmptyWrapper;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 11/8/16.
 */
public class MzituAlbumBrowseActivity extends BaseSwipeBackActivity {
    @BindView(R.id.main_photo_rv)
    RecyclerView mainPhotoRv;
    @BindView(R.id.main_ad_root)
    RelativeLayout mainAdRoot;

    private AdView adView;
    private List<PicInfo> mDatas;
    private CommonAdapter<PicInfo> mAdapter;
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;
    private EmptyWrapper mEmptyWrapper;
    private LoadMoreWrapper mLoadMoreWrapper;
    private int mHeaderCount;

    private LayoutInflater mInflater;
    private View mHeader;
    private RelativeLayout.LayoutParams reLayoutParams;

    private AlbumInfo mAlbumInfo;
    private boolean isFromFav;

    private CustomDialog mDownloadDialog;
    private DownloadService mDownloadService;

    private int calcItemPosition(int position) {
        int itemPosition = position - mHeaderCount;
        if (itemPosition < 0) {
            itemPosition = 0;
        } else if (itemPosition >= mDatas.size()) {
            itemPosition = mDatas.size() - 1;
        }
        return itemPosition;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatsWrapper.onPageStart(mContext, StatsReportConstants.ENTRY_PAGE_BROWSE_ACTIVITY);
        StatsWrapper.onEvent(MzituAlbumBrowseActivity.this, StatsReportConstants.ALBUM_ACTIVITY_SHOW_EVENT_ID);
        mContext = this;
        setContentView(R.layout.activity_mzitu_album_browse);
        ButterKnife.bind(this);

        mInflater = LayoutInflater.from(this);
        getActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.main_fragment_mzitu);

        initDatas();
        mainPhotoRv.setLayoutManager(new MyStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new CommonAdapter<PicInfo>(mContext, R.layout.mzitu_album_browse_item, mDatas) {
            @Override
            protected void convert(ViewHolder holder, PicInfo s, int position) {
                int itemPosition = calcItemPosition(position);
                ImageView photoIv = holder.getView(R.id.image);
                WindowManager wm = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                int lcdWidth = wm.getDefaultDisplay().getWidth();

                int widthIv = lcdWidth / 2;
                int heightIv = (int) (((double) (widthIv * mDatas.get(itemPosition).pic_height)) / mDatas.get(itemPosition).pic_width);
                ViewGroup.LayoutParams para;
                para = photoIv.getLayoutParams();
                para.height = heightIv;
                para.width = widthIv;
                photoIv.setLayoutParams(para);

                Picasso.with(mContext).load(mDatas.get(itemPosition).pic_url_address).config(Bitmap.Config.RGB_565).into(photoIv);
            }

            @Override
            protected void convertAd(ViewHolder holder, NativeAd t, int position) {
                ImageView photoIv = holder.getView(R.id.image);
                WindowManager wm = (WindowManager) mContext
                        .getSystemService(Context.WINDOW_SERVICE);
                int lcdWidth = wm.getDefaultDisplay().getWidth();

                int widthIv = lcdWidth / 2;
                ViewGroup.LayoutParams para;
                para = photoIv.getLayoutParams();
                para.height = widthIv * 4 / 3;
                para.width = widthIv;
                photoIv.setLayoutParams(para);
                photoIv.setScaleType(ImageView.ScaleType.FIT_XY);

                Picasso.with(mContext).load(t.imgUrl).config(Bitmap.Config.RGB_565).into(photoIv);
                t.performClick(holder.getConvertView(), AdConstants.ALBUM_AD_ID);
            }
        };
        mAdapter.setNativeAdEnable(AdManager.getInstance(this).enableAd(AdConstants.ALBUM_AD_ID));
        mAdapter.setNativeId(AdConstants.ALBUM_AD_ID);
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
//        mainPhotoRv.setFocusable(false);
        mAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                int itemPosition = calcItemPosition(position);
                DataManager.getInstance(mContext).setmCurrentIndex(itemPosition);
                Intent intent = new Intent(mContext, ImageDetailsActivity.class);
                mContext.startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return true;
            }
        });

        Utils.isNeedAccessPermission(mContext);
        if (AppConfigMgr.getNeedRate(mContext)) {
            long countTimes = AppConfigMgr.getCountTimes(mContext);
            if (countTimes == 50 ||
                    countTimes == 200 ||
                    countTimes == 500 ||
                    countTimes % 500 == 0) {
                StatsWrapper.onEvent(mContext, StatsReportConstants.RATE_US_EVENT_ID, "do");
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.update_confirm)
                        .setMessage(R.string.rate_us)
                        .setNegativeButton(
                                R.string.rate_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        doMark(mContext);
                                        StatsWrapper.onEvent(mContext, StatsReportConstants.RATE_US_EVENT_ID, "success");
                                        AppConfigMgr.setNeedRate(mContext, false);
                                    }
                                })
                        .setPositiveButton(R.string.rate_refuse,
                                null).show();
            }
            AppConfigMgr.setCountTimes(mContext, countTimes + 1);
        }
//        loadNewAd();
        loadFBAd();

        IntentFilter filter1 = new IntentFilter(Constants.ACTION_DOWNLOAD_STATUS_UPDATE);
        filter1.addAction(Constants.ACTION_DOWNLOAD_PERCENT_UPDATE);
        this.registerReceiver(mDownloadReceiver1, filter1);

        IntentFilter filter2 = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter2.addDataScheme("package");
        this.registerReceiver(mDownloadReceiver2, filter2);
        mDownloadService = DownloadService.getInstance(this);
        checkoutAppRecommend();
    }

    private void loadFBAd() {
        final Button closeButton = new Button(mContext);
        adView = new com.facebook.ads.AdView(mContext, "800529513419419_800542710084766", AdSize.BANNER_320_50);
        adView.loadAd();
        Log.e("MzituAlbumBrowse", "loadFBAd ");

        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e("MzituAlbumBrowse", "onError " + adError.getErrorCode());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.e("MzituAlbumBrowse", "onAdLoaded ");
                closeButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.e("MzituAlbumBrowse", "onAdClicked ");
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

    private void doMark(Context ctx) {
        try {
            Uri uri = Uri.parse("market://details?id=" + ctx.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(Intent.createChooser(intent, ctx.getString(R.string.setting_marking_chooser)));
        } catch (ActivityNotFoundException e) {
//            Toast.makeText(mContext,"Couldn't launch the market!",Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMore() {
        ProgressBar mLoadingPb = mLoadMoreWrapper.getmLoadMoreHolder().getView(R.id.loading_progress);
        TextView mLoadingTx = mLoadMoreWrapper.getmLoadMoreHolder().getView(R.id.loading_text);
        if (Utils.isNetworkAvaialble(mContext)) {

            if (mDatas.size() > 0) {
                mLoadingPb.setVisibility(View.INVISIBLE);
                mLoadingTx.setText(R.string.rv_list_end);
            }

        } else {
            mLoadingPb.setVisibility(View.INVISIBLE);
            mLoadingTx.setText(R.string.network_not_available);
        }
    }

    private void initHeaderAndFooter() {
        mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(mAdapter);
        mHeader = mInflater.inflate(R.layout.mzitu_album_header, null);
        ((TextView) mHeader.findViewById(R.id.album_name)).setText(mAlbumInfo.album_name);
        if (Utils.isZh(mContext)) {
            mHeaderAndFooterWrapper.addHeaderView(mHeader);
        }
        mHeaderCount = mHeaderAndFooterWrapper.getHeadersCount();

        mHeader.setFocusable(false);
    }

    private BroadcastReceiver mBrowseReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_BROWSE_REFRESH.equals(action)) {
                if (mDatas.size() != 0) {
                    mLoadMoreWrapper.notifyDataSetChanged();
                    MzituAlbumBrowseActivity.this.getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
                }
            }
        }
    };

    private void initDatas() {
        mDatas = DataManager.getInstance(mContext).getmPicListBrowseData();
        mAlbumInfo = DataManager.getInstance(mContext).getmCurrentAlbum();
        isFromFav = DataManager.getInstance(mContext).isFromFav();
        DataManager.getInstance(mContext).requestBrowseDataFromServerStart();

        IntentFilter filter = new IntentFilter(Constants.ACTION_BROWSE_REFRESH);
        mContext.registerReceiver(mBrowseReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        StatsWrapper.onResume(this);
        if (DataManager.getInstance(mContext).isNeedQuit()) {
            DataManager.getInstance(mContext).setIsNeedQuit(false);
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        StatsWrapper.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mzitu_album_browse_menu, menu);
        MenuItem mActionFavBtn = menu.findItem(R.id.action_fav_btn);
        MenuItem mActionFavTx = menu.findItem(R.id.action_fav_tx);
        if (mDatas.size() > 0) {
            if (mAlbumInfo.is_love > 0) {
                mActionFavBtn.setIcon(R.mipmap.heart_solid);
                mActionFavBtn.setTitle(R.string.image_deatil_action_not_favorite);
                mActionFavTx.setTitle(R.string.image_deatil_action_not_favorite);
            } else {
                mActionFavBtn.setIcon(R.mipmap.heart_hollow);
                mActionFavBtn.setTitle(R.string.image_deatil_action_favorite);
                mActionFavTx.setTitle(R.string.image_deatil_action_favorite);
            }
        } else {
            mActionFavBtn.setVisible(false);
            mActionFavTx.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mActionFavBtn = menu.findItem(R.id.action_fav_btn);
        MenuItem mActionFavTx = menu.findItem(R.id.action_fav_tx);
        if (mDatas.size() > 0) {
            if (mAlbumInfo.is_love > 0) {
                mActionFavBtn.setIcon(R.mipmap.heart_solid);
                mActionFavBtn.setTitle(R.string.image_deatil_action_not_favorite);
                mActionFavTx.setTitle(R.string.image_deatil_action_not_favorite);
            } else {
                mActionFavBtn.setIcon(R.mipmap.heart_hollow);
                mActionFavBtn.setTitle(R.string.image_deatil_action_favorite);
                mActionFavTx.setTitle(R.string.image_deatil_action_favorite);
            }
        } else {
            mActionFavBtn.setVisible(false);
            mActionFavTx.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_fav_btn || item.getItemId() == R.id.action_fav_tx) {
            if (isFromFav) {
                mAlbumInfo.is_love = 0;
                mAlbumInfo.love_time = System.currentTimeMillis();
                AlbumInfo newInfo = new AlbumInfo(mAlbumInfo);
                int lovePos = DataManager.getInstance(mContext).setFavoriteStatus(newInfo);
                ((BeautyApplication) mContext.getApplicationContext()).handleRefreshCallBack();
                int picMzituPos = DataManager.getInstance(mContext).removeLovePicMzituList(newInfo.album_address);
                if (picMzituPos >= 0) {
                    ((BeautyApplication) mContext.getApplicationContext()).handleMzituLoveRefreshCallBack(picMzituPos, 0);
                }
                finish();
            } else {
                if (mAlbumInfo.is_love > 0) {
                    mAlbumInfo.is_love = 0;
                } else {
                    mAlbumInfo.is_love = 1;
                }
                mAlbumInfo.love_time = System.currentTimeMillis();
                AlbumInfo newInfo = new AlbumInfo(mAlbumInfo);
                int lovePos = DataManager.getInstance(mContext).setFavoriteStatus(newInfo);
                ((BeautyApplication) mContext.getApplicationContext()).handleLoveRefreshCallBack(lovePos, mAlbumInfo.is_love);
                ((BeautyApplication) mContext.getApplicationContext()).handleRefreshCallBack();
                this.getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        adView.destroy();
        super.onDestroy();
        StatsWrapper.onPageEnd(mContext, StatsReportConstants.ENTRY_PAGE_BROWSE_ACTIVITY);
        DataManager.getInstance(this).requestBrowseDataFromServerStop();
        mContext.unregisterReceiver(mBrowseReceiver);
        mContext.unregisterReceiver(mDownloadReceiver1);
        mContext.unregisterReceiver(mDownloadReceiver2);
        if (mDownloadDialog != null) {
            mDownloadDialog.dismiss();
        }
    }


    private void showDownloadConfirmDialog(final AppRecommend app) {
        if (mDownloadDialog == null) {
            mDownloadDialog = new CustomDialog(mContext);
        }
        mDownloadDialog.setTitle(R.string.recommend_app_tips);
        View dialogView = mDownloadDialog.getLayoutInflater().inflate(R.layout.app_recommend_dialog, null);
        Picasso.with(mContext).load(app.iconUrl).config(Bitmap.Config.RGB_565).into((ImageView) dialogView.findViewById(R.id.recommend_find_dialog_icon));
        TextView contentName = (TextView) dialogView.findViewById(R.id.recommend_find_name);
        TextView contentSize = (TextView) dialogView.findViewById(R.id.recommend_find_size);
        TextView contentDesc = (TextView) dialogView.findViewById(R.id.recommend_find_desc);
        contentName.setText(app.appName);
        contentSize.setText(Utils.formatFlowNumWithUnit(app.pkgSize));
        contentDesc.setText(app.pkgDes);
        mDownloadDialog.setContentView(dialogView);

        mDownloadDialog.setCancelBtn(R.string.recommend_app_download_cancel, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MzituAlbumBrowseActivity.this.finish();
            }
        });

        mDownloadDialog.setOkBtn(R.string.recommend_app_download_ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = mDownloadService.getTaskStatus(app.pkgUrl, app.pkgName);
                if (status == mDownloadService.STATUS_DOWNLOAD) {
                    mDownloadService.startCommand(app.pkgUrl, null, false);
                    AppManager.getInstance(mContext).setmWaitApkInstall(true);
                    StatsWrapper.onEvent(MzituAlbumBrowseActivity.this, StatsReportConstants.CUSTOM_APP_AD_EVENT_ID,
                            StatsReportConstants.CUSTOM_APP_AD_LABEL_DOWNLOAD);
                } else if (status == mDownloadService.STATUS_DOWNLOADING) {

                } else if (status == mDownloadService.STATUS_INSTALL) {
                    mDownloadService.startCommand(app.pkgUrl, null, false);
                    AppManager.getInstance(mContext).setmWaitApkInstall(true);
                }
            }
        }, false);

        int status = mDownloadService.getTaskStatus(app.pkgUrl, app.pkgName);
        if (status == mDownloadService.STATUS_DOWNLOAD) {
            mDownloadDialog.getmOkBtn().setText(R.string.apk_download);
            mDownloadDialog.getmOkBtn().setEnabled(true);
        } else if (status == mDownloadService.STATUS_DOWNLOADING) {
            mDownloadDialog.getmOkBtn().setText(R.string.apk_downloading);
            mDownloadDialog.getmOkBtn().setEnabled(false);
        } else if (status == mDownloadService.STATUS_INSTALL) {
            mDownloadDialog.getmOkBtn().setText(R.string.apk_install_without_download);
            mDownloadDialog.getmOkBtn().setEnabled(true);
        }

        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    MzituAlbumBrowseActivity.this.finish();
                    return true;
                } else {
                    return false;
                }
            }
        });
        mDownloadDialog.show();
    }

    private BroadcastReceiver mDownloadReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            AppRecommend tempApp = AppManager.getInstance(mContext).getmCurrentAppRecommend();
            if (Constants.ACTION_DOWNLOAD_STATUS_UPDATE.equals(action)) {
                if (tempApp != null) {
                    int status = mDownloadService.getTaskStatus(tempApp.pkgUrl, tempApp.pkgName);
                    if (status == mDownloadService.STATUS_DOWNLOAD) {
                        mDownloadDialog.getmOkBtn().setText(R.string.apk_download);
                        mDownloadDialog.getmOkBtn().setEnabled(true);
                    } else if (status == mDownloadService.STATUS_DOWNLOADING) {
                        mDownloadDialog.getmOkBtn().setText(R.string.apk_downloading);
                        mDownloadDialog.getmOkBtn().setEnabled(false);
                    } else if (status == mDownloadService.STATUS_INSTALL) {
                        StatsWrapper.onEvent(MzituAlbumBrowseActivity.this, StatsReportConstants.CUSTOM_APP_AD_EVENT_ID,
                                StatsReportConstants.CUSTOM_APP_AD_LABEL_DOWNLOAD_SUCCESS);
                        mDownloadDialog.getmOkBtn().setText(R.string.apk_install);
                        mDownloadDialog.getmOkBtn().setEnabled(true);
                    }
                }
            } else if (Constants.ACTION_DOWNLOAD_PERCENT_UPDATE.equals(action)) {
                if (mDownloadDialog != null) {
                    int percent = intent.getIntExtra("percent", 0);
                    if (percent > 0) {
                        mDownloadDialog.getmOkBtn().setText(getString(R.string.recommend_app_downloading, percent));
                    } else {
                        mDownloadDialog.getmOkBtn().setText(R.string.apk_downloading);
                    }
                }
            }
        }
    };

    private BroadcastReceiver mDownloadReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            AppRecommend tempApp = AppManager.getInstance(mContext).getmCurrentAppRecommend();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                String packageName = intent.getDataString();

                if (tempApp != null && packageName.contains(tempApp.pkgName)) {
                    if (mDownloadDialog != null) {
                        mDownloadDialog.dismiss();
                    }
                    PackageUtils.startupApp(mContext, tempApp.pkgName);
                    StatsWrapper.onEvent(MzituAlbumBrowseActivity.this, StatsReportConstants.CUSTOM_APP_AD_EVENT_ID,
                            StatsReportConstants.CUSTOM_APP_AD_LABEL_START);
                }
            }
        }
    };

    private void checkoutAppRecommend() {
        AppManager.getInstance(mContext).findCurrentAppRecommend();
        AppRecommend tempApp = AppManager.getInstance(mContext).getmCurrentAppRecommend();

        long showCountConfig = AppManager.getInstance(mContext).getMzituAlbumCountConfig();
        long showCount = AppConfigMgr.getAppAdCountTimes(mContext);
        if (tempApp != null && showCountConfig > 0 && showCount > showCountConfig
                && AppConfigMgr.getAppAdLast(mContext) == false) {
            int status = mDownloadService.getTaskStatus(tempApp.pkgUrl, tempApp.pkgName);
            if (status == mDownloadService.STATUS_DOWNLOAD ||
                    status == mDownloadService.STATUS_DOWNLOADING ||
                    status == mDownloadService.STATUS_INSTALL) {
                showDownloadConfirmDialog(tempApp);
                StatsWrapper.onEvent(MzituAlbumBrowseActivity.this, StatsReportConstants.CUSTOM_APP_AD_EVENT_ID,
                        StatsReportConstants.CUSTOM_APP_AD_LABEL_SHOW);
            }
        } else {
            showCount += 1;
            AppConfigMgr.setAppAdCountTimes(mContext, showCount);
        }
    }
}
