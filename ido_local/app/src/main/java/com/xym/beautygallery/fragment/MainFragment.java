package com.xym.beautygallery.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.titans.android.common.CustomFragment;
import com.titans.android.common.CustomFragmentRoot;
import com.titans.android.common.TabInfo;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.AppConfigMgr;
import com.xym.beautygallery.base.Constants;
import com.xym.beautygallery.module.DataManager;

import java.util.List;

/**
 * Created by root on 9/27/16.
 */
public class MainFragment extends CustomFragmentRoot {
    private int choiceIndex = -1;
    private BroadcastReceiver mChannelReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_CHANNEL_REFRESH.equals(action)) {
                MainFragment.this.getActivity().getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        IntentFilter filter = new IntentFilter(Constants.ACTION_CHANNEL_REFRESH);
        this.getActivity().registerReceiver(mChannelReceiver, filter);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        this.getActivity().unregisterReceiver(mChannelReceiver);
        super.onDestroy();
    }

    @Override
    public void preparePage(List<TabInfo> tabs) {
        tabs.add(new TabInfo(getString(R.string.main_fragment_mzitu), MzituPhotoFragment.class, getString(R.string.main_fragment_mzitu), -1, null));
        tabs.add(new TabInfo(getString(R.string.main_fragment_classify), MzituTagFragment.class, getString(R.string.main_fragment_classify), -1, null));
        tabs.add(new TabInfo(getString(R.string.main_fragment_favorite), PhotoFavoriteFragment.class, getString(R.string.main_fragment_favorite), -1, null));

        super.preparePage(tabs);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main_fragment, menu);
        MenuItem mSwitchBtn = menu.findItem(R.id.action_switch_btn);
        if (DataManager.getInstance(mContext).isMultiMode()) {
            mSwitchBtn.setVisible(true);
        } else {
            mSwitchBtn.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem mSwitchBtn = menu.findItem(R.id.action_switch_btn);
        if (DataManager.getInstance(mContext).isMultiMode()) {
            mSwitchBtn.setVisible(true);
        } else {
            mSwitchBtn.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_switch_btn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog_Alert);
            AlertDialog dialog;
            builder.setTitle(R.string.switch_photo_channel);
            final String[] choice;
            choiceIndex = -1;

            int urlIndex = DataManager.getInstance(mContext).getChannelIndex();
            List<String> channelListName = DataManager.getInstance(mContext).getChannelListName();
            final List<Integer> channelListUrl = DataManager.getInstance(mContext).getChannelListUrl();

            if (channelListName.size() > 0) {
                int N = channelListName.size();
                choice = new String[N];
                for (int i = 0; i < N; i++) {
                    choice[i] = channelListName.get(i);
                }
                for (int i = 0; i < N; i++) {
                    if (urlIndex == channelListUrl.get(i)) {
                        choiceIndex = i;
                        break;
                    }
                }
                builder.setSingleChoiceItems(choice, choiceIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (choiceIndex != which) {
                            ((MzituPhotoFragment) getFragmentByTag(getString(R.string.main_fragment_mzitu))).clearData();
                            ((MzituTagFragment) getFragmentByTag(getString(R.string.main_fragment_classify))).clearData();
                            int tempIndex = channelListUrl.get(which);
                            DataManager.getInstance(mContext).setChannelIndex(tempIndex);
                            DataManager.getInstance(mContext).switchChannel(tempIndex);
                            AppConfigMgr.setChannelIndex(mContext, tempIndex);
                        }
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog = builder.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
