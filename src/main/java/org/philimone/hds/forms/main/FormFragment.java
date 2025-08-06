package org.philimone.hds.forms.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.adapters.ColumnViewDataAdapter;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.ColumnRepeatGroup;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.PreloadMap;
import org.philimone.hds.forms.model.RepeatColumnValue;
import org.philimone.hds.forms.model.RepeatObject;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.model.enums.RepeatCountType;
import org.philimone.hds.forms.parsers.ExcelFormParser;
import org.philimone.hds.forms.parsers.XmlDataReader;
import org.philimone.hds.forms.parsers.XmlDataUpdater;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class FormFragment extends DialogFragment implements ExternalMethodCallListener {

    private FragmentManager fragmentManager;
    private HForm form;
    private FormColumnSlider formSlider;
    private TextView txtFormTitle;
    private LinearLayout formHeaderLayout;
    private LinearLayout formContentLayout;
    private RelativeLayout formResumeLayout;
    private ListView lvResumeColumns;
    private Button btOpenResume;
    private Button btCloseResume;
    private Button btCancel;
    private Button btSave;
    private List<ColumnGroupView> columnGroupViewList;
    private String username;
    private String deviceId;
    private String startTimestamp;
    private String endTimestamp;
    private boolean executeOnUpload;
    private PreloadMap preloadedColumnValues;
    private String instancesDirPath;

    private boolean backgroundMode;
    private boolean resumeMode;

    private JexlEngine expressionEngine;

    public DateUtil.SupportedCalendar supportedCalendar;

    private ActivityResultLauncher<String> requestPermission;

    //Listeners
    private FormCollectionListener formListener;

    public FormFragment() {
        super();

        this.columnGroupViewList = new ArrayList<>();

        initExpEngine();

        initPermissions();
    }

    //Opening a new Form Instance
    public static FormFragment newInstance(FragmentManager fragmentManager, HForm form, DateUtil.SupportedCalendar supportedCalendarType, String instancesDirPath, String username, PreloadMap preloadedValues, boolean executeOnUpload, boolean bgMode, boolean gotoResume, FormCollectionListener formListener) {
        FormFragment formFragment = new FormFragment();
        formFragment.fragmentManager = fragmentManager;
        formFragment.form = form;
        formFragment.username = username;
        formFragment.executeOnUpload = executeOnUpload;
        formFragment.formListener = formListener;
        formFragment.preloadedColumnValues = new PreloadMap();
        formFragment.instancesDirPath = instancesDirPath;
        formFragment.backgroundMode = bgMode;
        formFragment.resumeMode = gotoResume;

        formFragment.form.setPostExecution(executeOnUpload);

        if (preloadedValues != null){
            formFragment.preloadedColumnValues.putAll(preloadedValues);
        }

        formFragment.supportedCalendar = supportedCalendarType;

        return formFragment;
    }

    //Reopening saved Form
    public static FormFragment newInstance(FragmentManager fragmentManager, HForm form, DateUtil.SupportedCalendar supportedCalendarType, String instancesDirPath, String username, String xmlSavedFormPath, PreloadMap updatedPreloadedValues, boolean executeOnUpload, boolean bgMode, boolean gotoResume, FormCollectionListener formListener) {
        FormFragment formFragment = new FormFragment();
        formFragment.fragmentManager = fragmentManager;
        formFragment.form = form;
        formFragment.username = username;
        formFragment.executeOnUpload = executeOnUpload;
        formFragment.formListener = formListener;
        formFragment.preloadedColumnValues = new PreloadMap();
        formFragment.instancesDirPath = instancesDirPath;
        formFragment.backgroundMode = bgMode;
        formFragment.resumeMode = gotoResume;

        formFragment.form.setPostExecution(executeOnUpload);

        if (!StringUtil.isBlank(xmlSavedFormPath)){

            Map<String,Object> map = XmlDataReader.getXmlMappedData(xmlSavedFormPath, form);
            formFragment.preloadedColumnValues.putAll(map);
        }

        if (updatedPreloadedValues != null) {
            formFragment.preloadedColumnValues.putAll(updatedPreloadedValues);
        }

        formFragment.supportedCalendar = supportedCalendarType;

        return formFragment;
    }

    /* Not being Used
    public static FormFragment newInstance(FragmentManager fragmentManager, HForm form, String instancesDirPath, String username, PreloadMap preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        return newInstance(fragmentManager, form, instancesDirPath, username, preloadedValues, executeOnUpload, false, false, formListener);
    }

    public static FormFragment newInstance(FragmentManager fragmentManager, File hFormXlsFile, String instancesDirPath, String username, PreloadMap preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        return newInstance(fragmentManager, new ExcelFormParser(hFormXlsFile).getForm(), instancesDirPath, username, preloadedValues, executeOnUpload, false, false, formListener);
    }

    public static FormFragment newInstance(FragmentManager fragmentManager, InputStream fileInputStream, String instancesDirPath, String username, PreloadMap preloadedValues, boolean executeOnUpload, FormCollectionListener formListener) {
        return newInstance(fragmentManager, new ExcelFormParser(fileInputStream).getForm(), instancesDirPath, username, preloadedValues, executeOnUpload, false, false, formListener);
    }
    */

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
        } else if (resumeMode) {
            onOpenResumeClicked();
        }

        this.formListener.onFormLoaded(null);
    }

    private void initPermissions() {
        this.requestPermission = registerForActivityResult(new RequestPermission(), granted -> {
            if (granted) {
                this.deviceId = readDeviceId();

                Log.d("deviceid", ""+deviceId);
                updateColumnDeviceId();
            } else {
                //Log.d("deviceid", "no permission to read it");
                DialogFactory.createMessageInfo(getCurrentContext(), R.string.device_id_title_lbl, R.string.device_id_permissions_error, new DialogFactory.OnClickListener() {
                    @Override
                    public void onClicked(DialogFactory.Buttons clickedButton) {
                        FormFragment.this.dismiss();
                    }
                }).show();
            }
        });
    }

    private void initialize(View view) {

        //this.formViewPagerLayout = view.findViewById(R.id.formViewPagerLayout);
        this.formSlider = view.findViewById(R.id.formSlider);
        this.txtFormTitle = (TextView) view.findViewById(R.id.txtFormTitle);
        this.formHeaderLayout = (LinearLayout) view.findViewById(R.id.formHeaderLayout);
        this.formContentLayout = view.findViewById(R.id.formContentLayout);
        this.formResumeLayout = view.findViewById(R.id.formResumeLayout);
        this.lvResumeColumns = view.findViewById(R.id.lvResumeColumns);
        this.btOpenResume = view.findViewById(R.id.btOpenResume);
        this.btCloseResume = view.findViewById(R.id.btCloseResume);
        this.btCancel = view.findViewById(R.id.btCancel);
        this.btSave = view.findViewById(R.id.btSave);

        this.btCancel.setOnClickListener(v -> onCancelClicked());

        this.btSave.setOnClickListener(v -> onSaveClicked());

        this.btOpenResume.setOnClickListener(v -> {
            onOpenResumeClicked();
        });

        this.btCloseResume.setOnClickListener(v -> {
            onCloseResumeClicked();
        });

        this.lvResumeColumns.setOnItemClickListener((parent, view1, position, id) -> {
            onResumeListItemClicked(position);
        });

        initFormTitle();

        formSlider.setFormFragment(this);

        initColumnViews();
    }

    private void initFormTitle() {
        this.txtFormTitle.setText(form.getFormName());

        if (this.form.getCustomTitle() != null && form.getFormName() != null) {
            String fname = this.form.getFormName();
            String newTitle = fname.replaceAll("\\$\\{form_title\\}", this.form.getCustomTitle());
            this.txtFormTitle.setText(newTitle);
        }
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

        try {
            JexlExpression jxelExpression = this.expressionEngine.createExpression(expressionText);
            return jxelExpression.evaluate(jexlContext);
        } catch (Exception ex) {
            Log.d("Jexl Evaluation", "Error: "+expressionText);
            ex.printStackTrace();

            return "false";
        }
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
        CollectedDataMap columnValueMap = getCollectedData();

        if (formListener != null) {
            ValidationResult result = formListener.onFormValidate(form, columnValueMap);

            if (result==null || result.hasErrors()) {
                //Show Errors - Get Focus

                for (ValidationResult.Error error : result.getColumnErrors()) {

                    setFocus(error.columnValue);

                    DialogFactory.createMessageInfo(getCurrentContext(), getString(R.string.info_lbl), error.errorMessage).show();
                }

                Log.d("errors", "errors - result"+result.hasErrors());
            } else {

                //update HForm columnValueMap
                formListener.onBeforeFormFinished(form, columnValueMap);

                XmlFormResult xmlResults = new XmlFormResult(form, columnValueMap.values(), instancesDirPath);
                //Log.d("result", ""+xmlResults.getXmlResult());

                //createXmlFile
                createXmlResultsFile(xmlResults);

                formListener.onFormFinished(form, columnValueMap, xmlResults);
                dismiss();

            }
        }

    }

    private void onOpenResumeClicked(){
        openResumeView();
    }

    private void onCloseResumeClicked(){
        closeResumeView();
    }

    private void onResumeListItemClicked(int position) {
        ColumnViewDataAdapter adapter = (ColumnViewDataAdapter) this.lvResumeColumns.getAdapter();
        //Log.d("position", ""+position +", adapter="+adapter);
        if (adapter != null) {
            closeResumeView();
            ColumnView columnView = adapter.getItem(position);
            this.formSlider.gotoPage(columnView);
        }
    }

    public void closeResumeView(){
        if (this.resumeMode){
            this.resumeMode = false;
            this.formResumeLayout.setVisibility(View.GONE);
            this.formContentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void openResumeView(){
        this.resumeMode = true;

        loadResumeListView();

        this.formResumeLayout.setVisibility(View.VISIBLE);
        this.formContentLayout.setVisibility(View.GONE);
    }

    private void loadResumeListView() {
        List<ColumnView> list = new ArrayList<>();

        for (ColumnGroupView columnGroupView : columnGroupViewList) {
            if (!columnGroupView.isHidden()) {
                for (ColumnView columnView : columnGroupView.getColumnViews()) {
                    if (!columnView.isHidden()) {
                        list.add(columnView);
                    }
                }
            }
        }

        ColumnViewDataAdapter adapter = new ColumnViewDataAdapter(this.getContext(), list);
        this.lvResumeColumns.setAdapter(adapter);
    }

    private Context getCurrentContext() {
        return this.getContext();
    }

    private void initColumnViews(){

        //add header layout content
        ColumnGroupView headerGroupView = null;
        boolean isUsingHeaderLayout = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (this.form.hasHeader()) {
            ColumnGroup columnGroup = this.form.getHeader();
            headerGroupView = new ColumnGroupView(this, getCurrentContext(), columnGroup, this);
            //this.columnGroupViewList.add(headerGroupView);

            if (formHeaderLayout != null && isUsingHeaderLayout) {
                formHeaderLayout.addView(headerGroupView);
            }
        }

        //For the view groups
        List<ColumnGroupView> groupViews = new ArrayList<>();
        for (ColumnGroup group : form.getColumns() ) {

            if (!group.isHeader()) {//ignore headers

                if (group instanceof ColumnRepeatGroup){
                    ColumnRepeatGroup repeatGroup = (ColumnRepeatGroup) group;

                    if (repeatGroup.getRepeatCountType()== RepeatCountType.VARIABLE){
                        //special group view (hidden), used to create anothers - the first instance of a repeat group powered by variable number
                        //******** TO BE DONE FOR PREGNANCY OUTCOME ***************
                        ColumnGroupView groupView = new ColumnGroupView(this, getCurrentContext(), repeatGroup, repeatGroup.getColumnsGroups().get(0), 0, true, this);
                        groupViews.add(groupView);
                        this.columnGroupViewList.add(groupView);
                        continue;
                    }

                    //EXTERNAL LOADER OR CONSTANT VALUE

                    Integer repeatGroupSize = repeatGroup.getRepeatSize(this.preloadedColumnValues);

                    for (int repeatIndex = 0; repeatIndex < repeatGroupSize; repeatIndex++) {
                        for (ColumnGroup innerGroup : repeatGroup.getColumnsGroups()) {
                            ColumnGroupView groupView = new ColumnGroupView(this, getCurrentContext(), repeatGroup, innerGroup, repeatIndex, repeatGroupSize, this);
                            groupViews.add(groupView);
                            this.columnGroupViewList.add(groupView);
                        }
                    }
                } else {

                    ColumnGroupView groupView = new ColumnGroupView(this, getCurrentContext(), group, this);
                    groupViews.add(groupView);
                    this.columnGroupViewList.add(groupView);
                }


            } else {
                this.columnGroupViewList.add(headerGroupView);

                if (!isUsingHeaderLayout) { //not using header layout - in landscape mode the header is a entire page
                    groupViews.add(headerGroupView);
                }else {
                    //because the header is not part of the FormColumnSlider - we need to evaluate now

                    headerGroupView.evaluateRequired();
                    headerGroupView.evaluateReadOnly();
                    headerGroupView.evaluateCalculations();
                    headerGroupView.evaluateDisplayCondition();
                }
            }
        }

        final ColumnView[] previous = {null};
        final ColumnGroupView[] previousGroups = {null};
        for (ColumnGroupView columnGroupView : this.columnGroupViewList) {
            if (previousGroups[0] != null) {
                previousGroups[0].setNextGroupView(columnGroupView);
                columnGroupView.setParentGroupView(previousGroups[0]);
            }
            previousGroups[0] = columnGroupView;

            for (ColumnView columnView : columnGroupView.getColumnViews()) {
                if (previous[0] != null) {
                    previous[0].setNextColumn(columnView);
                    columnView.setParentColumn(previous[0]);
                }
                previous[0] = columnView;

            }
        }

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

    private CollectedDataMap getCollectedData(){
        CollectedDataMap map = new CollectedDataMap();
        //List<ColumnValue> list = new ArrayList<>();

        for (ColumnGroupView columnGroupView : columnGroupViewList) {
            for (ColumnView columnView : columnGroupView.getColumnViews()) {
                ColumnValue columnValue = new ColumnValue(columnGroupView, columnView);

                if (columnValue.getColumnType() == ColumnType.START_TIMESTAMP && StringUtil.isBlank(columnValue.getValue())) { //start must be blank - means the first time form is opened, otherwise is reopening a saved form
                    columnValue.setValue(startTimestamp);
                }

                if (columnValue.getColumnType() == ColumnType.END_TIMESTAMP && backgroundMode==false) { //backgroung mode is not editing the form
                    columnValue.setValue(endTimestamp);
                }

                if (columnGroupView.belongsToRepeatGroup()) {
                    ColumnRepeatGroup repeatGroup = columnGroupView.getColumnRepeatGroup();

                    //repeatGroup.name, columnValue.name, columnGroupView.repeatGroupIndex
                    RepeatColumnValue repeatColumnValue = map.getRepeatColumn(repeatGroup.getName());
                    repeatColumnValue = repeatColumnValue == null ? new RepeatColumnValue(repeatGroup.getGroupName(), repeatGroup.getNodeName()) : repeatColumnValue;
                    repeatColumnValue.put(columnGroupView.getRepeatGroupIndex(), columnValue);

                    map.put(repeatColumnValue);
                    continue;
                }

                map.put(columnValue.getColumnName(), columnValue);
                //list.add(columnValue);
            }
        }

        return map;
    }

    private void loadColumnValues(){

        for (ColumnGroupView columnGroupView : columnGroupViewList) {
            for (ColumnView columnView : columnGroupView.getColumnViews()) {

                Column column = columnView.getColumn();

                if (columnView.getType() == ColumnType.DEVICE_ID && columnView instanceof ColumnTextView) {
                    columnView.setValue(this.getDeviceId());
                    Log.d("device-id*", columnView.getValue());
                }

                if (columnView.getType() == ColumnType.TIMESTAMP && columnView instanceof ColumnTextView) {
                    columnView.setValue(this.getTimestamp());
                }

                if (column.getType() == ColumnType.INSTANCE_UUID && column.isValueBlank()) { //id column - set only once
                    String uuid = UUID.randomUUID().toString().replaceAll("-", "");

                    columnView.setValue(uuid);

                    //Log.d("uuid-tag", ""+columnView.getValue());
                }

                if (columnGroupView.belongsToRepeatGroup()) {
                    ColumnRepeatGroup repeatGroup = columnGroupView.getColumnRepeatGroup();
                    Integer repeatIndex = columnGroupView.getRepeatGroupIndex();

                    if (this.preloadedColumnValues.containsKey(repeatGroup.getName())) { //contains a RepeatObject?
                        RepeatObject repeatObject = this.preloadedColumnValues.getRepeatObject(repeatGroup.getName());
                        String value = repeatObject.get(repeatIndex, column.getName());
                        columnView.setValue(value);
                        continue;
                    }
                }

                //overwrite values with pre-loaded data
                if (this.preloadedColumnValues.containsKey(column.getName())) {
                    String value = this.preloadedColumnValues.getStringValue(column.getName());
                    columnView.setValue(value);
                }

                if (column.getType() == ColumnType.GPS) {
                    Map<String, Double> gpsValues = getGpsPreloadedValues(column);
                    ((ColumnGpsView) columnView).setValues(gpsValues);
                }


            }
        }

        //update visibility of fragments
        if (formSlider.getAdapter() != null) {
            formSlider.getAdapter().reEvaluateDisplayConditions();
        }
    }

    private void updateColumnDeviceId() {
        for (ColumnGroupView columnGroupView : columnGroupViewList) {
            for (ColumnView columnView : columnGroupView.getColumnViews()) {

                if (columnView.getType() == ColumnType.DEVICE_ID && columnView instanceof ColumnTextView) {
                    columnView.setValue(this.getDeviceId());
                    Log.d("device-id*2", columnView.getValue());
                }

            }
        }
    }

    private Map<String, Double> getGpsPreloadedValues(Column gpsColumn) {
        String[] gps_cols = new String[]{ "Lat", "Lon", "Alt", "Acc" };
        Map<String,Double> gpsValues = new LinkedHashMap<>();

        for (String ext : gps_cols) {
            String column = gpsColumn.getName()+ext;

            if (preloadedColumnValues.containsKey(column)){
                String stringValue = preloadedColumnValues.getStringValue(column);
                gpsValues.put(column, Double.parseDouble(stringValue));
            }
        }

        return gpsValues.size()==0 ? null : gpsValues;
    }

    private void setFocus(ColumnValue columnValue) {
        ColumnGroupView groupView = columnGroupViewList.stream().filter(t -> t.getId()==columnValue.getColumnGroupId()).findFirst().orElse(null);
        ColumnView columnView = columnValue.getColumnView();

        if (groupView != null && columnView != null) {
            groupView.setFocusable(true);
            columnView.setFocusable(true);
            groupView.requestFocus();
            columnView.requestFocus();

            formSlider.gotoPage(columnView);
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
        /*
        if (grantedPermission || ActivityCompat.checkSelfPermission(getCurrentContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return "";
        }*/

        TelephonyManager mTelephonyManager = (TelephonyManager) getCurrentContext().getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(getCurrentContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            deviceId = mTelephonyManager.getImei();
        }
        String orDeviceId = "";

        if (deviceId != null ) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId = Settings.Secure.getString(getCurrentContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
            } else {
                orDeviceId = "imei:" + deviceId;
            }
        } else {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) getCurrentContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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

    private static String getTimestamp() {
        //TimeZone tz = TimeZone.getDefault();
        //Calendar cal = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        //long gmt = TimeUnit.HOURS.convert(tz.getRawOffset(), TimeUnit.MILLISECONDS);

        Calendar cal = Calendar.getInstance(); //using gregorian calendar
        cal.setTime(new Date());

        sdf.setCalendar(cal);


        //Log.d("timezone", "GMT "+gmt);
        //Log.d("realtime", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
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

    public static void updateEndTimestamp(HForm form, String xmlSavedFormPath) {
        if (!StringUtil.isBlank(xmlSavedFormPath)){

            Map<String,String> map = new LinkedHashMap<>();
            map.put(ColumnType.END_TIMESTAMP.getCode(), getTimestamp());


            XmlDataUpdater updater = new XmlDataUpdater(form, xmlSavedFormPath);
            updater.updateValues(map);
        }
    }

    public static void updateColumnOnXML(HForm form, String xmlSavedFormPath, String columnName, String columnValue){
        if (!StringUtil.isBlank(xmlSavedFormPath) && !StringUtil.isBlank(columnName)){

            //columnValue = StringTools.isBlank(columnValue) ? "" : columnValue;

            Map<String,String> map = new LinkedHashMap<>();
            map.put(columnName, columnValue);

            XmlDataUpdater updater = new XmlDataUpdater(form, xmlSavedFormPath);
            updater.updateValues(map);
        }
    }

    public static void updateColumnOnXML(HForm form, String xmlSavedFormPath, Map<String, String> columnNameValueMap){
        if (!StringUtil.isBlank(xmlSavedFormPath)){
            XmlDataUpdater updater = new XmlDataUpdater(form, xmlSavedFormPath);
            updater.updateValues(columnNameValueMap);
        }
    }

    /*
     * Used to call a method or function outside of HDS-Explorer
     */
    @Override
    public String onCallMethod(String methodExpression, String[] args) {
        return this.formListener.onFormCallMethod(methodExpression, args);
    }
}
