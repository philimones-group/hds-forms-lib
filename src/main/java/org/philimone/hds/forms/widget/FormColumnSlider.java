package org.philimone.hds.forms.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.adapters.ColumnGroupViewPageAdapter;
import org.philimone.hds.forms.listeners.OnSwipeListener;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.utilities.StringTools;
import org.philimone.hds.forms.widget.dialog.ToastX;

import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

public class FormColumnSlider extends LinearLayout {

    private ViewPager2 formViewPager;
    private float prevX = -1;
    private SlideDirection currentSlideDirection;
    private int minPages;
    private int maxPages;

    public enum SlideDirection { BACKWARDS, FORWARDS}

    public FormColumnSlider(Context context) {
        super(context);
        init();
    }

    public FormColumnSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        this.formViewPager = new ViewPager2(this.getContext());
        this.formViewPager.setUserInputEnabled(false);
        this.addView(this.formViewPager, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        this.setOnTouchListener(new OnSwipeListener(this.getContext()){
            public void onSwipeLeft(){
                onSlideForwards();
            }

            @Override
            public void onSwipeRight() {
                onSlideBackwards();
            }
        });
    }

    private void onSlideBackwards() {

        int current = formViewPager.getCurrentItem();
        ColumnGroupView currentGroupView = getAdapter().getItemView(current);
        ColumnGroupView prevGroupView = currentGroupView.getParentGroupView();
        int previous = getPreviousItem(current);

        while (prevGroupView != null) {

            Log.d("prev-try", ""+previous+", "+prevGroupView.getColumnViews().stream().map(t -> t.getName()).collect(Collectors.joining(","))+", displayable="+prevGroupView.isDisplayable()+", fragmentVisible="+prevGroupView.isFragmentVisible()+", pages="+getAdapter().getItemCount()+", current="+formViewPager.getCurrentItem());

            if (prevGroupView.isFragmentVisible()) {
                Log.d("show", ""+previous+", "+prevGroupView);
                formViewPager.setCurrentItem(previous, true);
                break;
            } else {
                //if is invisible jump the item
                previous = Math.max(previous-1, 0);
                prevGroupView = prevGroupView.getParentGroupView();

            }
        }

    }

    private void onSlideForwards() {
        //handle required columns
        if (hasRequiredAndEmptyField()){
            return;
        }

        //get next groupView and calculate if is displayable or not
        int current = formViewPager.getCurrentItem();
        ColumnGroupView currentGroupView = getAdapter().getItemView(current);
        ColumnGroupView nextGroupView = currentGroupView != null ? currentGroupView.getNextGroupView() : null;
        int next = getNextItem(current);

        while (nextGroupView != null) {

            nextGroupView.evaluateDisplayCondition(); //execute the display condition script

            Log.d("next-try", ""+next+", "+nextGroupView+", displayable="+nextGroupView.isDisplayable()+", fragmentVisible="+nextGroupView.isFragmentVisible()+", pages="+getAdapter().getItemCount()+", current="+formViewPager.getCurrentItem());

            if (nextGroupView.isDisplayable()) {
                boolean wasVisible = nextGroupView.isFragmentVisible();
                getAdapter().showPage(next, nextGroupView);

                if (next <= getAdapter().getItemCount()){
                    Log.d("show", ""+next+", nextg="+nextGroupView+", current="+ (getAdapter().getItemView(next)) +", RealNext="+(getAdapter().getItemView(next+1)));
                    formViewPager.setCurrentItem(next, wasVisible);
                }

                break;
            } else {
                getAdapter().hidePage(next, nextGroupView); //get next component
                //get the next item
                nextGroupView = nextGroupView.getNextGroupView();

            }
        }

        //Log.d("next-test", ""+next+", "+nextGroupView.getColumnViews().stream().map(t -> t.getName()).collect(Collectors.joining(",")));
    }

    int getNextItem(int currentItemIndex) {
        int curr = currentItemIndex;
        int next = curr==maxPages ? curr : curr+1;

        return next;
    }

    int getPreviousItem(int currentItemIndex) {
        int curr = currentItemIndex;
        int prev = curr==minPages ? curr : curr-1;

        return prev;
    }

    private boolean hasRequiredAndEmptyField() {

        int position = formViewPager.getCurrentItem();
        ColumnGroupView view = getAdapter().getItemView(position);

        for (ColumnView cview : view.getColumnViews()){
            ColumnValue columnValue = cview.getColumnValue();

            if (columnValue.isRequired() && StringTools.isBlank(columnValue.getValue())){
                showCustomToast(cview, this.getContext().getString(R.string.column_required_lbl));
                return true;
            }
        }

        return false;
    }

    private void showCustomToast(ColumnView columnView, String message) {

        Log.d("showtoast", ""+message);
        ToastX toast = new ToastX(this.getContext());
        toast.setMessage(message);
        toast.setGravityCenter(columnView.columnGroupView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();

        //highlight columnView

        //focus the input
        columnView.setFocusable(true);
        columnView.requestFocus();

    }

    public void setAdapter(ColumnGroupViewAdapter adapter) {
        this.formViewPager.setAdapter(adapter);

        minPages = 0;
        maxPages = getAdapter().getItemCount()-1;
    }

    public void setAdapter(ColumnGroupViewPageAdapter adapter) {
        this.formViewPager.setOffscreenPageLimit(adapter.getItemCount());
        this.formViewPager.setAdapter(adapter);

        minPages = 0;
        maxPages = getAdapter().getItemCount()-1;
    }

    public ColumnGroupViewAdapter getAdapter() {
        if (this.formViewPager.getAdapter() instanceof ColumnGroupViewAdapter) {
            return (ColumnGroupViewAdapter) this.formViewPager.getAdapter();
        }

        return null;
    }

    public ColumnGroupViewPageAdapter getAdapter2() {
        if (this.formViewPager.getAdapter() instanceof ColumnGroupViewPageAdapter) {
            return (ColumnGroupViewPageAdapter) this.formViewPager.getAdapter();
        }

        return null;
    }
}
