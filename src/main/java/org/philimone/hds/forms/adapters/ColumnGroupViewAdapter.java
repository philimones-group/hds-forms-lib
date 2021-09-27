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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ColumnGroupViewAdapter extends FragmentStateAdapter {

    private List<ColumnGroupView> visibleFragments = new ArrayList<>();
    private List<ColumnGroupView> defaultFragments = new ArrayList<>();

    public ColumnGroupViewAdapter(Fragment fragment, List<ColumnGroupView> groups) {
        super(fragment);

        groups.forEach(columnGroupView -> {
            if (!columnGroupView.isHidden()) {

                defaultFragments.add(columnGroupView);

                columnGroupView.evaluateDisplayCondition();

                if (columnGroupView.isDisplayable()) { //only add previously visible items
                    this.visibleFragments.add(columnGroupView);
                }

            }
        });
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ColumnGroupView groupView = visibleFragments.get(position);
        return ColumnGroupViewFragment.newInstance(groupView);
    }

/*
    @NonNull
    @Override
    public ColumnGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.column_group_layout, parent, false);
        LinearLayout mainLayout = (LinearLayout) view;

        //add my saved view to parent
        return new ColumnGroupViewHolder(mainLayout);
    }*/

    @Override
    public long getItemId(int position) {
        //Log.d("getitem", ""+position);

        ColumnGroupView groupView = this.visibleFragments.get(position);
        return groupView.getItemId();
    }

    @Override
    public boolean containsItem(long itemId) {
        return contains(visibleFragments, itemId);
    }

    @Override
    public int getItemCount() {
        return visibleFragments.size();
    }
/*
    @Override
    public void onBindViewHolder(@NonNull ColumnGroupViewHolder holder, int position) {

        holder.removeViews();

        ColumnGroupView groupView = this.visibleFragments.get(position); //nextGroup

        Log.d("bind", ""+position+", "+groupView);

        //if (groupView != null) {
            holder.setColumnGroupView(groupView);
        //}

    }*/

    public ColumnGroupView getItemView(int position) {
        ColumnGroupView groupView = position < getItemCount() ? this.visibleFragments.get(position) : null;
        return groupView;
    }

    public void hidePage(ColumnGroupView groupView){

        //printFragments();

        if (groupView != null){// && groupView.isFragmentVisible()){
            removePage(groupView);
            groupView.setFragmentVisible(false);
            notifyDataSetChanged();
        }

        printFragments();
    }

    public int showPage(int position, ColumnGroupView groupView) {

        //printFragments();

        if (groupView != null) {

            //if (!groupView.isFragmentVisible()) {
            if (!contains(this.visibleFragments, groupView)) {

                groupView.setFragmentVisible(true);

                Log.d("show-add", position+", "+groupView);
                this.visibleFragments.add(position, groupView);

                groupView.setFragmentVisible(true);
                notifyDataSetChanged();

                printFragments();
            }
        }

        return position;
    }

    public int getCorrectPosition(ColumnGroupView groupView) {
        //get where item is in visibleFragments or where it should be

        int index = this.visibleFragments.indexOf(groupView);
        if (index != -1) return index;

        //get possible position (get next left visible fg and next right visible fg)

        //get left
        ColumnGroupView leftgv = groupView.getParentGroupView();
        ColumnGroupView rightgv = groupView.getNextGroupView();

        while (leftgv != null) {
            if (this.visibleFragments.indexOf(leftgv) != -1){ //exists on visible fragments
                break;
            }

            leftgv = leftgv.getParentGroupView();
        }

        while (rightgv != null) {
            if (this.visibleFragments.indexOf(rightgv) != -1){ //exists
                break;
            }

            rightgv = rightgv.getNextGroupView();
        }

        if (leftgv == null && rightgv == null) {
            return 0;
        }

        if (leftgv != null){
            return this.visibleFragments.indexOf(leftgv)+1;
        }

        if (rightgv != null) {
            return this.visibleFragments.indexOf(rightgv);
        }

        return 0;
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

    public List<ColumnGroupView> getVisibleFragments() {
        return visibleFragments;
    }

    public List<ColumnGroupView> getDefaultFragments() {
        return this.defaultFragments;
    }
}
