package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.utilities.StringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public FormFragment getFormPanel() {
        return formPanel;
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
                view = column.isReadOnly() ? new ColumnTextView(this, column) : new ColumnTextboxView(this, column);
            }
            if (column.getType() == ColumnType.DATE) {
                view = new ColumnDateView(this, column);
            }
            if (column.getType() == ColumnType.DATETIME) {
                view = new ColumnDateTimeView(this, column);
            }
            if (column.getType() == ColumnType.SELECT){
                view = new ColumnSelectView(this, column);
            }
            if (column.getType() == ColumnType.MULTI_SELECT) {
                view = new ColumnMultiSelectView(this, column);
            }
            if (column.getType() == ColumnType.GPS) {
                ColumnGpsView gpsView = new ColumnGpsView(this, column);
                view = gpsView;
            }

            if (column.getType() == ColumnType.COLLECTED_BY) {
                column.setValue(formPanel.getUsername());

                view = new ColumnTextView(this, column);
                //this.setHidden(true);
            }

            if (column.getType() == ColumnType.INSTANCE_UUID) {
                view = new ColumnTextView(this, column);
                this.setHidden(true);
            }

            if (column.getType() == ColumnType.DEVICE_ID) {
                view = new ColumnTextView(this, column);
                this.setHidden(true);
            }

            if (column.getType() == ColumnType.START_TIMESTAMP || column.getType() == ColumnType.END_TIMESTAMP) {
                view = new ColumnTextView(this, column);
                this.setHidden(true);
            }

            if (column.getType() == ColumnType.EXECUTION_STATUS) {
                view = new ColumnTextView(this, column);
                this.setHidden(true);
            }

            if (column.getType() == ColumnType.TIMESTAMP) {
                view = new ColumnTextView(this, column);
                //this.setHidden(true);
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
