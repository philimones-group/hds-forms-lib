package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.ColumnType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Column {

    private String name;
    private ColumnType type;
    private Map<String, String> typeOptions; //if SELECT/MULTI_SELECT
    private String label;
    private String value;
    private Map<String, String> translatedLabel;
    private boolean required;
    private boolean readOnly;

    public Column() {
        this.typeOptions = new LinkedHashMap<>();
        this.translatedLabel = new HashMap<>();
    }

    public Column(String name, ColumnType type, Map<String, String> typeOptions, String label, String value, Map<String, String> translatedLabel, boolean required, boolean readOnly) {
        this();

        this.name = name;
        this.type = type;
        this.typeOptions = typeOptions;
        this.value = value;
        this.label = label;
        this.translatedLabel = translatedLabel;
        this.required = required;
        this.readOnly = readOnly;
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

    public Map<String, String> getTypeOptions() {
        return typeOptions;
    }

    public void setTypeOptions(Map<String, String> typeOptions) {
        if (typeOptions != null)
            this.typeOptions.putAll(typeOptions);
    }

    public void addTypeOptions(String optionValue, String optionLabel) {
        if (optionValue != null && optionLabel != null)
            this.typeOptions.put(optionValue, optionLabel);
    }

    public String getValue() {
        return value;
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

    public Map<String, String> getTranslatedLabel() {
        return translatedLabel;
    }

    public void setTranslatedLabel(Map<String, String> translatedLabel) {
        if (translatedLabel != null)
            this.translatedLabel.putAll(translatedLabel);
    }

    public void addLabel(String language, String labelText) {
        this.translatedLabel.put(language, labelText);
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
}
