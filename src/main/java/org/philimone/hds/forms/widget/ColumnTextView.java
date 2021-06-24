package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.Column;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnTextView extends ColumnView {

    private TextView txtName;
    private TextView txtValue;

    public ColumnTextView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(view, R.layout.column_string_ro_item, attrs, column);

        createView();
    }

    public ColumnTextView(ColumnGroupView view, @NonNull Column column) {
        this(view, null, column);
    }

    private void createView() {
        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.txtValue = findViewById(R.id.txtColumnValue);

        updateValues();
    }

    public void updateValues(){
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());
        txtValue.setText(column.getValue()==null ? "" : column.getValue());
    }

    public void setValue(String value) {
        this.column.setValue(value);
        updateValues();
    }

    @Override
    public String getValue() {
        return this.txtValue.getText().toString();
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

}
