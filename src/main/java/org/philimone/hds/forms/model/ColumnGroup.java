package org.philimone.hds.forms.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ColumnGroup {

    private String name;
    private String label;
    private Map<String, String> translatedLabel;
    private List<Column> columns;
    private boolean header;

    public ColumnGroup() {
        this.columns = new ArrayList<>();
        this.translatedLabel = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        this.translatedLabel = translatedLabel;
    }

    public void addLabel(String language, String labelText) {
        this.translatedLabel.put(language, labelText);
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }
}
