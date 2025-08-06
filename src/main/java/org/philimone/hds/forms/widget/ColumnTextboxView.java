package org.philimone.hds.forms.widget;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.enums.ColumnType;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnTextboxView extends ColumnView {

    private TextView txtName;
    private EditText editTxtValue;

    public ColumnTextboxView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_string_item, attrs, column, callListener);

        createView();
    }

    public ColumnTextboxView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.editTxtValue = findViewById(R.id.editTxtColumnValue);

        setInputType();

        editTxtValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                afterUserInput();
            }
        });

        updateValues();
    }

    private void setInputType() {
        switch (column.getType()){
            case STRING:
                //if display style is PHONE NUMBER change the input type
                if (this.column.getDisplayStyle().equals(Column.DISPLAY_STYLE_PHONE_NUMBER)){
                    editTxtValue.setInputType(InputType.TYPE_CLASS_PHONE);
                } else {
                    editTxtValue.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                break;
            case INTEGER: editTxtValue.setInputType(InputType.TYPE_CLASS_NUMBER); break;
            case DECIMAL:  editTxtValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);break;
        }
    }

    @Override
    public void setValue(String value) {
        this.column.setValue(value);
        updateValues();
    }

    @Override
    public void updateValues() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());
        editTxtValue.setText(column.getValue()==null ? "" : column.getValue());
    }

    @Override
    public void refreshState() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);

        if (column.isReadOnly()) {
            editTxtValue.setInputType(InputType.TYPE_NULL);
            editTxtValue.setTextIsSelectable(true);
            editTxtValue.setFocusable(false);
        } else {
            setInputType();
            editTxtValue.setTextIsSelectable(true);
            editTxtValue.setFocusable(true);
        }
    }

    @Override
    public String getValue() {
        return this.editTxtValue.getText().toString();
    }

    public Integer getValueAsInt(){
        if (column.getType() == ColumnType.INTEGER) {
            try {
                return Integer.parseInt(getValue());
            } catch (Exception ex) {
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
