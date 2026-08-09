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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.adapters.ColumnViewDataAdapter;
import org.philimone.hds.forms.model.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.ColumnGroupModel;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.ColumnRepeatGroup;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.PreloadMap;
import org.philimone.hds.forms.model.RepeatColumnValue;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.model.parsers.XmlDataReader;
import org.philimone.hds.forms.utilities.XmlDataUpdater;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;
import org.philimone.hds.forms.widget.ColumnAudioView;
import org.philimone.hds.forms.widget.ColumnBarcodeView;
import org.philimone.hds.forms.widget.ColumnGpsView;
import org.philimone.hds.forms.widget.ColumnGroupView;
import org.philimone.hds.forms.widget.ColumnImageView;
import org.philimone.hds.forms.widget.ColumnVideoView;
import org.philimone.hds.forms.widget.FormColumnSlider;
import org.philimone.hds.forms.widget.dialog.DialogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import android.content.Intent;

public class FormFragment extends DialogFragment implements ExternalMethodCallListener, FormController.OnFormStateListener {

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
    private String username;
    private String deviceId;
    private String startTimestamp;
    private String endTimestamp;
    private boolean executeOnUpload;
    private PreloadMap preloadedColumnValues;
    private String instancesDirPath;
    private String formInstanceFileName;

    private boolean backgroundMode;
    private boolean resumeMode;
    private boolean editingFormInstance;

    public DateUtil.SupportedCalendar supportedCalendar;

    private ActivityResultLauncher<String> requestPermission;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;
    private ActivityResultLauncher<String[]> audioPermissionLauncher;
    private ActivityResultLauncher<Intent> imageLauncher;
    private ActivityResultLauncher<Intent> videoLauncher;
    private ActivityResultLauncher<String[]> gpsPermissionLauncher;

    private ColumnBarcodeView activeBarcodeView;
    private ColumnAudioView activeAudioView;
    private ColumnImageView activeImageView;
    private ColumnVideoView activeVideoView;
    private ColumnGpsView activeGpsView;

    //Listeners
    private FormCollectionListener formListener;

    private FormController formController;

    public FormFragment() {
        super();

        initLaunchers();
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
        formFragment.editingFormInstance = true;

        formFragment.form.setPostExecution(executeOnUpload);

        if (!StringUtil.isBlank(xmlSavedFormPath)){
            formFragment.formInstanceFileName = formFragment.getInstanceFileName(xmlSavedFormPath);

            Map<String,Object> map = XmlDataReader.getXmlMappedData(xmlSavedFormPath, form);
            formFragment.preloadedColumnValues.putAll(map);
        }

        if (updatedPreloadedValues != null) {
            formFragment.preloadedColumnValues.putAll(updatedPreloadedValues);
        }

        formFragment.supportedCalendar = supportedCalendarType;

        return formFragment;
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
        } else if (resumeMode) {
            onOpenResumeClicked();
        }

        this.formListener.onFormLoaded(null);
    }

    private void initLaunchers() {
        this.requestPermission = registerForActivityResult(new RequestPermission(), granted -> {
            if (granted) {
                this.deviceId = readDeviceId();

                Log.d("deviceid", ""+deviceId);
                if (formController != null) {
                    formController.getFormContext().deviceId = deviceId;
                    formController.evaluateAll();
                }
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

        this.barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (activeBarcodeView != null) {
                activeBarcodeView.onBarcodeResult(result);
            }
        });

        this.audioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (activeAudioView != null) {
                activeAudioView.onPermissionsGranted(result);
            }
        });

        this.imageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (activeImageView != null) {
                activeImageView.onImageCaptured(result);
            }
        });

        this.videoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (activeVideoView != null) {
                activeVideoView.onVideoCaptured(result);
            }
        });

        this.gpsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (activeGpsView != null) {
                activeGpsView.onPermissionsGranted(result);
            }
        });
    }

    public void launchBarcodeScanner(ColumnBarcodeView view, ScanOptions options) {
        this.activeBarcodeView = view;
        this.barcodeLauncher.launch(options);
    }

    public void launchAudioPermissions(ColumnAudioView view, String[] permissions) {
        this.activeAudioView = view;
        this.audioPermissionLauncher.launch(permissions);
    }

    public void launchImageCapture(ColumnImageView view, Intent intent) {
        this.activeImageView = view;
        this.imageLauncher.launch(intent);
    }

    public void launchVideoCapture(ColumnVideoView view, Intent intent) {
        this.activeVideoView = view;
        this.videoLauncher.launch(intent);
    }

    public void launchGpsPermissions(ColumnGpsView view, String[] permissions) {
        this.activeGpsView = view;
        this.gpsPermissionLauncher.launch(permissions);
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

        initFormController();
    }

    @Override
    public void onFormStructureChanged() {
        if (formSlider != null) {
            formSlider.post(() -> {
                if (formSlider.getAdapter() != null) {
                    ColumnGroupViewAdapter adapter = (ColumnGroupViewAdapter) formSlider.getAdapter();
                    adapter.refreshVisibleModels();
                }
            });
        }
    }

    private void initFormTitle() {
        if (form == null) return;

        this.txtFormTitle.setText(form.getFormName());

        if (this.form.getCustomTitle() != null && form.getFormName() != null) {
            String fname = this.form.getFormName();
            String newTitle = fname.replaceAll("\\$\\{form_title\\}", this.form.getCustomTitle());
            this.txtFormTitle.setText(newTitle);
        }
    }

    private void initLoading(){

    }

    private void onCancelClicked(){
        dismiss();
        if (formListener != null) formListener.onFormCancelled();
    }

    private void onSaveClicked() {

        //update displayable of all fields
        this.formController.evaluateAll();

        //check required fields
        if (this.formSlider.hasAnyRequiredEmptyField()){
            return;
        }

        //check validation
        if (this.formSlider.hasAnyInvalidField()){
            return;
        }

        //get end timestamp
        this.endTimestamp = getTimestamp();
        this.formController.finalizeForm(endTimestamp);

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

                XmlFormResult xmlResults = new XmlFormResult(form, supportedCalendar, columnValueMap.values(), instancesDirPath, getFormInstanceFileName());
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
        if (adapter != null) {
            closeResumeView();
            ColumnModel columnModel = adapter.getItem(position);
            if (columnModel != null) {
                this.formSlider.gotoPage(columnModel.getParentGroupModel());
            }
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
        List<ColumnModel> list = new ArrayList<>();

        for (ColumnGroupModel groupModel : formController.getGroupModels()) {
            if (!groupModel.isHidden()) {
                for (ColumnModel columnModel : groupModel.getColumnModels()) {
                    if (!columnModel.getColumn().isHidden()) {
                        list.add(columnModel);
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

    /**
     * Create FormController, ColumnGroupModels and Adapter based on the blueprint provided by ColumnGroups
     */
    private void initFormController(){
        readInitialData();
        FormController.FormContext context = new FormController.FormContext(supportedCalendar, username, deviceId, startTimestamp);
        this.formController = new FormController(form, editingFormInstance, preloadedColumnValues, context, this);
        this.formController.setStateListener(this);
        //this.formController.evaluateAll();

        createFormInstanceFileName();

        // VIEWPAGER
        ColumnGroupViewAdapter adapter = new ColumnGroupViewAdapter(this, this.formController);

        if (formSlider != null) {
            formSlider.setAdapter(adapter);
        }

        //set header on formHeaderLayout
        ColumnGroupModel headerGroupModel = this.formController.getHeaderGroupModel();
        if (headerGroupModel != null) {
            ColumnGroupView headerView = new ColumnGroupView(this.getContext());
            headerView.bind(this, headerGroupModel, this);
            this.formHeaderLayout.addView(headerView);
        }
    }

    private void readInitialData() {
        //get start timestamp
        this.startTimestamp = getTimestamp();

        checkPermissionAndGetDeviceId();
        // loadColumnValues(); // Moved to FormController
    }

    /**
     * 1. RETRIEVE COLLECTED DATA
     * 2. ON SAVE CLICKED - DONT CLOSE OR HIDE
     *   2.1. RETRIEVE COLLECTED DATA (MAP OF COLUMN-VALUE AND XML/JSON)
     *   2.2. BUT CALL A LISTENER TO VALIDATE THE DATA
     */

    private CollectedDataMap getCollectedData(){
        CollectedDataMap map = new CollectedDataMap();

        for (ColumnGroupModel gm : formController.getGroupModels()) {

            for (ColumnModel cm : gm.getColumnModels()) {
                ColumnValue columnValue = new ColumnValue(gm, cm);

                if (gm.isRepeatItem()) {
                    RepeatColumnValue rcv = getRepeatColumnValue(map, gm);
                    rcv.put(gm.getRepeatIndex(), columnValue);
                } else {
                    map.put(cm.getName(), columnValue);
                }
            }
        }

        return map;
    }

    private RepeatColumnValue getRepeatColumnValue(CollectedDataMap map, ColumnGroupModel gm) {
        ColumnRepeatGroup repeatGroup = gm.getRepeatGroup();
        ColumnGroupModel contextGM = gm.getParentGroupModel();

        // Find the nearest ancestor that is a repeat instance
        while (contextGM != null && !contextGM.isRepeatItem()) {
            contextGM = contextGM.getParentGroupModel();
        }

        if (contextGM == null) {
            // Top-level repeat: Find or create in the root map
            RepeatColumnValue rcv = map.getRepeatColumn(repeatGroup.getName());
            if (rcv == null) {
                rcv = new RepeatColumnValue(repeatGroup.getGroupName(), repeatGroup.getNodeName());
                map.put(rcv);
            }
            return rcv;
        } else {
            // Nested repeat:
            // 1. Recursively get the RepeatColumnValue that contains the parent instance
            RepeatColumnValue parentRCV = getRepeatColumnValue(map, contextGM);

            // 2. Get the specific instance index of the parent
            int parentIndex = contextGM.getRepeatIndex();

            // 3. Find/Create the child RepeatColumnValue inside that parent instance
            ColumnValue childCV = parentRCV.get(repeatGroup.getName(), parentIndex);
            RepeatColumnValue childRCV;

            if (childCV instanceof RepeatColumnValue) {
                childRCV = (RepeatColumnValue) childCV;
            } else {
                childRCV = new RepeatColumnValue(repeatGroup.getGroupName(), repeatGroup.getNodeName());
                parentRCV.put(parentIndex, repeatGroup.getName(), childRCV);
            }

            return childRCV;
        }
    }

    public String getInstancesDirPath() {
        return this.instancesDirPath;
    }

    public String getFormInstanceFileName() {
        return this.formInstanceFileName;
    }

    private String createFormInstanceFileName() {
        //form-id + form-uuid + date
        //collectedDate is a timestamp or precise date
        String formattedDate = startTimestamp;
        if (startTimestamp != null) {
            Date date = DateUtil.toDatePrecise(startTimestamp);
            formattedDate = new DateUtil(supportedCalendar).formatPrecise(date);
        }

        String uuid = formController != null ? formController.getInstanceUUID() : "";

        if (!editingFormInstance) {
            //create new instance file name - if its editing it will just reuse the last one
            this.formInstanceFileName = form.getFormId() + "-" + uuid + "-" + formatUnderscoreDate(formattedDate);
        }

        return this.formInstanceFileName;
    }

    private String getInstanceFileName(String xmlSavedFormPath) {
        File savedFile = new File(xmlSavedFormPath);
        String filename = savedFile.getName();
        if (filename.endsWith(".xml")) {
            filename = filename.substring(0, filename.length() - 4);
        }

        return filename;
    }

    private String formatUnderscoreDate(String collectedDate) {
        //yyyy-MM-dd_HH_mm_ss
        collectedDate = collectedDate.replaceAll(" ", "_");
        collectedDate = collectedDate.replaceAll(":", "_");

        return collectedDate;
    }

    private void setFocus(ColumnValue columnValue) {
        String groupUuid = columnValue.getColumnGroupUuid();
        
        // Find the group model
        ColumnGroupModel groupModel = formController.getGroupModels().stream()
                .filter(gm -> gm.getUuid().equals(groupUuid))
                .findFirst().orElse(null);

        if (groupModel != null) {
            // Tell the slider to go to this group/page
            // We'll need a method in FormColumnSlider to handle models
            formSlider.gotoPage(groupModel);
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

    public FormController getFormController() {
        return formController;
    }

    public FormColumnSlider getFormSlider() {
        return formSlider;
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
