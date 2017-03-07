/**
 *
 */
package com.xym.beautygallery.tagview.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


import com.xym.beautygallery.R;
import com.xym.beautygallery.module.DataManager;
import com.xym.beautygallery.module.TagInfo;
import com.xym.beautygallery.ui.TagAlbumActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kince
 * @category ģ������Ӧ�õײ�tagview
 */
public class TagListView extends FlowLayout implements OnClickListener {

    private OnTagCheckedChangedListener mOnTagCheckedChangedListener;
    private OnTagClickListener mOnTagClickListener;
    private int mTagViewBackgroundResId;
    private int mTagViewTextColorResId;
    private final List<Tag> mTags = new ArrayList<Tag>();
    private Context mContext;
    private Toast mToast;

    /**
     * @param context
     */
    public TagListView(Context context) {
        super(context);
        mContext = context;
        // TODO Auto-generated constructor stub
        init();
    }

    /**
     * @param context
     * @param attributeSet
     */
    public TagListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        // TODO Auto-generated constructor stub
        init();
    }

    /**
     * @param context
     * @param attributeSet
     * @param defStyle
     */
    public TagListView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        mContext = context;
        // TODO Auto-generated constructor stub
        init();
    }

    @Override
    public void onClick(View v) {
        if ((v instanceof TagView)) {
            Tag localTag = (Tag) v.getTag();
            if (localTag.getTagInfo() != null) {
                if (localTag.getTitle() != null) {
                    DataManager.getInstance(mContext).setmCurrentTag(new String(localTag.getTitle()));
                } else {
                    DataManager.getInstance(mContext).setmCurrentTag(new String());
                }
                DataManager.getInstance(mContext).setmPicTagAlbumListData(new TagInfo(localTag.getTagInfo()));
                Intent intent = new Intent(mContext, TagAlbumActivity.class);
                mContext.startActivity(intent);
            }
        }
    }

    private void init() {

    }

    private void inflateTagView(final Tag t, boolean b) {

        TagView localTagView = (TagView) View.inflate(getContext(),
                R.layout.tag, null);
        localTagView.setText(t.getTitle());
        localTagView.setTag(t);

        if (mTagViewBackgroundResId <= 0) {
            mTagViewBackgroundResId = R.drawable.tag_bg;
            localTagView.setBackgroundResource(mTagViewBackgroundResId);
        }

        localTagView.setCheckEnable(t.isChecked());

        if (t.getBackgroundResId() > 0) {
            localTagView.setBackgroundResource(t.getBackgroundResId());
        }
        if ((t.getLeftDrawableResId() > 0) || (t.getRightDrawableResId() > 0)) {
            localTagView.setCompoundDrawablesWithIntrinsicBounds(
                    t.getLeftDrawableResId(), 0, t.getRightDrawableResId(), 0);
        }
        localTagView.setOnClickListener(this);
        addView(localTagView);
    }

    public void addTag(int i, String s) {
        addTag(i, s, false);
    }

    public void addTag(int i, String s, boolean b) {
        addTag(new Tag(i, s), b);
    }

    public void addTag(Tag tag) {
        addTag(tag, false);
    }

    public void addTag(Tag tag, boolean b) {
        mTags.add(tag);
        inflateTagView(tag, b);
    }

    public void addTags(List<Tag> lists) {
        addTags(lists, false);
    }

    public void addTags(List<Tag> lists, boolean b) {
        for (int i = 0; i < lists.size(); i++) {
            addTag((Tag) lists.get(i), b);
        }
    }

    public List<Tag> getTags() {
        return mTags;
    }

    public View getViewByTag(Tag tag) {
        return findViewWithTag(tag);
    }

    public void removeTag(Tag tag) {
        mTags.remove(tag);
        removeView(getViewByTag(tag));
    }

    public void setOnTagCheckedChangedListener(
            OnTagCheckedChangedListener onTagCheckedChangedListener) {
        mOnTagCheckedChangedListener = onTagCheckedChangedListener;
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        mOnTagClickListener = onTagClickListener;
    }

    public void setTagViewBackgroundRes(int res) {
        mTagViewBackgroundResId = res;
    }

    public void setTagViewTextColorRes(int res) {
        mTagViewTextColorResId = res;
    }

    public void setTags(List<? extends Tag> lists) {
        setTags(lists, false);
    }

    public void setTags(List<? extends Tag> lists, boolean b) {
        removeAllViews();
        mTags.clear();
        for (int i = 0; i < lists.size(); i++) {
            addTag((Tag) lists.get(i), b);
        }
    }

    public boolean checkIsMax() {
        int selectedNum = 0;
        for (int i = 0; i < mTags.size(); i++) {
            if (mTags.get(i).isChecked()) {
                selectedNum++;
            }
        }
        if (selectedNum >= 3) {
            return true;
        } else {
            return false;
        }
    }

    public static abstract interface OnTagCheckedChangedListener {
        public abstract void onTagCheckedChanged(TagView tagView, Tag tag);
    }

    public static abstract interface OnTagClickListener {
        public abstract void onTagClick(TagView tagView, Tag tag);
    }
}
