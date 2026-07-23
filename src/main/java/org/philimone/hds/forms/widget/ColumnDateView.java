package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.ColumnModel;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;
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
    private DateUtil dateUtil;


    public ColumnDateView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_date_item, attrs, columnModel, callListener);
        this.dateUtil = new DateUtil(getSupportedCalendar());
        createView();
    }

    public ColumnDateView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btnSelectDate = findViewById(R.id.btnSelectDate);
        this.txtSelectedDate = findViewById(R.id.txtSelectedDate);

        this.datePicker = DateTimeSelector.createDateWidget(this.getContext(), getSupportedCalendar(), this);

        btnSelectDate.setOnClickListener(v -> {
            onButtonSelectDateClicked();
        });

        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        refreshLabels();
        btnSelectDate.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    private void onButtonSelectDateClicked() {

        if (dateValue != null) {
            this.datePicker.setDefaultDate(dateValue); //its a gregorian date
        }

        this.datePicker.show();
    }

    @Override
    public void onDateSelected(Date selectedDate, String selectedDateText) {
        this.txtSelectedDate.setText(selectedDateText);
        this.dateValue = selectedDate;
        this.columnModel.setValue(DateUtil.formatGregorianYMD(this.dateValue)); //must save gregorian format

        afterUserInput();
    }

    @Override
    public void refreshModelToUI() {

        String formattedDate = "";

        //Get the date that will be displayed on txtSelectedDate - this date must be formatted into the correct calendar
        if (this.dateValue != null) {
            formattedDate = dateUtil.formatYMD(this.dateValue);

        } else if (!StringUtil.isBlank(columnModel.getValue())) {
            Date date = DateUtil.toDateYMD(columnModel.getValue()); //column.getValue is always a gregorian date
            this.dateValue = date;
            formattedDate = dateUtil.formatYMD(date);
        }

        txtSelectedDate.setText(formattedDate);
    }

    @Override
    public void refreshInteractionState() {
        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        btnSelectDate.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
        btnSelectDate.setEnabled(!columnModel.isReadOnly());
    }

    @Override
    public void setValue(String value) {
        this.columnModel.setValue(value); //this is a gregorian date format
        this.dateValue = DateUtil.toDateYMD(value);
        refreshModelToUI();
    }

    @Override
    public String getValue() {
        if (dateValue == null){
            return null;
        } else {
            return DateUtil.formatGregorianYMD(dateValue); //return gregorian if date is set
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
