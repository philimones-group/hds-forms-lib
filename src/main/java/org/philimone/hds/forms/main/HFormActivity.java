package org.philimone.hds.forms.main;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.adapters.ColumnGroupViewAdapter;
import org.philimone.hds.forms.main.testing.FormModelFactory;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.widget.ColumnGroupView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class HFormActivity extends Activity {

    private HForm form;
    private ViewPager2 formMainViewPager;
    private TextView txtFormTitle;
    private LinearLayout formHeaderLayout;
    private Button btCancel;
    private Button btSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_main);

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
                exitForm();
            }
        });

        this.btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitForm();
            }
        });


        this.txtFormTitle.setText(form.getFormName());

        initColumnViews();
    }

    private void initColumnViews(){
        //add header layout content
        if (this.form.hasHeader()) {
            ColumnGroup columnGroup = this.form.getHeader();
            formHeaderLayout.addView(new ColumnGroupView(this, columnGroup));

            Log.d("add this", ""+columnGroup);
        }

        //For the view groups
        List<ColumnGroupView> groupViews = new ArrayList<>();
        for (ColumnGroup group : form.getColumns() ) {
            if (!group.isHeader()) //ignore headers
                groupViews.add(new ColumnGroupView(this, group));
        }

        // VIEWPAGER
        ColumnGroupViewAdapter adapter = new ColumnGroupViewAdapter(groupViews);

        //formMainViewPager.setPageTransformer(createPageAnimator());
        formMainViewPager.setAdapter(adapter);
    }

    ViewPager2.PageTransformer createPageAnimator() {
        ViewPager2.PageTransformer animator = new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float absPos = Math.abs(position);

                page.setRotation(0f);

                page.setTranslationY(absPos * 0f);
                page.setTranslationX(absPos * 0f);
                page.setScaleX(1f);
                page.setScaleY(1f);
            }
        };

        return animator;
    }

    private void exitForm(){
        finish();
    }

}