package org.philimone.hds.forms.adapters;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.ColumnValue;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import mz.betainteractive.utilities.StringUtil;

public class ColumnViewDataAdapter extends ArrayAdapter<ColumnModel> {
    private Context mContext;
    private List<ColumnModel> columnsList = new ArrayList<>();

    public ColumnViewDataAdapter(@NonNull Context context, List<ColumnModel> columnModelList) {
        super(context, R.layout.resume_column_item);
        this.mContext = context;
        this.columnsList.addAll(columnModelList);
    }

    @Nullable
    @Override
    public ColumnModel getItem(int position) {
        return this.columnsList.get(position);
    }

    @Override
    public int getCount() {
        return this.columnsList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View mainView = inflater.inflate(R.layout.resume_column_item, parent, false);

        TextView txtLabel = mainView.findViewById(R.id.txtItem1);
        TextView txtValue = mainView.findViewById(R.id.txtItem2);

        ColumnModel model = getItem(position);

        if (model != null) {
            setTextHtml(txtLabel, model);
            // Using ColumnValue as a helper to get formatted labels
            ColumnValue cv = new ColumnValue(model.getParentGroupModel(), model);
            txtValue.setText(cv.getValueLabel() != null ? cv.getValueLabel() : model.getValue());
        }

        return mainView;
    }

    private void setTextHtml(TextView textView, ColumnModel model) {
        //we dont use HTML here is a plain text
        String labelText = model.getColumn().getLabel();
        if (labelText != null) {
            labelText = translateVariables(labelText, model);
            textView.setText(Html.fromHtml(labelText).toString()); //remove html tags if exists
        }
    }

    private String translateVariables(String text, ColumnModel columnModel) {
        if (StringUtil.isBlank(text) || !text.contains("${")) return text;

        ColumnModel parent = columnModel.getPreviousModel();
        while (parent != null) {
            String name = parent.getName();
            String value = parent.isDisplayable() ? parent.getValue() : "";
            if (value == null) value = "";

            text = text.replace("${" + name + "}", value);
            parent = parent.getPreviousModel();
        }

        return text;
    }
}
