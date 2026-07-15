package org.philimone.hds.forms.adapters;

import android.util.Log;
import org.philimone.hds.forms.model.ColumnGroupModel;
import org.philimone.hds.forms.model.FormController;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ColumnGroupViewAdapter extends FragmentStateAdapter {

    private List<ColumnGroupModel> visibleModels = new ArrayList<>();
    private FormController formController;

    public ColumnGroupViewAdapter(Fragment fragment, FormController formController) {
        super(fragment);
        this.formController = formController;
        this.visibleModels.addAll(formController.getVisibleGroupModels());
    }

    public void refreshVisibleModels() {
        this.visibleModels.clear();
        this.visibleModels.addAll(formController.getVisibleGroupModels());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ColumnGroupModel groupModel = visibleModels.get(position);
        return ColumnGroupViewFragment.newInstance(groupModel);
    }

    @Override
    public long getItemId(int position) {
        return visibleModels.get(position).getUuid().hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (ColumnGroupModel model : visibleModels) {
            if (model.getUuid().hashCode() == itemId) return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return visibleModels.size();
    }

    public ColumnGroupModel getItemModel(int position) {
        return (position >= 0 && position < visibleModels.size()) ? visibleModels.get(position) : null;
    }

    public int getItemPosition(ColumnGroupModel model) {
        return visibleModels.indexOf(model);
    }

    public List<ColumnGroupModel> getVisibleModels() {
        return visibleModels;
    }
}
