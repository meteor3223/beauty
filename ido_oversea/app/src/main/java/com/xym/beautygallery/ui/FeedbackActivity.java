
package com.xym.beautygallery.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;
import com.xym.beautygallery.R;
import com.xym.beautygallery.base.AppConfigMgr;
import com.xym.beautygallery.base.BaseSwipeBackActivity;
import com.xym.beautygallery.base.stats.StatsWrapper;

import java.util.List;

public class FeedbackActivity extends BaseSwipeBackActivity {

    private ListView mListView;
    private FeedbackAgent mAgent;
    private Conversation mComversation;
    private Context mContext;
    private ReplyAdapter adapter;
    private List<Reply> mReplyList;
    private ImageView sendBtn;
    private EditText inputEdit;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.umeng_fb_activity_conversation);
        mContext = this;
        getActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.wifimgr_say_to_product_manager);
        initView();
        mAgent = new FeedbackAgent(this);
        mComversation = mAgent.getDefaultConversation();
        adapter = new ReplyAdapter();
        mListView.setAdapter(adapter);
        sync();

        clearReplySize();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 清除新消息记录
     */
    private void clearReplySize() {
        AppConfigMgr.setReplySize(FeedbackActivity.this, 0);
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.fb_reply_list);
        sendBtn = (ImageView) findViewById(R.id.fb_send_btn);
        inputEdit = (EditText) findViewById(R.id.fb_send_content);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.fb_reply_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent, R.color.text_password_wrong, R.color.primary);
        sendBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String content = inputEdit.getText().toString();
                inputEdit.getEditableText().clear();
                if (!TextUtils.isEmpty(content)) {
                    mComversation.addUserReply(content);//添加到会话列表
                    mHandler.sendMessage(new Message());
                    sync();
                }

            }
        });

        //下拉刷新
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                sync();
            }
        });
    }

    // 数据同步
    private void sync() {

        mComversation.sync(new SyncListener() {

            @Override
            public void onSendUserReply(List<Reply> replyList) {

            }

            @Override
            public void onReceiveDevReply(List<Reply> replyList) {
                mSwipeRefreshLayout.setRefreshing(false);
                if (replyList == null || replyList.size() < 1) {
                    return;
                }
                mHandler.sendMessage(new Message());
                mListView.setSelection(mComversation.getReplyList().size());
            }
        });
    }

    // adapter
    class ReplyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mComversation.getReplyList().size() + 1;
        }

        @Override
        public Object getItem(int arg0) {
            return mComversation.getReplyList().get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return 0;
            else {
                Reply reply = mComversation.getReplyList().get(position - 1);
                if (Reply.TYPE_DEV_REPLY.equals(reply.type))
                    return 0;
                else
                    return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (position == 0) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.fb_custom_item, null);
                    holder = new ViewHolder();
                    holder.reply_item = (TextView) convertView.findViewById(R.id.fb_reply_item);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.reply_item.setText(R.string.default_reply);
            } else {
                Reply reply = mComversation.getReplyList().get(position - 1);
                if (convertView == null) {
                    if (Reply.TYPE_DEV_REPLY.equals(reply.type)) {
                        convertView = LayoutInflater.from(mContext).inflate(
                                R.layout.fb_custom_item, null);
                    } else {
                        convertView = LayoutInflater.from(mContext).inflate(
                                R.layout.fb_custom_replyitem, null);
                    }
                    holder = new ViewHolder();
                    holder.reply_item = (TextView) convertView.findViewById(R.id.fb_reply_item);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.reply_item.setText(reply.content);
            }

            return convertView;
        }


        class ViewHolder {
            TextView reply_item;
        }
    }

}
