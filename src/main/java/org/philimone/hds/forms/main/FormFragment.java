package org.philimone.hds.forms.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.parsers.ExcelFormParser;
import org.philimone.hds.forms.utilities.StringTools;
import org.philimone.hds.forms.widget.ColumnGpsView;
import org.philimone.hds.forms.widget.ColumnGroupView;
import org.philimone.hds.forms.widget.ColumnTextView;
import org.philimone.hds.forms.widget.ColumnView;
import org.philimone.hds.forms.widget.FormColumnSlider;
import org.philimone.hds.forms.widget.dialog.DialogFactory;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class FormFragment extends DialogFragment {

    private HForm form;
    private FormColumnSlider formSlider;
    private TextView txtFormTitle;
    private LinearLayout formHeaderLayout;
    private Button btCancel;
    private Button btSave;
    private List<ColumnGroupView> columnGroupViewList;
    private String username;
    private String deviceId;
    private String startTimestamp;
    private String endTimestamp;
    private boolean executeOnUpload;
    private Map<String, String> preloadedColumnValues;

    private ActivityResultLauncher<String> requestPermission;

    //Listeners
    private FormCollectionListener formListener;

    public FormFragment() {
        super();

        this.columnGroupViewList = new ArrayList<>();

        initPermissions();
    }

    public static FormFragment newInstance(HForm form, String username, Map<String, String> preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        FormFragment formFragment = new FormFragment();
        formFragment.form = form;
        formFragment.username = username;
        formFragment.executeOnUpload = executeOnUpload;
        formFragment.formListener = formListener;
        formFragment.preloadedColumnValues = new LinkedHashMap<>();

        if (preloadedValues != null){
            formFragment.preloadedColumnValues.putAll(preloadedValues);
        }

        return formFragment;
    }

    public static FormFragment newInstance(File hFormXlsFile, String username, Map<String, String> preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        return newInstance(new ExcelFormParser(hFormXlsFile).getForm(), username, preloadedValues, executeOnUpload, formListener);
    }

    public static FormFragment newInstance(InputStream fileInputStream, String username, Map<String, String> preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        return newInstance(new ExcelFormParser(fileInputStream).getForm(), username, preloadedValues, executeOnUpload, formListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.form_main, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    @Override
    public void onStart() {
        super.onStart();

        //get start timestamp
        this.startTimestamp = getTimestamp();

        checkPermissionAndGetDeviceId();
        loadColumnValues();
    }

    private void initPermissions() {
        this.requestPermission = registerForActivityResult(new RequestPermission(), granted -> {
            if (granted) {
                String deviceId = readDeviceId();
                Log.d("deviceid", ""+deviceId);
            } else {
                //Log.d("deviceid", "no permission to read it");
                DialogFactory.createMessageInfo(this.getContext(), R.string.device_id_title_lbl, R.string.device_id_permissions_error).show();
            }
        });
    }

    private void initialize(View view) {

        //this.formViewPagerLayout = view.findViewById(R.id.formViewPagerLayout);
        this.formSlider = view.findViewById(R.id.formSlider);
        this.txtFormTitle = (TextView) view.findViewById(R.id.txtFormTitle);
        this.formHeaderLayout = (LinearLayout) view.findViewById(R.id.formHeaderLayout);
        this.btCancel = view.findViewById(R.id.btCancel);
        this.btSave = view.findViewById(R.id.btSave);

        this.btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });

        this.btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
            }
        });


        this.txtFormTitle.setText(form.getFormName());

        initColumnViews();

    }

    private void onCancelClicked(){
        dismiss();
    }

    private void onSaveClicked() {

        //get end timestamp
        this.endTimestamp = getTimestamp();

        //get column values
        Map<String, ColumnValue> columnValueMap = getCollectedData();

        if (formListener != null) {
            ValidationResult result = formListener.onFormValidate(form, columnValueMap);

            if (result==null || result.hasErrors()) {
                //Show Errors - Get Focus

                for (ValidationResult.Error error : result.getColumnErrors()) {

                    DialogFactory.createMessageInfo(this.getContext(), getString(R.string.info_lbl), error.errorMessage).show();

                    setFocus(error.columnValue);
                }

                Log.d("errors", "errors - result");
            } else {
                XmlFormResult xmlResults = new XmlFormResult(form, columnValueMap.values());
                Log.d("result", ""+xmlResults.getXmlResult());
                formListener.onFormFinished(form, columnValueMap, xmlResults);
                dismiss();
            }
        }

    }

    private void initColumnViews(){

        //add header layout content
        if (this.form.hasHeader()) {
            ColumnGroup columnGroup = this.form.getHeader();
            ColumnGroupView columnGroupView = new ColumnGroupView(this, this.getContext(), columnGroup);
            this.columnGroupViewList.add(columnGroupView);

            formHeaderLayout.addView(columnGroupView);;
        }

        //For the view groups
        List<ColumnGroupView> groupViews = new ArrayList<>();
        for (ColumnGroup group : form.getColumns() ) {
            if (!group.isHeader()) //ignore headers
                groupViews.add(new ColumnGroupView(this, this.getContext(), group));
        }

        this.columnGroupViewList.addAll(groupViews);

        // VIEWPAGER
        ColumnGroupViewAdapter adapter = new ColumnGroupViewAdapter(groupViews);
        formSlider.setAdapter(adapter);

    }

    /**
     * 1. RETRIEVE COLLECTED DATA
     * 2. ON SAVE CLICKED - DONT CLOSE OR HIDE
     *   2.1. RETRIEVE COLLECTED DATA (MAP OF COLUMN-VALUE AND XML/JSON)
     *   2.2. BUT CALL A LISTENER TO VALIDATE THE DATA
     */

    private Map<String, ColumnValue> getCollectedData(){
        Map<String, ColumnValue> map = new LinkedHashMap<>();
        //List<ColumnValue> list = new ArrayList<>();

        columnGroupViewList.forEach( columnGroupView -> {
            for (ColumnView columnView : columnGroupView.getColumnViews()) {
                ColumnValue columnValue = new ColumnValue(columnGroupView, columnView);

                if (columnValue.getColumnType() == ColumnType.START_TIMESTAMP && StringTools.isBlank(columnValue.getValue())) { //start must be blank - means the first time form is opened, otherwise is reopening a saved form
                    columnValue.setValue(startTimestamp);
                }

                if (columnValue.getColumnType() == ColumnType.END_TIMESTAMP) {
                    columnValue.setValue(endTimestamp);
                }

                map.put(columnValue.getColumnName(), columnValue);
                //list.add(columnValue);
            }
        });

        return map;

    }

    private void loadColumnValues(){

        columnGroupViewList.forEach( columnGroupView -> {
            for (ColumnView columnView : columnGroupView.getColumnViews()) {

                Column column = columnView.getColumn();

                if (columnView.getType() == ColumnType.DEVICE_ID && columnView instanceof ColumnTextView) {
                    ((ColumnTextView) columnView).setValue(this.getDeviceId());
                }

                if (columnView.getType() == ColumnType.TIMESTAMP && columnView instanceof ColumnTextView) {
                    ((ColumnTextView) columnView).setValue(this.getTimestamp());
                }

                if (this.preloadedColumnValues.containsKey(column.getName())){
                    String value = this.preloadedColumnValues.get(column.getName());
                    columnView.setValue(value);
                }

                if (column.getType()==ColumnType.GPS) {
                    Map<String,Double> gpsValues = getGpsPreloadedValues(column);
                    ((ColumnGpsView)columnView).setValues(gpsValues);
                }

                if (column.getType()==ColumnType.INSTANCE_UUID && StringTools.isBlank(column.getValue())) { //id column - set only once
                    column.setValue(UUID.randomUUID().toString());
                }
            }
        });
    }

    private Map<String, Double> getGpsPreloadedValues(Column gpsColumn) {
        String[] gps_cols = new String[]{ "_lat", "_lon", "_alt", "_acc" };
        Map<String,Double> gpsValues = new LinkedHashMap<>();

        for (String ext : gps_cols) {
            String column = gpsColumn.getName()+ext;

            if (preloadedColumnValues.containsKey(column)){
                String stringValue = preloadedColumnValues.get(column);
                gpsValues.put(column, Double.parseDouble(stringValue));
            }
        }

        return gpsValues.size()==0 ? null : gpsValues;
    }

    private void setFocus(ColumnValue columnValue) {
        ColumnGroupView groupView = columnGroupViewList.stream().filter(t -> t.getId()==columnValue.getColumnGroupId()).findFirst().orElse(null);
        ColumnView columnView = groupView.findViewById(columnValue.getColumnId());

        if (groupView != null && columnView != null) {
            groupView.setFocusable(true);
            columnView.setFocusable(true);
            groupView.requestFocus();
            columnView.requestFocus();
        }
    }

    private void exitForm(){
        this.dismiss();
    }

    public String getUsername() {
        return this.username;
    }

    public String getDeviceId(){
        return this.deviceId;
    }

    public void checkPermissionAndGetDeviceId() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) { //without access
            this.requestPermission.launch(Manifest.permission.READ_PHONE_STATE);
        } else {
            this.deviceId = readDeviceId();
        }
    }

    public String readDeviceId(){

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }

        TelephonyManager mTelephonyManager = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = mTelephonyManager.getImei();
        String orDeviceId = "";

        if (deviceId != null ) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId = Settings.Secure.getString(this.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
            } else {
                orDeviceId = "imei:" + deviceId;
            }
        }
        if (deviceId == null) {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) this.getContext().getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();

            if (info != null) {
                deviceId = info.getMacAddress();
                orDeviceId = "mac:" + deviceId;
            }
        }
        // if it is still null, use ANDROID_ID
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(this.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;

            //sbuilder.append("<deviceId>"+ orDeviceId +"</deviceId>" + "\r\n");

            return  orDeviceId;
        }

        //sbuilder.append("<deviceId>"+ deviceId +"</deviceId>" + "\r\n");

        return deviceId;
    }

    private String getTimestamp() {
        //TimeZone tz = TimeZone.getDefault();
        //Calendar cal = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        //long gmt = TimeUnit.HOURS.convert(tz.getRawOffset(), TimeUnit.MILLISECONDS);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        sdf.setCalendar(cal);


        //Log.d("timezone", "GMT "+gmt);
        //Log.d("realtime", StringUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        //Log.d("original-date", ""+sdf.format(cal.getTime()));

        //cal.add(Calendar.HOUR_OF_DAY, (int) (-1 * gmt)); //Fixing ODK Error on this variable (ODK is adding GMT Hours number to the datetime of "start" variable)

        String dateString = sdf.format(cal.getTime());
        //Log.d("fixed-datetime", ""+dateString);


        return dateString;
    }
}
