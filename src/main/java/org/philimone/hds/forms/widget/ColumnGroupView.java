package org.philimone.hds.forms.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.ColumnRepeatGroup;
import org.philimone.hds.forms.model.enums.ColumnType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class ColumnGroupView extends LinearLayout {

    private static long ITEM_ID_COUNT;

    private String uuid;
    private FormFragment formPanel;
    private Context mContext;
    private ColumnRepeatGroup columnRepeatGroup;
    private ColumnGroup columnGroup;
    private Integer repeatGroupIndex;
    private Integer repeatGroupSize;
    private boolean repeatGroupResizable;
    private TextView txtColumnGroupName;
    private TextView txtRepeatGroupRequired;
    private TextView txtRepeatGroupName;
    private TextView txtRepeatGroupIndex;
    private LinearLayout formRepeatGroupLayout;
    private LinearLayout formColumnGroupLayout;
    private RelativeLayout formToastLayout;
    private TextView formToastMessage;
    private List<ColumnView> columnViews;
    private boolean hidden;
    private boolean displayable = true;
    private boolean fragmentVisible = true;
    private ColumnGroupView parentGroupView;
    private ColumnGroupView nextGroupView;
    private ExternalMethodCallListener methodCallListener;

    public ColumnGroupView(FormFragment formPanel, Context context, @Nullable AttributeSet attrs, ColumnGroup columnGroup, ExternalMethodCallListener callListener) {
        super(context, attrs);

        uuid = (ITEM_ID_COUNT++)+""; //UUID.randomUUID().toString();
        this.formPanel = formPanel;
        this.mContext = context;
        this.columnGroup = columnGroup;
        this.columnViews = new ArrayList<>();

        this.methodCallListener = callListener;

        buildViews();
    }

    public ColumnGroupView(FormFragment formPanel, Context context, ColumnRepeatGroup columnRepeatGroup, ColumnGroup columnGroup, Integer repeatGroupIndex, Integer repeatGroupSize, ExternalMethodCallListener callListener) {
        super(context, null);

        uuid = (ITEM_ID_COUNT++)+""; //UUID.randomUUID().toString();
        this.formPanel = formPanel;
        this.mContext = context;
        this.columnRepeatGroup = columnRepeatGroup;
        this.columnGroup = columnGroup;
        this.repeatGroupIndex = repeatGroupIndex;
        this.repeatGroupSize = repeatGroupSize;
        this.methodCallListener = callListener;

        this.columnViews = new ArrayList<>();

        buildViews();
    }

    public ColumnGroupView(FormFragment formPanel, Context context, ColumnRepeatGroup columnRepeatGroup, ColumnGroup columnGroup, Integer repeatGroupIndex, boolean repeatGroupResizable, ExternalMethodCallListener callListener) {
        super(context, null);

        uuid = (ITEM_ID_COUNT++)+""; //UUID.randomUUID().toString();
        this.formPanel = formPanel;
        this.mContext = context;
        this.columnRepeatGroup = columnRepeatGroup;
        this.columnGroup = columnGroup;
        this.repeatGroupIndex = repeatGroupIndex;
        this.repeatGroupSize = null;
        this.repeatGroupResizable = repeatGroupResizable;
        this.methodCallListener = callListener;

        this.columnViews = new ArrayList<>();

        buildViews();
    }

    public ColumnGroupView(FormFragment formPanel, Context context, ColumnGroup columnGroup, ExternalMethodCallListener callListener) {
        this(formPanel, context, null, columnGroup, callListener);
    }

    public FormFragment getFormPanel() {
        return formPanel;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        this.fragmentVisible = !hidden;
    }

    public boolean isRepeatGroupResizable() {
        return repeatGroupResizable;
    }

    public boolean belongsToRepeatGroup(){
        return columnRepeatGroup != null && repeatGroupIndex != null;
    }

    public ColumnRepeatGroup getColumnRepeatGroup() {
        return columnRepeatGroup;
    }

    public Integer getRepeatGroupIndex() {
        return repeatGroupIndex;
    }

    private void buildViews() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.column_group_view, this);

        this.txtColumnGroupName = findViewById(R.id.txtColumnGroupName);
        this.txtRepeatGroupRequired = findViewById(R.id.txtRepeatGroupRequired);
        this.txtRepeatGroupName = findViewById(R.id.txtRepeatGroupName);
        this.txtRepeatGroupIndex = findViewById(R.id.txtRepeatGroupIndex);
        this.formRepeatGroupLayout = findViewById(R.id.formRepeatGroupLayout);
        this.formColumnGroupLayout = findViewById(R.id.formColumnGroupLayout);
        this.formToastLayout = findViewById(R.id.formToastLayout);
        this.formToastMessage = findViewById(R.id.formToastMessage);

        this.txtColumnGroupName.setText(columnGroup.getLabel() != null ? columnGroup.getLabel(): "");

        this.formToastMessage.setText("");

        if (this.belongsToRepeatGroup()){
            this.formRepeatGroupLayout.setVisibility(VISIBLE);
            this.txtRepeatGroupName.setText(this.columnRepeatGroup.getLabel());
            this.txtRepeatGroupIndex.setText(this.mContext.getString(R.string.repeat_group_instance_index_lbl, (repeatGroupIndex+1)+"", repeatGroupSize.toString()));
            this.txtColumnGroupName.setText("");
        } else {
            this.formRepeatGroupLayout.setVisibility(GONE);
        }

        this.txtColumnGroupName.setVisibility(txtColumnGroupName.getText().length()==0 ? GONE : VISIBLE);

        boolean allHidden = true;

        for (Column column : this.columnGroup.getColumns() ) {
            ColumnView view = null;

            if (column.getType() == ColumnType.INTEGER || column.getType() == ColumnType.DECIMAL || column.getType() == ColumnType.STRING) {
                view = column.isReadOnly() ? new ColumnTextView(this, column, methodCallListener) : new ColumnTextboxView(this, column, methodCallListener);
            }
            if (column.getType() == ColumnType.DATE) {
                view = new ColumnDateView(this, column, methodCallListener);
            }
            if (column.getType() == ColumnType.DATETIME) {
                view = new ColumnDateTimeView(this, column, methodCallListener);
            }
            if (column.getType() == ColumnType.SELECT){
                view = new ColumnSelectView(this, column, methodCallListener);
            }
            if (column.getType() == ColumnType.MULTI_SELECT) {
                view = new ColumnMultiSelectView(this, column, methodCallListener);
            }
            if (column.getType() == ColumnType.GPS) {
                ColumnGpsView gpsView = new ColumnGpsView(this, column, methodCallListener);
                view = gpsView;
            }

            if (column.getType() == ColumnType.COLLECTED_BY) {
                column.setValue(formPanel.getUsername());

                view = new ColumnTextView(this, column, methodCallListener);
            }

            if (column.getType() == ColumnType.INSTANCE_UUID) {
                view = new ColumnTextView(this, column, methodCallListener);
            }

            if (column.getType() == ColumnType.DEVICE_ID) {
                view = new ColumnTextView(this, column, methodCallListener);
            }

            if (column.getType() == ColumnType.START_TIMESTAMP || column.getType() == ColumnType.END_TIMESTAMP) {
                view = new ColumnTextView(this, column, methodCallListener);
            }

            if (column.getType() == ColumnType.EXECUTION_STATUS) {
                view = new ColumnTextView(this, column, methodCallListener);
            }

            if (column.getType() == ColumnType.TIMESTAMP) {
                view = new ColumnTextView(this, column, methodCallListener);
            }

            if (view != null) {
                formColumnGroupLayout.addView(view);
                columnViews.add(view);

                if (column.isHidden()){
                    //view.setDisplayable(false);
                }

                view.setVisibility(column.isHidden() ? GONE : VISIBLE);
            }



            allHidden = allHidden && column.isHidden();
        }

        if (allHidden) {
            //Log.d("one is hidden", toString()+", CSIZE="+columnViews.size());
            this.setHidden(true);

           //if (columnViews.size()==1) {
           //    this.setHidden(true); //set columngroup view hidden
           //}

        }

    }

    public void showToastMessage(@StringRes int messageResId){
        this.formToastMessage.setText(mContext.getString(messageResId));

        this.formToastLayout.setAlpha(1f);
        this.formToastLayout.setVisibility(VISIBLE);
        this.formToastLayout.animate().alpha(1f).setDuration(200).setListener(null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                formToastLayout.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        formToastLayout.setVisibility(GONE);
                    }
                });
            }
        }, 1500);

    }

    public List<ColumnView> getColumnViews(){
        return this.columnViews;
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public void setDisplayable(boolean displayable) {
        this.displayable = displayable;
    }

    public ColumnGroupView getParentGroupView() {
        return parentGroupView;
    }

    public void setParentGroupView(ColumnGroupView parentGroupView) {
        this.parentGroupView = parentGroupView;
    }

    public ColumnGroupView getNextGroupView() {
        return nextGroupView;
    }

    public void setNextGroupView(ColumnGroupView nextGroupView) {
        this.nextGroupView = nextGroupView;
    }

    public void updateVisibility() {
        boolean invisible = this.columnViews.stream().filter(t -> t.displayable == true).count()==0;
        setDisplayable(!invisible);
    }

    public boolean evaluateDisplayCondition() {

        for (ColumnView columnView : this.columnViews) {
            columnView.evaluateDisplayCondition();
        }

        updateVisibility();

        return isDisplayable();
    }

    public void evaluateCalculations(){
        for (ColumnView columnView : this.columnViews) {
            columnView.evaluateCalculation();
        }
    }

    public void evaluateReadOnly(){
        for (ColumnView columnView : this.columnViews) {
            columnView.evaluateReadOnly();
        }
    }

    public void evaluateRequired(){
        for (ColumnView columnView : this.columnViews) {
            columnView.evaluateRequired();
        }
    }

    public boolean isFragmentVisible() {
        return fragmentVisible;
    }

    public void setFragmentVisible(boolean fragmentVisible) {
        this.fragmentVisible = fragmentVisible;
    }

    public boolean equalsTo(ColumnGroupView groupView){
        return this.uuid.equals(groupView.uuid);
    }

    @Override
    public String toString() {
        return "ColumnGroupView{" + getColumnViews().stream().map(t -> t.getName()).collect(Collectors.joining(",")) + "}";
    }

    public long getItemId() {
        return Long.parseLong(uuid);
    }
}
