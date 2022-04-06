package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.parsers.form.model.FormOptions;
import org.philimone.hds.forms.utilities.StringTools;

import java.util.LinkedHashMap;
import java.util.Map;

public class Column {

    private String name;
    private ColumnType type;
    private Map<String, FormOptions.OptionValue> typeOptions; //if SELECT/MULTI_SELECT
    private String label;
    private String value;
    private boolean required;
    private boolean readOnly;
    private boolean hidden;
    private String repeatCount;
    private String calculation;
    private String displayCondition; /* [#variable|value][><=!=][#variable|value]*/
    private String displayStyle; /* selected_only*/

    public static String DISPLAY_STYLE_SELECTED_ONLY = "selected_only";

    public Column() {
        this.typeOptions = new LinkedHashMap<>();
    }

    public Column(String name, ColumnType type, Map<String, FormOptions.OptionValue> typeOptions, String repeatCount, String label, String value, boolean required, boolean readOnly, String calculation, String displayCondition, String displayStyle) {
        this();

        this.name = name;
        this.type = type;
        this.typeOptions = typeOptions;
        this.repeatCount = repeatCount;
        this.value = value;
        this.label = label;
        this.required = required;
        this.readOnly = readOnly;
        this.calculation = calculation;
        this.displayCondition = displayCondition;
        this.displayStyle = displayStyle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public Map<String, FormOptions.OptionValue> getTypeOptions() {
        return typeOptions;
    }

    public void setTypeOptions(Map<String, FormOptions.OptionValue> typeOptions) {
        if (typeOptions != null)
            this.typeOptions.putAll(typeOptions);
    }

    public void addTypeOptions(String optionValue, String optionLabel, boolean optionReadonly) {
        if (optionValue != null && optionLabel != null)
            this.typeOptions.put(optionValue, new FormOptions.OptionValue(optionLabel, optionReadonly));
    }

    public String getValue() {
        return value;
    }

    public boolean isValueBlank() {
        return StringTools.isBlank(value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(String repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getCalculation() {
        return calculation;
    }

    public void setCalculation(String calculation) {
        this.calculation = calculation;
    }

    public String getDisplayCondition() {
        return displayCondition;
    }

    public void setDisplayCondition(String displayCondition) {
        this.displayCondition = displayCondition;
    }

    public String getDisplayStyle() {
        return displayStyle;
    }

    public void setDisplayStyle(String displayStyle) {
        this.displayStyle = displayStyle;
    }

    public void addDisplayCondition(String xdisplayCondition) {
        if (!StringTools.isBlank(xdisplayCondition)){
            if (StringTools.isBlank(this.displayCondition)){
                this.displayCondition = xdisplayCondition;
            } else {
                this.displayCondition = "(" + xdisplayCondition + ") and (" + this.displayCondition + ")";
            }
        }
    }
}
