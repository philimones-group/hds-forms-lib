package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.enums.ColumnType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.BigDecimal;

public class ColumnTextView extends ColumnView {

    private TextView txtName;
    private TextView txtValue;

    public ColumnTextView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_string_ro_item, attrs, column, callListener);

        createView();
    }

    public ColumnTextView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
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

    @Override
    public void refreshState() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
    }

    @Override
    public void setValue(String value) {
        this.column.setValue(value);
        updateValues();
    }

    @Override
    public String getValue() {
        return this.txtValue.getText().toString();
    }

    public Integer getValueAsInt(){
        if (column.getType() == ColumnType.INTEGER) {
            try {
                return Integer.parseInt(getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        return null;
    }

    public BigDecimal getValueDecimal(){
        if (column.getType() == ColumnType.DECIMAL) {
            try {
                return new BigDecimal(getValue());
            } catch (Exception ex) {
                return null;
            }
        }

        return null;
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

}
