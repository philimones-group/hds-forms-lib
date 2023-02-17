package org.philimone.hds.forms.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.adapters.ColumnGroupViewPageAdapter;
import org.philimone.hds.forms.listeners.GestureListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.utilities.StringTools;
import org.philimone.hds.forms.widget.dialog.ToastX;

import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

public class FormColumnSlider extends LinearLayout {

    private Context mContext;
    private ViewPager2 formViewPager;
    private GestureDetector gestureDetector;
    private float prevX = -1;
    private SlideDirection currentSlideDirection;
    private int minPages;
    private int maxPages;
    private OnNewPageSelectedEvents pageEvents = OnNewPageSelectedEvents.NO_ACTION;
    private FormFragment formFragment;

    public enum SlideDirection { BACKWARDS, FORWARDS}

    private enum OnNewPageSelectedEvents { CHECK_REQUIRED, NO_ACTION }

    public FormColumnSlider(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public FormColumnSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public FormFragment getFormFragment() {
        return formFragment;
    }

    public void setFormFragment(FormFragment formFragment) {
        this.formFragment = formFragment;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean handled = onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) return handled;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);
    }

    private void init() {

        this.formViewPager = new ViewPager2(this.getContext());
        this.formViewPager.setUserInputEnabled(false);

        this.addView(this.formViewPager, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        this.gestureDetector = new GestureDetector(mContext, new GestureListener(){
            @Override
            public void onSwipeLeft() {
                onSlideForwards();
            }

            @Override
            public void onSwipeRight() {
                onSlideBackwards();
            }
        });

        this.formViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //Log.d("selected", ""+position+", "+pageEvents);

                evaluateCalculation(position);

                switch (pageEvents) {
                    case CHECK_REQUIRED:
                        isCurrentRequiredEmptyField();
                        break;
                    case NO_ACTION: break;
                    default: break;
                }

                pageEvents = OnNewPageSelectedEvents.NO_ACTION;
            }

        });
    }

    public void onSlideBackwards() {

        int current = formViewPager.getCurrentItem();
        ColumnGroupView currentGroupView = getAdapter().getItemView(current);
        ColumnGroupView prevGroupView = currentGroupView.getParentGroupView();
        int previous = Math.max(current-1, 0);

        while (prevGroupView != null) {

            //Log.d("prev-try", ""+previous+", "+prevGroupView.getColumnViews().stream().map(t -> t.getName()).collect(Collectors.joining(","))+", displayable="+prevGroupView.isDisplayable()+", fragmentVisible="+prevGroupView.isFragmentVisible()+", pages="+getAdapter().getItemCount()+", current="+formViewPager.getCurrentItem());

            if (prevGroupView.isFragmentVisible() && !prevGroupView.isHidden()) {
                //Log.d("show", ""+previous+", "+prevGroupView);
                formViewPager.setCurrentItem(previous, true);
                break;
            } else {
                //if is invisible jump the item
                prevGroupView = prevGroupView.getParentGroupView();
            }
        }

    }

    public void onSlideForwards() {
        //handle required columns
        if (isCurrentRequiredEmptyField()){
            return;
        }

        //get next groupView and calculate if is displayable or not
        int current = formViewPager.getCurrentItem();
        ColumnGroupView currentGroupView = getAdapter().getItemView(current);
        ColumnGroupView nextGroupView = currentGroupView != null ? currentGroupView.getNextGroupView() : null;
        int next = current + 1;

        while (nextGroupView != null) {
            boolean displayableBefore = nextGroupView.isDisplayable();
            nextGroupView.evaluateDisplayCondition(); //execute the display condition script

            //Log.d("next-try", ""+next+", "+nextGroupView+", displayable=("+displayableBefore+"->"+nextGroupView.isDisplayable()+"), fragmentVisible="+nextGroupView.isFragmentVisible()+", pages="+getAdapter().getItemCount()+", current="+formViewPager.getCurrentItem());


            if (nextGroupView.isDisplayable() && !nextGroupView.isHidden()) {

                //if there is calculation execute it


                getAdapter().showPage(next, nextGroupView);

                //Log.d("show", ""+next+", nextg="+nextGroupView+", current="+ (getAdapter().getItemView(next)) +"/"+ formViewPager.getCurrentItem() +", RealNext="+(getAdapter().getItemView(next+1)));
                formViewPager.setCurrentItem(next);

                break;
            } else {
                getAdapter().hidePage(nextGroupView); //get next component
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

    private boolean isCurrentRequiredEmptyField() {

        int position = formViewPager.getCurrentItem();
        ColumnGroupView view = getAdapter().getItemView(position);

        for (ColumnView cview : view.getColumnViews()){
            ColumnValue columnValue = cview.getColumnValue();

            if (columnValue.isRequired() && StringTools.isBlank(columnValue.getValue())){
                displayRequiredToastMessage(view, cview);
                return true;
            }
        }

        return false;
    }

    public void displayRequiredToastMessage(ColumnGroupView view, ColumnView cview){
        //check if FormFragment is in resume mode - if it is close it
        if (formFragment != null){
            formFragment.closeResumeView();
        }

        view.showToastMessage(R.string.column_required_lbl);
        //focus the input
        cview.setFocusable(true);
        cview.requestFocus();
    }

    public boolean hasAnyRequiredEmptyField() {

        if (isCurrentRequiredEmptyField()){
            //Log.d("current required", "");
            return true;
        }

        for (int position=0; position < getAdapter().getVisibleFragments().size(); position++) {
            ColumnGroupView columnGroupView = getAdapter().getItemView(position);

            for (ColumnView cview : columnGroupView.getColumnViews()){
                ColumnValue columnValue = cview.getColumnValue();

                if (!cview.isHidden() && columnValue.isRequired() && StringTools.isBlank(columnValue.getValue())){
                    pageEvents = OnNewPageSelectedEvents.CHECK_REQUIRED;
                    formViewPager.setCurrentItem(position, false);

                    return true;
                }
            }
        }

        return false;
    }

    public void evaluateAllDisplayConditions() {
        for (ColumnGroupView columnGroupView : this.getAdapter().getDefaultFragments()) {
            columnGroupView.evaluateDisplayCondition();
            //should add or remove from FormColumnSlider

            if (columnGroupView.isDisplayable()){
                //make it visible, add
                //1,2,3,4,5,6,7,8
                //1,2, ,4, ,6,7,8
                //1,2,  3,  4,5,6
                //   (3)

                int position = this.getAdapter().getCorrectPosition(columnGroupView);
                this.getAdapter().showPage(position, columnGroupView);
            } else {
                this.getAdapter().hidePage(columnGroupView);
            }

        }
    }

    public void evaluateCalculation(int position){
        ColumnGroupView currentGroupView = getAdapter().getItemView(position);
        if (currentGroupView != null){
            currentGroupView.evaluateCalculations();
        }
    }

    public void gotoPage(ColumnView columnView) {
        int position = getAdapter().getItemPosition(columnView.columnGroupView);

        if (position >= 0) {
            pageEvents = OnNewPageSelectedEvents.NO_ACTION;
            formViewPager.setCurrentItem(position, false);
        }
    }

    public void setAdapter(ColumnGroupViewAdapter adapter) {
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
