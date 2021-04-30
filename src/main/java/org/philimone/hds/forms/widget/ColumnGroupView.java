package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;

import androidx.annotation.Nullable;

public class ColumnGroupView extends LinearLayout {

    private Context mContext;
    private ColumnGroup columnGroup;
    private TextView txtColumnGroupName;
    private LinearLayout formColumnGroupLayout;

    public ColumnGroupView(Context context, @Nullable AttributeSet attrs, ColumnGroup columnGroup) {
        super(context, attrs);
        this.mContext = context;
        this.columnGroup = columnGroup;

        buildViews();
    }

    public ColumnGroupView(Context context, ColumnGroup columnGroup) {
        this(context, null, columnGroup);
    }


    private void buildViews() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.column_group_view, this);

        this.txtColumnGroupName = findViewById(R.id.txtColumnGroupName);
        this.formColumnGroupLayout = findViewById(R.id.formColumnGroupLayout);

        this.txtColumnGroupName.setText(columnGroup.getLabel() != null ? columnGroup.getLabel(): "");

        for (Column column : this.columnGroup.getColumns() ) {
            ColumnView view = null;

            switch (column.getType()) {
                case INTEGER:
                case DECIMAL:
                case STRING:       view = column.isReadOnly() ? new ColumnTextView(this.mContext, column) : new ColumnTextboxView(this.mContext, column); break;
                case DATE:         view = new ColumnDateView(this.mContext, column); break;
                case DATETIME:     view = new ColumnDateTimeView(this.mContext, column); break;
                case SELECT:       view = new ColumnSelectView(this.mContext, column); break;
                case MULTI_SELECT: view = new ColumnMultiSelectView(this.mContext, column); break;
                case GPS:          view = new ColumnGpsView(this.mContext, column); break;
            }

            if (view != null) {
                formColumnGroupLayout.addView(view);
            }
        }

    }



}
