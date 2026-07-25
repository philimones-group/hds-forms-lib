package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.model.enums.ColumnValueStatus;
import org.philimone.hds.forms.model.parsers.form.model.FormOptions;
import org.philimone.hds.forms.model.utilities.GpsFormatter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mz.betainteractive.utilities.StringUtil;

/**
 * Represents the runtime state of a Column (form field).
 * Decoupled from the UI (View) to improve memory efficiency and allow logic evaluation
 * without inflating layouts.
 */
public class ColumnModel implements Serializable {

    public static final String DELIMITER = ",";

    private Column column;
    private ColumnGroupModel parentGroupModel;
    private ColumnModel previousModel;

    private String value;
    private ColumnValueStatus valueStatus;
    private boolean displayable = true;
    private boolean readOnly;
    private boolean required;
    private boolean valid = true;
    private String resolvedValidationMessage;

    // Helper for GPS and Multi-select which have complex values
    private Map<String, Double> gpsValues = new LinkedHashMap<>();
    private List<String> multiSelectValues = new ArrayList<>();

    public ColumnModel(Column column, ColumnGroupModel parentGroupModel) {
        this.column = column;
        this.parentGroupModel = parentGroupModel;
        this.value = column.getValue();
        this.valueStatus = ColumnValueStatus.FROM_XLS; // Default
        this.readOnly = column.isReadOnly();
        this.required = column.isRequired();
    }

    public Column getColumn() {
        return column;
    }

    public String getName() {
        return column.getName();
    }

    public ColumnType getType() {
        return column.getType();
    }

    public ColumnGroupModel getParentGroupModel() {
        return parentGroupModel;
    }

    public ColumnModel getPreviousModel() {
        return previousModel;
    }

    public void setPreviousModel(ColumnModel previousModel) {
        this.previousModel = previousModel;
    }

    public String getValue() {
        return value;
    }

    public boolean isValueBlank() {
        return value == null || value.trim().isEmpty();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(String value, ColumnValueStatus status) {
        this.value = value;
        this.valueStatus = status;
    }

    public ColumnValueStatus getValueStatus() {
        return valueStatus;
    }

    public void setValueStatus(ColumnValueStatus valueStatus) {
        this.valueStatus = valueStatus;
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public void setDisplayable(boolean displayable) {
        this.displayable = displayable;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getResolvedValidationMessage() {
        return resolvedValidationMessage;
    }

    public void setResolvedValidationMessage(String resolvedValidationMessage) {
        this.resolvedValidationMessage = resolvedValidationMessage;
    }

    public Map<String, Double> getGpsValues() {
        return gpsValues;
    }

    public void setGpsValues(Map<String, Double> gpsValues) {
        this.gpsValues = gpsValues;
        this.value = new GpsFormatter(getName(), gpsValues).format();
    }

    public List<String> getMultiSelectValues() {
        return multiSelectValues;
    }

    public void setMultiSelectValues(List<String> multiSelectValues) {
        this.multiSelectValues = multiSelectValues;
    }

    @Override
    public String toString() {
        return "ColumnModel{" + getName() + ", value='" + value + "'}";
    }

    public Integer getIntegerValue() {
        try {
            return value == null ? null : Integer.parseInt(value);
        } catch (Exception ignored) {}
        return null;
    }

    public BigDecimal getDecimalValue() {
        try {
            return new BigDecimal(value);
        } catch (Exception ignored) {}
        return null;
    }

    public String getSelectedValueLabel() {
        FormOptions.OptionValue option = column.getTypeOptions().get(value);
        if (option != null) {
            return translateVariables(option.label);
        }

        return value;
    }

    public String getSelectedValuesLabels() {
        StringBuilder sb = new StringBuilder();
        String[] values = value == null ? new String[0] : value.split(DELIMITER);

        for (int j = 0; j < values.length; j++) {
            String val = values[j];
            FormOptions.OptionValue option = column.getTypeOptions().get(val);
            String label = option != null ? translateVariables(option.label) : val;
            sb.append(label);
            if (j < values.length - 1) sb.append(", ");
        }

        return sb.toString();
    }

    public String translateVariables(String text) {
        if (StringUtil.isBlank(text) || !text.contains("${")) return text;

        ColumnModel current = this;
        while (current != null) {
            String name = current.getName();
            String value = current.isDisplayable() ? current.getValue() : "";
            if (value == null) value = "";

            text = text.replace("${" + name + "}", value);
            current = current.getPreviousModel();
        }

        return text;
    }

}
