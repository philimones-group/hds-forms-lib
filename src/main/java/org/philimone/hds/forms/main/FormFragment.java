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

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class FormFragment extends DialogFragment {

    private FragmentManager fragmentManager;
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
    private String instancesDirPath;

    private boolean backgroundMode;

    private JexlEngine expressionEngine;

    private ActivityResultLauncher<String> requestPermission;

    //Listeners
    private FormCollectionListener formListener;

    public FormFragment() {
        super();

        this.columnGroupViewList = new ArrayList<>();

        initExpEngine();

        initPermissions();
    }

    public static FormFragment newInstance(FragmentManager fragmentManager, HForm form, String instancesDirPath, String username, Map<String, String> preloadedValues, boolean executeOnUpload, boolean bgMode, FormCollectionListener formListener) {
        FormFragment formFragment = new FormFragment();
        formFragment.fragmentManager = fragmentManager;
        formFragment.form = form;
        formFragment.username = username;
        formFragment.executeOnUpload = executeOnUpload;
        formFragment.formListener = formListener;
        formFragment.preloadedColumnValues = new LinkedHashMap<>();
        formFragment.instancesDirPath = instancesDirPath;
        formFragment.backgroundMode = bgMode;

        formFragment.form.setPostExecution(executeOnUpload);

        if (preloadedValues != null){
            formFragment.preloadedColumnValues.putAll(preloadedValues);
        }

        return formFragment;
    }

    public static FormFragment newInstance(FragmentManager fragmentManager, HForm form, String instancesDirPath, String username, Map<String, String> preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        return newInstance(fragmentManager, form, instancesDirPath, username, preloadedValues, executeOnUpload, false, formListener);
    }

    public static FormFragment newInstance(FragmentManager fragmentManager, File hFormXlsFile, String instancesDirPath, String username, Map<String, String> preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        return newInstance(fragmentManager, new ExcelFormParser(hFormXlsFile).getForm(), instancesDirPath, username, preloadedValues, executeOnUpload, false, formListener);
    }

    public static FormFragment newInstance(FragmentManager fragmentManager, InputStream fileInputStream, String instancesDirPath, String username, Map<String, String> preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        return newInstance(fragmentManager, new ExcelFormParser(fileInputStream).getForm(), instancesDirPath, username, preloadedValues, executeOnUpload, false, formListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);

        this.setCancelable(false);
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

        initLoading();

        if (backgroundMode) {
            onSaveClicked();
        }
    }

    private void initPermissions() {
        this.requestPermission = registerForActivityResult(new RequestPermission(), granted -> {
            if (granted) {
                String deviceId = readDeviceId();
                Log.d("deviceid", ""+deviceId);
            } else {
                //Log.d("deviceid", "no permission to read it");
                DialogFactory.createMessageInfo(getCurrentContext(), R.string.device_id_title_lbl, R.string.device_id_permissions_error).show();
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

    private void initLoading(){
        //get start timestamp
        this.startTimestamp = getTimestamp();

        checkPermissionAndGetDeviceId();
        loadColumnValues();
    }

    private void initExpEngine() {
        this.expressionEngine = new JexlBuilder().create();
    }

    public Object evaluateExpression(String expressionText) {
        JexlContext jexlContext = new MapContext();
        JexlExpression jxelExpression = this.expressionEngine.createExpression(expressionText);

        return jxelExpression.evaluate(jexlContext);
    }

    private void onCancelClicked(){
        dismiss();
        if (formListener != null) formListener.onFormCancelled();
    }

    private void onSaveClicked() {

        //update displayable of all fields
        this.formSlider.evaluateAllDisplayConditions();

        //check required fields
        if (this.formSlider.hasAnyRequiredEmptyField()){
            return;
        }

        //get end timestamp
        this.endTimestamp = getTimestamp();

        //get column values
        Map<String, ColumnValue> columnValueMap = getCollectedData();

        if (formListener != null) {
            ValidationResult result = formListener.onFormValidate(form, columnValueMap);

            if (result==null || result.hasErrors()) {
                //Show Errors - Get Focus

                for (ValidationResult.Error error : result.getColumnErrors()) {

                    DialogFactory.createMessageInfo(getCurrentContext(), getString(R.string.info_lbl), error.errorMessage).show();

                    setFocus(error.columnValue);
                }

                Log.d("errors", "errors - result");
            } else {

                XmlFormResult xmlResults = new XmlFormResult(form, columnValueMap.values(), instancesDirPath);
                //Log.d("result", ""+xmlResults.getXmlResult());

                //createXmlFile
                createXmlResultsFile(xmlResults);

                formListener.onFormFinished(form, columnValueMap, xmlResults);
                dismiss();

            }
        }

    }
    
    private Context getCurrentContext() {
        return this.getContext();
    }

    private void initColumnViews(){

        //add header layout content
        if (this.form.hasHeader()) {
            ColumnGroup columnGroup = this.form.getHeader();
            ColumnGroupView columnGroupView = new ColumnGroupView(this, getCurrentContext(), columnGroup);
            this.columnGroupViewList.add(columnGroupView);

            if (formHeaderLayout != null) {
                formHeaderLayout.addView(columnGroupView);
            }
        }

        //For the view groups
        List<ColumnGroupView> groupViews = new ArrayList<>();
        for (ColumnGroup group : form.getColumns() ) {

            if (!group.isHeader()) {//ignore headers
                groupViews.add(new ColumnGroupView(this, getCurrentContext(), group));
            }
        }

        this.columnGroupViewList.addAll(groupViews);

        final ColumnView[] previous = {null};
        final ColumnGroupView[] previousGroups = {null};
        this.columnGroupViewList.forEach(columnGroupView -> {

            if (previousGroups[0] != null) {
                previousGroups[0].setNextGroupView(columnGroupView);
                columnGroupView.setParentGroupView(previousGroups[0]);
            }
            previousGroups[0] = columnGroupView;

            columnGroupView.getColumnViews().forEach(columnView -> {

                if (previous[0] != null) {
                    previous[0].setNextColumn(columnView);
                    columnView.setParentColumn(previous[0]);
                }
                previous[0] = columnView;

            });
        });

        // VIEWPAGER
        ColumnGroupViewAdapter adapter = new ColumnGroupViewAdapter(this, groupViews);

        if (formSlider != null) {
            formSlider.setAdapter(adapter);
        }
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

                if (column.getType()==ColumnType.INSTANCE_UUID && column.isValueBlank()) { //id column - set only once
                    String uuid = UUID.randomUUID().toString().replaceAll("-","");

                    columnView.setValue(uuid);

                    //Log.d("uuid-tag", ""+columnView.getValue());
                }

                //overwrite values with pre-loaded data
                if (this.preloadedColumnValues.containsKey(column.getName())){
                    String value = this.preloadedColumnValues.get(column.getName());
                    columnView.setValue(value);
                }

                if (column.getType()==ColumnType.GPS) {
                    Map<String,Double> gpsValues = getGpsPreloadedValues(column);
                    ((ColumnGpsView)columnView).setValues(gpsValues);
                }


            }
        });
    }

    private Map<String, Double> getGpsPreloadedValues(Column gpsColumn) {
        String[] gps_cols = new String[]{ "Lat", "Lon", "Alt", "Acc" };
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
        if (ContextCompat.checkSelfPermission(getCurrentContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) { //without access
            this.requestPermission.launch(Manifest.permission.READ_PHONE_STATE);
        } else {
            this.deviceId = readDeviceId();
        }
    }

    public String readDeviceId(){

        if (ActivityCompat.checkSelfPermission(getCurrentContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }

        TelephonyManager mTelephonyManager = (TelephonyManager) getCurrentContext().getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = mTelephonyManager.getImei();
        String orDeviceId = "";

        if (deviceId != null ) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId = Settings.Secure.getString(getCurrentContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
            } else {
                orDeviceId = "imei:" + deviceId;
            }
        }
        if (deviceId == null) {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) getCurrentContext().getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();

            if (info != null) {
                deviceId = info.getMacAddress();
                orDeviceId = "mac:" + deviceId;
            }
        }
        // if it is still null, use ANDROID_ID
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(getCurrentContext().getContentResolver(), Settings.Secure.ANDROID_ID);
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

    private boolean createXmlResultsFile(XmlFormResult xmlFormResult) {
        try {

            File file = new File(xmlFormResult.getFilename());
            file.createNewFile();


            PrintStream output = new PrintStream(xmlFormResult.getFilename());
            output.print(xmlFormResult.getXmlResult());
            output.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void startCollecting(){
/*
        if (backgroundMode){
            // Executes the form without visualizing it
            initColumnViews();
            initLoading();
            onSaveClicked();
            return;
        }*/

        show(fragmentManager, "hform");
    }

}
