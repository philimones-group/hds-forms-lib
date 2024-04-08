package org.philimone.hds.forms.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.widget.ColumnView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnViewDataAdapter extends ArrayAdapter<ColumnView> {
    private Context mContext;
    private List<ColumnView> columnsList = new ArrayList<>();

    public ColumnViewDataAdapter(@NonNull Context context, List<ColumnView> columnViewList) {
        super(context, R.layout.resume_column_item);
        this.mContext = context;
        this.columnsList.addAll(columnViewList);
    }

    @Nullable
    @Override
    public ColumnView getItem(int position) {
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

        ColumnView cview = getItem(position);

        if (cview != null) {
            txtLabel.setText(cview.getLabel());
            txtValue.setText(cview.getColumnValue().getValueLabel());
        }

        return mainView;
    }


}
