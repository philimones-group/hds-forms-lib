package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.utilities.StringTools;

import java.time.LocalDate;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnDateView extends ColumnView {

    private TextView txtName;
    private DatePicker dtpColumnValue;

    public ColumnDateView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(view, R.layout.column_date_item, attrs, column);

        createView();
    }

    public ColumnDateView(ColumnGroupView view, @NonNull Column column) {
        this(view, null, column);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.dtpColumnValue = findViewById(R.id.dtpColumnValue);




    }

    private void updateDatepicker() {
        LocalDate date = StringTools.toLocalDate(this.column.getValue());

        if (date != null) {
            int y = date.getYear();
            int m = date.getMonthValue();
            int d = date.getDayOfMonth();

            dtpColumnValue.updateDate(y, m, d);
        }
    }

    @Override
    public void updateValues() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());

        updateDatepicker();
    }

    @Override
    public void setValue(String value) {
        //value is yyyy-MM-dd
        this.column.setValue(value);
        updateValues();
    }

    @Override
    public String getValue() {
        int y = this.dtpColumnValue.getYear();
        int m = this.dtpColumnValue.getMonth();
        int d = this.dtpColumnValue.getDayOfMonth();

        String date = y + "-" + String.format("%02d", m) + "-" + String.format("%02d", d);

        return date;
    }

    public Date getValueAsDate() {
        return StringTools.toDate(getValue());
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

}
