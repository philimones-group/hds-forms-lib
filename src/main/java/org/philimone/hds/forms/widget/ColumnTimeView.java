package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.ColumnModel;

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

    public ColumnTimeView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_time_item, attrs, columnModel, callListener);
        createView();
    }

    public ColumnTimeView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btnSelectTime = findViewById(R.id.btnSelectTime);
        this.txtSelectedTime = findViewById(R.id.txtSelectedTime);

        this.timePicker = TimeSelector.createTimeWidget(this.getContext(), this);

        btnSelectTime.setOnClickListener(v -> {
            onButtonSelectDateClicked();
        });

        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        refreshLabels();
        btnSelectTime.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    private void onButtonSelectDateClicked() {
        if (dateValue != null) {
            this.timePicker.setDefaultTime(dateValue);
        }

        this.timePicker.show();
    }

    @Override
    public void onTimeSelected(Date selectedDate, String dateFormatted, String selectedDateText) {
        this.txtSelectedTime.setText(selectedDateText);
        this.dateValue = selectedDate;
        this.columnModel.setValue(dateFormatted); //should save gregorian YMDHMS format

        afterUserInput();
    }

    @Override
    public void refreshModelToUI() {

        String formattedTime = "";

        if (this.dateValue != null) {

            formattedTime = getFormattedTime(this.dateValue);

        } else if (!StringUtil.isBlank(columnModel.getValue())) {

            Date xmlDateValue = DateUtil.toDateYMDHMS(columnModel.getValue());

            if (xmlDateValue != null) { //need to match gregorian YMDHMS
                formattedTime = getFormattedTime(xmlDateValue);
                this.dateValue = xmlDateValue; //use the opportunity to update dataValue
            }
        }

        if (!StringUtil.isBlank(formattedTime)) {
            txtSelectedTime.setText(formattedTime);
        }
    }

    private String getFormattedTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hh = calendar.get(Calendar.HOUR_OF_DAY);
        int mm = calendar.get(Calendar.MINUTE);
        return String.format("%02d", hh) + ":" + String.format("%02d", mm);
    }

    @Override
    public void refreshInteractionState() {
        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        btnSelectTime.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
        btnSelectTime.setEnabled(!columnModel.isReadOnly());
    }

    @Override
    public void setValue(String value) {
        this.columnModel.setValue(value);
        this.dateValue = DateUtil.toDateYMDHMS(value);
        refreshModelToUI();
    }

    @Override
    public String getValue() {
        if (dateValue == null){
            return null;
        } else {
            return DateUtil.formatGregorianYMDHMS(dateValue);
        }
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
