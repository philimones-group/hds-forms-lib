package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.enums.ColumnType;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ColumnView extends LinearLayout {

    protected Context mContext;
    protected Column column;
    protected TextView txtColumnRequired;

    public ColumnView(Context context, @LayoutRes int resource, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(context, attrs);
        this.mContext = context;
        this.column = column;

        buildViews(resource);
    }

    public ColumnView(Context context, @LayoutRes int resource, @NonNull Column column) {
        this(context, resource,null, column);
    }

    public ColumnType getType(){
        return this.column.getType();
    }

    public abstract String getValue();

    public abstract String getValueAsXml();

    private void buildViews(@LayoutRes int resource) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(resource, this);
    }

}
