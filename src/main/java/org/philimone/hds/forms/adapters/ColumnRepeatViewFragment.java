package org.philimone.hds.forms.adapters;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnGroupModel;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.ColumnRepeatModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ColumnRepeatViewFragment extends Fragment {

    private ColumnRepeatModel repeatModel;

    public ColumnRepeatViewFragment() {}

    public static ColumnRepeatViewFragment newInstance(ColumnRepeatModel repeatModel) {
        ColumnRepeatViewFragment fragment = new ColumnRepeatViewFragment();
        fragment.repeatModel = repeatModel;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.column_repeat_layout, container, false);

        TextView txtRepeatGroupRequired = rootView.findViewById(R.id.txtRepeatGroupRequired);
        TextView txtRepeatGroupName = rootView.findViewById(R.id.txtRepeatGroupName);
        TextView txtGroupInnerName = rootView.findViewById(R.id.txtRepeatGroupInnerName);
        Button btAdd = rootView.findViewById(R.id.btAddRepeat);
        Button btRemoveLast = rootView.findViewById(R.id.btRemoveLastRepeat);

        if (repeatModel != null && repeatModel.getRepeatDefinition() != null) {

            setTextHtml(txtRepeatGroupName, repeatModel.getRepeatDefinition().getLabel());

            //txtGroupInnerName.setText(repeatModel.getRepeatDefinition().getLabel());
            
            // Only show delete button if there's more than one instance
            btRemoveLast.setVisibility(repeatModel.getCurrentInstanceCount() > 1 ? View.VISIBLE : View.GONE);
        }
        
        btAdd.setOnClickListener(v -> onAddClicked());
        btRemoveLast.setOnClickListener(v -> onRemoveLastClicked());

        return rootView;
    }

    private void refreshUI() {
        if (getView() != null && repeatModel != null) {
            Button btRemoveLast = getView().findViewById(R.id.btRemoveLastRepeat);
            if (btRemoveLast != null) {
                btRemoveLast.setVisibility(repeatModel.getCurrentInstanceCount() > 1 ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void onAddClicked() {
        FormFragment formFragment = (FormFragment) getParentFragment();
        if (formFragment != null && formFragment.getFormController() != null) {
            formFragment.getFormController().addRepeatInstance(repeatModel);
            
            // Slide back to the first group of the new instance
            List<ColumnGroupModel> instances = repeatModel.getInstanceModels();
            if (!instances.isEmpty()) {
                // Get the first group of the LAST instance (just added)
                int groupsPerInstance = repeatModel.getRepeatDefinition().getColumnsGroups().size();
                int firstGroupOfLastInstanceIndex = instances.size() - groupsPerInstance;
                ColumnGroupModel firstGroup = instances.get(firstGroupOfLastInstanceIndex);
                
                formFragment.getFormSlider().gotoPage(firstGroup);
            }
        }
    }

    private void onRemoveLastClicked() {
        FormFragment formFragment = (FormFragment) getParentFragment();
        if (formFragment != null && formFragment.getFormController() != null) {
            formFragment.getFormController().removeLastRepeatInstance(repeatModel);

            refreshUI();

            // 2. Force the ViewPager to stay on this specific anchor model
            // This recalculates the new index of the anchor and navigates to it instantly
            formFragment.getFormSlider().post(() -> {
                formFragment.getFormSlider().gotoPage(repeatModel);
            });
        }
    }

    protected void setTextHtml(TextView textView, String labelText) {
        ColumnModel model = repeatModel.getPrecedingColumnModel();
        setTextHtml(textView, labelText, model);
    }

    protected void setTextHtml(TextView textView, String labelText, ColumnModel model) {
        if (labelText != null) {
            if (model != null) {
                labelText = model.translateVariables(labelText);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(labelText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(labelText));
            }
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
