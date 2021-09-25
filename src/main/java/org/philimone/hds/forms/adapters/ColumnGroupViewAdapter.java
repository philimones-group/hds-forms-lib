package org.philimone.hds.forms.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.widget.ColumnGroupView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ColumnGroupViewAdapter extends RecyclerView.Adapter<ColumnGroupViewHolder> {

    private List<ColumnGroupView> defaultFragments = new ArrayList<>();
    private List<ColumnGroupView> visibleFragments = new ArrayList<>();

    public ColumnGroupViewAdapter(List<ColumnGroupView> groups) {
        groups.forEach(columnGroupView -> {
            Log.d("columns", ""+columnGroupView);
            if (!columnGroupView.isHidden()) {
                this.defaultFragments.add(columnGroupView);
                this.visibleFragments.add(columnGroupView);
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
    public long getItemId(int position) {
        Log.d("getitem", ""+position);

        ColumnGroupView groupView = this.visibleFragments.get(position);
        return groupView.getItemId();
    }

    @Override
    public int getItemCount() {
        return visibleFragments.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ColumnGroupViewHolder holder, int position) {



        holder.removeViews();

        ColumnGroupView groupView = this.visibleFragments.get(position); //nextGroup

        Log.d("bind", ""+position+", "+groupView);

        if (groupView != null) {
            holder.setColumnGroupView(groupView);
        }

    }

    public ColumnGroupView getItemView(int position) {
        ColumnGroupView groupView = position < getItemCount() ? this.visibleFragments.get(position) : null;
        return groupView;
    }

    public int hidePage(int position, ColumnGroupView groupView){

        printFragments();

        if (groupView != null && groupView.isFragmentVisible()){
            removePage(groupView);
            groupView.setFragmentVisible(false);
            notifyItemRemoved(position);
        }

        printFragments();

        return Math.min(position+1, visibleFragments.size());
    }

    public int showPage(int position, ColumnGroupView groupView) {

        printFragments();

        if (groupView != null) {

            if (!groupView.isFragmentVisible()) {
                groupView.setFragmentVisible(true);

                if (!contains(this.visibleFragments, groupView)) {
                    Log.d("show-add", position+", "+groupView);
                    this.visibleFragments.add(position, groupView);
                }

                groupView.setFragmentVisible(true);
                notifyItemInserted(position);;

                printFragments();
            }
        }

        return position;
    }

    private void printFragments() {
        Log.d("frags", visibleFragments.stream().map(t -> t.toString()).collect(Collectors.joining(",")));
    }

    private boolean contains(List<ColumnGroupView> groupViews, ColumnGroupView groupView){
        for (ColumnGroupView fg: groupViews) {
            if (fg.equalsTo(groupView)){
                return true;
            }
        }

        return false;
    }

    private boolean contains(List<ColumnGroupView> fragments, long itemId){
        for (ColumnGroupView fg: fragments) {
            if (fg.getItemId()==itemId){
                return true;
            }
        }

        return false;
    }

    private void removePage(ColumnGroupView groupView) {
        if (groupView != null) {
            this.visibleFragments.remove(groupView);
            //fragment.setNewItemId();
        }
    }

    public List<ColumnGroupView> getDefaultFragments() {
        return defaultFragments;
    }

    public List<ColumnGroupView> getVisibleFragments() {
        return visibleFragments;
    }
}
