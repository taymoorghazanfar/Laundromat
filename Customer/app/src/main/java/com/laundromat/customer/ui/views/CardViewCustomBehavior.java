package com.laundromat.customer.ui.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;

import java.util.List;

public class CardViewCustomBehavior extends AppBarLayout.ScrollingViewBehavior {
    static final int ANIM_STATE_NONE = 0;
    static final int ANIM_STATE_HIDING = 1;
    static final int ANIM_STATE_SHOWING = 2;
    static final int SHOW_HIDE_ANIM_DURATION = 200;
    int mAnimState = ANIM_STATE_NONE;

    public CardViewCustomBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return super.layoutDependsOn(parent, child, dependency) ||
                dependency instanceof CardView;
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final View child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout, final View child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0) {
            // User scrolled down -> hide the CardView
            List<View> dependencies = coordinatorLayout.getDependencies(child);
            for (View view : dependencies) {
                if (view instanceof CardView) {
                    hide(((CardView) view));

                }
            }
        } else if (dyConsumed < 0) {
            // User scrolled up -> show the CardView
            List<View> dependencies = coordinatorLayout.getDependencies(child);
            for (View view : dependencies) {
                if (view instanceof CardView) {
                    show((CardView) view);
                }
            }
        }
    }

    void show(final View mView) {
        if (isOrWillBeShown(mView)) {
            // We either are or will soon be visible, skip the call
            return;
        }

        mView.animate().cancel();

        if (shouldAnimateVisibilityChange(mView)) {
            mAnimState = ANIM_STATE_SHOWING;

            if (mView.getVisibility() != View.VISIBLE) {
                // If the view isn't visible currently, we'll animate it from a single pixel
                mView.setAlpha(0f);
                mView.setScaleY(0f);
                mView.setScaleX(0f);
            }

            mView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    // .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
//                            mView.internalSetVisibility(View.VISIBLE, false);
                            mView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimState = ANIM_STATE_NONE;

                        }
                    });
        } else {
//            mView.internalSetVisibility(View.VISIBLE, fromUser);
            mView.setVisibility(View.VISIBLE);
            mView.setAlpha(1f);
            mView.setScaleY(1f);
            mView.setScaleX(1f);

        }
    }

    void hide(final View mView) {
        if (isOrWillBeHidden(mView)) {
            // We either are or will soon be hidden, skip the call
            return;
        }

        mView.animate().cancel();

        if (shouldAnimateVisibilityChange(mView)) {
            mAnimState = ANIM_STATE_HIDING;

            mView.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
//                    .setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        private boolean mCancelled;

                        @Override
                        public void onAnimationStart(Animator animation) {
//                            mView.internalSetVisibility(View.VISIBLE, fromUser);
                            mView.setVisibility(View.VISIBLE);
                            mCancelled = false;

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            mCancelled = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimState = ANIM_STATE_NONE;

                            if (!mCancelled) {
//                                mView.internalSetVisibility(fromUser ? View.GONE : View.INVISIBLE,
//                                        fromUser);
                                mView.setVisibility(View.GONE);
//                                if (listener != null) {
//                                    listener.onHidden();
//                                }
                            }
                        }
                    });
        } else {
            // If the view isn't laid out, or we're in the editor, don't run the animation
//            mView.internalSetVisibility(true ? View.GONE : View.INVISIBLE, fromUser);
            mView.setOverScrollMode(View.GONE);
//            if (listener != null) {
//                listener.onHidden();
//            }
        }
    }


    boolean isOrWillBeShown(View mView) {
        if (mView.getVisibility() != View.VISIBLE) {
            // If we not currently visible, return true if we're animating to be shown
            return mAnimState == ANIM_STATE_SHOWING;
        } else {
            // Otherwise if we're visible, return true if we're not animating to be hidden
            return mAnimState != ANIM_STATE_HIDING;
        }
    }

    private boolean shouldAnimateVisibilityChange(View mView) {
        return ViewCompat.isLaidOut(mView) && !mView.isInEditMode();
    }

    boolean isOrWillBeHidden(View mView) {
        if (mView.getVisibility() == View.VISIBLE) {
            // If we currently visible, return true if we're animating to be hidden
            return mAnimState == ANIM_STATE_HIDING;
        } else {
            // Otherwise if we're not visible, return true if we're not animating to be shown
            return mAnimState != ANIM_STATE_SHOWING;
        }
    }


}