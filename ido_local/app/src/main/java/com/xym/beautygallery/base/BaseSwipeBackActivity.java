package com.xym.beautygallery.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.xym.beautygallery.swipeback.SwipeBackActivityBase;
import com.xym.beautygallery.swipeback.SwipeBackActivityHelper;
import com.xym.beautygallery.swipeback.SwipeBackLayout;
import com.xym.beautygallery.swipeback.UtilsSwipeBack;
import com.xym.beautygallery.utils.Utils;

public abstract class BaseSwipeBackActivity extends BaseActivity implements SwipeBackActivityBase {
    private static final int RightToLeft = 1;
    private static final int LeftToRight = 2;
    private static final int DURATION = 30000;
    private RectF mDisplayRect = new RectF();
    private final Matrix mMatrix = new Matrix();
    private int mDirection = RightToLeft;
    private ValueAnimator mCurrentAnimator;
    private float mScaleFactor;
    protected ImageView mBackground;

    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);

        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        UtilsSwipeBack.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

    protected void moveBackground() {
        if (Utils.hasHoneycomb()) {
            mBackground.post(new Runnable() {
                @Override
                public void run() {
                    mScaleFactor = (float) mBackground.getHeight() / (float) mBackground.getDrawable().getIntrinsicHeight();
                    mMatrix.postScale(mScaleFactor, mScaleFactor);
                    mBackground.setImageMatrix(mMatrix);
                    animate();
                }
            });
        }
    }


    private void animate() {
        updateDisplayRect();
        if (mDirection == RightToLeft) {
            animate(mDisplayRect.left, mDisplayRect.left - (mDisplayRect.right - mBackground.getWidth()));
        } else {
            animate(mDisplayRect.left, 0.0f);
        }
    }


    private void updateDisplayRect() {
        mDisplayRect.set(0, 0, mBackground.getDrawable().getIntrinsicWidth(), mBackground.getDrawable().getIntrinsicHeight());
        mMatrix.mapRect(mDisplayRect);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void animate(float from, float to) {
        mCurrentAnimator = ValueAnimator.ofFloat(from, to);
        mCurrentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                mMatrix.reset();
                mMatrix.postScale(mScaleFactor, mScaleFactor);
                mMatrix.postTranslate(value, 0);

                mBackground.setImageMatrix(mMatrix);

            }
        });
        mCurrentAnimator.setDuration(DURATION);
        mCurrentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mDirection == RightToLeft)
                    mDirection = LeftToRight;
                else
                    mDirection = RightToLeft;

                animate();
            }
        });
        mCurrentAnimator.start();
    }
}
