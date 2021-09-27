package org.philimone.hds.forms.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.widget.ColumnGroupView;

import androidx.fragment.app.Fragment;

public class ColumnGroupViewFragment extends Fragment {

    private static long ITEM_ID_COUNT;
    private long itemId;
    private ColumnGroupView groupView;

    public ColumnGroupViewFragment() {
        // Required empty public constructor
        this.itemId = ITEM_ID_COUNT++;
    }

    public static ColumnGroupViewFragment newInstance(ColumnGroupView groupView) {
        ColumnGroupViewFragment fragment = new ColumnGroupViewFragment();
        fragment.groupView = groupView;
        return fragment;
    }

    public long getItemId() {
        return groupView.getItemId();
    }

    public long setNewItemId() {
        this.itemId = itemId+100;
        return this.itemId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup mainView = (ViewGroup) inflater.inflate(R.layout.column_group_layout, container, false);

        removeViews(this.groupView.getParent());
        mainView.addView(this.groupView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        return mainView;
    }

    public void removeViews(ViewParent viewParent) {
        if (viewParent != null && viewParent instanceof ViewGroup) {
            ((ViewGroup) viewParent).removeAllViews();
        }
    }

    public ColumnGroupView getGroupView() {
        return groupView;
    }
}
