/*
 * MIT License
 *
 * Copyright (c) 2022 Ramzan Elmurzaev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elmurzaev.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

class NestedWebView extends WebView implements NestedScrollingChild {

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_NESTED_SCROLL = 1;
    public static final int SCROLL_STATE_SCROLL = 2;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];
    private final NestedScrollingChildHelper mChildHelper;
    private final int mTouchSlop;
    private final OnLongClickListener longClickListener = v -> true;
    private int mScrollState = SCROLL_STATE_IDLE;
    private int mLastTouchX;
    private int mLastTouchY;

    public NestedWebView(Context context) {
        this(context, null);
    }

    public NestedWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public NestedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
    }

    private void changeLongClickable(boolean enable) {
        setOnLongClickListener(enable ? null : longClickListener);
        setLongClickable(enable);
        setHapticFeedbackEnabled(enable);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        final MotionEvent vtev = MotionEvent.obtain(e);
        final int action = e.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsets[0] = mNestedOffsets[1] = 0;
        }
        vtev.offsetLocation(mNestedOffsets[0], mNestedOffsets[1]);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastTouchX = (int) (e.getX() + 0.5f);
                mLastTouchY = (int) (e.getY() + 0.5f);

                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                startNestedScroll(nestedScrollAxis);
                super.onTouchEvent(e);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final int x = (int) (e.getX() + 0.5f);
                final int y = (int) (e.getY() + 0.5f);
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;

                if (mScrollState == SCROLL_STATE_IDLE) {
                    if (Math.abs(dx) < mTouchSlop && Math.abs(dy) < mTouchSlop) {
                        break;
                    }
                }
                final boolean preScrollConsumed = dispatchNestedPreScroll(dx, dy, mScrollConsumed,
                        mScrollOffset);

                if (preScrollConsumed) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    mNestedOffsets[0] += mScrollOffset[0];
                    mNestedOffsets[1] += mScrollOffset[1];
                }

                if (preScrollConsumed) {
                    setScrollState(SCROLL_STATE_NESTED_SCROLL);
                } else {
                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];

                    if (dy < 0 && getScrollY() == 0) {
                        final boolean scrollConsumed = dispatchNestedScroll(0, 0, dx, dy, mScrollOffset);
                        if (scrollConsumed) {
                            mLastTouchX -= mScrollOffset[0];
                            mLastTouchY -= mScrollOffset[1];
                            vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                            mNestedOffsets[0] += mScrollOffset[0];
                            mNestedOffsets[1] += mScrollOffset[1];
                            setScrollState(SCROLL_STATE_NESTED_SCROLL);
                        }
                    } else {
                        if (dy != 0) {
                            setScrollState(SCROLL_STATE_SCROLL);
                        }
                        super.onTouchEvent(e);
                    }
                }
                if (mScrollState != SCROLL_STATE_IDLE) {
                    if (isLongClickable()) {
                        changeLongClickable(false);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                if (mScrollState == SCROLL_STATE_NESTED_SCROLL) {
                    e.setAction(MotionEvent.ACTION_CANCEL);
                }
                super.onTouchEvent(e);
                resetTouch();
                changeLongClickable(true);
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                super.onTouchEvent(e);
                resetTouch();
                changeLongClickable(true);
            }
            break;
        }
        vtev.recycle();
        return true;
    }


    private void resetTouch() {
        stopNestedScroll();
        setScrollState(SCROLL_STATE_IDLE);
    }

    void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        mScrollState = state;
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper
                .dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}