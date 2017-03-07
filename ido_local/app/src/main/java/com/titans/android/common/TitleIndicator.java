package com.titans.android.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.xym.beautygallery.R;

import java.util.List;

public class TitleIndicator extends LinearLayout implements OnClickListener,
        OnFocusChangeListener {

    private static final float FOOTER_LINE_HEIGHT = 4/** 4.0f */
            ;
    private static final int FOOTER_COLOR = 0xff0000ff;
    private static final float FOOTER_TRIANGLE_HEIGHT = 8;
    private final int BSSEEID = 0xffff00;
    LayoutInflater mInflater;
    private Context mCtx;
    private int mSelectedTab;
    private int mTabCount;
    private ViewPager mViewPager;
    private List<TabInfo> mTabs;
    private int mPerItemWidth = 0;
    private int mCurrentScroll = 0;
    private Paint mPaintFooterTriangle;
    private float mFooterTriangleHeight;
    private float mFooterLineHeight;
    private Path mPath = new Path();
    private int mCurrID = 0;
    private ColorStateList mTitleColor;
    private int mItemBg = -1;
    private int mTextSizeNormal;
    private int mTextSizeSelected;
    private int mFootColor;
    private Drawable mDividerBitmap;
    private Resources mRes;

    public TitleIndicator(Context context) {
        super(context);
        initView(context);
    }

    public TitleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setOnFocusChangeListener(this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleIndicator);
        // Retrieve the colors to be used for this view and apply them.
        mFootColor = a.getColor(R.styleable.TitleIndicator_footerColor, FOOTER_COLOR);
        mTitleColor = a.getColorStateList(R.styleable.TitleIndicator_textColor);
        mTextSizeNormal = (int) a.getDimension(R.styleable.TitleIndicator_textSizeNormal, 0);
        mTextSizeSelected = (int) a
                .getDimension(R.styleable.TitleIndicator_textSizeSelected, mTextSizeNormal);
        mFooterLineHeight = a.getDimension(R.styleable.TitleIndicator_footerLineHeight,
                FOOTER_LINE_HEIGHT);
        mFooterTriangleHeight = a.getDimension(R.styleable.TitleIndicator_footerTriangleHeight,
                FOOTER_TRIANGLE_HEIGHT);

        a.recycle();
        initView(context);
    }

    private void initView(Context ctx) {
        mCtx = ctx;
        mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPaintFooterTriangle = new Paint();
        mPaintFooterTriangle.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintFooterTriangle.setColor(mFootColor);
        mRes = mCtx.getResources();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this && hasFocus && mTabCount > 0) {
            getChildAt(mSelectedTab).requestFocus();
            return;
        }

        if (hasFocus) {
            int i = 0;
            int numTabs = mTabCount;
            while (i < numTabs) {
                if (getChildAt(i) == v) {
                    setCurrentTab(i);
                    break;
                }
                i++;
            }
        }
    }

    public void reinit(ViewPager viewPager, List<TabInfo> tabInfo, int defaultTab, int itemBg) {
        mCurrID = 0;
        this.removeAllViews();
        init(viewPager, tabInfo, mSelectedTab, itemBg);
    }

    public void init(ViewPager viewPager, List<TabInfo> tabInfo, int defaultTab, int itemBg) {
        this.mViewPager = viewPager;
        mItemBg = itemBg;
        mTabs = tabInfo;
        mTabCount = tabInfo.size();
        mPaintFooterTriangle.setColor(mRes.getColor(R.color.icons));
        for (int i = 0; i < tabInfo.size(); i++) {
            add(i, tabInfo.get(i));
        }
        setCurrentTab(defaultTab);
    }

    protected void add(int index, TabInfo info) {
        View tabIndicator;
        tabIndicator = mInflater.inflate(R.layout.custom_title_indicator_item, this, false);
        if (mItemBg > -1) {
            tabIndicator.setBackgroundResource(mItemBg);
        }
        final TextView tv = (TextView) tabIndicator.findViewById(R.id.title);
        final ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
        if (mTitleColor != null) {
            tv.setTextColor(mTitleColor);
        }
        if (mTextSizeNormal > 0) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeNormal);
        }
        tv.setText(info.tag);
        if (info.icon > 0) {
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(info.icon);
            //tv.setCompoundDrawablesWithIntrinsicBounds(0, info.icon, 0, 0);
        }
        tabIndicator.setId(BSSEEID + (mCurrID++));
        tabIndicator.setOnClickListener(this);
        LayoutParams lP = (LayoutParams) tabIndicator.getLayoutParams();
        lP.gravity = Gravity.CENTER_VERTICAL;
        lP.bottomMargin = (int) (mFooterLineHeight);
        addView(tabIndicator);
        invalidate();
    }

    public void setCurrentTab(int tab) {
        if (tab < 0 || tab >= mTabCount) {
            return;
        }
        if (mViewPager != null)
            mViewPager.setCurrentItem(tab);
        mSelectedTab = tab;
        int childViewCount = getChildCount();
        for (int i = 0; i < childViewCount; i++) {
            View childView = this.getChildAt(i);
            if (i == mSelectedTab) {
                childView.setSelected(true);
            } else {
                childView.setSelected(false);
            }
        }
        invalidate();
    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    public void setItemBackageground(int res) {
        mItemBg = res;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        float scroll_x = 0;
        if (mTabCount != 0) {
            mPerItemWidth = getWidth() / mTabCount;
            int tabID = mSelectedTab;
            scroll_x = (mCurrentScroll - ((tabID) * (getWidth() + mViewPager.getPageMargin()))) / mTabCount;
        } else {
            mPerItemWidth = getWidth();
            scroll_x = mCurrentScroll;
        }
        Path path = mPath;
        path.rewind();
        float offset = 0;
        float left_x = mSelectedTab * mPerItemWidth + offset + scroll_x + mFooterTriangleHeight;
        float right_x = (mSelectedTab + 1) * mPerItemWidth - offset + scroll_x - mFooterTriangleHeight;
        float top_y = getHeight() - mFooterLineHeight;
        float bottom_y = getHeight();
//        float top_y = 0;
//        float bottom_y = getHeight();

        path.moveTo(left_x, top_y);
        path.lineTo(right_x, top_y);
        path.lineTo(right_x, bottom_y);
        path.lineTo(left_x, bottom_y);
        path.close();
        canvas.drawPath(path, mPaintFooterTriangle);
        drawDivder(canvas);
        super.dispatchDraw(canvas);
    }

    private void drawDivder(Canvas canvas) {
        if (mDividerBitmap != null) {
            for (int i = 1; i < mTabCount; i++) {
                canvas.save();
                int left = (getWidth() / mTabCount) * i;
                mDividerBitmap.setBounds(left, (int) mFooterLineHeight * 2, left + 2, (int) (getHeight() - mFooterLineHeight * 2));
                mDividerBitmap.draw(canvas);
                canvas.restore();
            }
        }
    }

    public void setDividerDrawable(Drawable drawable) {
        mDividerBitmap = drawable;
    }

    public void onScrolled(int h) {
        mCurrentScroll = h;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mCurrentScroll == 0 && mSelectedTab != 0) {
            mCurrentScroll = (getWidth() + mViewPager.getPageMargin()) * mSelectedTab;
        }
    }

    @Override
    public void onClick(View v) {
        int position = v.getId() - BSSEEID;
        setCurrentTab(position);
        TabInfo tabInfo = mTabs.get(position);
    }

    public void setFootLineColor(int color) {
        if (mPaintFooterTriangle != null) {
            mPaintFooterTriangle.setColor(color);
        }
    }

}
