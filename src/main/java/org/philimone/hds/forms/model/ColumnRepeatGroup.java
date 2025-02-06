package org.philimone.hds.forms.model;

import android.util.Log;

import org.philimone.hds.forms.model.enums.RepeatCountType;
import org.philimone.hds.forms.utilities.StringTools;

import java.util.ArrayList;
import java.util.List;

public class ColumnRepeatGroup extends ColumnGroup {
    private List<ColumnGroup> columnsGroups;
    private String repeatCount;
    private String displayCondition;
    private String groupName;
    private String nodeName;

    public ColumnRepeatGroup(String groupName, String nodeName){
        super();
        this.columnsGroups = new ArrayList<>();
        this.groupName = groupName;
        this.nodeName = nodeName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getNodeName() {
        return nodeName;
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
        if (!columnsGroups.contains(columnGroup)) {
            this.columnsGroups.add(columnGroup);
            columnGroup.addDisplayCondition(displayCondition);
        }
    }

    public RepeatCountType getRepeatCountType(){
        String rexVar = "^\\$\\{\\w+\\}";
        String rexExtVar = "^\\$\\w+";
        String rexCons = "[0-9]+";

        if (repeatCount.matches(rexExtVar)){
            return RepeatCountType.EXTERNAL_LOADER;
        }

        if (repeatCount.matches(rexCons)){
            return RepeatCountType.CONSTANT_VALUE;
        }


        return RepeatCountType.VARIABLE; //NEEDS EXPRESSION CALCULATION
    }

    public Integer getRepeatSize(PreloadMap preloadedColumnValues) {
        RepeatCountType type = getRepeatCountType();

        if (type == RepeatCountType.VARIABLE){
            return null;
        }

        if (type == RepeatCountType.CONSTANT_VALUE){
            try {
                return Integer.parseInt(repeatCount);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        if (type == RepeatCountType.EXTERNAL_LOADER){
            try {
                RepeatObject repeatObject = preloadedColumnValues.getRepeatObject(this.getName());
                if (repeatObject != null) {
                    return repeatObject.count();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        return 0;
    }
}
