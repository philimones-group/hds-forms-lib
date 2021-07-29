package org.philimone.hds.forms.adapters;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.philimone.hds.forms.widget.ColumnGroupView;
import org.philimone.hds.forms.widget.FormColumnSlider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

public class ColumnGroupViewPageAdapter extends FragmentStateAdapter {

    private List<ColumnGroupView> defaultColumnGroupList = new ArrayList<>();
    private List<ColumnGroupViewFragment> defaultFragments = new ArrayList<>();
    private List<ColumnGroupViewFragment> visibleFragments = new ArrayList<>();

    private FormColumnSlider.SlideDirection direction;

    public ColumnGroupViewPageAdapter(@NonNull Fragment fragment, List<ColumnGroupView> groups) {
        super(fragment);

        groups.forEach(columnGroupView -> {
            if (!columnGroupView.isHidden()) {
                ColumnGroupViewFragment fg = ColumnGroupViewFragment.newInstance(columnGroupView);

                this.defaultColumnGroupList.add(columnGroupView);
                defaultFragments.add(fg);
                visibleFragments.add(fg);
            }
        });

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("create", position+", "+this.visibleFragments.get(position).getGroupView());
        return this.visibleFragments.get(position);
    }

    @Override
    public long getItemId(int position) {
        Log.d("item-id", position+"");
        return this.visibleFragments.get(position).getItemId();
    }

    @Override
    public boolean containsItem(long itemId) {
        return contains(this.visibleFragments, itemId);
    }

    @Override
    public int getItemCount() {
        //Log.d("page-count", ""+this.visibleFragments.size());
        return this.visibleFragments.size();
    }

    public ColumnGroupView getItemView(int position) {
        ColumnGroupViewFragment fragment = position < getItemCount() ? this.visibleFragments.get(position) : null;
        return fragment!=null ? fragment.getGroupView() : null;
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

            ColumnGroupViewFragment fragment = getFragment(groupView);

            if (!groupView.isFragmentVisible()) {
                groupView.setFragmentVisible(true);

                if (!contains(this.visibleFragments, groupView)) {
                    Log.d("show-add", position+", "+groupView);
                    this.visibleFragments.add(position, fragment);
                }

                groupView.setFragmentVisible(true);
                notifyItemInserted(position);;

                printFragments();
            }
        }

        return position;
    }

    private void printFragments() {
        Log.d("frags", visibleFragments.stream().map(t -> t.getGroupView().toString()).collect(Collectors.joining(",")));
    }

    private boolean contains(List<ColumnGroupViewFragment> fragments, ColumnGroupView groupView){
        for (ColumnGroupViewFragment fg: fragments) {
            if (fg.getGroupView().equalsTo(groupView)){
                return true;
            }
        }

        return false;
    }

    private boolean contains(List<ColumnGroupViewFragment> fragments, long itemId){
        for (ColumnGroupViewFragment fg: fragments) {
            if (fg.getItemId()==itemId){
                return true;
            }
        }

        return false;
    }

    private ColumnGroupViewFragment getFragment(ColumnGroupView groupView) {
        for (ColumnGroupViewFragment fg: this.defaultFragments) {
            if (fg.getGroupView().equalsTo(groupView)){
                return fg;
            }
        }

        return null;
    }

    private void removePage(ColumnGroupView groupView) {
        ColumnGroupViewFragment fragment = getFragment(groupView);

        if (fragment != null) {
            this.visibleFragments.remove(fragment);
            //fragment.setNewItemId();
        }
    }

}

