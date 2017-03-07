package com.titans.android.common;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

public class CustomFragment extends Fragment {

    protected View mView;
    protected Activity mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    protected View findViewById(int resid) {
        return mView.findViewById(resid);
    }

    public boolean onBackPress() {
        return false;
    }
}
