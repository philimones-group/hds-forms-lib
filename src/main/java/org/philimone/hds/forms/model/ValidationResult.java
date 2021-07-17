package org.philimone.hds.forms.model;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private boolean errors;
    private List<Error> columnErrors;

    public ValidationResult(){
        this.columnErrors = new ArrayList<>();
    }

    public static ValidationResult noErrors(){
        return new ValidationResult();
    }

    public ValidationResult(ColumnValue columnValue, String errorMessage) {
        this();
        this.addError(columnValue, errorMessage);
    }

    public boolean hasErrors() {
        return errors;
    }

    public List<Error> getColumnErrors(){
        return this.columnErrors;
    }

    public void addError(ColumnValue columnValue, String errorMessage) {
        this.columnErrors.add(new Error(columnValue, errorMessage));
        this.errors = true;
    }

    public class Error {
        public ColumnValue columnValue;
        public String errorMessage;

        public Error(ColumnValue columnValue, String errorMessage) {
            this.columnValue = columnValue;
            this.errorMessage = errorMessage;
        }
    }
}
