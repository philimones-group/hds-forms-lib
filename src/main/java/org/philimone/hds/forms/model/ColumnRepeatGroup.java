package org.philimone.hds.forms.model;

import java.util.ArrayList;
import java.util.List;

public class ColumnRepeatGroup extends ColumnGroup {
    private List<ColumnGroup> columnsGroups;
    private String repeatCount;
    private String displayCondition;

    public ColumnRepeatGroup(){
        super();
        this.columnsGroups = new ArrayList<>();
    }

    @Override
    public void setHeader(boolean header) {
        super.setHeader(false);
    }

    public String getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(String repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getDisplayCondition() {
        return displayCondition;
    }

    public void setDisplayCondition(String displayCondition) {
        this.displayCondition = displayCondition;
    }

    public List<ColumnGroup> getColumnsGroups() {
        return columnsGroups;
    }

    public void setColumnsGroups(List<ColumnGroup> columnsGroups) {
        this.columnsGroups.addAll(columnsGroups);
    }

    public void addColumn(ColumnGroup columnGroup){
        this.columnsGroups.add(columnGroup);
    }
}
