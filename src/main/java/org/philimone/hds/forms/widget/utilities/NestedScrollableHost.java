package org.philimone.hds.forms.widget.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager2.widget.ViewPager2;

import org.philimone.hds.forms.listeners.GestureListener;
import org.philimone.hds.forms.widget.FormColumnSlider;

// from https://issuetracker.google.com/issues/123006042#comment21

/**
 * Layout to wrap a scrollable component inside a ViewPager2. Provided as a solution to the problem
 * where pages of ViewPager2 have nested scrollable elements that scroll in the same direction as
 * ViewPager2. The scrollable element needs to be the immediate and only child of this host layout.
 *
 * This solution has limitations when using multiple levels of nested scrollable elements
 * (e.g. a horizontal RecyclerView in a vertical RecyclerView in a horizontal ViewPager2).
 */

public class NestedScrollableHost extends NestedScrollView {

    private Context mContext;
    private GestureDetector gestureDetector;

    public NestedScrollableHost(@NonNull Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        this.gestureDetector = new GestureDetector(mContext, new GestureListener(){
            @Override
            public void onSwipeLeft() {
                Log.d("slidding", "left");
                parentSlider().onSlideForwards();
            }

            @Override
            public void onSwipeRight() {
                Log.d("slidding", "right");
                parentSlider().onSlideBackwards();
            }
        });

    }

    private FormColumnSlider parentSlider() {
        View v = (View) this.getParent();

        while( v != null && !(v instanceof FormColumnSlider) ) {
            v = (View) v.getParent();
        }

        return (FormColumnSlider) v;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean handled = onTouchEvent(event);
        //Log.d("intercept", ""+event.getRawX()+", action="+event.getAction()+", "+event.getActionMasked() + ", handled="+handled);
        //if (event.getAction() == MotionEvent.ACTION_UP) return handled;
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);
    }
}
