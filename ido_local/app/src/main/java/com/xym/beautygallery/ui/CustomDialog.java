package com.xym.beautygallery.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xym.beautygallery.R;


public class CustomDialog extends Dialog {

    private TextView mTitle;
    private TextView mMsg;
    private Button mCancelBtn;
    private Button mOkBtn;
    private FrameLayout mContentView;
    private ImageView mIcon;
    private ScrollView mScrollView;
    private Context mContext;


    public Button getmCancelBtn() {
        return mCancelBtn;
    }

    public Button getmOkBtn() {
        return mOkBtn;
    }

    public CustomDialog(Context context) {
        super(context, R.style.ActivityTheme_CustomDialog);
        setContentView(R.layout.custom_dialog);
        mTitle = (TextView) findViewById(R.id.dialog_title);
        mContentView = (FrameLayout) findViewById(R.id.content_view);
        mMsg = (TextView) findViewById(R.id.dialog_msg);
        mCancelBtn = (Button) findViewById(R.id.cancel_btn);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        mIcon = (ImageView) findViewById(R.id.title_icon);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mContext = context;
        this.setCanceledOnTouchOutside(true);
    }

    public Context getmContext() {
        return mContext;
    }

    public void setIcon(int id) {
        findViewById(R.id.title_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.header_divider).setVisibility(View.VISIBLE);
        mIcon.setVisibility(View.VISIBLE);
        mIcon.setImageResource(id);
    }

    public void setTitle(CharSequence title) {
        findViewById(R.id.title_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.header_divider).setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(title);
    }

    public void setMessage(CharSequence msg) {
        mScrollView.setVisibility(View.VISIBLE);
        mMsg.setText(msg);
    }

    public void setMessage(int msg) {
        mScrollView.setVisibility(View.VISIBLE);
        mMsg.setText(msg);
    }

    public void setMessageGravityLeft() {
        mMsg.setGravity(Gravity.LEFT);
    }

    public void setContentView(View view) {
        mContentView.removeAllViews();
        mContentView.addView(view);
        mScrollView.setVisibility(View.VISIBLE);
    }

    public void setProgress(int message) {
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.GONE);
        if (message > 0) {
            ((TextView) findViewById(R.id.progess_message)).setText(message);
        }
    }

    public void setProgress(String message) {
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.progess_message)).setText(message);
    }

    public void setOkBtn(int id, View.OnClickListener listener) {
        setOkBtn(id, listener, true);
    }

    public void setOkBtn(int id, View.OnClickListener listener, boolean isAutoDismiss) {
        findViewById(R.id.btn_pannel).setVisibility(View.VISIBLE);
        findViewById(R.id.button_divider).setVisibility(View.VISIBLE);
        mOkBtn.setVisibility(View.VISIBLE);
        mOkBtn.setOnClickListener(isAutoDismiss ? new ExternalListener(listener) : listener);
        mOkBtn.setText(id);
    }

    public void setOkBtnBg(int bgId) {
        mOkBtn.setBackgroundResource(bgId);
    }

    public void setCancelBtn(int id, View.OnClickListener listener) {
        setCancelBtn(id, listener, true);
    }

    public void setCancelBtn(int id, View.OnClickListener listener, boolean isAutoDismiss) {
        findViewById(R.id.btn_pannel).setVisibility(View.VISIBLE);
        findViewById(R.id.button_divider).setVisibility(View.VISIBLE);
        mCancelBtn.setVisibility(View.VISIBLE);
        mCancelBtn.setOnClickListener(isAutoDismiss ? new ExternalListener(listener) : listener);
        mCancelBtn.setText(id);
    }

    public ListView setListAdapter(ListAdapter adapter, int selected) {
        ListView listView = setListAdapter(adapter);
        if (adapter instanceof SimpleAdapter) {
            SimpleAdapter ada = (SimpleAdapter) adapter;
            if (ada.getViewBinder() == null) {
                SimpleAdapter.ViewBinder binder = new SimpleAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Object obj, String s) {
                        if (view instanceof TextView && obj instanceof String) {
                            ((TextView) view).setText((String) obj);
                            return true;
                        }
                        return false;
                    }
                };
                ada.setViewBinder(binder);
            }
        }
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if (selected >= 0) listView.setItemChecked(selected, true);
        return listView;
    }

    public ListView setListAdapter(ListAdapter adapter) {
        return setListAdapter(adapter, true);
    }

    public ListView setListAdapter(ListAdapter adapter, boolean isScrollViewHidden) {
        if (isScrollViewHidden) {
            mScrollView.setVisibility(View.GONE);
        }
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(adapter);
        return listView;
    }

    public void setTitleVisibility(int visibility) {
        findViewById(R.id.title_layout).setVisibility(visibility);
        findViewById(R.id.header_divider).setVisibility(visibility);
    }

    public void setButtonVisibility(int visibility) {
        findViewById(R.id.btn_pannel).setVisibility(visibility);
        findViewById(R.id.button_divider).setVisibility(visibility);
    }

    private class ExternalListener implements View.OnClickListener {
        private View.OnClickListener mListener;

        public ExternalListener(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            CustomDialog.this.dismiss();
            if (mListener != null) {
                mListener.onClick(v);
            }
        }
    }
}
