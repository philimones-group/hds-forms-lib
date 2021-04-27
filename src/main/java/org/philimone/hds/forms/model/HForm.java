package org.philimone.hds.forms.model;

import java.util.List;

public class HForm {

    private String formId;
    private String formName;
    private List<ColumnGroup> columns;

    public HForm(String formId, String formName) {
        this.formId = formId;
        this.formName = formName;
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
        this.columns.add(columnGroup);
    }

    public boolean hasHeader() {
        for (ColumnGroup cgroup : columns) {
            if (cgroup.isHeader()) return true;
        }
        return false;
    }


}
