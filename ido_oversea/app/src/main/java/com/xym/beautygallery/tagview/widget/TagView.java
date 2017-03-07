package com.xym.beautygallery.tagview.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.xym.beautygallery.R;


public class TagView extends ToggleButton {

    private boolean mCheckEnable = false;

    public TagView(Context paramContext) {
        super(paramContext);
        init();
    }

    public TagView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    public TagView(Context paramContext, AttributeSet paramAttributeSet,
                   int paramInt) {
        super(paramContext, paramAttributeSet, 0);
        init();
    }

    private void init() {
        setTextOn(null);
        setTextOff(null);
        setText("");
        setBackgroundResource(R.drawable.tag_bg);
    }

    public void setCheckEnable(boolean paramBoolean) {
//		this.mCheckEnable = paramBoolean;
//		if (!this.mCheckEnable) {
//			super.setChecked(false);
//		}
        super.setChecked(paramBoolean);
        if (paramBoolean) {
            super.setTextColor(getResources().getColor(R.color.common_blue));
        } else {
            super.setTextColor(getResources().getColor(R.color.common_blue));
        }
    }

    @Override
    public void setChecked(boolean paramBoolean) {
        if (this.mCheckEnable) {
            super.setChecked(paramBoolean);
            if (paramBoolean) {
                super.setTextColor(getResources().getColor(R.color.common_blue));
            } else {
                super.setTextColor(getResources().getColor(R.color.common_blue));
            }
        }
    }
}
