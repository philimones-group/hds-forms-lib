package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;
import org.philimone.hds.forms.widget.dialog.TimeSelector;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnTimeView extends ColumnView implements TimeSelector.OnSelectedListener {

    private TextView txtName;
    private Button btnSelectTime;
    private TextView txtSelectedTime;
    private TimeSelector timePicker;
    private Date dateValue;

    public ColumnTimeView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_time_item, attrs, column, callListener);
        createView();
    }

    public ColumnTimeView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btnSelectTime = findViewById(R.id.btnSelectTime);
        this.txtSelectedTime = findViewById(R.id.txtSelectedTime);

        this.timePicker = TimeSelector.createDateTimeWidget(this.getContext(), this);

        btnSelectTime.setOnClickListener(v -> {
            onButtonSelectDateClicked();
        });

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        updateLabelTexts();
        btnSelectTime.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);
    }

    @Override
    public void updateLabelTexts() {
        setTextHtml(txtName, column.getLabel());
    }

    private void onButtonSelectDateClicked() {
        if (dateValue != null) {
            this.timePicker.setDefaultTime(dateValue);
        }

        this.timePicker.show();
    }

    @Override
    public void onDateSelected(Date selectedDate, String selectedDateText) {
        this.txtSelectedTime.setText(selectedDateText);
        this.dateValue = selectedDate;
        this.column.setValue(selectedDateText); //must save gregorian format

        afterUserInput();
    }

    @Override
    public void updateValues() {

        String formattedDate = "";

        //Get the date that will be displayed on txtSelectedDate - this date must be formatted into the correct calendar
        if (this.dateValue != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.dateValue);
            int hh = calendar.get(Calendar.HOUR_OF_DAY);
            int mm = calendar.get(Calendar.MINUTE);

            formattedDate = String.format("%02d", hh) + ":" + String.format("%02d", mm);

        } else if (!StringUtil.isBlank(this.column.getValue())) {

            //check if this value is matching time format
            if (!this.column.getValue().matches("\\d{2}:\\d{2}")) {
                //get hours and minutes
                String[] values = this.column.getValue().split(":");
                int hh = Integer.parseInt(values[0]);
                int mm = Integer.parseInt(values[1]);

                Calendar cal = Calendar.getInstance();
                cal.set(1900, 0, 1, hh, mm, 0);

                formattedDate = this.column.getValue();
            }
        }

        txtSelectedTime.setText(formattedDate);
        btnSelectTime.setEnabled(!this.column.isReadOnly());
    }

    @Override
    public void refreshState() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        btnSelectTime.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);
        btnSelectTime.setEnabled(!this.column.isReadOnly());
    }

    @Override
    public void setValue(String value) {
        this.column.setValue(value);
        this.dateValue = DateUtil.toDateYMDHMS(value);
        updateValues();
    }

    @Override
    public String getValue() {
        if (dateValue == null){
            return null;
        } else {
            return DateUtil.formatGregorianYMDHMS(dateValue); //return gregorian if date is set
        }

        //return txtSelectedDate.getText().toString();
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
