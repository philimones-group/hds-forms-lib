package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.RepeatCountType;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Repeat Group anchor/generator.
 * This model does not contain fields itself but manages the dynamic creation
 * of ColumnGroupModel instances for each iteration of the repeat.
 */
public class ColumnRepeatModel extends ColumnGroupModel {

    private ColumnRepeatGroup repeatDefinition;
    private RepeatCountType repeatCountType;
    private int currentInstanceCount = 0;
    private List<ColumnGroupModel> instanceModels = new ArrayList<>();

    public ColumnRepeatModel(ColumnRepeatGroup repeatDefinition, RepeatCountType repeatCountType) {
        super(repeatDefinition); // Empty group as base
        this.repeatGroup = repeatDefinition;
        this.repeatDefinition = repeatDefinition;
        this.repeatCountType = repeatCountType;
        this.setHidden(true);
        this.setDisplayable(false);
    }

    public ColumnRepeatGroup getRepeatDefinition() {
        return repeatDefinition;
    }

    public RepeatCountType getRepeatCountType() {
        return repeatCountType;
    }

    public int getCurrentInstanceCount() {
        return currentInstanceCount;
    }

    public void setCurrentInstanceCount(int count) {
        this.currentInstanceCount = count;
    }

    public List<ColumnGroupModel> getInstanceModels() {
        return instanceModels;
    }

    public void addInstanceModel(ColumnGroupModel model) {
        this.instanceModels.add(model);
    }

    public void removeInstanceModel(ColumnGroupModel model) {
        this.instanceModels.remove(model);
    }

    public void clearInstanceModels() {
        this.instanceModels.clear();
    }
}
