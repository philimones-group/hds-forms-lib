package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
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
    private OnNewPageSelectedEvents pageEvents = OnNewPageSelectedEvents.NO_ACTION;

    public enum SlideDirection { BACKWARDS, FORWARDS}

    private enum OnNewPageSelectedEvents { CHECK_REQUIRED, NO_ACTION }

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

        this.formViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //Log.d("selected", ""+position+", "+pageEvents);

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

    private void onSlideBackwards() {

        int current = formViewPager.getCurrentItem();
        ColumnGroupView currentGroupView = getAdapter().getItemView(current);
        ColumnGroupView prevGroupView = currentGroupView.getParentGroupView();
        int previous = Math.max(current-1, 0);

        while (prevGroupView != null) {

            Log.d("prev-try", ""+previous+", "+prevGroupView.getColumnViews().stream().map(t -> t.getName()).collect(Collectors.joining(","))+", displayable="+prevGroupView.isDisplayable()+", fragmentVisible="+prevGroupView.isFragmentVisible()+", pages="+getAdapter().getItemCount()+", current="+formViewPager.getCurrentItem());

            if (prevGroupView.isFragmentVisible()) {
                Log.d("show", ""+previous+", "+prevGroupView);
                formViewPager.setCurrentItem(previous, true);
                break;
            } else {
                //if is invisible jump the item
                prevGroupView = prevGroupView.getParentGroupView();
            }
        }

    }

    private void onSlideForwards() {
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

            Log.d("next-try", ""+next+", "+nextGroupView+", displayable=("+displayableBefore+"->"+nextGroupView.isDisplayable()+"), fragmentVisible="+nextGroupView.isFragmentVisible()+", pages="+getAdapter().getItemCount()+", current="+formViewPager.getCurrentItem());


            if (nextGroupView.isDisplayable()) {
                getAdapter().showPage(next, nextGroupView);

                Log.d("show", ""+next+", nextg="+nextGroupView+", current="+ (getAdapter().getItemView(next)) +"/"+ formViewPager.getCurrentItem() +", RealNext="+(getAdapter().getItemView(next+1)));
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
                view.showToastMessage(R.string.column_required_lbl);
                //focus the input
                cview.setFocusable(true);
                cview.requestFocus();
                return true;
            }
        }

        return false;
    }

    public boolean hasAnyRequiredEmptyField() {

        if (isCurrentRequiredEmptyField()){
            return true;
        }

        for (int position=0; position < getAdapter().getVisibleFragments().size(); position++) {
            ColumnGroupView columnGroupView = getAdapter().getItemView(position);

            for (ColumnView cview : columnGroupView.getColumnViews()){
                ColumnValue columnValue = cview.getColumnValue();

                if (columnValue.isRequired() && StringTools.isBlank(columnValue.getValue())){
                    pageEvents = OnNewPageSelectedEvents.CHECK_REQUIRED;
                    formViewPager.setCurrentItem(position, false);

                    return true;
                }
            }
        }

        return false;
    }

    public void evaluateAllDisplayConditions() {
        this.getAdapter().getDefaultFragments().forEach( columnGroupView -> {
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

        });
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
