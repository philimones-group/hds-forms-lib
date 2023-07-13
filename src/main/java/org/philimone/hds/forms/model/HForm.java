package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.ColumnType;

import java.util.ArrayList;
import java.util.List;

public class HForm {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_END = "end";
    public static final String COLUMN_DEVICE_ID = "device_id";
    public static final String COLUMN_POST_EXECUTION = "postExecution";

    private String formId;
    private String formName;
    private List<ColumnGroup> columns;
    private boolean postExecution;

    public HForm() {
        this.columns = new ArrayList<>();
    }

    public HForm(String formId, String formName) {
        this();
        this.formId = formId;
        this.formName = formName;

        initDefaultColumns();
    }

    public HForm(String formId, String formName, boolean postExecution) {
        this();
        this.formId = formId;
        this.formName = formName;
        this.postExecution = postExecution;

        initDefaultColumns();
    }

    private void initDefaultColumns() {
        //id, start, end, device_id

        Column column1 = new Column(COLUMN_ID,        ColumnType.INSTANCE_UUID,   null, "", "", "", true, true, "", null, "", true);
        Column column2 = new Column(COLUMN_START,     ColumnType.START_TIMESTAMP, null, "", "", "", true, true, "", null, "", true);
        Column column3 = new Column(COLUMN_END,       ColumnType.END_TIMESTAMP,   null, "", "", "", true, true, "", null, "", true);
        Column column4 = new Column(COLUMN_DEVICE_ID, ColumnType.DEVICE_ID,       null, "", "", "", true, true, "", null, "", true);
        Column column5 = new Column(COLUMN_POST_EXECUTION, ColumnType.EXECUTION_STATUS,   null, "", "", postExecution+"", true, true, "", null, "", true);

        ColumnGroup initialGroup = new ColumnGroup();
        initialGroup.addColumn(column1);
        initialGroup.addColumn(column2);
        initialGroup.addColumn(column3);
        initialGroup.addColumn(column4);
        initialGroup.addColumn(column5);

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

    public boolean isPostExecution() {
        return postExecution;
    }

    public void setPostExecution(boolean postExecution) {
        this.postExecution = postExecution;

        ColumnGroup initialGroup = columns.get(0);
        Column colPostExec = (initialGroup != null) ? initialGroup.getColumns().stream().filter(t -> t.getName().equals(COLUMN_POST_EXECUTION)).findFirst().orElse(null) : null;

        if (colPostExec != null) {
            colPostExec.setValue(postExecution+"");
        }
    }

    public boolean isRepeatColumnName(String columnName) {
        for (ColumnGroup columnGroup : this.columns){
            String cName = columnGroup.getName();

            if (cName != null && columnName != null && columnGroup.getName().equals(columnName) && columnGroup instanceof ColumnRepeatGroup) return true;
        }
        return false;
    }

    public Column getColumn(String columnName) {
        for (ColumnGroup columnGroup : this.columns){
            Column column = columnGroup.getColumn(columnName);

            if (column != null) return column;
        }

        return null;
    }

    public void setReadonly(boolean value) {
        for (ColumnGroup columnGroup : this.columns){
            for (Column column : columnGroup.getColumns()) {
                column.setReadOnly(!column.isHidden() && value);
            }
        }
    }
}
