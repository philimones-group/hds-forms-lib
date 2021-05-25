package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.enums.ColumnType;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class ColumnGroupView extends LinearLayout {

    private FormFragment formPanel;
    private Context mContext;
    private ColumnGroup columnGroup;
    private TextView txtColumnGroupName;
    private LinearLayout formColumnGroupLayout;
    private List<ColumnView> columnViews;
    private boolean hidden;

    public ColumnGroupView(FormFragment formPanel, Context context, @Nullable AttributeSet attrs, ColumnGroup columnGroup) {
        super(context, attrs);
        this.formPanel = formPanel;
        this.mContext = context;
        this.columnGroup = columnGroup;
        this.columnViews = new ArrayList<>();

        buildViews();
    }

    public ColumnGroupView(FormFragment formPanel, Context context, ColumnGroup columnGroup) {
        this(formPanel, context, null, columnGroup);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    private void buildViews() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.column_group_view, this);

        this.txtColumnGroupName = findViewById(R.id.txtColumnGroupName);
        this.formColumnGroupLayout = findViewById(R.id.formColumnGroupLayout);

        this.txtColumnGroupName.setText(columnGroup.getLabel() != null ? columnGroup.getLabel(): "");

        for (Column column : this.columnGroup.getColumns() ) {
            ColumnView view = null;

            if (column.getType() == ColumnType.INTEGER || column.getType() == ColumnType.DECIMAL || column.getType() == ColumnType.STRING) {
                view = column.isReadOnly() ? new ColumnTextView(this.mContext, column) : new ColumnTextboxView(this.mContext, column);
            }
            if (column.getType() == ColumnType.DATE) {
                view = new ColumnDateView(this.mContext, column);
            }
            if (column.getType() == ColumnType.DATETIME) {
                view = new ColumnDateTimeView(this.mContext, column);
            }
            if (column.getType() == ColumnType.SELECT){
                view = new ColumnSelectView(this.mContext, column);
            }
            if (column.getType() == ColumnType.MULTI_SELECT) {
                view = new ColumnMultiSelectView(this.mContext, column);
            }
            if (column.getType() == ColumnType.GPS) {
                ColumnGpsView gpsView = new ColumnGpsView(this.mContext, column);
                view = gpsView;

                if (this.formPanel != null) {
                    this.formPanel.setPermissionListener(gpsView);
                }
            }

            if (column.getType() == ColumnType.DEVICE_ID) {
                column.setValue(formPanel.getDeviceId());

                view = new ColumnTextView(this.mContext, column);
                this.setHidden(true);
            }

            if (column.getType() == ColumnType.COLLECTED_BY) {
                column.setValue(formPanel.getUsername());

                view = new ColumnTextView(this.mContext, column);
                this.setHidden(true);
            }

            if (column.getType() == ColumnType.START_TIMESTAMP || column.getType() == ColumnType.END_TIMESTAMP) {
                view = new ColumnTextView(this.mContext, column);
                this.setHidden(true);
            }

            if (view != null) {
                formColumnGroupLayout.addView(view);
                columnViews.add(view);
            }
        }

    }

    public List<ColumnView> getColumnViews(){
        return this.columnViews;
    }

}
