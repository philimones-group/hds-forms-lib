package org.philimone.hds.forms.main;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.listeners.FormCollectionListener;
import org.philimone.hds.forms.listeners.GpsPermissionListener;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;
import org.philimone.hds.forms.parsers.ExcelFormParser;
import org.philimone.hds.forms.widget.ColumnGroupView;
import org.philimone.hds.forms.widget.ColumnView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

public class FormFragment extends DialogFragment {

    public static final int REQUEST_GPS_PERMISSION = 34;

    private HForm form;
    private ViewPager2 formMainViewPager;
    private TextView txtFormTitle;
    private LinearLayout formHeaderLayout;
    private Button btCancel;
    private Button btSave;
    private List<ColumnGroupView> columnGroupViewList;
    private String username;

    //Listeners
    private GpsPermissionListener permissionListener;
    private FormCollectionListener formListener;

    public FormFragment() {
        super();

        this.columnGroupViewList = new ArrayList<>();
    }

    public static FormFragment newInstance(HForm form, String username, FormCollectionListener formListener) {
        FormFragment formFragment = new FormFragment();
        formFragment.form = form;
        formFragment.username = username;
        formFragment.formListener = formListener;

        return formFragment;
    }

    public static FormFragment newInstance(File hFormXlsFile, String username, FormCollectionListener formListener) {
        return newInstance(new ExcelFormParser(hFormXlsFile).getForm(), username, formListener);
    }

    public static FormFragment newInstance(InputStream fileInputStream, String username, FormCollectionListener formListener) {
        return newInstance(new ExcelFormParser(fileInputStream).getForm(), username, formListener);
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

    private void initialize(View view) {

        this.formMainViewPager = (ViewPager2) view.findViewById(R.id.formMainViewPager);
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

    public GpsPermissionListener getPermissionListener() {
        return permissionListener;
    }

    public void setPermissionListener(GpsPermissionListener permissionListener) {
        this.permissionListener = permissionListener;
    }

    private void onCancelClicked(){
        dismiss();
    }

    private void onSaveClicked() {
        //get column values
        List<ColumnValue> columnValueList = getCollectedData();

        if (formListener != null) {
            ValidationResult result = formListener.onFormValidate(form, columnValueList);

            if (result.hasErrors()) {
                //Show Errors
                Log.d("errors", "errors - result");
            } else {
                XmlFormResult xmlResults = new XmlFormResult(form, columnValueList);
                Log.d("result", ""+xmlResults.getXmlResult());
                formListener.onFormFinished(form, columnValueList, xmlResults);
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
        formMainViewPager.setAdapter(adapter);
    }

    /**
     * 1. RETRIEVE COLLECTED DATA
     * 2. ON SAVE CLICKED - DONT CLOSE OR HIDE
     *   2.1. RETRIEVE COLLECTED DATA (MAP OF COLUMN-VALUE AND XML/JSON)
     *   2.2. BUT CALL A LISTENER TO VALIDATE THE DATA
     * 3. RECEIVE CSV/JSON FROM INTENT DATA AND THEN CONVERT INTO A HFORM  (CsvHFormParser,JsonHFormParser)
     * 4. Convert SQLite to ObjectBox
     */

    private List<ColumnValue> getCollectedData(){
        List<ColumnValue> list = new ArrayList<>();

        columnGroupViewList.forEach( columnGroupView -> {
            for (ColumnView columnView : columnGroupView.getColumnViews()) {
                ColumnValue columnValue = new ColumnValue(columnGroupView, columnView);
                list.add(columnValue);
            }
        });

        return list;

    }

    private void exitForm(){
        this.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_GPS_PERMISSION) {
            //call gps detector if

            long granted = Arrays.stream(grantResults).filter(g -> g== PackageManager.PERMISSION_GRANTED).count();

            if (granted == grantResults.length) {
                if (permissionListener!=null) permissionListener.onGpsPermissionGranted();
            } else {
                if (permissionListener!=null) permissionListener.onGpsPermissionDenied();
            }

        }
    }
}
