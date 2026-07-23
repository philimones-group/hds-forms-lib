package org.philimone.hds.forms.widget.dialog;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import org.philimone.hds.forms.R;

import mz.betainteractive.utilities.DateUtil;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

public class TimeSelector extends AppCompatDialog {

    private Context mContext;
    private TextView txtDialogTitle;
    private TextView txtDialogMessage;
    private Button btDialogOk;
    private Button btDialogCancel;
    private TimePicker dtpColumnTimeValue;

    private String dialogTitle;
    private String dialogMessage;

    private Date defaultDateValue;
    public enum Buttons { OK, CANCEL };

    private OnSelectedListener listener;

    public TimeSelector(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public static TimeSelector createTimeWidget(Context context, OnSelectedListener listener){
        TimeSelector dialog = new TimeSelector(context);
        dialog.listener = listener;

        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.time_selector);

        initialize();
    }

    private void initialize(){
        this.txtDialogTitle = (TextView) findViewById(R.id.txtDialogTitle);
        this.txtDialogMessage = (TextView) findViewById(R.id.txtDialogMessage);
        this.btDialogOk = (Button) findViewById(R.id.btDialogOk);
        this.btDialogCancel = (Button) findViewById(R.id.btDialogCancel);
        this.dtpColumnTimeValue = findViewById(R.id.dtpColumnTimeValue);

        if (this.btDialogOk != null)
            this.btDialogOk.setOnClickListener(v -> onOkClicked());

        if (this.btDialogCancel != null)
            this.btDialogCancel.setOnClickListener(v -> onCancelCicked());

        initializeDates();
        doLayout();
    }

    private void initializeDates() {
        if (defaultDateValue == null) {
            this.defaultDateValue = new Date();
        }
    }

    public void doLayout() {

        setCancelable(false);
        this.btDialogOk.setVisibility(View.VISIBLE);
        this.btDialogCancel.setVisibility(View.VISIBLE);

        setDefaultTime(defaultDateValue);
    }

    private void onCancelCicked(){
        dismiss();
    }

    private void onTimeSelected(){

        SelectedTime selectedTime = getDate();

        if (listener != null) {
            listener.onTimeSelected(selectedTime.date, selectedTime.dateFormatted, selectedTime.timeFormatted);
        }
    }

    private SelectedTime getDate() {
        int hh = 0;
        int mm = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hh = this.dtpColumnTimeValue.getHour();
            mm = this.dtpColumnTimeValue.getMinute();
        } else {
            hh = this.dtpColumnTimeValue.getCurrentHour();
            mm = this.dtpColumnTimeValue.getCurrentMinute();
        }

        Calendar cal = Calendar.getInstance();
        cal.set(1900, 0, 1, hh, mm, 0);
        cal.set(Calendar.MILLISECOND, 0); //important - our date picker is not collecting second and millisecond
        Date date = cal.getTime();

        String formatted = String.format("%02d", hh) + ":" + String.format("%02d", mm);

        return new SelectedTime(date, DateUtil.formatGregorianYMDHMS(date), formatted);
    }

    private void onOkClicked() {
        dismiss();
        onTimeSelected();
    }

    public void setDefaultTime(Date date) {
        this.defaultDateValue = date;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Log.d("date", ""+date+", cal="+cal);
        Log.d("dtp", ""+dtpColumnTimeValue);

        if (dtpColumnTimeValue != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dtpColumnTimeValue.setHour(cal.get(Calendar.HOUR_OF_DAY));
                dtpColumnTimeValue.setMinute(cal.get(Calendar.MINUTE));
            } else {
                dtpColumnTimeValue.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
                dtpColumnTimeValue.setCurrentMinute(cal.get(Calendar.MINUTE));
            }
        }
    }

    public void setDialogTitle(String title){
        this.dialogTitle = title;
    }

    public void setDialogMessage(String message){
        this.dialogMessage = message;
    }

    public interface OnSelectedListener {
        void onTimeSelected(Date selectedDate, String dateFormatted, String selectedDateText);
    }

    class SelectedTime {
        public Date date;
        public String dateFormatted;
        public String timeFormatted;

        public SelectedTime(Date date, String dateFormatted, String timeFormatted) {
            this.date = date;
            this.dateFormatted = dateFormatted;
            this.timeFormatted = timeFormatted;
        }
    }
}
