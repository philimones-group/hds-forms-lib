package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.utilities.StringTools;
import org.philimone.hds.forms.widget.dialog.DateTimeSelector;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnDateView extends ColumnView implements DateTimeSelector.OnSelectedListener {

    private TextView txtName;
    private Button btnSelectDate;
    private TextView txtSelectedDate;
    private DateTimeSelector datePicker;
    private Date dateValue;


    public ColumnDateView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_date_item, attrs, column, callListener);

        createView();
    }

    public ColumnDateView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btnSelectDate = findViewById(R.id.btnSelectDate);
        this.txtSelectedDate = findViewById(R.id.txtSelectedDate);

        this.datePicker = DateTimeSelector.createDateWidget(this.getContext(), this);

        btnSelectDate.setOnClickListener(v -> {
            onButtonSelectDateClicked();
        });

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());
        btnSelectDate.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);
    }

    private void onButtonSelectDateClicked() {

        if (dateValue != null) {
            this.datePicker.setDefaultDate(dateValue);
        }

        this.datePicker.show();
    }

    @Override
    public void onDateSelected(Date selectedDate, String selectedDateText) {
        this.txtSelectedDate.setText(selectedDateText);
        this.dateValue = selectedDate;
        this.column.setValue(selectedDateText);

        afterUserInput();
    }

    @Override
    public void updateValues() {
        txtSelectedDate.setText(this.column.getValue());
        btnSelectDate.setEnabled(!this.column.isReadOnly());
    }

    @Override
    public void setValue(String value) {
        this.column.setValue(value);
        this.dateValue = StringTools.toDate(value);
        updateValues();
    }

    @Override
    public String getValue() {
        if (dateValue == null){
            return null;
        }

        return txtSelectedDate.getText().toString();
    }

    public Date getValueAsDate() {
        return dateValue;
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }


}
