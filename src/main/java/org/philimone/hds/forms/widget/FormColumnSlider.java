package org.philimone.hds.forms.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.utilities.StringTools;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;
import kotlin.jvm.internal.PropertyReference0Impl;

public class FormColumnSlider extends LinearLayout {

    private ViewPager2 formViewPager;
    private float prevX = -1;
    private SlideDirection currentSlideDirection;
    private int minPages;
    private int maxPages;

    private enum SlideDirection { BACKWARDS, FORWARDS}

    public FormColumnSlider(Context context) {
        super(context);
        init();
    }

    public FormColumnSlider(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.formViewPager = new ViewPager2(this.getContext());
        this.formViewPager.setUserInputEnabled(false);
        this.addView(this.formViewPager, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (prevX != -1) {
            if (event.getX() > prevX) {
                // Left to Right swipe - Move backward

                this.currentSlideDirection = SlideDirection.BACKWARDS;

                //Log.d("DEBUG", MotionEvent.ACTION_MOVE + ":" + event.getAction() + ":" + event.getActionMasked() + ":Left Swipe" + ":" + prevX + ":" + event.getX() + ":" + formViewPager.getCurrentItem());
            } else if (prevX > event.getX()) {
                // Right to left swipe - Move Forward
                this.currentSlideDirection = SlideDirection.FORWARDS;

                //Log.d("DEBUG", MotionEvent.ACTION_MOVE + ":" + event.getAction() + ":" + event.getActionMasked() + ":Right Swipe" + ":" + prevX + ":" + event.getX() + ":" + formViewPager.getCurrentItem());
            }
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            prevX = event.getX();
        } else {
            prevX = -1;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) { //finished moving

            if (this.currentSlideDirection == SlideDirection.BACKWARDS){
                onSlideBackwards();
            }else {
                onSlideForwards();
            }
        }

        return true;
    }

    private void onSlideBackwards() {
        int curr = formViewPager.getCurrentItem();

        int next = curr==minPages ? curr : curr-1;
        formViewPager.setCurrentItem(next, true);
    }

    private void onSlideForwards() {
        int curr = formViewPager.getCurrentItem();


        //its conditionally
        ColumnView columnView = getCurrentColumnViewIfRequiredAndBlank();

        if (columnView != null) {



            Toast toast = Toast.makeText(this.getContext(), R.string.column_required_lbl, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0,0);
            TextView textView = toast.getView().findViewById(android.R.id.message);
            textView.setTextSize(16);
            //toast.getView().setBackgroundColor(Color.DKGRAY);
            toast.show();

            //highlight columnView

            //focus the input

            return;
        }

        int next = curr==maxPages ? curr : curr+1;
        formViewPager.setCurrentItem(next, true);
    }

    ColumnView getCurrentColumnViewIfRequiredAndBlank() {
        int position = formViewPager.getCurrentItem();
        ColumnGroupView view = getAdapter().getItemView(position);

        for (ColumnView cview : view.getColumnViews()){
            ColumnValue columnValue = cview.getColumnValue();

            if (columnValue.isRequired() && StringTools.isBlank(columnValue.getValue())){
                return cview;
            }
        }

        return null;
    }



    public void setAdapter(ColumnGroupViewAdapter adapter) {
        this.formViewPager.setAdapter(adapter);

        minPages = 0;
        maxPages = getAdapter().getItemCount();
    }

    public ColumnGroupViewAdapter getAdapter() {
        if (this.formViewPager.getAdapter() instanceof ColumnGroupViewAdapter) {
            return (ColumnGroupViewAdapter) this.formViewPager.getAdapter();
        }

        return null;
    }
}
