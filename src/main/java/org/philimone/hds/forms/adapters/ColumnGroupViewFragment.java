package org.philimone.hds.forms.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnGroupModel;
import org.philimone.hds.forms.widget.ColumnGroupView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ColumnGroupViewFragment extends Fragment {

    private ColumnGroupModel groupModel;
    private ColumnGroupView groupView;

    public ColumnGroupViewFragment() {
        // Required empty public constructor
    }

    public static ColumnGroupViewFragment newInstance(ColumnGroupModel groupModel) {
        ColumnGroupViewFragment fragment = new ColumnGroupViewFragment();
        fragment.groupModel = groupModel;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.column_group_layout, container, false);
        
        this.groupView = new ColumnGroupView(getContext());
        FormFragment formFragment = (FormFragment) getParentFragment();
        this.groupView.bind(formFragment, groupModel, formFragment);
        
        rootView.addView(groupView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.groupView != null) {
            this.groupView.refreshChildViews();
        }
    }

    public ColumnGroupModel getGroupModel() {
        return groupModel;
    }
}
