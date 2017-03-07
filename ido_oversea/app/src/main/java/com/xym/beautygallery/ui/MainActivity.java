package com.xym.beautygallery.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.ActivityTack;
import com.xym.beautygallery.base.AppConfigMgr;
import com.xym.beautygallery.base.BaseActivity;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.base.stats.StatsReportConstants;
import com.xym.beautygallery.base.stats.StatsWrapper;
import com.xym.beautygallery.fragment.MainFragment;
import com.xym.beautygallery.fragment.NavigationDrawerFragment;
import com.xym.beautygallery.fragment.SettingFragment;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.utils.T;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    ActionBar ab;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;

    MainFragment mMainFragment;
    SettingFragment mSettingFragment;

    private View mFragmentContainerView;

    public static final long TWO_SECOND = 2 * 1000;
    long preTime;

    private ConnectivityManager mConnectivityManager;
    private NetworkInfo netInfo;
    private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                mConnectivityManager = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                netInfo = mConnectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isAvailable()) {
                    DataManager.getInstance(mContext).requestDataAgain();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addShortcut();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFragmentContainerView = (View) findViewById(R.id.navigation_drawer);
        onNavigationDrawerItemSelected(0);
        initDrawer();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(mHomeReceiver, filter);
    }

    private void initDrawer() {
        // TODO Auto-generated method stub
        ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);// 给home icon的左边加上一个返回的图标
        ab.setHomeButtonEnabled(true);// 需要api level 14 使用home-icon 可点击

        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                drawerArrow, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
//        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
//                mDrawerLayout);

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
        super.onDestroy();
        this.unregisterReceiver(mHomeReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(mFragmentContainerView)) {
                drawerLayout.closeDrawer(mFragmentContainerView);
            } else {
                drawerLayout.openDrawer(mFragmentContainerView);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (mMainFragment != null) {
            transaction.hide(mMainFragment);
            StatsWrapper.onPageEnd(mContext, StatsReportConstants.ENTRY_PAGE_INTRODUCE_ACTIVITY);
        }

        if (mSettingFragment != null) {
            transaction.hide(mSettingFragment);
            StatsWrapper.onPageEnd(mContext, StatsReportConstants.ENTRY_PAGE_SETTING_ACTIVITY);
        }
    }

    public void closeDrawer() {
        drawerLayout.closeDrawers();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // 开启一个Fragment事务
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);

        switch (position) {
            case Constants.ACTION_MAIN:
                StatsWrapper.onPageStart(mContext, StatsReportConstants.ENTRY_PAGE_INTRODUCE_ACTIVITY);
                closeDrawer();
                if (mMainFragment == null) {
                    mMainFragment = new MainFragment();
                    transaction.add(R.id.container, mMainFragment);
                } else {
                    transaction.show(mMainFragment);
                }
                transaction.commit();
                break;

            case Constants.ACTION_SETTING:
                StatsWrapper.onPageStart(mContext, StatsReportConstants.ENTRY_PAGE_SETTING_ACTIVITY);
                closeDrawer();
                if (mSettingFragment == null) {
                    mSettingFragment = new SettingFragment();
                    transaction.add(R.id.container, mSettingFragment);
                } else {
                    transaction.show(mSettingFragment);
                }
                transaction.commit();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 截获后退键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTime = new Date().getTime();

            // 如果时间间隔大于2秒, 不处理
            if ((currentTime - preTime) > TWO_SECOND) {
                // 显示消息
                T.showShort(mContext, mContext.getResources().getString(R.string.on_back_pressed_prompt));

                // 更新时间
                preTime = currentTime;

                // 截获事件,不再处理
                return true;
            } else {
                ActivityTack.getInstanse().exit(mContext);
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void addShortcut() {
        if (!AppConfigMgr.isShortcutSetup(this)) {
            Intent addShortcutIntent = new Intent(Constants.ACTION_ADD_SHORTCUT);
            // 不允许重复创建
            addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
            // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
            // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
            // 屏幕上没有空间时会提示
            // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式

            // 名字
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));

            // 图标
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this,
                            R.mipmap.ic_launcher));

            // 设置关联程序
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.setClass(this, MainActivity.class);
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            addShortcutIntent
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            // 发送广播
            this.sendBroadcast(addShortcutIntent);
            AppConfigMgr.setShortcutSetup(this, true);
        }
    }
}
