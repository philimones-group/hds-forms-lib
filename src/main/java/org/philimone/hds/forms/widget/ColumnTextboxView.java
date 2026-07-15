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
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.enums.ColumnType;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnTextboxView extends ColumnView {

    private TextView txtName;
    private EditText editTxtValue;

    public ColumnTextboxView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_string_item, attrs, columnModel, callListener);

        createView();
    }

    public ColumnTextboxView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
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
                onInputTextChanged(s.toString());
            }
        });

        refreshModelToUI();
    }

    private void onInputTextChanged(String text) {
        this.setValue(text);
        afterUserInput();
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    private void setInputType() {
        switch (column.getType()){
            case STRING:
                //if display style is PHONE NUMBER change the input type
                if (Column.DISPLAY_STYLE_PHONE_NUMBER.equals(this.column.getDisplayStyle())){
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
        this.columnModel.setValue(value);
        refreshModelToUI();
    }

    @Override
    public void refreshModelToUI() {
        String newValue = columnModel.getValue() == null ? "" : columnModel.getValue();
        if (!editTxtValue.getText().toString().equals(newValue)) {
            editTxtValue.setText(newValue);
        }
    }

    @Override
    public void refreshInteractionState() {
        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);

        if (columnModel.isReadOnly()) {
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
        return this.columnModel.getValue();
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
