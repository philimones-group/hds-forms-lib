package org.philimone.hds.forms.model;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private boolean errors;
    private List<ColumnValue> columnValues;

    public ValidationResult(){
        this.columnValues = new ArrayList<>();
    }

    public ValidationResult(boolean hasErrors) {
        this();
        this.errors = hasErrors;
    }

    public ValidationResult(boolean hasErrors, List<ColumnValue> columnValues) {
        this(hasErrors);
        this.setColumnValues(columnValues);
    }

    public boolean hasErrors() {
        return errors;
    }

    public void setErrors(boolean hasErrors) {
        this.errors = errors;
    }

    public List<ColumnValue> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(List<ColumnValue> columnValues) {
        this.columnValues.addAll(columnValues);
    }

    public void addColumnWithError(ColumnValue columnValue) {
        this.columnValues.add(columnValue);
    }
}
