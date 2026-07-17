package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.adapters.ColumnGroupViewFragment;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnGroupModel;
import org.philimone.hds.forms.model.ColumnModel;
import mz.betainteractive.utilities.StringUtil;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class FormColumnSlider extends LinearLayout {

    private Context mContext;
    private ViewPager2 formViewPager;
    private GestureDetector gestureDetector;
    private OnNewPageSelectedEvents pageEvents = OnNewPageSelectedEvents.NO_ACTION;
    private FormFragment formFragment;
    private int touchSlop;
    private float startX, startY;

    public enum SlideDirection { BACKWARDS, FORWARDS}

    private enum OnNewPageSelectedEvents { CHECK_REQUIRED, CHECK_VALIDATION, NO_ACTION }

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
        this.gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float diffX = Math.abs(event.getX() - startX);
                float diffY = Math.abs(event.getY() - startY);
                if (diffX > touchSlop && diffX > diffY) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);
    }

    private void init() {
        this.touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

        this.formViewPager = new ViewPager2(this.getContext());
        this.formViewPager.setUserInputEnabled(false);

        this.addView(this.formViewPager, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        this.gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    if (diffX > 0) {
                        onSlideBackwards();
                    } else {
                        onSlideForwards();
                    }
                    return true;
                }
                return false;
            }
        });

        this.formViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                executeEvaluations(position);

                if (pageEvents == OnNewPageSelectedEvents.CHECK_REQUIRED) {
                    isCurrentRequiredEmptyField();
                }

                if (pageEvents == OnNewPageSelectedEvents.CHECK_VALIDATION) {
                    isCurrentInvalidField();
                }

                pageEvents = OnNewPageSelectedEvents.NO_ACTION;
            }

        });
    }

    public void onSlideBackwards() {
        Log.d("is sliding back", "true");
        int current = formViewPager.getCurrentItem();
        int previous = Math.max(current - 1, 0);
        formViewPager.setCurrentItem(previous, true);
    }

    public void onSlideForwards() {
        Log.d("is sliding forwards", "true");
        if (isCurrentRequiredEmptyField() || isCurrentInvalidField()) {
            return;
        }

        int current = formViewPager.getCurrentItem();
        int next = current + 1;
        ColumnGroupViewAdapter adapter = getAdapter();

        if (adapter != null && next < adapter.getItemCount()) {
            formViewPager.setCurrentItem(next, true);
        }
    }

    private boolean isCurrentRequiredEmptyField() {
        int position = formViewPager.getCurrentItem();
        ColumnGroupViewAdapter adapter = getAdapter();
        if (adapter == null) return false;
        
        ColumnGroupModel groupModel = adapter.getItemModel(position);
        if (groupModel == null) return false;

        for (ColumnModel cm : groupModel.getColumnModels()) {
            if (cm.isRequired() && StringUtil.isBlank(cm.getValue())) {
                displayRequiredToastMessage(cm);
                return true;
            }
        }

        return false;
    }

    private boolean isCurrentInvalidField() {
        ColumnGroupViewAdapter adapter = getAdapter();
        if (adapter != null) {
            int current = formViewPager.getCurrentItem();
            ColumnGroupModel groupModel = adapter.getItemModel(current);
            if (groupModel != null) {
                for (ColumnModel columnModel : groupModel.getColumnModels()) {
                    if (!columnModel.isValid() && columnModel.isDisplayable()) {
                        displayValidationToastMessage(columnModel);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void displayRequiredToastMessage(ColumnModel columnModel) {
        if (formFragment != null) {
            formFragment.closeResumeView();
        }

        Log.d("trying to toast", columnModel.getName()+"");

        // Search for the currently visible ColumnGroupView inside the ViewPager
        ColumnGroupView currentView = findColumnGroupView(this.formViewPager);
        if (currentView != null) {
            currentView.showToastMessage(R.string.column_required_lbl);
        }
    }

    public void displayValidationToastMessage(ColumnModel columnModel) {
        if (formFragment != null) {
            formFragment.closeResumeView();
        }

        Log.d("trying to toast val", columnModel.getName() + "");

        // Search for the currently visible ColumnGroupView inside the ViewPager
        ColumnGroupView currentView = findColumnGroupView(this.formViewPager);
        if (currentView != null) {
            String message = columnModel.getResolvedValidationMessage();
            if (StringUtil.isBlank(message)) {
                currentView.showToastMessage(R.string.column_validation_err_lbl);
            } else {
                currentView.showToastMessage(message);
            }
        }
    }

    private ColumnGroupView findColumnGroupView(View view) {
        if (view instanceof ColumnGroupView && view.isShown()) {
            return (ColumnGroupView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                ColumnGroupView found = findColumnGroupView(vg.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }

    public boolean hasAnyRequiredEmptyField() {
        if (isCurrentRequiredEmptyField()) {
            return true;
        }

        ColumnGroupViewAdapter adapter = getAdapter();
        if (adapter == null) return false;

        for (int i = 0; i < adapter.getItemCount(); i++) {
            ColumnGroupModel groupModel = adapter.getItemModel(i);
            if (groupModel == null) continue;

            for (ColumnModel cm : groupModel.getColumnModels()) {
                if (cm.isDisplayable() && cm.isRequired() && StringUtil.isBlank(cm.getValue())) {
                    pageEvents = OnNewPageSelectedEvents.CHECK_REQUIRED;
                    formViewPager.setCurrentItem(i, false);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasAnyInvalidField() {
        if (isCurrentInvalidField()) {
            return true;
        }

        ColumnGroupViewAdapter adapter = getAdapter();
        if (adapter == null) return false;

        for (int i = 0; i < adapter.getItemCount(); i++) {
            ColumnGroupModel groupModel = adapter.getItemModel(i);
            if (groupModel == null) continue;

            for (ColumnModel cm : groupModel.getColumnModels()) {
                if (cm.isDisplayable() && !cm.isValid()) {
                    pageEvents = OnNewPageSelectedEvents.CHECK_VALIDATION;
                    formViewPager.setCurrentItem(i, false);
                    return true;
                }
            }
        }

        return false;
    }

    public void executeEvaluations(int position) {
        if (formFragment != null && formFragment.getFormController() != null) {
            ColumnGroupModel groupModel = getAdapter().getItemModel(position);
            if (groupModel != null) {
                formFragment.getFormController().evaluateGroup(groupModel);
            }
        }
    }

    public void gotoPage(ColumnGroupModel model) {
        int position = getAdapter().getItemPosition(model);
        if (position >= 0) {
            pageEvents = OnNewPageSelectedEvents.NO_ACTION;
            formViewPager.setCurrentItem(position, false);
        }
    }

    public void setAdapter(ColumnGroupViewAdapter adapter) {
        this.formViewPager.setAdapter(adapter);
    }

    public ColumnGroupViewAdapter getAdapter() {
        if (this.formViewPager.getAdapter() instanceof ColumnGroupViewAdapter) {
            return (ColumnGroupViewAdapter) this.formViewPager.getAdapter();
        }
        return null;
    }
}
