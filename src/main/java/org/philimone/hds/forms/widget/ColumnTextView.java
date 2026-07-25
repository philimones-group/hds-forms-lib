package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.enums.ColumnType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;

import mz.betainteractive.utilities.DateUtil;

public class ColumnTextView extends ColumnView {

    private TextView txtName;
    private TextView txtValue;

    public ColumnTextView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_string_ro_item, attrs, columnModel, callListener);

        createView();
    }

    public ColumnTextView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void createView() {
        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.txtValue = findViewById(R.id.txtColumnValue);

        refreshModelToUI();
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    public void refreshModelToUI(){
        txtValue.setText(columnModel.getValue()==null ? "" : columnModel.getValue());

        //If is a timestamp column display agnostic formatted datetime
        if (column.getType() == ColumnType.TIMESTAMP) {

            DateUtil dateUtil = new DateUtil(getSupportedCalendar());
            if (columnModel.getValue() != null) {
                Date dateValue = DateUtil.toDatePrecise(columnModel.getValue()); //get gregorian
                txtValue.setText(dateValue != null ? dateUtil.formatPrecise(dateValue) : ""); ////transform to display
            }
        }
    }

    @Override
    public void refreshInteractionState() {
        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
    }

    @Override
    public void setValue(String value) {
        this.columnModel.setValue(value);
        refreshModelToUI();
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
