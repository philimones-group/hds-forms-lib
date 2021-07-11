package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.ColumnType;

import java.util.ArrayList;
import java.util.List;

public class HForm {

    private String formId;
    private String formName;
    private List<ColumnGroup> columns;

    public HForm() {
        this.columns = new ArrayList<>();
    }

    public HForm(String formId, String formName) {
        this();
        this.formId = formId;
        this.formName = formName;

        initDefaultColumns();
    }

    private void initDefaultColumns() {
        //id, start, end, device_id

        Column column1 = new Column("id",        ColumnType.INSTANCE_UUID,   null, "", "", true, true);
        Column column2 = new Column("start",     ColumnType.START_TIMESTAMP, null, "", "", true, true);
        Column column3 = new Column("end",       ColumnType.END_TIMESTAMP,   null, "", "", true, true);
        Column column4 = new Column("device_id", ColumnType.DEVICE_ID,       null, "", "", true, true);

        ColumnGroup initialGroup = new ColumnGroup();
        initialGroup.addColumn(column1);
        initialGroup.addColumn(column2);
        initialGroup.addColumn(column3);
        initialGroup.addColumn(column4);

        addColumn(initialGroup);
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public List<ColumnGroup> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnGroup> columns) {
        this.columns.addAll(columns);
    }

    public void addColumn(ColumnGroup columnGroup){
        if (!this.columns.contains(columnGroup)){
            this.columns.add(columnGroup);
        }
    }

    public boolean hasHeader() {
        for (ColumnGroup cgroup : columns) {
            if (cgroup.isHeader()) return true;
        }
        return false;
    }

    public ColumnGroup getHeader() {
        for (ColumnGroup cgroup : columns) {
            if (cgroup.isHeader()) return cgroup;
        }
        return null;
    }
}
