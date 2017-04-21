package android.xwpeng.swipemenulayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

/**
 * 还可优化：
 * 扩展支持右滑
 * 速率条件
 * 关闭与展开条件
 * mIntercepted与mIsQuickly
 * ．．．等
 * Created by xwpeng on 17-3-22.
 */
public class SwipeMenuLayout extends LinearLayout {
    private int mMaxVelocity;
    private int mPointerId;
    private int mMenuWidths;
    private int mLimit;
    private static SwipeMenuLayout mViewCache;
    private static boolean isTouching;
    private View mContentView;
    private PointF mLastP = new PointF();
    private boolean isMoved;
    private boolean isSwipeEnable = true;
    //快速模式
    private boolean mIsQuickly;
    private boolean mIntercepted;
    private VelocityTracker mVelocityTracker;

    public SwipeMenuLayout(Context context) {
        this(context, null);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public boolean isSwipeEnable() {
        return isSwipeEnable;
    }

    public void setSwipeEnable(boolean swipeEnable) {
        isSwipeEnable = swipeEnable;
    }

    public boolean getIsQuickly() {
        return mIsQuickly;
    }

    public void setIsQuickly(boolean isQuickly) {
        mIsQuickly = isQuickly;
    }

    public static SwipeMenuLayout getViewCache() {
        return mViewCache;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwipeMenuLayout, defStyleAttr, 0);
        int count = ta.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = ta.getIndex(i);
            //如果引用成AndroidLib 资源都不是常量，无法使用switch case
            if (attr == R.styleable.SwipeMenuLayout_swipeEnable) {
                isSwipeEnable = ta.getBoolean(attr, true);
            } else if (attr == R.styleable.SwipeMenuLayout_quickly) {
                mIsQuickly = ta.getBoolean(attr, false);
            }
        }
        ta.recycle();
        if (!isClickable()) setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMenuWidths = 0;
        int childCount = getChildCount();
        final boolean parentWrapMode = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
                if (parentWrapMode && lp.height == LayoutParams.MATCH_PARENT) {
                    forceUniformHeight(childView, widthMeasureSpec, lp);
                }
                if (i > 0) {
                    mMenuWidths += childView.getMeasuredWidth();
                } else {
                    mContentView = childView;
                }
            }
        }
        mLimit = mMenuWidths * 4 / 10;//滑动判断的临界值
    }

    /**
     * 为了子View的高，可以matchParent
     * LinearLayout有同名的方法
     * @param childView
     * @param widthMeasureSpec
     * @param lp
     */
    private void forceUniformHeight(View childView, int widthMeasureSpec, MarginLayoutParams lp) {
        //measureChildWithMargins 这个函数会用到宽，所以要保存一下
        int oldWidth = lp.width;
        lp.width = childView.getMeasuredWidth();
        //以父布局高度构建一个Exactly的测量参数
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        measureChildWithMargins(childView, widthMeasureSpec, 0, uniformMeasureSpec, 0);
        lp.width = oldWidth;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mContentView == null) mContentView = getChildAt(0);
        return swipeDispatch(ev) && super.dispatchTouchEvent(ev);
    }

    private boolean swipeDispatch(MotionEvent ev) {
        if (!isSwipeEnable) return false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouching) return false;
                else isTouching = true;
                isMoved = false;
                mIntercepted = false;
                mLastP.set(ev.getRawX(), ev.getRawY());
                if (mViewCache != null) {
                    if (mViewCache != this) {
                        mViewCache.smoothClose();
                        mIntercepted = !mIsQuickly;
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                mPointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIntercepted) break;
                float gapX = mLastP.x - ev.getRawX();
                float gapY = mLastP.y - ev.getRawY();
                boolean xWillMove = Math.abs(gapX) > 0 && (Math.abs(gapX) > Math.abs(gapY));
                boolean xMoved = Math.abs(getScrollX()) > 0;
                if (xMoved || xWillMove) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    scrollBy((int) (gapX), 0);
                    if (getScrollX() < 0) scrollTo(0, 0);
                    if (getScrollX() > mMenuWidths) scrollTo(mMenuWidths, 0);
                    isMoved = true;
                }
                mLastP.set(ev.getRawX(), ev.getRawY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                acquireVelocityTracker(ev);
                final VelocityTracker verTracker = mVelocityTracker;
                if (!mIntercepted) {
                    verTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final float velocityX = verTracker.getXVelocity(mPointerId);
                    if (Math.abs(velocityX) > 1000) {
                        if (velocityX < -1000) smoothExpand();
                        else smoothClose();
                    } else {
                        if (Math.abs(getScrollX()) > mLimit) smoothExpand();
                        else smoothClose();
                    }
                }
                releaseVelocityTracker();
                isTouching = false;
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isSwipeEnable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isMoved) return true;
                    if (getScrollX() > 0) {
                        if (ev.getX() < getWidth() - getScrollX()) {
                            //不是移动那就是点击
                            if (!isMoved) smoothClose();
                            return true;
                        }
                    }
                    break;
            }
            if (mIntercepted) return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private ValueAnimator mExpandAnim, mCloseAnim;

    public void smoothExpand() {
        mViewCache = SwipeMenuLayout.this;
        if (null != mContentView) {
            mContentView.setLongClickable(false);
        }
        cancelAnim();
        mExpandAnim = ValueAnimator.ofInt(getScrollX(), mMenuWidths);
        mExpandAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
            }
        });
        mExpandAnim.setInterpolator(new OvershootInterpolator());
        mExpandAnim.setDuration(300).start();
    }

    private void cancelAnim() {
        if (mCloseAnim != null && mCloseAnim.isRunning()) mCloseAnim.cancel();
        if (mExpandAnim != null && mExpandAnim.isRunning()) mExpandAnim.cancel();

    }

    public void smoothClose() {
        mViewCache = null;
        if (null != mContentView) mContentView.setLongClickable(true);
        cancelAnim();
        mCloseAnim = ValueAnimator.ofInt(getScrollX(), 0);
        mCloseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
            }
        });
        mCloseAnim.setInterpolator(new AccelerateInterpolator());
        mCloseAnim.setDuration(300).start();
    }

    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            mViewCache.smoothClose();
            mViewCache = null;
        }
        super.onDetachedFromWindow();
    }

}