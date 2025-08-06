package org.philimone.hds.forms.widget.dialog;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.widget.utilities.NumberPicker;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.ibm.icu.util.EthiopicCalendar;

public class DateTimeSelector extends AppCompatDialog {

    private Context mContext;
    private TextView txtDialogTitle;
    private TextView txtDialogMessage;
    private Button btDialogOk;
    private Button btDialogCancel;
    private DatePicker dtpColumnDateValue;
    private TimePicker dtpColumnTimeValue;

    private LinearLayout ethiopianLayout;
    private NumberPicker nbpDateDay;
    private NumberPicker nbpDateMonth;
    private NumberPicker nbpDateYear;

    private String dialogTitle;
    private String dialogMessage;
    private boolean dateWithTime;

    private Date defaultDateValue;
    private DateUtil.SupportedCalendar currentSupportedCalendar = DateUtil.SupportedCalendar.GREGORIAN;

    public enum Buttons { OK, CANCEL };

    public enum NumberPickerScroll { NO_CHANGE, INCREASE_WRAP, INCREASE, DECREASE_WRAP, DECREASE }

    private OnSelectedListener listener;

    public DateTimeSelector(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public static DateTimeSelector createDateWidget(Context context, DateUtil.SupportedCalendar supportedCalendar, OnSelectedListener listener){
        DateTimeSelector dialog = new DateTimeSelector(context);
        dialog.listener = listener;
        dialog.dateWithTime = false;
        dialog.currentSupportedCalendar = supportedCalendar;

        return dialog;
    }

    public static DateTimeSelector createDateTimeWidget(Context context, DateUtil.SupportedCalendar supportedCalendar, OnSelectedListener listener){
        DateTimeSelector dialog = new DateTimeSelector(context);
        dialog.listener = listener;
        dialog.dateWithTime = true;
        dialog.currentSupportedCalendar = supportedCalendar;

        return dialog;
    }

    public static DateTimeSelector createDateWidget(Context context, DateUtil.SupportedCalendar supportedCalendar, Date defaultDate, OnSelectedListener listener){
        DateTimeSelector dialog = new DateTimeSelector(context);
        dialog.listener = listener;
        dialog.dateWithTime = false;
        dialog.defaultDateValue = defaultDate;
        dialog.currentSupportedCalendar = supportedCalendar;

        return dialog;
    }

    public static DateTimeSelector createDateTimeWidget(Context context, DateUtil.SupportedCalendar supportedCalendar, Date defaultDate, OnSelectedListener listener){
        DateTimeSelector dialog = new DateTimeSelector(context);
        dialog.listener = listener;
        dialog.dateWithTime = true;
        dialog.defaultDateValue = defaultDate;
        dialog.currentSupportedCalendar = supportedCalendar;

        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.datetime_selector);

        initialize();
    }

    private void initialize(){
        this.txtDialogTitle = (TextView) findViewById(R.id.txtDialogTitle);
        this.txtDialogMessage = (TextView) findViewById(R.id.txtDialogMessage);
        this.btDialogOk = (Button) findViewById(R.id.btDialogOk);
        this.btDialogCancel = (Button) findViewById(R.id.btDialogCancel);
        this.dtpColumnDateValue = findViewById(R.id.dtpColumnDateValue);
        this.dtpColumnTimeValue = findViewById(R.id.dtpColumnTimeValue);

        this.ethiopianLayout = findViewById(R.id.ethiopianLayout);
        this.nbpDateDay = findViewById(R.id.nbpDateDay);
        this.nbpDateMonth = findViewById(R.id.nbpDateMonth);
        this.nbpDateYear = findViewById(R.id.nbpDateYear);

        if (this.btDialogOk != null)
            this.btDialogOk.setOnClickListener(v -> onOkClicked());

        if (this.btDialogCancel != null)
            this.btDialogCancel.setOnClickListener(v -> onCancelCicked());

        if (this.nbpDateDay != null)
            this.nbpDateDay.setOnValueChangedListener((picker, oldDay, newDay) -> {
                onEthiopianDateDayChanged(oldDay, newDay);
            });
        if (this.nbpDateMonth != null)
            this.nbpDateMonth.setOnValueChangedListener((picker, oldMonth, newMonth) -> {
                onEthiopianDateMonthChanged(oldMonth, newMonth);
            });
        if (this.nbpDateYear != null)
            this.nbpDateYear.setOnValueChangedListener((picker, oldYear, newYear) -> {
                onEthiopianDateYearChanged(oldYear, newYear);
            });

        initializeDates();
        doLayout();
    }

    private void onEthiopianDateDayChanged(int oldDay, int newDay) {
        NumberPickerScroll direction = getEthiopianDirection(nbpDateDay, oldDay, newDay);
        int month = nbpDateMonth.getValue();
        int year = nbpDateYear.getValue();

        if (direction == NumberPickerScroll.INCREASE_WRAP) {
            //Log.d("increase-wrap", "old-day="+oldDay+", new-day="+newDay+", curr-month="+month+", curr-year="+year);
            //always jump to day 1
            //jump to next month
            month = getNextNumber (nbpDateMonth, +1); //(month - nbpDateMonth.getMinValue() + (1) + nbpDateMonth.getMaxValue()) % nbpDateMonth.getMaxValue()) + nbpDateMonth.getMinValue();

            if (month == nbpDateMonth.getMaxValue()) {
                nbpDateDay.setMaxValue(isEthiopianLeapYear(year) ? 6 : 5); //we where is the month that have 5 or 6
            } else if (month == nbpDateMonth.getMinValue()) { //if jumped to another year, also update the year
                nbpDateDay.setMaxValue(30);
                int nextYear = getNextNumber(nbpDateYear, +1);
                nbpDateYear.setValue(nextYear);
                year = nextYear;
            } else {
                nbpDateDay.setMaxValue(30);
            }

            nbpDateMonth.setValue(month);
        } else if (direction == NumberPickerScroll.DECREASE_WRAP) {
            //Log.d("decrease-wrap", "old-day="+oldDay+", new-day="+newDay+", curr-month="+month+", curr-year="+year);
            month = getNextNumber(nbpDateMonth, -1); //((month - nbpDateMonth.getMinValue() + (-1) + nbpDateMonth.getMaxValue()) % nbpDateMonth.getMaxValue()) + nbpDateMonth.getMinValue();

            //if jumps to month 13, the day must be either 5 or 6
            if (month == nbpDateMonth.getMaxValue()) {
                //decreased the year
                int prevYear = getNextNumber(nbpDateYear, -1);
                year = prevYear;

                nbpDateDay.setMaxValue(isEthiopianLeapYear(year) ? 6 : 5);
                nbpDateDay.setValue(isEthiopianLeapYear(year) ? 6 : 5);

                nbpDateYear.setValue(year);
            } else {
                nbpDateDay.setMaxValue(30);
                nbpDateDay.setValue(30);
            }

            nbpDateMonth.setValue(month);
        }

        if (month == nbpDateMonth.getMaxValue()) {
            nbpDateDay.setMaxValue(isEthiopianLeapYear(year) ? 6 : 5);
        }

    }

    private void onEthiopianDateMonthChanged(int oldMonth, int newMonth) {
        NumberPickerScroll direction = getEthiopianDirection(nbpDateMonth, oldMonth, newMonth);
        int year = nbpDateYear.getValue();

        if (direction == NumberPickerScroll.INCREASE_WRAP) {
            //jump to next year
            year = getNextNumber(nbpDateYear, +1);
            nbpDateYear.setValue(year);
            //the month is the first of the year - days must be 30
            nbpDateDay.setMaxValue(30);

        } else if (direction == NumberPickerScroll.DECREASE_WRAP) {
            year = getNextNumber(nbpDateYear, -1);
            nbpDateYear.setValue(year);
            //the month is the last of the year, 5 or 6 days
            nbpDateDay.setMaxValue(isEthiopianLeapYear(year) ? 6 : 5);
        } else {
            if (newMonth == nbpDateMonth.getMaxValue()) {
                //last month = 13
                nbpDateDay.setMaxValue(isEthiopianLeapYear(year) ? 6 : 5);
            } else if (oldMonth == nbpDateMonth.getMaxValue()) {
                nbpDateDay.setMaxValue(30);
            }
        }
    }

    private void onEthiopianDateYearChanged(int oldYear, int newYear) {
        NumberPickerScroll direction = getEthiopianDirection(nbpDateYear, oldYear, newYear);
        int month = nbpDateMonth.getValue();

        if (month == nbpDateMonth.getMaxValue()){
            nbpDateDay.setMaxValue(isEthiopianLeapYear(newYear) ? 6 : 5);
        } else if (nbpDateDay.getMaxValue() != 30) {
            nbpDateDay.setMaxValue(30);
        }
    }

    private NumberPickerScroll getEthiopianDirection(NumberPicker numberPicker, int oldVal, int newVal) {
        int min = numberPicker.getMinValue();
        int max = numberPicker.getMaxValue();
        //int range = max - min + 1;

        if ((newVal == max && oldVal == min)) {
            return NumberPickerScroll.DECREASE_WRAP; //direction = "up (wrapped)";
        } else if ((newVal == min && oldVal == max)) {
            return NumberPickerScroll.INCREASE_WRAP; //direction = "down (wrapped)";
        } else if (newVal > oldVal) {
            return NumberPickerScroll.INCREASE;  //direction = "down";
        } else if (newVal < oldVal) {
            return NumberPickerScroll.DECREASE; //direction = "up";
        } else {
            return NumberPickerScroll.NO_CHANGE; //direction = "no change";
        }
    }

    private boolean isEthiopianLeapYear(int year) {
        return year % 4 == 3;
    }

    private int getNextNumber(NumberPicker numberPicker, int increment) {
        int min = numberPicker.getMinValue();
        int max = numberPicker.getMaxValue();
        int range = max - min + 1;
        int current = numberPicker.getValue();

        return ((current - min + (increment) + range) % range) + min;
    }

    private void initializeDates() {
        if (currentSupportedCalendar == DateUtil.SupportedCalendar.ETHIOPIAN) {
            String[] ethiopianMonths = this.mContext.getResources().getStringArray(R.array.ethiopian_months_array);
            nbpDateMonth.setMinValue(0);
            nbpDateMonth.setMaxValue(12);
            nbpDateMonth.setDisplayedValues(ethiopianMonths);

            String[] days = new String[30];
            for (int i=1; i <= days.length; i++) days[i-1] = String.format("%02d", i);
            nbpDateDay.setDisplayedValues(days);
        }

        if (defaultDateValue == null) {
            this.defaultDateValue = new Date();
        }
    }

    public void doLayout() {

        setCancelable(false);
        this.btDialogOk.setVisibility(View.VISIBLE);
        this.btDialogCancel.setVisibility(View.VISIBLE);
        this.dtpColumnTimeValue.setVisibility(dateWithTime ? View.VISIBLE : View.GONE);

        if (currentSupportedCalendar == DateUtil.SupportedCalendar.GREGORIAN) {
            this.ethiopianLayout.setVisibility(View.GONE);
            this.dtpColumnDateValue.setVisibility(View.VISIBLE);
        } else if (currentSupportedCalendar == DateUtil.SupportedCalendar.ETHIOPIAN) {
            this.ethiopianLayout.setVisibility(View.VISIBLE);
            this.dtpColumnDateValue.setVisibility(View.GONE);
        }

        setDefaultDate(defaultDateValue);
    }

    private void onCancelCicked(){
        dismiss();
    }

    private void onDateSelected(){

        SelectedDate selectedDate = null;

        if (currentSupportedCalendar == DateUtil.SupportedCalendar.GREGORIAN) {
            selectedDate = getGregorianDate();
        } else if (currentSupportedCalendar == DateUtil.SupportedCalendar.ETHIOPIAN) {
            selectedDate = getEthiopianDate();
        }

        if (listener != null) {
            listener.onDateSelected(selectedDate.date, selectedDate.dateFormatted);
        }
    }

    private SelectedDate getGregorianDate() {
        int y = this.dtpColumnDateValue.getYear();
        int m = this.dtpColumnDateValue.getMonth()+1;
        int d = this.dtpColumnDateValue.getDayOfMonth();
        int hh = dateWithTime ? this.dtpColumnTimeValue.getCurrentHour() : 0;
        int mm = dateWithTime ? this.dtpColumnTimeValue.getCurrentMinute() : 0;

        Calendar cal = Calendar.getInstance();
        cal.set(y, m, d, hh, mm);
        Date date = cal.getTime();

        String formatted = dateWithTime ? DateUtil.formatGregorianYMDHMS(date) : DateUtil.formatGregorianYMD(date);

        return new SelectedDate(date, formatted, dateWithTime);
    }

    private SelectedDate getEthiopianDate() {
        int y = this.nbpDateYear.getValue();
        int m = this.nbpDateMonth.getValue();
        int d = this.nbpDateDay.getValue();
        int hh = dateWithTime ? this.dtpColumnTimeValue.getCurrentHour() : 0;
        int mm = dateWithTime ? this.dtpColumnTimeValue.getCurrentMinute() : 0;

        EthiopicCalendar calendar = DateUtil.toEthiopianCalendar(y, m, d, hh, mm, 0);
        Date date = calendar.getTime();

        String formatted = dateWithTime ? DateUtil.formatEthiopianYMDHMS(date) : DateUtil.formatEthiopianYMD(date);

        return new SelectedDate(date, formatted, dateWithTime);
    }

    private void onOkClicked() {
        dismiss();
        onDateSelected();
    }

    public void setDefaultDate(Date date) {
        this.defaultDateValue = date;

        if (defaultDateValue != null) {

            if (currentSupportedCalendar == DateUtil.SupportedCalendar.GREGORIAN) {
                setDefaultDateGregorianCalendar(defaultDateValue);
            } else if (currentSupportedCalendar == DateUtil.SupportedCalendar.ETHIOPIAN) {
                setDefaultDateEthiopianCalendar(defaultDateValue);
            }
        }
    }

    private void setDefaultDateGregorianCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        dtpColumnDateValue.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        setDefaultTime(cal);
    }

    private void setDefaultDateEthiopianCalendar(Date date) {
        //DateUtil dateUtil = new DateUtil(DateUtil.SupportedCalendar.ETHIOPIAN);
        EthiopicCalendar cal = DateUtil.toEthiopianCalendar(date);

        nbpDateYear.setValue(cal.get(Calendar.YEAR));
        nbpDateMonth.setValue(cal.get(Calendar.MONTH));
        nbpDateDay.setValue(cal.get(Calendar.DAY_OF_MONTH));

        setDefaultTime(cal);
    }

    private void setDefaultTime(Calendar cal) {
        if (dateWithTime) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dtpColumnTimeValue.setHour(cal.get(Calendar.HOUR_OF_DAY));
                dtpColumnTimeValue.setMinute(cal.get(Calendar.MINUTE));
            } else {
                dtpColumnTimeValue.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
                dtpColumnTimeValue.setCurrentMinute(cal.get(Calendar.MINUTE));
            }
        }
    }

    private void setDefaultTime(EthiopicCalendar cal) {
        if (dateWithTime) {
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
        void onDateSelected(Date selectedDate, String selectedDateText);
    }

    class SelectedDate {
        public Date date;
        public String dateFormatted;

        public SelectedDate(Date date, String dateFormatted, boolean isDateWithTime) {
            this.date = date;
            this.dateFormatted = dateFormatted;
        }
    }
}
