package com.xym.beautygallery.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import com.xym.beautygallery.R;
import com.xym.beautygallery.base.BaseSwipeBackActivity;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 7/17/16.
 */
public class AboutActivity extends BaseSwipeBackActivity {
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.version)
    TextView version;
    @BindView(R.id.image)
    ImageView mImageView;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        mBackground = mImageView;
        moveBackground();
        getActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.about_as);
        name.setText(R.string.app_name);
        try {
            PackageInfo pkg = getPackageManager().getPackageInfo(getPackageName(), -1);
            version.setText(getString(R.string.wifimgr_version, pkg.versionName));
        } catch (PackageManager.NameNotFoundException e) {
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
        overridePendingTransition(0, 0);
        StatsWrapper.onPause(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (Utils.hasHoneycomb()) {
            View demoContainerView = findViewById(R.id.image);
            demoContainerView.setAlpha(0);
            ViewPropertyAnimator animator = demoContainerView.animate();
            animator.alpha(1);
            if (Utils.hasICS()) {
                animator.setStartDelay(250);
            }
            animator.setDuration(1000);
        }
    }
}
