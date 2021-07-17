package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.enums.ColumnType;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ColumnView extends LinearLayout {

    protected ColumnGroupView columnGroupView;
    protected Column column;
    protected TextView txtColumnRequired;

    public ColumnView(ColumnGroupView view, @LayoutRes int resource, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(view.getContext(), attrs);
        this.columnGroupView = view;
        this.column = column;

        buildViews(resource);
    }

    public ColumnView(ColumnGroupView view, @LayoutRes int resource, @NonNull Column column) {
        this(view, resource,null, column);
    }

    public FormFragment getActivity() {
        return this.columnGroupView.getFormPanel();
    }

    public ColumnType getType(){
        return this.column.getType();
    }

    public abstract String getValue();

    public abstract String getValueAsXml();

    public abstract void setValue(String value);

    public abstract void updateValues();

    public Column getColumn() {
        return this.column;
    }

    private void buildViews(@LayoutRes int resource) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(resource, this);
    }

    public ColumnValue getColumnValue(){
        ColumnValue columnValue = new ColumnValue(columnGroupView, this);
        return columnValue;
    }

}
