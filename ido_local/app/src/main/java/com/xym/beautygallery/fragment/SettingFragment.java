package com.xym.beautygallery.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.BaseFragment;
import com.xym.beautygallery.ui.AboutActivity;
import com.xym.beautygallery.ui.FeedbackActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by root on 7/17/16.
 */
public class SettingFragment extends BaseFragment {
    @BindView(R.id.slide_feedback)
    MaterialRippleLayout slideFeedback;
    @BindView(R.id.slide_about)
    MaterialRippleLayout slideAbout;
    @BindView(R.id.slide_mark)
    MaterialRippleLayout slideMark;
    private Context mContext;
    private Unbinder unbinder;


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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        mContext = getActivity();

        unbinder = ButterKnife.bind(this, view);
        slideFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FeedbackActivity.class);
                mContext.startActivity(intent);
            }
        });

        slideAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AboutActivity.class);
                mContext.startActivity(intent);
            }
        });

        slideMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doMark(mContext);
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
