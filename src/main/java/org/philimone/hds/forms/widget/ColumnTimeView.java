package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
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

        this.timePicker = TimeSelector.createDateTimeWidget(this.getContext(), this);

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
    public void onDateSelected(Date selectedDate, String selectedDateText) {
        this.txtSelectedTime.setText(selectedDateText);
        this.dateValue = selectedDate;
        this.columnModel.setValue(selectedDateText);

        afterUserInput();
    }

    @Override
    public void refreshModelToUI() {

        String formattedDate = "";

        if (this.dateValue != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.dateValue);
            int hh = calendar.get(Calendar.HOUR_OF_DAY);
            int mm = calendar.get(Calendar.MINUTE);

            formattedDate = String.format("%02d", hh) + ":" + String.format("%02d", mm);

        } else if (!StringUtil.isBlank(columnModel.getValue())) {

            if (columnModel.getValue().matches("\\d{2}:\\d{2}")) {
                formattedDate = columnModel.getValue();
            } else {
                String[] values = columnModel.getValue().split(":");
                try {
                    int hh = Integer.parseInt(values[0]);
                    int mm = Integer.parseInt(values[1]);
                    formattedDate = String.format("%02d", hh) + ":" + String.format("%02d", mm);
                } catch (Exception e) {}
            }
        }

        if (!StringUtil.isBlank(formattedDate)) {
            txtSelectedTime.setText(formattedDate);
        }
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
