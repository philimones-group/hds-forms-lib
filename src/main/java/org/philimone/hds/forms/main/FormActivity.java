package org.philimone.hds.forms.main;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.listeners.GpsPermissionListener;
import org.philimone.hds.forms.main.testing.FormModelFactory;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.widget.ColumnGroupView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class FormActivity extends Activity {

    public static final int REQUEST_GPS_PERMISSION = 34;

    private HForm form;
    private ViewPager2 formMainViewPager;
    private TextView txtFormTitle;
    private LinearLayout formHeaderLayout;
    private Button btCancel;
    private Button btSave;
    private List<ColumnGroupView> columnGroupViewList;
    //Listeners
    private GpsPermissionListener permissionListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_main);

        this.columnGroupViewList = new ArrayList<>();

        initViews();
    }

    private void initViews() {
        this.form = FormModelFactory.getTestForm();

        this.formMainViewPager = (ViewPager2) findViewById(R.id.formMainViewPager);
        this.txtFormTitle = (TextView) findViewById(R.id.txtFormTitle);
        this.formHeaderLayout = (LinearLayout) findViewById(R.id.formHeaderLayout);
        this.btCancel = findViewById(R.id.btCancel);
        this.btSave = findViewById(R.id.btSave);

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

    }

    private void onSaveClicked() {

    }

    private void initColumnViews(){
        //add header layout content
        if (this.form.hasHeader()) {
            ColumnGroup columnGroup = this.form.getHeader();
            ColumnGroupView columnGroupView = new ColumnGroupView(this, columnGroup);
            this.columnGroupViewList.add(columnGroupView);

            formHeaderLayout.addView(columnGroupView);;
        }

        //For the view groups
        List<ColumnGroupView> groupViews = new ArrayList<>();
        for (ColumnGroup group : form.getColumns() ) {
            if (!group.isHeader()) //ignore headers
                groupViews.add(new ColumnGroupView(this, group));
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

    private void exitForm(){
        finish();
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