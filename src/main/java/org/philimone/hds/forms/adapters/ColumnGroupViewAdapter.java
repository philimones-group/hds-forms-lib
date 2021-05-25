package org.philimone.hds.forms.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.widget.ColumnGroupView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ColumnGroupViewAdapter extends RecyclerView.Adapter<ColumnGroupViewHolder> {

    private List<ColumnGroupView> columnGroupList = new ArrayList<>();

    public ColumnGroupViewAdapter(List<ColumnGroupView> groups) {
        groups.forEach(columnGroupView -> {
            if (!columnGroupView.isHidden()) {
                this.columnGroupList.add(columnGroupView);
            }
        });

    }

    @NonNull
    @Override
    public ColumnGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.column_group_layout, parent, false);
        LinearLayout mainLayout = (LinearLayout) view;

        //add my saved view to parent
        return new ColumnGroupViewHolder(mainLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ColumnGroupViewHolder holder, int position) {

        holder.removeViews();

        ColumnGroupView columnGroupView = columnGroupList.get(position);
        if (columnGroupView != null) {
            holder.setColumnGroupView(columnGroupView);
        }


    }

    @Override
    public int getItemCount() {
        return columnGroupList.size();
    }
}
