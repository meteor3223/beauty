package com.xym.beautygallery.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xym.beautygallery.R;
import com.xym.beautygallery.ad.AdConstants;
import com.xym.beautygallery.ad.AdManager;
import com.xym.beautygallery.ad.AdUtils;
import com.xym.beautygallery.ad.NativeAd;
import com.xym.beautygallery.base.BaseSwipeBackActivity;
import com.xym.beautygallery.base.BeautyApplication;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.module.AlbumInfo;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.module.PicInfo;
import com.xym.beautygallery.utils.Utils;
import com.xym.beautygallery.view.ExtendedViewPager;
import com.xym.beautygallery.view.TouchImageView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 10/8/16.
 */
public class ImageDetailsActivity extends BaseSwipeBackActivity implements OnPageChangeListener {
    @BindView(R.id.view_pager)
    ExtendedViewPager viewPager;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.main_ad_root)
    RelativeLayout mainAdRoot;
    private AdView adView;

    private Context mContext;
    private boolean isFromFav;
    private int imagePosition;
    private int imageAndAdPosition;
    private int imageTotal;
    private int imageAndAdTotal;
    private List<PicInfo> mDatas;
    private TouchImageAdapter mAdapter;
    private AlbumInfo mAlbumInfo;
    private int mAdGap;
    private boolean isAdEnable = false;//AdManager.getInstance(mContext).enableAd(AdConstants.DEATIL_AD_ID);
    protected Map<Integer, NativeAd> mAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatsWrapper.onPageStart(mContext, StatsReportConstants.ENTRY_PAGE_BROWSE_ALBUM_ACTIVITY);
        mContext = this;
        setContentView(R.layout.activity_photo_detail);
        ButterKnife.bind(this);

        imagePosition = DataManager.getInstance(mContext).getmCurrentIndex();
        isFromFav = DataManager.getInstance(mContext).isFromFav();
        mAlbumInfo = DataManager.getInstance(mContext).getmCurrentAlbum();
        mDatas = DataManager.getInstance(mContext).getmPicListBrowseData();
        mAdGap = AdManager.getInstance(mContext).getNativeAdGap(AdConstants.DEATIL_AD_ID);
        mAds = new HashMap<>();
        if (isAdEnable) {
            if (mAdGap > 1) {
                isAdEnable = true;
            }
        }

        if (isAdEnable) {
            imageTotal = mDatas.size();
            imageAndAdTotal = mDatas.size() + mDatas.size() / (mAdGap - 1);
            imageAndAdPosition = imagePosition + imagePosition / (mAdGap - 1);
        } else {
            imageTotal = mDatas.size();
            imageAndAdTotal = mDatas.size();
        }


        getActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle((imagePosition + 1) + "/" + imageTotal);

        mAdapter = new TouchImageAdapter();
        viewPager.setAdapter(mAdapter);
        if (isAdEnable) {
            viewPager.setCurrentItem(imageAndAdPosition);
        } else {
            viewPager.setCurrentItem(imagePosition);
        }
        viewPager.setOnPageChangeListener(this);
//        loadNewAd();
        loadFBAd();
    }

    private void loadFBAd() {
        final Button closeButton = new Button(mContext);
        adView = new com.facebook.ads.AdView(mContext, "800529513419419_800542710084766", AdSize.BANNER_320_50);
        adView.loadAd();
        Log.e("ImageDetailsActivity", "loadFBAd ");

        adView.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e("ImageDetailsActivity", "onError " + adError.getErrorCode());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.e("ImageDetailsActivity", "onAdLoaded ");
                closeButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.e("ImageDetailsActivity", "onAdClicked ");
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


    private class TouchImageAdapter extends PagerAdapter {
        private LayoutInflater inflater;

        TouchImageAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return imageAndAdTotal;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View viewLayout = inflater.inflate(R.layout.photo_detail_item, container,
                    false);
            TouchImageView imageView = (TouchImageView) viewLayout
                    .findViewById(R.id.image);
            String url = "";
            if (isAdEnable) {
                if (AdUtils.isAdItem(position, mAdGap)) {
                    NativeAd ad = mAds.get(position);
                    if (ad == null) {
                        ad = AdManager.getInstance(mContext).randomAdInfo(AdConstants.DEATIL_AD_ID);
                        mAds.put(position, ad);
                    }
                    url = ad.imgUrl;
                    ad.performClick(imageView, AdConstants.DEATIL_AD_ID);
                } else {
                    int tempPosition = AdUtils.convertPos(position, mAdGap);
                    url = mDatas.get(tempPosition).pic_url_address;
                }
            } else {
                url = mDatas.get(position).pic_url_address;
            }
            loading.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(url).config(Bitmap.Config.RGB_565).into(imageView,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            loading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                        }
                    });
            container.addView(viewLayout, 0); // 将图片增加到ViewPager
            return viewLayout;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

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
        StatsWrapper.onPageEnd(mContext, StatsReportConstants.ENTRY_PAGE_BROWSE_ALBUM_ACTIVITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_detail_menu, menu);
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
        MenuItem mActionShareTx = menu.findItem(R.id.action_share_tx);
        MenuItem mActionDownloadTx = menu.findItem(R.id.action_download_tx);

        if (isAdEnable) {
            if (AdUtils.isAdItem(imageAndAdPosition, mAdGap)) {
                mActionFavBtn.setVisible(false);
                mActionFavTx.setVisible(false);
                mActionShareTx.setVisible(false);
                mActionDownloadTx.setVisible(false);
            } else {
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
            }
        } else {
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
                DataManager.getInstance(mContext).setIsNeedQuit(true);
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
        } else if (item.getItemId() == R.id.action_download_tx) {
            download();
        } else if (item.getItemId() == R.id.action_share_tx) {
            sharePic();
        }
        return super.onOptionsItemSelected(item);
    }

    private void download() {
        //获得图片的地址
        String url = mDatas.get(imagePosition).pic_url_address;

        //Target
        Target target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    // 首先保存图片
                    File appDir = new File(Constants.PIC_SAVE_PATH);
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }
                    String fileName = System.currentTimeMillis() + ".jpg";
                    File file = new File(appDir, fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();

                    // 其次把文件插入到系统图库
                    MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                            file.getAbsolutePath(), fileName, null);

                    // 最后通知图库更新
                    mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsoluteFile())));
                    Toast.makeText(mContext, R.string.image_download_success, Toast.LENGTH_SHORT).show();
                    StatsWrapper.onEvent(ImageDetailsActivity.this, StatsReportConstants.IMAGE_DOWNLOAD_EVENT_ID, "success");
                } catch (Exception e) {
                    Toast.makeText(mContext, R.string.image_download_failed, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    StatsWrapper.onEvent(ImageDetailsActivity.this, StatsReportConstants.IMAGE_DOWNLOAD_EVENT_ID, "fail");
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        //Picasso下载
        Picasso.with(this).load(url).into(target);
    }


    private void sharePic() {
        //获得图片的地址
        String url = mDatas.get(imagePosition).pic_url_address;

        //Target
        Target target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    // 首先保存图片
                    File appDir = new File(Constants.PIC_SAVE_PATH);
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }
                    String fileName = System.currentTimeMillis() + ".jpg";
                    File file = new File(appDir, fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();

                    Uri uriToImage = Uri.fromFile(file);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
                    shareIntent.setType("image/*");
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.pic_share_chooser)));
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        //Picasso下载
        Picasso.with(this).load(url).into(target);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        imagePosition = position;
        if (isAdEnable) {
            imageAndAdPosition = position;
            imagePosition = AdUtils.convertPos(position, mAdGap);
            if (AdUtils.isAdItem(position, mAdGap)) {
                getActionBar().setTitle(R.string.commom_ad);
            } else {
                getActionBar().setTitle((imagePosition + 1) + "/" + imageTotal);
            }
            this.getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
        } else {
            getActionBar().setTitle((imagePosition + 1) + "/" + imageTotal);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
