package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.utilities.StringTools;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnDateTimeView extends ColumnView {

    private TextView txtName;
    private DatePicker dtpColumnDateValue;
    private TimePicker dtpColumnTimeValue;

    public ColumnDateTimeView(Context context, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(context, R.layout.column_datetime_item, attrs, column);

        createView();
    }

    public ColumnDateTimeView(Context context, @NonNull Column column) {
        this(context, null, column);
    }

    private void createView() {

        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        View rowView = inflater.inflate(R.layout.column_datetime_item, this);

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.dtpColumnDateValue = findViewById(R.id.dtpColumnDateValue);
        this.dtpColumnTimeValue = findViewById(R.id.dtpColumnTimeValue);

        this.txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        this.txtName.setText(column.getLabel());
        this.dtpColumnTimeValue.setIs24HourView(true);

        updateDatepicker();
    }

    private void updateDatepicker() {
        Date date = StringTools.toDateTime(this.column.getValue());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (date != null) {
            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH);
            int d = calendar.get(Calendar.DAY_OF_MONTH);
            int hh = calendar.get(Calendar.HOUR);
            int mm = calendar.get(Calendar.MONTH);
            int ss = calendar.get(Calendar.SECOND);

            dtpColumnDateValue.updateDate(y, m, d);
            dtpColumnTimeValue.setHour(hh);
            dtpColumnTimeValue.setMinute(mm);
        }
    }

    @Override
    public String getValue() {
        int y = this.dtpColumnDateValue.getYear();
        int m = this.dtpColumnDateValue.getMonth();
        int d = this.dtpColumnDateValue.getDayOfMonth();
        int hh = this.dtpColumnTimeValue.getHour();
        int mm = this.dtpColumnTimeValue.getMinute();
        int ss = 0;


        String date = y + "-" + String.format("%02d", m) + "-" + String.format("%02d", d) + " " + String.format("%02d", hh) + ":" + String.format("%02d", mm) + ":00";

        return date;
    }

    public Date getValueAsDate() {
        return StringTools.toDateTime(getValue());
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }
}
