package org.philimone.hds.forms.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.philimone.hds.forms.widget.ColumnGroupView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ColumnGroupViewHolder extends RecyclerView.ViewHolder {

    private ColumnGroupView columnGroupView;
    private ViewGroup mainView;

    public ColumnGroupViewHolder(@NonNull View itemView) {
        super(itemView);

        this.mainView = (ViewGroup) itemView;
    }

    public void setColumnGroupView(ColumnGroupView columnGroupView) {
        this.columnGroupView = columnGroupView;

        if (columnGroupView.getParent() == null) {
            this.mainView.addView(columnGroupView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
    }

    public void removeViews() {
        mainView.removeAllViews();
    }
}
