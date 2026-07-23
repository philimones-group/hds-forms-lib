package org.philimone.hds.forms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.DateUtil;

/**
 * Represents the runtime state of a ColumnGroup.
 * For Repeat Groups, each instance of a repeat will have its own ColumnGroupModel.
 */
public class ColumnGroupModel implements Serializable {

    private String uuid;
    private ColumnGroup columnGroup;
    private ColumnGroupModel previousGroupModel;
    protected ColumnRepeatGroup repeatGroup; // Reference if this belongs to a repeat group
    private Integer repeatIndex;           // Index of the repeat (0, 1, 2...)
    private Integer repeatSize;            // Total number of repeats at creation time

    private List<ColumnModel> columnModels = new ArrayList<>();

    private boolean displayable = true;
    private boolean hidden;
    private boolean header;

    private DateUtil.SupportedCalendar supportedCalendar;

    public ColumnGroupModel(ColumnGroup columnGroup) {
        this.columnGroup = columnGroup;
        this.uuid = java.util.UUID.randomUUID().toString();
    }

    public ColumnGroupModel(ColumnRepeatGroup repeatGroup, ColumnGroup columnGroup, Integer repeatIndex, Integer repeatSize) {
        this(columnGroup);
        this.repeatGroup = repeatGroup;
        this.repeatIndex = repeatIndex;
        this.repeatSize = repeatSize;
    }

    public String getUuid() {
        return uuid;
    }

    public ColumnGroup getColumnGroup() {
        return columnGroup;
    }

    public ColumnGroupModel getPreviousGroupModel() {
        return previousGroupModel;
    }

    public void setPreviousGroupModel(ColumnGroupModel previousGroupModel) {
        this.previousGroupModel = previousGroupModel;
    }

    public ColumnRepeatGroup getRepeatGroup() {
        return repeatGroup;
    }

    public boolean isRepeatItem() {
        return repeatGroup != null && repeatIndex != null;
    }

    public Integer getRepeatIndex() {
        return repeatIndex;
    }

    public void setRepeatIndex(Integer repeatIndex) {
        this.repeatIndex = repeatIndex;
    }

    public Integer getRepeatSize() {
        return repeatSize;
    }

    public void setRepeatSize(Integer repeatSize) {
        this.repeatSize = repeatSize;
    }

    public List<ColumnModel> getColumnModels() {
        return columnModels;
    }

    public void addColumnModel(ColumnModel columnModel) {
        this.columnModels.add(columnModel);
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public void setDisplayable(boolean displayable) {
        this.displayable = displayable;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public String getName() {
        return this.columnGroup.getName();
    }

    public DateUtil.SupportedCalendar getSupportedCalendar() {
        return supportedCalendar;
    }

    public void setSupportedCalendar(DateUtil.SupportedCalendar supportedCalendar) {
        this.supportedCalendar = supportedCalendar;
    }

    public ColumnModel getPrecedingColumnModel() {
        if (!columnModels.isEmpty()) {
            return columnModels.get(0).getPreviousModel();
        }

        ColumnGroupModel prevGroup = previousGroupModel;
        while (prevGroup != null && prevGroup.getColumnModels().isEmpty()) {
            prevGroup = prevGroup.getPreviousGroupModel();
        }

        if (prevGroup != null) {
            List<ColumnModel> prevColumns = prevGroup.getColumnModels();
            return prevColumns.get(prevColumns.size() - 1);
        }

        return null;
    }

    @Override
    public String toString() {
        return "ColumnGroupModel{" + (isRepeatItem() ? repeatGroup.getName() + "[" + repeatIndex + "]" : columnGroup.getName()) + "}";
    }
}
